# Aula 3 — SQL, JOINs e paginação

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Na Aula 2 relacionamos `User`, `Treino` e `Comentario`. Agora vamos olhar para as consultas geradas e transformar `GET /treinos` no endpoint de listagem do Feed.

---

## Pré-requisitos

Antes de começar, tenha uma massa de dados com cerca de 20 treinos de autores diferentes, e habilite em `application.properties`:

```properties
spring.jpa.show-sql=true
```

Os logs SQL serão importantes ao longo da aula. Se quiser gerar essa massa rapidamente, peça para a IA:

```text
A aplicação está rodando, preciso de cerca de 20 treinos de autores diferentes. Faça alguns requests para criar dados novos: usuários, treinos, curtidas e comentários.
```

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 3 do projeto runner-circle-api.

Leia os arquivos .java e confirme o estado atual após a Aula 2. Espere meus comandos.

Nesta aula vamos trabalhar SQL, JOINs, paginação, ordenação e busca no GET /treinos.

Implemente somente o que eu pedir em cada etapa, preserve a arquitetura existente e não antecipe conteúdos. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 3.1 — O problema das consultas N+1

**Contexto:** o Feed precisa listar vários treinos e mostrar quem publicou cada um. Faça `GET /treinos` e observe os logs SQL: primeiro uma consulta busca os treinos, depois aparece uma consulta extra `SELECT ... FROM users WHERE id = ?` para cada treino. Esse padrão é o problema **N+1** (1 consulta para a lista + N consultas para os relacionamentos). Com poucos registros parece irrelevante; num Feed com muitos treinos, o custo cresce rapidamente.

Nenhuma implementação neste vídeo.

---

## Vídeo 3.2 — JOINs em SQL

**Contexto:** JOIN combina linhas de duas (ou mais) tabelas com base em uma condição de igualdade — aqui, "o `autor_id` do treino é igual ao `id` do usuário". Sem JOIN, `treinos` e `users` são tabelas separadas; o JOIN "junta" a linha do treino com a do autor correspondente numa única linha de resultado.

No DBeaver, com `treinos` e `users`:

```sql
-- INNER JOIN: só linhas com correspondência dos dois lados
SELECT * FROM treinos t INNER JOIN users u ON t.autor_id = u.id;

-- LEFT JOIN: mantém todas as linhas de treinos, mesmo sem correspondência em users
SELECT * FROM treinos t LEFT JOIN users u ON t.autor_id = u.id;
```

Um `INNER JOIN` esconde treinos cujo `autor_id` for `NULL` ou não existir mais; um `LEFT JOIN` mantém todos os treinos, preenchendo com `NULL` os campos de `users` quando não há correspondência. Essa diferença é o motivo de escolhermos `LEFT JOIN` na consulta do próximo vídeo: queremos listar **todos** os treinos do Feed, mesmo um eventual treino sem autor.

Nenhuma alteração Java neste vídeo.

---

## Vídeo 3.3 — JPQL e JOIN FETCH

**Contexto:** queremos carregar treinos e autores sem uma consulta separada por relacionamento. JPQL trabalha com entidades e atributos Java, não com tabelas e colunas (`SQL: treinos, users, autor_id` vs. `JPQL: Treino, User, autor`).

Por que não basta um `JOIN` comum em JPQL? Um `JOIN` sem `FETCH` serve só para filtrar/ordenar — não muda o que é carregado no objeto Java. `treino.getAutor()` continuaria como proxy `LAZY`, podendo estourar `LazyInitializationException` fora da sessão. `JOIN FETCH` diz ao Hibernate para popular de verdade o objeto associado com o resultado do JOIN, eliminando a segunda consulta (o N+1 do vídeo 3.1). Usamos `LEFT JOIN FETCH` pelo mesmo motivo do vídeo anterior: manter no resultado um eventual treino sem autor.

**Peça para a IA:**

```text
No TreinoRepository, crie uma consulta JPQL com JOIN FETCH para buscar os treinos junto com seus autores, ordenados por dataCriacao decrescente.

Atualize GET /treinos para usar essa consulta.

Não implemente paginação, busca ou filtros ainda. Pare para revisão.
```

**Revisar:** confira o `@Query` no Repository, o `JOIN FETCH t.autor`, e confirme que `GET /treinos` passou a usar esse método.

**Testar:**

`GET /treinos` pelo Swagger ou Postman. Compare os logs com o vídeo 3.1: agora deve aparecer **uma única consulta** (com `left outer join`), em vez de 1 + N consultas.

---

## Vídeo 3.4 — Paginação com Pageable

**Contexto:** `GET /treinos` hoje devolve todos os registros — o que acontece com 100 mil treinos? Um Feed precisa carregar dados em blocos: `page` (qual página) e `size` (quantos elementos por página). No Spring Data, isso é `Pageable` (pedido de paginação) e `Page<T>` (resultado paginado).

**Peça para a IA:**

```text
Adapte a consulta de GET /treinos para suportar paginação com Pageable e retornar Page<TreinoResponseDTO>.

Mantenha o carregamento do autor e preserve o comportamento atual.

Não implemente busca nem filtro por tipo ainda. Pare para revisão.
```

