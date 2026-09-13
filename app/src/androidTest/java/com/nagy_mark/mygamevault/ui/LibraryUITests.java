package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
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
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.adapters.LibraryAdapter;
import com.nagy_mark.mygamevault.models.SavedGameModel;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LibraryUITests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private List<SavedGameModel> testGames;

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
    }

    @Test
    public void testLibraryItemsAreDisplayed() {
        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("A Way Out"))));
    }

    @Test
    public void testFavoriteFilterFunctionality() {
        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnFavoriteLibrary)
                ));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.swFavoritesFilterLibrary))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("A Way Out"))));

        Espresso.onView(ViewMatchers.withText("Far Cry 3"))
                .check(ViewAssertions.doesNotExist());

        Espresso.onView(ViewMatchers.withId(R.id.swFavoritesFilterLibrary))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnFavoriteLibrary)
                ));

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testSearchFunctionality() {
        Espresso.onView(ViewMatchers.withId(R.id.etSearchLibrary))
                .perform(ViewActions.typeText("Far Cry 3"), ViewActions.closeSoftKeyboard());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("Far Cry 3"))));

        Espresso.onView(ViewMatchers.withText("A Way Out"))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testSortFunctionality() {
        Context context = ApplicationProvider.getApplicationContext();
        String[] sortOptions = context.getResources().getStringArray(R.array.sort_options);
        String nameDesc = sortOptions[1];

        Espresso.onView(ViewMatchers.withId(R.id.actvSortLibrary))
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

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("It Takes Two"))));

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(2))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("A Way Out"))));
    }

    @Test
    public void testItemClickNavigatesToDetail() {
        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.tvGameTitleLibraryDetail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testDeleteDialogAppearsAndCancel() {
        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(
                        0,
                        clickChildViewWithId(R.id.btnDeleteLibrary)
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

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.scrollToPosition(0))
                .check(ViewAssertions.matches(ViewMatchers.hasDescendant(ViewMatchers.withText("A Way Out"))));
    }
}
