package br.com.grupo2.sabixao.sabixao.model;

/**
 * Classe modelo para representar um quiz
 */
public class Quiz {
    private String pin;
    private String nome;
    private String descricao;
    private boolean ativo;
    private int totalPerguntas;
    private int tempoPorPergunta; // em segundos

    public Quiz() {
    }

    public Quiz(String pin, String nome) {
        this.pin = pin;
        this.nome = nome;
        this.ativo = true;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public int getTotalPerguntas() {
        return totalPerguntas;
    }

    public void setTotalPerguntas(int totalPerguntas) {
        this.totalPerguntas = totalPerguntas;
    }

    public int getTempoPorPergunta() {
        return tempoPorPergunta;
    }

    public void setTempoPorPergunta(int tempoPorPergunta) {
        this.tempoPorPergunta = tempoPorPergunta;
    }

    @Override
    public String toString() {
        return "Quiz{" +
                "pin='" + pin + '\'' +
                ", nome='" + nome + '\'' +
                ", ativo=" + ativo +
                ", totalPerguntas=" + totalPerguntas +
                '}';
    }
}
