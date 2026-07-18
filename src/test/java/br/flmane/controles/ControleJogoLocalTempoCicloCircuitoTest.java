package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;

/**
 * {@code ciclo} deixou de vir de circuitos.properties (ver
 * circuito-info-editor): {@code tempoCicloCircuito()} agora é um repasse
 * direto de {@code circuito.getCiclo()}.
 */
class ControleJogoLocalTempoCicloCircuitoTest {

    @Test
    void tempoCicloCircuito_retornaCicloDoCircuitoCarregado() throws Exception {
        ControleJogoLocal controle = new ControleJogoLocal(1L);
        Circuito circuito = new Circuito();
        circuito.setCiclo(233);
        controle.circuito = circuito;

        assertEquals(233, controle.tempoCicloCircuito());
    }
}
