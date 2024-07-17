package com.dev.alex.planner.trip;

import com.dev.alex.planner.activity.ActivityCreateResponse;
import com.dev.alex.planner.activity.ActivityData;
import com.dev.alex.planner.activity.ActivityRequestPayload;
import com.dev.alex.planner.activity.ActivityService;
import com.dev.alex.planner.link.LinkCreateResponse;
import com.dev.alex.planner.link.LinkData;
import com.dev.alex.planner.link.LinkRequestPayload;
import com.dev.alex.planner.link.LinkService;
import com.dev.alex.planner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@RestController
@RequestMapping("/trips")
public class TripController {

    @Autowired
    private TripService tripService;
    @Autowired
    private ParticipantService  participantService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private LinkService linkService;

    private TripRepository repository;
    public TripController(TripRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);
        TripCreateResponse tripCreateResponse = this.tripService.createTripService(newTrip);
        this.participantService.registerParticipantsToTrip(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(tripCreateResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        var tripFound = this.tripService.findByTripId(id);
        return tripFound.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        var tripFound = this.tripService.findByTripId(id);

        if (tripFound.isPresent()) {
            var rawTrip = tripFound.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.tripService.updateTrip(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }
        return tripFound.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        var tripFound = this.tripService.findByTripId(id);

        if (tripFound.isPresent()) {
            var rawTrip = tripFound.get();
            rawTrip.setIsConfirmed(true);

            this.tripService.updateTrip(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }
        return ResponseEntity.notFound().build();
    }

    // participants @endpoints
    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipants(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload) {
        var trip = this.tripService.findByTripId(id);

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

    @GetMapping("/{id}/participants/confirmed")
    public ResponseEntity<List<ParticipantData>> getAllParticipantsConfirmed(@PathVariable UUID id) {
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participantList.stream().filter(participant -> participant.isConfirmed()).toList());
    }

    @GetMapping("/{id}/participants/unconfirmed")
    public ResponseEntity<List<ParticipantData>> getAllParticipantsUnconfirmed(@PathVariable UUID id) {
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participantList.stream().filter(participant -> !participant.isConfirmed()).toList());
    }

    // activities @endpoints
    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityCreateResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload) {
        var trip = this.tripService.findByTripId(id);

        if (trip.isPresent()) {
            Trip rawTrip = trip.get();

            ActivityCreateResponse activityCreateResponse = this.activityService.registerActivity(payload, rawTrip);

            return ResponseEntity.ok(activityCreateResponse); // returns the id of the invited participant
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activityList = this.activityService.getAllActivitiesFromTrip(id);

        return ResponseEntity.ok(activityList);
    }

    // links @endpoints
    @PostMapping("/{id}/links")
    public ResponseEntity<LinkCreateResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload) {
        var trip = this.tripService.findByTripId(id);

        if (trip.isPresent()) {
            var rawTrip = trip.get();

            LinkCreateResponse linkCreateResponse = this.linkService.registerLink(payload, rawTrip);

            return ResponseEntity.ok(linkCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> linkList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkList);
    }
}
