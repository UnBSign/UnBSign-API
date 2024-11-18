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

import org.springframework.stereotype.Service;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
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

@Service
public class PdfSignService {
    
    public static final String KEYSTORE = "/keystore/ks";
    public static final char[] PASSWORD = "password".toCharArray();

    TreeSet<Float> yList;

    protected String getCNFromX509Certificate(X509Certificate cert) {
        String subjectDN = cert.getSubjectX500Principal().getName();
        String[] parts = subjectDN.split(",");
        for (String part : parts) {
            if(part.startsWith("CN=")) return part.substring(3);
        }
        return "Sem CN";
    }

    protected void sign(String src, String dest, Certificate[] chain,
                      PrivateKey pk, String digestAlgorithm, String provider,
                      CryptoStandard subFilter, String reason, String location, String certName)
            throws GeneralSecurityException, IOException, DocumentException {
                PdfReader reader = new PdfReader(src);
                FileOutputStream os = new FileOutputStream(dest);
                PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

                this.yList = new TreeSet<Float>();
                PdfReaderContentParser parser = new PdfReaderContentParser(reader);
                parser.processContent(reader.getNumberOfPages(), new TextMarginFinder() {
                    @Override
                    public void renderText(TextRenderInfo renderInfo) {
                        super.renderText(renderInfo);
                        PdfSignService.this.yList.add(renderInfo.getBaseline().getBoundingRectange().y);
                    }
                });

                PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                appearance.setReason(reason);
                appearance.setLocation(location);
                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
                appearance.setLayer2Font(new Font(Font.FontFamily.TIMES_ROMAN, 6));

                appearance.setLayer2Text("Assinado digitalmente por: " + certName + "\nUniversidade de Brasília\nData: " +
                        (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())));
                InputStream imageStream = getClass().getResourceAsStream("/logo_unb.png");
                appearance.setSignatureGraphic(Image.getInstance(imageStream.readAllBytes()));

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
                    }
                    else {
                        if (current - prior > 30) {
                            float half = (current + prior) / 2f;
                            lly = half - 12.5f;
                            ury = half + 12.5f;
                            break;
                        }
                    }
                }

                appearance.setVisibleSignature(new Rectangle(5, lly, 400, ury),
                        reader.getNumberOfPages(), "sig");


                ExternalDigest digest = new BouncyCastleDigest();
                ExternalSignature signature = new PrivateKeySignature(pk, digestAlgorithm, provider);
                MakeSignature.signDetached(appearance, digest, signature, chain,
                        null, null, null, 0, subFilter);
            }

    public static void executeSign(String SRC, String DEST, String certName)
            throws GeneralSecurityException, IOException, DocumentException{
        
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        //keystore que contem pk e certificado
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        try(InputStream ksStream = PdfSignService.class.getResourceAsStream(KEYSTORE)) {
            if (ksStream == null) {
                throw new IOException("key store not found!");
            }
            ks.load(ksStream, PASSWORD);
        }

        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfSignService app = new PdfSignService();

        app.sign(SRC, String.format(DEST, 1), chain, pk, DigestAlgorithms.SHA256,
                provider.getName(), CryptoStandard.CMS, "Test", "Brasília", certName);
    }

}
