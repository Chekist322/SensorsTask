package com.example.batrakov.broadcastreceivertask;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

/**
 * Created by batrakov on 29.11.17.
 */
public class NotificationJobService extends JobService implements SensorEventListener {

    private static final long JOB_DELAY = 5000;
    SensorManager mSensorManager;
    Sensor mSensor;
    JobParameters mJobParameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        mJobParameters = params;
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            ComponentName componentName = new ComponentName(getBaseContext(), NotificationJobService.class);
            JobInfo jobInfo = new JobInfo.Builder(0, componentName)
                    .setMinimumLatency(JOB_DELAY)
                    .setPersisted(true).build();

            jobScheduler.schedule(jobInfo);
        }
        return false;
    }

    private boolean isLying(float aX, float aY, float aZ) {
        if (aX > -2 && aX < 2) {
            if (aY > -2 && aY < 2) {
                if (aZ > 7 && aZ < 12) {
                    return  true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);

            int lyingCounter;
            if (isLying(Math.abs(event.values[0]), Math.abs(event.values[1]), Math.abs(event.values[2]))) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                lyingCounter = sharedPref.getInt(getString(R.string.lying_counter), 0);

                lyingCounter++;

                if (lyingCounter >= 4) {
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), "Test channel");
                    builder.setContentTitle("Receiver notification")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setContentText("MOVE!")
                            .setSmallIcon(R.mipmap.ic_launcher);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    if (manager != null) {
                        manager.notify(1, builder.build());
                    }
                }
            } else {
                lyingCounter = 0;
            }
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.lying_counter), lyingCounter);
            editor.apply();
        }
        jobFinished(mJobParameters, false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
