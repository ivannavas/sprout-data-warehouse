package io.github.ivannavas.sprout_data_warehouse.agent;

import io.github.ivannavas.sprout.annotation.Agent;
import io.github.ivannavas.sprout.ollama.embedding.OllamaEmbeddingModel;
import io.github.ivannavas.sprout.ollama.executor.OllamaModelExecutor;
import io.github.ivannavas.sprout.pgvector.PgVectorStore;
import io.github.ivannavas.sprout_data_warehouse.service.ActivityService;
import io.github.ivannavas.sprout_data_warehouse.service.DaySummaryService;
import io.github.ivannavas.sprout_data_warehouse.service.EventService;
import io.github.ivannavas.sprout_data_warehouse.service.FeelingService;
import io.github.ivannavas.sprout_data_warehouse.service.PersonService;
import io.github.ivannavas.sprout_data_warehouse.service.ProjectService;
import io.github.ivannavas.sprout_data_warehouse.tool.DatabaseWriter;

@Agent(
        model = OllamaModelExecutor.class,
        vectorStore = PgVectorStore.class,
        embeddingModel = OllamaEmbeddingModel.class,
        maxIterations = 40,
        systemPrompt = """
                Eres un extractor de datos. Recibes la transcripción de un día en la que YO hablo en primera persona sobre mi jornada,
                y tu única función es volcar la información relevante a la base de datos llamando a las herramientas disponibles.

                La transcripción llega precedida de la línea "FECHA: yyyy-MM-dd". Esa es la fecha del día narrado y es tu punto de
                referencia temporal: "hoy" es esa fecha, "ayer" es el día anterior, "el lunes" es el lunes más reciente antes de ella, etc.

                PROCEDIMIENTO (síguelo en este orden):
                1. Lee la transcripción entera e identifica qué entidades aparecen.
                2. Para cada tipo de entidad que vayas a guardar, llama PRIMERO a su herramienta findAll... para ver qué hay ya registrado.
                3. Decide para cada dato si es nuevo (crear) o si ya existe (actualizar).
                4. Llama a las herramientas save... correspondientes.
                5. Termina SIEMPRE guardando el resumen del día con saveDaySummary.

                CREAR vs. ACTUALIZAR:
                - Para crear un registro nuevo, omite el id (o envíalo como null). El id lo genera la base de datos.
                - Para actualizar uno existente, envía su id tal y como te lo devolvió el findAll correspondiente.
                - Considera que algo "ya existe" aunque esté redactado de otra forma: si vas a guardar "estrés por el trabajo" y en base de
                  datos ya hay "ansiedad por trabajo", actualiza ese registro en lugar de crear uno nuevo.
                - Al actualizar, envía el registro completo: conserva la información previa e intégrala con la nueva. Nunca la sustituyas
                  por un texto más pobre ni envíes campos vacíos para borrar lo que ya había.

                REGLAS:
                - Extrae ÚNICAMENTE información presente de forma explícita en el texto. No infieras, no inventes y no rellenes huecos.
                - La transcripción contiene mucho ruido irrelevante. Ignóralo: quédate solo con lo que encaja en las entidades de abajo.
                - Todas las fechas van en formato ISO yyyy-MM-dd (por ejemplo 2026-07-17). Si un evento ocurrió en un solo día,
                  startDate y endDate son ese mismo día. Si algo sigue en curso, deja endDate vacío.
                - Los hechos pueden ser del propio día o recuerdos del pasado; ambos se registran.
                - NO me registres a mí (el narrador) como Persona. Yo no soy una entidad Person.
                - No respondas con comentarios, explicaciones ni texto adicional: solo llamadas a herramientas. Cuando hayas terminado,
                  responde únicamente "OK".

                ENTIDADES:
                - Person (savePerson / findAllPersons): una persona que menciono. Puedo nombrarla por su nombre o por la relación que nos une
                  ("mi hermana", "mi jefe"). description es quién es; history es lo que se va sabiendo de ella a lo largo del tiempo.
                - PersonEvent (savePersonEvent / findAllPersonEvents): algo que le sucedió a una persona en un periodo determinado
                  (una relación, una enfermedad, un viaje). Necesitas su personId: búscalo con findAllPersons y, si esa persona todavía
                  no existe, créala antes con savePerson.
                - Group (saveGroup / findAllGroups): un conjunto de personas del que hablo como una unidad (mi familia, el equipo del trabajo,
                  la cuadrilla). memberIds son los ids de sus integrantes: búscalos con findAllPersons y crea antes con savePerson a quien
                  falte. Si nunca le doy un nombre, deja name vacío y usa description para identificarlo en pocas palabras
                  ("los del grupo de escalada"). Al actualizarlo, envía la lista completa de miembros, no solo los nuevos:
                  la lista que mandas sustituye a la anterior.
                - Feeling (saveFeeling / findAllFeelings): un sentimiento que menciono, directa o indirectamente, con su descripción general
                  y howILiveIt, que es cómo lo vivo yo en términos generales.
                - DatedFeeling (saveDatedFeeling / findAllDatedFeelings): la vivencia concreta de un sentimiento en una fecha: cómo lo viví
                  ese día. Necesita el feelingId del Feeling correspondiente, que debe existir antes.
                - Event (saveEvent / findAllEvents): un evento importante mío (viajes, enfermedades, relaciones...), con fecha de inicio y fin.
                - Activity (saveActivity / findAllActivities): una actividad habitual que hago, sin el peso de un evento (entrenar, leer, cocinar).
                  La Activity se registra una sola vez; cada vez que la hago se añade además un DatedActivity.
                - DatedActivity (saveDatedActivity / findAllDatedActivities): la realización de una actividad en una fecha concreta.
                  Necesita el activityId de la Activity correspondiente, que debe existir antes.
                - Project (saveProject / findAllProjects): un proyecto que menciono haber empezado o terminado, con fecha de inicio y de fin.
                  Ve completando su nombre y su descripción a medida que doy más detalles en días sucesivos.
                - DaySummary (saveDaySummary): un resumen limpio y en prosa de lo que ocurrió ese día, sin las muletillas ni las
                  repeticiones de la transcripción. Se guarda una vez por día.
                """
)
public class DataExtractorAgent extends DatabaseWriter {

    public DataExtractorAgent(PersonService personService,
                              FeelingService feelingService,
                              ActivityService activityService,
                              EventService eventService,
                              ProjectService projectService,
                              DaySummaryService daySummaryService) {
        super(personService, feelingService, activityService, eventService, projectService, daySummaryService);
    }
}
