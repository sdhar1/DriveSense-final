package wisc.drivesense.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;


public class DatabaseHelper {

    // Logcat tag
    private static final String TAG = "DatabaseHelper";

    private static SQLiteDatabase meta_ = null;
    private static SQLiteDatabase db_ = null;

    private static String DB_PATH = "/data/data/wisc.drivesense/files/";
    //private static String DB_PATH = "/sdcard/databases/";

    // Database Version
    private static final String DATABASE_NAME = "summary.db";
    private static final String TABLE_META = "meta";
    private static final String CREATE_TABLE_META = "CREATE TABLE IF NOT EXISTS "
            + TABLE_META + "(starttime INTEGER, endtime INTEGER, distance REAL, deleted INTEGER, uploaded INTEGER);";


    // Table Names
    private static final String TABLE_ACCELEROMETER = "accelerometer";
    private static final String TABLE_GYROSCOPE = "gyroscope";
    private static final String TABLE_MAGNETOMETER = "magnetometer";
    private static final String TABLE_ROTATION_MATRIX = "rotation_matrix";
    private static final String TABLE_GPS = "gps";


    private static final String KEY_TIME = "time";

    /*rotation matrix*/
    private static final String KEY_VALUES[] = {"x0", "x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8"};


    // Table Create Statements

    private static final String CREATE_TABLE_GPS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GPS + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + KEY_VALUES[0]
            + " REAL," + KEY_VALUES[1] + " REAL," +  KEY_VALUES[2] + " REAL" + ");";
    private static final String CREATE_TABLE_ACCELEROMETER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ACCELEROMETER + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + KEY_VALUES[0]
            + " REAL," + KEY_VALUES[1] + " REAL," +  KEY_VALUES[2] + " REAL" + ");";
    private static final String CREATE_TABLE_GYROSCOPE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GYROSCOPE + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + KEY_VALUES[0]
            + " REAL," + KEY_VALUES[1] + " REAL," +  KEY_VALUES[2] + " REAL" + ");";
    private static final String CREATE_TABLE_MAGNETOMETER = "CREATE TABLE IF NOT EXISTS "
            + TABLE_MAGNETOMETER + "(" + KEY_TIME + " INTEGER PRIMARY KEY," + KEY_VALUES[0]
            + " REAL," + KEY_VALUES[1] + " REAL," +  KEY_VALUES[2] + " REAL" + ");";


    private static final String CREATE_TABLE_ROTATION_MATRIX = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ROTATION_MATRIX + "(" + KEY_TIME + " INTEGER PRIMARY KEY,"
            + KEY_VALUES[0] + " REAL," + KEY_VALUES[1] + " REAL," +  KEY_VALUES[2] + " REAL,"
            + KEY_VALUES[3] + " REAL," + KEY_VALUES[4] + " REAL," +  KEY_VALUES[5] + " REAL,"
            + KEY_VALUES[6] + " REAL," + KEY_VALUES[7] + " REAL," +  KEY_VALUES[8] + " REAL"
            + ")";



    private Context context = null;
    private long time = 0;
    private boolean opened = false;
    // public interfaces
    public DatabaseHelper(Context cont) {

        this.context = cont;
        //openOrCreateDatabase(DATABASE_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
        File dir = this.context.getFilesDir();
        meta_ = SQLiteDatabase.openOrCreateDatabase(dir.toString() + "/" + DATABASE_NAME, null, null);
        meta_.execSQL(CREATE_TABLE_META);
        //we never close meta_ explicitly, maybe
    }


    //open and close for each trip
    public void createDatabase(long t) {
        this.time = t;
        this.opened = true;
        db_ = SQLiteDatabase.openOrCreateDatabase(DB_PATH + String.valueOf(t).concat(".db"), null, null);
        db_.execSQL(CREATE_TABLE_ACCELEROMETER);
        db_.execSQL(CREATE_TABLE_GYROSCOPE);
        db_.execSQL(CREATE_TABLE_MAGNETOMETER);
        db_.execSQL(CREATE_TABLE_GPS);
        db_.execSQL(CREATE_TABLE_ROTATION_MATRIX);
    }
    public void closeDatabase() {
        this.opened = false;
        db_.close();
    }
    public boolean isOpen() {
        return this.opened;
    }

