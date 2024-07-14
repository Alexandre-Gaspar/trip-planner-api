package com.dev.alex.planner.participant;

import com.dev.alex.planner.trip.Trip;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    private ParticipantRepository repository;

    public ParticipantService(ParticipantRepository repository) {
        this.repository = repository;
    }

    public void registerParticipantsToTrip(List<String> participantsToInvite, Trip trip) {
        List<Participant> participants = participantsToInvite
                .stream()
                .map(email -> new Participant(email, trip))
                .toList();

        this.repository.saveAll(participants);

        System.out.println(participants.get(0).getId());
    }

    public void triggerConfirmationEmailToParticipants(UUID tripId) {}
}
