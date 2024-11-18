package com.sign.controller;

import com.sign.service.PdfSignService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.GeneralSecurityException;

import com.itextpdf.text.DocumentException;



@RestController
@RequestMapping("/api/pdf")
public class PdfSignController {

    private final PdfSignService pdfSignService;

    @Autowired
    public PdfSignController(PdfSignService pdfSignService) {
        this.pdfSignService = pdfSignService;
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquiv inválido. Envie um arquivo PDF");
        }

        try {
            //salva pdf temporiaramente
            File tempFile = File.createTempFile("uploaded-", ".pdf");
            try (OutputStream os = new FileOutputStream(tempFile)) {
                os.write(file.getBytes());
            }

            //arquivo de saida assinado
            File signedFile = new File(tempFile.getParent(), "signed_" + file.getOriginalFilename());
            pdfSignService.executeSign(tempFile.getAbsolutePath(), signedFile.getAbsolutePath(), "Eu, o testador");

            //retorna arquivo assinado
            InputStream signedFileStream = new FileInputStream(signedFile);
            byte[] signedContent = signedFileStream.readAllBytes();
            signedFileStream.close();

            //resposta com arquivo assinado
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + signedFile.getName());
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return ResponseEntity.ok().headers(headers).body(signedContent);


        } catch (IOException | GeneralSecurityException | DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao assinar o PDF: " + e.getMessage());
        }
    }
    

}

