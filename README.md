# 🎵 Gestão de Artistas API - Desafio Técnico SEPLAG 2026

API RESTful desenvolvida para o gerenciamento de artistas, álbuns e discografias, com foco em arquitetura escalável, segurança robusta e integridade de dados. Este projeto foi construído atendendo aos requisitos do processo seletivo para Desenvolvedor Sênior.

---

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21 (LTS) - Utilizando novos recursos como Records e Pattern Matching.
* **Framework:** Spring Boot 3.5 - Ecossistema robusto para desenvolvimento ágil.
* **Banco de Dados:** PostgreSQL 16 - Relacional, robusto e escalável.
* **Versionamento de Banco:** Flyway Migration - Controle de versão do schema do banco.
* **Object Storage:** MinIO - Armazenamento de objetos compatível com AWS S3.
* **Segurança:** Spring Security 6 + JWT (Stateless Authentication).
* **Proteção:** Bucket4j (Rate Limiting) e CORS Configurado.
* **Real-time:** WebSocket (STOMP) para notificações push.
* **Testes:** JUnit 5, Mockito, Testcontainers e Awaitility.
* **Infraestrutura:** Docker & Docker Compose.

---

## 🏛️ Arquitetura e Decisões de Design

A arquitetura do projeto segue o padrão de **Camadas (Layered Architecture)**, promovendo a separação de responsabilidades (Separation of Concerns) e facilitando a manutenção e testabilidade.

### 1. Estrutura de Camadas
* **Controller Layer (`web`):** Responsável apenas por receber as requisições HTTP, validar os dados de entrada (Bean Validation) e converter DTOs. Não contém regras de negócio.
* **Service Layer (`business`):** O coração da aplicação. Encapsula toda a lógica de negócio, regras de validação complexas e controle transacional (`@Transactional`). Garante a consistência dos dados antes de persistir.
* **Repository Layer (`data-access`):** Abstração do acesso a dados utilizando **Spring Data JPA**. Permite a troca fácil da implementação de persistência e facilita a criação de Mocks para testes unitários.
* **Domain Layer (`entities`):** Representa os objetos persistentes do banco de dados (ORM).
* **DTO Layer (Data Transfer Objects):** Padrão utilizado para desacoplar a API pública do modelo de dados interno. Isso evita o vazamento de dados sensíveis (ex: senhas) e permite evoluir o banco de dados sem quebrar contratos de API existentes (Versioning).

### 2. Histórico de Regionais (SCD Tipo 2)
Para atender ao requisito de negócio que exige **preservar o histórico** caso o nome de uma Regional mude na API externa, optou-se pela estratégia de **Slowly Changing Dimension (SCD) Type 2**.
* **Problema:** A API externa é a "fonte da verdade", mas seus dados são mutáveis. Um `UPDATE` simples perderia o rastro histórico de vinculações passadas.
* **Solução:** A tabela `regionals` foi modelada com uma chave sub-rogada (`id`) distinta da chave de negócio (`external_id`) e um flag `active`.
* **Fluxo:** Ao detectar mudança de nome:
    1. O registro antigo é inativado (`active=false`).
    2. Um novo registro é criado (`active=true`).
    3. Isso garante integridade referencial histórica para relatórios e auditoria.

### 3. Strategy Pattern para Armazenamento
Utilizamos uma interface `StorageService` para abstrair o armazenamento de arquivos.
* **Implementação Atual:** `MinIOStorageService` (simulando S3).
* **Benefício:** Permite migrar para AWS S3, Azure Blob Storage ou Google Cloud Storage apenas alterando a injeção de dependência, sem tocar na lógica de negócio dos Controllers ou Services.

### 4. Cross-Cutting Concerns (Aspectos Transversais)
Funcionalidades que atravessam toda a aplicação foram implementadas via **Filtros e Configurações Globais**, garantindo que a regra de negócio não seja poluída.
* **Global Exception Handling:** Um `@RestControllerAdvice` captura exceções (como `ResourceNotFoundException` ou `BadCredentialsException`) e padroniza a resposta JSON com códigos HTTP corretos (404, 403, 400).
* **Rate Limiting:** Implementado via Filtro (`RateLimitFilter`) utilizando o algoritmo **Token Bucket**. Protege a API contra ataques de Força Bruta e Negação de Serviço (DoS), limitando requisições por IP.
* **Auditoria de Segurança:** O Spring Security intercepta todas as requisições para validar tokens JWT antes que elas cheguem aos Controllers.
---

