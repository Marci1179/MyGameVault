package com.nagy_mark.mygamevault.ui;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.nagy_mark.mygamevault.MainActivity;
import com.nagy_mark.mygamevault.R;
import com.nagy_mark.mygamevault.utils.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginUITests {

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.clearSession();
        ActivityScenario.launch(MainActivity.class);
    }

    @After
    public void tearDown() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SessionManager sessionManager = new SessionManager(context);
        sessionManager.clearSession();
    }

    @Test
    public void testEmptyFieldsShowErrors() {
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_email_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withText(R.string.error_password_required))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testInvalidEmailShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                .perform(ViewActions.replaceText("hibasemailformatum"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withText(R.string.error_invalid_email))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testWrongCredentialsBlockLogin() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                .perform(ViewActions.replaceText("nemletezo@mygamevault.hu"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etLoginPassword))
                .perform(ViewActions.replaceText("RosszJelszo123!"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Thread.sleep(3000);

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSuccessfulLoginFlow() throws InterruptedException {
        Espresso.onView(ViewMatchers.withId(R.id.etLoginEmail))
                .perform(ViewActions.replaceText("teszt1@teszt.hu"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.etLoginPassword))
                .perform(ViewActions.replaceText("teszt1"), ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Thread.sleep(3000);

        Espresso.onView(ViewMatchers.withId(R.id.rvLibrary))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testNavigationToRegister() {
        Espresso.onView(ViewMatchers.withId(R.id.tvRegister))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.btnRegister))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}