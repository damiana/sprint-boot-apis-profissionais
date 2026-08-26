# Aula 6 — Autorização e roles

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Na Aula 5 a API passou a saber **quem** está fazendo cada requisição. Agora respondemos a segunda pergunta, em aberto desde o vídeo 5.2: **o que essa pessoa pode fazer**.

> Regra da aula: nunca usar um `id` enviado pelo cliente para decidir permissão — a identidade usada em qualquer verificação de autorização vem sempre do token validado pelo filtro JWT, nunca de um parâmetro de request.

---

## Pré-requisitos

1. Confirme que a variável `JWT_SECRET` continua configurada (Aula 5) — sem ela a aplicação nem sobe.
2. Depois do vídeo 6.2, `User` ganha o campo `role`. Se você já tem uma massa de usuários em `data.sql`, garanta que a IA adicione a coluna com valor padrão (`USER`) para as linhas existentes.
3. Promova **manualmente** pelo menos um desses usuários para `ADMIN` direto no banco (DBeaver: `UPDATE users SET role = 'ADMIN' WHERE id = ...`). Não existe endpoint para isso no curso — de propósito, é um risco de segurança promover alguém a admin por uma rota da própria API sem uma camada extra de proteção; tratamos isso como operação manual de backoffice.
4. Qualquer token gerado **antes** do vídeo 6.2 não carrega a claim de `role`. Depois de implementar esse vídeo, faça login novamente para pegar um token atualizado — vale para todos os usuários de teste, inclusive o que virou `ADMIN`.
5. Tenha à mão pelo menos dois tokens de pessoas `USER` diferentes (uma autora de um treino, outra não) e um token `ADMIN`. Esta aula depende de alternar entre eles o tempo todo.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 6 do projeto runner-circle-api.

Leia os arquivos .java e confirme o estado atual após a Aula 5 (Spring Security, JWT, JwtAuthenticationFilter). Espere meus comandos.

Nesta aula vamos implementar roles (USER/ADMIN) e autorização: restrição de endpoints por role e garantia de que cada pessoa só edita ou remove os próprios treinos e comentários.

Implemente somente o que eu pedir em cada etapa e não antecipe testes automatizados, que serão assunto da próxima aula.

Nunca use um id vindo do cliente para decidir permissão — a identidade correta vem do SecurityContext, populado pelo JwtAuthenticationFilter. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 6.1 — O problema: todo usuário autenticado pode tudo

**Contexto:** depois da Aula 5, a API exige um token válido para operações protegidas, mas ainda não verificamos **de quem** é o token. Demonstre:

```text
1. Login como pessoa A → token A
2. Login como pessoa B → token B
3. Pessoa A cria um treino → id 42
4. Com o token B, chame PUT /treinos/42 (ou DELETE /treinos/42)
```

O treino é de A, mas B consegue editar ou apagar — isso funciona hoje e é o problema desta aula. Autorização, aqui, tem dois formatos diferentes: por papel (role — só `ADMIN` pode listar todas as pessoas usuárias) e por posse do recurso (só quem criou o treino pode editá-lo).

Nenhuma implementação neste vídeo.

---

## Vídeo 6.2 — Modelando roles no domínio

**Contexto:** `User` ainda não distingue tipos de pessoa usuária. Modele com enum (evita valores livres como `"admin"`/`"Admin"`):

```java
public enum Role {
    USER,
    ADMIN
}
```

A role precisa estar disponível logo depois do login — por isso também vai para dentro do JWT, junto com a identidade da pessoa.

**Peça para a IA:**

```text
Crie o enum Role com USER e ADMIN no pacote model.

Adicione o campo role em User, com USER como valor padrão atribuído no cadastro (POST /auth/register).

Se existir um data.sql com usuários de teste, adicione a coluna role preenchida com USER para as linhas existentes — sem isso, a leitura desses usuários pode falhar depois desta mudança.

Atualize o JwtService para incluir a role como claim no token gerado no login.

Atualize LoginResponseDTO para expor a role da pessoa autenticada.

Não implemente restrição de endpoints ainda. Pare para revisão.
```

**Revisar:** confira o enum `Role`, o valor padrão `USER` no registro, a claim de role no `JwtService`, `LoginResponseDTO` expondo a role.

**Testar:**

`POST /auth/register` para uma pessoa nova e confirme no banco que `role` foi salva como `USER`. `POST /auth/login` com um usuário existente, copie o token e decodifique em jwt.io — confirme a claim de role. Login com o usuário promovido a `ADMIN` e confirme a claim `ADMIN`.

---

## Vídeo 6.3 — Restringindo endpoints por role

**Contexto:** alguns endpoints não deveriam existir para qualquer pessoa autenticada — por exemplo, listar todas as pessoas usuárias, algo só cabível para quem modera a plataforma. `@PreAuthorize` permite autorização declarativa direto na assinatura do método:

```java
@PreAuthorize("hasRole('ADMIN')")
```

