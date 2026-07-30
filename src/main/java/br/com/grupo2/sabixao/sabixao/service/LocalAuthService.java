package br.com.grupo2.sabixao.sabixao.service;

import br.com.grupo2.sabixao.sabixao.model.Usuario;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cadastro de usuários em memória, para o app rodar sem backend.
 *
 * Antes as telas de login e criar-conta apenas validavam o formato dos campos e
 * anunciavam "sucesso" sem verificar nada — dava para entrar com qualquer nome e
 * qualquer senha. Aqui a conta criada realmente passa a existir e o login de
 * verdade recusa credencial errada.
 *
 * Os dados vivem só enquanto o app está aberto: fechar a janela apaga tudo.
 * Quando existir backend, esta classe é o ponto único a trocar — os métodos HTTP
 * equivalentes já estão em {@link ApiService}.
 */
public final class LocalAuthService {

    /** Mesmo mínimo que as telas já exigiam antes desta classe. */
    public static final int MIN_NAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;

    private static final LocalAuthService INSTANCE = new LocalAuthService();

    // Chave é o nome em minúsculas: "Erik" e "erik" são a mesma conta.
    private final Map<String, Usuario> accounts = new ConcurrentHashMap<>();

    private Usuario authenticated;

    private LocalAuthService() {
    }

    public static LocalAuthService getInstance() {
        return INSTANCE;
    }

    /**
     * Registra uma conta nova.
     *
     * @return false se já existe conta com esse nome
     */
    public boolean register(String nome, String senha) {
        return accounts.putIfAbsent(key(nome), new Usuario(nome, senha)) == null;
    }

    public boolean exists(String nome) {
        return accounts.containsKey(key(nome));
    }

    /**
     * Confere nome e senha. Em caso de acerto o usuário fica marcado como
     * autenticado na sessão.
     */
    public Optional<Usuario> authenticate(String nome, String senha) {
        Usuario account = accounts.get(key(nome));
        if (account == null || !account.getSenha().equals(senha)) {
            return Optional.empty();
        }
        authenticated = account;
        return Optional.of(account);
    }

    /** Usuário logado nesta sessão, se houver. */
    public Optional<Usuario> getAuthenticated() {
        return Optional.ofNullable(authenticated);
    }

    public void logout() {
        authenticated = null;
    }

    /** Quantidade de contas cadastradas — usado nos testes. */
    public int countAccounts() {
        return accounts.size();
    }

    /** Zera o cadastro. Existe para os testes não vazarem estado entre si. */
    public void clear() {
        accounts.clear();
        authenticated = null;
    }

    private String key(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase();
    }
}
