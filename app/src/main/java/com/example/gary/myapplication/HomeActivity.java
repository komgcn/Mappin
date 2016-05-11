package com.example.gary.myapplication;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private CallbackManager cbManager;
    private AccessToken token;
    public static ArrayList<RoadEvent> roadEvents = new ArrayList<>();
    private LocationResultReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        cbManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_home);
        LoginButton fbButton = (LoginButton) findViewById(R.id.button_fb);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.mipmap.mappin);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        mReceiver = new LocationResultReceiver(new Handler());

        Log.d(getString(R.string.debug_tag), "User enters Home page.");

        if (fbButton != null) {
            fbButton.registerCallback(cbManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Toast.makeText(HomeActivity.this, "Login successfully.", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag), "User login facebook successfully.");
                }

                @Override
                public void onCancel() {
                    Toast.makeText(HomeActivity.this, "Login attempt canceled.", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag), "User cancel fb login.");
                }

                @Override
                public void onError(FacebookException e) {
                    Toast.makeText(HomeActivity.this, "Login attempt failed.", Toast.LENGTH_LONG).show();
                    Log.d(getString(R.string.debug_tag), "User failed to login fb.");
                    e.printStackTrace();
                }
            });
        }
    }

    public void getFBTraffic(View view) {
        Log.d(getString(R.string.debug_tag), "User click on \"retrieve event\" button.");
        token = AccessToken.getCurrentAccessToken();
        if (token != null) {
            Bundle param = new Bundle();
            long one_hour_ago = (System.currentTimeMillis() / 1000) - 3600;
            param.putString("limit", "100");
            param.putString("since", String.valueOf(one_hour_ago));
            param.putString("fields", "message, likes.limit(100).summary(true)");
            new GraphRequest(
                    token,
                    "/" + getString(R.string.mappin_live_traffic_fb_group_id) + "/feed",
                    param,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            JSONObject jObj = response.getJSONObject();
                            if (jObj != null) {
                                Log.e(getString(R.string.debug_tag), jObj.toString());
                                JSONParser parser = new JSONParser();
                                roadEvents = parser.parseRoadEvent(jObj);
                                for (int i = 0; i < roadEvents.size(); i++) {
                                    RoadEvent event = roadEvents.get(i);
                                    if (!event.isLoc()) {
                                        startIntentService(event.getAddress(), i);
                                    }
                                }
                                Toast.makeText(HomeActivity.this, "Events retrieved from Facebook successfully.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(HomeActivity.this, "No internet connection", Toast.LENGTH_LONG).show();
                                Log.d(getString(R.string.debug_tag), "No internet connection.");
                            }
                        }
                    }
            ).executeAsync();
        } else {
            Toast.makeText(this, "Please login to facebook.", Toast.LENGTH_LONG).show();
            Log.d(getString(R.string.debug_tag), "User did not log into FB while retrieve events, failed.");
        }
    }

    protected void startIntentService(String address, int num) {
        Log.d(getString(R.string.debug_tag), "Start intent service to get latlng of address: " + address);
        String query_key = "latlng";
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, address);
        intent.putExtra(FetchAddressIntentService.Constants.POSITION_NO, num);
        intent.putExtra(FetchAddressIntentService.Constants.QUERY_TYPE, query_key);
        startService(intent);
    }

    @SuppressLint("ParcelCreator")
    class LocationResultReceiver extends ResultReceiver {

        public LocationResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            Location location = resultData.getParcelable(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                int num = resultData.getInt(FetchAddressIntentService.Constants.POSITION_NO);
                RoadEvent event = roadEvents.get(num);
                event.setLocation(location);
                Log.d(getString(R.string.debug_tag), "latlng received.");
                Log.d(getString(R.string.debug_tag), event.getAddress() + ". " + event.getLocation().getLatitude() + ", " + event.getLocation().getLongitude());
            }
        }
    }

    public void searchRoute(View view) {
        Log.d(getString(R.string.debug_tag), "User click on \"search route\" button.");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void reportTraffic(View view) {
        Log.d(getString(R.string.debug_tag), "User click on \"report traffic\" button.");
        Intent intent = new Intent(this, ReportActivity.class);
        startActivity(intent);
    }

    public void startLog(View view) {
        File storage = Environment.getExternalStorageDirectory();
        File dir = new File(storage.getAbsolutePath() + "/ALogcat");
        dir.mkdirs();
        File file = new File(dir, "logcat" + System.currentTimeMillis() + ".txt");

        try {
            Process process = Runtime.getRuntime().exec("logcat -c");
            process = Runtime.getRuntime().exec("logcat -f " + file + " " + getString(R.string.debug_tag) + ":D *:S");
            Toast.makeText(HomeActivity.this, "Start Logging", Toast.LENGTH_LONG).show();
            Log.d(getString(R.string.debug_tag), "User clicks on start logging button.");
            Log.d(getString(R.string.debug_tag), "@@@@@@@@@@@@@@@@@@@Start logging@@@@@@@@@@@@@@@@@@@@@@");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showHelp(View view) {
        Log.d(getString(R.string.debug_tag), "User clicks on Help button.");
        DialogFragment frag = new HelpDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HelpDialogFragment.HELP_MSG, getString(R.string.home_help_msg));
        frag.setArguments(bundle);
        frag.show(getFragmentManager(), HelpDialogFragment.HELP_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        cbManager.onActivityResult(requestCode, resultCode, data);
    }

}
