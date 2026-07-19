package io.github.ivannavas.sprout_data_warehouse.tool;

import io.github.ivannavas.sprout.annotation.Tool;
import io.github.ivannavas.sprout.annotation.ToolParam;
import io.github.ivannavas.sprout.executor.AgentExecutor;
import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedFeeling;
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

import java.time.LocalDate;
import java.util.List;

/**
 * The agent's read side: what is already in the warehouse, so a day's extraction can tell a new record
 * from an update to an existing one.
 *
 * <p>The finders split into two shapes on purpose. The catalogue entities — people, groups, feelings,
 * activities, events, projects — are returned whole: the agent has to compare a mention against every
 * one of them to recognise that "estrés por el trabajo" is the "ansiedad por trabajo" already on file,
 * and these tables level off, since a diary stops introducing new people and new activities long
 * before it stops being written.
 *
 * <p>The rest are scoped, because they never level off. Dated records gain rows every single day
 * ingested and person events grow with them, so returning those whole would put the entire history in
 * the prompt on every call and make each day of a backfill dearer than the one before. Scoping them
 * loses nothing: a dated record is only ever compared against others on its own date, and a person
 * event against the other events of that same person.
 */
public class DatabaseReader extends AgentExecutor {

    protected final PersonService personService;
    protected final FeelingService feelingService;
    protected final ActivityService activityService;
    protected final EventService eventService;
    protected final ProjectService projectService;
    protected final DaySummaryService daySummaryService;

    public DatabaseReader(PersonService personService,
                          FeelingService feelingService,
                          ActivityService activityService,
                          EventService eventService,
                          ProjectService projectService,
                          DaySummaryService daySummaryService) {
        this.personService = personService;
        this.feelingService = feelingService;
        this.activityService = activityService;
        this.eventService = eventService;
        this.projectService = projectService;
        this.daySummaryService = daySummaryService;
    }

    @Tool(name = "findAllPersons", description = "Obtiene todas las personas de la base de datos")
    public List<Person> findAllPersons() {
        return personService.getAllPersons();
    }

    @Tool(name = "findPersonEventsByPerson", description = "Obtiene los eventos registrados de una persona concreta")
    public List<PersonEvent> findPersonEventsByPerson(
            @ToolParam(description = "Id de la persona cuyos eventos se quieren consultar", required = true) Long personId) {
        return personService.getPersonEventsByPersonId(personId);
    }

    @Tool(name = "findAllGroups", description = "Obtiene todos los grupos de personas de la base de datos")
    public List<Group> findAllGroups() {
        return personService.getAllGroups();
    }

    @Tool(name = "findAllFeelings", description = "Obtiene todos los sentimientos de la base de datos")
    public List<Feeling> findAllFeelings() {
        return feelingService.getAllFeelings();
    }

    @Tool(name = "findDatedFeelingsByDate", description = "Obtiene los sentimientos ya registrados en una fecha concreta")
    public List<DatedFeeling> findDatedFeelingsByDate(
            @ToolParam(description = "Fecha a consultar, en formato yyyy-MM-dd", required = true) String date) {
        return feelingService.getDatedFeelingsByDate(parseDate(date));
    }

    @Tool(name = "findAllEvents", description = "Obtiene todos mis eventos de la base de datos")
    public List<Event> findAllEvents() {
        return eventService.getAllEvents();
    }

    @Tool(name = "findAllActivities", description = "Obtiene todas las actividades de la base de datos")
    public List<Activity> findAllActivities() {
        return activityService.getAllActivities();
    }

    @Tool(name = "findDatedActivitiesByDate", description = "Obtiene las actividades ya registradas en una fecha concreta")
    public List<DatedActivity> findDatedActivitiesByDate(
            @ToolParam(description = "Fecha a consultar, en formato yyyy-MM-dd", required = true) String date) {
        return activityService.getDatedActivitiesByDate(parseDate(date));
    }

    @Tool(name = "findAllProjects", description = "Obtiene todos los proyectos de la base de datos")
    public List<Project> findAllProjects() {
        return projectService.getAllProjects();
    }

    /** Parses a {@code yyyy-MM-dd} tool argument, treating a missing or blank date as none. */
    protected static LocalDate parseDate(String date) {
        return date == null || date.isBlank() ? null : LocalDate.parse(date.trim());
    }
}
