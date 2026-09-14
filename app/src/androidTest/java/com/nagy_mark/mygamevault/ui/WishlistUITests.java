package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.content.Context;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WishlistUITests {
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

        Espresso.onView(ViewMatchers.withId(R.id.wishlistFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testWishlistItemsAreDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Grand Theft Auto VI"))));
    }

    @Test
    public void testSearchFunctionality() {
        Espresso.onView(ViewMatchers.withId(R.id.etSearchWishlist))
                .perform(ViewActions.typeText("Grand Theft Auto VI"), ViewActions.closeSoftKeyboard());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Grand Theft Auto VI"))));

        Espresso.onView(ViewMatchers.withText("PowerWash Simulator 2"))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testSortFunctionality() {
        Context context = ApplicationProvider.getApplicationContext();
        String[] sortOptions = context.getResources().getStringArray(R.array.sort_options);
        String nameDesc = sortOptions[1];

        Espresso.onView(ViewMatchers.withId(R.id.actvSortWishlist))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(nameDesc))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("PowerWash Simulator 2"))));

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollToPosition(2))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Grand Theft Auto VI"))));
    }

    @Test
    public void testItemClickNavigatesToDetail() {
        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.tvGameTitleWishlistDetail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testDeleteDialogAppearsAndCancel() {
        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnDeleteWishlist)
                ));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(R.string.delete_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.cancel))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Grand Theft Auto VI"))));
    }
}
