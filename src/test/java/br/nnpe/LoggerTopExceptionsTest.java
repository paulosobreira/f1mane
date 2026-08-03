package br.nnpe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Cobre o limite de crescimento de {@link Logger#topExceptions}, mapa estático
 * que acompanha toda a vida do processo servidor. Antes, ao encher, ele parava
 * de registrar qualquer exceção nova e o diagnóstico ficava congelado no começo
 * do uptime; agora descarta a entrada mais antiga e continua registrando, sem
 * ultrapassar o teto.
 */
class LoggerTopExceptionsTest {

    @BeforeEach
    void limparMapa() {
        Logger.topExceptions.clear();
    }

    @Test
    void acimaDoLimite_mapaNaoCresceAlemDoTeto() {
        int excedente = 50;

        for (int i = 0; i < Logger.MAX_TOP_EXCEPTIONS + excedente; i++) {
            Logger.topExecpts(excecaoComAssinaturaUnica(i));
        }

        assertEquals(Logger.MAX_TOP_EXCEPTIONS, Logger.topExceptions.size());
    }

    @Test
    void acimaDoLimite_mantemAsAssinaturasMaisRecentes() {
        int excedente = 5;

        for (int i = 0; i < Logger.MAX_TOP_EXCEPTIONS + excedente; i++) {
            Logger.topExecpts(excecaoComAssinaturaUnica(i));
        }

        assertTrue(contemAssinatura(Logger.MAX_TOP_EXCEPTIONS + excedente - 1),
                "a exceção mais recente deve estar registrada");
        assertFalse(contemAssinatura(0),
                "a assinatura mais antiga deve ter sido descartada");
    }

    @Test
    void mesmaAssinaturaRepetida_incrementaContagemSemNovaEntrada() {
        Exception excecao = excecaoComAssinaturaUnica(1);

        Logger.topExecpts(excecao);
        Logger.topExecpts(excecao);
        Logger.topExecpts(excecao);

        assertEquals(1, Logger.topExceptions.size());
        assertEquals(3, Logger.topExceptions.values().iterator().next());
    }

    private boolean contemAssinatura(int identificador) {
        String marca = "assinatura-" + identificador + " ";
        for (String chave : Logger.topExceptions.keySet()) {
            if (chave.contains(marca)) {
                return true;
            }
        }
        return false;
    }

    /**
     * A chave do mapa é montada a partir da mensagem e do stack trace, então
     * basta variar a mensagem para produzir assinaturas distintas.
     */
    private Exception excecaoComAssinaturaUnica(int identificador) {
        return new IllegalStateException("assinatura-" + identificador + " ");
    }
}
