package com.example.gary.myapplication;

import android.app.Instrumentation.ActivityMonitor;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.action.ViewActions.click;
/**
 * Created by gary on 11/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class HomeStartNewActivityTest {

    @Rule
    public ActivityTestRule<HomeActivity> activityRule = new ActivityTestRule<>(HomeActivity.class);

    @Test
    public void testStartMainActivity(){
        ActivityMonitor activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(MainActivity.class.getName(), null, false);

        onView(withId(R.id.button_search)).perform(click());

        MainActivity activity = (MainActivity) InstrumentationRegistry.getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        assertNotNull(activity);
    }

    @Test
    public void testStartReportActivity(){
        ActivityMonitor activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(ReportActivity.class.getName(), null, false);

        onView(withId(R.id.button_report)).perform(click());

        ReportActivity activity = (ReportActivity) InstrumentationRegistry.getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        assertNotNull(activity);
    }
}
