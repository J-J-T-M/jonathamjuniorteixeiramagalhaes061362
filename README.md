# 🎵 Gestão de Artistas API - Desafio Técnico SEPLAG/MT 2026

![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-brightgreen?style=for-the-badge&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Available-blue?style=for-the-badge&logo=docker)
![Security](https://img.shields.io/badge/Spring_Security-JWT-red?style=for-the-badge&logo=springsecurity)

> **Candidato:** Jonatham Junior Teixeira Magalhães
> > **Vaga:** Engenheiro da Computação - Sênior
> **Edital:** Nº 29.150 (SEPLAG/MT)

---

## 📖 Sobre o Projeto

Este projeto consiste em uma API RESTful de alta performance para o gerenciamento de artistas, discografias e integração com serviços governamentais de regionais. A solução foi arquitetada focando em **Escalabilidade**, **Resiliência** e **Segurança**, estritamente alinhada aos requisitos não funcionais de sistemas corporativos modernos.

A aplicação não é apenas um CRUD; ela implementa padrões de design robustos para resolver problemas de concorrência, latência e integridade de dados históricos.

---

## 🏛️ Arquitetura e Decisões de Design (Sênior)

### 1. Camadas e Separação de Responsabilidades (Clean Architecture)
A estrutura segue o princípio de **Separation of Concerns (SoC)**:
* **Web Layer (`controller`):** Realiza validação de entrada (`Bean Validation`) e conversão de DTOs.
* **Business Layer (`service`):** Orquestra transações (`@Transactional`) e aplica as regras de negócio.
* **Data Layer (`repository`):** Abstração via Spring Data JPA com suporte a Specifications para filtros dinâmicos.
* **Entity Layer:** Uso de `entities` para mapeamento objeto-relacional fiel ao banco de dados PostgreSQL.

### 2. Rate Limiting Híbrido (Segurança Avançada)
Implementação customizada via `RateLimitFilter` utilizando o algoritmo **Token Bucket** (via Bucket4j).
* **Estratégia:** Limites dinâmicos de 10 requisições por minuto.
    * **Usuário Autenticado:** Limite atrelado ao *Username* extraído do JWT.
    * **Anônimo:** Fallback baseado no endereço **IP**.

### 3. Histórico de Dados e SCD Type 2
Para a integração com a API de Regionais:
* **Solução:** Implementação de **Slowly Changing Dimension (SCD) Tipo 2**.
    * Ao detectar alteração na API externa, o registro local antigo é marcado como `active=false`.
    * Um novo registro é criado com os dados atualizados (`active=true`), garantindo integridade histórica e auditoria.

### 4. Gestão de Mídia (Object Storage)
Upload de capas de álbuns desacoplado do banco de dados utilizando o **MinIO** (compatível com API S3).
* **Segurança:** Acesso aos ativos via **Presigned URLs** temporárias com expiração de 30 minutos.

### 5. Notificações em Tempo Real (Event-Driven)
Utilização de **WebSockets (STOMP)** para notificar o frontend a cada novo álbum cadastrado, conforme exigido no edital.

---

## 🛠️ Stack Tecnológica

* **Core:** Java 21 (LTS), Spring Boot 3.4.2
* **Dados:** PostgreSQL 16, Flyway (Migration)
* **Storage:** MinIO (API S3)
* **Segurança:** Spring Security 6, JWT (JJWT), Bucket4j
* **Documentação:** OpenAPI 3.1 (Swagger UI)
* **Testes:** JUnit 5, Mockito, Testcontainers
* **Observabilidade:** Spring Actuator (Health, Liveness/Readiness)

---

## 🚀 Como Executar

### Pré-requisitos
* Docker & Docker Compose

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone https://github.com/J-J-T-M/jonathamjuniorteixeiramagalhaes061362.git
    cd gestao-artistas-api
    ```

2.  **Suba o ambiente (App + Banco + MinIO):**
    ```bash
    docker-compose up -d --build
    ```

3.  **Acesse a Documentação Interativa:**
    * Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

4.  **Credenciais de Teste (MinIO):**
    * User: `minioadmin` | Password: `minioadmin`
    * Console: [http://localhost:9001](http://localhost:9001)

---

## 🔌 Documentação Detalhada dos Endpoints

Todos os endpoints (exceto Auth) exigem o cabeçalho: `Authorization: Bearer <token>`.
Abaixo seguem os exemplos de **Request Body** e **Response Body** para cada operação.

### 🔐 1. Authentication
Endpoints para controle de acesso.

#### **POST** `/api/v1/auth/register`
*Registrar um novo usuário.*

**Request Body (JSON):**
```json
{
  "fullName": "Jonatham Junior",
  "email": "jonatham@email.com",
  "password": "senha_segura_123"
}

```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "d29a-4b6c-8d1e...",
  "type": "Bearer"
}

```

---

#### **POST** `/api/v1/auth/login`

*Autenticar usuário existente.*

**Request Body:**

```json
{
  "email": "admin@email.com",
  "password": "123456"
}

```

**Response (200 OK):**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "d29a-4b6c-8d1e...",
  "type": "Bearer"
}

