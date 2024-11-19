package com.sign.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

public class FileServiceTest {
    private FileService fileService;
    private File tempFile;

    @BeforeEach
    void setUp() {
        fileService = new FileService();
        tempFile = null;
    }

    @AfterEach
    void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            assertTrue(tempFile.delete());
        }
    }

    @Test
    void testSaveTempFile() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "PDF content".getBytes()
        );

        String tempFilePath = fileService.saveTempFile(mockFile);

        tempFile = new File(tempFilePath);
        assertTrue(tempFile.exists());
        assertTrue(tempFilePath.endsWith(".pdf"));

        assertEquals("PDF content", new String(java.nio.file.Files.readAllBytes(tempFile.toPath())));
    }

    @Test
    void testGetSignedFilePath() {
        String fileName = "test.pdf";

        String signedFilePath = fileService.getSignedFilePath(fileName);

        // verifica se o caminho contém o diretório temporário e o nome esperado
        String expectedPath = System.getProperty("java.io.tmpdir") + File.separator + "test_assinado.pdf";
        assertEquals(expectedPath, signedFilePath);
    }
}
