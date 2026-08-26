# Aula 7 — Testes unitários e de integração

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Na Aula 6 fechamos as regras de autorização: role e posse do recurso. Até aqui, toda validação foi manual — Swagger ou Postman, clique a clique. Agora vamos automatizar essa verificação.

> Regra da aula: cada teste precisa rodar sozinho, sem depender da ordem de execução nem de dados deixados por outro teste.

---

## Pré-requisitos

1. O `spring-boot-starter-test` (já presente desde o curso anterior) traz JUnit 5, Mockito e AssertJ — não é preciso adicionar nada para testes unitários.
2. Para os testes de integração (vídeo 7.4 em diante), adicione o driver H2 em escopo `test` no `pom.xml`, para não depender do PostgreSQL rodando durante os testes.
3. Crie `src/test/resources/application-test.properties` com um banco H2 em memória e `ddl-auto=create-drop`.
4. ⚠️ O `JwtService` (Aula 5) lê a chave secreta com `@Value("${jwt.secret}")`, esperando a variável de ambiente `JWT_SECRET`. Rodando `mvn test` sem essa variável exportada, o contexto do Spring falha ao subir para **qualquer** teste `@SpringBootTest`. Para não depender de configurar variável de ambiente só para rodar testes, defina a chave diretamente em `application-test.properties` (`jwt.secret=uma-chave-de-teste-com-pelo-menos-32-bytes-1234567890`) — os testes usam essa chave, `dev`/`prod` continuam usando a variável de ambiente.
5. Os testes de integração e de MockMvc usam `@ActiveProfiles("test")` para carregar esse arquivo.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 7 do projeto runner-circle-api.

Leia os arquivos .java e confirme o estado atual após a Aula 6 (roles, autorização por posse, UsuarioAutenticadoService, AcessoNegadoException). Espere meus comandos.

Nesta aula vamos escrever testes unitários com JUnit e Mockito, testes de integração com @SpringBootTest e testes de endpoint com MockMvc, incluindo endpoints protegidos por autenticação e autorização.

Implemente somente o que eu pedir em cada etapa e não altere código de produção a menos que eu peça explicitamente. Cada teste deve ser independente dos demais. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 7.1 — Por que testar? O custo de não testar

**Contexto:** na Aula 6 implementamos a regra "só o dono edita o próprio treino". O que acontece se, no futuro, uma refatoração fizer essa verificação sumir por acidente? Sem testes automatizados, a regressão só aparece quando alguém perceber em produção. Testar manualmente exige repetir todo cenário e alguém disponível para clicar; testar automaticamente roda em segundos, em qualquer máquina, e vira parte do build. A pirâmide de testes (unitários → integração → e2e) situa esta aula na base e no meio.

Nenhuma implementação neste vídeo.

---

## Vídeo 7.2 — Testes unitários com JUnit e Mockito

**Contexto:** testar o `TreinoService` sem depender de um banco real exige simular o `TreinoRepository`. JUnit executa os testes (`@Test`) e as verificações (`assertEquals`, `assertThrows`...); Mockito cria dublês (mocks) das dependências. Isolamento: um teste unitário testa uma classe fingindo que as dependências já funcionam corretamente. Estrutura **Arrange-Act-Assert**: prepara os dados/mocks → chama o método testado → verifica o resultado.

**Peça para a IA:**

```text
Crie a classe TreinoServiceTest em src/test/java, no mesmo pacote de TreinoService.

Use @ExtendWith(MockitoExtension.class), um @Mock de TreinoRepository e @InjectMocks para TreinoService.

Escreva um único teste, buscarTreinoPorId_quandoExiste_retornaTreino, seguindo Arrange-Act-Assert: configure o mock para retornar um Treino de exemplo, chame o método de busca do Service e verifique o resultado.

Não escreva outros testes ainda. Pare para revisão.
```

