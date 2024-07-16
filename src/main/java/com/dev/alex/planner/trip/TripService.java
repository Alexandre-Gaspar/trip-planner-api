package com.dev.alex.planner.trip;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripService {
    private TripRepository repository;
    public TripService(TripRepository repository) {
        this.repository = repository;
    }

    public Optional<Trip> findByTripId(UUID tripId) {
        var tripFound = this.repository.findById(tripId);
        return tripFound;
    }

    public Trip updateTrip(Trip rawTrip) {
        this.repository.save(rawTrip);
        return rawTrip;
    }

    public TripCreateResponse createTripService(Trip newTrip) {
        this.repository.save(newTrip);

        return new TripCreateResponse(newTrip.getId());
    }
}
