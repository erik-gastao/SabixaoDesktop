# 📚 Índice da Documentação - Sabixão Desktop

## 📖 Guias Principais

### 1. 📄 [README.md](README.md)
**Descrição geral do projeto**
- Funcionalidades implementadas
- Estrutura do projeto
- Como executar a aplicação
- Integração com backend
- Tecnologias utilizadas

### 2. 🚀 [IMPLEMENTACAO_RESUMO.md](IMPLEMENTACAO_RESUMO.md)
**Resumo completo de tudo que foi implementado**
- ✅ Checklist de implementações
- Controllers criados
- FXML corrigidos
- Modelos de dados
- Status do projeto

### 3. 🔌 [API_INTEGRATION_EXAMPLES.md](API_INTEGRATION_EXAMPLES.md)
**Exemplos práticos de integração com API**
- Como usar ApiService nos controllers
- Tratamento avançado de erros
- Modo offline (MockApiService)
- Classe ApiResponse para respostas
- Alternância entre Mock e API real

### 4. 🏗️ [BACKEND_GUIDE.md](BACKEND_GUIDE.md)
**Guia completo para criar o backend**
- **Opção 1:** Spring Boot REST API (recomendado)
  - Estrutura do projeto
  - Exemplos de código (Controllers, Services, Models)
  - Configuração de segurança
  - application.properties
- **Opção 2:** Javalin (backend leve)
- Como testar a API (cURL, Postman)
- Recursos úteis e documentação

### 5. 🔄 [NAVEGACAO_ENTRE_TELAS.md](NAVEGACAO_ENTRE_TELAS.md)
**Como navegar entre diferentes telas**
- Método 1: App.setRoot() (simples)
- Método 2: FXMLLoader direto (com passagem de dados)
- Método 3: SceneManager (recomendado)
- SessionManager para dados globais
- Navegação com animações
- Exemplo completo: Home → Quiz → Resultado

### 6. 🎯 [PROXIMOS_PASSOS.md](PROXIMOS_PASSOS.md)
**Checklist completo para continuar o desenvolvimento**
- Interface do Quiz
- Tela de Resultados
- Tela de Ranking
- Melhorias de autenticação
- Configurações
- Integração com backend
- Funcionalidades adicionais
- Persistência local
- Temas e personalização
- Tratamento de erros
- Animações e UX
- Estatísticas do jogador
- Testes
- Distribuição
- Checklist do Backend
- Recursos de aprendizado

## 📁 Estrutura de Arquivos do Projeto

### Código Fonte Java

```
src/main/java/br/com/grupo2/sabixao/sabixao/
├── App.java                      # Classe principal JavaFX
├── HomeController.java           # Controller da tela inicial ✅
├── LoginController.java          # Controller do cadastro ✅
├── QuizController.java           # Controller do quiz (exemplo) 📝
├── PrimaryController.java        # Controller exemplo (pode remover)
├── SecondaryController.java      # Controller exemplo (pode remover)
├── model/
│   ├── User.java                # Modelo de usuário ✅
│   ├── Question.java            # Modelo de pergunta ✅
│   └── Quiz.java                # Modelo de quiz ✅
├── service/
│   └── ApiService.java          # Serviço de integração com API ✅
└── util/ (criar se usar)
    ├── SceneManager.java        # Gerenciador de navegação 📝
    └── SessionManager.java      # Gerenciador de sessão 📝
```

### Recursos FXML

```
src/main/resources/br/com/grupo2/sabixao/sabixao/
├── home.fxml                     # Tela inicial ✅
├── login.fxml                    # Tela de cadastro ✅
├── quiz.fxml                     # Tela do quiz (exemplo) ✅
├── resultado.fxml               # Tela de resultado 📝
├── ranking.fxml                 # Tela de ranking 📝
└── styles.css (criar)           # Estilos CSS 📝
```

### Configuração

```
├── pom.xml                       # Configuração Maven ✅
├── module-info.java             # Módulos Java ✅
├── nbactions.xml                # Ações do NetBeans ✅
└── .gitignore                   # Arquivos ignorados pelo Git ✅
```

