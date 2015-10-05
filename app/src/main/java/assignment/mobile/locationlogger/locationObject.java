package assignment.mobile.locationlogger;

import java.util.Date;

/**
 * Created by coyle on 10/3/2015.
 */
public class LocationObject {

    final double longitude, latitude;
    final Date timeStamp;

    public LocationObject(){
        longitude = 0;
        latitude = 0;
        timeStamp = new Date();
    }

    public LocationObject(double lon, double lat){
        longitude = lon;
        latitude = lat;
        timeStamp = new Date();
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public Date getTimeStamp(){
        return timeStamp;
    }

}
