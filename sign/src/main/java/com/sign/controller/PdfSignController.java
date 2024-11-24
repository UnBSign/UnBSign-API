package com.sign.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.sign.service.FileService;
import com.sign.service.PdfSignService;
import com.sign.service.ResponseService;



@RestController
@RequestMapping("/api/pdf")
public class PdfSignController {

    private final FileService fileService;
    private final ResponseService responseService;

    @Autowired
    public PdfSignController(FileService fileService, ResponseService responseService) {
        this.fileService = fileService;
        this.responseService = responseService;
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signPdf(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "x", required = false) Float x,
                                     @RequestParam(value = "y", required = false) Float y) {
        if (file.isEmpty() || !file.getContentType().equals("application/pdf")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Arquivo inválido. Envie um arquivo PDF");
        }

        try {
            //salva pdf temporiaramente
            String tempFilePath = fileService.saveTempFile(file);

            //arquivo de saida assinado
            String signedFilePath = fileService.getSignedFilePath(file.getOriginalFilename());
            PdfSignService.executeSign(tempFilePath, signedFilePath, "Eu, o testador", 1f, 50f);

            //retorna arquivo assinado
            return responseService.createFileResponse(signedFilePath);


        } catch (IOException | GeneralSecurityException | DocumentException e) {
            return ResponseEntity.internalServerError().body("Erro ao assinar arquivo: " + e.getMessage());
        }
    }
}

