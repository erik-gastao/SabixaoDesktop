package br.com.grupo2.sabixao.sabixao.model;

/**
 * Classe modelo para representar um usuário do sistema
 */
public class User {
    private String nome;
    private String senha;
    private int pontuacao;

    public User() {
    }

    public User(String nome, String senha) {
        this.nome = nome;
        this.senha = senha;
        this.pontuacao = 0;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }

    @Override
    public String toString() {
        return "User{" +
                "nome='" + nome + '\'' +
                ", pontuacao=" + pontuacao +
                '}';
    }
}
