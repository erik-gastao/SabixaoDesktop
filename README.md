# Sabixão Desktop - Aplicação JavaFX

## 📋 Descrição
Aplicação desktop JavaFX para o quiz Sabixão, com interface gráfica moderna e integração com backend REST API.

## ✨ Funcionalidades Implementadas

### 🏠 Tela Inicial (home.fxml)
- Campo para inserir nome do jogador
- Campo para inserir PIN do quiz
- Validação de campos vazios
- Validação de PIN (apenas números)
- Botão "COMEÇAR" - inicia o jogo com validações
- Botão "LOGIN" - navega para tela de cadastro

### 🔐 Tela de Login/Cadastro (login.fxml)
- Campo para nome do usuário
- Campo para senha
- Campo para confirmar senha
- Validações implementadas:
  - Campos vazios
  - Nome mínimo de 3 caracteres
  - Senha mínima de 6 caracteres
  - Confirmação de senha
- Botão "CRIAR CONTA" - registra novo usuário
- Botão "VOLTAR" - retorna à tela inicial

## 🏗️ Estrutura do Projeto

```
src/main/java/br/com/grupo2/sabixao/sabixao/
├── App.java                    # Classe principal JavaFX
├── HomeController.java         # Controller da tela inicial
├── LoginController.java        # Controller da tela de cadastro
├── model/
│   └── User.java              # Modelo de dados do usuário
└── service/
    └── ApiService.java        # Serviço para comunicação com API

src/main/resources/br/com/grupo2/sabixao/sabixao/
├── home.fxml                  # Interface da tela inicial
└── login.fxml                 # Interface da tela de cadastro
```

## 🚀 Como Executar

### Pré-requisitos
- Java 11 ou superior
- Maven 3.6+

### Executar a aplicação
```bash
mvn clean javafx:run
```

### Compilar
```bash
mvn clean package
```

## 🔌 Integração com Backend

A classe `ApiService` está preparada para integração com uma API REST. Por padrão, a URL base é:
```
http://localhost:8080/api
```

### Endpoints esperados:

#### 1. Registrar Usuário
```
POST /api/users/register
Content-Type: application/json

{
  "nome": "João Silva",
  "senha": "senha123"
}
```

#### 2. Login
```
POST /api/users/login
Content-Type: application/json

{
  "nome": "João Silva",
  "senha": "senha123"
}
```

#### 3. Validar PIN do Quiz
```
GET /api/quiz/validate-pin?pin=12345
```

## 🎨 Melhorias de Responsividade

- Tamanhos mínimos definidos para janela (720x480)
- Campos com `promptText` para melhor UX
- Labels com fonte em negrito
- BorderPane e FlowPane para layout responsivo
- Botões com efeito visual e cursor pointer

## 📝 Próximos Passos

### Backend a ser criado:

1. **API REST com Spring Boot** (recomendado)
   - Endpoints para autenticação
   - CRUD de usuários
   - Gerenciamento de quizzes e PINs
   - Registro de pontuações

2. **Banco de Dados**
   - H2 (para desenvolvimento)
   - PostgreSQL ou MySQL (para produção)
   - Entidades: User, Quiz, Question, Answer, Score

3. **Segurança**
   - Hash de senhas (BCrypt)
   - JWT para autenticação
   - CORS configurado para aceitar requisições do desktop

## 🛠️ Tecnologias Utilizadas

- JavaFX 13
- Java 11
- Maven
- HttpClient (Java 11) para requisições HTTP

## 📦 Dependências Adicionais Recomendadas

Para adicionar ao `pom.xml`:

```xml
<!-- Para trabalhar com JSON -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

## 👥 Autores
Grupo 2 - Sabixão Desktop
