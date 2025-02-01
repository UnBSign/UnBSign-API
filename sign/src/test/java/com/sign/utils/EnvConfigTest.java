package com.sign.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnvConfigTest {

    private static EnvConfig config;

    @BeforeAll
    static void setUp() {
        config = EnvConfig.getInstance();
    }

    @Test
    void testKeyStoreLoaded() {
        assertNotNull(config.KEYSTORE, "KEYSTORE should not be null");
        assertFalse(config.KEYSTORE.isEmpty(), "KEYSTORE should not be empty");
    }

    @Test
    void testPasswordLoaded() {
        assertNotNull(config.PASSWORD, "PASSWORD should not be null");
        assertTrue(config.PASSWORD.length > 0, "PASSWORD should not be empty");
    }

    @Test
    void testSingletonInstance() {
        EnvConfig anotherConfig = EnvConfig.getInstance();
        assertSame(config, anotherConfig, "EnvConfig should be a singleton");
    }
}
