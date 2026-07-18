package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.ObjetoGuardRails;
import br.flmane.entidades.ObjetoPista;

/**
 * Ao posicionar um ponto de ObjetoGuardRails (na criação ou arrastando um
 * ponto já existente), o editor aproxima (snap) o ponto de um nó de pista ou
 * de um ponto de outro objeto de cenário próximo, dentro de uma tolerância em
 * pixels de tela — evita que o usuário precise acertar o pixel exato pra
 * encostar a barreira num nó existente.
 */
class MainPanelEditorGuardRailsSnapTest {

    private No criarNo(int x, int y) {
        No no = new No();
        no.setPoint(new Point(x, y));
        return no;
    }

    @Test
    void candidatoDentroDaToleranciaDeUmNo_faZSnapParaONo() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setPista(new ArrayList<>(Arrays.asList(criarNo(100, 100))));
        editor.setCircuito(circuito);

        Point resultado = editor.aplicaSnap(new Point(104, 103), new ObjetoGuardRails());

        assertEquals(new Point(100, 100), resultado);
    }

    @Test
    void candidatoForaDaToleranciaDeQualquerNo_naoSofreSnap() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setPista(new ArrayList<>(Arrays.asList(criarNo(100, 100))));
        editor.setCircuito(circuito);

        Point candidato = new Point(200, 200);
        Point resultado = editor.aplicaSnap(candidato, new ObjetoGuardRails());

        assertEquals(candidato, resultado);
    }

    @Test
    void noENoOutroObjetoCompetem_escolheOMaisProximo() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setPista(new ArrayList<>(Arrays.asList(criarNo(100, 100))));

        ObjetoGuardRails outroGuardRails = new ObjetoGuardRails();
        outroGuardRails.setPontos(new ArrayList<>(Arrays.asList(new Point(106, 100))));
        outroGuardRails.gerar();
        outroGuardRails.setPosicaoQuina(outroGuardRails.obterArea().getLocation());

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(outroGuardRails);
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        // (101,100): nó em (100,100) está a 1px; ponto do outro guard rails em (106,100) está a 5px —
        // o nó, mais próximo, deveria vencer.
        Point resultado = editor.aplicaSnap(new Point(101, 100), new ObjetoGuardRails());

        assertEquals(new Point(100, 100), resultado);
    }

    @Test
    void ignoraPontosDoProprioObjetoEmEdicao() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();

        ObjetoGuardRails emEdicao = new ObjetoGuardRails();
        emEdicao.setPontos(new ArrayList<>(Arrays.asList(new Point(50, 50), new Point(60, 60))));
        emEdicao.gerar();
        emEdicao.setPosicaoQuina(emEdicao.obterArea().getLocation());

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(emEdicao);
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        Point candidato = new Point(51, 51);
        Point resultado = editor.aplicaSnap(candidato, emEdicao);

        assertEquals(candidato, resultado, "não deveria atrair o próprio objeto passado como 'ignorar'");
    }
}
