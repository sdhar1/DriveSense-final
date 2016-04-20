package wisc.drivesense.rating;

import wisc.drivesense.utility.Constants;
import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;

/**
 * Created by lkang on 4/20/16.
 */
public class Rating {
    private Trip trip_;
    private Trace lastTrace_;
    private double lastSpeed_;

    public Rating(Trip trip) {
        this.trip_ = trip;
        lastSpeed_ = -1.0;
        lastTrace_ = null;
    }

    public int readingData(Trace trace) {
        if(trace.type != Trace.GPS) {
            return 0;
        }
        if(lastTrace_ == null) {
            lastTrace_ = trace;
            return 0;
        }
        double curSpeed = distance(lastTrace_, trace);
        if(lastSpeed_ == -1.0) {
            lastSpeed_ = curSpeed;
            return 0;
        }
        double time = trace.time - lastTrace_.time;
        double a = (curSpeed - lastSpeed_)/(time/1000.0);
        if(a < -2.5) {
            
            return -1;
        }
        return 0;
    }

    /**
     *@param gps0 (lat,long ...)
     *@param gps1 (lat, lomg..)
     *@return the distance between two points, in meters
     * based on haversine formula
     */
    public static double distance(Trace gps0, Trace gps1) {

        double lat1 = Math.toRadians(gps0.values[0]);
        double lng1 = Math.toRadians(gps0.values[1]);
        double lat2 = Math.toRadians(gps1.values[0]);
        double lng2 = Math.toRadians(gps1.values[1]);
        double p1 = Math.cos(lat1)*Math.cos(lat2)*Math.cos(lng1-lng2);
        double p2 = Math.sin(lat1)*Math.sin(lat2);

        double res = Math.acos(p1 + p2);
        if(res<Constants.kSmallEPSILON || res!=res) {
            res = 0.0;
        }
        return res * Constants.kEarthRadius;
    }


}
