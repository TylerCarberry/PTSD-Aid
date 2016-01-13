package com.tytanapps.ptsd;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

/**
 * Test the main fragment containing the emotions and recommendations
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PTSDFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    MainActivity mainActivity;


    public PTSDFragmentTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mainActivity = getActivity();

        mainActivity.switchFragment(new PTSDTestFragment());
    }

    /**
     * Test that answering none for every question results in minimal symptoms
     */
    @Test
    public void testNoSymptoms() {
        onView(withText(is(mainActivity.getString(R.string.submit_test)))).perform(scrollTo(), click());
        onView(withText(is(mainActivity.getString(R.string.result_minimal)))).check(matches(isDisplayed()));
        onView(withText(is(mainActivity.getString(R.string.share_results)))).check(matches(isDisplayed()));
        onView(withText(is(mainActivity.getString(R.string.find_professional)))).check(matches(isDisplayed()));
    }

}
