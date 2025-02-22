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
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.TextMarginFinder;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.SignaturePolicyInfo;

import org.springframework.stereotype.Service;

import com.sign.security.KeyStoreManager;

@Service
public class PdfSignService {
    
    private static final char[] PASSWORD = "password".toCharArray();
    private static final int MAX_LOGO_WIDTH = 200;
    private static final int MAX_LOGO_HEIGHT = 50;
    private static final Font TEXT_FONT = new Font(Font.FontFamily.TIMES_ROMAN, 8);

    TreeSet<Float> yList;

    private final KeyStoreManager ksManager;

    public PdfSignService(KeyStoreManager ksManager) {
        this.ksManager = ksManager;
    }

    private String getCNFromX509Certificate(X509Certificate cert) {
        String subjectDN = cert.getSubjectX500Principal().getName();
        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            if(part.startsWith("CN=")) return part.substring(3);
        }
        return "Sem CN";
    }

    private void setAppearance(PdfSignatureAppearance appearance, String reason, String location, String certName) throws IOException, DocumentException {
        String signDate = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
        appearance.setLayer2Font(TEXT_FONT);

        String signatureText = "Assinado digitalmente por: \n" + certName + "\nUniversidade de Brasília\nData: " + signDate;

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

        if (llx == null || lly == null) {
            return setDefaultSignatureRectangle();
        }

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

    private Rectangle setDefaultSignatureRectangle() {
        float lly = 5f, ury = 30f;
        Float current = Float.MAX_VALUE, prior;
    
        for (Float y : yList) {
            prior = current;
            current = y;
            if (prior == Float.MAX_VALUE && current > 30) {
                float half = current / 2f;
                lly = half - 12.5f;
                ury = half + 12.5f;
                break;
            } else if (current - prior > 30) {
                float half = (current + prior) / 2f;
                lly = half - 12.5f;
                ury = half + 12.5f;
                break;
            }
        }
        return new Rectangle(5, lly, 400, ury);
    } 

    protected void setSignaturePosition(PdfReader reader) throws IOException{
        this.yList = new TreeSet<>();
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(reader.getNumberOfPages(), new TextMarginFinder() {
            @Override
            public void renderText(TextRenderInfo renderInfo) {
                super.renderText(renderInfo);
                yList.add(renderInfo.getBaseline().getBoundingRectange().y);
            }
        });
    }

    protected String getNextSignatureFieldName(PdfReader reader) throws IOException {
        AcroFields acroFields = reader.getAcroFields();
        List<String> signatureNames = acroFields.getSignatureNames();
        
        int maxIndex = 0;
        for (String name : signatureNames) {
            if (name.startsWith("sig")) {
                try {
                    int index = Integer.parseInt(name.substring(3));
                    maxIndex = Math.max(maxIndex, index);
                } catch (NumberFormatException e) {
                    
                }
            }
        }
        
        return "sig" + (maxIndex + 1);
    }

    private SignaturePolicyInfo generateSignaturePolicy() throws NoSuchAlgorithmException {
        
        String policyIdentifier = "2.16.76.1.5.1.1.1";
        byte[] policyHash = MessageDigest.getInstance("SHA-256").digest("PolicyContent".getBytes());
        String policyDigestAlgorithm = "SHA-256";
        String policyUrl = "https://www.unbsign/UnbSign-API/acraiz";
        return new SignaturePolicyInfo(policyIdentifier, policyHash, policyDigestAlgorithm, policyUrl);
    }

    protected void sign(String src, String dest, Certificate[] chain,
                      PrivateKey pk, String digestAlgorithm, String provider,
                      CryptoStandard subFilter, String reason, String location, String certName, Integer pageNumber, Float posX, Float posY)
            throws GeneralSecurityException, IOException, DocumentException {
                PdfReader reader = new PdfReader(src);
                
                if (pageNumber == 0) {
                    pageNumber = reader.getNumberOfPages();
                }

                float pageWidth = reader.getPageSize(pageNumber).getWidth();
                float pageHeight = reader.getPageSize(pageNumber).getHeight();

                try (FileOutputStream os = new FileOutputStream(dest)) {
                    PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);

                    PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                    setAppearance(appearance, reason, location, certName);

                    String signatureFieldName = getNextSignatureFieldName(reader);

                    if (posX == null || posY == null) {
                        setSignaturePosition(reader);
                        appearance.setVisibleSignature(setDefaultSignatureRectangle(), pageNumber, signatureFieldName);
                    } else {
                        appearance.setVisibleSignature(getSignatureRectangle(posX, posY, pageWidth, pageHeight), pageNumber, signatureFieldName);
                    }

                    ExternalDigest digest = new BouncyCastleDigest();
                    ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);

                    SignaturePolicyInfo spi = generateSignaturePolicy();

                    MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, subFilter, spi);
                }

                
        }

    public void executeSign(String SRC, String DEST, String id, int pageNumber, Float posX, Float posY)
            throws GeneralSecurityException, IOException, DocumentException{
        
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        KeyStore ks = ksManager.loadKeyStore();
        
        String alias = id;
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        
        if(cert == null){
            throw new GeneralSecurityException("User Certificate Not Found");
        }

        String certName = getCNFromX509Certificate(cert);

        PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        Certificate[] chain = ks.getCertificateChain(alias);

        sign(SRC, String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256,
                provider.getName(), CryptoStandard.CMS, "Test", "Brasília", certName, pageNumber, posX, posY);
    }

}