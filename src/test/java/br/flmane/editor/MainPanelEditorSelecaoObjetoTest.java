package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPista;

/**
 * Prioridade de seleção por clique quando as áreas de dois objetos se
 * sobrepõem: o mais recente (adicionado por último na lista) deve ganhar,
 * porque é ele que é desenhado por cima do mais antigo. Reproduz o relato:
 * "Objeto 6" grande sobrepondo "Objeto 7" menor — clicar na área do 7
 * selecionava o 6 (mais antigo), quando deveria selecionar o 7.
 */
class MainPanelEditorSelecaoObjetoTest {

    private ObjetoLivre criarRetangulo(String nome, int x, int y, int largura, int altura) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome(nome);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(x, y));
        pontos.add(new Point(x + largura, y));
        pontos.add(new Point(x + largura, y + altura));
        pontos.add(new Point(x, y + altura));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        return objeto;
    }

    @Test
    void clickEmAreaSobreposta_prioriza_objetoMaisRecenteDaLista() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();

        // "Objeto 6": grande, cobre toda a área do "Objeto 7", adicionado antes.
        ObjetoLivre objeto6 = criarRetangulo("Objeto 6", 0, 0, 1000, 1000);
        // "Objeto 7": pequeno, totalmente dentro da área do 6, adicionado depois.
        ObjetoLivre objeto7 = criarRetangulo("Objeto 7", 400, 400, 100, 100);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto6);
        objetos.add(objeto7);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);

        ObjetoPista encontrado = editor.encontraObjetoPista(new Point(450, 450));

        assertSame(objeto7, encontrado, "clique na área sobreposta deveria selecionar o objeto mais recente (7)");
    }

    @Test
    void clickForaDaAreaDoMaisRecente_aindaSelecionaOMaisAntigo() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();

        ObjetoLivre objeto6 = criarRetangulo("Objeto 6", 0, 0, 1000, 1000);
        ObjetoLivre objeto7 = criarRetangulo("Objeto 7", 400, 400, 100, 100);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto6);
        objetos.add(objeto7);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);

        // Ponto dentro do 6 mas fora da área (com tolerância) do 7.
        ObjetoPista encontrado = editor.encontraObjetoPista(new Point(10, 10));

        assertSame(objeto6, encontrado, "fora da área do 7, o clique ainda deveria alcançar o 6");
    }

    @Test
    void semSobreposicao_selecionaOObjetoCorreto() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();

        ObjetoLivre objetoA = criarRetangulo("A", 0, 0, 100, 100);
        ObjetoLivre objetoB = criarRetangulo("B", 500, 500, 100, 100);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objetoA);
        objetos.add(objetoB);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);

        assertSame(objetoA, editor.encontraObjetoPista(new Point(50, 50)));
        assertSame(objetoB, editor.encontraObjetoPista(new Point(550, 550)));
    }
}
