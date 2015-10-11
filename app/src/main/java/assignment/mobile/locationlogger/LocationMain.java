package assignment.mobile.locationlogger;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request.*;
import com.*;



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


/**
 * Created by godzilla on 9/30/2015.
 */
public class LocationMain extends Activity {

    private boolean gpsAvailable;

    private double start_logitude;
    private double start_latitude;

    private double stop_logitude;
    private double stop_latitude;

    private WebView mWebView ;

    private int tracker;

    private RequestQueue queue;

    private String observation;

    //TextView Objects
    private TextView textLocationView;
    private TextView battTextView;
    private TextView distanceTextview;
    //Location Objects
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;

    private float[] distanceArray;


    private Intent batteryStatus;

    //  Menu
    ActionBar actionBar;
    //  Menu
    private boolean settingsBoolean = false;

    SettingsFragment settings;



    private ArrayList locationList;

    private float continuousDistance = -1.0f;
    private Location previousLocation;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        actionBar = getActionBar();

        setContentView(R.layout.main);
        setupItems();
        setupLocation();
    }



    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.getAccuracy() < 10) {
                    //add location to location Array list
                    locationList.add(location);
                    //call method to calculate continuous distance
                    calculateContinuous(location);
                    //calculate straight line distance from start to stop
                    updateLocationInfo(location);

                    sendLocationInfoGet(location);
                }
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

    private void calculateContinuous(Location location) {
        if(continuousDistance == -1.0f){
            previousLocation = location;
            continuousDistance = 0.0f;
        }else{
            continuousDistance = previousLocation.distanceTo(location);
        }
    }

    private void updateLocationInfo(Location location) {
        double longitude,latitude;
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
                    calculate();
                    break;
        }

    }

    private void setupItems() {
        IntentFilter intentFilter;
        queue = Volley.newRequestQueue(this);

        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        location = new Location("distance");

        textLocationView = (TextView) findViewById(R.id.textLocation);
        battTextView = (TextView) findViewById(R.id.battLevel);
        distanceTextview = (TextView) findViewById(R.id.distanceText);

        observation = "location2";

        tracker = 1;
        // Menu
        settings = new SettingsFragment();
        //Menu
        distanceArray = new float[5];

        textLocationView.setMovementMethod(new ScrollingMovementMethod());

        batteryStatus = this.registerReceiver(null, intentFilter);

        locationList = new ArrayList();
        final Button stop = (Button) findViewById(R.id.stopButton);
        final Button start = (Button) findViewById(R.id.startButton);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
                textLocationView.setText("");
                battTextView.setText("Start Batt Level: " + Float.toString(getBatteryLevel()) + "/n");
                stop.setVisibility(View.VISIBLE);
                start.setVisibility(View.INVISIBLE);


            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tracker = 3;
                battTextView.append("End Batt Level: " + Float.toString(getBatteryLevel()));
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
            }
        });
    }
    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //Menu

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.settings:
                callSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }

    }

    private String getCurrentTime(){
        String isoTime;
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        isoTime = df.format(new Date());
        return isoTime;
    }

    private void setObservation(String setObservation){
        observation = setObservation;
    }
        //Lon then Lat


    private void sendLocationInfoGet(Location location) {

        mWebView  = new WebView(this);
        // Enable javascript
        mWebView.getSettings().setJavaScriptEnabled(true);
        // Impersonate Mozzila browser
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");
        final Activity activity = this;

        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
            }
        });


        String url = "http://coyle5280.cartodb.com/api/v2/sql?q=INSERT INTO " +
                "mobile_data(the_geom, observation, timestamp)" +
                " VALUES (ST_GeomFromText('POINT(" + location.getLongitude() + " " + location.getLatitude() + ")', 4326),' " + observation + " ','" + getCurrentTime() + "')" +
                "&api_key=baba08a4c371dc7ed93027f141d0f425c4606c45";



        mWebView.loadUrl(url);

        Log.i("another Try", url);
//        StringRequest stringRequest = new StringRequest(url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Log response
//                        Log.i("response Success", response);
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Log.e("response get", error.getMessage(), error);
//            }
//        });
//        {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//                params.put("Accept-Encoding", "gzip, deflate, sdch");
//                params.put("Accept-Language", "en-US,en;q=0.8");
//                params.put("Cache-Control", "max-age=0");
//                params.put("Connection", "keep-alive");
//                params.put("Accept-Encoding", "gzip, deflate, sdch");
//                params.put("Host", "coyle5280.cartodb.com");
//                params.put("Remote Address", "coyle5280.cartodb.com");
//                return params;
//            }
//        };
//        queue.add(stringRequest);






    }

    private float getBatteryLevel (){
        float batteryLevel;
        int batteryStartLevelInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        Log.i("batStartLevelInt", Integer.toString(batteryStartLevelInt));
        int batteryStartScaleInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        Log.i("batStartScaleInt", Integer.toString(batteryStartScaleInt));
        batteryLevel = batteryStartLevelInt/(float) batteryStartScaleInt;
        return  batteryLevel;
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

        if(distanceArray[0] > 1) {
            distanceTextview.setText(Float.toString(distanceArray[0]));
        }else
            distanceTextview.setText("Error You Did Not Travel Any Distance");
    }

    private void callSettings() {
        if(!settingsBoolean) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.root, settings);
            fragmentTransaction.commit();
            settingsBoolean = true;
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(settings);
            fragmentTransaction.commit();
            settingsBoolean = false;
        }
    }


}
