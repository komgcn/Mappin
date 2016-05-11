package com.example.gary.myapplication;

import android.app.DialogFragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;

import java.util.Arrays;

public class ReportWriteActivity extends AppCompatActivity {

    private EditText message;
    private TextView location_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_write);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        message = (EditText) findViewById(R.id.text_msg);
        location_msg = (TextView) findViewById(R.id.textview_location);
        Intent intent = getIntent();
        Location location = intent.getParcelableExtra("location");
        String address = intent.getStringExtra("address");
        Log.d(getString(R.string.debug_tag), "User enters report write page.");
        if (address != null) {
            String displayMsg = "LatLng: " + location.getLatitude() + ", " + location.getLongitude() + ". " + address + ". ";
            location_msg.setText(displayMsg);
        } else {
            String displayMsg = "LatLng: " + location.getLatitude() + ", " + location.getLongitude() + ". ";
            location_msg.setText(displayMsg);
        }
        /*LoginManager.getInstance().logInWithPublishPermissions(
                this,
                Arrays.asList("publish_actions"));*/
    }

    public void clearText(View view) {
        message.setText("");
        Log.d(getString(R.string.debug_tag), "User clicks clear button, text clear to empty.");
    }

    public void submitText(View view) {
        Log.d(getString(R.string.debug_tag), "User clicks submit button.");

        String msg = "Report: " + location_msg.getText() + message.getText().toString();
        Bundle params = new Bundle();
        params.putString("message", msg);
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + getString(R.string.mappin_live_traffic_fb_group_id) + "/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Log.d(getString(R.string.debug_tag), response.toString());
                        Toast.makeText(ReportWriteActivity.this, "Post success", Toast.LENGTH_LONG).show();
                        Log.d(getString(R.string.debug_tag), "Post success.");
                    }
                }
        ).executeAsync();

    }

    public void showHelp(View view) {
        Log.d(getString(R.string.debug_tag), "User clicks on Help button.");
        DialogFragment frag = new HelpDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(HelpDialogFragment.HELP_MSG, getString(R.string.report_write_help_msg));
        frag.setArguments(bundle);
        frag.show(getFragmentManager(), HelpDialogFragment.HELP_TAG);
    }
}
