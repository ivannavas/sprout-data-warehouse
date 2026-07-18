package io.github.ivannavas.sprout_data_warehouse.service.impl;

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
import io.github.ivannavas.sprout_data_warehouse.entity.ValidatableEntity;
import io.github.ivannavas.sprout_data_warehouse.service.ValidationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validation is the one operation every entity shares, so it is served generically rather than repeated nine
 * times: the type comes in by name and the record is loaded through the {@link EntityManager}. Types are keyed
 * by their {@code @Table} name, so the URL and the table cannot drift apart.
 */
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private static final List<Class<? extends ValidatableEntity>> ENTITY_TYPES = List.of(
            Person.class, PersonEvent.class, Group.class, Feeling.class, DatedFeeling.class, Event.class,
            Activity.class, DatedActivity.class, Project.class, DaySummary.class);

    // A LinkedHashMap so the review queue comes out in the declared order rather than a hash order.
    private static final Map<String, Class<? extends ValidatableEntity>> TYPES_BY_NAME =
            Collections.unmodifiableMap(ENTITY_TYPES.stream()
                    .collect(Collectors.toMap(
                            type -> type.getAnnotation(Table.class).name(), Function.identity(),
                            (a, b) -> a, LinkedHashMap::new)));

    private final EntityManager entityManager;

    @Override
    @Transactional
    public void validate(String type, Long id) {
        ValidatableEntity entity = entityManager.find(resolve(type), id);
        if (entity == null) {
            throw new NoSuchElementException("No " + type + " with id " + id);
        }

        // Inside the transaction the entity is managed, so the flush writes the change.
        entity.setValidated(true);
    }

    @Override
    public List<? extends ValidatableEntity> findPending(String type) {
        return pending(resolve(type));
    }

    @Override
    public Map<String, List<? extends ValidatableEntity>> findAllPending() {
        Map<String, List<? extends ValidatableEntity>> queue = new LinkedHashMap<>();
        TYPES_BY_NAME.forEach((name, entityType) -> {
            List<? extends ValidatableEntity> records = pending(entityType);
            if (!records.isEmpty()) {
                queue.put(name, records);
            }
        });
        return queue;
    }

    @Override
    public Set<String> validatableTypes() {
        return TYPES_BY_NAME.keySet();
    }

    private <T extends ValidatableEntity> List<T> pending(Class<T> entityType) {
        // The entity name, not the table name: this is JPQL.
        return entityManager
                .createQuery("select e from " + entityType.getSimpleName() + " e where e.validated = false",
                        entityType)
                .getResultList();
    }

    private Class<? extends ValidatableEntity> resolve(String type) {
        Class<? extends ValidatableEntity> entityType = TYPES_BY_NAME.get(type);
        if (entityType == null) {
            throw new IllegalArgumentException(
                    "Unknown entity type '" + type + "'. Known types: " + validatableTypes());
        }
        return entityType;
    }
}
