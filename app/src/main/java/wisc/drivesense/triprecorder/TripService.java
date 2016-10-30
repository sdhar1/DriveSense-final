package wisc.drivesense.triprecorder;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.Rating;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;

public class TripService extends Service {

    private DatabaseHelper dbHelper_ = null;
    public Trip curtrip_ = null;
    public Rating rating_ = null;
    public RealTimeTiltCalculation tiltCal_ = null;

    public Binder _binder = new TripServiceBinder();
    private AtomicBoolean _isRunning = new AtomicBoolean(false);

    private final String TAG = "Trip Service";


    private static Intent mSensor = null;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return _binder;
    }

    public class TripServiceBinder extends Binder {
        public TripService getService() {return TripService.this;}
        public Trip getTrip() {
            return curtrip_;
        }
        public boolean isRunning() {
            return _isRunning.get();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "stop driving detection service");
        _isRunning.set(false);
        stopService(mSensor);
        mSensor = null;

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

        //validate the trip based on distance and travel time
        if(curtrip_.getDistance() >= Constants.kTripMinimumDistance && curtrip_.getDuration() >= Constants.kTripMinimumDuration) {
            Toast.makeText(this, "Saving trip in background!", Toast.LENGTH_SHORT).show();
            dbHelper_.insertTrip(curtrip_);
        } else {
            Toast.makeText(this, "Trip too short, not saved!", Toast.LENGTH_SHORT).show();
            dbHelper_.deleteTrip(curtrip_.getStartTime());
        }
        dbHelper_.closeDatabase();

        stopSelf();
    }



    private void startService() {
        _isRunning.set(true);
        Log.d(TAG, "start driving detection service");

        mSensor = new Intent(this, SensorService.class);
        startService(mSensor);

        //start recording
        File dbDir = new File(Constants.kDBFolder);
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }

        Toast.makeText(this, "Start trip in background!", Toast.LENGTH_SHORT).show();
        dbHelper_ = new DatabaseHelper();

        long time = System.currentTimeMillis();
        dbHelper_.createDatabase(time);
        curtrip_ = new Trip(time);
        rating_ = new Rating(curtrip_);
        tiltCal_ = new RealTimeTiltCalculation();

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("sensor"));
    }


    /**
     * where we get the sensor data
     */
    private long lastGPS = 0;
    private long lastSpeedNonzero = 0;
    private boolean stoprecording = false;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("trace");
            Trace trace = new Trace();
            trace.fromJson(message);
            tiltCal_.processTrace(trace);
            curtrip_.setTilt(tiltCal_.getTilt());

            if(lastSpeedNonzero == 0 && lastGPS == 0) {
                lastSpeedNonzero = trace.time;
                lastGPS = trace.time;
            }

            if(trace.type.compareTo(Trace.GPS) == 0) {
                Log.d(TAG, "Got message: " + trace.toJson());

                if(trace.values[2] != 0.0) {
                    lastSpeedNonzero = trace.time;
                }
                lastGPS = trace.time;

                trace = calculateTraceByGPS(trace);
                curtrip_.addGPS(trace);
                sendTrip(trace);
            } else if(trace.type.compareTo(Trace.ACCELEROMETER) == 0) {
                sendTrip(trace);
            } else {

            }

            long curtime = trace.time;
            if(curtime - lastSpeedNonzero > Constants.kInactiveDuration || curtime - lastGPS > Constants.kInactiveDuration) {
                stoprecording = true;
            } else {
                stoprecording = false;
            }
            if(dbHelper_.isOpen() && stoprecording == false) {
                dbHelper_.insertSensorData(trace);
            }
        }

        private Trace calculateTraceByGPS(Trace trace) {
            int brake = rating_.readingData(trace);
            //create a new trace for GPS, since we use GPS to capture driving behaviors
            Trace ntrace = new Trace(7);
            ntrace.type = trace.type;
            ntrace.time = trace.time;
            System.arraycopy(trace.values, 0, ntrace.values, 0, trace.values.length);
            ntrace.values[5] = ntrace.values[3];
            ntrace.values[3] = (float)curtrip_.getScore();
            ntrace.values[4] = (float)brake;
            ntrace.values[6] = (float)tiltCal_.getTilt();
            return ntrace;
        }
    };

    private void sendTrip(Trace trace) {
        Intent intent = new Intent("driving");
        intent.putExtra("trip", trace.toJson());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }




}
