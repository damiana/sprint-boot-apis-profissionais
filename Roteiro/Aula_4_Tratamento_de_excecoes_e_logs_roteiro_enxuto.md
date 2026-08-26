# Aula 4 — Tratamento de exceções e logs

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Até aqui a API já tem relacionamentos e um Feed paginado, ordenado e com busca. Agora vamos melhorar como ela responde e registra erros.

> Regra da aula: nunca registrar senhas, tokens ou outros dados sensíveis em logs.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 4 do projeto runner-circle-api.

Leia os arquivos .java e confirme o estado atual após a Aula 3. Espere meus comandos.

Nesta aula vamos trabalhar exceções customizadas, tratamento centralizado, formato de erro e logs com SLF4J.

Implemente somente o que eu pedir em cada etapa, preserve a arquitetura existente e não antecipe conteúdos. Nunca inclua senhas ou tokens em logs. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 4.1 — O problema das exceções genéricas

**Contexto:** a API já trata erros de validação com `@Valid`. Faça `GET /treinos/99999` e observe a resposta atual — um erro esperado da aplicação pode acabar aparecendo como erro genérico ou expondo detalhes internos. Existe diferença entre "algo inesperado que quebrou" e "o treino solicitado não existe": o segundo caso é conhecido pela aplicação e deveria produzir uma resposta clara, sem espalhar `try/catch` por todos os Controllers.

Nenhuma implementação neste vídeo.

---

## Vídeo 4.2 — Criando exceções customizadas

**Contexto:** em vez de uma exceção genérica ao buscar um treino inexistente, queremos algo que comunique exatamente o problema: `TreinoNotFoundException`. O mesmo raciocínio vale para comentários. Já existe também `ImagemInvalidaException`, criada no upload (Aula 1).

**Peça para a IA:**

```text
Crie TreinoNotFoundException e ComentarioNotFoundException como RuntimeException.

Atualize TreinoService e ComentarioService para lançar essas exceções quando o recurso solicitado não existir.

Preserve o comportamento restante e não altere o GlobalExceptionHandler ainda. Pare para revisão.
```

**Revisar:** confira as classes no pacote `exception`, a herança de `RuntimeException`, onde cada exceção é lançada, e se o Controller continua sem `try/catch`.

Se o projeto já tem `UsuarioNaoEncontradoException` (usada desde a Aula 2 quando um `userId` não corresponde a nenhum usuário), ela também ainda não tem tratamento no `GlobalExceptionHandler` — isso é coberto no próximo vídeo, junto com as demais.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

`GET /treinos/99999` ainda deve aparecer como erro genérico do Spring — falta transformar essa exceção em resposta HTTP adequada, o que acontece no vídeo 4.4.

---

## Vídeo 4.3 — Generalizando o GlobalExceptionHandler

**Contexto:** já sabemos lançar `TreinoNotFoundException`, `ComentarioNotFoundException` e `ImagemInvalidaException`, mas precisamos transformá-las em respostas HTTP. Já existe um `GlobalExceptionHandler` usado para validação — a ideia é centralizar também os outros erros ali, para que os Controllers não precisem conhecer cada tratamento:

```text
recurso não encontrado (treino/comentário/usuário) → 404
imagem inválida                                    → 400
tipo de conteúdo não suportado no upload           → 415
imagem maior que o limite configurado              → 400
erro inesperado                                    → 500
```

⚠️ **Cuidado com a ordem de implementação.** É tentador criar o `@ExceptionHandler(Exception.class) → 500` **antes** de mapear os casos específicos. O problema: esse catch-all também intercepta exceções que o próprio Spring já lançaria com o status certo — `HttpMediaTypeNotSupportedException` (deveria virar `415`, ex.: parte `treino` do multipart sem `Content-Type: application/json`, ver Aula 1) e `MaxUploadSizeExceededException` (deveria virar `400`, imagem acima de `spring.servlet.multipart.max-file-size`). Sem handler específico, ambas caem no genérico e viram `500` — um status enganoso para um erro do cliente.

**Peça para a IA:**

```text
Amplie o GlobalExceptionHandler para tratar, cada um com o status adequado:

- TreinoNotFoundException, ComentarioNotFoundException e UsuarioNaoEncontradoException → 404
- ImagemInvalidaException → 400
- HttpMediaTypeNotSupportedException (Content-Type não suportado, ex.: parte multipart sem application/json) → 415
- MaxUploadSizeExceededException (imagem acima do limite configurado no multipart) → 400

Adicione também um handler para Exception que retorne 500 sem expor stack trace ou detalhes internos — mas garanta que ele não capture os casos acima antes dos handlers específicos.

Preserve o tratamento de validação já existente. Pare para revisão.
```

