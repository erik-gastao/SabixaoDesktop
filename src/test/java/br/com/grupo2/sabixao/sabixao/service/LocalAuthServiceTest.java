package br.com.grupo2.sabixao.sabixao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cadastro local que substitui o backend enquanto ele não existe.
 *
 * O que estes testes garantem é justamente o que as telas não faziam antes:
 * senha errada não entra e conta inexistente não entra.
 */
class LocalAuthServiceTest {

    private final LocalAuthService auth = LocalAuthService.getInstance();

    @BeforeEach
    void limparCadastro() {
        auth.clear();
    }

    @Test
    @DisplayName("conta criada consegue entrar com a senha certa")
    void fluxoFeliz() {
        assertTrue(auth.register("Erik", "senha123"));
        assertTrue(auth.authenticate("Erik", "senha123").isPresent());
        assertTrue(auth.getAuthenticated().isPresent());
        assertEquals("Erik", auth.getAuthenticated().get().getNome());
    }

    @Test
    @DisplayName("senha errada é recusada")
    void senhaErradaNaoEntra() {
        auth.register("Erik", "senha123");
        assertTrue(auth.authenticate("Erik", "outrasenha").isEmpty());
        assertTrue(auth.getAuthenticated().isEmpty());
    }

    @Test
    @DisplayName("conta que não existe é recusada")
    void contaInexistenteNaoEntra() {
        assertTrue(auth.authenticate("Ninguem", "senha123").isEmpty());
    }

    @Test
    @DisplayName("nome duplicado não sobrescreve a conta existente")
    void nomeDuplicadoERecusado() {
        assertTrue(auth.register("Erik", "senha123"));
        assertFalse(auth.register("Erik", "senhaNova"));
        assertEquals(1, auth.countAccounts());
        // a senha original continua valendo
        assertTrue(auth.authenticate("Erik", "senha123").isPresent());
    }

    @Test
    @DisplayName("nome não diferencia maiúsculas nem espaços nas pontas")
    void nomeNormalizado() {
        auth.register("Erik", "senha123");
        assertTrue(auth.exists("erik"));
        assertTrue(auth.exists("  ERIK  "));
        assertFalse(auth.register("ERIK", "outra"));
        assertTrue(auth.authenticate("erik", "senha123").isPresent());
    }

    @Test
    void logoutDeslogaMasNaoApagaAConta() {
        auth.register("Erik", "senha123");
        auth.authenticate("Erik", "senha123");
        auth.logout();

        assertTrue(auth.getAuthenticated().isEmpty());
        assertTrue(auth.authenticate("Erik", "senha123").isPresent());
    }
}
