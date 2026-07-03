package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;

import org.junit.jupiter.api.Test;

/**
 * Cobre a haste simétrica de PontoCurva: sem haste ajustada os dois pontos de
 * controle degeneram para a própria posição (segmento reto); com haste
 * ajustada, o controle de entrada é o espelho matemático do controle de
 * saída em torno da posição do vértice.
 */
class PontoCurvaTest {

    @Test
    void semHaste_controleSaidaEEntradaIgualAPosicao() {
        PontoCurva ponto = new PontoCurva(new Point(10, 20));

        assertEquals(new Point(10, 20), ponto.getControleSaida());
        assertEquals(new Point(10, 20), ponto.getControleEntrada());
    }

    @Test
    void hasteIgualPosicao_tratadaComoSemHaste() {
        PontoCurva ponto = new PontoCurva(new Point(10, 20));
        ponto.setHasteFim(new Point(10, 20));

        assertEquals(new Point(10, 20), ponto.getControleSaida());
        assertEquals(new Point(10, 20), ponto.getControleEntrada());
    }

    @Test
    void hasteAjustada_controleSaidaEAPropriaHaste() {
        PontoCurva ponto = new PontoCurva(new Point(0, 0));
        ponto.setHasteFim(new Point(30, -40));

        assertEquals(new Point(30, -40), ponto.getControleSaida());
    }

    @Test
    void hasteAjustada_controleEntradaEEspelhoDaHasteEmTornoDaPosicao() {
        PontoCurva ponto = new PontoCurva(new Point(0, 0));
        ponto.setHasteFim(new Point(30, -40));

        assertEquals(new Point(-30, 40), ponto.getControleEntrada());
    }

    @Test
    void hasteAjustada_comPosicaoNaoNaOrigem_espelhaCorretamente() {
        PontoCurva ponto = new PontoCurva(new Point(100, 100));
        ponto.setHasteFim(new Point(130, 60));

        // espelho = 2*posicao - haste
        assertEquals(new Point(70, 140), ponto.getControleEntrada());
    }
}