**Revisar:** confira `Pageable`, `Page<Treino>` e a conversão para `Page<TreinoResponseDTO>`. Na resposta, observe `content`, `totalElements`, `totalPages`, `number` e `size`.

**Testar:**

```text
GET /treinos?page=0&size=5
GET /treinos?page=1&size=5
```

---

## Vídeo 3.5 — Ordenação com Sort

**Contexto:** em qual ordem os registros devem aparecer? No Runner Circle, os treinos mais recentes primeiro (`dataCriacao DESC`). Paginação precisa de uma ordenação previsível para que o conteúdo das páginas faça sentido — sem isso, não se deve depender da ordem em que o banco devolve os registros.

**Peça para a IA:**

```text
Defina dataCriacao DESC como ordenação padrão do GET /treinos, integrada à paginação existente.

Não implemente busca ou novos filtros. Preserve o restante do comportamento e pare para revisão.
```

**Testar:**

Com treinos de datas diferentes, faça `GET /treinos?page=0&size=5` e confirme, pelo campo `dataCriacao` de cada item em `content`, que os mais recentes aparecem primeiro.

---

## Vídeo 3.6 — Busca textual no Feed

**Contexto:** `GET /treinos` precisa aceitar um parâmetro opcional de busca pela descrição, sem virar um endpoint novo (`GET /treinos?busca=parque`, não `/buscarTreinos`).

⚠️ **Cuidado ao pedir isso para a IA:** nunca vincule um parâmetro nulo dentro de `LOWER`/`CONCAT` no JPQL — isso quebra em runtime no PostgreSQL (`function lower(bytea) does not exist`, porque o driver não consegue inferir o tipo do parâmetro nessa combinação). A forma "óbvia" de escrever — `WHERE (:busca IS NULL OR LOWER(t.descricao) LIKE LOWER(CONCAT('%', :busca, '%')))` — compila e parece correta, mas quebra exatamente quando `busca` não é informado (o caso mais comum). Um ótimo exemplo de código que compila e parece certo, mas só quebra sob um teste real.

**Peça para a IA:**

```text
Adicione ao GET /treinos um parâmetro opcional busca.

Quando informado, filtre os treinos pela descricao de forma case-insensitive.

Importante: nunca vincule um parâmetro nulo dentro de LOWER/CONCAT no JPQL — isso quebra em runtime no PostgreSQL (a driver não consegue inferir o tipo do parâmetro nessa combinação e o bind cai para bytea, gerando "function lower(bytea) does not exist"). Em vez de um WHERE (:busca IS NULL OR LOWER(...) LIKE LOWER(CONCAT('%', :busca, '%'))), monte o padrão "%...%" já no Service, sempre como uma String não nula (use string vazia quando busca não for informado), e no Repository compare direto com LOWER(:buscaPattern), sem CONCAT nem IS NULL.

A busca deve continuar funcionando com a paginação, ordenação e carregamento do autor já existentes.

Não implemente filtro por tipo ainda. Pare para revisão.
```

**Revisar:** o ponto mais importante — o padrão é montado no Service (`"%" + busca + "%"`), nunca um `:busca` nulo indo direto pro JPQL. Confira também o comportamento quando `busca` não é informado e a manutenção da paginação.

**Testar:**

```text
GET /treinos?busca=parque&page=0&size=5
GET /treinos?busca=corrida&page=0&size=5
GET /treinos?page=0&size=5
```

O terceiro caso (sem `busca`) é o mais importante — é justamente o cenário que dispara o bug acima, caso ele apareça.

---

## Vídeo 3.7 — Combinando filtros

**Contexto:** além da busca por descrição, filtrar por tipo (`CORRIDA`/`CAMINHADA`). Os filtros devem ser independentes e combináveis: `/treinos?tipoTreino=CORRIDA`, `/treinos?busca=parque`, `/treinos?tipoTreino=CORRIDA&busca=parque` — todos paginados e ordenados.

**Peça para a IA:**

```text
Adicione ao GET /treinos um filtro opcional por tipoTreino.

Ele deve funcionar sozinho ou combinado com busca, paginação e ordenação.

Mantenha o carregamento do autor e evite duplicar lógica desnecessariamente.

Ao terminar, explique a consulta resultante e pare para revisão.
```

**Revisar:** confira como a IA combinou os parâmetros opcionais — desconfie de uma solução excessivamente complexa para apenas dois filtros. Depois peça:

```text
Revise a consulta do GET /treinos que combina autor, busca, tipoTreino e paginação.

Sem alterar o código, aponte somente riscos reais ou melhorias necessárias na consulta atual. Espere minha autorização antes de modificar qualquer arquivo.
```

**Testar:**

```text
GET /treinos?tipoTreino=CORRIDA&busca=parque&page=0&size=10
```

Teste também cada filtro separadamente, e sem nenhum filtro (`GET /treinos?page=0&size=10`) — de novo, o caso "sem filtro nenhum" é o que mais expõe bugs de parâmetro nulo.

---