package wisc.drivesense.uploader;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static String TAG = "NetworkStateReceiver";


    private static Intent mUploaderIntent = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();


        if(isConnected) {
            Log.d(TAG, "Internet is Connected!");
            if(isMyServiceRunning(context) == false) {
                Log.d(TAG, "Start upload service!!!");
                mUploaderIntent = new Intent(context, UploaderService.class);
                context.startService(mUploaderIntent);
            }
        } else {
            Log.d(TAG, "Internet is Closed!");
            //end uploading
            if(isMyServiceRunning(context) == true) {
                Log.d(TAG, "Stop upload service!!!");
                context.stopService(mUploaderIntent);
            }
        }
    }

    private boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (UploaderService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
