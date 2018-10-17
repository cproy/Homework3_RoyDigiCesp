package com.example.ehar.SensorExample;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Observable;

/**
 * Created by ehar on 8/12/2016.
 */
public class AccelerometerHandler
        extends Observable
        implements SensorEventListener {

    private int THRESHOLD;

    private Sensor accelerometer = null;
    private SensorManager sensorManager = null;
    private long prev_time = 0;

    public AccelerometerHandler(int threshold, Activity act) {
        THRESHOLD = threshold;
        prev_time = System.currentTimeMillis();
        sensorManager = (SensorManager) act.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        long curr_time = System.currentTimeMillis();

        if (curr_time - prev_time > THRESHOLD) {
            prev_time = curr_time;
            setChanged();
            notifyObservers(event.values);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }
}
