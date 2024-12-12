package com.sign.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {
    public ResponseEntity<ByteArrayResource> createFileResponse(String filePath) throws IOException {
        File file = new File(filePath);

        if (!file.exists()) {
            throw new IOException("Signed File not found: " + filePath);
        }

        byte[] fileContent;
        try (FileInputStream fis = new FileInputStream(file)) {
            fileContent = fis.readAllBytes();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ByteArrayResource(("Error reading file: " + e.getMessage()).getBytes()));
        }

        ByteArrayResource resource = new ByteArrayResource(fileContent);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        // headers.add("Access-Control-Allow-Origin", "*");
        return ResponseEntity.ok().headers(headers)
                .body(resource);
    }
}

