package com.sign.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;
import com.sign.service.FileService;
import com.sign.service.PdfSignService;
import com.sign.service.ResponseService;
import com.sign.service.PdfValidateService;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    private final FileService fileService;
    private final ResponseService responseService;

    @Autowired
    public PdfController(FileService fileService, ResponseService responseService) {
        this.fileService = fileService;
        this.responseService = responseService;
    }

    @PostMapping("/signature")
    public ResponseEntity<?> signPdf(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "posX", required = false) Float posX,
                                     @RequestParam(value = "posY", required = false) Float posY,
                                     @RequestParam(value = "pageNumber", required =  false) int pageNumber) {
        validateUploadedFile(file);

        try {
            //salva pdf temporiaramente
            String tempFilePath = fileService.saveTempFile(file);

            //arquivo de saida assinado
            String signedFilePath = fileService.getSignedFilePath(file.getOriginalFilename());
            PdfSignService.executeSign(tempFilePath, signedFilePath, "Eu, o testador", pageNumber, posX, posY);

            //retorna arquivo assinado
            return responseService.createFileResponse(signedFilePath);


        } catch (IOException | GeneralSecurityException | DocumentException e) {
            return ResponseEntity.internalServerError().body("Error to sign file: " + e.getMessage());
        }
    }

    @PostMapping("/validation")
    public ResponseEntity<?> postMethodName(@RequestParam("file") MultipartFile file) {
        validateUploadedFile(file);

        try {
            String tempFilePath = fileService.saveTempFile(file);
            PdfValidateService service = new PdfValidateService();

            boolean isValid = service.validateSignature(tempFilePath);
            
            if (isValid) {
                return ResponseEntity.ok(Map.of("success", true, "message", "The PDF signature is valid."));
            } else {
                return ResponseEntity.ok(Map.of("success", false, "message", "The PDF signature is invalid."));
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error validating file: " + e.getMessage());
        }
    }

    private ResponseEntity<?> validateUploadedFile (MultipartFile file){
        if (file == null || file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is missing or empty");
        }

        if (!"application/pdf".equals(file.getContentType())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file type, only PDF files are supported");
        }

        return null;
    }
    
}

