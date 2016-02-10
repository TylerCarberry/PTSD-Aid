package com.tytanapps.ptsd;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Before;
import org.junit.runner.RunWith;

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

        // Switch to the PTSD test fragment
        mainActivity.switchFragment(new PTSDTestFragment());
    }

//    /**
//     * Test that answering none for every question results in minimal symptoms
//     */
//    @Test
//    public void testNoSymptoms() {
//        onView(withText(is(mainActivity.getString(R.string.submit_test)))).perform(scrollTo(), click());
//        onView(withText(is(mainActivity.getString(R.string.result_minimal)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.share_results)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.find_professional)))).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Test that answering a lot for every question results in a high chance of PTSD
//     */
//    @Test
//    public void testAllSymptoms() {
//        String[] questions = mainActivity.getResources().getStringArray(R.array.stress_questions);
//
//        for(String question : questions)
//            onView(allOf(hasSibling(withText(question)), withId(R.id.result_seekbar))).perform(scrollTo(), setProgress(4)).check(matches(isDisplayed()));
//
//        onView(withText(is(mainActivity.getString(R.string.submit_test)))).perform(scrollTo(), click());
//        onView(withText(is(mainActivity.getString(R.string.result_high)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.share_results)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.find_professional)))).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Test that having some symptoms results in a medium chance of PTSD
//     */
//    @Test
//    public void testSomeSymptoms() {
//        String[] questions = mainActivity.getResources().getStringArray(R.array.stress_questions);
//
//        onView(allOf(hasSibling(withText(questions[0])), withId(R.id.result_seekbar))).perform(scrollTo(), setProgress(4)).check(matches(isDisplayed()));
//        onView(allOf(hasSibling(withText(questions[1])), withId(R.id.result_seekbar))).perform(scrollTo(), setProgress(4)).check(matches(isDisplayed()));
//
//        onView(withText(is(mainActivity.getString(R.string.submit_test)))).perform(scrollTo(), click());
//        onView(withText(is(mainActivity.getString(R.string.result_medium)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.share_results)))).check(matches(isDisplayed()));
//        onView(withText(is(mainActivity.getString(R.string.find_professional)))).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Test that tapping on find professional switches to the nearby facilities fragment
//     */
//    @Test
//    public void testFindProfessional() {
//        onView(withText(is(mainActivity.getString(R.string.submit_test)))).perform(scrollTo(), click());
//        onView(withText(is(mainActivity.getString(R.string.find_professional)))).perform(click());
//        //onView(withId(R.id.nearby_facilities_container)).check(matches(isDisplayed()));
//    }
//
//    /**
//     * Set the progress of a SeekView
//     * @param progress The amount that the SeekView is scrolled
//     * @return The view action to be used by espresso
//     */
//    public static ViewAction setProgress(final int progress) {
//        return new ViewAction() {
//            @Override
//            public void perform(UiController uiController, View view) {
//                ((SeekBar) view).setProgress(progress);
//            }
//
//            @Override
//            public String getDescription() {
//                return "Set a progress";
//            }
//
//            @Override
//            public Matcher<View> getConstraints() {
//                return ViewMatchers.isAssignableFrom(SeekBar.class);
//            }
//        };
//    }

}
