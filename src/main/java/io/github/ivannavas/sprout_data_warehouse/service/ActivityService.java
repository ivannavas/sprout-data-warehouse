package io.github.ivannavas.sprout_data_warehouse.service;

import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;

import java.util.List;

public interface ActivityService {
    Activity saveActivity(Activity activity);

    List<Activity> getAllActivities();

    DatedActivity saveDatedActivity(DatedActivity datedActivity);

    List<DatedActivity> getAllDatedActivities();
}
