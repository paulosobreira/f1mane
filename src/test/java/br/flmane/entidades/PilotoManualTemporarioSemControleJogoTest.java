package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

/**
 * Regressão: Piloto.isManualTemporario() passou a delegar pra
 * ControleAutomacao (via InterfaceJogo) quando a lógica de automação foi
 * extraída de Piloto. Pilotos "modelo"/sem partida associada (ex.:
 * TemporadasDefault.pilotos, usados pra listar pilotos disponíveis por
 * temporada no endpoint REST) nunca têm controleJogo setado, e o Jackson
 * chama esse getter ao serializar esses objetos — sem o guard de nulo,
 * isso derrubava a resposta do endpoint com um NullPointerException 400.
 */
class PilotoManualTemporarioSemControleJogoTest {

    @Test
    void isManualTemporario_semControleJogo_naoLancaExcecao() {
        Piloto piloto = new Piloto();

        assertDoesNotThrow(piloto::isManualTemporario);
        assertFalse(piloto.isManualTemporario());
    }

    @Test
    void setManualTemporario_semControleJogo_naoLancaExcecao() {
        Piloto piloto = new Piloto();
        piloto.setJogadorHumano(true);

        assertDoesNotThrow(piloto::setManualTemporario);
    }
}
