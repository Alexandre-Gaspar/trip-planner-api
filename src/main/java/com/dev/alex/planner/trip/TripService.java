package com.dev.alex.planner.trip;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TripService {
    private TripRepository repository;
    public TripService(TripRepository repository) {
        this.repository = repository;
    }

    public TripCreateResponse createTripService(Trip newTrip) {
        this.repository.save(newTrip);

        return new TripCreateResponse(newTrip.getId());
    }
}
