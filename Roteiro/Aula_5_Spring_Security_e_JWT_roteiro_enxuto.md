# Aula 5 — Spring Security e JWT

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Até a Aula 4 a API já tem estrutura, relacionamentos, consultas e tratamento de erros. Agora precisamos responder: **quem está fazendo a requisição?**

> Regras da aula: nunca devolver senha ou hash em responses e nunca colocar a chave secreta do JWT diretamente no código.

---

## Pré-requisitos

A partir desta aula a aplicação **exige** a variável de ambiente `JWT_SECRET` para subir — sem ela, `@Value("${jwt.secret}")` no `JwtService` falha e a aplicação nem inicia. Defina algo com pelo menos 32 bytes (o algoritmo HMAC exige um tamanho mínimo de chave):

```bash
export JWT_SECRET="uma-chave-bem-grande-e-aleatoria-so-para-desenvolvimento-1234567890"
./mvnw spring-boot:run
```

No IntelliJ IDEA: `Run → Edit Configurations...` → selecione a configuração da `RunnerCircleApiApplication` → em `Environment variables`, adicione `JWT_SECRET=...` → `Apply` → rode novamente.

Use um gerenciador de variáveis de ambiente (`.env` + `direnv`, ou as variáveis do próprio Postman/IntelliJ) para não esquecer isso.

O Swagger só ganha o botão **Authorize** (🔒) depois que configurarmos o esquema de segurança do OpenAPI, no vídeo 5.7. Até lá, testar endpoints protegidos pelo Swagger exige editar manualmente o header em cada chamada — o Postman é mais rápido nesse meio-tempo.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 5 do projeto runner-circle-api.

Leia os arquivos .java e confirme o estado atual após a Aula 4. Espere meus comandos.

Nesta aula vamos implementar autenticação com Spring Security, BCrypt e JWT.

Implemente somente o que eu pedir em cada etapa e não antecipe autorização, que será assunto da próxima aula.

Nunca exponha senha ou hash em responses e mantenha a chave JWT fora do código. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 5.1 — O problema: uma API aberta

**Contexto:** hoje conseguimos criar, editar e apagar treinos, curtir e comentar, mas a API não sabe **quem** está fazendo essas operações — qualquer cliente que conheça os endpoints pode chamá-los.

Nenhuma implementação neste vídeo.

---

## Vídeo 5.2 — Autenticação vs. autorização

**Contexto:**

```text
Autenticação → quem é você?      (email + senha → "essa pessoa é a Damiana")
Autorização  → o que você pode fazer?  (ela pode editar ESTE treino?)
```

Nesta aula resolvemos a autenticação; autorização é a Aula 6.

Nenhum código neste vídeo.

---

## Vídeo 5.3 — Adicionando Spring Security

**Contexto:** adicionar a infraestrutura de segurança ao projeto.

**Peça para a IA:**

```text
Adicione spring-boot-starter-security ao projeto.

Execute a aplicação e não configure nada ainda. Quero primeiro observar o comportamento padrão do Spring Security.

Pare para revisão.
```

Suba a aplicação e tente acessar um endpoint: o Spring Security passa a proteger a aplicação por padrão e uma senha temporária aparece no console. A partir daqui assumimos o controle dessa configuração.

**Peça para a IA:**

```text
Crie SecurityConfig com SecurityFilterChain e deixe todos os endpoints permitidos temporariamente com permitAll().

Não implemente JWT ou autenticação ainda. Pare para revisão.
```

**Revisar:** confira `SecurityFilterChain`, `authorizeHttpRequests`, `permitAll`.

