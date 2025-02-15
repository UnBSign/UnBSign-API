package com.sign.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public ResponseEntity<Map<String, Object>> createValidationResponse(Map<String, Object> signaturesResults) {
        List<Map<String, Object>> signatures = (List<Map<String, Object>>) signaturesResults.get("Signatures");
        boolean allSignatureRecognized = Boolean.TRUE.equals(signaturesResults.get("AllSignaturesRecognized"));
        String documentHash = (String) signaturesResults.get("DocumentHash");
        
        boolean allValid = signatures.stream()
                                      .allMatch(signature -> Boolean.TRUE.equals(signature.get("Integrity")));
        
        Map<String, Object> response = Map.of(
                "success", allValid,  
                "signatures", signatures,
                "documentHash", documentHash,
                "allSignatureRecognized", allSignatureRecognized
            );
        
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> validateUploadedFile (MultipartFile file){
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is missing or empty");
        }

        if (!"application/pdf".equals(file.getContentType())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type, only PDF files are supported");
        }

        return null;
    }
}

