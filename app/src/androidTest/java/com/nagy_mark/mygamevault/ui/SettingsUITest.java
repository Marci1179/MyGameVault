package com.nagy_mark.mygamevault.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.widget.AutoCompleteTextView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

@RunWith(AndroidJUnit4.class)
public class SettingsUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        Intents.init();

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
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.settingsFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testDeleteAccountDialogAppearsAndCancelButton() {
        Espresso.onView(ViewMatchers.withId(R.id.btnDeleteAccountSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.delete_account_confirmation))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.cancel))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.delete_account_confirmation))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testPasswordChangeDialogAppearsAndCancelButton() {
        Espresso.onView(ViewMatchers.withId(R.id.btnChangePasswordSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etNewPasswordChangePassword))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.cancel))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etConfirmNewPasswordChangePassword))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testPasswordChangeDialog_ValidationErrors() {
        Espresso.onView(ViewMatchers.withId(R.id.btnChangePasswordSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.save))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_password_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_confirm_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.etNewPasswordChangePassword))
                .perform(ViewActions.replaceText("123"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etConfirmNewPasswordChangePassword))
                .perform(ViewActions.replaceText("1234"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withText(R.string.save))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_weak_password))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_passwords_mismatch))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testStatisticsNavigation() {
        Espresso.onView(ViewMatchers.withId(R.id.btnStatisticsSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.statistics))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testLogoutDialogAppearsAndCancelButton() {
        Espresso.onView(ViewMatchers.withId(R.id.btnLogoutSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.logout_message))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.cancel))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.logout_message))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testLogout() {
        Espresso.onView(ViewMatchers.withId(R.id.btnLogoutSettings))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.yes))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testDarkModeToggle() {
        final int[] initialMode = new int[1];

        Espresso.onView(ViewMatchers.withId(R.id.swDarkModeSettings)).check((view, noViewFoundException) -> {
            initialMode[0] = view.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        });

        Espresso.onView(ViewMatchers.withId(R.id.swDarkModeSettings))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.swDarkModeSettings)).check((view, noViewFoundException) -> {
            int newMode = view.getContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

            Assert.assertNotEquals("A témaváltás nem történt meg a rendszerben!", initialMode[0], newMode);
        });

        Espresso.onView(ViewMatchers.withId(R.id.swDarkModeSettings))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testLanguageSelection() {
        final String[] initialLanguage = new String[1];
        final String[] targetLanguage = new String[1];
        final String[] targetLocaleCode = new String[1];

        Espresso.onView(ViewMatchers.withId(R.id.actvLanguageSettings)).check((view, noViewFoundException) -> {
            AutoCompleteTextView actv = (AutoCompleteTextView) view;
            initialLanguage[0] = actv.getText().toString();

            if ("English".equals(initialLanguage[0])) {
                targetLanguage[0] = "Magyar";
                targetLocaleCode[0] = "hu";
            } else {
                targetLanguage[0] = "English";
                targetLocaleCode[0] = "en";
            }
        });

        Espresso.onView(ViewMatchers.withId(R.id.actvLanguageSettings))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(targetLanguage[0]))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.actvLanguageSettings))
                .check(ViewAssertions.matches(ViewMatchers.withText(targetLanguage[0])));

        Espresso.onView(ViewMatchers.withId(R.id.actvLanguageSettings)).check((view, noViewFoundException) -> {
            Locale currentLocale = view.getContext().getResources().getConfiguration().getLocales().get(0);
            Assert.assertEquals("A rendszer nyelve nem váltott át a vártra!", targetLocaleCode[0], currentLocale.getLanguage());
        });

        Espresso.onView(ViewMatchers.withId(R.id.actvLanguageSettings))
                .perform(ViewActions.click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withText(initialLanguage[0]))
                .inRoot(RootMatchers.isPlatformPopup())
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}
    }

    @Test
    public void testProfilePicturePickerIntent() {
        Intent resultData = new Intent();
        resultData.setData(Uri.parse("content://dummy/image/path"));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        Intents.intending(Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT),
                IntentMatchers.hasType("image/*")
        )).respondWith(result);

        Espresso.onView(ViewMatchers.withId(R.id.ivProfilePictureSettings))
                .perform(ViewActions.click());

        Intents.intended(Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_GET_CONTENT),
                IntentMatchers.hasType("image/*")
        ));
    }

    @Test
    public void testUsernameUpdate() {
        String testUsername = "ui_teszt_" + System.currentTimeMillis();

        Espresso.onView(ViewMatchers.withId(R.id.etUsernameSettings))
                .perform(ViewActions.replaceText(testUsername));

        Espresso.onView(ViewMatchers.withId(R.id.btnSaveProfileSettings))
                .perform(ViewActions.click());

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.searchFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.settingsFragment))
                .perform(ViewActions.click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {}

        Espresso.onView(ViewMatchers.withId(R.id.etUsernameSettings))
                .check(ViewAssertions.matches(ViewMatchers.withText(testUsername)));
    }
}
