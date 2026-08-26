# Aula 8 — Profiles, Actuator e deploy

Roteiro para reproduzir esta aula do curso **Spring Boot: construindo uma API profissional (Runner Circle)** no seu próprio ambiente.

Dinâmica de cada etapa: **contextualizar → pedir para a IA → revisar → testar**.

Na Aula 7 fechamos a suíte de testes automatizados. A API tem arquitetura em camadas, relacionamentos, consultas otimizadas, tratamento de erros, autenticação, autorização e testes. Falta uma última coisa: colocar isso tudo para rodar de verdade, fora da sua máquina. Esta aula termina com um **deploy real na AWS** — não uma simulação.

> Regra da aula: nunca cole Access Key, Secret Access Key, senha de banco ou qualquer credencial da AWS diretamente no chat com a IA. Credenciais se configuram no terminal (`aws configure`) e ficam salvas localmente — a IA orquestra comandos que já enxergam essas credenciais, ela nunca precisa "saber" delas.

---

## Pré-requisitos

1. Confirme a suíte de testes da Aula 7 passando (`./mvnw test`) antes de mexer em configuração de produção.
2. Tenha Docker instalado e o `Dockerfile`/`docker-compose.yml` do curso anterior funcionando localmente — vamos reaproveitar exatamente esse arquivo no deploy.
3. A partir do vídeo 8.5, esta aula depende de uma **conta AWS real**, com cartão de crédito cadastrado (mesmo dentro do free tier).
4. Os recursos criados na AWS no vídeo 8.6 (instância EC2, Security Group, key pair) devem ser **terminados logo depois de usar** (`aws ec2 terminate-instances`), para não gerar custo contínuo.

---

## Contexto inicial para a IA

Use uma vez, no início da aula:

```text
Estamos na Aula 8 do projeto runner-circle-api, a última do curso.

Leia os arquivos .java e de configuração e confirme o estado atual após a Aula 7 (suíte de testes completa). Espere meus comandos.

Nesta aula vamos configurar Spring Profiles, adicionar o Actuator, preparar a aplicação para produção e, ao final, fazer o deploy real na AWS (uma instância EC2 rodando o docker-compose.yml do projeto).

Implemente somente o que eu pedir em cada etapa. Nunca inclua credenciais (chaves de acesso da AWS, senha do banco, JWT_SECRET, o arquivo .pem de acesso SSH) em arquivos versionados pelo Git — sempre via variável de ambiente ou fora do repositório. Antes de rodar qualquer comando que crie ou altere recursos reais na AWS, explique o que ele vai fazer e espere minha confirmação. Pare ao final de cada etapa para revisão.
```

---

## Vídeo 8.1 — O problema de uma configuração única para todos os ambientes

**Contexto:** hoje o `application.properties` tem um único conjunto de configurações, usado tanto localmente quanto (hipoteticamente) em produção. O que normalmente varia entre ambientes: banco de dados (local vs. nuvem), nível de log (detalhado em dev vs. enxuto em produção), chave secreta do JWT (uma chave "qualquer" em dev vs. forte e privada em produção).

Nenhuma implementação neste vídeo.

---

## Vídeo 8.2 — Configurando Spring Profiles

**Contexto:** um profile é um conjunto de configurações nomeado que o Spring Boot escolhe ativar:

```text
application.properties       → configurações comuns a todos os ambientes
application-dev.properties   → específicas de desenvolvimento
application-prod.properties  → específicas de produção
```

Ativado via `spring.profiles.active=dev` (ou `prod`).

**Peça para a IA:**

```text
Crie application-dev.properties e application-prod.properties.

Em application-dev.properties, mova as configurações atuais de banco (Docker local) e deixe o log em nível DEBUG para o pacote da aplicação.

Em application-prod.properties, configure spring.datasource.url, spring.datasource.username e spring.datasource.password para serem lidos de variáveis de ambiente (ainda sem valores reais, isso vem do vídeo 8.6) e deixe o log em WARN para o root e INFO para o pacote da aplicação.

Em application.properties, defina spring.profiles.active=dev como padrão, para continuar rodando localmente sem precisar passar nada explicitamente.

Adicione @ActiveProfiles("test") na classe RunnerCircleApiApplicationTests (o teste de contexto padrão, gerado desde o início do projeto). Sem isso, esse teste tenta subir o contexto sem um profile definido e pode falhar ou se comportar de forma inconsistente agora que dev e prod existem como profiles explícitos.

Não altere JWT_SECRET, que já é lido de variável de ambiente desde a Aula 5. Pare para revisão.
```

