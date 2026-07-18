package io.github.ivannavas.sprout_data_warehouse.tool;

import io.github.ivannavas.sprout.annotation.Tool;
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

import java.util.List;

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

    @Tool(name = "findAllPersonEvents", description = "Obtiene todos los eventos de personas de la base de datos")
    public List<PersonEvent> findAllPersonEvents() {
        return personService.getAllPersonEvents();
    }

    @Tool(name = "findAllGroups", description = "Obtiene todos los grupos de personas de la base de datos")
    public List<Group> findAllGroups() {
        return personService.getAllGroups();
    }

    @Tool(name = "findAllFeelings", description = "Obtiene todos los sentimientos de la base de datos")
    public List<Feeling> findAllFeelings() {
        return feelingService.getAllFeelings();
    }

    @Tool(name = "findAllDatedFeelings", description = "Obtiene todos los sentimientos fechados de la base de datos")
    public List<DatedFeeling> findAllDatedFeelings() {
        return feelingService.getAllDatedFeelings();
    }

    @Tool(name = "findAllEvents", description = "Obtiene todos mis eventos de la base de datos")
    public List<Event> findAllEvents() {
        return eventService.getAllEvents();
    }

    @Tool(name = "findAllActivities", description = "Obtiene todas las actividades de la base de datos")
    public List<Activity> findAllActivities() {
        return activityService.getAllActivities();
    }

    @Tool(name = "findAllDatedActivities", description = "Obtiene todas las actividades fechadas de la base de datos")
    public List<DatedActivity> findAllDatedActivities() {
        return activityService.getAllDatedActivities();
    }

    @Tool(name = "findAllProjects", description = "Obtiene todos los proyectos de la base de datos")
    public List<Project> findAllProjects() {
        return projectService.getAllProjects();
    }
}
