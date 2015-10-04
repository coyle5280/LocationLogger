package assignment.mobile.locationlogger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by godzilla on 9/30/2015.
 */
public class LocationMain extends Activity {

    Button start;
    Button stop;

    double start_logitude;
    double start_latitude;

    double stop_logitude;
    double stop_latitude;

    int tracker;

    double longitude,latitude;

    TextView textLocationView;

    TextView distanceTextview;

    LocationManager locationManager;
    LocationListener locationListener;

    Location location;

    float[] distanceArray;

    IntentFilter intentFilter;
    Intent batteryStatus;


    float batteryStartLevel;


    int batteryEndLevel;
    int batteryEndScale;



    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setupItems();
        setupLocation();
    }

    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
    }

    private void updateLocationInfo(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        switch(tracker){
            case 1: start_logitude = longitude;
                    start_latitude = latitude;
                    textLocationView.append("Lat: " + latitude + "  Lon: " + longitude + "\n");
                    textLocationView.append("\n");
                    tracker = 2;
                    break;
            case 2: textLocationView.append("Lat: " + latitude + "  Lon: " + longitude + "\n");
                    textLocationView.append("\n");
                    break;
            case 3: stop_logitude = longitude;
                    stop_latitude = latitude;
                    locationManager.removeUpdates(locationListener);
                    //calculate();
                    break;
        }

    }

    private void setupItems() {


        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        location = new Location("distance");

        textLocationView = (TextView) findViewById(R.id.textLocation);

        distanceTextview = (TextView) findViewById(R.id.distanceText);

        tracker = 1;

        distanceArray = new float[5];

        textLocationView.setMovementMethod(new ScrollingMovementMethod());

        batteryStatus = this.registerReceiver(null, intentFilter);
        int batteryStartLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int batteryStartScale = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        batteryStartLevel = batteryStartLevel/batteryStartScale;

        start = (Button) findViewById(R.id.startButton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000, 0, locationListener);
            }
        });

        stop = (Button) findViewById(R.id.stopButton);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker = 3;
            }
        });


    }

    private void calculate(){
        try {
            location.distanceBetween(start_latitude, start_logitude, stop_latitude, stop_logitude, distanceArray);
        }catch(IllegalArgumentException	e){
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Error " + e.toString());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        int distance = 0;

        distance = Math.round(distanceArray[0]);
        if(distance > 1) {
            distanceTextview.setText(distance);
        }else
            distanceTextview.setText("Error You Did Not Travel Any Distance");
    }
}
