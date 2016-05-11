package com.example.gary.myapplication;

import android.app.Instrumentation;
import android.app.Instrumentation.*;
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
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
/**
 * Created by gary on 11/04/2016.
 */
@RunWith(AndroidJUnit4.class)
public class MainStartNewActivityTest {

    public static final String TEST_ADDRESS_TO = "University of Southampton";

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testStartMapActivity(){
        ActivityMonitor activityMonitor = InstrumentationRegistry.getInstrumentation().addMonitor(MapActivity.class.getName(),null,false);

        onView(withId(R.id.text_to)).perform(typeText(TEST_ADDRESS_TO),closeSoftKeyboard());
        onView(withId(R.id.button_go)).perform(click());

        MapActivity activity = (MapActivity) InstrumentationRegistry.getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        assertNotNull(activity);
    }
}
