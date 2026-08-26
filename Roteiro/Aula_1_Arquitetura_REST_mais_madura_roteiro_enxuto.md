# Aula 1 — Arquitetura REST mais madura

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**. A IA implementa, mas quem decide o que deve ser feito e valida o resultado é você.

---

## Pré-requisitos

- Projeto de partida do Runner Circle (CRUD básico de `Treino` e `User`, PostgreSQL, Docker Compose e Swagger já prontos, no mesmo estilo do curso anterior).
- Suba o banco: `docker compose up -d postgres`.
- Rode a API: `./mvnw spring-boot:run` (sobe em `http://localhost:8080`).
- Confirme o Swagger em `http://localhost:8080/swagger-ui/index.html`.
- Sempre que reiniciar a API depois de uma refatoração, faça um `GET /treinos` rápido para perceber se algo quebrou.

Ao longo da aula, use **Swagger** (mais rápido, mostra o schema) ou **Postman** (mais realista) para testar — o roteiro indica quando um dos dois é mais indicado.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Este é o projeto runner-circle-api, continuação do curso anterior. CRUD, JPA, DTOs, validação, Swagger e Docker já foram ensinados.

Leia os arquivos .java para entender o estado atual e espere meus comandos.

Em cada etapa, implemente somente o que eu pedir, sem antecipar conteúdos. Preserve o comportamento existente quando a tarefa for uma refatoração e pare ao final para revisão.
```

---

## Vídeo 1.2 — Revisitando os princípios REST

**Contexto:** revise rapidamente os princípios REST já aplicados no curso anterior: URI representa recurso, método HTTP representa ação, status code comunica o resultado. Por isso `POST /treinos`, não `POST /criarTreino`. O Modelo de Maturidade de Richardson (nível 0: HTTP como transporte; nível 1: recursos; nível 2: recursos + verbos HTTP + status codes; nível 3: HATEOAS) situa nossa API no nível 2.

Nenhuma implementação neste vídeo.

---

## Vídeo 1.3 — O problema do Controller "gordo"

**Contexto:** abra o `TreinoController` e identifique tudo o que ele faz hoje: recebe HTTP, converte dados, acessa Repository, salva/busca/remove, monta resposta. O código funciona, mas o Runner Circle ainda vai ganhar curtidas, comentários, autenticação e autorização — e um Controller que concentra tudo isso não escala. Esse é o gancho para o Single Responsibility Principle: cada classe deveria ter uma responsabilidade principal, e o Controller deveria cuidar principalmente de HTTP.

Nenhuma implementação neste vídeo.

---

## Vídeo 1.4 — Criando a camada de Service

**Contexto:** vamos extrair a lógica do Controller para uma camada de Service, sem alterar o comportamento externo da API (uma refatoração). Responsabilidades: Controller cuida de HTTP, Service cuida das regras da aplicação, Repository cuida da persistência.

**Peça para a IA:**

```text
Extraia do TreinoController a lógica de CRUD para um TreinoService, que deve usar o TreinoRepository.

Refatore o Controller para delegar as operações ao Service.

Preserve endpoints, DTOs, status codes e comportamento atual. Não crie Mapper nem reorganize pacotes ainda.

Ao terminar, informe os arquivos alterados e pare para revisão.
```

**Revisar:** confira `@Service` e o uso do `TreinoRepository` em `TreinoService`; confirme que o Controller ficou mais enxuto e não acessa mais o Repository diretamente. Código gerado não é código aprovado — sempre revise o que a IA produziu.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

Depois, pelo Swagger ou Postman, repita `GET /treinos`, `POST /treinos`, `PUT /treinos/{id}`, `DELETE /treinos/{id}` — externamente nada deve ter mudado: mesmos endpoints, mesmos status codes, mesmo formato de resposta.

---

## Vídeo 1.5 — Padronizando o mapeamento com Mapper

**Contexto:** localize as conversões `TreinoRequestDTO ↔ Treino ↔ TreinoResponseDTO`. Esse mapeamento vai crescer quando `Treino` precisar exibir dados do autor, curtidas e comentários — em vez de espalhar conversões pelo projeto, vamos centralizá-las em uma classe `TreinoMapper`.

**Peça para a IA:**

```text
Crie um TreinoMapper para centralizar as conversões entre TreinoRequestDTO, Treino e TreinoResponseDTO.

Refatore o código para usar o Mapper e remova conversões duplicadas do Controller ou Service.

Não altere DTOs, endpoints ou pacotes. Não use biblioteca externa.

