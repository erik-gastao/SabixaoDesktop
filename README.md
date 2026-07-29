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
Carrega as 4 telas e confere que todo `onAction` do FXML tem método
correspondente no controller e que as imagens referenciadas existem.

### Testar a integração com a API (sem abrir a interface)
`TestApiIntegration` tem um `main` que busca perguntas e imprime no console:
```bash
./mvnw clean test-compile
java -cp "target/classes;target/test-classes;<caminho-do-gson.jar>" br.com.grupo2.sabixao.sabixao.TestApiIntegration
```

## ✨ Funcionalidades

### 🏠 Tela Inicial (`inicio.fxml`)
- Campos de nome do jogador e PIN do quiz
- Validação de campos vazios e de PIN (apenas números)
- **COMEÇAR** — inicia o quiz
- **LOGIN** — vai para a tela de login

### 🔐 Login (`login.fxml`)
- Nome e senha, com validação (nome ≥ 3 caracteres, senha ≥ 6)
- **ENTRAR**, **VOLTAR** e link para criar conta
- ⚠️ Autenticação ainda não integrada ao backend — o login sempre aceita

### 📝 Criar Conta (`criar-conta.fxml`)
- Nome, senha e confirmação de senha, com validação
- ⚠️ Cadastro ainda não integrado ao backend — nada é persistido

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
    ├── ApiService.java          # Busca perguntas e fala com o backend
    └── TranslatorService.java   # Tradução EN → PT-BR

src/main/resources/
├── br/com/grupo2/sabixao/sabixao/   # inicio, login, criar-conta, quiz (.fxml)
└── images/                          # background e logo

src/test/java/br/com/grupo2/sabixao/sabixao/
├── CarregamentoTelasTest.java   # garante que as 4 telas carregam
└── TestApiIntegration.java      # main() para testar a API no console
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

Nenhum controller chama esses métodos ainda — as telas de login e cadastro são apenas visuais.

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
