package com.example.gary.myapplication;

import android.app.Instrumentation;
import android.app.Instrumentation.*;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
/**
 * Created by gary on 11/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class ReportActivityTest {

    public static final String TEST_ADDRESS = "University of Southampton University Road Southampton SO17 1BJ";
    public static final LatLng TEST_LATLNG = new LatLng(50.936484, -1.3952249);

    @Rule
    public ActivityTestRule<ReportActivity> activityTestRule = new ActivityTestRule<>(ReportActivity.class);

    @Test
    public void test_startReportWriteActivity(){
        ActivityMonitor activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(ReportWriteActivity.class.getName(),null,false);

        onView(withId(R.id.button_report_location)).perform(click());

        ReportWriteActivity activity = (ReportWriteActivity) InstrumentationRegistry.getInstrumentation().waitForMonitorWithTimeout(activityMonitor,5000);
        assertNotNull(activity);
    }

    @Test
    public void test_displayAddressNewActivity(){
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.text_location)).perform(replaceText(TEST_ADDRESS), closeSoftKeyboard());
        onView(withId(R.id.button_report_location)).perform(click());
        onView(withId(R.id.textview_location)).check(matches(withText("LatLng: " + TEST_LATLNG.latitude + ", " + TEST_LATLNG.longitude + ". " + TEST_ADDRESS + ". ")));
    }
}