Ao terminar, informe o que mudou e pare para revisão.
```

**Revisar:** confira as conversões em `TreinoMapper` e confirme que Controller e Service deixaram de ter mapeamento duplicado.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

No Swagger ou Postman, repita `POST /treinos` e `GET /treinos` — a resposta deve ser idêntica à do vídeo anterior.

---

## Vídeo 1.6 — Organizando os pacotes

**Contexto:** com poucas classes, tudo na raiz ainda é fácil de achar; isso deixa de escalar conforme o projeto cresce. Estrutura recomendada: `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `exception`. Este curso usa **pacotes por camada** para deixar as responsabilidades explícitas (existem outras formas de organizar um projeto, mas não são o foco aqui).

**Peça para a IA:**

```text
Reorganize o projeto nos pacotes controller, service, repository, model, dto, mapper e exception.

Mova as classes existentes conforme sua responsabilidade, ajuste packages e imports e mantenha RunnerCircleApiApplication na raiz do pacote base.

Não crie funcionalidades novas. Compile ao final e pare para revisão.
```

**Revisar:** confira a árvore de pacotes e abra algumas classes para conferir os novos `package`.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

Depois, `GET /treinos` pelo Swagger ou Postman. O objetivo é só confirmar que mover arquivos de pacote não quebrou nada (import esquecido, `@Component`/`@Service` perdido). Se o Swagger não carregar, o mais comum é `package` desalinhado com a pasta física.

---

## Vídeo 1.7 — Upload de imagem e validações do formulário

**Contexto:** a tela de Nova Postagem também envia uma imagem — dados estruturados + arquivo em uma mesma requisição `multipart/form-data`. `MultipartFile` representa o arquivo recebido pelo Spring. Regras a definir antes de pedir para a IA:

- imagem opcional;
- máximo 5 MB;
- jpg/jpeg, png ou webp;
- nome único com UUID;
- armazenamento local em `uploads/`;
- URL salva em `imagemUrl`.

Armazenamento local é suficiente para aprender o fluxo; storage em nuvem (S3, Cloudinary) fica como evolução possível depois do curso.

**Peça para a IA:**

```text
Adapte POST /treinos para receber multipart/form-data com os dados do treino e um MultipartFile opcional chamado imagem.

Para a imagem: limite 5 MB, aceite jpg/jpeg, png e webp, gere nome único com UUID, salve em uploads/ e grave a URL em imagemUrl. Crie ImagemInvalidaException para arquivos inválidos e configure o acesso HTTP aos uploads.

É importante que o campo upload apareça como um campo de arquivo no Swagger UI, não como um campo de texto. Mas mantenha os demais campos no formato json.

Adicione @Size(max = 500) à descrição se ainda não existir.

Não trate ImagemInvalidaException no GlobalExceptionHandler ainda e não implemente storage em nuvem.

Ao terminar, explique os arquivos alterados e pare para revisão.
```

**Revisar:** confira como o endpoint recebe `multipart/form-data`, o uso de `MultipartFile`, a validação de tamanho e formato, a geração do UUID, a pasta `uploads/`, o preenchimento de `imagemUrl` e a configuração para servir o arquivo por HTTP.

⚠️ **Detalhe que costuma pegar todo mundo:** como `treino` chega como uma parte JSON dentro do multipart (via `@RequestPart`), o Spring só converte esse pedaço em `TreinoRequestDTO` se a parte declarar `Content-Type: application/json`. Um cliente que manda essa parte sem Content-Type (o padrão do Postman ao adicionar um campo de texto comum) é recusado pelo Spring. Isso deveria virar `415 Unsupported Media Type`, mas só vai virar isso quando o `GlobalExceptionHandler` tratar `HttpMediaTypeNotSupportedException` — o que só acontece na Aula 4. Até lá, esse erro cai no handler genérico (se existir um catch-all para `Exception` retornando 500, você vai ver um 500 em vez de um 415).

**Testar:**

Prefira o Swagger para o fluxo principal; use o Postman para entender o detalhe do Content-Type acima.

Pelo Swagger: abra `POST /treinos` → `Try it out`. O Swagger divide o formulário em `treino` (textarea JSON) e `imagem` (seletor de arquivo), montando o Content-Type de cada parte sozinho. Edite o JSON, escolha uma imagem e `Execute`. Confirme o `201`, o campo `imagemUrl` na resposta, e abra essa URL (`http://localhost:8080/uploads/<nome-gerado>`) no navegador.

Pelo Postman: `POST /treinos`, `Body → form-data`. Na chave `treino`, ajuste o Content-Type da parte para `application/json` (menu de `...` ao lado do campo) e cole o JSON como valor. Na chave `imagem`, mude o tipo de `Text` para `File`. Envie e confirme `201`, arquivo em `uploads/`, `imagemUrl` preenchido. Repita sem ajustar o Content-Type da parte `treino` para ver o erro (415 ou 500, dependendo se a Aula 4 já rodou). Depois envie um `.pdf` no lugar da imagem (espera `400`) e um arquivo maior que 5 MB (espera `400`).

---
