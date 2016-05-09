package wisc.drivesense.uploader;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.Toast;

import wisc.drivesense.activity.MainActivity;
import wisc.drivesense.sensor.SensorService;

public class StateReceiver extends BroadcastReceiver {

    private static String TAG = "StateReceiver";


    private static Intent mUploaderIntent = null;
    private static Intent mSensorIntent = null;
    private static Intent mMainActivity = null;


    @Override
    public void onReceive(Context context, Intent intent) {

        //check internet connecction, and do uploading
        /*
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(isConnected) {
            Log.d(TAG, "Internet is Connected!");
            if(MainActivity.isMyServiceRunning(context, UploaderService.class) == false) {
                Log.d(TAG, "Start upload service!!!");
                mUploaderIntent = new Intent(context, UploaderService.class);
                context.startService(mUploaderIntent);
            }
        } else {
            Log.d(TAG, "Internet is Closed!");
            //end uploading
            if(MainActivity.isMyServiceRunning(context, UploaderService.class) == true) {
                Log.d(TAG, "Stop upload service!!!");
                context.stopService(mUploaderIntent);
                mUploaderIntent = null;
            }
        }
        */
        //check charging status, and start sensor service automatically

        String action = intent.getAction();
        Toast.makeText(context, action, Toast.LENGTH_LONG).show();
        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            Log.d(TAG, "plugged");
            if(MainActivity.isMyServiceRunning(context, MainActivity.class) == false) {
                Log.d(TAG, "starting activity");
                mMainActivity = new Intent();
                mMainActivity.setClass(context, MainActivity.class);
                mMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(mMainActivity);
            }
            /*
            if(MainActivity.isMyServiceRunning(context, SensorService.class) == false) {
                mSensorIntent = new Intent(context, SensorService.class);
                context.startService(mSensorIntent);
            }
            */
        }
        else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Log.d(TAG, "unplugged");
            if(MainActivity.isMyServiceRunning(context, MainActivity.class) == true) {
                mMainActivity = new Intent(context, MainActivity.class);
                context.stopService(mMainActivity);
            }
            /*
            if(MainActivity.isMyServiceRunning(context, SensorService.class) == true) {
                context.stopService(mSensorIntent);
                mSensorIntent = null;
            }
            */
        }

    }



}
