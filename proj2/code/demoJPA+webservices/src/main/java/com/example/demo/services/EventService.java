package com.example.demo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.data.Event;
import com.example.demo.repositories.EventRepository;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        List<Event> userRecords = new ArrayList<>();
        eventRepository.findAll().forEach(userRecords::add);
        return userRecords;
    }

    public void addEvent(Event event) {
        eventRepository.save(event);
    }

    public void deleteEvent(Event event) {
        eventRepository.delete(event);
    }

    public Optional<Event> getEvent(int id) {
        return eventRepository.findById(id);
    }
}