⚠️ **O detalhe que mais derruba essa configuração:** `hasRole('ADMIN')` não procura a authority `"ADMIN"` — ele procura `"ROLE_ADMIN"` (o prefixo `ROLE_` é adicionado automaticamente por convenção do Spring Security). Se o `JwtAuthenticationFilter` montar a `Authentication` sem `GrantedAuthority` (ou com uma authority só `"ADMIN"`, sem o prefixo), `hasRole('ADMIN')` vai **sempre** retornar falso — inclusive para quem realmente é admin. O sintoma é sutil: nada quebra, a aplicação sobe normal, só que todo mundo toma `403`.

**Peça para a IA:**

```text
Habilite @EnableMethodSecurity na configuração de segurança, se ainda não estiver habilitado.

No JwtAuthenticationFilter, ao montar a Authentication a partir do token, inclua uma GrantedAuthority com o prefixo ROLE_ seguido da role da pessoa (ex.: ROLE_ADMIN, ROLE_USER), lida da claim de role do token.

Implemente GET /users, listando todas as pessoas usuárias, restrito com @PreAuthorize("hasRole('ADMIN')").

Não implemente restrição por posse de recurso ainda. Pare para revisão.
```

**Revisar:** confira `@EnableMethodSecurity`, a `GrantedAuthority` com o prefixo `ROLE_` no filtro, e `@PreAuthorize("hasRole('ADMIN')")` em `GET /users`. Um jeito rápido de confirmar o prefixo: coloque um log temporário logo após a autenticação ser montada e confira o texto exato da authority — deve ser `ROLE_ADMIN`, não `ADMIN`.

**Testar:**

```text
GET /users
sem token         → 401
token USER        → 403
token ADMIN       → 200, lista de pessoas usuárias
```

Se um token `ADMIN` retornar `403`, volte direto para o prefixo `ROLE_` — é a causa mais comum.

---

## Vídeo 6.4 — Garantindo que cada pessoa edite apenas o que é seu

**Contexto:** retome o problema do vídeo 6.1: mesmo autenticada, uma pessoa comum ainda consegue editar ou remover o treino de outra, só trocando o `id` na URL. O mesmo vale para comentários e para editar perfil.

A diferença central em relação à Aula 5 é onde mora a verificação: autenticação é o filtro decidindo se a requisição passa; autorização por posse é o Service decidindo se **esta** pessoa pode agir sobre **este** recurso. A aplicação precisa, em qualquer ponto do código, responder "quem é a pessoa autenticada agora?" sem depender de parâmetro do cliente — essa resposta vem do `SecurityContextHolder`, preenchido pelo `JwtAuthenticationFilter`. Em vez de espalhar `SecurityContextHolder.getContext()...` por cada Service, centralize isso em um único componente (mesmo raciocínio de responsabilidade única do Mapper, na Aula 1).

O endpoint `PUT /users/me` não recebe `id` nenhum — a pessoa a ser editada é sempre a autenticada, eliminando o problema por design.

**Peça para a IA:**

```text
Crie um UsuarioAutenticadoService com um método obterUsuarioAutenticado() que retorna o User correspondente à identidade presente no SecurityContextHolder.

Crie AcessoNegadoException (RuntimeException) e trate-a no GlobalExceptionHandler retornando 403, no mesmo formato de erro padronizado na Aula 4.

Atualize TreinoService: nas operações de atualizar e remover um treino, use UsuarioAutenticadoService para obter quem está autenticado e lance AcessoNegadoException se essa pessoa não for a autora do treino.

Atualize ComentarioService: só a pessoa autora do comentário, ou a pessoa autora do treino comentado, pode remover um comentário; qualquer outra pessoa recebe AcessoNegadoException.

Crie UserUpdateRequestDTO com username, nome e bio, e implemente PUT /users/me, que atualiza somente a pessoa autenticada (obtida via UsuarioAutenticadoService), sem receber id por parâmetro.

Não implemente moderação por ADMIN nesses fluxos ainda. Pare para revisão.
```

**Revisar:** confirme que `UsuarioAutenticadoService` é o único ponto que fala diretamente com `SecurityContextHolder`. Em `TreinoService` e `ComentarioService`, confira que a verificação de posse acontece antes de alterar dados, que nenhum `id` de usuário é lido do corpo/query para decidir permissão, e que `AcessoNegadoException` é lançada nos casos certos. Confira `PUT /users/me`: sem `{id}` na URL nem no DTO de entrada.

Nota conceitual: por que `403` e não `404`? `403` é mais honesto tecnicamente (o recurso existe, você só não pode agir sobre ele); `404` evita confirmar a existência do recurso para quem não deveria nem saber que ele existe. Este curso usa `403`, mas "não revelar existência" é prática real em APIs mais sensíveis.

**Testar:**

Com os tokens preparados (duas pessoas `USER` distintas):

```text
Pessoa A cria um treino → id 42

Token A → PUT /treinos/42     → 200 (dono)
Token B → PUT /treinos/42     → 403 (não é dono)
Token B → DELETE /treinos/42  → 403

Pessoa B comenta no treino 42 (dono é A) → comentário id 10
Token B → DELETE /comentarios/10  → 200 (autora do comentário)
Token A → DELETE /comentarios/10  → 200 (autora do treino, se ainda existir)

Token C (terceira pessoa) tentando apagar comentário de outra pessoa em treino que não é dela → 403
```

Depois teste `PUT /users/me` com o token A, alterando `username`, `nome` e `bio`, e confirme que a resposta reflete só a própria pessoa autenticada.

---