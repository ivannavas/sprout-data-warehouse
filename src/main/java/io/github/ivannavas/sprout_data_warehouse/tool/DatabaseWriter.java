package io.github.ivannavas.sprout_data_warehouse.tool;

import io.github.ivannavas.sprout.annotation.Tool;
import io.github.ivannavas.sprout.annotation.ToolParam;
import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
import io.github.ivannavas.sprout_data_warehouse.entity.DaySummary;
import io.github.ivannavas.sprout_data_warehouse.entity.Event;
import io.github.ivannavas.sprout_data_warehouse.entity.Feeling;
import io.github.ivannavas.sprout_data_warehouse.entity.Group;
import io.github.ivannavas.sprout_data_warehouse.entity.Person;
import io.github.ivannavas.sprout_data_warehouse.entity.PersonEvent;
import io.github.ivannavas.sprout_data_warehouse.entity.Project;
import io.github.ivannavas.sprout_data_warehouse.service.ActivityService;
import io.github.ivannavas.sprout_data_warehouse.service.DaySummaryService;
import io.github.ivannavas.sprout_data_warehouse.service.EventService;
import io.github.ivannavas.sprout_data_warehouse.service.FeelingService;
import io.github.ivannavas.sprout_data_warehouse.service.PersonService;
import io.github.ivannavas.sprout_data_warehouse.service.ProjectService;

import java.util.ArrayList;
import java.util.List;

public class DatabaseWriter extends DatabaseReader {

    public DatabaseWriter(PersonService personService,
                          FeelingService feelingService,
                          ActivityService activityService,
                          EventService eventService,
                          ProjectService projectService,
                          DaySummaryService daySummaryService) {
        super(personService, feelingService, activityService, eventService, projectService, daySummaryService);
    }

    @Tool(name = "savePerson", description = "Guarda una persona nueva, o actualiza la existente si se pasa el id")
    public void savePerson(
            @ToolParam(description = "Id de la persona a actualizar. Omitir para crear una nueva", required = false) Long id,
            @ToolParam(description = "Nombre de la persona, o la relación que nos une si no doy su nombre", required = true) String name,
            @ToolParam(description = "Quién es esta persona", required = false) String description,
            @ToolParam(description = "Lo que se sabe de esta persona a lo largo del tiempo", required = false) String history) {
        personService.savePerson(new Person(id, name, description, history));
    }

    @Tool(name = "savePersonEvent", description = "Guarda un evento de una persona, o lo actualiza si se pasa el id")
    public void savePersonEvent(
            @ToolParam(description = "Id del evento a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Id de la persona a la que le sucedió el evento", required = true) Long personId,
            @ToolParam(description = "Fecha de inicio en formato yyyy-MM-dd", required = true) String startDate,
            @ToolParam(description = "Fecha de fin en formato yyyy-MM-dd. Omitir si sigue en curso", required = false) String endDate,
            @ToolParam(description = "Qué le sucedió a la persona", required = true) String description) {
        personService.savePersonEvent(new PersonEvent(id, personId, parseDate(startDate), parseDate(endDate), description));
    }

    @Tool(name = "saveGroup", description = "Guarda un grupo de personas nuevo, o actualiza el existente si se pasa el id")
    public void saveGroup(
            @ToolParam(description = "Id del grupo a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Nombre del grupo. Omitir si nunca le doy un nombre", required = false) String name,
            @ToolParam(description = "Quiénes son y qué les une. Si el grupo no tiene nombre, esto es lo que lo identifica", required = true) String description,
            @ToolParam(description = "Ids de las personas que pertenecen al grupo", required = true) List<Long> memberIds) {
        personService.saveGroup(new Group(id, name, description, memberIds == null ? new ArrayList<>() : memberIds));
    }

