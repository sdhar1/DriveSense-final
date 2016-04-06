package wisc.drivesense.sensor;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import wisc.drivesense.database.DatabaseHelper;


public class SensorServiceConnection implements ServiceConnection {

    private SensorService.SensorBinder service_ = null;
    private DatabaseHelper dbHelper_ = null;

    private static String TAG = "SensorServiceConnection";
    public SensorServiceConnection(DatabaseHelper dbhelper) {
        dbHelper_ = dbhelper;
    }
    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "service connected");
        service_ = (SensorService.SensorBinder)binder;
        service_.setDatabaseHelper(dbHelper_);
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
        // TODO Auto-generated method stub
        service_ = null;
    }

    /**
     * Sets a callback in the service.
     *
     * @param listener
     */
    public void setDatabaseHelper(DatabaseHelper dbhelper) {
        dbHelper_ = dbhelper;
    }

    public boolean isRunning() {
        if (service_ == null) {
            return false;
        }

        return service_.isRunning();
    }

    public double getSpeed() {
        return this.service_.getSpeed();
    }



}