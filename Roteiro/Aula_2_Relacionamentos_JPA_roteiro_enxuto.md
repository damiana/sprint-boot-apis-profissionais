# Aula 2 — Relacionamentos JPA

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Na Aula 1 a aplicação ficou organizada em Controller → Service → Repository, com Mapper e pacotes por camada. Agora os dados precisam se relacionar.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 2 do projeto runner-circle-api.

Leia os arquivos .java para confirmar o estado atual após a Aula 1 e espere meus comandos.

Nesta aula vamos trabalhar relacionamentos JPA entre User, Treino e Comentario.

Implemente somente o que eu pedir em cada etapa, sem antecipar conteúdos. Preserve a arquitetura em camadas criada na Aula 1 e pare ao final de cada etapa para revisão.
```

---

## Vídeo 2.1 — O problema: treinos sem dono

**Contexto:** hoje `Treino` não sabe quem publicou o quê — é como se todos os treinos fossem soltos, sem pessoa autora. Relembrando os tipos de relacionamento (1:1, 1:N, N:N): uma pessoa pode publicar vários treinos, mas cada treino tem uma única pessoa autora, ou seja, `User (1) ── (N) Treino`.

Nenhuma implementação neste vídeo.

---

## Vídeo 2.2 — Preparando a entidade User

**Contexto:** a partir das telas de Cadastro, Perfil e Editar perfil, os dados de `User` são: nome, username, email, senha, bio e avatarUrl. A senha ainda não será protegida nesta aula — isso é assunto da Aula 5.

**Peça para a IA:**

```text
Revise a entidade User existente e ajuste-a, se necessário, para possuir: id, nome, username, email, senha, bio e avatarUrl.

Garanta também um UserRepository estendendo JpaRepository<User, Long>.

Não implemente segurança, roles ou relacionamentos ainda. Preserve a estrutura de pacotes da Aula 1 e pare para revisão.
```

**Revisar:** confira `@Entity`, `@Id`, a estratégia de geração do id, os campos e o `UserRepository`. Não aceite automaticamente campos extras que a IA sugerir sem necessidade.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

`User` ainda não tem Controller próprio, então a validação por aqui é só a compilação — isso muda no vídeo 2.6.

---

## Vídeo 2.3 — @ManyToOne e @OneToMany

**Contexto:** representar `User (1) ── (N) Treino`: em `Treino`, o atributo `autor` usa `@ManyToOne` (o lado "dono" do relacionamento, que guarda a referência no banco); em `User`, a coleção `treinos` usa `@OneToMany(mappedBy = "autor")` (o lado inverso, sem duplicar a configuração).

**Peça para a IA:**

```text
Crie o relacionamento 1:N entre User e Treino.

Adicione autor em Treino com @ManyToOne e uma coleção de treinos em User com @OneToMany(mappedBy = "autor").

Não altere DTOs ou endpoints ainda e não implemente curtidas ou comentários. Pare para revisão.
```

**Revisar:** confira `Treino.autor → @ManyToOne` e `User.treinos → @OneToMany(mappedBy = "autor")`.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

Ainda sem endpoint para exercitar isso via HTTP — a API só passa a usar o relacionamento no vídeo 2.6. Para confirmar visualmente antes disso, suba a aplicação e abra o banco no DBeaver (próximo vídeo).

---

## Vídeo 2.4 — A chave estrangeira e o SQL gerado

**Contexto:** suba a aplicação e abra o DBeaver. Nas tabelas `users` e `treinos`, localize a coluna que referencia `users` (`treinos.autor_id → users.id`) — essa é a **foreign key**. Se o nome da coluna não estiver explícito no código, use `@JoinColumn` para defini-lo.

**Peça para a IA (se necessário):**

```text
No relacionamento Treino.autor, defina explicitamente a coluna de chave estrangeira como autor_id usando @JoinColumn.

Não altere nenhum outro comportamento.
```

---

## Vídeo 2.5 — Fetch Type: LAZY vs EAGER

**Contexto:** buscar um treino não precisa necessariamente carregar todos os dados relacionados imediatamente.

- `LAZY` → carrega quando necessário. Na prática, o Hibernate coloca no lugar de `autor` um **proxy** (objeto que só sabe o id); a consulta real só acontece no primeiro `treino.getAutor().getNome()`. Dois riscos: acessar esse getter fora de uma transação/sessão aberta lança `LazyInitializationException`; e logar/imprimir a entidade inteira pode disparar carregamentos inesperados.
- `EAGER` → carrega imediatamente, normalmente com uma consulta adicional (a não ser que exista `JOIN FETCH`, assunto da Aula 3). É mais previsível, mas pode carregar dado que você nunca vai usar.

Padrões: `@ManyToOne` é `EAGER` por padrão; `@OneToMany` é `LAZY` por padrão. Ative `spring.jpa.show-sql=true` e observe as consultas geradas.

Decisão adotada no projeto: `User.treinos` fica `LAZY` (ao carregar uma pessoa, raramente você quer *todos* os treinos dela junto). `Treino.autor` fica com `fetch = FetchType.LAZY` explícito, sobrescrevendo o padrão EAGER do `@ManyToOne` — como a Aula 3 vai usar `JOIN FETCH` para buscar o autor sob demanda no Feed, não faz sentido pagar o carregamento automático em *toda* consulta de treino.

**Peça para a IA (se houver ajuste):**

```text
Revise o fetch dos relacionamentos User.treinos e Treino.autor.

