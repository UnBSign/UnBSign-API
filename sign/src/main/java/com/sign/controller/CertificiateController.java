package com.sign.controller;

import com.sign.dto.CertificateRequest;
import com.sign.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/certificates")
public class CertificiateController {

    @Autowired
    private CertificateService certificateService;
    
    @PostMapping("/generate")
    public String generateCertificate(@RequestBody CertificateRequest request) {

        certificateService.createAndStoreCertificate(request.getId(), request.getCn());

        return "Digital Certificate generated successfully";
    }

    @DeleteMapping("/deleteAll")
    public String deleteAllCertificates(){
        try {
            certificateService.deleteAllCertificates();
            return "All certificates have been deleted from the keystore.";
        } catch (Exception e) {
            return "Failed to delete certificates: " + e.getMessage();
        }
    }
    
    @GetMapping("/certificate/{id}")
    public ResponseEntity<String> getCertificateById(@PathVariable String id) {
        try {
            // Recuperando o certificado pelo alias gerado a partir do id
            String certificate = certificateService.getCertificateByAlias(id + "_cert");
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            // Retorna a mensagem de erro com o código de status 404 caso o certificado não seja encontrado
            return ResponseEntity.status(404).body("Failed to find certificate: " + e.getMessage());
        }
    }
    
}
