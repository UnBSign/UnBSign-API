package com.sign.controller;

import com.sign.dto.CertificateRequest;
import com.sign.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/certificates")
public class CertificiateController {

    @Autowired
    private CertificateService certificateService;
    
    @PostMapping("/generate-self-signed")
    public String generateCertificate(@RequestBody CertificateRequest request) {

        certificateService.createAndStoreCertificate(request.getId(), request.getCn());

        return "Self Signed Digital Certificate generated successfully";
    }

    @DeleteMapping("/delete-all")
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
            String certificate = certificateService.getCertificateByAlias(id);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Failed to find certificate: " + e.getMessage());
        }
    }

    @PostMapping("/issue-certificate")
    public ResponseEntity<String> issueAndSignCertificate(@RequestBody CertificateRequest request) throws Exception{

        String id = request.getId();
        String cn = request.getCn();
        
        String csrContent = certificateService.createCsr(id, cn);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("csr", csrContent);
        body.add("commonName", cn);

        return certificateService.pkiSignCertificate(body, id);   
    }
    
}
