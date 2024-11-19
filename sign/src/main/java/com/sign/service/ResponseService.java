package com.sign.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {
    public ResponseEntity<byte[]> createFileResponse(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) throw new IOException("arquivo assinado nao encontrado: " + filePath);

        byte[] fileContent;
        try (FileInputStream fis = new FileInputStream(file)) {
            fileContent = fis.readAllBytes();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

        return ResponseEntity.ok().headers(headers).body(fileContent);

    }
}
