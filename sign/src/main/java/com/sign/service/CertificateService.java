package com.sign.service;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

import java.math.BigInteger;

// import sun.security.x509.*;
@Service
public class CertificateService {

    private static final String KEYSTORE = "/keystore/ks";
    private static final char[] PASSWORD = "password".toCharArray();

    public void createAndStoreCertificate(String id, String cn){
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            X509Certificate certificate = generateSelfSignedCertificate(keyPair, cn);

            KeyStore ks = loadKeyStore();
            ks.setKeyEntry(id, keyPair.getPrivate(), PASSWORD, new java.security.cert.Certificate[]{certificate});

            try (FileOutputStream fos = new FileOutputStream(KEYSTORE)) {
                ks.store(fos, PASSWORD);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error to generate certificate: " + e.getMessage(), e);
        }
    }

    protected KeyStore loadKeyStore() throws IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try(InputStream ksStream = getClass().getResourceAsStream(KEYSTORE)) {
            if (ksStream == null) {
                throw new IOException("key store not found!");
            }
            ks.load(ksStream, PASSWORD);
        }

        return ks;
    }

    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String cn) throws Exception {

        long validity = 365 * 24 * 60 * 60 * 1000L;
        Date notBefore = new Date();
        Date notAfter = new Date(notBefore.getTime() + validity);

        BigInteger serial = new BigInteger(64, new SecureRandom());

        X500Name issuer = new X500Name("CN=" + cn + ", O=Universidade de Brasília, C=BR");
        X500Name subject = issuer;

        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
                issuer,          // Emissor
                serial,          // Número de série
                notBefore,       // Data de início da validade
                notAfter,        // Data de expiração
                subject,         // Sujeito
                publicKeyInfo    // Informação da chave pública
        );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

        X509CertificateHolder certHolder = certBuilder.build(signer);

        JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
        X509Certificate certificate = certConverter.getCertificate(certHolder);

        return certificate;

    }

    public void deleteAllCertificates() {
        try {
            KeyStore ks = loadKeyStore();
            List<String> aliasesToDelete = new ArrayList<>();
            Enumeration<String> aliases = ks.aliases();
            
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                aliasesToDelete.add(alias);
            }

            for (String alias : aliasesToDelete) {
                ks.deleteEntry(alias);
            }

            try (FileOutputStream fos = new FileOutputStream(KEYSTORE)) {
                ks.store(fos, PASSWORD);
            } 
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete certificates from keystore", e);
        }
    }
}
