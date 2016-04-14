package wisc.drivesense.uploader;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import wisc.drivesense.database.DatabaseHelper;
import wisc.drivesense.sensor.SensorService;


public class UploaderServiceConnection implements ServiceConnection {

    private UploaderService.UploaderBinder service_ = null;
    private DatabaseHelper dbHelper_ = null;

    private static String TAG = "SensorServiceConnection";
    public UploaderServiceConnection(DatabaseHelper dbhelper) {
        dbHelper_ = dbhelper;
    }
    @Override
    public void onServiceConnected(ComponentName arg0, IBinder binder) {
        // TODO Auto-generated method stub
        Log.d(TAG, "service connected");
        service_ = (UploaderService.UploaderBinder)binder;
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




}