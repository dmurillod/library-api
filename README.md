## 🏗️ Multicloud Architecture

```mermaid
flowchart TD
    Client(["👤 Client / Yostin MS\nPQR System - AWS ECS Fargate"])

    subgraph AWS ["☁️ Amazon Web Services"]
        subgraph YostinMS ["PQR MS — Yostin"]
            PQR["🔔 PQR API\nSpring Boot"]
            PQRDB[("🗄️ RDS PostgreSQL\nPQR DB")]
            CW["📊 CloudWatch\n+ Grafana Alloy\n+ Prometheus"]
            PQR --- PQRDB
            PQR --- CW
        end

        subgraph DiegoMS ["Library MS — Diego"]
            LIB["📚 Library API\nSpring Boot · EC2 t3.micro"]
            LIBDB[("🗄️ RDS PostgreSQL\nLibrary DB")]
            ECR["🐳 ECR\nDocker Registry"]
            MON["📈 Prometheus\n+ Grafana"]
            LIB --- LIBDB
            LIB --- MON
            ECR --> LIB
        end
    end

    subgraph GCP ["☁️ Google Cloud Platform"]
        subgraph DanielMS ["Receipts MS — Daniel"]
            REC["🧾 Receipts API\nGKE Kubernetes"]
            GEMINI["🤖 Gemini LLM\nPDF Generation"]
            WS["🔌 WebSocket\nReal-time notifications"]
            REC --> GEMINI
            REC --- WS
        end
    end

    subgraph CICD ["⚙️ CI/CD — GitHub Actions"]
        PIPE1["🔵 Staging Pipeline\nCoverage ≥ 60%"]
        PIPE2["🟢 Production Pipeline\nCoverage ≥ 85%"]
    end

    Client -->|"POST /api/v2/books/purchase\n{ titulo_libro, autor, pqr }"| LIB
    LIB -->|"Saves book\n{ libro }"| LIBDB
    LIB -->|"POST /v2/receipts/from-text\n{ text, uploader_nit }"| REC
    WS -->|"suggestion_completed\n{ receipt_id }"| LIB
    PIPE2 -->|"docker push"| ECR

    LIB -->|"Enriched response\n{ pqr, libro, receipt, pdf_url }"| Client
```

### Flow Description
1. **Yostin's MS** detects 5 PQRs for the same book → calls `POST /api/v2/books/purchase`
2. **Library API** saves the book in RDS → calls Daniel's MS
3. **Daniel's MS** generates accounting PDF with Gemini LLM → notifies via WebSocket
4. **Library API** returns enriched response with all 3 entities

### Final Response
```json
{
  "pqr":     { "id": "uuid", "asunto": "Clean Code", "responsable": "...", "conteo": 5 },
  "libro":   { "id": 1, "title": "Clean Code", "author": "Robert Martin", "isbn": "..." },
  "receipt": { "id": "uuid", "empresa": "Biblioteca Central", "valor": 85000, "pdf_url": "..." },
  "pdf_url": "http://34.60.178.4/v2/receipts/{id}/pdf"
}
```
