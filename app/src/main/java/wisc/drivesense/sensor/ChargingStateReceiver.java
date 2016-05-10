package wisc.drivesense.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

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
            if(MainActivity.isServiceRunning(context, DrivingDetectionService.class) == false) {
                Log.d(TAG, "Start driving detection service!!!");
                mDrivingDetectionIntent = new Intent(context, DrivingDetectionService.class);
                context.startService(mDrivingDetectionIntent);
            }

        } else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Log.d(TAG, "unplugged");
            if(MainActivity.isServiceRunning(context, DrivingDetectionService.class) == true) {
                Log.d(TAG, "Stop driving detection service!!!");
                context.stopService(mDrivingDetectionIntent);
                mDrivingDetectionIntent = null;
            }
        } else {

        }

    }



}
