package com.sign.service;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Base64;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.sign.security.KeyStoreManager;
import java.math.BigInteger;

@Service
public class CertificateService {

    private final KeyStoreManager ksManager;
    private static final String KEYSTORE = "/keystore/ks";
    private static final char[] PASSWORD = "password".toCharArray();

    public CertificateService(KeyStoreManager ksManager) {
        this.ksManager = ksManager;
    }

    public void createAndStoreCertificate(String id, String cn){

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            X509Certificate certificate = generateSelfSignedCertificate(keyPair, cn);
            System.out.println(certificate);
            KeyStore ks = ksManager.loadKeyStore();
            
            ks.setKeyEntry(id + "_cert", keyPair.getPrivate(), PASSWORD, new java.security.cert.Certificate[]{certificate});
            ksManager.storeKeyStore(ks);

        } catch (Exception e) {
            throw new RuntimeException("Error to generate certificate: " + e.getMessage(), e);
        }
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
            KeyStore ks = ksManager.loadKeyStore();
            List<String> aliasesToDelete = new ArrayList<>();
            Enumeration<String> aliases = ks.aliases();
            
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                aliasesToDelete.add(alias);
            }

            for (String alias : aliasesToDelete) {
                ks.deleteEntry(alias);
            }

            ksManager.storeKeyStore(ks);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete certificates from keystore", e);
        }
    }

    public String getCertificateByAlias(String alias) throws GeneralSecurityException, IOException {
        KeyStore ks = ksManager.loadKeyStore();
        Certificate cert = ks.getCertificate(alias);
        if (cert == null) {
            throw new IOException("Certificate not found for alias: " + alias);
        }

        // Convertendo o certificado para o formato PEM (Base64)
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write("-----BEGIN CERTIFICATE-----\n".getBytes());
            byteArrayOutputStream.write(Base64.getEncoder().encode(cert.getEncoded()));
            byteArrayOutputStream.write("\n-----END CERTIFICATE-----\n".getBytes());
            return byteArrayOutputStream.toString();
        }
    }
}
