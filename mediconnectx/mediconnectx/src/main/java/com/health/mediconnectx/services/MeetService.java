package com.health.mediconnectx.services;

import com.health.mediconnectx.repository.MeetRepository;
import com.health.mediconnectx.dto.MeetDTO;
import com.health.mediconnectx.entity.Meet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetService {

    @Autowired
    private MeetRepository meetRepository;

    public List<Meet> getMeet(){

        List<Meet> meetDetails = meetRepository.findAll();

        if (meetDetails.isEmpty()) {
            throw new RuntimeException("No meet available");
        }

        return meetDetails;
    }

    public void setMeet(Meet meet){

        if (meet.getName() == null){
            throw new RuntimeException("A meet name is must required");}

        meetRepository.save(meet);
    }

    public void deleteMeet(Long id) {
        meetRepository.delete(meetRepository.getReferenceById(id));
    }
}
