package com.sign.service;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.io.File;

@Service
public class CertificateService {

    private final KeyStoreManager ksManager;
    private static final char[] PASSWORD = "password".toCharArray();
    private PrivateKey privateKeyForCsr;

    public CertificateService(KeyStoreManager ksManager) {
        this.ksManager = ksManager;
    }

    public String createCsr(String id, String commonName) throws Exception {
        KeyStore ks = ksManager.loadKeyStore();


        KeyPair keyPair = generateRsaKeyPair();

        PublicKey publicKey = keyPair.getPublic();
        privateKeyForCsr = keyPair.getPrivate();

        PKCS10CertificationRequest csr = generateCsr(privateKeyForCsr, publicKey, commonName);

        //saveCsrToFile(csr, id);

        try (ByteArrayOutputStream csrOut = new ByteArrayOutputStream()) {
            csrOut.write("-----BEGIN CERTIFICATE REQUEST-----\n".getBytes());
            csrOut.write(Base64.getEncoder().encode(csr.getEncoded()));
            csrOut.write("\n-----END CERTIFICATE REQUEST-----\n".getBytes());
    
            return csrOut.toString();
        }

    }

    private PKCS10CertificationRequest generateCsr(PrivateKey privateKey, PublicKey publicKey, String commonName) throws Exception{
        X500Name subject = new X500Name("CN=" + commonName + ", O=Universidade de Brasília, C=BR");

        PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder (
            subject, publicKey
        );
        JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer = csBuilder.build(privateKey);
        PKCS10CertificationRequest csr = p10Builder.build(signer);

        return csr;
    }

    private void saveCsrToFile(PKCS10CertificationRequest csr, String id) throws IOException {
        Path csrDirectory = Paths.get("certs/csr");
        if (!Files.exists(csrDirectory)) {
            Files.createDirectories(csrDirectory);
        }

        Path csrFilePath = csrDirectory.resolve(id + ".csr");

        try (BufferedWriter writer = Files.newBufferedWriter(csrFilePath, StandardCharsets.UTF_8)) {
            writer.write("-----BEGIN CERTIFICATE REQUEST-----\n");
            writer.write(Base64.getEncoder().encodeToString(csr.getEncoded()));
            writer.write("\n-----END CERTIFICATE REQUEST-----\n");
        }
    }

    public void createAndStoreCertificate(String id, String cn){

        try {
            KeyPair keyPair = generateRsaKeyPair();
            X509Certificate certificate = generateSelfSignedCertificate(keyPair, cn);
            storeCertificate(id, certificate);

        } catch (Exception e) {
            throw new RuntimeException("Error to generate certificate: " + e.getMessage(), e);
        }
    }

    public String processAndStore(String id, String signedCertContent) throws RuntimeException {
        try {
            X509Certificate signedCertificate = convertPemToX509Certificate(signedCertContent);
    
            // KeyStore ks = ksManager.loadKeyStore();
            // PrivateKey privateKey = (PrivateKey) ks.getKey(id, PASSWORD);
            
            // if (privateKey == null) {
            //     throw new RuntimeException("Private key not found for alias: " + id);
            // }
    
            storeCertificate(id, signedCertificate);
    
            return "Certificate stored successfully.";
        } catch (Exception e) {
            throw new RuntimeException("Error processing the signed certificate: " + e.getMessage(), e);
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

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byteArrayOutputStream.write("-----BEGIN CERTIFICATE-----\n".getBytes());
            byteArrayOutputStream.write(Base64.getEncoder().encode(cert.getEncoded()));
            byteArrayOutputStream.write("\n-----END CERTIFICATE-----\n".getBytes());
            return byteArrayOutputStream.toString();
        }
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048); 
        return keyGen.generateKeyPair();
    }

    public void storeCertificate(String alias, X509Certificate certificate) {
        try {
            KeyStore ks = ksManager.loadKeyStore();
            
            ks.setKeyEntry(alias, privateKeyForCsr, PASSWORD, new java.security.cert.Certificate[]{certificate});
            
            ksManager.storeKeyStore(ks);
        } catch (Exception e) {
            throw new RuntimeException("Error to store certificate in KeyStore: " + e.getMessage(), e);
        }
    }

    private X509Certificate convertPemToX509Certificate(String pem) throws Exception {
        String cleanedPem = pem.replace("-----BEGIN CERTIFICATE-----", "")
                               .replace("-----END CERTIFICATE-----", "")
                               .replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleanedPem);
        
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
        
        return (X509Certificate) certFactory.generateCertificate(inputStream);
    }
}
