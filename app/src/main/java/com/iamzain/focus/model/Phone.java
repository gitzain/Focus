package com.iamzain.focus.model;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Phone implements SensorEventListener {

    private float lastX, lastY, lastZ;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float deltaXMax = 0;
    private float deltaYMax = 0;
    private float deltaZMax = 0;
    private float deltaX = 0;
    private float deltaY = 0;
    private float deltaZ = 0;
    private float vibrateThreshold = 0;
    private boolean doNotDisturb;
    private NotificationManager mNotificationManager;
    private Activity activeActivity;

    public Phone(Activity currentActivity)
    {
        activeActivity = currentActivity;
        sensorManager = (SensorManager) activeActivity.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // success! we have an accelerometer

            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            vibrateThreshold = accelerometer.getMaximumRange() / 2;
        } else {
            // fai! we dont have an accelerometer!
        }




        // Get the notification manager instance
        mNotificationManager = (NotificationManager) activeActivity.getSystemService(NOTIFICATION_SERVICE);


        try {
            int value = Settings.Global.getInt(activeActivity.getContentResolver(), "zen_mode");
            if (value == 0)
                doNotDisturb = false;
            else
                doNotDisturb = true;

        }
        catch (Exception e)
        {

        }
    }

    protected void onResume() {
        //super.onResume();
        //super.onResume();
        //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // get the change of the x,y,z values of the accelerometer
        deltaX = event.values[0];
        deltaY = event.values[1];
        deltaZ = event.values[2];

        // if the change is below 2, it is just plain noise
        if (deltaX < 2)
            deltaX = 0;

        if (deltaY < 2)
            deltaY = 0;

        if (deltaZ >= 9)
            deltaZ = 10;

        if (deltaZ <= -9)
            deltaZ = -10;


        if (deltaX == 0 && deltaY == 0 && deltaZ == -10)
        {
            if (doNotDisturb == true)
                return;
            else {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALARMS);
                doNotDisturb = true;
            }


        }
        else
        {
            if (doNotDisturb == false)
                return;
            else {
                changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALL);
                doNotDisturb = false;
            }

        }


    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }



    protected void changeInterruptionFiler(int interruptionFilter){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        { // If api level minimum 23
            if(hasDoNotDisturbPermissions())
            {
                mNotificationManager.setInterruptionFilter(interruptionFilter);
            }
            else
            {
                getDoNotDisturbPermissions();
            }
        }
    }

    public boolean hasDoNotDisturbPermissions()
    {
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M) { // If api level minimum 23
            if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                return true;
            }
        }
        else
        {
            return true;
        }

        return false;
    }

    public void getDoNotDisturbPermissions()
    {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(activeActivity);
        builder.setTitle("Permission needed")
                .setMessage("For this app to work properly we need the Do Not Disturb permission. Tap OK below to manually grant access.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        activeActivity.startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activeActivity.finish();
                    }
                })
                .show();
    }


}
