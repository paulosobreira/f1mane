package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre a reconexão de {@code Circuito.gerarEscapeMap()} ao novo modelo de
 * {@link ObjetoEscapada}: {@code pista4Full}/{@code pista5Full} passam a ser
 * derivadas dos objetos do circuito (ao invés de sempre nulas), com o
 * mapeamento tracadoOrigem==1 → traçado 5 / tracadoOrigem==2 → traçado 4
 * exigido pelas regras de retorno de {@code Piloto.mudarTracado} (só permite
 * voltar de 4 pra 2 e de 5 pra 1) e pelo antigo {@code escapaTracado()}.
 */
class CircuitoEscapadaTracadoTest {

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000));
        pista.add(criarNo(4000, 1000));
        pista.add(criarNo(4000, 4000));
        pista.add(criarNo(1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista();
        return circuito;
    }

    private No criarNo(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    private ObjetoEscapada criarEscapada(int tracadoOrigem, List<Point> pontos, int indiceEntrada, int indiceSaida) {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setTracadoOrigem(tracadoOrigem);
        escapada.setPontos(pontos);
        escapada.setIndiceEntrada(indiceEntrada);
        escapada.setIndiceSaida(indiceSaida);
        return escapada;
    }

    @Test
    void escapadaNoTracado1_populaSoPista5FullNoIntervaloCerto() {
        Circuito circuito = circuitoVetorizado();
        No noEntrada = circuito.getPista1Full().get(300);
        No noSaida = circuito.getPista1Full().get(360);
        List<Point> pontos = new ArrayList<>();
        pontos.add(noEntrada.getPoint());
        pontos.add(noSaida.getPoint());
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(criarEscapada(1, pontos, 300, 360));
        circuito.setObjetos(objetos);

        circuito.reprocessarEscapadas();

        for (int i = 0; i < circuito.getPista5Full().size(); i++) {
            if (i >= 300 && i <= 360) {
                assertNotNull(circuito.getPista5Full().get(i), "índice " + i + " deveria estar populado (dentro da zona)");
            } else {
                assertNull(circuito.getPista5Full().get(i), "índice " + i + " deveria continuar nulo (fora da zona)");
            }
        }
        for (No no : circuito.getPista4Full()) {
            assertNull(no, "pista4Full não deveria ser afetada por uma escapada ancorada no traçado 1");
        }
        assertEquals(noEntrada.getPoint(), circuito.getPista5Full().get(300).getPoint());
        assertEquals(noSaida.getPoint(), circuito.getPista5Full().get(360).getPoint());
    }

    @Test
    void escapadaNoTracado2_populaSoPista4FullNoIntervaloCerto() {
        Circuito circuito = circuitoVetorizado();
        No noEntrada = circuito.getPista2Full().get(500);
        No noSaida = circuito.getPista2Full().get(550);
        List<Point> pontos = new ArrayList<>();
        pontos.add(noEntrada.getPoint());
        pontos.add(noSaida.getPoint());
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(criarEscapada(2, pontos, 500, 550));
        circuito.setObjetos(objetos);

        circuito.reprocessarEscapadas();

        for (int i = 0; i < circuito.getPista4Full().size(); i++) {
            if (i >= 500 && i <= 550) {
                assertNotNull(circuito.getPista4Full().get(i), "índice " + i + " deveria estar populado (dentro da zona)");
            } else {
                assertNull(circuito.getPista4Full().get(i), "índice " + i + " deveria continuar nulo (fora da zona)");
            }
        }
        for (No no : circuito.getPista5Full()) {
            assertNull(no, "pista5Full não deveria ser afetada por uma escapada ancorada no traçado 2");
        }
        assertEquals(noEntrada.getPoint(), circuito.getPista4Full().get(500).getPoint());
        assertEquals(noSaida.getPoint(), circuito.getPista4Full().get(550).getPoint());
    }

    @Test
    void semObjetoEscapada_mantemAmbasAsListasTotalmenteNulas() {
        Circuito circuito = circuitoVetorizado();

        for (No no : circuito.getPista4Full()) {
            assertNull(no);
        }
        for (No no : circuito.getPista5Full()) {
            assertNull(no);
        }
    }

    @Test
    void escapadaComTrajetoDegenerado_naoGeraExcecaoNemPreencheNada() {
        Circuito circuito = circuitoVetorizado();
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(criarEscapada(1, new ArrayList<>(), 300, 360));
        circuito.setObjetos(objetos);

        circuito.reprocessarEscapadas();

        for (No no : circuito.getPista4Full()) {
            assertNull(no);
        }
    }
}
