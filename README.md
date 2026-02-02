# 🎵 Gestão de Artistas API - Desafio Técnico SEPLAG 2026

API RESTful desenvolvida para o gerenciamento de artistas, álbuns e discografias, com foco em arquitetura escalável, segurança robusta e integridade de dados. Este projeto foi construído atendendo aos requisitos do processo seletivo para Desenvolvedor Sênior.

---

## 🚀 Tecnologias Utilizadas

* **Linguagem:** Java 21 (LTS)
* **Framework:** Spring Boot 3.5
* **Banco de Dados:** PostgreSQL 16
* **Versionamento de Banco:** Flyway Migration
* **Object Storage:** MinIO (Compatível com AWS S3)
* **Segurança:** Spring Security, JWT (HMAC SHA256), Refresh Token
* **Proteção:** Bucket4j (Rate Limiting), CORS (Allowlist estrita)
* **Real-time:** WebSocket (STOMP)
* **Testes:** JUnit 5, Mockito, Testcontainers, Awaitility
* **Containerização:** Docker & Docker Compose

---

## 🏛️ Decisões de Arquitetura e Design

### 1. Histórico de Regionais (SCD Tipo 2)

Para atender ao requisito de negócio que exige **preservar o histórico** caso o nome de uma Regional mude na API externa, optou-se pela estratégia de **Slowly Changing Dimension (SCD) Type 2**.

**O problema**
A API externa é a *fonte da verdade*. Se a regional `101` muda de `Cuiabá` para `Cuiabá - Centro`, um simples `UPDATE` destruiria a informação histórica de que, no passado, álbuns estavam vinculados à regional `Cuiabá`.

**A solução**

1. A tabela `regionals` foi estruturada desacoplando a **chave primária interna (`id`)** da **chave de negócio (`external_id`)**.
2. Foi adicionado o campo booleano `active` para controle de versão.

**Fluxo de sincronização**

* Ao detectar mudança de nome para o mesmo `external_id`:

  * O registro antigo é marcado como `active = false`.
  * Um novo registro é inserido com o novo nome e `active = true`.

Essa abordagem garante integridade referencial, rastreabilidade histórica e suporte a auditorias e relatórios retroativos.

---

### 2. Armazenamento de Imagens (Strategy Pattern + MinIO)

Para evitar o antipadrão de armazenar binários (BLOBs) no banco de dados, foi aplicada a estratégia de **abstração por interface**.

* Interface: `StorageService`
* Implementação padrão: **MinIO** (compatível com AWS S3)

**Benefícios**

* Redução de carga no banco de dados
* Melhor escalabilidade
* Facilidade de migração para AWS S3 ou Google Cloud Storage apenas trocando configurações

---

### 3. Segurança em Profundidade (Defense in Depth)

* **Autenticação Stateless** com Access Token (curta duração) e Refresh Token (7 dias)
* **Rate Limiting** via Bucket4j (10 requisições/minuto por IP, configurável)
* **CORS** com política restritiva baseada em Allowlist

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