```

---

### 🎤 2. Artistas

Gestão de bandas e cantores.

#### **POST** `/api/v1/artists`

*Criar novo artista.*

**Request Body:**

```json
{
  "name": "Linkin Park",
  "type": "BAND"
}

```

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "Linkin Park",
  "type": "BAND",
  "albums": [],
  "createdAt": "2026-02-04T10:00:00Z"
}

```

#### **GET** `/api/v1/artists/{id}`

*Buscar detalhes do artista.*

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "Linkin Park",
  "type": "BAND",
  "albums": [
    "Hybrid Theory",
    "Meteora"
  ],
  "createdAt": "2026-02-04T10:00:00Z"
}

```

#### **PUT** `/api/v1/artists/{id}`

*Atualizar dados do artista.*

**Request Body:**

```json
{
  "name": "Linkin Park Updated",
  "type": "BAND"
}

```

**Response (200 OK):**

```json
{
  "id": 1,
  "name": "Linkin Park Updated",
  "type": "BAND",
  "albums": ["Hybrid Theory"],
  "createdAt": "2026-02-04T10:00:00Z"
}

```

---

### 💿 3. Álbuns

Gestão de discografia.

#### **POST** `/api/v1/albums`

*Cadastra álbum e vincula a um ou mais artistas. Dispara notificação WebSocket.*

**Request Body:**

```json
{
  "title": "Hybrid Theory",
  "releaseYear": 2000,
  "artistIds": [
    1
  ]
}

```

**Response (200 OK):**

```json
{
  "id": 9007199254740991,
  "title": "Hybrid Theory",
  "releaseYear": 2000,
  "artistIds": [
    1
  ]
}

```

#### **GET** `/api/v1/albums`

*Listagem paginada de álbuns.*
*Parâmetros opcionais de Query: `?page=0&size=10&sort=title,asc&artistId=1*`

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 10,
      "title": "Hybrid Theory",
      "releaseYear": 2000,
      "artistIds": [1]
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true, "unsorted": false, "empty": false }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}

```

---

### 🖼️ 4. Albums – Media

Upload e download de capas via MinIO (S3).

#### **POST** `/api/v1/albums/{id}/cover`

*Upload de imagem (Multipart).*

**Request (multipart/form-data):**

* `files`: [binary_image.jpg]

**Response (200 OK):**

```json
[
  "http://minio:9000/covers/album-1-cover.jpg"
]

```

#### **GET** `/api/v1/albums/{id}/cover`

*Obter URLs assinadas (temporárias) para download.*

**Response (200 OK):**

```json
[
  "http://localhost:9000/covers/album-1.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&..."
]

```

---

### 🌍 5. Regionais – Synchronization

Sincronização governamental.

#### **POST** `/api/v1/regionais/sync`

*Forçar sincronização com API Externa (Processamento Assíncrono).*

**Response (200 OK):**

```json
"Sincronização iniciada com sucesso. Verifique os logs para status."

```

#### **GET** `/api/v1/regionais`

*Listar regionais sincronizadas.*
*Parâmetros: `?active=true*`

**Response (200 OK):**

```json
{
  "content": [
    {
      "id": 55,
      "externalId": 1020,
      "name": "Regional Sul",
      "active": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}

```

---

## 📡 Integração em Tempo Real (WebSocket)

O sistema utiliza STOMP sobre WebSocket para notificações push.

* **URL de Conexão:** `ws://localhost:8080/ws-gestao-artistas`
* **Tópico de Assinatura:** `/topic/notifications` (Eventos gerais) e `/topic/albums` (Novos álbuns).

**Teste de Conexão via Backend:**
Envie uma mensagem STOMP para `/app/test-connection` e observe a resposta no tópico `/topic/notifications`.

---

## 🧪 Estratégia de Testes

O projeto garante a qualidade através de **Testes de Integração** robustos (`@SpringBootTest` + `Testcontainers`).

1. **Containerização:** O banco de dados PostgreSQL é levantado em container Docker para cada bateria de testes, garantindo ambiente limpo.
2. **Cenários Cobertos:**
* ✅ Ciclo de vida completo (CRUD) de Artistas e Álbuns.
* ✅ Validação rigorosa de segurança (401/403) e Rate Limit (429).
* ✅ Concorrência na sincronização de Regionais.
* ✅ Upload de arquivos (Mock do serviço de Storage).



Para executar os testes:

```bash
./mvnw test
```
