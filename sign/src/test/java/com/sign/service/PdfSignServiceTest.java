// package com.sign.service;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyChar;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.FileOutputStream;
// import java.io.InputStream;
// import java.io.IOException;
// import java.security.GeneralSecurityException;
// import java.security.PrivateKey;
// import java.security.cert.X509Certificate;
// import java.security.cert.Certificate;
// import java.security.KeyStore;

// import com.itextpdf.text.DocumentException;
// import com.itextpdf.text.pdf.PdfReader;
// import com.itextpdf.text.pdf.PdfStamper;
// import com.itextpdf.text.pdf.security.MakeSignature;
// import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
// import java.util.Arrays;
// import java.util.Collection;
// import java.util.stream.Stream;

// import javax.security.auth.x500.X500Principal;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.junit.jupiter.params.ParameterizedTest;
// import org.junit.jupiter.params.provider.Arguments;
// import org.junit.jupiter.params.provider.MethodSource;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
// import org.mockito.junit.jupiter.MockitoExtension;

// import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;

// public class PdfSignServiceTest {

//     private PdfSignService pdfSignService;
//     private PrivateKey mockPrivateKey;
//     private PdfSignService mockPdfSignService;
//     private KeyStore mockKeyStore;

    

//     public static Collection<Object[]> data() {
//         return Arrays.asList(new Object[][]{
//             {createMockCertificate("CN=João Silva"), "João Silva", "source.pdf", "destination.pdf", 
//              "Reason 1", "Location 1", "João Silva", "SHA-256", "BC", CryptoStandard.CMS},
//             {createMockCertificate("CN=Maria do Carmo"), "Maria do Carmo", "source.pdf", "destination.pdf",
//              "Reason 2", "Location 2", "Maria do Carmo", "SHA-256", "BC", CryptoStandard.CMS},
//             {createMockCertificate("O=Some Organization,CN=Alicia Texeira"), "Alicia Texeira", "source.pdf", "destination.pdf",
//              "Reason 3", "Location 3", "Alicia Texeira", "SHA-256", "BC", CryptoStandard.CMS},
//             {createMockCertificate("O=Another Organization"), "Sem CN", "source.pdf", "destination.pdf",
//              "Reason 4", "Location 4", "Another User", "SHA-256", "BC", CryptoStandard.CMS}
//         });
//     }

//     @BeforeEach
//     public void setUp(){
//         pdfSignService = new PdfSignService();
//         mockPrivateKey = mock(PrivateKey.class);
//         mockPdfSignService = mock(PdfSignService.class);

//         mockKeyStore = mock(KeyStore.class);

         
//     }

//     @ParameterizedTest
//     @MethodSource("data")
//     public void testGetCNFromX509Certificate(X509Certificate inputCertificate, String expectedCN) {
//         String actualCN = pdfSignService.getCNFromX509Certificate(inputCertificate);
//         assertEquals(expectedCN, actualCN);
//     }

//     @ParameterizedTest
//     @MethodSource("data")
//     public void testSign(X509Certificate inputCertificate, String expectedCN, String src, String dest, 
//                      String reason, String location, String certName, 
//                      String digestAlgorithm, String provider, CryptoStandard subFilter) 
//                      throws Exception {
        
//         Certificate[] mockChain = new Certificate[]{inputCertificate};
        
//         // Chama o método sign no mock
//         mockPdfSignService.sign(src, dest, mockChain, mockPrivateKey, digestAlgorithm, provider, subFilter, reason, location, certName);

//         // Verifica se o método sign foi chamado com os parâmetros corretos
//         verify(mockPdfSignService).sign(eq(src), eq(dest), eq(mockChain), eq(mockPrivateKey), eq(digestAlgorithm), eq(provider), eq(subFilter), eq(reason), eq(location), eq(certName));
//     }



//     // @ParameterizedTest
//     // @MethodSource("data")
//     // public void testExecuteSign(X509Certificate inputCertificate, String src, String dest, String certName) 
//     //                          throws GeneralSecurityException, IOException, DocumentException {
//     //     // Criação da cadeia de certificados
//     //     Certificate[] mockChain = new Certificate[]{inputCertificate};

//     //     // Configuração do KeyStore mock
//     //     X509Certificate mockCertificate = mock(X509Certificate.class);
//     //     when(mockKeyStore.getCertificate(anyString())).thenReturn(mockCertificate);
//     //     when(mockKeyStore.getKey(anyString(), any(char[].class))).thenReturn(mockPrivateKey);

//     //     try {
//     //         mockPdfSignService.executeSign(src, dest, certName);

//     //         // verifica se o método sign foi chamado com os parâmetros corretos
//     //         verify(mockPdfSignService).sign(eq(src), eq(dest), eq(mockChain), eq(mockPrivateKey), 
//     //                                         anyString(), anyString(), any(), 
//     //                                         anyString(), anyString(), eq(certName));
//     //     } catch (IOException e) {
//     //         if (!e.getMessage().contains("key store not found")) {
//     //             throw e;
//     //         }
//     //     }
//     // }

//     private static X509Certificate createMockCertificate(String subject) {
//         X509Certificate mockCertificate = mock(X509Certificate.class);
//         when(mockCertificate.getSubjectX500Principal()).thenReturn(new X500Principal(subject));
//         return mockCertificate;
//     }
// }