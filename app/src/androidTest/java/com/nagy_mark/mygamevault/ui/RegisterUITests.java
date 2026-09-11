package com.nagy_mark.mygamevault.ui;

import android.content.Context;

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

public class RegisterUITests {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        ActivityScenario.launch(MainActivity.class);
        Espresso.onView(ViewMatchers.withId(R.id.tvRegister))
                .perform(ViewActions.click());
    }

    @Test
    public void testEmptyFieldsShowErrors() {
        Espresso.onView(ViewMatchers.withId(R.id.btnRegister))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_email_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_confirm_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testInvalidEmailPasswordAndConfirmPassword() {
        Espresso.onView(ViewMatchers.withId(R.id.etRegisterEmail))
                .perform(ViewActions.replaceText("asd"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPassword))
                .perform(ViewActions.replaceText("asd"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPasswordConfirm))
                .perform(ViewActions.replaceText("asda"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnRegister))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_invalid_email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_weak_password))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_passwords_mismatch))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testEmailAlredyRegistered() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.etRegisterEmail))
                .perform(ViewActions.replaceText("teszt1@teszt.hu"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPassword))
                .perform(ViewActions.replaceText("Teszt!01"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPasswordConfirm))
                .perform(ViewActions.replaceText("Teszt!01"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnRegister))
                .perform(ViewActions.click());

        Thread.sleep(3000);

        Espresso.onView(ViewMatchers.withText(R.string.error_email_registered))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSuccessfulRegistrationFlow() throws InterruptedException {
        String uniqueEmail = "teszt_" + System.currentTimeMillis() + "@teszt.hu";

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterEmail))
                .perform(ViewActions.replaceText(uniqueEmail), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPassword))
                .perform(ViewActions.replaceText("Teszt!03"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etRegisterPasswordConfirm))
                .perform(ViewActions.replaceText("Teszt!03"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnRegister))
                .perform(ViewActions.click());

        Thread.sleep(3000);

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationToLogin() {
        Espresso.onView(ViewMatchers.withId(R.id.tvLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