**Revisar:** confira os três arquivos — o que ficou comum, o que é só de dev, o que é só de prod (com placeholders de variável de ambiente, não valores reais) — e `RunnerCircleApiApplicationTests` anotada com `@ActiveProfiles("test")`.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw spring-boot:run
```

Confirme que sobe normalmente com o profile `dev` (padrão). Depois:

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

Sem as variáveis de ambiente de produção configuradas, isso deve falhar ao subir — esperado, ainda não configuramos o ambiente de produção. O objetivo aqui é só confirmar que o profile `prod` existe e é reconhecido. Rode também `./mvnw test` e confirme que `RunnerCircleApiApplicationTests` continua passando, agora usando o profile `test`.

---

## Vídeo 8.3 — Spring Boot Actuator

**Contexto:** depois que a aplicação estiver rodando em produção, como saber se ela está saudável? O Actuator é um conjunto de endpoints de monitoramento prontos, embutidos no Spring Boot. O mais importante para nós: `GET /actuator/health` → `{"status": "UP"}`. Em produção, nem todo endpoint do Actuator deveria ficar público — alguns expõem detalhes internos. Vamos expor só o `health`.

**Peça para a IA:**

```text
Adicione spring-boot-starter-actuator ao projeto.

Configure management.endpoints.web.exposure.include para expor apenas o endpoint health, tanto em dev quanto em prod.

Não exponha outros endpoints do Actuator publicamente. Pare para revisão.
```

**Revisar:** confira a dependência e a propriedade de exposição. Expor tudo (`management.endpoints.web.exposure.include=*`) seria arriscado em produção.

**Testar:**

```bash
execute a aplicação pela IDE (mais fácil!) ou rode o comando na raiz do projeto: ./mvnw spring-boot:run
```

`GET http://localhost:8080/actuator/health` pelo navegador ou Postman — confirme `{"status": "UP"}`. Esse é o endpoint que vamos usar no vídeo 8.6 para confirmar que o deploy funcionou.

---

## Vídeo 8.4 — Preparando a aplicação para produção

**Contexto:** checklist antes de ir para a nuvem — `ddl-auto=update`/`create` é ótimo em dev, arriscado em produção (o Hibernate pode alterar o schema sozinho); logs SQL detalhados são úteis em dev, mas viram ruído e risco de vazar dado sensível em produção; `JWT_SECRET` no código nunca é aceitável (já resolvido desde a Aula 5).

**Peça para a IA:**

```text
Em application-prod.properties, ajuste spring.jpa.hibernate.ddl-auto para validate, para que a aplicação em produção NUNCA altere o schema do banco sozinha.

Confirme que spring.jpa.show-sql não está ativo (ou está false) no profile prod.

No docker-compose.yml do curso anterior, o serviço api provavelmente está comentado (só o banco costuma ficar ativo por padrão). Descomente esse serviço, com build: ., a porta 8080:8080 mapeada e depends_on: postgres.

Adicione ao serviço api a variável de ambiente SPRING_PROFILES_ACTIVE: dev — mesmo sendo o padrão em application.properties, deixe isso explícito no compose, para não depender de um comportamento implícito.

Adicione também JWT_SECRET ao serviço api, no formato ${JWT_SECRET:-dev-secret-troque-em-producao-0123456789abcdef}: assim o container lê a variável JWT_SECRET do seu ambiente se ela estiver exportada, e cai num valor padrão só para uso local se não estiver — sem essa variável (ou um valor padrão), o container trava no startup, pelo mesmo motivo que vimos na Aula 5.

Gere o JAR final do projeto e, em seguida, confirme se o Dockerfile e o docker-compose.yml ajustado sobem a aplicação corretamente com esse JAR.

Não altere o profile dev nos arquivos .properties. Pare para revisão.
```

