package wisc.drivesense.triprecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import wisc.drivesense.activity.MainActivity;

public class ChargingStateReceiver extends BroadcastReceiver {

    private static String TAG = "ChargingStateReceiver";
    private static Intent mDrivingDetectionIntent = null;
    @Override
    public void onReceive(Context context, Intent intent) {

        //check charging status, and start sensor service automatically
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            Log.d(TAG, "plugged");
            if(MainActivity.isServiceRunning(context, TripService.class) == false) {
                Log.d(TAG, "Start driving detection service!!!");
                mDrivingDetectionIntent = new Intent(context, TripService.class);
                context.startService(mDrivingDetectionIntent);
            }

        } else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Log.d(TAG, "unplugged");
            if(MainActivity.isServiceRunning(context, TripService.class) == true) {
                Log.d(TAG, "Stop driving detection service!!!");
                context.stopService(mDrivingDetectionIntent);
                mDrivingDetectionIntent = null;
             }
        } else {

        }

    }



}
