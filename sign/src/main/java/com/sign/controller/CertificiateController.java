package com.sign.controller;

import com.sign.dto.CertificateRequest;
import com.sign.service.CertificateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificados", description = "Gerenciamento de certificados digitais")
public class CertificiateController {

    @Autowired
    private CertificateService certificateService;

    @PostMapping("/generate-self-signed")
    @Operation(summary = "Gerar certificado autoassinado", description = "Cria e armazena um certificado autoassinado com base no ID e CN fornecidos.")
    public String generateCertificate(@RequestBody CertificateRequest request) {
        certificateService.createAndStoreCertificate(request.getId(), request.getCn());
        return "Self Signed Digital Certificate generated successfully";
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Excluir todos os certificados", description = "Remove todos os certificados do keystore.")
    public String deleteAllCertificates() {
        try {
            certificateService.deleteAllCertificates();
            return "All certificates have been deleted from the keystore.";
        } catch (Exception e) {
            return "Failed to delete certificates: " + e.getMessage();
        }
    }

    @GetMapping("/certificate/{id}")
    @Operation(summary = "Obter certificado por ID", description = "Retorna o certificado correspondente ao ID fornecido.")
    public ResponseEntity<String> getCertificateById(@PathVariable String id) {
        try {
            String certificate = certificateService.getCertificateByAlias(id);
            return ResponseEntity.ok(certificate);
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Failed to find certificate: " + e.getMessage());
        }
    }

    @PostMapping("/issue-certificate")
    @Operation(summary = "Emitir e assinar um certificado", description = "Cria um CSR (Certificate Signing Request) e envia para assinatura.")
    public ResponseEntity<String> issueAndSignCertificate(@RequestBody CertificateRequest request) throws Exception {
        String id = request.getId();
        String cn = request.getCn();
        try {
            String csrContent = certificateService.createCsr(id, cn);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("csr", csrContent);
            body.add("commonName", cn);
    
            return certificateService.pkiSignCertificate(body, id);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.internalServerError().body("Error to issue certificate: " + e.getMessage());
        }
        
    }
}