**Revisar:** confira `ddl-auto=validate` em produção — isso significa que o schema do banco em produção precisa existir antes da aplicação subir (resolvido, para os fins deste curso, rodando `ddl-auto=update` manualmente uma vez logo após o primeiro deploy — ferramentas de migração como Flyway/Liquibase ficam como exercício). Confira também o `docker-compose.yml`: serviço `api` descomentado com `build: .`, porta `8080:8080` e `depends_on: postgres`; `SPRING_PROFILES_ACTIVE: dev` explícito; `JWT_SECRET` com valor padrão de fallback.

**Testar:**

```bash
./mvnw clean package
docker compose build
docker compose up
```

Confirme que os containers sobem e a aplicação responde em `GET /actuator/health`.

---

## Vídeo 8.5 — Pré-requisitos: configurando sua conta AWS

**Contexto:** esta etapa não pode ser delegada à IA — envolve dados de pagamento, senha da conta e chaves de acesso, informações sensíveis que nunca devem ser digitadas por um assistente de IA ou coladas em um chat. Você configura manualmente, uma única vez; depois disso, a IA só orquestra comandos que já enxergam essas credenciais no seu computador.

**Passo a passo:**

**1 — Criar a conta AWS**

- Acesse `aws.amazon.com/free` e crie uma conta gratuita.
- Informe e-mail, senha e um cartão de crédito (obrigatório mesmo no free tier — você só é cobrado(a) se passar dos limites gratuitos).
- Escolha o plano de suporte "Basic (gratuito)".

**2 — Configurar um alarme de orçamento**

- No console da AWS, busque "Budgets" (Orçamentos) → crie um orçamento simples de alguns dólares (ex.: US$ 5).
- Você recebe um e-mail se os gastos se aproximarem desse valor — uma rede de segurança contra esquecer algo ligado.

**3 — Criar um usuário IAM (nunca use a conta root no dia a dia)**

- Console → "IAM" → "Users" → "Create user".
- Nome, por exemplo: `runner-circle-deploy`.
- Em permissões, selecione "Attach policies directly" e marque `AdministratorAccess`.

⚠️ Em um projeto real de produção, o correto é dar só as permissões estritamente necessárias (aqui, bastaria algo como `AmazonEC2FullAccess`) — nunca acesso total. Para este curso, `AdministratorAccess` simplifica a configuração; lembre-se de apagar essas chaves de acesso ao final (vídeo 8.6).

**4 — Gerar as chaves de acesso (Access Key)**

- Abra o usuário criado → aba "Security credentials" → "Create access key".
- Escolha o caso de uso "Command Line Interface (CLI)" → confirme o aviso → crie.
- A AWS mostra o **Access Key ID** e a **Secret Access Key** uma única vez. Copie os dois agora, em um gerenciador de senhas — depois de fechar essa tela, a Secret Access Key não pode mais ser recuperada (só é possível gerar uma nova).

**5 — Instalar a AWS CLI**

```bash
# macOS
brew install awscli

# confirme a instalação
aws --version
```

(Windows: instalador `.msi` oficial da AWS. Linux: pacote da distro ou instalador oficial.)

**6 — Configurar a AWS CLI com suas credenciais**

```bash
aws configure
```

- Cole o Access Key ID.
- Cole o Secret Access Key.
- Região: escolha uma perto de você (ex.: `sa-east-1` para São Paulo, ou `us-east-1`).
- Formato de saída: `json`.

⚠️ Faça esse comando **você mesmo, direto no terminal** — nunca peça para a IA digitar ou exibir suas chaves. Elas ficam salvas localmente em `~/.aws/credentials`, e a partir daqui qualquer ferramenta no seu computador (inclusive o Claude Code) consegue usá-las automaticamente, sem que você precise colá-las em lugar nenhum do chat.

**7 — Confirmar que funcionou**

```bash
aws sts get-caller-identity
```

Deve devolver o `Account`, o `UserId` e o `Arn` do usuário IAM que você criou — não da conta root. Isso prova que a CLI está autenticada.

---

## Vídeo 8.6 — Deploy real na AWS: EC2 + Docker Compose

**Contexto:** temos a imagem Docker pronta (vídeo 8.4) e o `docker-compose.yml` do curso anterior, que já sobe a API e o Postgres juntos, localmente. A AWS está configurada (vídeo 8.5). Vamos reaproveitar exatamente esse arquivo, só que rodando numa máquina na nuvem:

