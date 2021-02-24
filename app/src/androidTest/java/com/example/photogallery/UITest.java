package com.example.photogallery;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class UITest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testUI() {
        // open app
        onView(withId(R.id.buttonSignIn)).perform(click());

        // press search button
        onView(withId(R.id.imageButtonSearch)).perform(click());

        // enter time window
        onView(withId(R.id.editTextStartTime)).perform(clearText(), typeText("2021-01-26 00:00:00"), closeSoftKeyboard());
        onView(withId(R.id.editTextEndTime)).perform(clearText(), typeText("2021-12-27 00:00:00"), closeSoftKeyboard());

        // enter keyword
        onView(withId(R.id.editTextKeyword)).perform(typeText("cat"), closeSoftKeyboard());
        pauseTestFor(2000);

        // press okay
        onView(withId(R.id.buttonOkay)).perform(click());

        // check that the text matches
        onView(withId(R.id.editTextCaption)).check(matches(withText("cat")));
        pauseTestFor(1000);

        // press right button
        onView(withId(R.id.buttonRight)).perform(click());
        pauseTestFor(1000);

        // press left button
        onView(withId(R.id.buttonLeft)).perform(click());
        pauseTestFor(2000);
    }

    @Test
    public void testLocSearch() {
        // open app
        onView(withId(R.id.buttonSignIn)).perform(click());

        // press search button
        onView(withId(R.id.imageButtonSearch)).perform(click());

        // enter location
        onView(withId(R.id.etLatMin)).perform(clearText(), typeText("0"), closeSoftKeyboard());
        onView(withId(R.id.etLatMax)).perform(clearText(), typeText("40"), closeSoftKeyboard());
        onView(withId(R.id.etLongMin)).perform(clearText(), typeText("-999"), closeSoftKeyboard());
        onView(withId(R.id.etLongMax)).perform(clearText(), typeText("999"), closeSoftKeyboard());

        pauseTestFor(1000);

        // press okay
        onView(withId(R.id.buttonOkay)).perform(click());

        pauseTestFor(1000);

        // check that the location matches

    }

    private void pauseTestFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

