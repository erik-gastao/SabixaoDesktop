# Navegação Entre Telas - Sabixão Desktop

## ✅ Atualizado em: 26 de Novembro de 2025

## Estrutura Atual de Navegação

### 1. 🏠 Tela Home (`home.fxml`)
**Controller:** `HomeController.java`

**Campos:**
- `nomeField` - Campo para nome do usuário
- `pinField` - Campo para PIN do quiz

**Botões e Ações:**
- **COMEÇAR** (`handleComecar()`)
  - Valida nome e PIN
  - Inicia o quiz (TODO: implementar navegação para tela de quiz)
  
- **LOGIN** (`handleLogin()`)
  - Navega para tela de login
  - Arquivo: `login-form.fxml`

---

### 2. 🔐 Tela de Login (`login-form.fxml`)
**Controller:** `LoginFormController.java`

**Campos:**
- `nomeField` - Campo para nome do usuário
- `senhaField` - Campo para senha

**Botões e Ações:**
- **ENTRAR** (`handleEntrar()`)
  - Valida os campos (nome mínimo 3 caracteres, senha mínimo 6 caracteres)
  - Faz login do usuário (TODO: integrar com backend)
  - Redireciona para tela do quiz
  
- **VOLTAR** (`handleVoltar()`)
  - Retorna para tela home
  - Arquivo: `home.fxml`

- **Criar uma conta** (`handleCriarConta()`)
  - Link estilizado abaixo dos campos
  - Navega para tela de criar conta
  - Arquivo: `criar-conta.fxml`

---

### 3. 📝 Tela de Criar Conta (`criar-conta.fxml`)
**Controller:** `CriarContaController.java`

**Campos:**
- `nomeField` - Campo para nome do usuário
- `senhaField` - Campo para senha
- `confirmarSenhaField` - Campo para confirmar senha

**Botões e Ações:**
- **CRIAR CONTA** (`handleCriarConta()`)
  - Valida os campos (nome mínimo 3 caracteres, senha mínimo 6 caracteres)
  - Verifica se as senhas coincidem
  - Cria nova conta (TODO: integrar com backend)
  - Após sucesso, redireciona para `login-form.fxml`
  
- **VOLTAR** (`handleVoltar()`)
  - Retorna para tela de login
  - Arquivo: `login-form.fxml`

---

## Fluxo de Navegação

```
┌─────────────┐
│    HOME     │
│ home.fxml   │
└──────┬──────┘
       │
       ├─ COMEÇAR ──> Quiz (TODO)
       │
       └─ LOGIN ────┐
                    │
              ┌─────▼──────────┐
              │  LOGIN FORM    │
              │login-form.fxml │
              └─────┬──────────┘
                    │
                    ├─ ENTRAR ──> Quiz (TODO)
                    │
                    ├─ VOLTAR ──> HOME
                    │
                    └─ Criar uma conta ──┐
                                         │
                                   ┌─────▼─────────┐
                                   │ CRIAR CONTA   │
                                   │criar-conta.fxml│
                                   └─────┬─────────┘
                                         │
                                         ├─ CRIAR CONTA ──> LOGIN FORM
                                         │
                                         └─ VOLTAR ──> LOGIN FORM
```

---

## Arquivos FXML e Controllers

| Arquivo FXML       | Controller              | Descrição                    |
|-------------------|-------------------------|------------------------------|
| `home.fxml`       | `HomeController`        | Tela inicial do aplicativo   |
| `login-form.fxml` | `LoginFormController`   | Tela de login (autenticação) |
| `criar-conta.fxml`| `CriarContaController`  | Tela de cadastro de usuário  |
| `quiz.fxml`       | `QuizController`        | Tela do quiz (em desenvolvimento) |

---

## Como Navegar Entre Telas

Para navegar entre telas, use o método `App.setRoot()`:

```java
try {
    App.setRoot("nome-do-arquivo-fxml-sem-extensao");
} catch (IOException e) {
    e.printStackTrace();
    // Tratar erro
}
```

**Exemplos:**
- `App.setRoot("home")` - Vai para home.fxml
- `App.setRoot("login-form")` - Vai para login-form.fxml
- `App.setRoot("criar-conta")` - Vai para criar-conta.fxml
- `App.setRoot("quiz")` - Vai para quiz.fxml (quando implementado)

---

## Próximas Implementações

1. **Tela de Quiz**
   - Completar `quiz.fxml`
   - Implementar `QuizController.java` completo
   - Adicionar timer e sistema de pontuação
   - Carregar perguntas do backend

2. **Tela de Resultados**
   - Criar `resultados.fxml`
   - Mostrar pontuação final
   - Exibir ranking
   - Opção para jogar novamente

3. **Integração com Backend**
   - Conectar com API para validar PIN
   - Salvar e recuperar usuários
   - Armazenar pontuações
   - Gerenciar sessões de quiz

---

## Dicas de Desenvolvimento

### Validação de Campos
Sempre valide os campos antes de processar:
```java
if (campo.getText().trim().isEmpty()) {
    showAlert("Erro", "Campo vazio!", Alert.AlertType.WARNING);
    return;
}
```

### Exibir Alertas
Use o método `showAlert()` para feedback ao usuário:
```java
private void showAlert(String title, String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}
```

### Limpar Campos
Sempre limpe os campos após operações bem-sucedidas:
```java
nomeField.clear();
senhaField.clear();
```

### Navegação Segura
Sempre envolva navegações em try-catch:
```java
@FXML
private void handleAction() {
    try {
        App.setRoot("tela-destino");
    } catch (IOException e) {
        e.printStackTrace();
        showAlert("Erro", "Não foi possível navegar!", Alert.AlertType.ERROR);
    }
}
```

---

## Resumo de Mudanças - 26/11/2025

### 🆕 Arquivos Criados
- ✅ `login-form.fxml` - Nova tela dedicada para login
- ✅ `LoginFormController.java` - Controller para autenticação
- ✅ `CriarContaController.java` - Controller para cadastro

### ♻️ Arquivos Renomeados/Atualizados
- ✅ `login.fxml` → `criar-conta.fxml` - Agora é apenas para cadastro
- ✅ `LoginController.java` → Mantido para retrocompatibilidade
- ✅ `HomeController.java` - Atualizado para navegar para `login-form`

### 📐 Melhorias no Design
- Tela de login separada da tela de criar conta
- Link "Criar uma conta" na tela de login
- Fluxo de navegação mais intuitivo
- Posicionamento melhorado dos elementos

---

**Desenvolvido por**: Grupo 2  
**Projeto**: Sabixão Desktop - Quiz Game  
**Data**: Novembro 2025
