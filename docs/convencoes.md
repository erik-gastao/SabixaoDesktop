# Convenções de Código — Sabixão Desktop

Padrão adotado pelo Grupo 2. Vale para código novo e para o que for tocado.

## Idioma: domínio em português, infraestrutura em inglês

A regra que resolve 90% das dúvidas:

> **Se representa uma ideia do jogo, é português. Se é encanamento técnico, é inglês.**
> **Nunca misture os dois num mesmo identificador.**

### Português — domínio e interface

Classes de modelo, controllers e tudo que o jogador enxerga:

```java
class Pergunta {
    String getTexto()
    List<String> getOpcoes()
    boolean verificarResposta(int indice)
}

class QuizController {
    void carregarPergunta()
    void verificarResposta(int opcao)
    void mostrarAlerta(String titulo, String mensagem)
    void aoEscolherOpcaoA()
}
```

### Inglês — infraestrutura e DTOs de API

Serviços, transporte HTTP, parsing e a classe `App`:

```java
class ApiService {
    List<Pergunta> fetchTriviaQuestions(int amount, String difficulty)
    String decodeHtml(String text)
}

class TranslatorService {
    String translateToPortuguese(String text)
}
```

`TriviaQuestion` e `TriviaResponse` são **obrigatoriamente** em inglês: o Gson
desserializa pelo nome do campo, então `correct_answer` precisa continuar
`correct_answer`. Renomear quebra a leitura da API silenciosamente.

### O que evitar

| ❌ Evite | ✅ Use | Por quê |
|---|---|---|
| `handleComecar` | `aoComecar` | prefixo inglês + verbo português no mesmo nome |
| `carregarPerguntasAPI` | `carregarPerguntas` | sigla inglesa colada em nome português |
| `showAlert` num controller | `mostrarAlerta` | controller é interface, logo português |
| `traduzirAsync` | `translateAsync` | está em `ApiService`, logo inglês |

## Nomes de arquivo

| Tipo | Padrão | Exemplo |
|---|---|---|
| Classe Java | `PascalCase`, sufixo pelo papel | `QuizController`, `ApiService` |
| FXML | `kebab-case` minúsculo, nome da tela | `criar-conta.fxml`, `login.fxml` |
| Imagem | `kebab-case`, descreve o uso | `background-podium.png` |
| Documento | `kebab-case.md` dentro de `docs/` | `guia-backend.md` |

Só `README.md` fica em `MAIÚSCULAS` na raiz. Todo o resto vai para `docs/`.

**Nunca versione pelo nome do arquivo.** `NAVEGACAO_TELAS_ATUALIZADO.md` foi
removido justamente por isso — na atualização seguinte viraria
`..._ATUALIZADO_v2.md`. O histórico é responsabilidade do git.

Imagem sem semântica no nome também não passa. `background2.png` e
`background3.png` não diziam a ninguém o que eram, e viraram
`background-roxo.png` e `background-azul.png`. Quando a tela de destino já
estiver decidida, o nome deve seguir o uso — como em `background-podium.png`.

## Controllers e FXML

- Um controller por tela, com o nome da tela: `criar-conta.fxml` ↔ `CriarContaController`
- Campos `@FXML` seguem `substantivoPortuguês` + `TipoEmInglês`: `nomeField`, `tempoProgressBar`
- Handlers de botão começam com `ao`: `aoComecar`, `aoVoltar`, `aoEscolherOpcaoA`
- Troca de tela é **sempre** via `App.setRoot(...)`. Não manipule `Stage` ou
  `FXMLLoader` dentro de um controller
- Para passar dados entre telas, use a sobrecarga com `Consumer`:

```java
App.setRoot("quiz", (QuizController c) -> c.iniciar(nome, pin));
```

- `initialize()` sem argumentos pertence ao JavaFX — o FXMLLoader chama sozinho.
  Para inicialização com dados, use outro nome (`iniciar`)

## Documentação

Documento só entra em `docs/` se continuar verdadeiro daqui a um mês.

Relatórios do tipo "o que fizemos hoje" apodrecem: quatro deles foram removidos
do projeto descrevendo telas e classes que não existiam mais, e um deles
afirmava ter corrigido caminhos de imagem enquanto o `quiz.fxml` seguia
quebrado. Para "o que mudou", use `git log`.
