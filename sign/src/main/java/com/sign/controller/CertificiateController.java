package com.sign.controller;

import com.sign.dto.CertificateRequest;
import com.sign.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;



@RestController
@RequestMapping("/api/certificates")
public class CertificiateController {

    @Autowired
    private CertificateService certificateService;

    private static final String REMOTE_SIGNATURE_URL = "http://localhost:8081/api/pki/certificates/signature";
    
    @PostMapping("/generateSelfSigned")
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
            String certificate = certificateService.getCertificateByAlias(id + "_cert");
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
        // return ResponseEntity.ok(csrContent);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("csr", csrContent);  // CSR em formato de string
        body.add("commonName", cn);   // O Common Name

        // Envio do corpo da solicitação
        RestTemplate restTemplate = new RestTemplate();
        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
            .post(REMOTE_SIGNATURE_URL)
            .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
            .body(body);
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                String result = certificateService.processAndStore(id, response.getBody());
                return ResponseEntity.ok(result);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error storing the signed certificate: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to sign certificate. Response: " + response.getBody());
        }
    }
    
}
