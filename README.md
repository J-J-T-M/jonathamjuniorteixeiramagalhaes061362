# 🎵 Gestão de Artistas API - Desafio Técnico SEPLAG/MT 2026

![Java 21](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-brightgreen?style=for-the-badge&logo=spring)
![Docker](https://img.shields.io/badge/Docker-Available-blue?style=for-the-badge&logo=docker)
![Security](https://img.shields.io/badge/Spring_Security-JWT-red?style=for-the-badge&logo=springsecurity)

> **Candidato:** Jonatham Junior
> **Vaga:** Engenheiro da Computação - Sênior
> **Edital:** Nº 29.150 (SEPLAG/MT)

---

## 📖 Sobre o Projeto

Este projeto consiste em uma API RESTful de alta performance para o gerenciamento de artistas, discografias e integração com serviços governamentais de regionais. A solução foi arquitetada focando em **Escalabilidade**, **Resiliência** e **Segurança**, estritamente alinhada aos requisitos não funcionais de sistemas corporativos modernos.

A aplicação não é apenas um CRUD; ela implementa padrões de design robustos para resolver problemas de concorrência, latência e integridade de dados históricos.

---

## 🏛️ Arquitetura e Decisões de Design (Sênior)

### 1. Camadas e Separação de Responsabilidades (Clean Architecture)
A estrutura segue o princípio de **Separation of Concerns (SoC)**:
* **Controller Layer (`web`):** Estritamente acoplada ao protocolo HTTP. Realiza apenas validação de entrada (`Bean Validation`) e conversão de DTOs. Não contém regras de negócio.
* **Service Layer (`business`):** Coração da aplicação. Gerencia transações (`@Transactional`), orquestra chamadas a repositórios e aplica as regras de negócio.
* **Persistence Layer (`data`):** Abstração via Spring Data JPA.
* **Integration Layer:** Serviços isolados para comunicação externa (API de Regionais) e Storage (MinIO).

### 2. Rate Limiting Híbrido (Segurança Avançada)
Implementação customizada (`RateLimitFilter`) utilizando o algoritmo **Token Bucket** (via biblioteca Bucket4j).
* **Estratégia:** O filtro aplica limites dinâmicos baseados na identidade do requisitante.
    * **Usuário Autenticado:** Limite atrelado ao *Username* (extraído do JWT). Permite maior throughput.
    * **Anônimo:** Fallback de segurança que aplica o limite baseado no endereço **IP**.
* **Justificativa:** Protege a infraestrutura contra ataques de Negação de Serviço (DDoS) e Brute Force, garantindo QoS (Quality of Service) para usuários legítimos.

### 3. Histórico de Dados e SCD Type 2
Para a integração com a API de Regionais do Estado:
* **Desafio:** Os nomes das regionais externas podem mudar, mas os relatórios antigos precisam manter a integridade histórica.
* **Solução:** Implementação de **Slowly Changing Dimension (SCD) Tipo 2**.
    * Ao detectar uma alteração na API externa, o registro local antigo é marcado como `active=false`.
    * Um novo registro é criado com os dados atualizados (`active=true`).
    * Isso garante auditoria completa e consistência temporal.

### 4. Gestão de Mídia (Object Storage)
Upload de capas de álbuns desacoplado do banco de dados relacional.
* **Storage:** Utilização do **MinIO** (compatível com AWS S3).
* **Segurança:** Imagens não são servidas publicamente de forma direta. A API gera **Presigned URLs** temporárias, garantindo controle de acesso aos ativos digitais.

### 5. Notificações em Tempo Real (Event-Driven)
Utilização de **WebSockets (STOMP)** para notificar clientes conectados sobre a criação de novos álbuns, eliminando a necessidade de *polling* constante pelo frontend.

---

## 🛠️ Stack Tecnológica

* **Core:** Java 21 (LTS), Spring Boot 3.5
* **Dados:** PostgreSQL 16, Flyway (Migration)
* **Storage:** MinIO
* **Segurança:** Spring Security 6, JWT (JJWT), Bucket4j
* **Documentação:** OpenAPI 3.0 (Swagger UI)
* **Testes:** JUnit 5, Mockito, Testcontainers
* **Observabilidade:** Spring Actuator

---

## 🚀 Como Executar

### Pré-requisitos
* Docker & Docker Compose

### Passo a Passo

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/jonathamjtm/gestao-artistas-api.git](https://github.com/jonathamjtm/gestao-artistas-api.git)
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

## 🔌 Documentação dos Endpoints Reais

Todos os endpoints (exceto Auth) exigem o cabeçalho: `Authorization: Bearer <token>`

### 🔐 Autenticação (`/api/v1/auth`)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **POST** | `/login` | Autentica usuário e retorna JWT + Refresh Token. |
| **POST** | `/register` | Registra novo usuário no sistema. |
| **POST** | `/refresh` | Renova o Access Token expirado. |

### 🎤 Artistas (`/api/v1/artists`)
*Gerencia cantores (`SINGER`) e bandas (`BAND`).*

| Método | Endpoint | Params/Body | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | `?name=X`, `?createdAfter=Date`, `?sortDirection=ASC` | Listagem com filtros dinâmicos e ordenação. |
| **GET** | `/{id}` | - | Detalhes de um artista específico. |
| **POST** | `/` | `{ "name": "...", "type": "BAND" }` | Cria novo artista. |
| **PUT** | `/{id}` | `{ "name": "...", "type": "..." }` | Atualiza dados cadastrais. |
| **DELETE**| `/{id}` | - | Remove o artista (Logicamente ou Fisicamente). |

### 💿 Álbuns (`/api/v1/albums`)
*Gerencia discografia e vinculação N:N com artistas.*

| Método | Endpoint | Params/Body | Descrição |
| :--- | :--- | :--- | :--- |
| **GET** | `/` | `?title=X`, `?artistId=1`, `?releaseYear=2020` | Busca paginada (`Pageable`) de álbuns. |
| **GET** | `/{id}` | - | Busca álbum por ID. |
| **POST** | `/` | `{ "title": "...", "releaseYear": 2024, "artistIds": [1, 2] }` | Cria álbum e vincula a artistas. **Dispara WebSocket.** |
| **PUT** | `/{id}` | `{ "title": "...", ... }` | Atualiza metadados do álbum. |
| **DELETE**| `/{id}` | - | Deleta o álbum. |

#### 🖼️ Capas de Álbuns (Mídia)
| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **GET** | `/{id}/cover` | Retorna JSON com **URL assinada** (temporária) para download seguro da imagem. |
| **POST** | `/{id}/cover` | Upload `multipart/form-data` da capa (salva no MinIO). |

### 🌍 Regionais (`/api/v1/regionais`)
*Integração governamental com versionamento histórico.*

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| **GET** | `/` | Lista regionais sincronizadas. Filtro opcional: `?active=true`. |
| **POST** | `/sync` | **[Async]** Força o disparo do job de sincronização com a API externa. |

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

1.  **Containerização:** O banco de dados PostgreSQL é levantado em container Docker para cada bateria de testes, garantindo ambiente limpo.
2.  **Cenários Cobertos:**
    * ✅ Ciclo de vida completo (CRUD) de Artistas e Álbuns.
    * ✅ Validação rigorosa de segurança (401/403) e Rate Limit (429).
    * ✅ Concorrência na sincronização de Regionais.
    * ✅ Upload de arquivos (Mock do serviço de Storage).

Para executar os testes:
```bash
./mvnw test
