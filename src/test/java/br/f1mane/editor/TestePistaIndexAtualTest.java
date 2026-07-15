package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoPista;

/**
 * Cobre a mudança transparencia-intervalo-safetycar-preview: TestePista
 * passa a expor indexAtual/estaNoBox, atualizado junto de testCar em
 * posicionaCarro, posicionaCarroConsiderandoEscapada e posicionaCarroBox —
 * usado pelo preview de teste do editor para respeitar o mesmo filtro de
 * intervalo/box já aplicado ao piloto e ao safety car.
 */
class TestePistaIndexAtualTest {

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(no(1000, 1000));
        pista.add(no(4000, 1000));
        pista.add(no(4000, 4000));
        pista.add(no(1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista();
        return circuito;
    }

    private No no(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    @Test
    void posicionaCarro_defineIndexAtualEEstaNoBoxFalse() {
        Circuito circuito = circuitoVetorizado();
        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        List<No> pistaFull = circuito.getPistaFull();

        testePista.posicionaCarro(500, pistaFull.get(500), pistaFull);

        assertEquals(500, testePista.getIndexAtual());
        assertFalse(testePista.isEstaNoBox());
    }

    @Test
    void posicionaCarroBox_defineIndexAtualEEstaNoBoxTrue() {
        Circuito circuito = circuitoVetorizado();
        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        List<No> pistaFull = circuito.getPistaFull();

        testePista.posicionaCarroBox(300, pistaFull.get(300), pistaFull);

        assertEquals(300, testePista.getIndexAtual());
        assertTrue(testePista.isEstaNoBox());
    }

    @Test
    void posicionaCarroConsiderandoEscapada_semModoEscapada_delegaParaPosicionaCarro() {
        Circuito circuito = circuitoVetorizado();
        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        List<No> pistaFull = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(700, pistaFull);

        assertEquals(700, testePista.getIndexAtual());
        assertFalse(testePista.isEstaNoBox());
    }

    @Test
    void posicionaCarroConsiderandoEscapada_comModoEscapadaAtivo_indexAtualContinuaPistaENaoBox() {
        Circuito circuito = circuitoVetorizado();
        List<No> pistaFull = circuito.getPistaFull();

        ObjetoEscapada escapada = new ObjetoEscapada();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(1500, 1000));
        pontos.add(new Point(2000, 1000));
        escapada.setPontos(pontos);
        escapada.setIndiceEntrada(500);
        escapada.setIndiceSaida(900);
        escapada.gerar();
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        circuito.setObjetos(objetos);

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        testePista.testarEscapada();

        testePista.posicionaCarroConsiderandoEscapada(700, pistaFull);

        assertEquals(700, testePista.getIndexAtual());
        assertFalse(testePista.isEstaNoBox());
    }
}
