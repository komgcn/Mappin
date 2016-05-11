package com.example.gary.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnMyLocationButtonClickListener, OnMapClickListener, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    public static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int REQUEST_RESOLVE_ERROR = 101;
    private static final String DIALOG_ERROR = "dialog_error";
    private static final String STATE_RESOLVING_ERROR = "resolving_error";
    private static final String LOCATION_KEY = "location_key";
    private static final String LOCATION_ADDRESS_KEY = "location_address_key";
    private static final String query_key = "address";
    private static final long update_interval = 10000;
    private static final long fastest_interval = 10000;
    private static final String from_key = "from";
    private static final String to_key = "to";
    private boolean mResolvingError = false;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation, targetLocation;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;
    private EditText textfield_from, textfield_to;
    private boolean update_flag = false;
    private boolean go_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateValuesFromBundle(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        textfield_from = (EditText) findViewById(R.id.text_from);
        textfield_to = (EditText) findViewById(R.id.text_to);

        Log.d(getString(R.string.debug_tag), "User enters search route page.");

        mResultReceiver = new AddressResultReceiver(new Handler());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                mLastLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }
            if (savedInstanceState.keySet().contains(STATE_RESOLVING_ERROR)) {
                mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR);
            }
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        outState.putParcelable(LOCATION_KEY, mLastLocation);
        outState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(getString(R.string.debug_tag), "Google map ready.");
        mMap = map;
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(update_flag){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            if (resultCode == RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
        }
    }

    /****
     * Google API Client relevant codes
     ****/
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(getString(R.string.debug_tag), "Google api client connected.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMapClickListener(this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                updateMapCamera();
                startLocationUpdates();
                if (!Geocoder.isPresent()) {
                    Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
                } else {
                    startIntentService(mLastLocation, from_key);
                }
            }

            /**LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
             PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
             result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

            @Override public void onResult(LocationSettingsResult result) {
            final Status status = result.getStatus();
            final LocationSettingsStates states = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
            break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
            try {
            status.startResolutionForResult(MainActivity.this,
            REQUEST_CHECK_SETTINGS);
            } catch (IntentSender.SendIntentException e) {
            }
            break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
            break;
            }
            }
            });**/
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Toast.makeText(this, "Google service network lost or disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
        Log.d(getString(R.string.debug_tag), "Google service network lost or disconnected. Please re-connect. toast shown to user. ");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(getString(R.string.debug_tag), "Google api client connection failed.");
        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        } else {
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }

    /***
     * Map motion and gesture events
     ***/
    @Override
    public boolean onMyLocationButtonClick() {
        Log.d(getString(R.string.debug_tag), "User click on my location button.");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateMapCamera();
        }
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
        } else {
            startIntentService(mLastLocation, from_key);
        }
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(getString(R.string.debug_tag), "User click on map:" + latLng.latitude + "," + latLng.longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
        targetLocation = new Location("");
        targetLocation.setLatitude(latLng.latitude);
        targetLocation.setLongitude(latLng.longitude);
        if (!Geocoder.isPresent()) {
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
        } else {
            startIntentService(targetLocation, to_key);
        }
    }

    /***
     * Locatoin relevant codes
     ***/
    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(update_interval);
        mLocationRequest.setFastestInterval(fastest_interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            update_flag = true;
            Log.d(getString(R.string.debug_tag), "Start location updates.");
        }
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        update_flag = false;
        Log.d(getString(R.string.debug_tag), "Stop location updates.");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(getString(R.string.debug_tag), "Location changed Main.");
        mLastLocation = location;
        updateMapCamera();
    }

    private void updateMapCamera() {
        LatLng latlng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latlng, 17);
        mMap.animateCamera(cameraUpdate);
        Log.d(getString(R.string.debug_tag), "Map camera updated Main.");
    }

    /***
     * Location address geocoding relevant codes
     ***/
    protected void startIntentService(Location location, String key) {
        Log.d(getString(R.string.debug_tag),"Starting intent service to get address of "+location.getLatitude()+","+location.getLongitude()+" type of "+key);
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        intent.putExtra(FetchAddressIntentService.Constants.ADDRESS_TYPE, key);
        intent.putExtra(FetchAddressIntentService.Constants.QUERY_TYPE, query_key);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    public class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
                String type = resultData.getString(FetchAddressIntentService.Constants.ADDRESS_TYPE);
                if (type.equals(from_key)) {
                    textfield_from.setText(mAddressOutput);
                    Log.d(getString(R.string.debug_tag),"From textfield set to "+mAddressOutput);
                }
                if (type.equals(to_key)) {
                    textfield_to.setText(mAddressOutput);
                    Log.d(getString(R.string.debug_tag),"To textfield set to "+mAddressOutput);
                }
                Toast.makeText(MainActivity.this, R.string.address_found, Toast.LENGTH_SHORT).show();
                Log.d(getString(R.string.debug_tag),"Address found toast shown to user.");
            }
        }
    }

    /***
     * Direction relevant codes
     ***/
    public void getRoute(View view) {
        Log.d(getString(R.string.debug_tag),"User click GO! button");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        if (textfield_from.getText().toString().isEmpty() || textfield_to.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill in both location text field.", Toast.LENGTH_LONG).show();
            Log.d(getString(R.string.debug_tag),"Please fill in both location text field toast shown to user.");
        } else {
            try {
                List<Address> address_from = geocoder.getFromLocationName(textfield_from.getText().toString(), 1);
                if (address_from == null || address_from.size() == 0) {
                    Toast.makeText(this, "Can't find origin address latitude/longitude.", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag), "Can't find origin address latitude/longitude toast shown to user.");
                } else {
                    Location loc_from = new Location("");
                    loc_from.setLatitude(address_from.get(0).getLatitude());
                    loc_from.setLongitude(address_from.get(0).getLongitude());
                    mLastLocation = loc_from;
                    Log.d(getString(R.string.debug_tag),"Location from set to "+mLastLocation.getLatitude()+","+mLastLocation.getLongitude());
                }
                List<Address> address_to = geocoder.getFromLocationName(textfield_to.getText().toString(), 1);
                if (address_to == null || address_to.size() == 0) {
                    Toast.makeText(this, "Can't find destination address latitude/longitude.", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag), "Can't find destination address latitude/longitude toast shown to user.");
                } else {
                    Location loc_to = new Location("");
                    loc_to.setLatitude(address_to.get(0).getLatitude());
                    loc_to.setLongitude(address_to.get(0).getLongitude());
                    targetLocation = loc_to;
                    Log.d(getString(R.string.debug_tag),"Target location set to "+targetLocation.getLatitude()+","+targetLocation.getLongitude());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mLastLocation == null || targetLocation == null) {
                Toast.makeText(this, "Please fill in both location text field.", Toast.LENGTH_LONG).show();
                Log.d(getString(R.string.debug_tag), "Please fill in both location text field toast shown to user.");
            } else {
                String url = makeUrl(mLastLocation.getLatitude(), mLastLocation.getLongitude(), targetLocation.getLatitude(), targetLocation.getLongitude());
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isConnected()) {
                    new DownloadDirectionTask().execute(url);
                } else {
                    Toast.makeText(this, "No network connection available", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag),"No network connection available toast shown to user.");
                }
            }
        }
    }

    public String makeUrl(double lat, double log, double deslat, double deslog) {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json?origin=");
        urlString.append(Double.toString(lat));
        urlString.append(",");
        urlString.append(Double.toString(log));
        urlString.append("&destination=");
        urlString.append(Double.toString(deslat));
        urlString.append(",");
        urlString.append(Double.toString(deslog));
        urlString.append("&mode=transit&alternatives=true&transit_mode=bus");
        urlString.append("&key=");
        urlString.append(getString(R.string.direction_api_key));
        Log.d(getString(R.string.debug_tag), urlString.toString());
        return urlString.toString();
    }

    private String snapURL(List<LatLng> polyline) {
        StringBuilder params = new StringBuilder();
        for (LatLng latlng : polyline) {
            params.append(latlng.latitude);
            params.append(",");
            params.append(latlng.longitude);
            params.append("|");
        }
        params.setLength(params.length() - 1);
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://roads.googleapis.com/v1/snapToRoads?path=");
        urlString.append(params);
        urlString.append("&interpolate=true&key=");
        urlString.append(getString(R.string.direction_api_key));
        Log.d(getString(R.string.debug_tag), urlString.toString());
        return urlString.toString();
    }

    private String downloadUrl(String myUrl) throws IOException {
        Log.d(getString(R.string.debug_tag),"Starting to downloadUrl");
        String data = "";
        InputStream is;
        HttpsURLConnection conn;
        try {
            URL url = new URL(myUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(getString(R.string.debug_tag), "The response is: " + response);
            is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d(getString(R.string.debug_tag), e.toString());
        }
        return data;
    }

    private class DownloadDirectionTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve direction, URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<Journey>> {

        @Override
        protected List<Journey> doInBackground(String... jsonData) {
            JSONObject jObj;
            List<Journey> routes = new ArrayList<>();
            try {
                jObj = new JSONObject(jsonData[0]);
                Log.d(getString(R.string.debug_tag),jObj.toString());
                JSONParser parser = new JSONParser();
                routes = parser.parseJourneys(jObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<Journey> result) {
            if (result == null) {
                Toast.makeText(MainActivity.this, "No route found.", Toast.LENGTH_LONG).show();
                Log.d(getString(R.string.debug_tag),"No route found toast shown to user.");
            } else {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                JSONParser parser = new JSONParser();
                ArrayList<Journey> routes = (ArrayList<Journey>) result;
                for (int i = 0; i < routes.size(); i++) {
                    Journey route = routes.get(i);
                    ArrayList<Step> steps = route.getSteps();
                    for (int j = 0; j < steps.size(); j++) {
                        Step step = steps.get(j);
                        if (step.getTravelMode() == 1) {
                            String url = snapURL(step.getPolyline());
                            try {
                                String jsonData = downloadUrl(url);
                                JSONObject jObj = new JSONObject(jsonData);
                                List<LatLng> polyline = parser.parseSnappedPoints(jObj);
                                step.setPolyline(polyline);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                intent.putExtra("last_location", mLastLocation);
                intent.putExtra("target_location", targetLocation);
                intent.putParcelableArrayListExtra("routes", routes);
                startActivity(intent);
            }

        }
    }

    public void showHelp(View view){
        Log.d(getString(R.string.debug_tag),"User clicks on Help button.");
        DialogFragment frag = new HelpDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HelpDialogFragment.HELP_MSG,getString(R.string.main_help_msg));
        frag.setArguments(bundle);
        frag.show(getFragmentManager(),HelpDialogFragment.HELP_TAG);
    }

    /****
     * Error dialog codes
     ****/
    private void showErrorDialog(int errorCode) {
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    public void onDialogDismissed() {
        mResolvingError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity) getActivity()).onDialogDismissed();
        }
    }
}
