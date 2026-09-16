package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.view.View;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class FeedUITests {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Kattintás a kártyán belüli " + id + " azonosítójú gombra";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    @Before
    public void setUp() {
        try {
            Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                    .perform(ViewActions.typeText("teszt2@teszt.hu"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.etLoginPassword))
                    .perform(ViewActions.typeText("teszt2"), ViewActions.closeSoftKeyboard());

            Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                    .perform(ViewActions.click());

            Thread.sleep(3000);
        } catch (Exception e) {

        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.feedFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testEmptyFeedIsDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.tvEmptyFeed))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSearchUser() {
        Espresso.onView(ViewMatchers.withId(R.id.btnFindUsersFeed))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.etSearchUsers))
                .perform(ViewActions.replaceText("marci1179"), ViewActions.pressImeActionButton());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("marci1179"))));

        Espresso.onView(ViewMatchers.withText("teszt1"))
                .check(ViewAssertions.doesNotExist());

        Espresso.onView(ViewMatchers.withId(R.id.etSearchUsers))
                .perform(ViewActions.replaceText(""), ViewActions.closeSoftKeyboard());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("teszt1"))));
    }

    @Test
    public void testEmptyFollowing() {
        Espresso.onView(ViewMatchers.withId(R.id.btnFindUsersFeed))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.btnFollowingUsers))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.tvEmptyUsers))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testFollowUser() {
        Espresso.onView(ViewMatchers.withId(R.id.btnFindUsersFeed))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnFollowHeartUser)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.btnFollowingUsers))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnFollowHeartUser)));
    }

    @Test
    public void CheckFeed() {
        Espresso.onView(ViewMatchers.withId(R.id.btnFindUsersFeed))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnFollowHeartUser)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.pressBack();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvFeed))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.btnFindUsersFeed))
                .perform(ViewActions.click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.btnFollowingUsers))
                .perform(ViewActions.click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvUsers))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithId(R.id.btnFollowHeartUser)));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.pressBack();
    }
}
