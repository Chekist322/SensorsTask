package com.example.batrakov.LyingDetector;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

/**
 * Created by batrakov on 01.12.17.
 */
public class SensorService extends Service implements SensorEventListener {

    private static final int WAKE_LOCK_TIMER = 5000;
    private static final long BORDER_FOR_LAST_SENSOR_CHANGE = 3000000000L;
    private static final int AMOUNT_OF_LYING_CHECK_FOR_TRIGGER_NOTIFICATION = 4;

    private long mTimeToStop;
    private PowerManager.WakeLock mWakeLock;
    private int mMovingCounter = 0;
    private float mXCoordinateSum;
    private float mYCoordinateSum;
    private float mZCoordinateSum;
    private int mAmountOfCoordinates;
    private float mXInterpolatedCoordinate;
    private float mYInterpolatedCoordinate;
    private float mZInterpolatedCoordinate;
    private boolean mNeedToAnalyze;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mXCoordinateSum = 0;
        mYCoordinateSum = 0;
        mZCoordinateSum = 0;
        mAmountOfCoordinates = 0;
        mXInterpolatedCoordinate = 0;
        mYInterpolatedCoordinate = 0;
        mZInterpolatedCoordinate = 0;
        mNeedToAnalyze = true;

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        mTimeToStop = 0;
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "MyWakelockTag");
            mWakeLock.acquire(WAKE_LOCK_TIMER);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mTimeToStop == 0) {
            mTimeToStop = event.timestamp + BORDER_FOR_LAST_SENSOR_CHANGE;
        }
        if (mNeedToAnalyze) {
            if (event.timestamp < mTimeToStop) {
                mXCoordinateSum += event.values[0];
                mYCoordinateSum += event.values[1];
                mZCoordinateSum += event.values[2];
                mAmountOfCoordinates++;
            } else {
                mNeedToAnalyze = false;
                try {
                    processCoordinates();
                    if (isLying()) {
                        mMovingCounter++;
                        System.out.println(mMovingCounter);
                        if (mMovingCounter >= AMOUNT_OF_LYING_CHECK_FOR_TRIGGER_NOTIFICATION) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), "Test channel");
                            builder.setContentTitle("SensorService")
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
                        mMovingCounter = 0;
                    }

                    mWakeLock.release();
                } catch (RuntimeException ignored) {
                }

            }
        }
    }

    private void processCoordinates() {
        mXInterpolatedCoordinate = mXCoordinateSum / mAmountOfCoordinates;
        mYInterpolatedCoordinate = mYCoordinateSum / mAmountOfCoordinates;
        mZInterpolatedCoordinate = mZCoordinateSum / mAmountOfCoordinates;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean isLying() {
        if (Math.abs(mXInterpolatedCoordinate) < 2) {
            if (Math.abs(mYInterpolatedCoordinate) < 2) {
                if (Math.abs(mZInterpolatedCoordinate) > 7) {
                    return true;
                }
            }
        }
        return false;
    }


}