    public void insertTrip(Trip trip) {
        ContentValues values = new ContentValues();
        values.put("starttime", trip.getStartTime());
        values.put("endtime", trip.getEndTime());
        values.put("distance", trip.getDistance());
        values.put("deleted", 0);
        values.put("uploaded", 0);
        meta_.insert(TABLE_META, null, values);
    }


    public void insertSensorData(Trace trace) {
        String type = trace.type;
        ContentValues values = new ContentValues();
        values.put(KEY_TIME, trace.time);
        for(int i = 0; i < trace.dim; ++i) {
            values.put(KEY_VALUES[i], trace.values[i]);
        }
        if (type.equals(Trace.ROTATION_MATRIX)) {
            db_.insert(TABLE_ROTATION_MATRIX, null, values);
        } else if (type.equals(Trace.ACCELEROMETER)) {
            db_.insert(TABLE_ACCELEROMETER, null, values);
        } else if (type.equals(Trace.GYROSCOPE)) {
            db_.insert(TABLE_GYROSCOPE, null, values);
        } else if (type.equals(Trace.MAGNETOMETER)) {
            db_.insert(TABLE_MAGNETOMETER, null, values);
        } else if (type.equals(Trace.GPS)) {
            db_.insert(TABLE_GPS, null, values);
        } else {
            assert 0 == 1;
        }
    }



    /**
     * @brief get the gps points of a trip, which is identified by the start time (the name of the database)
     * @param time the start time of a trip (also the name of the database)
     * @return a list of trace, or gps points
     */
    public List<Trace> getGPSPoints(long time) {
        SQLiteDatabase tmpdb = SQLiteDatabase.openDatabase(DB_PATH + String.valueOf(time).concat(".db"), null, SQLiteDatabase.OPEN_READONLY);
        List<Trace> res = new ArrayList<Trace>();
        String selectQuery = "SELECT  * FROM " + TABLE_GPS;
        Cursor cursor = tmpdb.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        do {
            Trace trace = new Trace();
            trace.time = cursor.getLong(0);
            for(int i = 0; i < 3; ++i) {
                trace.values[i] = cursor.getFloat(i + 1);
            }
            res.add(trace);
        } while (cursor.moveToNext());
        return res;
    }

    /**
     * @brief remove the record of the table, so that the user cannot see it
     * but the file is still in the database
     * @param time
     */
    public void removeTrip(long time) {
        ContentValues data = new ContentValues();
        data.put("deleted", 1);
        meta_.update(TABLE_META, data, "tripid=" + time, null);
    }

    /**
     * @ delete it upon removal, only if the trip is deleted and uploaded
     * @param time
     */
    public void deleteTrip(long time) {
        SQLiteDatabase.deleteDatabase(new File(DB_PATH + String.valueOf(time).concat(".db")));
    }

    public List<Trip> showTrips() {
        List<Trip> trips = new ArrayList<Trip>();
        String selectQuery = "SELECT  * FROM " + TABLE_GPS;
        Cursor cursor = meta_.rawQuery(selectQuery, null);
        cursor.moveToFirst();
        do {
            long stime = cursor.getLong(0);
            long etime = cursor.getLong(1);
            double dist = cursor.getDouble(2);
            int deleted = cursor.getInt(3);
            if(deleted == 1) {
                continue;
            }
            Trip trip = new Trip();
            trip.setStartTime(stime);
            trip.setEndTime(etime);
            trip.setDistance(dist);
            trip.setGPSPoints(this.getGPSPoints(stime));
            trips.add(trip);
        } while (cursor.moveToNext());
        return trips;
    }


}
