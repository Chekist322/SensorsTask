package com.example.batrakov.LyingDetector;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;

/**
 * Created by batrakov on 29.11.17.
 */
public class NotificationJobService extends JobService {

    private static final long JOB_DELAY = 5000;

    @Override
    public boolean onStartJob(JobParameters params) {
        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            ComponentName componentName = new ComponentName(getBaseContext(), NotificationJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                    .setMinimumLatency(JOB_DELAY)
                    .setPersisted(true).build();

            jobScheduler.schedule(jobInfo);
        }

        Intent startSensorServiceIntent = new Intent(this, SensorService.class);
        startService(startSensorServiceIntent);
        jobFinished(params, false);
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
