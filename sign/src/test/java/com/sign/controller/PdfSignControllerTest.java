package com.sign.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.sign.service.FileService;
import com.sign.service.PdfSignService;
import com.sign.service.ResponseService;

class PdfSignControllerTest {

    @InjectMocks
    private PdfSignController pdfSignController;

    @Mock
    private FileService fileService;

    @Mock
    private ResponseService responseService;

    @Mock
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);  // Inicializa os mocks
    }

    // @Test
    // void testSignPdf_Success() throws Exception {
    //     // Mocking the behavior of FileService and ResponseService
    //     when(file.isEmpty()).thenReturn(false);
    //     when(file.getContentType()).thenReturn("application/pdf");
    //     when(file.getOriginalFilename()).thenReturn("test.pdf");

    //     // Mocking the file service methods
    //     when(fileService.saveTempFile(file)).thenReturn("tempFilePath");
    //     when(fileService.getSignedFilePath("test.pdf")).thenReturn("signedFilePath");

    //     // Mocking PdfSignService.executeSign() (Assumindo que executeSign é um método estático ou alterando o código para ser chamado por uma instância)
    //     PdfSignService pdfSignServiceMock = mock(PdfSignService.class);
    //     doNothing().when(pdfSignServiceMock).executeSign(anyString(), anyString(), anyString());

    //     // Mocking ResponseService createFileResponse()
    //     when(responseService.createFileResponse("signedFilePath")).thenReturn(ResponseEntity.ok().body("signedFileBytes".getBytes()));

    //     // Chamada ao método
    //     ResponseEntity<?> response = pdfSignController.signPdf(file);

    //     // Verifica se o método retornou com sucesso
    //     assertEquals(HttpStatus.OK, response.getStatusCode());
    //     verify(responseService, times(1)).createFileResponse("signedFilePath");
    // }

    @Test
    void testSignPdf_InvalidFile() {
        
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");

        // chamada ao método
        ResponseEntity<?> response = pdfSignController.signPdf(file);

        // verifica se retornou o status de BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Arquivo inválido. Envie um arquivo PDF", response.getBody());
    }

    @Test
    void testSignPdf_FileIsEmpty() {
        when(file.isEmpty()).thenReturn(true);

        // chamada ao método
        ResponseEntity<?> response = pdfSignController.signPdf(file);

        // verifica se retornou o status de BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Arquivo inválido. Envie um arquivo PDF", response.getBody());
    }

    @Test
    void testSignPdf_ExceptionHandling() throws Exception {
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getOriginalFilename()).thenReturn("test.pdf");

        when(fileService.saveTempFile(file)).thenThrow(new IOException("Erro ao salvar o arquivo"));

        // chamada ao método
        ResponseEntity<?> response = pdfSignController.signPdf(file);

        // verifica se retornou o status de INTERNAL_SERVER_ERROR
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro ao assinar arquivo: Erro ao salvar o arquivo", response.getBody());
    }
}
