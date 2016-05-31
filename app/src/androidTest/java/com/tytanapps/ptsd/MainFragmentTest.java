package com.tytanapps.ptsd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Test the main fragment containing the emotions and recommendations
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainFragmentTest extends ActivityInstrumentationTestCase2<MainActivity> {

    /**
     * The activity that will be tested
     */
    MainActivity mainActivity;

    public MainFragmentTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mainActivity = getActivity();
    }

    /**
     * This test will always pass. If it does not, there is a problem with Espresso
     */
    @Test
    public void testAlwaysPasses() {
        assertTrue(true);
    }

    /**
     * Verify that all of the emotions are visible when you start the app
     */
    @Test
    public void testAllEmotionsVisible() {
        onView(withId(R.id.happy_face)).check(matches(isDisplayed()));
        onView(withId(R.id.sad_face)).check(matches(isDisplayed()));
        onView(withId(R.id.ok_face)).check(matches(isDisplayed()));
    }

    /**
     * Tapping on the happy face should hide the other two faces and keep the happy face visible
     */
    @Test
    public void testTapHappy() {
        onView(withId(R.id.happy_face)).perform(click()).check(matches(isDisplayed()));
        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.sad_face).getVisibility());
        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.ok_face).getVisibility());
    }

    /**
     * Tapping on the ok face should hide the other two faces and keep the ok face visible
     */
    @Test
    public void testTapOk() {
        onView(withId(R.id.ok_face)).perform(click()).check(matches(isDisplayed()));

        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.sad_face).getVisibility());
        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.happy_face).getVisibility());
    }

    /**
     * Tapping on the sad face should hide the other two faces and keep the sad face visible
     */
    @Test
    public void testTapSad() {
        onView(withId(R.id.sad_face)).perform(click()).check(matches(isDisplayed()));

        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.happy_face).getVisibility());
        assertNotSame(View.VISIBLE, mainActivity.findViewById(R.id.ok_face).getVisibility());
    }

    /**
     * Tapping on the trusted contact button should prompt the creation of a contact if none exists
     */
    @Test
    public void testCreateTrustedContact() {
        removeSharedPreference(mainActivity.getString(R.string.pref_trusted_name_key));
        removeSharedPreference(mainActivity.getString(R.string.pref_trusted_phone_key));

        onView(withId(R.id.fab)).perform(click());
        onView(withText("Add Trusted Contact")).check(matches(isDisplayed()));
    }

    /**
     * Long pressing on the trusted contact button should prompt the
     * creation of a trusted contact if none exists
     */
    @Test
    public void testLongPressTrustedContact() {
        removeSharedPreference(mainActivity.getString(R.string.pref_trusted_name_key));
        removeSharedPreference(mainActivity.getString(R.string.pref_trusted_phone_key));

        onView(withId(R.id.fab)).perform(longClick());
        onView(withText("Add Trusted Contact")).check(matches(isDisplayed()));
    }

    /**
     * Long pressing on the trusted contact button should prompt a change in the contact
     * if a trusted contact already exists
     */
    @Test
    public void testChangeTrustedContact() {
        putStringSharedPreference(mainActivity.getString(R.string.pref_trusted_name_key), "Tyler");
        putStringSharedPreference(mainActivity.getString(R.string.pref_trusted_phone_key), "234-555-7890");

        onView(withId(R.id.fab)).perform(longClick());
        onView(withText("Change Trusted Contact")).check(matches(isDisplayed()));
    }
    
    /**
     * Test whether any version of Google Play Services is available on the device
     * The app needs at least version 8.3 but this test will pass even if an outdated version is present
     */
    @Test
    public void testGooglePlayServicesInstalled() {
        // Query for the status of Google Play services on the device
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mainActivity);
        assertTrue(statusCode == ConnectionResult.SUCCESS);
        assertTrue(GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE >= 0);
    }

    /**
     * Remove a shared preference
     * @param prefKey The preference to remove
     */
    private void removeSharedPreference(String prefKey) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(prefKey);
        editor.apply();
    }

    /**
     * Put a string into a shared preference
     * @param prefKey The key of the preference
     * @param value The value to save
     */
    private void putStringSharedPreference(String prefKey, String value) {
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

}
