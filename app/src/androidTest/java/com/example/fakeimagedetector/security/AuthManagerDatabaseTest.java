package com.example.fakeimagedetector.security;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AuthManagerDatabaseTest {

    private AuthManager authManager;
    private DatabaseHelper dbHelper;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        context.deleteDatabase("UserAuth.db");
        authManager = new AuthManager(context);
    }

    @After
    public void closeDb() {
        dbHelper.close();
    }

    @Test
    public void testRegisterAndLoginSuccess() {
        String user = "testUser";
        String pass = "securePassword123";

        boolean isRegistered = authManager.register(user, pass);
        assertTrue("La registrazione dovrebbe avere successo", isRegistered);

        boolean loginOk = authManager.login(user, pass);
        assertTrue("Il login dovrebbe avere successo con credenziali corrette", loginOk);
    }

    @Test
    public void testLoginFailureWrongPassword() {
        String user = "testUser";
        authManager.register(user, "correctPass");

        boolean loginFail = authManager.login(user, "wrongPass");
        assertFalse("Il login dovrebbe fallire con password errata", loginFail);
    }

    @Test
    public void testDuplicateUserRegistration() {
        String user = "uniqueUser";
        authManager.register(user, "pass1");

        boolean secondRegister = authManager.register(user, "pass2");
        assertFalse("La registrazione di un utente duplicato dovrebbe fallire", secondRegister);
    }

    @Test
    public void testIsAnyUserRegistered() {
        assertFalse(authManager.isAnyUserRegistered());
        authManager.register("admin", "admin123");
        assertTrue(authManager.isAnyUserRegistered());
    }
}