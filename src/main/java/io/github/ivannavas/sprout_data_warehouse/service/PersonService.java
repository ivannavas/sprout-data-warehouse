package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Group;
import io.github.ivannavas.sprout_data_warehouse.entity.Person;
import io.github.ivannavas.sprout_data_warehouse.entity.PersonEvent;

import java.util.List;

public interface PersonService {
    Person savePerson(Person person);

    List<Person> getAllPersons();

    PersonEvent savePersonEvent(PersonEvent personEvent);

    List<PersonEvent> getAllPersonEvents();

    Group saveGroup(Group group);

    List<Group> getAllGroups();
}
