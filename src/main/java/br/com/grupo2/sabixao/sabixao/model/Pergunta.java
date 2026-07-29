package br.com.grupo2.sabixao.sabixao.model;

import java.util.List;

/**
 * Classe modelo para representar uma pergunta do quiz
 */
public class Question {
    private Long id;
    private String texto;
    private List<String> opcoes;
    private int respostaCorreta; // índice da resposta correta (0-3)
    private String categoria;
    private String dificuldade; // FACIL, MEDIO, DIFICIL

    public Question() {
    }

    public Question(String texto, List<String> opcoes, int respostaCorreta) {
        this.texto = texto;
        this.opcoes = opcoes;
        this.respostaCorreta = respostaCorreta;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public List<String> getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(List<String> opcoes) {
        this.opcoes = opcoes;
    }

    public int getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(int respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDificuldade() {
        return dificuldade;
    }

    public void setDificuldade(String dificuldade) {
        this.dificuldade = dificuldade;
    }

    public boolean verificarResposta(int indiceResposta) {
        return indiceResposta == respostaCorreta;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", texto='" + texto + '\'' +
                ", categoria='" + categoria + '\'' +
                ", dificuldade='" + dificuldade + '\'' +
                '}';
    }
}
