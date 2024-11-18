package com.sign.controller;

import com.sign.service.PdfSignService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class PdfSignControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private PdfSignController pdfSignController;

    private File tempPdfFile;

    @BeforeEach
    public void setup() throws IOException {
        // Criação de um arquivo PDF temporário para teste
        tempPdfFile = File.createTempFile("test", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdfFile)) {
            fos.write("PDF Test".getBytes());  // Escreve dados de exemplo no arquivo
        }
        // Inicializa o MockMvc com o controlador
        mockMvc = MockMvcBuilders.standaloneSetup(pdfSignController).build();
    }

    @AfterEach
    public void tearDown() {
        // Exclui o arquivo temporário após o teste
        if (tempPdfFile.exists()) {
            tempPdfFile.delete();
        }
    }

    @Test
    public void testSignPdf() throws Exception {
        // Cria o MockMultipartFile a partir do arquivo temporário
        MockMultipartFile file = new MockMultipartFile("file", tempPdfFile.getName(),
        "application/pdf", Files.readAllBytes(tempPdfFile.toPath()));


        // Envia o arquivo para a API e verifica se a resposta é OK (200)
        mockMvc.perform(multipart("/api/pdf/sign").file(file))
                .andExpect(status().isOk());
    }

    @Test
    public void testSignPdf_InvalidFile() throws Exception {
        // Envia uma requisição com um arquivo inválido
        mockMvc.perform(multipart("/api/pdf/sign").file("file", "invalid".getBytes()))
                .andExpect(status().isBadRequest());  // Verifica que retorna status 400
    }

    @Test
    public void testSignPdf_InternalError() throws Exception {
        // Simula um erro no serviço
        mockMvc.perform(multipart("/api/pdf/sign").file("file", new byte[0]))  // Envia um arquivo vazio
                .andExpect(status().isInternalServerError());  // Verifica que retorna status 500
    }
}
