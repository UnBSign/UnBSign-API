package com.sign.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

public class ResponseServiceTest {
    
    private ResponseService responseService = new ResponseService();

    @Test
    void testCreateFileResponse() throws IOException {
        File tempFile = File.createTempFile("test", ".pdf");
        String fileContent = "TESTE";
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(fileContent.getBytes());
        }

        // Testa o método createFileResponse
        ResponseEntity<byte[]> response = responseService.createFileResponse(tempFile.getAbsolutePath());

        // verifica status
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(fileContent, new String(response.getBody()));

        assertTrue(tempFile.delete());
    }

    @Test
    void testCreateFileResponse_FileNotFound() {
        // Testa para um arquivo inexistente
        String invalidFilePath = "invalid-file.pdf";

        IOException exception = assertThrows(IOException.class, () ->
                responseService.createFileResponse(invalidFilePath));

        assertEquals("arquivo assinado nao encontrado: " + invalidFilePath, exception.getMessage());
    }

}