**Revisar:** confira `@ExtendWith(MockitoExtension.class)`, `@Mock` no `TreinoRepository`, `@InjectMocks` no `TreinoService`, e as três partes do teste claramente separadas. Nenhum banco de dados é usado — o `TreinoRepository` é uma simulação que só devolve o que foi configurado no Arrange.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test -Dtest=TreinoServiceTest
```

Confirme que passa. Para ver o valor do teste, quebre temporariamente o método do `TreinoService` (retorne sempre `null`) e rode de novo para ver o teste falhar — depois desfaça.

---

## Vídeo 7.3 — Testando a camada de Service

**Contexto:** um teste de sucesso não basta — cobrir também os caminhos de erro: treino inexistente e autorização por posse.

```text
buscar treino existente          → retorna o treino
buscar treino inexistente        → TreinoNotFoundException
atualizar treino de outra pessoa → AcessoNegadoException
atualizar treino do próprio dono → sucesso
```

Para testar a regra de posse, `TreinoService` depende do `UsuarioAutenticadoService` (Aula 6) — que também precisa ser mockado, não usado com `SecurityContextHolder` real. É exatamente por isso que esse acesso foi extraído para um componente próprio na Aula 6: um `SecurityContextHolder` estático seria bem mais difícil de simular em teste unitário.

**Peça para a IA:**

```text
Amplie TreinoServiceTest com os seguintes casos, um método de teste por cenário:

- buscarTreinoPorId_quandoNaoExiste_lancaTreinoNotFoundException
- atualizarTreino_quandoNaoEhAutor_lancaAcessoNegadoException
- atualizarTreino_quandoEhAutor_atualizaComSucesso

Adicione um @Mock de UsuarioAutenticadoService e configure-o para retornar a pessoa autenticada esperada em cada cenário.

Use assertThrows para os casos de exceção.

Não altere TreinoService nem outras classes de produção. Pare para revisão.
```

**Revisar:** para cada teste, confira que o mock corresponde exatamente ao cenário do nome do método, que `assertThrows` captura a exceção certa, e que nenhum teste depende da ordem de execução dos outros.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test -Dtest=TreinoServiceTest
```

Confirme que os quatro testes passam. Depois, comente temporariamente a verificação de posse em `TreinoService` e rode de novo — `atualizarTreino_quandoNaoEhAutor_lancaAcessoNegadoException` deve falhar. Desfaça a alteração.

---

## Vídeo 7.4 — Testes de integração com @SpringBootTest

**Contexto:** os testes unitários provam que o `TreinoService` se comporta corretamente **isoladamente**, mas não garantem que `Service`, `Repository`, JPA e banco funcionam juntos. Teste unitário usa mocks, sem contexto Spring; teste de integração sobe o contexto Spring de verdade, incluindo um banco. `@ActiveProfiles("test")` direciona a aplicação para o H2 em memória, sem tocar no banco real.

**Peça para a IA:**

```text
Adicione o driver H2 em escopo test no pom.xml, se ainda não estiver presente, e confirme que src/test/resources/application-test.properties existe com H2 em memória e ddl-auto=create-drop.

Crie TreinoIntegrationTest com @SpringBootTest e @ActiveProfiles("test").

Escreva um teste que usa diretamente TreinoRepository e UserRepository (injetados via @Autowired) para: criar um User, criar um Treino associado a ele através do TreinoService, e depois buscar esse treino de volta pelo TreinoRepository, validando que os dados persistidos batem com os enviados.

Pare para revisão.
```

**Revisar:** confira `@SpringBootTest` e `@ActiveProfiles("test")`, que o teste realmente grava e lê do banco (sem mock), e que os dados usados não colidem com nada preexistente (o H2 em memória começa vazio a cada execução, graças ao `create-drop`).

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test -Dtest=TreinoIntegrationTest
```

Se quiser comparar velocidade, rode o unitário (`TreinoServiceTest`) e o de integração um depois do outro — o de integração é visivelmente mais lento, porque sobe o contexto Spring inteiro.

---

## Vídeo 7.5 — Testando endpoints com MockMvc

**Contexto:** um teste de integração como o anterior ainda não passa pela camada HTTP: não testa serialização JSON, status code nem validação do `@Valid`. `MockMvc` simula requisições HTTP contra os Controllers, sem subir a aplicação em uma porta real:

```text
mockMvc.perform(post("/treinos")...)
       .andExpect(status().isCreated())
