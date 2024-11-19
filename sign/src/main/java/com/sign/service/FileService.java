package com.sign.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
    // salva arquivo temporariamente
    public String saveTempFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("uploaded-", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(file.getBytes());
        }
        return tempFile.getAbsolutePath();
    }

    //caminho do arquivo assinado
    public String getSignedFilePath(String fileName) {

        String extension = "";

        int separator = fileName.lastIndexOf('.');

        if (separator > 0 && separator < fileName.length() - 1) {
            extension = fileName.substring(separator);
            fileName = fileName.substring(0, separator);
        }
        
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        return new File(tempDir, fileName + "_assinado" + extension).getAbsolutePath();
    }
}
