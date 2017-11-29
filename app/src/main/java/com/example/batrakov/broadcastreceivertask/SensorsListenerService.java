package com.example.batrakov.broadcastreceivertask;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by batrakov on 28.11.17.
 */

public class SensorsListenerService extends Service {

    private static final String SCHEDULE_INCOMING_JOB = "schedule";
    private static final long HANDLER_DELAY = 20000;

    IBinder mBinder = new LocalBinder();
    SensorManager mSensorManager;
    Sensor mSensor;
    Handler mMovingCheckHandler;

    long mLastMovingTimeStamp;
    boolean mMoved = false;

    TriggerEventListener mTriggerEventListener;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand: ");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

            mTriggerEventListener = new TriggerEventListener() {
                @Override
                public void onTrigger(TriggerEvent event) {
                    mMoved = true;
                    buildMovingStateNotification(true);
                    mSensorManager.requestTriggerSensor(this, mSensor);

                }
            };
            mSensorManager.requestTriggerSensor(mTriggerEventListener, mSensor);
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Test channel");
        builder.setContentTitle("Listener Service Notification")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText("Listener Service")
                .setSmallIcon(R.mipmap.ic_launcher);

        startForeground(0, builder.build());

        mMovingCheckHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (!mMoved) {
                    Intent intent = new Intent("com.example.batrakov.broadcastreceivertask.START_ACTION");
                    intent.putExtra(SCHEDULE_INCOMING_JOB, 0);
                    sendBroadcast(intent);
                }
                mMoved = false;
                buildMovingStateNotification(false);
                mMovingCheckHandler.sendEmptyMessageDelayed(0, HANDLER_DELAY);
                return false;
            }
        });

        mMovingCheckHandler.sendEmptyMessageDelayed(0, HANDLER_DELAY);
        return super.onStartCommand(intent, flags, startId);
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class LocalBinder extends Binder {
        SensorsListenerService getService() {
            return SensorsListenerService.this;
        }
    }

    private void buildMovingStateNotification(boolean aMoved) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Test channel");
        builder.setContentTitle(String.valueOf(aMoved))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(NotificationCompat.PRIORITY_LOW)
                .setContentText("moving state")
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(20, builder.build());
        }
    }
}
