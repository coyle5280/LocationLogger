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
import android.os.AsyncTask;
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



//    private RequestQueue queue;

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

    String name;
    private int updateLocationTimer;
    private int updateLocationDistance;
    private int locationAccuracySetting;

    private Intent batteryStatus;

    //  Menu
    ActionBar actionBar;
    //  Menu
    private boolean settingsBoolean = false;
    private boolean mapBoolean = false;

    SettingsFragment settings;

    MapView map;

    ArrayList<String> failedHttpCallUrls;

    private ArrayList locationList;

    private WebView mWebView;

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


    /**
     *
     */
    private void setupLocation() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(location.getAccuracy() < locationAccuracySetting) {
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
            continuousDistance += previousLocation.distanceTo(location);
            previousLocation = location;
        }
    }

    private void updateLocationInfo(Location location) {
        textLocationView.append("Lat: " + location.getLatitude() + "  Lon: " + location.getLongitude() + "\n");
        textLocationView.append("\n");
        }

    private void setupItems() {
//        queue = Volley.newRequestQueue(this);

        mWebView = new WebView(this);
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 6.2; Win64; x64; rv:21.0.0) Gecko/20121011 Firefox/21.0.0");
        final Activity activity = this;
        mWebView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//              Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
                failedHttpCallUrls.add(failingUrl);
            }
        });

        failedHttpCallUrls = new ArrayList<>();
        //location Listener
        location = new Location("distance");

        //Connect XML UI Items
        textLocationView = (TextView) findViewById(R.id.textLocation);
        textLocationView.setMovementMethod(new ScrollingMovementMethod());

        battTextView = (TextView) findViewById(R.id.battLevel);
        distanceTextview = (TextView) findViewById(R.id.distanceText);
        final Button stop = (Button) findViewById(R.id.stopButton);
        final Button start = (Button) findViewById(R.id.startButton);

        //data for database
        observation = "location2";
        name = "josh";

        //Location update settings
        updateLocationDistance = 0;
        updateLocationTimer = 0;
        locationAccuracySetting = 10;




        // Menu
        settings = new SettingsFragment();
        map = new MapView();
        //Menu


        distanceArray = new float[5];




        //Batt Level listener

        //Array List to Hold location objects
        locationList = new ArrayList();



        start.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick Listener for Start Button
             * @param v
             */
            @Override
            public void onClick(View v) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateLocationTimer, updateLocationDistance, locationListener);
                //Clear Out Previous Info
                textLocationView.setText("");
                distanceTextview.setText("");
                battTextView.setText("");
                battTextView.setText("Start Battery Level: " + Float.toString(getBatteryLevel()) + "\n");
                stop.setVisibility(View.VISIBLE);
                start.setVisibility(View.INVISIBLE);


            }
        });


        stop.setOnClickListener(new View.OnClickListener() {
            /**
             * onClick Listener for Stop Button
             * @param v
             */
            @Override
            public void onClick(View v) {
                calculateStartEnd();
                start.setVisibility(View.VISIBLE);
                stop.setVisibility(View.INVISIBLE);
                locationManager.removeUpdates(locationListener);
                tryFailedUrls();
                displayResults();
            }
        });
    }

    /**
     *
     */
    private void tryFailedUrls() {
        String url = "";
        int count = 0;
        if(failedHttpCallUrls.size() != 0) {
            do {
                url = failedHttpCallUrls.get(0);
                failedHttpCallUrls.remove(0);
                mWebView.loadUrl(url);
                count++;
            } while (failedHttpCallUrls.size() != 0 && count < 75);
        }
    }

    //Menu

    /**
     *
     * @param menu
     * @return
     */
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
            case R.id.map:
                switchToMapView();
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }

    }


    /**
     *
     */
    private void switchToMapView() {
        if(!mapBoolean) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.root, map);
            fragmentTransaction.commit();
            mapBoolean = true;
        }else{
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(map);
            fragmentTransaction.commit();
            mapBoolean = false;
        }
    }

    /**
     *
     * @return
     */
    private String getCurrentTime(){
        String isoTime;
        TimeZone tz = TimeZone.getTimeZone("America/Denver");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        isoTime = df.format(new Date());
        return isoTime;
    }

    //Change Settings Methods
    private void updateSettings(SettingsObject newSettings){
        observation = newSettings.getObservation();
        name = newSettings.getName();
        updateLocationTimer = newSettings.getTimerFrequencyVariable();
        updateLocationDistance = newSettings.getDistanceUpdateVariable();

    }

    //End Change Settings Methods

    /**
     *
     * @param location
     */
    private void sendLocationInfoGet(Location location) {


        // Enable javascript
//        mWebView.getSettings().setJavaScriptEnabled(true);
        // Impersonate Mozzila browser


        //Lon then Lat
        String url = "http://coyle5280.cartodb.com/api/v2/sql?q=INSERT INTO " +
                "mobile_data(the_geom, observation, timestamp, name, battery_level)" +
                " VALUES (ST_GeomFromText('POINT(" + location.getLongitude() + " " +
                location.getLatitude() + ")', 4326),' " + observation + " ','" +
                getCurrentTime() + "','" + name + "','" + getBatteryLevel() + "')" +
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

    /**
     *
     * @return
     */
    private float getBatteryLevel (){
        float batteryLevel;
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = this.registerReceiver(null, intentFilter);
        int batteryStartLevelInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
//        Log.i("batStartLevelInt", Integer.toString(batteryStartLevelInt));
        int batteryStartScaleInt = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
//        Log.i("batStartScaleInt", Integer.toString(batteryStartScaleInt));
        batteryLevel = batteryStartLevelInt/(float) batteryStartScaleInt;
        return  batteryLevel;
    }

    /**
     *
     */
    private void calculateStartEnd() {
        if (locationList.size() > 1) {
            int lastEntryIndex = (locationList.size() - 1);
            Location start_Location = (Location) locationList.get(0);
            Location stop_Location = (Location) locationList.get(lastEntryIndex);
            try {
                location.distanceBetween(start_Location.getLatitude(),start_Location.getLongitude(),
                        stop_Location.getLatitude(), stop_Location.getLongitude(), distanceArray);
            } catch (IllegalArgumentException e) {
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


        }else{
            distanceTextview.setText("Error You Do Not Have Enough Locations");
        }
    }

    /**
     *
     */
    private void callSettings() {
        if(!settingsBoolean) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.root, settings);
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

    /**
     *
     */
    private void displayResults(){

        //Battery Level At End
        battTextView.append("End Battery Level: " + Float.toString(getBatteryLevel()));


        //Display Straight Line Distance
        if (distanceArray[0] > 1 && continuousDistance != -1.0f) {
            distanceTextview.setText("Start/End Distance: " +
                    Float.toString(distanceArray[0]) + "\n");
            distanceTextview.append("Continuous Distance: " + Float.toString(continuousDistance));
        } else {
            distanceTextview.setText("Error You Did Not Travel Any Distance");
        }
    }



}
