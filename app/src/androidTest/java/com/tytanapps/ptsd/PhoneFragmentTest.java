package com.tytanapps.ptsd;


import android.support.test.espresso.ViewInteraction;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class PhoneFragmentTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void phoneFragmentTest() {
        ViewInteraction imageButton = onView(
                allOf(withContentDescription("Open navigation drawer"),
                        withParent(withId(R.id.toolbar)),
                        isDisplayed()));
        imageButton.perform(click());

        ViewInteraction appCompatCheckedTextView = onView(
                allOf(withId(R.id.design_menu_item_text), withText("Call"), isDisplayed()));
        appCompatCheckedTextView.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.phone_number_textview), withText("1-800-273-8255"), isDisplayed()));
        textView.perform(scrollTo()).check(matches(withText("1-800-273-8255")));

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.phone_number_textview), withText("1-888-777-4443"), isDisplayed()));
        textView2.perform(scrollTo()).check(matches(withText("1-888-777-4443")));

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.phone_details_textview), withText("We help veterans and their families who are enduring a crisis or who have a critical need for help. Veterans of any era can call for assistance."), isDisplayed()));
        textView3.perform(scrollTo()).check(matches(withText("We help veterans and their families who are enduring a crisis or who have a critical need for help. Veterans of any era can call for assistance.")));

    }
}
