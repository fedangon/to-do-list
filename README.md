# Todo List API

API REST de gerenciamento de tarefas desenvolvida em Spring Boot com PostgreSQL. Projeto de portfólio construído em etapas para demonstrar evolução técnica — desde o CRUD básico até containerização com Docker.

## Stack

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 17 | Linguagem |
| Spring Boot | 3.2.5 | Framework principal |
| Spring Data JPA | — | Persistência e Specifications |
| PostgreSQL | 15 | Banco de dados (produção) |
| H2 | — | Banco em memória (testes) |
| MapStruct | 1.5.5 | Mapeamento DTO ↔ Entidade |
| Lombok | — | Redução de boilerplate |
| Log4j2 | — | Logging estruturado |
| JUnit 5 + Mockito | — | Testes unitários |
| MockMvc | — | Testes de integração |
| Docker + Compose | — | Containerização |

## Funcionalidades

- CRUD completo de tarefas e categorias
- Relacionamento ManyToMany entre tarefas e categorias
- Filtros combinados por status, prioridade e categoria
- Paginação e ordenação configuráveis
- Endpoint de estatísticas (total, concluídas, pendentes, por prioridade)
- Validação de entrada com respostas de erro padronizadas em JSON
- Logging detalhado com arquivos rotativos (diário + 10MB)

## Estrutura do projeto

```
src/
├── main/
│   ├── java/com/portfolio/todo/
│   │   ├── controller/       # Endpoints REST
│   │   ├── service/          # Regras de negócio
│   │   ├── repository/       # Acesso ao banco (JPA + Specifications)
│   │   ├── model/            # Entidades JPA
│   │   ├── dto/              # Request e Response DTOs
│   │   ├── mapper/           # MapStruct (DTO ↔ Entidade)
│   │   └── exception/        # Tratamento global de erros
│   └── resources/
│       ├── application.properties
│       └── log4j2.xml
└── test/
    ├── java/com/portfolio/todo/
    │   ├── controller/       # Testes de integração (MockMvc + H2)
    │   └── service/          # Testes unitários (Mockito)
    └── resources/
        └── application.properties
```

## Como rodar

### Pré-requisitos

- Java 17+
- Maven 3.8+
- Docker e Docker Compose (para rodar via container)

---

### Opção 1 — Docker (recomendado)

Sobe a aplicação e o PostgreSQL juntos, sem precisar instalar nada localmente além do Docker:

```bash
docker compose up --build
```

A API ficará disponível em `http://localhost:8080`.

Para parar:
```bash
docker compose down
```

Para parar e remover os dados do banco:
```bash
docker compose down -v
```

---

### Opção 2 — Local com Maven

Requer PostgreSQL rodando localmente. Crie o banco e o usuário:

```sql
CREATE DATABASE todo_db;
CREATE USER admin WITH PASSWORD 'admin123';
GRANT ALL PRIVILEGES ON DATABASE todo_db TO admin;
```

Execute a aplicação:

```bash
mvn spring-boot:run
```

---

### Rodar os testes

```bash
mvn test
```

Os testes usam H2 em memória — não precisam de PostgreSQL.

---

## Endpoints

### Tarefas

#### Listar tarefas
```
GET /api/tasks
```

Parâmetros de query (todos opcionais):

| Parâmetro | Tipo | Descrição | Padrão |
|---|---|---|---|
| `completed` | boolean | Filtrar por status | — |
| `priority` | string | `LOW`, `MEDIUM` ou `HIGH` | — |
| `categoryId` | long | ID da categoria | — |
| `page` | int | Número da página | `0` |
| `size` | int | Itens por página | `10` |
| `sortBy` | string | Campo de ordenação | `createdAt` |
| `direction` | string | `asc` ou `desc` | `desc` |

```bash
# Todas as tarefas
curl http://localhost:8080/api/tasks

# Tarefas pendentes com prioridade alta
curl "http://localhost:8080/api/tasks?completed=false&priority=HIGH"

# Segunda página com 5 itens por página
curl "http://localhost:8080/api/tasks?page=1&size=5"
```

**Resposta:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Estudar Spring Boot",
      "description": "Revisar JPA e Hibernate",
      "completed": false,
      "priority": "HIGH",
      "categories": [
        { "id": 1, "name": "Estudos" }
      ],
      "createdAt": "2024-01-15T10:00:00",
      "updatedAt": "2024-01-15T10:00:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}
```

---

#### Estatísticas
```
GET /api/tasks/stats
```

```bash
curl http://localhost:8080/api/tasks/stats
```

**Resposta:**
```json
{
  "total": 10,
  "completed": 4,
  "pending": 6,
  "byPriority": {
    "LOW": 2,
    "MEDIUM": 5,
    "HIGH": 3
  }
}
```

---

#### Buscar por ID
```
GET /api/tasks/{id}
```

```bash
curl http://localhost:8080/api/tasks/1
```

---

#### Criar tarefa
```
POST /api/tasks
```

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Estudar Spring Boot",
    "description": "Revisar JPA e Hibernate",
    "priority": "HIGH",
    "categoryIds": [1]
  }'
```

**Campos obrigatórios:** `title`, `priority`

**Resposta:** `201 Created`

---

#### Atualizar tarefa
```
PUT /api/tasks/{id}
```

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Estudar Spring Boot",
    "priority": "HIGH",
    "completed": true
  }'
```

**Resposta:** `200 OK`

---

#### Deletar tarefa
```
DELETE /api/tasks/{id}
```

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**Resposta:** `204 No Content`

---

### Categorias

#### Listar categorias
```
GET /api/categories
```

```bash
curl http://localhost:8080/api/categories
```

**Resposta:**
```json
[
  { "id": 1, "name": "Trabalho" },
  { "id": 2, "name": "Estudos" }
]
```

---

#### Buscar por ID
```
GET /api/categories/{id}
```

```bash
curl http://localhost:8080/api/categories/1
```

---

#### Criar categoria
```
POST /api/categories
```

```bash
curl -X POST http://localhost:8080/api/categories \
  -H "Content-Type: application/json" \
  -d '{ "name": "Trabalho" }'
```

**Resposta:** `201 Created`

---

#### Atualizar categoria
```
PUT /api/categories/{id}
```

```bash
curl -X PUT http://localhost:8080/api/categories/1 \
  -H "Content-Type: application/json" \
  -d '{ "name": "Trabalho Remoto" }'
```

---

#### Deletar categoria
```
DELETE /api/categories/{id}
```

```bash
curl -X DELETE http://localhost:8080/api/categories/1
```

**Resposta:** `204 No Content`

---

## Respostas de erro

Todos os erros retornam JSON no mesmo formato:

```json
{
  "status": 404,
  "message": "Task not found with id: 99",
  "errors": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

| Código | Situação |
|---|---|
| `400` | Campos obrigatórios ausentes ou inválidos |
| `404` | Recurso não encontrado |
| `409` | Conflito (ex: categoria com nome duplicado) |
| `500` | Erro interno inesperado |

## Logs

A aplicação gera logs em dois arquivos no diretório `logs/`:

- `todo-api.log` — todos os níveis (rotação diária e a cada 10MB)
- `todo-api-errors.log` — apenas WARN e ERROR