**Revisar:** confira os `@ExceptionHandler` e os status retornados. Liste-os e confirme que cada exceção tem exatamente um handler dedicado antes do genérico — se `UsuarioNaoEncontradoException`, `HttpMediaTypeNotSupportedException` ou `MaxUploadSizeExceededException` não aparecerem na lista, elas caem silenciosamente no 500.

**Testar:**

```text
treino inexistente                  → GET /treinos/99999                        → 404
comentário em treino inexistente    → POST /treinos/99999/comentarios           → 404
curtir com userId inexistente       → POST /treinos/{id}/curtir?userId=99999    → 404
imagem inválida (.pdf/.txt)                                                     → 400
imagem acima de 5MB                                                             → 400
upload sem Content-Type: application/json na parte "treino" (ver Aula 1)        → 415
```

---

## Vídeo 4.4 — Padronizando o formato de erro

**Contexto:** os status já estão corretos, mas quem consome a API também precisa de um corpo previsível — evitando que cada erro tenha um formato diferente. Formato simples:

```json
{
  "timestamp": "...",
  "status": 404,
  "mensagem": "Treino não encontrado",
  "caminho": "/treinos/99999"
}
```

`ProblemDetail`/RFC 7807 é uma referência de mercado para padronização de erros (sem aprofundar aqui).

**Peça para a IA:**

```text
Crie um ErrorResponse com timestamp, status, mensagem e caminho.

Refatore o GlobalExceptionHandler para usar esse mesmo formato em todos os erros, incluindo validação.

Não altere os status HTTP definidos anteriormente. Pare para revisão.
```

**Revisar:** abra `ErrorResponse` e o Handler; confira se todos os cenários seguem a mesma estrutura.

**Testar:**

```text
404 → GET /treinos/99999
400 → POST /treinos com campo obrigatório faltando
500 → force um erro inesperado, se tiver algum cenário fácil à mão
```

Compare os corpos: todos devem ter exatamente os campos `timestamp`, `status`, `mensagem`, `caminho`.

---

## Vídeo 4.5 — Introdução a logs com SLF4J

**Contexto:** a resposta HTTP ajuda quem consome a API, mas quem mantém a aplicação precisa de registros no servidor para investigar problemas. Evite `System.out.println`; use SLF4J, já incluído no Spring Boot:

```java
Logger logger = LoggerFactory.getLogger(TreinoService.class);
```

**Peça para a IA:**

```text
Adicione a depencia do logging com SLF4J no Pom.xml e na classe TreinoService.

Registre a criação de um treino e a tentativa de buscar um treino inexistente.

Não use System.out.println e não registre dados sensíveis. Não altere outras classes ainda. Pare para revisão.
```

**Revisar:** confira `Logger`, `LoggerFactory`, onde os logs foram adicionados e se a informação registrada ajuda a entender o que aconteceu sem expor dado desnecessário.

**Testar:**

Crie um treino (`POST /treinos`) e busque um treino inexistente (`GET /treinos/99999`). Observe os logs no console/terminal onde a aplicação está rodando — não no corpo da resposta HTTP.

---

## Vídeo 4.6 — Níveis de log e boas práticas

**Contexto:** níveis principais — `DEBUG` (detalhe para investigação), `INFO` (evento normal), `WARN` (situação que merece atenção), `ERROR` (falha inesperada). O nível depende do contexto; para esta API: criação de treino → `INFO`, recurso não encontrado → `WARN`, erro inesperado → `ERROR`. Nunca logar senha, token JWT ou credenciais.

**Peça para a IA:**

```text
Adicione logs ao GlobalExceptionHandler.

Use WARN para exceções esperadas de negócio e ERROR para exceções inesperadas.

Revise também os logs existentes para garantir que não registram senha, token ou outros dados sensíveis.

Não adicione logs em excesso. Pare para revisão.
```

**Revisar:** confira os níveis escolhidos e as mensagens; no `ERROR`, verifique se há informação suficiente para investigação no servidor, sem expor isso na resposta HTTP.

**Testar:**

```text
erro de negócio  → GET /treinos/99999 (WARN esperado)
erro inesperado  → algum cenário que dispare o handler genérico (ERROR esperado)
```

---