    @Tool(name = "saveFeeling", description = "Guarda un sentimiento nuevo, o actualiza el existente si se pasa el id")
    public void saveFeeling(
            @ToolParam(description = "Id del sentimiento a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Nombre del sentimiento", required = true) String name,
            @ToolParam(description = "Descripción general del sentimiento", required = false) String description,
            @ToolParam(description = "Cómo vivo este sentimiento en términos generales", required = false) String howILiveIt) {
        feelingService.saveFeeling(new Feeling(id, name, description, howILiveIt));
    }

    @Tool(name = "saveDatedFeeling", description = "Guarda la vivencia de un sentimiento en una fecha, o la actualiza si se pasa el id")
    public void saveDatedFeeling(
            @ToolParam(description = "Id del sentimiento fechado a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Id del sentimiento que se ha vivido", required = true) Long feelingId,
            @ToolParam(description = "Fecha en la que se sintió, en formato yyyy-MM-dd", required = true) String date,
            @ToolParam(description = "Cómo se vivió el sentimiento ese día concreto", required = true) String description) {
        feelingService.saveDatedFeeling(new DatedFeeling(id, feelingId, parseDate(date), description));
    }

    @Tool(name = "saveEvent", description = "Guarda un evento mío nuevo, o actualiza el existente si se pasa el id")
    public void saveEvent(
            @ToolParam(description = "Id del evento a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Nombre del evento", required = true) String name,
            @ToolParam(description = "Descripción del evento", required = false) String description,
            @ToolParam(description = "Fecha de inicio en formato yyyy-MM-dd", required = true) String startDate,
            @ToolParam(description = "Fecha de fin en formato yyyy-MM-dd. Omitir si sigue en curso", required = false) String endDate) {
        eventService.saveEvent(new Event(id, name, description, parseDate(startDate), parseDate(endDate)));
    }

    @Tool(name = "saveActivity", description = "Guarda una actividad nueva, o actualiza la existente si se pasa el id")
    public void saveActivity(
            @ToolParam(description = "Id de la actividad a actualizar. Omitir para crear una nueva", required = false) Long id,
            @ToolParam(description = "Nombre de la actividad", required = true) String name,
            @ToolParam(description = "En qué consiste la actividad", required = false) String description) {
        activityService.saveActivity(new Activity(id, name, description));
    }

    @Tool(name = "saveDatedActivity", description = "Guarda la realización de una actividad en una fecha, o la actualiza si se pasa el id")
    public void saveDatedActivity(
            @ToolParam(description = "Id de la actividad fechada a actualizar. Omitir para crear una nueva", required = false) Long id,
            @ToolParam(description = "Id de la actividad realizada", required = true) Long activityId,
            @ToolParam(description = "Fecha en la que se realizó, en formato yyyy-MM-dd", required = true) String date,
            @ToolParam(description = "Detalles de cómo se realizó ese día", required = false) String description) {
        activityService.saveDatedActivity(new DatedActivity(id, activityId, parseDate(date), description));
    }

    @Tool(name = "saveProject", description = "Guarda un proyecto nuevo, o actualiza el existente si se pasa el id")
    public void saveProject(
            @ToolParam(description = "Id del proyecto a actualizar. Omitir para crear uno nuevo", required = false) Long id,
            @ToolParam(description = "Nombre del proyecto", required = true) String name,
            @ToolParam(description = "Descripción del proyecto", required = false) String description,
            @ToolParam(description = "Fecha de inicio en formato yyyy-MM-dd", required = false) String startDate,
            @ToolParam(description = "Fecha de fin en formato yyyy-MM-dd. Omitir si sigue en curso", required = false) String endDate) {
        projectService.saveProject(new Project(id, name, description, parseDate(startDate), parseDate(endDate)));
    }

    @Tool(name = "saveDaySummary", description = "Guarda el resumen limpio del día. Si ya existe un resumen para esa fecha, lo reemplaza")
    public void saveDaySummary(
            @ToolParam(description = "Fecha del día resumido, en formato yyyy-MM-dd", required = true) String date,
            @ToolParam(description = "Resumen en prosa de lo que ocurrió ese día", required = true) String summary) {
        daySummaryService.saveDaySummary(new DaySummary(null, parseDate(date), summary));
    }
}
