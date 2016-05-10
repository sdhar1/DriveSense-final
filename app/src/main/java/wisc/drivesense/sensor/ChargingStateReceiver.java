package wisc.drivesense.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import wisc.drivesense.activity.MainActivity;

public class ChargingStateReceiver extends BroadcastReceiver {

    private static String TAG = "ChargingStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        //check charging status, and start sensor service automatically
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_POWER_CONNECTED)) {
            // Do something when power connected
            Log.d(TAG, "plugged");

        } else if(action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
            // Do something when power disconnected
            Log.d(TAG, "unplugged");

        } else {

        }

    }



}
