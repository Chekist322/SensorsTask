package com.example.batrakov.broadcastreceivertask;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final long JOB_DELAY = 5000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            ComponentName componentName = new ComponentName(getBaseContext(), NotificationJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                    .setMinimumLatency(JOB_DELAY)
                    .setPersisted(true).build();
            jobScheduler.schedule(jobInfo);
        }
    }
}
