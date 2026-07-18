package io.github.ivannavas.sprout_data_warehouse.controller;

import io.github.ivannavas.sprout_data_warehouse.entity.ValidatableEntity;
import io.github.ivannavas.sprout_data_warehouse.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/warehouse/entities")
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PatchMapping("/{type}/{id}/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validate(@PathVariable String type, @PathVariable Long id) {
        validationService.validate(type, id);
    }

    @GetMapping("/pending")
    public Map<String, List<? extends ValidatableEntity>> pending() {
        return validationService.findAllPending();
    }

    @GetMapping("/{type}/pending")
    public List<? extends ValidatableEntity> pending(@PathVariable String type) {
        return validationService.findPending(type);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String unknownType(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NoSuchElementException e) {
        return e.getMessage();
    }
}