```text
Hoje, localmente:   docker compose up  →  app + Postgres rodando no seu notebook
Agora, na AWS:      docker compose up  →  app + Postgres rodando numa instância EC2
```

```text
EC2             → uma máquina virtual "crua" na nuvem. Você escolhe tamanho e sistema operacional
                   e instala o que precisar — como instalar Docker na sua própria máquina, só remoto.
Security Group  → o firewall da instância. Por padrão, tudo fechado; abrimos só o necessário.
```

Diferente de um banco gerenciado (RDS), aqui o Postgres roda **dentro de um container, na mesma instância** — exatamente como já roda na sua máquina hoje. É bem mais simples de configurar e entender, mas tem um trade-off real: se a instância for terminada ou tiver problema, o banco de dados vai junto, sem backup automático. Para os fins deste curso, essa simplicidade vale a pena; evoluir para um banco gerenciado (RDS) fica registrado como exercício no vídeo 8.7.

**Peça para a IA:**

```text
Estou com a AWS CLI configurada localmente (aws sts get-caller-identity funciona).

Quero subir o runner-circle-api numa instância EC2, usando o docker-compose.yml que já temos (app + Postgres no mesmo compose).

1. Crie um key pair chamado runner-circle-key. Salve o arquivo .pem aqui no projeto, em uma pasta que já esteja no .gitignore (ou adicione essa entrada se não existir), e ajuste as permissões do arquivo para 400.

2. Descubra meu IP público atual e crie um Security Group liberando a porta 22 (SSH) apenas para esse IP, e a porta 8080 (HTTP) para qualquer origem.

3. Suba uma instância EC2 (Ubuntu, t2.micro ou t3.micro, dentro do free tier) usando esse key pair e esse security group.

4. Quando a instância estiver no estado "running", conecte via SSH e instale Docker e o plugin docker compose.

5. Copie o Dockerfile, o docker-compose.yml e o código do projeto para a instância.

6. Crie um arquivo .env na instância com um novo JWT_SECRET (gere um valor forte) e uma senha forte para o Postgres, diferentes dos valores usados em desenvolvimento. Não me mostre a senha completa no chat — apenas confirme que o arquivo foi criado.

7. Rode docker compose up -d --build na instância.

Antes de cada comando que crie ou altere recursos reais na AWS, explique o que ele faz e espere minha confirmação.
```

**Revisar:** confira, item a item, que o Security Group libera SSH (porta 22) só para o seu IP — não para `0.0.0.0/0` (SSH aberto para o mundo é um dos erros mais comuns e mais explorados em instâncias na nuvem); que a instância usa um tipo elegível ao free tier; que o arquivo `.pem` não foi commitado (confira o `.gitignore`); que o `.env` na instância tem `JWT_SECRET` e senha de Postgres **novos**, diferentes dos usados em desenvolvimento; e que o `docker-compose.yml` expõe a porta 8080 para fora do container.

⚠️ Se o seu IP público mudar depois (redes domésticas costumam trocar de IP), o acesso SSH configurado no Security Group para de funcionar. Nesse caso, peça para a IA atualizar a regra do Security Group com o novo IP — não abra a porta para qualquer origem só para "resolver rápido".

**Testar:**

Pegue o IP público da instância (`aws ec2 describe-instances` ou direto no console da AWS):

```bash
curl http://<ip-publico>:8080/actuator/health
curl http://<ip-publico>:8080/treinos?page=0&size=1&sort=dataCriacao,desc
```

Confirme `{"status": "UP"}`. Depois, pelo Postman, repita o fluxo real trocando `localhost:8080` pelo IP público da instância: `POST /auth/register`, `POST /auth/login`, `GET /treinos`.

**Encerrando (custos):**

```bash
aws ec2 terminate-instances --instance-ids <id-da-instancia>
```

- Isso apaga a instância e tudo que está nela, **incluindo o banco de dados** — não existe backup automático nesse modelo.
- Se não for usar mais, remova também o Security Group e o Key Pair criados (`aws ec2 delete-security-group`, `aws ec2 delete-key-pair`).
- Se as chaves de acesso do usuário IAM não forem mais necessárias, desative ou apague-as em `IAM → Users → Security credentials`.

---