package io.github.ivannavas.sprout_data_warehouse.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ivannavas.sprout_data_warehouse.entity.*;
import io.github.ivannavas.sprout_data_warehouse.service.ValidationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private static final List<Class<? extends ValidatableEntity>> ENTITY_TYPES = List.of(
            Person.class, PersonEvent.class, Group.class, Feeling.class, DatedFeeling.class, Event.class,
            Activity.class, DatedActivity.class, Project.class, DaySummary.class);

    private static final Map<String, Class<? extends ValidatableEntity>> TYPES_BY_NAME =
            Collections.unmodifiableMap(ENTITY_TYPES.stream()
                    .collect(Collectors.toMap(
                            type -> type.getAnnotation(Table.class).name(), Function.identity(),
                            (a, b) -> a, LinkedHashMap::new)));

    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void validate(String type, Long id, Map<String, Object> changes) {
        ValidatableEntity entity = entityManager.find(resolve(type), id);
        if (entity == null) {
            throw new NoSuchElementException("No " + type + " with id " + id);
        }

        applyChanges(entity, changes);

        entity.setValidated(true);
    }

    private void applyChanges(ValidatableEntity entity, Map<String, Object> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }
        if (changes.containsKey("id")) {
            throw new IllegalArgumentException("The id of a record cannot be changed");
        }

        JsonNode patch = objectMapper.valueToTree(changes);
        try {
            objectMapper.readerForUpdating(entity)
                    .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                    .readValue(patch);
        } catch (IllegalArgumentException | IOException e) {
            throw new IllegalArgumentException("Cannot apply the changes: " + e.getMessage(), e);
        }
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
