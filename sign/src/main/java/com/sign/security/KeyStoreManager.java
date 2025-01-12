package com.sign.security;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;

@Component
public class KeyStoreManager {

    private static final String KEYSTORE = "/keystore/ks";
    private static final char[] PASSWORD = "password".toCharArray();

    public KeyStore loadKeyStore() throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream ksStream = KeyStoreManager.class.getResourceAsStream(KEYSTORE)) {
            if (ksStream == null){
                throw new IOException("Key Store not found");
            }
            ks.load(ksStream, PASSWORD);
        }
        return ks;
    }

    public void storeKeyStore(KeyStore ks) throws GeneralSecurityException, IOException {
        
        try (FileOutputStream fos = new FileOutputStream("/home/sidney/Documentos/UnB/UNBSIGN/UnBSign-API/sign/src/main/resources/keystore/ks")) {
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
