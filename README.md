# Sabixão Desktop - Aplicação JavaFX

## 📋 Descrição
Aplicação desktop JavaFX para o quiz Sabixão, com interface gráfica e integração com API externa de perguntas (Open Trivia Database).

## 🚀 Como Executar

### Pré-requisitos
- **JDK 17 ou superior** (testado com Temurin 21)
- Maven **não** precisa estar instalado — o projeto inclui o Maven Wrapper

### Executar a aplicação

Windows:
```cmd
mvnw javafx:run
```

Linux / macOS:
```bash
./mvnw javafx:run
```

Se o `java` do seu PATH for antigo, aponte o `JAVA_HOME` para o JDK 17+:
```cmd
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot"
mvnw javafx:run
```

### Compilar
```bash
./mvnw clean package
```

### Rodar os testes
```bash
./mvnw test
```
115 testes, nenhum usa internet. Cobrem:
- carregamento das 4 telas — `onAction` do FXML casando com método do controller,
  imagens referenciadas existindo e cada tela declarando um fundo
- embaralhamento das alternativas preservando o índice da resposta correta
- decodificação de entidades HTML e limpeza da resposta do tradutor
- cadastro e autenticação local
- validação de PIN e cálculo de pontuação

### Testar a integração real com a API
Fica fora do build normal, porque depende de internet e é lento:
```bash
./mvnw test -Dgroups=integracao
```

## ✨ Funcionalidades

### 🏠 Tela Inicial (`inicio.fxml`)
- Campos de nome do jogador e PIN do quiz
- Validação de campos vazios e de PIN (apenas números)
- **COMEÇAR** — inicia o quiz
- **LOGIN** — vai para a tela de login

### 🔐 Login (`login.fxml`)
- Nome e senha, autenticados contra o cadastro local (`LocalAuthService`)
- **ENTRAR**, **VOLTAR** e link para criar conta
- Senha errada e conta inexistente são recusadas, com mensagens diferentes
- Login aceito volta para a tela inicial com o nome já preenchido
- ⚠️ Sem backend: o cadastro vive em memória e desaparece ao fechar o app

### 📝 Criar Conta (`criar-conta.fxml`)
- Nome, senha e confirmação, com validação (nome ≥ 3 caracteres, senha ≥ 6)
- Registra no cadastro local e recusa nome já em uso
- ⚠️ Sem backend: nada é persistido em disco

### ❓ Quiz (`quiz.fxml`)
- 10 perguntas de múltipla escolha vindas da API externa
- Timer de 30 segundos por pergunta com barra de progresso
- Pontuação: 100 pontos + 2 por segundo restante
- Cai em perguntas de exemplo em português se a API estiver indisponível

## 🏗️ Estrutura do Projeto

```
src/main/java/br/com/grupo2/sabixao/sabixao/
├── App.java                     # Classe principal JavaFX e troca de telas
├── InicioController.java        # Tela inicial
├── LoginController.java         # Tela de login
├── CriarContaController.java    # Tela de cadastro
├── QuizController.java          # Tela do quiz
├── model/
│   ├── Usuario.java             # Usuário
│   ├── Pergunta.java            # Pergunta no modelo interno
│   ├── TriviaQuestion.java      # Pergunta como vem da API externa
│   └── TriviaResponse.java      # Envelope da resposta da API
└── service/
    ├── ApiService.java          # Busca perguntas, converte e embaralha
    ├── TranslatorService.java   # Tradução EN → PT-BR
    └── LocalAuthService.java    # Cadastro em memória (no lugar do backend)

src/main/resources/
├── br/com/grupo2/sabixao/sabixao/   # inicio, login, criar-conta, quiz (.fxml)
│                                    # + styles.css (fundo das telas)
└── images/                          # background e logo

src/test/java/br/com/grupo2/sabixao/sabixao/
├── CarregamentoTelasTest.java   # as 4 telas carregam e têm fundo
├── LogicaQuizTest.java          # PIN e pontuação
├── TestApiIntegration.java      # API real, @Tag("integracao")
└── service/
    ├── ApiServiceTest.java          # embaralhamento e entidades HTML
    ├── TranslatorServiceTest.java   # limpeza da tradução
    └── LocalAuthServiceTest.java    # cadastro e login local
```

📐 O padrão de nomes está em **[docs/convencoes.md](docs/convencoes.md)** — leia
antes de criar classe, tela ou documento novo.

## 🔌 APIs Externas

Perguntas vêm da **Open Trivia Database** (`https://opentdb.com/api.php`), com **The Trivia API** (`https://the-trivia-api.com`) como alternativa automática se a principal falhar.

A tradução EN → PT-BR usa a API pública do **MyMemory**, em paralelo (8 threads) e com cache por sessão. Termos sem tradução (nomes próprios, siglas) ficam em inglês.

## 🔧 Backend Próprio (ainda não existe)

`ApiService` já tem os métodos prontos apontando para `http://localhost:8080/api`, aguardando o backend:

| Método | Endpoint esperado |
|---|---|
| `registerUser(User)` | `POST /api/users/register` |
| `loginUser(nome, senha)` | `POST /api/users/login` |
| `validatePin(pin)` | `GET /api/quiz/validate-pin?pin=...` |

Nenhum controller chama esses métodos ainda. Enquanto não houver servidor, login e
cadastro funcionam de verdade contra `LocalAuthService`, que guarda as contas em
memória — é o único ponto a trocar quando o backend existir.

### A fazer no backend
1. **API REST** (Spring Boot) — autenticação, CRUD de usuários, quizzes/PINs, pontuações
2. **Banco de dados** — H2 em desenvolvimento, PostgreSQL ou MySQL em produção
3. **Segurança** — hash de senha com BCrypt, JWT e CORS
   ⚠️ Hoje a senha trafega em texto plano no JSON — resolver junto com o backend

## 🛠️ Tecnologias

- Java 17 (compilado com `release 17`)
- JavaFX 21.0.1
- Gson 2.10.1
- Maven Wrapper 3.3.2 / Maven 3.9.9

## 👥 Autores
Grupo 2 - Sabixão Desktop
