package io.github.ivannavas.sprout_data_warehouse.service.impl;

import io.github.ivannavas.sprout_data_warehouse.entity.Activity;
import io.github.ivannavas.sprout_data_warehouse.entity.DatedActivity;
import io.github.ivannavas.sprout_data_warehouse.repository.ActivityRepository;
import io.github.ivannavas.sprout_data_warehouse.repository.DatedActivityRepository;
import io.github.ivannavas.sprout_data_warehouse.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final DatedActivityRepository datedActivityRepository;

    @Override
    public Activity saveActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    @Override
    public DatedActivity saveDatedActivity(DatedActivity datedActivity) {
        return datedActivityRepository.save(datedActivity);
    }

    @Override
    public List<DatedActivity> getAllDatedActivities() {
        return datedActivityRepository.findAll();
    }
}