## 🛠️ Como Rodar o Projeto

### Pré-requisitos

* Docker
* Docker Compose
* (Opcional) Java 21 e Maven

---

### 🚀 Execução Rápida (Recomendado)

1. Clone o repositório:

```bash
git clone <seu-repo>
cd gestao-artistas-api
```

2. Suba os containers:

```bash
docker-compose up -d --build
```

3. Acesse a documentação Swagger:

* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

4. Console do MinIO:

* URL: [http://localhost:9001](http://localhost:9001)
* Usuário: `minioadmin`
* Senha: `minioadmin`

---

## 📚 Documentação dos Endpoints

### 🔐 Autenticação (`/api/v1/auth`)

#### 1. Registrar Usuário

**POST** `/register`

Request:

```json
{
  "fullName": "Admin User",
  "email": "admin@seplag.mt.gov.br",
  "password": "SenhaForte123!"
}
```

Response **200 OK**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ...",
  "type": "Bearer"
}
```

---

#### 2. Login

**POST** `/login`

Request:

```json
{
  "email": "admin@seplag.mt.gov.br",
  "password": "SenhaForte123!"
}
```

Response **200 OK**: Access Token + Refresh Token

---

#### 3. Renovar Token

**POST** `/refresh`

Request:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ..."
}
```

Response **200 OK**: Novo Access Token

---

## 🎤 Artistas (`/api/v1/artists`)

### 1. Criar Artista

**POST** `/`

Header:

```
Authorization: Bearer <token>
```

Request:

```json
{
  "name": "Legião Urbana"
}
```

Response **201 Created**:

```json
{
  "id": 1,
  "name": "Legião Urbana"
}
```

---

### 2. Listar Artistas (Paginado e Filtrado)

**GET** `/?page=0&size=10&name=Legiao`

Response **200 OK**:

```json
{
  "content": [
    { "id": 1, "name": "Legião Urbana" }
  ],
  "totalElements": 1
}
```

---

### 3. Exportar Relatório Excel

**GET** `/export/excel`

Response **200 OK**: Download de arquivo `.xlsx`

---

## 💿 Álbuns (`/api/v1/albums`)

### 1. Criar Álbum

**POST** `/`

Request:

```json
{
  "title": "Dois",
  "releaseYear": 1986,
  "artistIds": [1]
}
```

Response **201 Created**:

```json
{
  "id": 10,
  "title": "Dois",
  "releaseYear": 1986,
  "artists": [
    { "id": 1, "name": "Legião Urbana" }
  ]
}
```

🔔 Evento WebSocket enviado para `/topic/albums`

---

### 2. Listar Álbuns

**GET** `/?title=Dois&releaseYear=1986`

Response **200 OK**: Lista paginada

---

### 3. Buscar Álbum por ID

**GET** `/{id}`

* **200 OK**: Álbum encontrado
* **404 Not Found**: ID inexistente

---

### 4. Upload da Capa

**POST** `/{id}/cover`

Content-Type: `multipart/form-data`

Response **200 OK**:

```
Upload realizado com sucesso
```

---

### 5. Download da Capa

**GET** `/{id}/cover`

Response **200 OK**: `image/jpeg` ou `image/png`

---

### 6. Deletar Álbum

**DELETE** `/{id}`

Response **204 No Content**

---

## 🌍 Regionais (`/api/v1/regionais`)

### 1. Listar Regionais Ativas

**GET** `/`

Response **200 OK**:

```json
[
  { "id": 5, "externalId": 101, "name": "Cuiabá", "active": true },
  { "id": 6, "externalId": 102, "name": "Várzea Grande", "active": true }
]
```

---

### 2. Forçar Sincronização

**POST** `/sync`

Dispara manualmente a sincronização com a API externa.

Response **200 OK**:

```
Sincronização iniciada com sucesso
```

---

## 🔔 WebSocket (Real-time)

* Endpoint: `/ws-gestao-artistas`
* Tópico: `/topic/albums`
* Evento disparado ao criar novos álbuns

---

## 🧪 Testes

Para executar:

```bash
./mvnw test
```

**Destaques**

* `AuthControllerTest`: autenticação, refresh token e rate limit (429)
* `RegionalSyncServiceTest`: versionamento SCD Tipo 2
* `AlbumControllerTest`: CRUD completo e exceções

---

## 👨‍💻 Autor

**Jonatham Junior**
Projeto desenvolvido para o **Desafio Técnico SEPLAG – Perfil Sênior**
