package com.sign.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static EnvConfig instance;
    private final Dotenv dotenv;

    public final String KEYSTORE;
    public final char[] PASSWORD;
    public final String SECRET_KEY;
    public final String API_PKI;

    private EnvConfig() {
        dotenv = Dotenv.configure().load();

        KEYSTORE = dotenv.get("KEYSTORE");
        PASSWORD = dotenv.get("PASSWORD").toCharArray();
        SECRET_KEY = dotenv.get("SECRET_KEY");
        API_PKI = dotenv.get("API_PKI");
    }

    public static EnvConfig getInstance() {
        if (instance == null) {
            synchronized (EnvConfig.class) {
                if (instance == null) {
                    instance = new EnvConfig();
                }
            }
        }
        return instance;
    }

}
