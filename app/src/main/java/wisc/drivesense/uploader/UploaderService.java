package wisc.drivesense.uploader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import wisc.drivesense.database.DatabaseHelper;

/**
 * Created by lkang on 3/30/16.
 */
public class UploaderService extends Service {

    private static String TAG = "uploader";
    private AtomicBoolean _isRunning = new AtomicBoolean(false);
    DatabaseHelper _dbHelper = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "oncreate");
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, TAG + " onDestroy", Toast.LENGTH_LONG).show();
        stopService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        Toast.makeText(this, TAG + " onStartCommand", Toast.LENGTH_LONG).show();

        Bundle extras = intent.getExtras();
        String mac = null;
        if(extras != null) {
            mac = (String) extras.get("mac");
        }
        startService(mac);
        return START_STICKY;
    }

    private void startService(String mac) {
        Log.d(TAG, "Starting service with mac:" + mac);

        if(null == mac || "".equals(mac)) {
            stopService();
        }

    }



    public void stopService() {
        Log.d(TAG, "Stopping service..");
        //stop service only when car halt
        _isRunning.set(false);

        stopSelf();

    }





}
