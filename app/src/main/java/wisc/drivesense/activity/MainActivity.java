
package wisc.drivesense.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import wisc.drivesense.R;
import wisc.drivesense.triprecorder.TripService;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;


public class MainActivity extends AppCompatActivity {

    //for display usage only, all calculation is conducted in TripService
    private Trip curtrip_ = null;

    private int started = 0;

    private static String TAG = "MainActivity";

    private TextView tvScore = null;
    private TextView tvTime = null;
    private Button btnStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvScore = (TextView) findViewById(R.id.textscore);
        tvTime = (TextView) findViewById(R.id.duration);
        btnStart = (Button) findViewById(R.id.btnstart);

        android.support.v7.widget.Toolbar mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.maintoolbar);
        setSupportActionBar(mToolbar);

        File dbDir = new File(Constants.kDBFolder);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
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


        //final TextView txtView= (TextView) findViewById(R.id.textspeed);
        btnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(SettingActivity.isAutoMode(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "Disable Auto Mode in Settings", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(TAG, "start button clicked");
                if (MainActivity.isServiceRunning(MainActivity.this, TripService.class) == false) {
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
                    showDriveRating();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user:
                showUser();
                return true;

            case R.id.settings:
                showSettings();
                return true;

            case R.id.history:
                showHistory();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    private static Intent mTripServiceIntent = null;
    private synchronized void startRunning() {
        Log.d(TAG, "start running");

        curtrip_ = new Trip(System.currentTimeMillis());
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("driving"));

        mTripServiceIntent = new Intent(this, TripService.class);
        if(MainActivity.isServiceRunning(this, TripService.class) == false) {
            Log.d(TAG, "Start driving detection service!!!");
            startService(mTripServiceIntent);
        }

        startTimer();

        //displayWarning();
    }

    private synchronized void stopRunning() {

        Log.d(TAG, "Stopping live data..");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        tvScore.setText(String.format("%.1f", 10.0));
        //tvTime.setText(String.format("%.2f", 0.00));
        if(handler != null) {
            handler.removeCallbacks(runnable);
        }

        if(MainActivity.isServiceRunning(this, TripService.class) == true) {
            Log.d(TAG, "Stop driving detection service!!!");
            stopService(mTripServiceIntent);
            mTripServiceIntent = null;
        }

    }


    private void displayWarning() {
        Toast toast = new Toast(MainActivity.this);
        ImageView view = new ImageView(MainActivity.this);
        view.setImageResource(R.drawable.attention_512);

        toast.setView(view);
        toast.show();
    }

    //
    /**
     * where we get the sensor data
     */

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("trip");
            Log.d(TAG, "Got message: " + message);
            Trace trace = new Trace();
            trace.fromJson(message);
            curtrip_.addGPS(trace);
            tvScore.setText(String.format("%.1f", curtrip_.getScore()));

            long ms = trace.time - curtrip_.getStartTime();
            tvTime.setText(timeFormat(ms));

            if(trace.values[2] < 0) {
                displayWarning();
            }
        }
    };


    public void showDriveRating() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("Current Trip", curtrip_);
        startActivity(intent);
    }

    public void showSettings() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    public void showHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void showUser() {
        Intent intent = new Intent(this, UserActivity.class);
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


    protected void onPause() {
        Log.d(TAG, "onPuase");
        super.onPause();
    }
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        /*
        if(curtrip_ == null) {
            Log.d(TAG, "curtrip is null");
        }

        if(MainActivity.isServiceRunning(this, TripService.class) == true) {
            btnStart.setBackgroundResource(R.drawable.stop_button);
            btnStart.setText(R.string.stop_button);
        } else {
            btnStart.setBackgroundResource(R.drawable.start_button);
            btnStart.setText(R.string.start_button);
        }
        */
    }
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        //stopRunning();
    }

    private Handler handler = null;
    private Runnable runnable;
    private String timeFormat(long millis) {
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        return hms;
    }

    public void startTimer() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 1000);
                long ms = System.currentTimeMillis() - curtrip_.getStartTime();
                tvTime.setText(timeFormat(ms));
            }
        };
        handler.postDelayed(runnable, 0);
    }

}


