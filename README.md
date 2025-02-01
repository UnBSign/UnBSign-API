# Codificação do JWT

```
# Parâmetros
SECRET_KEY = 'sua_chave_secreta'
ALGORITHM = 'HS256'
ACCESS_TOKEN_HOURS = 1  # Por exemplo, 1 hora

# Dados do Payload
payload = {
  "issuer": "webapp_test",
  "user_id": id,  # ID do usuário (utilizado para salvar e consultar o certificado na keystore)
  "username": username,  # Nome do usuário
  "exp": datetime.utcnow() + timedelta(hours=ACCESS_TOKEN_HOURS)  # Expiração
}

```

# Documentação dos Endpoints

## 1. Endpoint: Assinatura de PDF
### **POST** `/api/pdf/signature`

Este endpoint é responsável por realizar a assinatura digital de um arquivo PDF.

### **Requisição**
- **URL:** `/api/signature`
- **Método:** POST
- **Cabeçalhos:**
- `Authorization: Bearer <JWT_TOKEN>`
- `Content-Type: multipart/form-data`
- **Parâmetros do Corpo (Form-Data):**
- `file` (**obrigatório**): Arquivo PDF que será assinado.
- `posX`: Posição X onde a assinatura será inserida no PDF.
- `posY`: Posição Y onde a assinatura será inserida no PDF.
- `pageNumber`: Número da página onde a assinatura será inserida.

### **Exemplo de Requisição**
```bash
curl -X POST "http://localhost:8080/api/signature" \
-H "Authorization: Bearer <JWT_TOKEN>" \
-H "Content-Type: multipart/form-data" \
-F "file=@/caminho/para/o/arquivo.pdf" \
-F "posX=100.0" \
-F "posY=200.0" \
-F "pageNumber=1"
```

### **Respostas**
- **Sucesso (200):**
```json
{
  "message": "File signed successfully",
  "filePath": "/caminho/para/o/arquivo-assinado.pdf"
}
```
- **Erro (400):** Certificado do usuário não encontrado ou arquivo inválido:
```json
{
  "message": "Certificado do usuário não encontrado"
}
```
- **Erro (500):** Problema interno no servidor:
```json
{
  "message": "Error to sign file: <mensagem_de_erro>"
}
```

---

## 2. Endpoint: Validação de Assinatura de PDF
### **POST** `/api/pdf/validation`

Este endpoint é responsável por validar as assinaturas digitais presentes em um arquivo PDF.

### **Requisição**
- **URL:** `/api/validation`
- **Método:** POST
- **Cabeçalhos:**
- `Content-Type: multipart/form-data`
- **Parâmetros do Corpo (Form-Data):**
- `file` (**obrigatório**): Arquivo PDF que será validado.

### **Exemplo de Requisição**
```bash
curl -X POST "http://localhost:8080/api/validation" \
-H "Content-Type: multipart/form-data" \
-F "file=@/caminho/para/o/arquivo-assinado.pdf"
```

### **Respostas**
- **Sucesso (200):**
```json
  {
  "DocumentHash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "Signatures": [
    {
      "SignatureID": "12345",
      "CN": "John Doe",
      "SerialNumber": "1234567890abcdef",
      "SigningDate": "2025-02-01T10:00:00Z",
      "Integrity": true,
      "PKIRecognized": true
    },
    {
      "SignatureID": "67890",
      "CN": "Jane Doe",
      "SerialNumber": "abcdef1234567890",
      "SigningDate": "2025-02-01T11:00:00Z",
      "Integrity": false,
      "PKIRecognized": false
    }
  ],
  "AllSignaturesRecognized": false
}
```
- **Erro (400):** Arquivo inválido:
```json
{
  "message": "Invalid file format"
}
```
- **Erro (500):** Problema interno no servidor:
```json
{
  "message": "Error validating file: <mensagem_de_erro>"
}
```

## 3. Endpoint: Geração de Certificado Digital Autoassinado
### **POST** `/api/certificates/generateSelfSigned`

### **Requisição**
- **URL:** `/api/certificates/generateSelfSigned`
- **Método:** POST
- **Cabeçalhos:**
- `Content-Type: application/json`
- **Parâmetros do Corpo (Form-Data):**
- `id` (**obrigatório**): Identificador único do certificado.
- `cn` (**obrigatório**): Nome comum (Common Name) do certificado.

### **Exemplo de Requisição**
```bash
curl -X POST "http://localhost:8080/api/certificates/generateSelfSigned" \
-H "Content-Type: application/json" \
-d '{"id": "user123", "cn": "John Doe"}'

```

### **Respostas**
- **Sucesso (200):**
```json
  {
"message": "Self Signed Digital Certificate generated successfully"
}
```

- **Erro (500):** Problema interno no servidor:
```json
{
"message": "Error generating certificate: <mensagem_de_erro>"
}

```

## 4. Endpoint: Emissão e Assinatura de Certificado
### **POST** `/api/certificates/issue-certificate`

### **Requisição**
- **URL:** `/api/certificates/issue-certificate`
- **Método:** POST
- **Cabeçalhos:**
- `Content-Type: application/json`
- **Parâmetros do Corpo (Form-Data):**
- `id` (**obrigatório**): Identificador único do certificado.
- `cn` (**obrigatório**): Nome comum (Common Name) do certificado.

### **Exemplo de Requisição**
```bash
curl -X POST "http://localhost:8080/api/certificates/generateSelfSigned" \
-H "Content-Type: application/json" \
-d '{"id": "user123", "cn": "John Doe"}'

```

### **Respostas**
- **Sucesso (200):**
```json
  {
"message": "Certificate issued and signed successfully"
}

```

- **Erro (500):** Problema interno no servidor:
```json
{
"message": "Error issuing certificate: <mensagem_de_erro>"
}
```


