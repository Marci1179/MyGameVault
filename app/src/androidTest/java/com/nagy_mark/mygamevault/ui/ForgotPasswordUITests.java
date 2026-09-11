package com.nagy_mark.mygamevault.ui;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ForgotPasswordUITests {
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        ActivityScenario.launch(MainActivity.class);
    }

    @Test
    public void testForgotPasswordDialogAppears() {
        Espresso.onView(ViewMatchers.withId(R.id.tvForgotPassword))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etForgotEmailDialogForgotPassword))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testForgotPasswordDialogCancelButton() {
        Espresso.onView(ViewMatchers.withId(R.id.tvForgotPassword))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(android.R.id.button2))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etForgotEmailDialogForgotPassword))
                .check(ViewAssertions.doesNotExist());
    }

    @Test
    public void testEmptyEmailShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.tvForgotPassword))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(android.R.id.button1))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_email_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testInvalidEmailShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.tvForgotPassword))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etForgotEmailDialogForgotPassword))
                .perform(ViewActions.replaceText("rosszemail"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(android.R.id.button1))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_invalid_email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testResetPasswordOtpDialogFlow() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.tvForgotPassword))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etForgotEmailDialogForgotPassword))
                .perform(ViewActions.replaceText("teszt4@teszt.hu"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(android.R.id.button1))
                .perform(ViewActions.click());

        Thread.sleep(6000);

        Espresso.onView(ViewMatchers.withId(R.id.etOtpCodeDialogResetPassword))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(android.R.id.button1))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_otp_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_confirm_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.etOtpCodeDialogResetPassword))
                .perform(ViewActions.replaceText("123"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etResetNewPasswordDialogResetPassword))
                .perform(ViewActions.replaceText("gyenge"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etResetConfirmPasswordDialogResetPassword))
                .perform(ViewActions.replaceText("gyenge2"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(android.R.id.button1))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_otp_length))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_weak_password))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_passwords_mismatch))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(android.R.id.button2)).perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etOtpCodeDialogResetPassword))
                .check(ViewAssertions.doesNotExist());
    }
}
