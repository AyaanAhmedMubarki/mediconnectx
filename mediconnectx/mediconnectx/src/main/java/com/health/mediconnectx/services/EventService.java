package com.health.mediconnectx.services;

import com.health.mediconnectx.dto.EventDTO;
import com.health.mediconnectx.entity.Event;
import com.health.mediconnectx.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll(); // Fetch all events from database
    }

    public Event getEventById(Long id) {
        return eventRepository.findById(id).orElse(null); // Fetch an event by ID or return null if not found
    }

    public void createEvent(EventDTO eventDTOObject, byte[] imageBytes) {

        Event event = new Event();
        event.setEventName(eventDTOObject.getEventName());
        event.setEventDate(eventDTOObject.getEventDate());
        event.setCategory(eventDTOObject.getCategory());
        event.setMeetName(eventDTOObject.getMeetName());
        event.setLocation(eventDTOObject.getLocation());

        // Convert byte[] to Base64 string and set it in the Admin entity
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            event.setImageLink(base64Image);
        }
        eventRepository.save(event);
    }

    public void deleteEvent(Long id) {
        eventRepository.delete(eventRepository.getReferenceById(id));
    }

    public void updateEvent(EventDTO eventDTOObject, byte[] imageBytes) {
        Event event = eventRepository.getReferenceById(eventDTOObject.getId());
        event.setEventName(eventDTOObject.getEventName());
        event.setEventDate(eventDTOObject.getEventDate());
        event.setCategory(eventDTOObject.getCategory());
        event.setMeetName(eventDTOObject.getMeetName());
        event.setLocation(eventDTOObject.getLocation());

        // Convert byte[] to Base64 string and set it in the Admin entity
        if (imageBytes != null && imageBytes.length > 0) {
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            event.setImageLink(base64Image);
        }
        eventRepository.save(event);
    }

}
