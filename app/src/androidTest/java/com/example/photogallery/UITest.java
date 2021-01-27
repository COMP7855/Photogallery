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
        onView(withId(R.id.buttonSignIn)).perform(click());
        onView(withId(R.id.imageButtonSearch)).perform(click());
        onView(withId(R.id.editTextStartTime)).perform(clearText(), typeText("2021-01-26 00:00:00"), closeSoftKeyboard());
        onView(withId(R.id.editTextEndTime)).perform(clearText(), typeText("2021-01-27 00:00:00"), closeSoftKeyboard()); //yyyyMMdd_HHmmss - 4:00pm jan 25
        onView(withId(R.id.editTextKeyword)).perform(typeText("capC"), closeSoftKeyboard());
        pauseTestFor(2000);
        onView(withId(R.id.buttonOkay)).perform(click());
        onView(withId(R.id.editTextCaption)).check(matches(withText("capC")));
        pauseTestFor(1000);
        onView(withId(R.id.buttonRight)).perform(click());
        pauseTestFor(1000);
        onView(withId(R.id.buttonLeft)).perform(click());
        pauseTestFor(2000);
    }
    private void pauseTestFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

