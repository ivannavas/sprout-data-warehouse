package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Event;

import java.util.List;

public interface EventService {
    Event saveEvent(Event event);

    List<Event> getAllEvents();
}
