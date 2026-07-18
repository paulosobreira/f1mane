package br.flmane.visao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;
import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.Piloto;

/**
 * Regressão: PainelCircuito.centralizaCarroDesenhar lançava
 * NullPointerException ("p5 is null") ao renderizar um piloto que está sobre
 * um nó do box (onde p4/p5 nunca eram calculados) mas ainda carrega
 * tracadoAntigo=5 (faixa de fuga) de antes de entrar no box — o ramo
 * `piloto.getTracado() == 2` usa `p5.x`/`p5.y` nesse caso. Reproduzida em
 * jogo real: vários pilotos entrando no box na mesma volta derrubavam o
 * render em loop.
 */
class PainelCircuitoCentralizaCarroBoxTest {

    private No noComPonto(int index, int x, int y) {
        No no = new No();
        no.setIndex(index);
        no.setPoint(new Point(x, y));
        no.setTipo(No.RETA);
        return no;
    }

    private Circuito circuitoVetorizado() {
        Circuito circuito = new Circuito();
        List<No> pista = new ArrayList<>();
        pista.add(noComPonto(0, 1000, 1000));
        pista.add(noComPonto(0, 4000, 1000));
        pista.add(noComPonto(0, 4000, 4000));
        pista.add(noComPonto(0, 1000, 4000));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());
        circuito.setMultiplicadorLarguraPista(1.5);
        circuito.vetorizarPista();
        return circuito;
    }

    @Test
    void carroNoBoxVindoDoTracadoDeFuga_naoLancaExcecao() {
        Circuito circuito = circuitoVetorizado();
        circuito.getBox1Full().add(noComPonto(0, 100, 100));
        circuito.getBox2Full().add(noComPonto(0, 200, 200));

        No noAtual = noComPonto(0, 150, 150);
        noAtual.setBox(true);

        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.isModoQualify()).thenReturn(false);
        List<No> lista = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            lista.add(noComPonto(i, i, i));
        }
        when(controleJogo.obterPista(noAtual)).thenReturn(lista);
        when(controleJogo.getNosDoBox()).thenReturn(new ArrayList<>());

        Piloto piloto = new Piloto();
        piloto.setNoAtual(noAtual);
        piloto.setTracado(2);
        piloto.setTracadoAntigo(5);
        piloto.setIndiceTracado(5);

        PainelCircuito painel = new PainelCircuito(circuito, controleJogo);

        assertDoesNotThrow(() -> painel.centralizaCarroDesenhar(controleJogo, piloto));
    }
}
