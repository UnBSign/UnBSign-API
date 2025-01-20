package com.sign.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import com.itextpdf.text.DocumentException;
import com.sign.service.FileService;
import com.sign.service.PdfSignService;
import com.sign.service.ResponseService;
import com.sign.service.PdfValidateSignService;
import com.sign.dto.SignPdfRequest;
import com.sign.utils.JwtUtils;


@RestController
@RequestMapping("/api/pdf")
public class PdfController {

    @Autowired
    private final FileService fileService;
    private final ResponseService responseService;
    private final PdfSignService pdfSignService;

    public PdfController(FileService fileService, ResponseService responseService, PdfSignService pdfSignService) {
        this.fileService = fileService;
        this.responseService = responseService;
        this.pdfSignService = pdfSignService;
    }

    @PostMapping("/signature")
    public ResponseEntity<?> signPdf(
        @ModelAttribute SignPdfRequest request,
        @RequestHeader(value = "Authorization", required = false) String authorizationHeader
        ) {
        MultipartFile file = request.getFile();
        Float posX = request.getPosX();
        Float posY = request.getPosY();
        int pageNumber = request.getPageNumber();
        // String id = request.getId();
        System.out.println(authorizationHeader);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            
            String id= JwtUtils.extractUserId(token);
            System.out.println("User ID: " + id);

            validateUploadedFile(file);

            try {
                String tempFilePath = fileService.saveTempFile(file);

                String signedFilePath = fileService.getSignedFilePath(file.getOriginalFilename());
                pdfSignService.executeSign(tempFilePath, signedFilePath, id, pageNumber, posX, posY);

                return responseService.createFileResponse(signedFilePath);

            } catch(IllegalArgumentException e){ 
                return ResponseEntity.badRequest().body(e.getMessage());
            } catch (IOException | GeneralSecurityException | DocumentException e) {
                if (e.getMessage().equals("User Certificate Not Found")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Certificado do usuário não encontrado");
                }
                return ResponseEntity.internalServerError().body("Error to sign file: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token not provided");
        }

        
    }

    @PostMapping("/validation")
    public ResponseEntity<?> validatePdfSignature(@ModelAttribute SignPdfRequest request) {
        MultipartFile file = request.getFile();
        validateUploadedFile(file);

        try {
            String tempFilePath = fileService.saveTempFile(file);
            PdfValidateSignService service = new PdfValidateSignService();

            List<Map<String, Object>> signaturesResults = service.validateSignature(tempFilePath);
        
            return responseService.createValidationResponse(signaturesResults);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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

