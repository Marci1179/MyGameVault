package com.nagy_mark.mygamevault.ui;

import android.Manifest;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LibraryAndWishlistUITests {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        try {
            Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                    .perform(ViewActions.typeText("teszt1@teszt.hu"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.etLoginPassword))
                    .perform(ViewActions.typeText("teszt1"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                    .perform(ViewActions.click());

            Thread.sleep(3000);
        } catch (Exception e) {

        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testEmptyLibraryIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.tvEmptyLibrary))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testEmptyWishlistIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.wishlistFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.tvEmptyWishlist))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}


