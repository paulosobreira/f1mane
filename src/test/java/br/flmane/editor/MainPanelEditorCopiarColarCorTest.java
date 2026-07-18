package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoLivre;
import br.flmane.entidades.ObjetoPista;
import br.flmane.recursos.idiomas.Lang;

/**
 * Copiar Cor / Colar Cor: copia corPimaria/corSecundaria do objeto
 * selecionado na lista única de objetos e aplica em todos os objetos
 * selecionados no momento de colar — suporta colar em vários objetos de uma
 * vez, mesmo misturando itens vindos de circuito.objetos e
 * circuito.objetosCenario. Também cobre os botões "Primeiro"/"Ultimo",
 * sempre presentes na lista única.
 */
class MainPanelEditorCopiarColarCorTest {

    private ObjetoLivre criarObjeto(String nome, Color cor1, Color cor2) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome(nome);
        objeto.setCorPimaria(cor1);
        objeto.setCorSecundaria(cor2);
        return objeto;
    }

    private MainPanelEditor editorComListasPopuladas(List<ObjetoPista> objetos, List<ObjetoPista> objetosCenario) {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetos(objetos);
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
        editor.formularioListaObjetos.listarObjetos();
        return editor;
    }

    /**
     * Seleciona vários índices de uma vez sem passar pelo
     * ListSelectionListener de produção (que chama centralizarPonto() e
     * exige um MainPanelEditor totalmente montado, com scrollPane etc.) —
     * mesmo mecanismo de supressão usado por selecionarSemCentralizar(),
     * generalizado aqui só para o teste simular seleção múltipla.
     */
    private static void selecionarIndicesSemCentralizar(FormularioListaObjetos formulario, int... indices) {
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndices(indices);
        } finally {
            formulario.selecaoProgramatica = false;
        }
    }

    @Test
    void copiarCor_pegaCorDoObjetoSelecionadoNaLista() {
        ObjetoLivre alvo = criarObjeto("Alvo", new Color(10, 20, 30), new Color(40, 50, 60));
        ObjetoLivre outro = criarObjeto("Outro", Color.BLACK, Color.WHITE);
        MainPanelEditor editor = editorComListasPopuladas(
                new ArrayList<>(List.of(outro, alvo)), new ArrayList<>());
        editor.formularioListaObjetos.selecionarSemCentralizar(alvo);

        editor.copiarCorObjetoSelecionado();

        ObjetoLivre destino = criarObjeto("Destino", Color.GRAY, Color.GRAY);
        editor.formularioListaObjetos.getDefaultListModelOP().addElement(destino);
        editor.formularioListaObjetos.selecionarSemCentralizar(destino);
        editor.colarCorObjetosSelecionados();

        assertEquals(new Color(10, 20, 30), destino.getCorPimaria());
        assertEquals(new Color(40, 50, 60), destino.getCorSecundaria());
    }

    @Test
    void copiarCor_semSelecao_naoAlteraNadaAoColar() {
        ObjetoLivre destino = criarObjeto("Destino", Color.GRAY, Color.GRAY);
        MainPanelEditor editor = editorComListasPopuladas(
                new ArrayList<>(List.of(destino)), new ArrayList<>());

        editor.copiarCorObjetoSelecionado();
        editor.formularioListaObjetos.selecionarSemCentralizar(destino);
        editor.colarCorObjetosSelecionados();

        assertEquals(Color.GRAY, destino.getCorPimaria(), "nada foi copiado, colar não deveria alterar a cor");
    }

    @Test
    void colarCor_aplicaEmVariosObjetosSelecionadosDeUmaVez() {
        ObjetoLivre origem = criarObjeto("Origem", Color.RED, Color.BLUE);
        ObjetoLivre destino1 = criarObjeto("D1", Color.GRAY, Color.GRAY);
        ObjetoLivre destino2 = criarObjeto("D2", Color.GRAY, Color.GRAY);
        ObjetoLivre destino3 = criarObjeto("D3", Color.GRAY, Color.GRAY);
        MainPanelEditor editor = editorComListasPopuladas(
                new ArrayList<>(List.of(origem, destino1, destino2, destino3)), new ArrayList<>());

        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarCorObjetoSelecionado();

        selecionarIndicesSemCentralizar(editor.formularioListaObjetos, 1, 2, 3);
        editor.colarCorObjetosSelecionados();

        for (ObjetoLivre destino : List.of(destino1, destino2, destino3)) {
            assertEquals(Color.RED, destino.getCorPimaria());
            assertEquals(Color.BLUE, destino.getCorSecundaria());
        }
    }

    @Test
    void colarCor_aplicaMisturandoObjetosDeCenarioEDeFuncao() {
        ObjetoLivre origem = criarObjeto("Origem", Color.RED, Color.BLUE);
        ObjetoLivre destinoFuncao = criarObjeto("DestinoFuncao", Color.GRAY, Color.GRAY);
        ObjetoLivre destinoCenario = criarObjeto("DestinoCenario", Color.GRAY, Color.GRAY);
        MainPanelEditor editor = editorComListasPopuladas(
                new ArrayList<>(List.of(origem, destinoFuncao)),
                new ArrayList<>(List.of(destinoCenario)));

        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarCorObjetoSelecionado();

        // listarObjetos() unificado concatena objetosCenario primeiro,
        // depois objetos: índice 0=destinoCenario, 1=origem, 2=destinoFuncao.
        selecionarIndicesSemCentralizar(editor.formularioListaObjetos, 0, 2);
        editor.colarCorObjetosSelecionados();

        assertEquals(Color.RED, destinoFuncao.getCorPimaria());
        assertEquals(Color.RED, destinoCenario.getCorPimaria());
    }

    @Test
    void colarCor_semNadaSelecionado_naoLancaExcecao() {
        ObjetoLivre origem = criarObjeto("Origem", Color.RED, Color.BLUE);
        MainPanelEditor editor = editorComListasPopuladas(
                new ArrayList<>(List.of(origem)), new ArrayList<>());
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarCorObjetoSelecionado();
        editor.formularioListaObjetos.selecionarSemCentralizar(null);

        editor.colarCorObjetosSelecionados();
    }

    private static List<JButton> todosOsBotoes(Component componente) {
        List<JButton> botoes = new ArrayList<>();
        if (componente instanceof JButton) {
            botoes.add((JButton) componente);
        }
        if (componente instanceof Container) {
            for (Component filho : ((Container) componente).getComponents()) {
                botoes.addAll(todosOsBotoes(filho));
            }
        }
        return botoes;
    }

    /**
     * "Editar" (duplo-clique no item substitui o botão) não foi testado
     * diretamente disparando o clique: objetoLivreFormulario() abre um
     * JOptionPane.showMessageDialog modal de verdade neste ambiente
     * (headless=false), que bloquearia o teste esperando interação.
     * <p>
     * A lista única do editor (FormularioListaObjetos.unificada) sempre
     * habilita Primeiro/Ultimo agora — útil pros itens de cenário
     * misturados na lista, sem efeito adicional pros de função
     * (Escapada/Transparência), que não têm um botão próprio separado.
     */
    @Test
    void listaUnificada_temBotoesPrimeiroEUltimoERemoverMasNaoEditar() {
        FormularioListaObjetos formulario = FormularioListaObjetos.unificada(new MainPanelEditor());

        List<String> textos = new ArrayList<>();
        for (JButton botao : todosOsBotoes(formulario.getObjetos())) {
            textos.add(botao.getText());
        }

        assertTrue(textos.contains(Lang.msg("moverParaPrimeiro")));
        assertTrue(textos.contains(Lang.msg("moverParaUltimo")));
        assertTrue(textos.contains(Lang.msg("287")));
        assertTrue(textos.contains(Lang.msg("288")));
        assertTrue(textos.contains(Lang.msg("removerObjetoLista")));
        assertFalse(textos.contains("Editar"), "botão Editar deveria ter sido removido (duplo-clique no lugar)");
    }

    /**
     * Primeiro/Ultimo movem o item selecionado direto pro início/fim da
     * lista (e do circuito, via atualizarCircuito), preservando a ordem dos
     * demais.
     */
    @Test
    void botaoPrimeiro_moveItemSelecionadoParaOInicioDaLista() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre a = criarObjeto("A", Color.RED, Color.BLUE);
        ObjetoLivre b = criarObjeto("B", Color.GREEN, Color.YELLOW);
        ObjetoLivre c = criarObjeto("C", Color.BLACK, Color.WHITE);
        circuito.setObjetosCenario(new ArrayList<>(List.of(a, b, c)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = new FormularioListaObjetos(editor, Circuito::getObjetosCenario, true);
        formulario.listarObjetos();
        selecionarIndicesSemCentralizar(formulario, 2);

        JButton primeiro = botaoComTexto(formulario.getObjetos(), Lang.msg("moverParaPrimeiro"));
        // O próprio botão reseleciona a lista ao mover o item, o que
        // dispararia centralizarPonto() num MainPanelEditor sem a UI
        // completa (scrollPane nulo neste teste) — suprime com o mesmo
        // mecanismo de seleção "programática" usado acima.
        formulario.selecaoProgramatica = true;
        try {
            primeiro.doClick();
        } finally {
            formulario.selecaoProgramatica = false;
        }

        assertEquals(List.of(c, a, b), circuito.getObjetosCenario());
        assertEquals(0, formulario.getList().getSelectedIndex());
    }

    @Test
    void botaoUltimo_moveItemSelecionadoParaOFinalDaLista() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre a = criarObjeto("A", Color.RED, Color.BLUE);
        ObjetoLivre b = criarObjeto("B", Color.GREEN, Color.YELLOW);
        ObjetoLivre c = criarObjeto("C", Color.BLACK, Color.WHITE);
        circuito.setObjetosCenario(new ArrayList<>(List.of(a, b, c)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = new FormularioListaObjetos(editor, Circuito::getObjetosCenario, true);
        formulario.listarObjetos();
        selecionarIndicesSemCentralizar(formulario, 0);

        JButton ultimo = botaoComTexto(formulario.getObjetos(), Lang.msg("moverParaUltimo"));
        formulario.selecaoProgramatica = true;
        try {
            ultimo.doClick();
        } finally {
            formulario.selecaoProgramatica = false;
        }

        assertEquals(List.of(b, c, a), circuito.getObjetosCenario());
        assertEquals(2, formulario.getList().getSelectedIndex());
    }

    private static JButton botaoComTexto(Component raiz, String texto) {
        for (JButton botao : todosOsBotoes(raiz)) {
            if (texto.equals(botao.getText())) {
                return botao;
            }
        }
        throw new AssertionError("botão \"" + texto + "\" não encontrado");
    }

    /**
     * Confirma que o duplo-clique é detectado no índice correto do item
     * clicado (locationToIndex), sem de fato disparar a abertura do
     * formulário (que exigiria interação com um diálogo modal real).
     */
    @Test
    void duploCliqueNaLista_resolveOIndiceDoItemClicado() {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        ObjetoLivre primeiro = criarObjeto("Primeiro", Color.RED, Color.BLUE);
        ObjetoLivre segundo = criarObjeto("Segundo", Color.GREEN, Color.YELLOW);
        circuito.setObjetosCenario(new ArrayList<>(List.of(primeiro, segundo)));
        editor.setCircuito(circuito);

        FormularioListaObjetos formulario = new FormularioListaObjetos(editor, Circuito::getObjetosCenario);
        formulario.listarObjetos();

        java.awt.Rectangle celaSegundo = formulario.getList().getCellBounds(1, 1);
        int indice = formulario.getList().locationToIndex(
                new Point(celaSegundo.x + 1, celaSegundo.y + 1));

        assertEquals(1, indice, "o ponto dentro da célula do segundo item deveria resolver pro índice 1");
        assertEquals(segundo, formulario.getDefaultListModelOP().get(indice));
    }
}
