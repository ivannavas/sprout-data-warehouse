package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.ValidatableEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ValidationService {

    /**
     * Marks the record of the given type and id as validated. The type is the entity's table name
     * (see {@link #validatableTypes()}).
     */
    void validate(String type, Long id);

    /** The records of the given type still awaiting validation. */
    List<? extends ValidatableEntity> findPending(String type);

    /**
     * The whole review queue: every type that has pending records, mapped to them. Types with nothing
     * pending are left out, so an empty map means there is nothing to review.
     */
    Map<String, List<? extends ValidatableEntity>> findAllPending();

    /** The type names {@link #validate} accepts. */
    Set<String> validatableTypes();
}
