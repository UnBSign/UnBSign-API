package com.sign.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.springframework.stereotype.Component;

import com.sign.utils.EnvConfig;

@Component
public class KeyStoreManager {

    private static final EnvConfig config = EnvConfig.getInstance();
    
    private static final String KEYSTORE = config.KEYSTORE;
    private static final char[] PASSWORD = config.PASSWORD;

    public KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());

        try (FileInputStream fis = new FileInputStream(KEYSTORE)) {
            ks.load(fis, PASSWORD);
        } catch (IOException e) {
            throw new IOException("Failed to load the KeyStore: " + e.getMessage(), e);
        }

        return ks;
    }

    public void storeKeyStore(KeyStore ks) throws GeneralSecurityException, IOException {
        
        try (FileOutputStream fos = new FileOutputStream(KEYSTORE)) {
                ks.store(fos, PASSWORD);
        }
    }

    public void addCertificate(KeyStore ks, String alias, java.security.cert.Certificate cert) throws GeneralSecurityException, IOException {
        ks.setCertificateEntry(alias, cert);
        storeKeyStore(ks);
    }

    public void deleteCertificate(KeyStore ks, String alias) throws GeneralSecurityException, IOException {
        ks.deleteEntry(alias);
        storeKeyStore(ks);
    }
}
