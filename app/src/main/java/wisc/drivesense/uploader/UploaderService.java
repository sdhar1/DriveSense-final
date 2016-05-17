package wisc.drivesense.uploader;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.utility.Constants;

/**
 * Created by lkang on 3/30/16.
 */
public class UploaderService extends Service {

    private static String TAG = "uploader";
    //doing nothing at this version
    private DatabaseHelper dbHelper_ = new DatabaseHelper();

    private AtomicBoolean isRunning_ = new AtomicBoolean(false);


    private SendHttpRequestTask httpRequest = null;

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
        //Toast.makeText(this, TAG + " onDestroy", Toast.LENGTH_LONG).show();
        stopService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        //Toast.makeText(this, TAG + " onStartCommand", Toast.LENGTH_LONG).show();

        startService();
        return START_STICKY;
    }

    private void startService() {
        Log.d(TAG, "Starting uploding service");
        isRunning_.set(true);

        selectAndUploadOneFile(/*null*/"do not upload summary");
    }

    private void selectAndUploadOneFile(String pre) {

        String dbname = null;
        if(pre == null) {
            //upload summary first
            dbname = "summary";
        } else {
            //upload next trip
            long time = dbHelper_.nextTripToUpload();
            if(-1 == time) {
                stopService();
                return;
            }
            dbname = String.valueOf(time);
        }

        String androidid = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        int len = androidid.length();
        androidid = androidid.substring(len - 6);
        Log.d(TAG, "uploading" + dbname);
        String[] params = new String[]{Constants.kUploadURL, androidid, dbname};

        httpRequest = new SendHttpRequestTask();
        httpRequest.execute(params);

        SystemClock.sleep(1000 * 10);
    }


    private class SendHttpRequestTask extends AsyncTask<String, Void, String> {

        protected void onPostExecute(String result) {
            Log.d(TAG, "uploading result:" + result);
            if(result == null) {
                //server is done, retry later
                stopService();
                return;
            }
            //confirm the trip is uploaded
            try {
                long time = Long.parseLong(result);
                dbHelper_.tripUploadDone(time);
            } catch (Exception e) {
                    //uploaded summary istead of trips
                Log.d(TAG, e.toString());
            }
            this.cancel(true);
            selectAndUploadOneFile(result);
        }

        protected String doInBackground(String... params) {

            Log.d(TAG, "Sending file");
            String url = params[0];
            String devid = params[1];
            String dbname = params[2];

            byte[] byteArray = null;
            try {
                File dbfile = new File(Constants.kDBFolder + dbname + ".db");
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
                client.addFilePart("file", dbname + ".db", byteArray);
                client.finishMultipart();
                res = client.getResponse();
            } catch(Throwable t) {
                t.printStackTrace();
                return null;
            }
            //expect the server returns an okay package
            //return a null if not okay
            if(null != res && res.contains("okay")) {
                return dbname;
            } else {
                return null;
            }
        }

    }


    public void stopService() {
        Log.d(TAG, "Stopping service..");

        if(httpRequest != null && httpRequest.isCancelled() == false) {
            httpRequest.cancel(true);
        }
        if(dbHelper_ != null && dbHelper_.isOpen()) {
            dbHelper_.closeDatabase();
        }

        isRunning_.set(false);
        stopSelf();

    }

}
