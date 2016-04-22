package wisc.drivesense.uploader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import wisc.drivesense.database.DatabaseHelper;

/**
 * Created by lkang on 3/30/16.
 */
public class UploaderService extends Service {

    private static String TAG = "uploader";
    //doing nothing at this version
    private DatabaseHelper dbHelper_ = null;

    private final Binder binder_ = new UploaderBinder();
    private AtomicBoolean isRunning_ = new AtomicBoolean(false);


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

        startService();
        return START_STICKY;
    }

    private void startService() {
        Log.d(TAG, "Starting uploding service");
        isRunning_.set(true);

        /*
        String time = "summary";
        String url = "http://128.105.22.44:8000/upload";
        String devid = "id";
        String dbname = time + ".db";
        SendHttpRequestTask t = new SendHttpRequestTask();
        String[] params = new String[]{url, devid, dbname};
        t.execute(params);
        Log.d(TAG, String.valueOf(t.getStatus() == AsyncTask.Status.FINISHED));
        */
    }


    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {

            Log.d(TAG, "Sending file");
            String url = params[0];
            String devid = params[1];
            String dbname = params[2];

            byte[] byteArray = null;
            try {
                File dbfile = new File(dbHelper_.DB_PATH + dbname);
                long fsz = dbfile.length();
                InputStream inputStream = new FileInputStream(dbfile);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] b = new byte[(int)fsz];
                int bytesRead =0;
                while ((bytesRead = inputStream.read(b)) != -1) {
                    bos.write(b, 0, bytesRead);
                }
                byteArray = bos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            String res = null;
            try {
                HttpClient client = new HttpClient(url);
                client.connectForMultipart();
                client.addFormPart("deviceid", devid);
                client.addFilePart("file", dbname, byteArray);
                client.finishMultipart();
                res = client.getResponse();
            } catch(Throwable t) {
                t.printStackTrace();
            }
            return res;
        }

    }




    public void stopService() {
        Log.d(TAG, "Stopping service..");
        //stop service only when car halt
        isRunning_.set(false);

        stopSelf();

    }



    public class UploaderBinder extends Binder {
        public void setDatabaseHelper(DatabaseHelper dbhelper) {
            dbHelper_ = dbhelper;
        }
        public boolean isRunning() {
            return isRunning_.get();
        }
        public UploaderService getService() {
            return UploaderService.this;
        }
    }


}
