
package wisc.drivesense.activity;

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

import java.io.File;

import wisc.drivesense.R;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.rating.Rating;
import wisc.drivesense.sensor.SensorService;
import wisc.drivesense.sensor.SensorServiceConnection;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;


public class MainActivity extends AppCompatActivity {


    private static DatabaseHelper dbHelper_;
    private Trip curtrip_;
    private int started = 0;
    private Rating rating;

    /////////////
    private static Intent mSensorIntent = null;
    //dumb service connection, almost useless, use local broadcast receiver instead
    private static SensorServiceConnection mSensorServiceConnection = null;


    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
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

        //Setting custom font for TextViews
        //TextView setfont=(TextView)findViewById(R.id.textspeed);
        //Typeface DriveSenseFont=Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-Light.ttf");
        //setfont.setTypeface(DriveSenseFont);
//        ImageView imageViewIcon = (ImageView)findViewById(R.id.imageView);
//        imageViewIcon.setColorFilter(getContext().getResources().getColor(R.color.primary_dark_material_dark));

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

    public void showDriveRating(Trip trip) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("Current Trip", trip);
        startActivity(intent);
    }





    public void showHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
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
        mSensorServiceConnection = new SensorServiceConnection(dbHelper_);
        Log.d(TAG, "Binding sensor service..");
        bindService(mSensorIntent, mSensorServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mSensorIntent);

    }


    private synchronized void stopRunning() {

        Log.d(TAG, "Stopping live data..");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        if(curtrip_.getDistance() >= 0.5 && curtrip_.getDuration() >= 2.0) {
            dbHelper_.insertTrip(curtrip_);
        }
        dbHelper_.closeDatabase();


        if (mSensorServiceConnection != null && mSensorServiceConnection.isRunning()) {
            Log.d(TAG, "stop sensor servcie");
            unbindService(mSensorServiceConnection);
            stopService(mSensorIntent);
            mSensorIntent = null;
            mSensorServiceConnection = null;
        }
    }

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
                TextView tvSpeed = (TextView) findViewById(R.id.textspeed);
                TextView tvMiles = (TextView) findViewById(R.id.milesdriven);
                tvSpeed.setText(String.format("%.1f", curtrip_.getSpeed()));
                tvMiles.setText(String.format("%.1f", curtrip_.getDistance()));
            }
            if(rating != null) {
                rating.readingData(trace);
            }



        }
    };

    protected void onPause() {
        super.onPause();
    }
    protected void onResume() {
        super.onResume();
    }

}


