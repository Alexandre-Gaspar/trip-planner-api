package com.dev.alex.planner.trip;

import com.dev.alex.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private ParticipantService  participantService;

    private TripRepository repository;

    public TripController(TripRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);
        this.participantService.registerParticipantsToTrip(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        var tripFound = repository.findById(id);
        return tripFound.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        var tripFound = repository.findById(id);

        if (tripFound.isPresent()) {
            var rawTrip = tripFound.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }
        return tripFound.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        var tripFound = this.repository.findById(id);

        if (tripFound.isPresent()) {
            var rawTrip = tripFound.get();
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipants(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        var trip = this.repository.findById(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantCreateResponse = this.participantService.registerParticipantToTrip(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantCreateResponse); // returns the id of the invited participant
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participantList);
    }
}