```

**Peça para a IA:**

```text
Crie TreinoControllerTest com @SpringBootTest, @AutoConfigureMockMvc e @ActiveProfiles("test").

Escreva três testes usando MockMvc:

- criarTreino_comDadosValidos_retorna201: POST /treinos com um corpo válido, esperando 201 e o campo id presente na resposta.
- criarTreino_semTipoTreino_retorna400: POST /treinos sem o campo tipoTreino, esperando 400.
- buscarTreino_quandoNaoExiste_retorna404: GET /treinos/99999, esperando 404.

Use o ObjectMapper do Spring Boot (injetado, não instanciado manualmente) para serializar os corpos de requisição.

Considere que POST /treinos exige um usuário autenticado (Aula 5/6) — para este vídeo, teste apenas os cenários que não dependem de autenticação; endpoints protegidos ficam para o próximo vídeo.

Pare para revisão.
```

**Revisar:** confira `@AutoConfigureMockMvc`, o `ObjectMapper` injetado via `@Autowired` (não `new ObjectMapper()`), e se os três status codes batem com o que o `GlobalExceptionHandler` das Aulas 4 e 5 já retorna. Se o teste de criação exigir autenticação e ainda não tiver token, ele falha com `401` em vez de `201` — é esperado, e é o gancho para o próximo vídeo.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test -Dtest=TreinoControllerTest
```

---

## Vídeo 7.6 — Testando endpoints protegidos

**Contexto:** boa parte dos endpoints exige autenticação, e alguns exigem uma role específica. Duas formas de obter um token dentro do teste: chamar `POST /auth/login` de verdade via MockMvc e extrair o token da resposta, ou gerar o token diretamente com o `JwtService` injetado no teste. Use a primeira como padrão: exercita o fluxo real de login e não exige inventar um usuário "fantasma" direto no banco.

**Peça para a IA:**

```text
No TreinoControllerTest, adicione um método auxiliar privado que registra uma pessoa usuária via POST /auth/register, faz login via POST /auth/login e retorna o token JWT extraído da resposta.

Reescreva o teste criarTreino_comDadosValidos_retorna201 para incluir o header Authorization: Bearer <token> usando esse método auxiliar, e confirme que agora passa com 201.

Adicione dois novos testes:

- criarTreino_semToken_retorna401
- listarUsuarios_comTokenDeUsuarioComum_retorna403 (GET /users, protegido por role ADMIN na Aula 6)

Para o teste de role, promova a pessoa criada para ADMIN diretamente via UserRepository dentro do teste (sem passar por um endpoint, já que não existe um), e escreva também listarUsuarios_comTokenDeAdmin_retorna200.

Pare para revisão.
```

**Revisar:** confira que o método auxiliar de login não duplica lógica de negócio (só orquestra chamadas HTTP), que o header `Authorization` está no formato `Bearer <token>`, e que o teste de role usa o `UserRepository` só para ajustar a role de teste, não para simular login.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test -Dtest=TreinoControllerTest
```

Confirme que todos os testes passam, incluindo os quatro cenários de autenticação/autorização.

---

## Vídeo 7.7 — Ampliando a cobertura

**Contexto:** consolide a suíte e amplie a cobertura para curtidas, comentários e o feed com busca e paginação.

**Peça para a IA:**

```text
Com base no padrão já usado em TreinoServiceTest e TreinoControllerTest, sugira (sem implementar ainda) uma lista de testes adicionais para cobrir:

- curtir e descurtir um treino (incluindo curtir duas vezes com o mesmo usuário)
- criar e remover comentários, incluindo a regra de posse da Aula 6
- GET /treinos com paginação, ordenação e busca textual (incluindo o caso sem nenhum filtro)

Liste os nomes dos métodos de teste propostos e o que cada um valida. Espere minha aprovação antes de implementar qualquer um.
```

Escolha dois ou três desses testes para implementar, seguindo o mesmo padrão Arrange-Act-Assert.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw test
```

Rode a suíte completa e confira o resumo final (quantos testes, quantos passaram) — esse comando também é o que entraria em um pipeline de build.

---