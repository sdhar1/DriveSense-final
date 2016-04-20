
package wisc.drivesense.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.File;

import wisc.drivesense.R;
import wisc.drivesense.sensor.SensorService;
import wisc.drivesense.sensor.SensorServiceConnection;
import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.uploader.UploaderService;
import wisc.drivesense.uploader.UploaderServiceConnection;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;


public class MainActivity extends AppCompatActivity {


    private Spinner spinnerOn, spinnerOff;
    private Button btnSubmit, btnSet;
    private EditText total;

    private TextView txtSpeed;
    private static DatabaseHelper dbHelper_;
    private Trip curtrip_;
    private int started=0;
    /////////////
    private static Intent mSensorIntent = null;
    private static SensorServiceConnection mSensorServiceConnection = null;

    private static Intent mUploaderIntent = null;
    private static UploaderServiceConnection mUploaderServiceConnection = null;

    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(mToolbar);

        dbHelper_ = new DatabaseHelper(this);
        curtrip_ = new Trip();

        addListenerOnButton();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        //Setting custom font for TextViews
        TextView setfont=(TextView)findViewById(R.id.textspeed);
        Typeface DriveSenseFont=Typeface.createFromAsset(getAssets(), "fonts/JosefinSans-Light.ttf");
        setfont.setTypeface(DriveSenseFont);
//        ImageView imageViewIcon = (ImageView)findViewById(R.id.imageView);
//        imageViewIcon.setColorFilter(getContext().getResources().getColor(R.color.primary_dark_material_dark));

        return true;
    }

    //get the selected dropdown list value
    public void addListenerOnButton() {

        final Button btnStart = (Button) findViewById(R.id.btnstart);
        final TextView txtView= (TextView) findViewById(R.id.textspeed);
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (started == 0) {
                    Toast.makeText(MainActivity.this, "Service Started!", Toast.LENGTH_SHORT).show();
                    startRunning();
                    started = 1;
                    txtView.setText("0"); // speed variable to be displayed here instead of 0
                    txtView.setTextSize(50);
                    btnStart.setBackgroundResource(R.drawable.stop_button);
                    btnStart.setText(R.string.stop_button);
                } else {
                    Toast.makeText(MainActivity.this, "Service Stopped!", Toast.LENGTH_SHORT).show();
                    stopRunning();
                    started = 0;
                    txtView.setText(R.string.pressButton); // speed variable to be displayed here instead of 0
                    txtView.setTextSize(20);
                    btnStart.setBackgroundResource(R.drawable.start_button);
                    btnStart.setText(R.string.start_button);
                    showDriveRating(curtrip_);

                }
            }
        });
    }

    public void showDriveRating(Trip curtrip) {
        Intent intent = new Intent(this, DriveRatingActivity.class);
        intent.putExtra("Current Trip", curtrip_);
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
        curtrip_.setStartTime(time);

       // LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("sensor"));

        mSensorIntent = new Intent(this, SensorService.class);
        mSensorServiceConnection = new SensorServiceConnection(dbHelper_);
        Log.d(TAG, "Binding sensor service..");
        bindService(mSensorIntent, mSensorServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mSensorIntent);


        mUploaderIntent = new Intent(this, UploaderService.class);
        mUploaderServiceConnection = new UploaderServiceConnection(dbHelper_);
        Log.d(TAG, "Binding uploader service..");
        bindService(mUploaderIntent, mUploaderServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mUploaderIntent);

    }


    private synchronized void stopRunning() {

        Log.d(TAG, "Stopping live data..");
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);


        curtrip_.setEndTime(System.currentTimeMillis());
        dbHelper_.insertTrip(curtrip_);
        dbHelper_.closeDatabase();


        if (mSensorServiceConnection != null && mSensorServiceConnection.isRunning()) {
            Log.d(TAG, "stop sensor servcie");
            unbindService(mSensorServiceConnection);
            stopService(mSensorIntent);
            mSensorIntent = null;
            mSensorServiceConnection = null;
        }
        if (mUploaderServiceConnection != null && mUploaderServiceConnection.isRunning()) {
            Log.d(TAG, "stop sensor servcie");
            unbindService(mUploaderServiceConnection);
            stopService(mUploaderIntent);
            mUploaderIntent = null;
            mUploaderServiceConnection = null;
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
            if(trace.type == Trace.GPS) {
                curtrip_.addGPS(trace);
            }
            Log.d(TAG, "Got message: " + trace.toJson());

            TextView tvSpeed = (TextView) findViewById(R.id.textspeed);
            if(mSensorServiceConnection.isRunning()) {
                tvSpeed.setText(String.valueOf(mSensorServiceConnection.getSpeed()));
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