📌 Mais adiante (vídeo 5.7), vamos posicionar um filtro JWT na cadeia com `addFilterBefore(..., UsernamePasswordAuthenticationFilter.class)`. Em versões recentes do Spring Security (6.5+/7.x, padrão do Spring Boot 4.1) essa classe está em `org.springframework.security.web.authentication`, não em `org.springframework.security.authentication` (pacote antigo, que muitos exemplos — e a IA, por hábito — ainda sugerem).

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw compile
```

Com a aplicação no ar, `GET /treinos` pelo Swagger ou Postman ainda deve funcionar normalmente (tudo liberado por `permitAll()` neste ponto).

---

## Vídeo 5.4 — Cadastro com senha protegida

**Contexto:** senha nunca deve ser armazenada em texto puro — queremos um **hash** no banco, não `senha123`.

Um hash transforma qualquer entrada numa saída de tamanho fixo, fácil de calcular numa direção e praticamente impossível de reverter na outra: dá para transformar `"123456"` no hash, mas não dá para, a partir do hash, "descobrir" a senha original.

Por que **BCrypt** e não MD5/SHA-256 puro:

- BCrypt é **adaptativo/lento de propósito** (tem um fator de custo configurável) — quanto mais lento calcular um hash, mais caro fica testar milhões de senhas por segundo em caso de vazamento. MD5/SHA-256 puros são rápidos por design, o que os torna mais fáceis de atacar por força bruta.
- BCrypt embute um **salt aleatório** em cada hash — `encode("123456")` chamado duas vezes produz hashes diferentes, evitando que senhas iguais gerem o mesmo hash (o que impediria ataques com tabelas prontas de hash→senha).

```java
String hash = passwordEncoder.encode(senhaDigitada); // gera o hash (com salt embutido)
boolean confere = passwordEncoder.matches(senhaDigitada, hashArmazenado); // recalcula e compara
```

`matches` funciona porque o salt fica guardado dentro do próprio hash (`$2a$10$...` inclui algoritmo, fator de custo e salt) — o encoder extrai o salt do hash armazenado, recalcula o hash da senha informada com o mesmo salt e compara os dois. Nunca "descriptografamos" nada.

**Peça para a IA:**

```text
Configure um BCryptPasswordEncoder.

Crie RegisterRequestDTO com nome, username, email e senha e implemente POST /auth/register.

Antes de salvar, gere o hash da senha com BCrypt.

A resposta nunca deve conter senha nem hash. Não implemente login ou JWT ainda.

Pare para revisão.
```

**Revisar:** confira `BCryptPasswordEncoder`, o DTO de entrada, a geração do hash e o objeto retornado pelo endpoint — verifique explicitamente se senha ou hash aparecem no response.

**Testar:**

`POST /auth/register` (Swagger ou Postman) com `nome`, `username`, `email`, `senha` (mín. 6 caracteres). Confira na resposta (`201`): sem `senha`, sem hash. Depois abra o banco (DBeaver, tabela `users`) e confirme que o valor salvo começa com `$2a$`/`$2b$` (formato BCrypt). Se houver uma massa em `data.sql`, confira que a coluna `senha` foi mesmo atualizada para hash — é fácil a IA esquecer esse arquivo, já que não é `.java`.

---

## Vídeo 5.5 — O que é JWT

**Contexto:** um JWT tem a estrutura `header.payload.signature` — header (informações do token), payload (claims), signature (garante integridade/autenticidade). Veja um exemplo em jwt.io. JWT **não é criptografia**: o payload pode ser decodificado, por isso nunca colocamos senha ou dado sensível nele. É stateless: login → token → requisições seguintes enviam o token.

Nenhuma implementação neste vídeo.

---

## Vídeo 5.6 — Gerando e validando JWT

**Contexto:** implementar `email + senha → validar credenciais → gerar JWT`. O campo `lembrarMe` da tela de Login muda o tempo de expiração do token.

**Peça para a IA:**

```text
Adicione a biblioteca JJWT e crie JwtService para gerar e validar tokens.

O token deve identificar o usuário e possuir expiração. Leia a chave secreta de variável de ambiente, nunca do código.

Crie LoginRequestDTO com email, senha e lembrarMe e LoginResponseDTO com token e dados públicos do usuário.

Implemente POST /auth/login usando BCrypt para validar a senha. Use uma expiração maior quando lembrarMe=true.

Crie CredenciaisInvalidasException (RuntimeException) para email não encontrado ou senha incorreta, e trate-a no GlobalExceptionHandler retornando 401 com o mesmo formato de erro padronizado na Aula 4 — não deixe login inválido cair no handler genérico de 500.

Não proteja endpoints ainda. Pare para revisão.
```

**Revisar:** confira em `JwtService` a geração, identificação da pessoa, expiração, assinatura, validação e a chave fora do código. Confira o login e a comparação com BCrypt (não comparamos senha com senha — o BCrypt compara a senha informada com o hash armazenado). Confira o `GlobalExceptionHandler`: `CredenciaisInvalidasException` precisa de handler próprio para `401`, senão uma senha errada vira `500`.

**Testar:**

Pelo Swagger, configure o botão **Authorize** (se ainda não aparecer, falta o esquema de segurança do OpenAPI — ver vídeo 5.7). Pelo Postman, `POST /auth/login`. Faça login com `lembrarMe=false` e `lembrarMe=true`, decodifique os tokens em jwt.io e compare a expiração. Teste senha incorreta e email inexistente — os dois devem retornar `401` com o corpo `{"timestamp", "status": 401, "mensagem": "email ou senha inválidos", "caminho": "/auth/login"}`.

**Revisão com IA:**

```text
Revise o JwtService sem alterar o código.

