package com.dev.alex.planner.activity;

import com.dev.alex.planner.trip.Trip;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private ActivityRepository repository;

    public ActivityService(ActivityRepository repository) {
        this.repository = repository;
    }

    public ActivityCreateResponse registerActivity(ActivityRequestPayload payload, Trip trip) {
        Activity newActivity = new Activity(payload.title(), payload.occursAt(), trip);
        this.repository.save(newActivity);

        return new ActivityCreateResponse(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromTrip(UUID id) {
        return this.repository.findByTripId(id)
                .stream()
                .map(activity -> new ActivityData(activity.getId(), activity.getTitle(), activity.getOccursAt()))
                .toList();
    }
}
