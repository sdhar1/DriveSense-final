package wisc.drivesense.rating;

import wisc.drivesense.utility.Trace;
import wisc.drivesense.utility.Trip;

/**
 * Created by lkang on 4/20/16.
 */
public class Rating {
    private Trip trip_;
    private Trace lastTrace_;
    private double lastSpeed_;
    private double score_ = 10.0;

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
        double curSpeed = Trip.distance(lastTrace_, trace);
        if(lastSpeed_ == -1.0) {
            lastSpeed_ = curSpeed;
            return 0;
        }
        double time = trace.time - lastTrace_.time;
        double a = (curSpeed - lastSpeed_)/(time/1000.0);
        if(a < -2.5) {
            score_ *= 0.95;
            trip_.setScore(score_);
            return -1;
        }
        return 0;
    }



}