### Documentação

```
├── README.md                     # Documentação principal ✅
├── IMPLEMENTACAO_RESUMO.md      # Resumo das implementações ✅
├── API_INTEGRATION_EXAMPLES.md  # Exemplos de integração ✅
├── BACKEND_GUIDE.md             # Guia do backend ✅
├── NAVEGACAO_ENTRE_TELAS.md     # Guia de navegação ✅
├── PROXIMOS_PASSOS.md           # Próximos passos ✅
└── INDICE_DOCUMENTACAO.md       # Este arquivo ✅
```

## 🎓 Como Usar Esta Documentação

### Para Começar
1. Leia o **README.md** para entender o projeto
2. Revise o **IMPLEMENTACAO_RESUMO.md** para ver o que já foi feito
3. Execute a aplicação seguindo as instruções do README

### Para Desenvolver o Backend
1. Abra o **BACKEND_GUIDE.md**
2. Escolha entre Spring Boot ou Javalin
3. Siga os exemplos de código fornecidos
4. Teste os endpoints com Postman ou cURL

### Para Integrar Desktop com Backend
1. Leia **API_INTEGRATION_EXAMPLES.md**
2. Use o MockApiService para desenvolvimento offline
3. Troque para ApiService quando o backend estiver pronto
4. Teste a integração completa

### Para Adicionar Novas Telas
1. Consulte **NAVEGACAO_ENTRE_TELAS.md**
2. Escolha o método de navegação adequado
3. Crie o arquivo FXML
4. Crie o Controller correspondente
5. Teste a navegação

### Para Continuar o Desenvolvimento
1. Abra **PROXIMOS_PASSOS.md**
2. Veja a lista de prioridades
3. Marque os itens conforme completa
4. Consulte os recursos de aprendizado

## 📌 Legendas

- ✅ = Implementado e funcionando
- 📝 = Exemplo/Template criado, precisa adaptar
- 🔄 = Em desenvolvimento / A ser implementado
- ⏳ = Planejado para futuro

## 🔍 Busca Rápida

### Procurando por...

**Como executar a aplicação?**
→ README.md, seção "Como Executar"

**Como criar o backend?**
→ BACKEND_GUIDE.md

**Como integrar com API?**
→ API_INTEGRATION_EXAMPLES.md

**Como navegar entre telas?**
→ NAVEGACAO_ENTRE_TELAS.md

**O que fazer depois?**
→ PROXIMOS_PASSOS.md

**O que já foi feito?**
→ IMPLEMENTACAO_RESUMO.md

**Endpoints da API?**
→ BACKEND_GUIDE.md, seção "Endpoints esperados"

**Validações dos formulários?**
→ README.md, seção "Funcionalidades Implementadas"

**Modelos de dados?**
→ Ver código em `src/main/java/.../model/`

**Exemplos de código?**
→ API_INTEGRATION_EXAMPLES.md e BACKEND_GUIDE.md

## 💡 Dicas de Uso

1. **Mantenha a documentação atualizada** conforme adiciona novas funcionalidades
2. **Siga a estrutura de pastas** sugerida para manter o projeto organizado
3. **Use os exemplos de código** como base para suas implementações
4. **Consulte os checklists** do PROXIMOS_PASSOS.md regularmente
5. **Teste frequentemente** cada nova funcionalidade adicionada

## 🤝 Contribuindo

Se adicionar novas funcionalidades:
1. Atualize o README.md
2. Adicione exemplos em API_INTEGRATION_EXAMPLES.md se relevante
3. Marque no PROXIMOS_PASSOS.md o que foi concluído
4. Documente qualquer nova tela ou controller

## 📞 Suporte

Para dúvidas específicas:
- **JavaFX:** [Documentação Oficial](https://openjfx.io/)
- **Spring Boot:** [Spring Guides](https://spring.io/guides)
- **Maven:** [Maven Documentation](https://maven.apache.org/guides/)

---

**Última atualização:** 26 de novembro de 2025
**Versão do Projeto:** 1.0-SNAPSHOT
**Desenvolvido por:** Grupo 2 - Sabixão
