
package wisc.drivesense.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import wisc.drivesense.R;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.rating.Rating;
import wisc.drivesense.sensor.SensorService;
import wisc.drivesense.uploader.UploaderService;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;


public class MainActivity extends AppCompatActivity {


    public static boolean running = false;

    private static DatabaseHelper dbHelper_;
    private Trip curtrip_;
    private int started = 0;
    private Rating rating;

    /////////////
    private static Intent mSensorIntent = null;

    private static String TAG = "MainActivity";

    private TextView tvSpeed = null;
    private TextView tvMiles = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed = (TextView) findViewById(R.id.textspeed);
        tvMiles = (TextView) findViewById(R.id.milesdriven);

        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(mToolbar);


        File dbDir = new File(Constants.kDBFolder);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        dbHelper_ = new DatabaseHelper();
        addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    //get the selected dropdown list value
    public void addListenerOnButton() {

        final Button btnStart = (Button) findViewById(R.id.btnstart);
        //final TextView txtView= (TextView) findViewById(R.id.textspeed);
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (started == 0) {
                    //Toast.makeText(MainActivity.this, "Service Started!", Toast.LENGTH_SHORT).show();
                    startRunning();
                    started = 1;
                    btnStart.setBackgroundResource(R.drawable.stop_button);
                    btnStart.setText(R.string.stop_button);
                } else {
                    //Toast.makeText(MainActivity.this, "Service Stopped!", Toast.LENGTH_SHORT).show();
                    stopRunning();
                    started = 0;
                    btnStart.setBackgroundResource(R.drawable.start_button);
                    btnStart.setText(R.string.start_button);
                    showDriveRating(curtrip_);
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:

                return true;

            case R.id.history:
                showHistory();
                return true;
            case R.id.about:

                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private synchronized void startRunning() {
        Log.d(TAG, "start running");

        long time = System.currentTimeMillis();
        dbHelper_.createDatabase(time);
        curtrip_ = new Trip(time);
        rating = new Rating(curtrip_);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("sensor"));

        mSensorIntent = new Intent(this, SensorService.class);
        Log.d(TAG, "Starting sensor service..");
        startService(mSensorIntent);

    }

    private synchronized void stopRunning() {

        Log.d(TAG, "Stopping live data..");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        if(curtrip_.getDistance() >= 0.3 && curtrip_.getDuration() >= 1.0) {
            dbHelper_.insertTrip(curtrip_);
        } else {
            Toast.makeText(MainActivity.this, "Trip too short, not saved!", Toast.LENGTH_SHORT).show();
            dbHelper_.deleteTrip(curtrip_.getStartTime());
        }
        dbHelper_.closeDatabase();


        stopService(mSensorIntent);
        mSensorIntent = null;

        tvSpeed.setText(String.format("%.1f", 0.0));
        tvMiles.setText(String.format("%.2f", 0.00));
    }


    //
    /**
     * where we get the sensor data
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("trace");
            Trace trace = new Trace();
            trace.fromJson(message);
            if(dbHelper_.isOpen()) {
                dbHelper_.insertSensorData(trace);
            }
            if(trace.type.compareTo(Trace.GPS) == 0) {
                Log.d(TAG, "Got message: " + trace.toJson());
                curtrip_.addGPS(trace);
                //UI
                tvSpeed.setText(String.format("%.1f", curtrip_.getSpeed()));
                tvMiles.setText(String.format("%.2f", curtrip_.getDistance()));
            }
            if(rating != null) {
                rating.readingData(trace);
                //Log.d(TAG, String.valueOf(curtrip_.getScore()));
            }
        }
    };

    public void showDriveRating(Trip trip) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("Current Trip", trip);
        startActivity(intent);
    }

    public void showHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }



    public static boolean isServiceRunning(Context context, Class running) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (running.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*
    public static boolean isActivityRunning(Context context, String name) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo app : manager.getRunningAppProcesses()) {
            if (name.equals(app.processName)) {
                return true;
            }
        }
        return false;
    }
    */

    protected void onPause() {
        super.onPause();
    }
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }



}


