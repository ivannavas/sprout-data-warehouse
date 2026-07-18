package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.ValidatableEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ValidationService {


    void validate(String type, Long id, Map<String, Object> changes);

    List<? extends ValidatableEntity> findPending(String type);

    Map<String, List<? extends ValidatableEntity>> findAllPending();

    Set<String> validatableTypes();
}
