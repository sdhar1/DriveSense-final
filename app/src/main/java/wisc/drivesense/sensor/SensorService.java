package wisc.drivesense.sensor;
import java.util.concurrent.atomic.AtomicBoolean;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import wisc.drivesense.utility.Trace;
import wisc.drivesense.database.DatabaseHelper;

public class SensorService extends Service implements SensorEventListener, LocationListener {


    //doing nothing at this version
    private DatabaseHelper dbHelper_ = null;

    private final Binder binder_ = new SensorBinder();
    private AtomicBoolean isRunning_ = new AtomicBoolean(false);

    private SensorManager sensorManager;
    private LocationManager locationManager;

    int numberOfSensors = 3;
    int[] sensorType = {Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE, Sensor.TYPE_MAGNETIC_FIELD,
    };

    /*Marked: for orientation*/
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];


    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];

    private long tLastGyroscope = 0;
    private long tLastAccelerometer = 0;
    private long tLastMagnetometer = 0;


    private double latitude_;
    private double longitude_;
    private double speed_;


    private final String TAG = "Sensor Service";


    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "location update speed:" + String.valueOf(location.getSpeed()));
        // TODO Auto-generated method stub
        if(location != null && dbHelper_ != null){
            speed_ = location.getSpeed();
            Trace trace = new Trace(3);
            trace.time = System.currentTimeMillis();
            trace.values[0] = (float) location.getLatitude();
            trace.values[1] = (float) location.getLongitude();
            trace.values[2] = location.getSpeed();

            //dbHelper_.insertSensorData(trace);
            sendTrace(trace);
        }

    }

    @Override
    public void onProviderDisabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(isRunning_.get()==false/* || dbHelper_ == null*/) {
            return;
        }

        int type = event.sensor.getType();
        long time = System.currentTimeMillis();
        if(type== Sensor.TYPE_MAGNETIC_FIELD && (time - tLastMagnetometer) >= 200) {
            tLastMagnetometer = time;
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;

            Trace trace = new Trace(3);
            trace.time = time;
            trace.type = Trace.MAGNETOMETER;
            System.arraycopy(event.values, 0, trace.values, 0, event.values.length);

            //dbHelper_.insertSensorData(trace);
            sendTrace(trace);

        } else if(type==Sensor.TYPE_ACCELEROMETER && (time - tLastAccelerometer) >= 200) {
            tLastAccelerometer = time;
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
            //store into database
            //Log.d(TAG, mLastAccelerometer.toString());

            Trace trace = new Trace(3);
            trace.time = time;
            trace.type = Trace.ACCELEROMETER;
            System.arraycopy(event.values, 0, trace.values, 0, event.values.length);

            //dbHelper_.insertSensorData(trace);
            sendTrace(trace);

        } else if (type == Sensor.TYPE_GYROSCOPE && (time - tLastGyroscope) >= 200) {
            tLastGyroscope = time;


            Trace trace = new Trace(3);
            trace.time = time;
            trace.type = Trace.GYROSCOPE;
            System.arraycopy(event.values, 0, trace.values, 0, event.values.length);
            //dbHelper_.insertSensorData(trace);
            sendTrace(trace);

        } else {

        }

        /*Marked*/
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            mLastMagnetometerSet = false;
            mLastAccelerometerSet = false;

            Trace trace = new Trace(9);
            trace.time = time;
            trace.type = Trace.ROTATION_MATRIX;
            System.arraycopy(mR, 0, trace.values, 0, mR.length);
            //dbHelper_.insertSensorData(trace);
            sendTrace(trace);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return binder_;
    }

    public class SensorBinder extends Binder {
        public void setDatabaseHelper(DatabaseHelper dbhelper) {
            dbHelper_ = dbhelper;
        }
        public boolean isRunning() {
            return isRunning_.get();
        }
        public SensorService getService() {
            return SensorService.this;
        }
        public double getSpeed() {return speed_;}
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        startService();
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "stop service");
        sensorManager.unregisterListener(this);
        locationManager.removeUpdates(this);
        isRunning_.set(false);
        dbHelper_ = null;
        stopSelf();
    }

    private void startService() {
        Log.d(TAG, "start service");
        isRunning_.set(true);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        for(int i = 0; i < numberOfSensors; ++i) {
            Sensor sensor = sensorManager.getDefaultSensor(sensorType[i]);
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        isRunning_.set(true);
    }

    private void sendTrace(Trace trace) {
        Log.d(TAG, trace.toJson());
        Intent intent = new Intent("sensor");
        intent.putExtra("trace", trace.toJson());

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


}