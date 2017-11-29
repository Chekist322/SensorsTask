package com.example.batrakov.broadcastreceivertask;

import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by batrakov on 28.11.17.
 */

public class StarterReceiver extends BroadcastReceiver {

    private static final String SCHEDULE_INCOMING_JOB = "schedule";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(SCHEDULE_INCOMING_JOB)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Test channel");
            builder.setContentTitle("Receiver notification")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setContentText("MOVE!")
                    .setSmallIcon(R.mipmap.ic_launcher);
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(1, builder.build());
            }
        }
    }
}