Mantenha User.treinos como LAZY e ajuste Treino.autor conforme a decisão atual do projeto, sem alterar outros relacionamentos.

Explique o impacto da configuração e pare para revisão.
```

**Revisar:** observe os logs SQL e relacione as consultas ao carregamento das entidades. Não tente otimizar tudo nesta etapa.

---

## Vídeo 2.6 — Treinos agora têm autor

**Contexto:** o relacionamento existe no banco, mas a API ainda não usa. Como autenticação ainda não existe, use temporariamente um `userId` para identificar o autor no momento de criar um treino — isso é uma solução transitória, que na Aula 5 passa a vir da pessoa autenticada. A resposta deve mostrar nome, username e avatarUrl do autor, e precisamos consultar os treinos de uma pessoa (`GET /users/{id}/treinos`).

**Peça para a IA:**

```text
Atualize a criação de Treino para receber temporariamente um userId, buscar o User correspondente e atribuí-lo como autor.

Atualize TreinoResponseDTO e TreinoMapper para retornar nome, username e avatarUrl do autor.

Implemente também GET /users/{id}/treinos seguindo a arquitetura em camadas existente.

Não implemente autenticação ainda. Pare para revisão.
```

**Revisar:** confira o fluxo `userId → UserRepository → User → Treino.autor`, o Mapper e o DTO, e se o novo endpoint respeita as responsabilidades de Controller/Service/Repository.

**Testar:**

Ainda não existe `POST /users`, então crie pelo menos dois usuários de teste direto no banco (DBeaver). Pelo Swagger ou Postman, faça `POST /treinos` (incluindo `userId`) para cada usuário, com pelo menos um treino cada. Depois valide `GET /users/{id}/treinos` e confirme que a resposta traz só os treinos daquele `id`, com `nome`, `username` e `avatarUrl` do autor embutidos.

---

## Vídeo 2.7 — Curtidas: relacionamento N:N

**Contexto:** uma pessoa curte vários treinos, um treino é curtido por várias pessoas — `User (N) ── (N) Treino`. No banco, um N:N precisa de uma tabela intermediária (`treino_curtidas`); com JPA, representamos isso com `@ManyToMany` e `@JoinTable`.

**Peça para a IA:**

```text
Implemente curtidas como relacionamento N:N entre Treino e User.

Em Treino, use Set<User> com @ManyToMany e @JoinTable.

Crie POST /treinos/{id}/curtir e DELETE /treinos/{id}/curtir, recebendo temporariamente userId.

Inclua curtidasCount no TreinoResponseDTO e ajuste o Mapper.

Preserve a arquitetura em camadas e não implemente autenticação ainda. Pare para revisão.
```

**Revisar:** confira `@ManyToMany`, `@JoinTable`, as colunas da tabela de junção, o uso de `Set<User>` e o cálculo de `curtidasCount`. Abra o banco e veja a tabela intermediária criada.

**Testar:**

`POST /treinos/{id}/curtir?userId={id}` (Swagger ou Postman, sem body) — repita trocando o `userId` para simular pessoas diferentes e confirme `curtidasCount` crescendo. Depois `DELETE /treinos/{id}/curtir?userId=...` e confirme que o contador cai. Curta o mesmo treino duas vezes com o **mesmo** `userId`: como `curtidas` é um `Set<User>`, a segunda chamada não deve duplicar nem dar erro.

---

## Vídeo 2.8 — Comentários: repetindo o padrão 1:N

**Contexto:** um comentário pertence a um treino e possui uma pessoa autora — dois `@ManyToOne` (`User (1) ── (N) Comentario` e `Treino (1) ── (N) Comentario`). Não é preciso reinventar a arquitetura: reaproveite o padrão de camadas definido na Aula 1 (`ComentarioController`, `ComentarioService`, `ComentarioRepository`, `ComentarioMapper`, DTOs).

**Peça para a IA:**

```text
Implemente Comentario seguindo o mesmo padrão de camadas usado em Treino.

Comentario deve ter id, texto, dataCriacao, autor (@ManyToOne User) e treino (@ManyToOne Treino).

Crie Repository, Service, Controller, Mapper e DTOs necessários.

Implemente:
GET /treinos/{id}/comentarios
POST /treinos/{id}/comentarios

No POST, receba temporariamente userId para identificar o autor.

Não implemente segurança nem funcionalidades adicionais. Pare para revisão.
```

**Revisar:** compare a nova estrutura com `Treino` — a IA seguiu o padrão do projeto ou inventou uma arquitetura diferente? Confira relacionamentos, responsabilidades das camadas, DTOs, Mapper e endpoints.

**Testar:**

`POST /treinos/{treinoId}/comentarios` com body `{"texto": "...", "userId": <id>}` (repita com um `userId` diferente). Depois `GET /treinos/{id}/comentarios`. Confira no banco `autor_id` e `treino_id` preenchidos em `comentarios`. Teste também comentar em um `treinoId` inexistente — ainda deve ser o comportamento padrão do Spring, já que o `GlobalExceptionHandler` só ganha tratamento para isso na Aula 4.

---