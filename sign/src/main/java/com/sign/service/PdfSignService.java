package com.sign.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;


public class PdfSignService {
    
    private static final String KEYSTORE = "/keystore/ks";
    private static final char[] PASSWORD = "password".toCharArray();
    private static final int MAX_LOGO_WIDTH = 200;
    private static final int MAX_LOGO_HEIGHT = 50;
    private static final Font TEXT_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 8);
    private static String SIGN_DATE = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));


    TreeSet<Float> yList;

    protected String getCNFromX509Certificate(X509Certificate cert) {
        String subjectDN = cert.getSubjectX500Principal().getName();
        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            if(part.startsWith("CN=")) return part.substring(3);
        }
        return "Sem CN";
    }

    protected KeyStore loadKeyStore() throws IOException, GeneralSecurityException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try(InputStream ksStream = PdfSignService.class.getResourceAsStream(KEYSTORE)) {
            if (ksStream == null) {
                throw new IOException("key store not found!");
            }
            ks.load(ksStream, PASSWORD);
        }

        return ks;
    }

    protected void setAppearance(PdfSignatureAppearance appearance, String reason, String location, String certName) throws IOException, DocumentException {
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
        appearance.setLayer2Font(TEXT_FONT);

        String signatureText = "Assinado digitalmente por: \n" + certName + "\nUniversidade de Brasília\nData: " + SIGN_DATE;

        appearance.setLayer2Text(signatureText);
        addLogoToAppearance(appearance);
    }

    private void addLogoToAppearance(PdfSignatureAppearance appearance) throws IOException, DocumentException {
        InputStream imageStream = getClass().getResourceAsStream("/logo_unb.png");
        Image logo = Image.getInstance(imageStream.readAllBytes());
        logo.scaleToFit(MAX_LOGO_WIDTH, MAX_LOGO_HEIGHT);
        appearance.setSignatureGraphic(logo);
    }

    private Rectangle getSignatureRectangle(Float llx, Float lly, Float pageWidth, Float pageHeight) {

        // Dimensões do retângulo
        Float width = 180f;
        Float height = 50f;
    
        // Restringir o valor de lly (coordenada Y do canto inferior esquerdo)
        lly = Math.max(0f, Math.min(lly, pageHeight - height));
    
        // Restringir o valor de llx (coordenada X do canto inferior esquerdo)
        llx = Math.max(0f, Math.min(llx, pageWidth - width));
    
        // a coordenada superior direita com base nas coordenadas inferiores
        Float urx = llx + width;
        Float ury = lly + height;
    
        return new Rectangle(llx, lly, urx, ury);
    }

    protected void sign(String src, String dest, Certificate[] chain,
                      PrivateKey pk, String digestAlgorithm, String provider,
                      CryptoStandard subFilter, String reason, String location, String certName, int pageNumber, Float posX, Float posY)
            throws GeneralSecurityException, IOException, DocumentException {
                PdfReader reader = new PdfReader(src);

                float pageWidth = reader.getPageSize(pageNumber).getWidth();
                float pageHeight = reader.getPageSize(pageNumber).getHeight();

                System.out.printf("%.2f %.2f%n", pageWidth, pageHeight);

                try (FileOutputStream os = new FileOutputStream(dest)) {
                    PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

                    PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                    setAppearance(appearance, reason, location, certName);
                    
                    appearance.setVisibleSignature(getSignatureRectangle(posX, posY, pageWidth, pageHeight), pageNumber, "sig");

                    ExternalDigest digest = new BouncyCastleDigest();
                    ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);

                    MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subFilter);
                }

                
        }

    public static void executeSign(String SRC, String DEST, String certName, int pageNumber, Float posX, Float posY)
            throws GeneralSecurityException, IOException, DocumentException{
        
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = new PdfSignService().loadKeyStore();

        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfSignService app = new PdfSignService();

        app.sign(SRC, String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256,
                provider.getName(), CryptoStandard.CMS, "Test", "Brasília", certName, pageNumber, posX, posY);
    }

}