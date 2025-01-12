package com.sign.controller;

import com.sign.dto.CertificateRequest;
import com.sign.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/certificate")
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
    
}
