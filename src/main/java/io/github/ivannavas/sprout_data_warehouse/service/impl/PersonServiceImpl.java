package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.Group;
import io.github.ivannavas.sprout_data_warehouse.entity.Person;
import io.github.ivannavas.sprout_data_warehouse.entity.PersonEvent;
import io.github.ivannavas.sprout_data_warehouse.repository.GroupRepository;
import io.github.ivannavas.sprout_data_warehouse.repository.PersonEventRepository;
import io.github.ivannavas.sprout_data_warehouse.repository.PersonRepository;
import io.github.ivannavas.sprout_data_warehouse.service.PersonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonServiceImpl implements PersonService {

    private final PersonRepository personRepository;
    private final PersonEventRepository personEventRepository;
    private final GroupRepository groupRepository;

    @Override
    public Person savePerson(Person person) {
        return personRepository.save(person);
    }

    @Override
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    @Override
    public PersonEvent savePersonEvent(PersonEvent personEvent) {
        return personEventRepository.save(personEvent);
    }

    @Override
    public List<PersonEvent> getPersonEventsByPersonId(Long personId) {
        return personId == null ? List.of() : personEventRepository.findByPersonId(personId);
    }

    @Override
    public Group saveGroup(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }
}
