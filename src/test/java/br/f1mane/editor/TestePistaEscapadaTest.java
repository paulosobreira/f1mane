package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.PontoEscape;

/**
 * Cobre o carro de teste do editor (TestePista) seguindo o traçado de
 * escapada quando o índice atual cai dentro de uma zona de escapada, em vez
 * de continuar sempre pela pista normal.
 */
class TestePistaEscapadaTest {

    private Circuito circuitoComEscapada() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000));
        pista.add(criarNo(3000, 1000));
        pista.add(criarNo(3000, 3000));
        pista.add(criarNo(1000, 3000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);

        List<ObjetoPista> objetos = new ArrayList<>();
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setPosicaoQuina(new Point(1500, 985));
        objetos.add(escapada);
        circuito.setObjetos(objetos);

        circuito.vetorizarPista();
        return circuito;
    }

    private No criarNo(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    @Test
    void dentroDaZonaDeEscapada_comModoEscapadaLigado_carroDeTesteSeguePontosDaEscapada() {
        Circuito circuito = circuitoComEscapada();
        Map<PontoEscape, List<No>> escapeMap = circuito.getEscapeMap();
        Map.Entry<PontoEscape, List<No>> entrada = escapeMap.entrySet().iterator().next();
        List<No> tracadoEscapada = entrada.getValue();

        int indiceDentroDaZona = -1;
        for (int i = 0; i < tracadoEscapada.size(); i++) {
            if (tracadoEscapada.get(i) != null) {
                indiceDentroDaZona = i;
                break;
            }
        }
        // Pula pra um pouco depois do início, onde o afastamento já é
        // perceptível (perto do início a curva senoidal ainda está perto de 0).
        indiceDentroDaZona += 50;

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(indiceDentroDaZona, pontosPista);

        Point pontoEscapada = tracadoEscapada.get(indiceDentroDaZona).getPoint();
        Point pontoPistaNormal = pontosPista.get(indiceDentroDaZona).getPoint();

        assertEquals(pontoEscapada, testePista.getTestCar());
        assertNotEquals(pontoPistaNormal, testePista.getTestCar());
    }

    @Test
    void dentroDaZonaDeEscapada_comModoEscapadaDesligado_carroDeTesteUsaPistaNormal() {
        Circuito circuito = circuitoComEscapada();
        Map<PontoEscape, List<No>> escapeMap = circuito.getEscapeMap();
        Map.Entry<PontoEscape, List<No>> entrada = escapeMap.entrySet().iterator().next();
        List<No> tracadoEscapada = entrada.getValue();

        int indiceDentroDaZona = -1;
        for (int i = 0; i < tracadoEscapada.size(); i++) {
            if (tracadoEscapada.get(i) != null) {
                indiceDentroDaZona = i;
                break;
            }
        }
        indiceDentroDaZona += 50;

        // modoEscapada começa desligado por padrão (sem chamar testarEscapada()) —
        // o carro de teste deve ficar na pista normal mesmo dentro da zona.
        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        List<No> pontosPista = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(indiceDentroDaZona, pontosPista);

        assertEquals(pontosPista.get(indiceDentroDaZona).getPoint(), testePista.getTestCar());
    }

    @Test
    void foraDaZonaDeEscapada_carroDeTesteUsaPistaNormal() {
        Circuito circuito = circuitoComEscapada();
        List<No> pontosPista = circuito.getPistaFull();

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        // Índice bem longe do ponto de escapada (perto do fim da lista).
        int indiceForaDaZona = pontosPista.size() - 200;

        testePista.posicionaCarroConsiderandoEscapada(indiceForaDaZona, pontosPista);

        assertEquals(pontosPista.get(indiceForaDaZona).getPoint(), testePista.getTestCar());
    }
}
