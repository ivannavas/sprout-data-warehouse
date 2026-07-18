package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.Event;
import io.github.ivannavas.sprout_data_warehouse.repository.EventRepository;
import io.github.ivannavas.sprout_data_warehouse.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }
}
