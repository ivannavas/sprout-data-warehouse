package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;

import java.time.LocalDate;
import java.util.List;

public interface ActivityService {
    Activity saveActivity(Activity activity);

    List<Activity> getAllActivities();

    DatedActivity saveDatedActivity(DatedActivity datedActivity);

    /**
     * The activity records for one day. Scoped by date rather than fetched whole because this table
     * gains rows every day ingested: over a long backfill the full list grows without bound, while
     * what a day's extraction needs stays the handful of rows sharing its date.
     */
    List<DatedActivity> getDatedActivitiesByDate(LocalDate date);
}
