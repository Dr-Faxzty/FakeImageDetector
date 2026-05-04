package com.example.fakeimagedetector;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.fakeimagedetector.security.HashUtils;

public class HashUtilsTest {

    @Test
    public void testHashConsistency() {
        String password = "password123";
        String hash1 = HashUtils.hashPassword(password);
        String hash2 = HashUtils.hashPassword(password);

        assertEquals(hash1, hash2);
    }

    @Test
    public void testDifferentPasswords() {
        String hash1 = HashUtils.hashPassword("admin");
        String hash2 = HashUtils.hashPassword("root");

        assertNotEquals(hash1, hash2);
    }
}