package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.view.View;
import android.widget.RatingBar;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class LibraryDetailUITests {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    public static ViewAction setRating(final float rating) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RatingBar.class);
            }

            @Override
            public String getDescription() {
                return "Értékelés beállítása: " + rating;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RatingBar ratingBar = (RatingBar) view;
                ratingBar.setRating(rating);
            }
        };
    }

    public static Matcher<View> withRating(final float expectedRating) {
        return new BoundedMatcher<View, RatingBar>(RatingBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("Az elvárt értékelés: " + expectedRating);
            }

            @Override
            protected boolean matchesSafely(RatingBar item) {
                return item.getRating() == expectedRating;
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
    public void testUpdateGameDetails() {
        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.actvGameStatusLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.click());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(R.string.status_finished))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.rbGameRatingLibraryDetail))
                .perform(ViewActions.scrollTo(), setRating(5.0f));

        Espresso.onView(ViewMatchers.withId(R.id.etGameNoteLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.replaceText("Fantasztikus játék, mindenkinek ajánlom!"));

        Espresso.onView(ViewMatchers.withId(R.id.btnSaveGameLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.pressBack();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, ViewActions.click()));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.actvGameStatusLibraryDetail))
                .perform(ViewActions.scrollTo())
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.status_finished)));

        Espresso.onView(ViewMatchers.withId(R.id.rbGameRatingLibraryDetail))
                .perform(ViewActions.scrollTo())
                .check(ViewAssertions.matches(withRating(5.0f)));

        Espresso.onView(ViewMatchers.withId(R.id.etGameNoteLibraryDetail))
                .perform(ViewActions.scrollTo())
                .check(ViewAssertions.matches(ViewMatchers.withText("Fantasztikus játék, mindenkinek ajánlom!")));

        Espresso.onView(ViewMatchers.withId(R.id.actvGameStatusLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(R.string.status_owned))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.rbGameRatingLibraryDetail))
                .perform(ViewActions.scrollTo(), setRating(0.0f));

        Espresso.onView(ViewMatchers.withId(R.id.etGameNoteLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.replaceText(""));

        Espresso.onView(ViewMatchers.withId(R.id.btnSaveGameLibraryDetail))
                .perform(ViewActions.scrollTo(), ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.pressBack();
    }
}
