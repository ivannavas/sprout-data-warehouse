package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Group;
import io.github.ivannavas.sprout_data_warehouse.entity.Person;
import io.github.ivannavas.sprout_data_warehouse.entity.PersonEvent;

import java.util.List;

public interface PersonService {
    Person savePerson(Person person);

    List<Person> getAllPersons();

    PersonEvent savePersonEvent(PersonEvent personEvent);

    /**
     * The events recorded for one person. Scoped by person rather than fetched whole because this
     * table grows with every person and every day ingested, and an event only ever needs comparing
     * against the other events of the same person.
     */
    List<PersonEvent> getPersonEventsByPersonId(Long personId);

    Group saveGroup(Group group);

    List<Group> getAllGroups();
}
