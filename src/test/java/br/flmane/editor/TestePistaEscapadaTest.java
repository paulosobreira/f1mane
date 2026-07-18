package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoPista;

/**
 * Cobre o carro de teste do editor (TestePista) com o novo modelo de
 * ObjetoEscapada (entrada → trajeto livre → saída, ancorados a
 * indiceEntrada/indiceSaida no traçado em que a escapada foi definida): com
 * modoEscapada ligado e o índice da pista dentro de [indiceEntrada,
 * indiceSaida] de alguma escapada, o carro de teste segue o trajeto
 * interpolado da escapada em vez da pista normal; fora desse intervalo, ou
 * com modoEscapada desligado, continua usando a pista normal.
 */
class TestePistaEscapadaTest {

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

    /** Escapada válida: entrada/saída em nós reais do traçado 1, com um ponto livre desviado no meio. */
    private ObjetoEscapada criarEscapada(No noEntrada, No noSaida) {
        ObjetoEscapada escapada = new ObjetoEscapada();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(noEntrada.getPoint()));
        pontos.add(new Point(noEntrada.getPoint().x + 200, noEntrada.getPoint().y + 200));
        pontos.add(new Point(noSaida.getPoint()));
        escapada.setPontos(pontos);
        escapada.setTracadoOrigem(1);
        escapada.setIndiceEntrada(noEntrada.getIndex());
        escapada.setIndiceSaida(noSaida.getIndex());
        escapada.gerar();
        escapada.setPosicaoQuina(escapada.obterArea().getLocation());
        return escapada;
    }

    private void anexarEscapada(Circuito circuito, ObjetoEscapada escapada) {
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        circuito.setObjetos(objetos);
    }

    @Test
    void dentroDaZonaComModoEscapadaLigado_carroSegueOTrajetoDaEscapada() {
        Circuito circuito = circuitoVetorizado();
        No noEntrada = circuito.getPista1Full().get(300);
        No noSaida = circuito.getPista1Full().get(340);
        anexarEscapada(circuito, criarEscapada(noEntrada, noSaida));

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();

        testePista.posicionaCarroConsiderandoEscapada(noEntrada.getIndex(), pontosPista);
        assertEquals(noEntrada.getPoint(), testePista.getTestCar(),
                "no índice de entrada, o carro deveria estar exatamente no primeiro ponto do trajeto da escapada");

        testePista.posicionaCarroConsiderandoEscapada(noSaida.getIndex(), pontosPista);
        assertEquals(noSaida.getPoint(), testePista.getTestCar(),
                "no índice de saída, o carro deveria estar exatamente no último ponto do trajeto da escapada");

        int indiceMeio = (noEntrada.getIndex() + noSaida.getIndex()) / 2;
        testePista.posicionaCarroConsiderandoEscapada(indiceMeio, pontosPista);
        assertNotEquals(pontosPista.get(indiceMeio).getPoint(), testePista.getTestCar(),
                "no meio da zona, o carro deveria estar seguindo o trajeto desviado da escapada, não a pista normal");
    }

    @Test
    void dentroDaZonaComModoEscapadaDesligado_carroDeTesteUsaPistaNormal() {
        Circuito circuito = circuitoVetorizado();
        No noEntrada = circuito.getPista1Full().get(300);
        No noSaida = circuito.getPista1Full().get(340);
        anexarEscapada(circuito, criarEscapada(noEntrada, noSaida));

        // modoEscapada começa desligado por padrão (sem chamar testarEscapada()).
        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        List<No> pontosPista = circuito.getPistaFull();
        int indiceMeio = (noEntrada.getIndex() + noSaida.getIndex()) / 2;

        testePista.posicionaCarroConsiderandoEscapada(indiceMeio, pontosPista);

        assertEquals(pontosPista.get(indiceMeio).getPoint(), testePista.getTestCar());
    }

    @Test
    void foraDaZonaDeEscapada_carroDeTesteUsaPistaNormal() {
        Circuito circuito = circuitoVetorizado();
        No noEntrada = circuito.getPista1Full().get(300);
        No noSaida = circuito.getPista1Full().get(340);
        anexarEscapada(circuito, criarEscapada(noEntrada, noSaida));

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();
        int indiceForaDaZona = pontosPista.size() - 200;

        testePista.posicionaCarroConsiderandoEscapada(indiceForaDaZona, pontosPista);

        assertEquals(pontosPista.get(indiceForaDaZona).getPoint(), testePista.getTestCar());
    }

    @Test
    void escapadaSemIndicesValidos_naoAtivaEUsaPistaNormal() {
        // Objeto degenerado (nunca teve entrada/saída validadas com sucesso,
        // indiceEntrada/indiceSaida ficam no default -1) não deveria travar
        // nem ser tratado como zona ativa em nenhum índice.
        Circuito circuito = circuitoVetorizado();
        ObjetoEscapada escapadaDegenerada = new ObjetoEscapada();
        anexarEscapada(circuito, escapadaDegenerada);

        TestePista testePista = new TestePista(new MainPanelEditor(), circuito);
        testePista.testarEscapada();
        List<No> pontosPista = circuito.getPistaFull();
        int indice = 300;

        testePista.posicionaCarroConsiderandoEscapada(indice, pontosPista);

        assertEquals(pontosPista.get(indice).getPoint(), testePista.getTestCar());
    }
}
