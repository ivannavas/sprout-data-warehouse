package io.github.ivannavas.sprout_data_warehouse.agent;

import io.github.ivannavas.sprout.annotation.Agent;
import io.github.ivannavas.sprout.annotation.Tool;
import io.github.ivannavas.sprout.annotation.ToolParam;
import io.github.ivannavas.sprout.anthropic.embedding.VoyageEmbeddingModel;
import io.github.ivannavas.sprout.anthropic.executor.AnthropicModelExecutor;
import io.github.ivannavas.sprout.model.AgentResult;
import io.github.ivannavas.sprout.model.StreamListener;
import io.github.ivannavas.sprout.pgvector.PgVectorStore;
import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;
import io.github.ivannavas.sprout_data_warehouse.service.ActivityService;
import io.github.ivannavas.sprout_data_warehouse.service.DaySummaryService;
import io.github.ivannavas.sprout_data_warehouse.service.EventService;
import io.github.ivannavas.sprout_data_warehouse.service.FeelingService;
import io.github.ivannavas.sprout_data_warehouse.service.PersonService;
import io.github.ivannavas.sprout_data_warehouse.service.ProjectService;
import io.github.ivannavas.sprout_data_warehouse.tool.DatabaseReader;

import java.time.LocalDate;
import java.util.List;

/**
 * The read side of the warehouse turned into a conversation: answers questions about the diary from
 * the two things the ingestion leaves behind.
 *
 * <p>Those two are complementary, which is why the agent is given both. The structured tables it
 * inherits from {@link DatabaseReader} answer what can be counted or listed — who someone is, when a
 * project ran, which days an activity happened — but they hold only what the extractor decided was
 * worth a row. The transcriptions indexed in pgvector keep the day as it was actually told, so RAG
 * covers what the extraction dropped: the wording, the reasoning, the asides. Retrieval happens
 * before every turn on the question as asked, so the agent starts each answer already holding the
 * days that sound like it, and reaches for the tools when the answer needs precision instead.
 *
 * <p>It extends {@code DatabaseReader} rather than {@code DatabaseWriter} on purpose: asking about
 * the diary must not be able to rewrite it.
 */
@Agent(
        model = AnthropicModelExecutor.class,
        vectorStore = PgVectorStore.class,
        embeddingModel = VoyageEmbeddingModel.class,
        retrievalTopK = 5,
        maxIterations = 15,
        systemPrompt = """
                Eres mi asistente personal y conoces mi diario. Yo te hago preguntas sobre mi propia vida y tú
                las respondes apoyándote ÚNICAMENTE en lo que hay registrado.

                DE DÓNDE SACAS LA INFORMACIÓN (tienes dos fuentes y se complementan):
                1. El contexto recuperado que acompaña a mi pregunta: fragmentos de las transcripciones originales
                   de mis días, tal y como los conté. Ahí está el detalle, los matices y todo lo que no cabe en una
                   tabla.
                2. Las herramientas de base de datos: los datos ya estructurados que se extrajeron de esas
                   transcripciones (personas, sentimientos, actividades, eventos, proyectos, resúmenes de días).

                CUÁNDO USAR CADA UNA:
                - Si la pregunta va de contar, listar, ordenar o cruzar datos ("cuántas veces", "qué días",
                  "desde cuándo", "quién es"), usa las herramientas: son la fuente exacta.
                - Si la pregunta va de cómo fue algo, cómo me sentí o qué pasó realmente, apóyate en el contexto
                  recuperado y en findDaySummaryByDate, que es donde está el relato.
                - Lo normal es combinar ambas: localiza las fechas con las herramientas y cuenta lo que pasó con
                  las transcripciones y los resúmenes.
                - El contexto recuperado se elige por parecido con la pregunta, así que puede traer días que no
                  vengan a cuento. Descarta sin más lo que no encaje; no fuerces una respuesta con ello.

                REGLAS:
                - No inventes NADA. Si algo no está registrado, dilo con naturalidad ("no lo tengo apuntado",
                  "de eso no hay nada en el diario") en lugar de deducirlo o rellenarlo.
                - Distingue siempre lo que está registrado de lo que es interpretación tuya. Si te pido una
                  valoración o un patrón, dala, pero deja claro en qué registros te apoyas.
                - Cita las fechas concretas de lo que cuentas. Son la referencia con la que yo me sitúo.
                - Antes de decir que algo no existe, búscalo de verdad con las herramientas: que no aparezca en el
                  contexto recuperado no significa que no esté en la base de datos.
                - Las fechas se manejan en formato ISO yyyy-MM-dd. Tu referencia temporal es la fecha de hoy que
                  te llega al principio de cada pregunta.
                - Responde en español, en prosa, directo y sin florituras. Nada de enumerar todo lo que has
                  consultado: responde a lo que te he preguntado.
                """
)
public class ConversationalAgent extends DatabaseReader {

    public ConversationalAgent(PersonService personService,
                               FeelingService feelingService,
                               ActivityService activityService,
                               EventService eventService,
                               ProjectService projectService,
                               DaySummaryService daySummaryService) {
        super(personService, feelingService, activityService, eventService, projectService, daySummaryService);
    }

    /**
     * Stamps the question with the date it was asked before handing it to the loop. The system prompt is
     * fixed at build time, so without this the model has no idea when "hoy" is and cannot resolve the
     * relative dates a question is normally phrased with ("la semana pasada", "el lunes").
     */
    @Override
    public AgentResult execute(String conversationId, String prompt) {
        return super.execute(conversationId, withToday(prompt));
    }

    @Override
    public void executeStream(String conversationId, String prompt, StreamListener listener) {
        super.executeStream(conversationId, withToday(prompt), listener);
    }

    private static String withToday(String prompt) {
        return "HOY ES: " + LocalDate.now() + System.lineSeparator() + System.lineSeparator() + prompt;
    }

    /**
     * Kept here rather than on {@code DatabaseReader} because the extraction agent has no use for it —
     * it writes the summary of the day it is on and never reads one back — and every tool added there
     * enlarges the schema block it resends on each of its iterations.
     */
    @Tool(name = "findDaySummaryByDate", description = "Obtiene el resumen de lo que ocurrió en una fecha concreta")
    public DaySummary findDaySummaryByDate(
            @ToolParam(description = "Fecha del día a consultar, en formato yyyy-MM-dd", required = true) String date) {
        return daySummaryService.getDaySummaryByDate(parseDate(date));
    }

    @Tool(name = "findRecordedDays", description = """
            Obtiene las fechas de todos los días registrados en el diario, sin su contenido. Sirve para saber \
            qué periodo cubre el diario y si un día concreto llegó a registrarse.""")
    public List<LocalDate> findRecordedDays() {
        return daySummaryService.getRecordedDates();
    }
}