Aponte somente riscos reais de segurança na geração, assinatura, expiração ou validação do token e espere minha autorização.
```

---

## Vídeo 5.7 — Protegendo endpoints com o filtro JWT

**Contexto:** o Spring Security precisa verificar o token antes que uma requisição protegida chegue ao Controller:

```text
Request → JwtAuthenticationFilter → valida token → SecurityContext → Controller
```

A requisição enviará `Authorization: Bearer <token>`.

**Peça para a IA:**

```text
Crie JwtAuthenticationFilter usando OncePerRequestFilter.

Leia o token do header Authorization Bearer, valide com JwtService e, quando válido, registre a autenticação no SecurityContext.

Adicione o filtro ao SecurityFilterChain com addFilterBefore, posicionado antes de UsernamePasswordAuthenticationFilter — importe org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter (esse é o pacote correto no Spring Security 6.5+/7.x; não use org.springframework.security.authentication, que é de versões antigas e não existe mais nessa versão).

Mantenha /auth/** e GET /treinos públicos. Exija autenticação para as demais operações em /treinos/**.

Token ausente, inválido ou adulterado deve resultar em 401. Implemente isso com um AuthenticationEntryPoint customizado que escreve o mesmo formato de erro (timestamp, status, mensagem, caminho) padronizado na Aula 4 — não o corpo de erro padrão do Spring Boot. Ao serializar manualmente para JSON nesse entry point:
- injete o ObjectMapper autoconfigurado pelo Spring Boot 4 (pacote tools.jackson.databind.ObjectMapper, do Jackson 3 — não com.fasterxml.jackson.databind.ObjectMapper, mesmo que ele apareça no classpath via outra dependência, como jjwt-jackson ou springdoc);
- defina explicitamente response.setCharacterEncoding("UTF-8") (ou escreva os bytes com objectMapper.writeValueAsBytes) antes de escrever a resposta, para acentos não saírem corrompidos — o Servlet usa ISO-8859-1 como padrão quando isso não é definido.

Configure também um esquema de segurança Bearer no OpenAPI/Swagger (um bean OpenAPI com SecurityScheme do tipo http/bearer), para que o Swagger UI mostre um botão "Authorize" e permita colar o token para testar os endpoints protegidos.

Não implemente regras de propriedade do treino ainda. Pare para revisão.
```

**Revisar:** confira `OncePerRequestFilter`, leitura do header, remoção de `Bearer`, validação, `SecurityContext`, ordem do filtro, endpoints públicos/protegidos, o import correto de `UsernamePasswordAuthenticationFilter`, o `ObjectMapper` injetado (Jackson 3) e o encoding UTF-8 explícito na resposta do entry point — teste com uma mensagem acentuada e confira que não corrompe.

📌 Por que um `AuthenticationEntryPoint` customizado, em vez do `GlobalExceptionHandler`? Exceções de autenticação do Spring Security acontecem **antes** do `DispatcherServlet`, na cadeia de filtros do Security — o `@RestControllerAdvice` ainda não está em jogo nesse ponto, então a resposta precisa ser formatada manualmente.

**Testar:**

Pelo Postman: gere um token via `POST /auth/login`, depois em uma requisição protegida (ex.: `POST /treinos`) use `Authorization → Type: Bearer Token`. Pelo Swagger: `Authorize` (🔒), cole o token.

```text
sem token        → 401, corpo no formato padronizado da Aula 4
token válido     → acesso permitido
token adulterado → 401 (edite um caractere do token antes de enviar)
```

Confirme também que `POST /auth/login` e `GET /treinos` continuam públicos, sem token.

---

## Vídeo 5.8 — Além do JWT: OAuth2 e login social (conceito)

**Contexto:** login social ("Entrar com Google") não é validado diretamente por nós — confiamos em um provedor que já autenticou a pessoa: `Runner Circle → Google → usuário autentica → Google confirma identidade → Runner Circle continua o fluxo`. Referência: **Authorization Code Flow**. OAuth2/login social resolve um problema diferente de simplesmente gerar nosso próprio JWT.

Fica como exercício explorar `spring-boot-starter-oauth2-client` depois do curso — não é implementado aqui.

---