package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;
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
import com.nagy_mark.mygamevault.network.SupabaseApi;
import com.nagy_mark.mygamevault.network.SupabaseApiClient;
import com.nagy_mark.mygamevault.utils.SessionManager;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import retrofit2.Response;

public class SearchUITests {
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
        } catch (Exception e) {}

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.searchFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
    }

    @After
    public void tearDown() {
        Context context = ApplicationProvider.getApplicationContext();
        SupabaseApi api = SupabaseApiClient.getClient(context).create(SupabaseApi.class);
        SessionManager sessionManager = new SessionManager(context);

        String userId = sessionManager.getUserId();

        if (userId != null) {
            try {
                Response<Void> response = api.deleteGameByName("eq.Red Dead Redemption 2", "eq." + userId).execute();

                if (!response.isSuccessful()) {
                    Log.e("TEARDOWN_ERROR", "Nem sikerült törölni a játékot. Kód: " + response.code());
                } else {
                    Log.d("TEARDOWN_SUCCESS", "A Red Dead Redemption 2 sikeresen törölve a teszt után!");
                }
            } catch (IOException e) {
               Log.e("TEARDOWN_EXCEPTION", "Hálózati hiba a törlés során: " + e.getMessage());
            }
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testTopGamesAreLoaded() {
        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("The Witcher 3: Wild Hunt - Game of the Year Edition"))));
    }

    @Test
    public void testSearchAndSearchClear() {
        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
                .perform(ViewActions.typeText("Red Dead Redemption 2"), ViewActions.pressImeActionButton());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Red Dead Redemption 2"))));

        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
                .perform(ViewActions.clearText());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("The Witcher 3: Wild Hunt - Game of the Year Edition"))));
    }

    @Test
    public void testAddGameToLibrary() {
        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
                .perform(ViewActions.typeText("Red Dead Redemption 2"), ViewActions.pressImeActionButton());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnAddLibrarySearch)
                ));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.libraryFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollTo(
                        ViewMatchers.hasDescendant(ViewMatchers.withText("Red Dead Redemption 2"))
                ))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasDescendant(ViewMatchers.withText("Red Dead Redemption 2"))
                ));
    }

    @Test
    public void testAddGameToWishlist() {
        Espresso.onView(ViewMatchers.withId(R.id.etSearch))
                .perform(ViewActions.typeText("Red Dead Redemption 2"), ViewActions.pressImeActionButton());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvSearch))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnAddWishlistSearch)
                ));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.wishlistFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvWishlist))
                .perform(RecyclerViewActions.scrollTo(
                        ViewMatchers.hasDescendant(ViewMatchers.withText("Red Dead Redemption 2"))
                ))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasDescendant(ViewMatchers.withText("Red Dead Redemption 2"))
                ));
    }
}
