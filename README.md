# Runner Circle API — projeto de partida

Backend do **Runner Circle**, uma rede social para corredores e caminhantes. Este é o **projeto de partida** do curso "Spring Boot: construindo uma API profissional (Runner Circle)" — ele já traz o CRUD básico de `Treino` e `User` no mesmo nível técnico do curso anterior ("Spring Boot: construindo sua primeira API"): JPA, DTOs, validação com Bean Validation e documentação com Swagger/OpenAPI.

De propósito, tudo ainda mora num pacote só e o `TreinoController` faz tudo sozinho (sem Service, sem Mapper, sem relacionamento entre `Treino` e `User`). É esse ponto de partida que a Aula 1 do curso evolui para uma arquitetura em camadas.

## Stack

* Java 25
* Spring Boot 4.1.0
* Spring Data JPA + PostgreSQL
* Bean Validation
* springdoc-openapi (Swagger UI)
* Docker / Docker Compose

## Rodando localmente (sem Docker)

Pré-requisitos: Java 25 e um PostgreSQL rodando em `localhost:5432` com um banco `runnercircle` (usuário/senha `postgres`/`postgres`, ou ajuste via variáveis de ambiente — veja abaixo).

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Rodando com Docker Compose

Sobe a API e o PostgreSQL juntos:

```bash
docker compose up --build
```

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `runnercircle` | Nome do banco |
| `DB_USER` | `postgres` | Usuário do banco |
| `DB_PASSWORD` | `postgres` | Senha do banco |

## Swagger / OpenAPI

Com a aplicação rodando:

* Swagger UI: `http://localhost:8080/swagger-ui.html`
* Especificação OpenAPI: `http://localhost:8080/v3/api-docs`

## Endpoints disponíveis

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/treinos` | Lista os treinos (filtro opcional `?tipoTreino=CAMINHADA\|CORRIDA`) |
| `GET` | `/treinos/{id}` | Busca um treino pelo id |
| `POST` | `/treinos` | Cria um treino |
| `PUT` | `/treinos/{id}` | Atualiza um treino |
| `DELETE` | `/treinos/{id}` | Remove um treino |

`User` existe como entidade e repositório (`UserRepository`), mas ainda sem endpoint próprio — isso é construído ao longo do curso.
