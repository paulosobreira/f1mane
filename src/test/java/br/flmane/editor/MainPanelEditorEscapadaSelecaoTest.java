package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPista;

/**
 * Uma vez criada, uma ObjetoEscapada não tem NENHUMA interação por clique no
 * canvas do editor — nem seleção, nem arraste do objeto inteiro, nem menu de
 * contexto (clique direito), nem diálogo de propriedades (duplo clique). A
 * única forma de interagir com ela depois de criada é pela lista de objetos
 * (botão "Remover" ou tecla Delete, que não passam por hit-testing de clique
 * no canvas). encontraObjetoPista/encontraObjetoPistaNaLista é o ponto único
 * de hit-testing por clique no canvas, então excluir ObjetoEscapada ali
 * desarma todos os fluxos de uma vez (mousePressed, menu de contexto,
 * duplo-clique).
 */
class MainPanelEditorEscapadaSelecaoTest {

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
    }

    private static void enviarEvento(MainPanelEditor editor, int tipo, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, tipo, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, false, botao);
        editor.dispatchEvent(evento);
    }

    /** Escapada com largura generosa, pra garantir que nem um clique bem dentro da espessura visível a selecione. */
    private ObjetoEscapada criarEscapadaQuaseHorizontal() {
        ObjetoEscapada escapada = new ObjetoEscapada();
        escapada.setNome("Escapada Teste");
        escapada.setLargura(30);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(100, 200));
        pontos.add(new Point(400, 205));
        escapada.setPontos(pontos);
        escapada.gerar();
        escapada.setPosicaoQuina(escapada.obterArea().getLocation());
        return escapada;
    }

    private MainPanelEditorTestDouble criarEditorComEscapada(ObjetoEscapada escapada) throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);
        ativarMouseListener(editor);
        return editor;
    }

    @Test
    void encontraObjetoPista_nuncaEncontraUmaEscapada_nemEmCimaDoVertice_nemDentroDaEspessura() throws Exception {
        ObjetoEscapada escapada = criarEscapadaQuaseHorizontal();
        MainPanelEditorTestDouble editor = criarEditorComEscapada(escapada);

        // Exatamente em cima do vértice de entrada.
        assertNull(editor.encontraObjetoPista(new Point(100, 200)),
                "clique em cima do vértice de entrada não deveria encontrar a escapada");
        // Dentro da espessura visível (largura=30), mas fora do retângulo bruto dos vértices.
        assertNull(editor.encontraObjetoPista(new Point(250, 185)),
                "clique dentro da espessura visível do traçado não deveria encontrar a escapada");
        // Bem fora de qualquer área do objeto.
        assertNull(editor.encontraObjetoPista(new Point(250, 50)),
                "clique fora da escapada, como esperado, não deveria encontrar nada");
    }

    @Test
    void mousePressed_naoSelecionaNemArmaArrasteDeUmaEscapadaJaCriada() throws Exception {
        ObjetoEscapada escapada = criarEscapadaQuaseHorizontal();
        MainPanelEditorTestDouble editor = criarEditorComEscapada(escapada);
        Point posicaoQuinaAntes = new Point(escapada.getPosicaoQuina());

        Point cliqueNaEspessura = new Point(250, 185);
        enviarEvento(editor, MouseEvent.MOUSE_PRESSED, cliqueNaEspessura, MouseEvent.BUTTON1);

        assertNull(editor.getObjetoPista(), "mousePressed sobre a escapada não deveria selecioná-la");

        Point destino = new Point(300, 235);
        enviarEvento(editor, MouseEvent.MOUSE_DRAGGED, destino, MouseEvent.BUTTON1);

        assertEquals(posicaoQuinaAntes, escapada.getPosicaoQuina(),
                "sem seleção, arrastar o mouse não deveria mover a escapada");
    }

    @Test
    void cliqueDireito_naoAbreMenuDeContextoParaUmaEscapadaJaCriada() throws Exception {
        ObjetoEscapada escapada = criarEscapadaQuaseHorizontal();
        MainPanelEditorTestDouble editor = criarEditorComEscapada(escapada);

        Point cliqueNaEspessura = new Point(250, 185);
        enviarEvento(editor, MouseEvent.MOUSE_CLICKED, cliqueNaEspessura, MouseEvent.BUTTON3);

        assertNull(editor.getObjetoPista(),
                "clique direito sobre a escapada não deveria selecioná-la nem abrir o menu de contexto");
    }

    /**
     * ObjetoEscapada não tem propriedades editáveis pelo diálogo de
     * duplo-clique (Largura já está no painel rápido do menu de contexto, e
     * a cor é fixa) — testado via o predicado
     * {@link MainPanelEditor#temPropriedadesEditaveisPorDialogo(ObjetoPista)}
     * em vez de exercitar {@code editaObjetoPista}/{@code FormularioObjetos}
     * de verdade, que abriria um JOptionPane real (proibido em teste, ver
     * CLAUDE.md).
     */
    @Test
    void temPropriedadesEditaveisPorDialogo_falseParaEscapada_trueParaOutrosTipos() {
        MainPanelEditor editor = new MainPanelEditor();

        assertFalse(editor.temPropriedadesEditaveisPorDialogo(new ObjetoEscapada()),
                "ObjetoEscapada não deveria ter propriedades editáveis por esse diálogo");
        assertTrue(editor.temPropriedadesEditaveisPorDialogo(new ObjetoLivre()),
                "outros tipos de objeto continuam com propriedades editáveis por esse diálogo");
    }
}
