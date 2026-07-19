package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoEscapada;
import br.flmane.entidades.ObjetoLivre;

/**
 * Copiar Objetos / Colar Objetos: copia um ou mais objetos selecionados na
 * lista única para uma memória do editor; colar avisa (diálogo, via double)
 * que o próximo clique no canvas define onde os objetos colados devem
 * aparecer, e esse clique insere cópias independentes (deep copy) centradas
 * no ponto clicado, preservando o arranjo relativo entre vários objetos
 * colados juntos.
 */
class MainPanelEditorCopiarColarObjetosTest {

    private ObjetoLivre criarObjetoLivre(String nome, Point... pontos) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNome(nome);
        List<Point> lista = new ArrayList<>();
        for (Point p : pontos) {
            lista.add(new Point(p));
        }
        objeto.setPontos(lista);
        objeto.gerar();
        return objeto;
    }

    private ObjetoEscapada criarObjetoEscapada(String nome, Point... pontos) {
        ObjetoEscapada objeto = new ObjetoEscapada();
        objeto.setNome(nome);
        List<Point> lista = new ArrayList<>();
        for (Point p : pontos) {
            lista.add(new Point(p));
        }
        objeto.setPontos(lista);
        objeto.gerar();
        return objeto;
    }

    private MainPanelEditorTestDouble editorComListasPopuladas(List<br.flmane.entidades.ObjetoPista> objetos,
            List<br.flmane.entidades.ObjetoPista> objetosCenario) throws Exception {
        MainPanelEditorTestDouble editor = new MainPanelEditorTestDouble();
        Circuito circuito = new Circuito();
        circuito.setObjetos(objetos);
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
        editor.formularioListaObjetos.listarObjetos();
        ativarMouseListener(editor);
        return editor;
    }

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
    }

    private static void clicar(MainPanelEditor editor, Point ponto) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, false, MouseEvent.BUTTON1);
        editor.dispatchEvent(evento);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(MainPanelEditor editor, String nome) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return (T) campo.get(editor);
    }

    @Test
    void copiar_semSelecao_naoAlteraAMemoria() throws Exception {
        ObjetoLivre destino = criarObjetoLivre("Destino", new Point(0, 0), new Point(10, 0), new Point(5, 10));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(destino)));

        editor.copiarObjetosSelecionados();
        editor.colarObjetosSelecionados();

        assertEquals(0, editor.getAvisosClicarParaColarObjetos(),
                "sem nada selecionado no copiar, colar não deveria ter nada pra colar, logo nenhum diálogo");
        assertFalse((Boolean) getField(editor, "posicionaObjetoPista"));
    }

    @Test
    void copiar_substituiConteudoAnteriorDaMemoria() throws Exception {
        ObjetoLivre a = criarObjetoLivre("A", new Point(0, 0), new Point(10, 0), new Point(5, 10));
        ObjetoLivre b = criarObjetoLivre("B", new Point(100, 100), new Point(110, 100), new Point(105, 110));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(a, b)));

        editor.formularioListaObjetos.selecionarSemCentralizar(a);
        editor.copiarObjetosSelecionados();
        editor.formularioListaObjetos.selecionarSemCentralizar(b);
        editor.copiarObjetosSelecionados();

        editor.colarObjetosSelecionados();
        clicar(editor, new Point(500, 500));

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(3, cenario.size(), "deveria ter a=orig, b=orig, e a cópia de b (não de a)");
        ObjetoLivre colado = (ObjetoLivre) cenario.get(2);
        assertEquals(b.getPontos().size(), colado.getPontos().size());
    }

    @Test
    void colar_semNadaCopiado_naoMostraDialogoNemEntraEmModo() throws Exception {
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(), new ArrayList<>());

        editor.colarObjetosSelecionados();

        assertEquals(0, editor.getAvisosClicarParaColarObjetos());
        assertFalse((Boolean) getField(editor, "posicionaObjetoPista"));
        assertNull(getField(editor, "objetosParaColar"));
    }

    @Test
    void colar_comObjetoCopiado_mostraDialogoEEntraEmModoDePosicionamento() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(10, 0), new Point(5, 10));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();

        editor.colarObjetosSelecionados();

        assertEquals(1, editor.getAvisosClicarParaColarObjetos());
        assertTrue((Boolean) getField(editor, "posicionaObjetoPista"));
        List<?> lote = getField(editor, "objetosParaColar");
        assertEquals(1, lote.size());
    }

    @Test
    void cliqueAposColar_insereObjetoCentradoNoPontoClicado() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();
        Rectangle area = origem.obterArea();

        editor.colarObjetosSelecionados();
        Point clique = new Point(1000, 800);
        clicar(editor, clique);

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(2, cenario.size());
        ObjetoLivre colado = (ObjetoLivre) cenario.get(1);
        assertNotSame(origem, colado);
        Point quinaEsperada = new Point(clique.x - area.width / 2, clique.y - area.height / 2);
        assertEquals(quinaEsperada, colado.getPosicaoQuina(),
                "o objeto colado deveria nascer centrado no ponto clicado");
        assertFalse((Boolean) getField(editor, "posicionaObjetoPista"),
                "modo de posicionamento deveria ter sido encerrado após o clique");
        assertNull(getField(editor, "objetosParaColar"));
    }

    /**
     * Regressão: obterArea() de tipos multi-ponto (via caminho/generalPath)
     * só refletia a posição nova depois de uma chamada real a desenha() —
     * que estaVisivelNoViewport() (chamada antes de desenha() no laço de
     * pintura) nunca disparava sozinha, porque ela mesma decide se desenha
     * com base nessa área ainda desatualizada. Sem o "aquecimento" da área
     * logo após colar, o objeto colado nunca aparecia até algo mais (ex.
     * recarregar o circuito) forçar um desenha() de outra forma. Aqui
     * confirmamos que obterArea() já reflete a posição final imediatamente
     * após o clique de colagem, sem depender de nenhum repaint real.
     */
    @Test
    void cliqueAposColar_areaJaReflexteAPosicaoFinalSemPrecisarDeRepaint() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();

        editor.colarObjetosSelecionados();
        Point clique = new Point(5000, 5000);
        clicar(editor, clique);

        ObjetoLivre colado = (ObjetoLivre) editor.getCircuito().getObjetosCenario().get(1);
        Rectangle areaColado = colado.obterArea();
        assertEquals(clique.x, areaColado.x + areaColado.width / 2,
                "obterArea() deveria já refletir a posição final logo após o clique, sem depender de repaint");
        assertEquals(clique.y, areaColado.y + areaColado.height / 2);
    }

    @Test
    void colarMultiplosObjetos_preservaArranjoRelativoEntreEles() throws Exception {
        ObjetoLivre a = criarObjetoLivre("A", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        ObjetoLivre b = criarObjetoLivre("B", new Point(100, 0), new Point(150, 0), new Point(125, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(a, b)));
        Rectangle areaA = a.obterArea();
        Rectangle areaB = b.obterArea();
        selecionarIndicesSemCentralizar(editor.formularioListaObjetos, 0, 1);
        editor.copiarObjetosSelecionados();

        editor.colarObjetosSelecionados();
        Point clique = new Point(2000, 2000);
        clicar(editor, clique);

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(4, cenario.size());
        ObjetoLivre coladoA = (ObjetoLivre) cenario.get(2);
        ObjetoLivre coladoB = (ObjetoLivre) cenario.get(3);

        int dx = coladoA.getPosicaoQuina().x - areaA.x;
        int dy = coladoA.getPosicaoQuina().y - areaA.y;
        Point quinaBEsperada = new Point(areaB.x + dx, areaB.y + dy);
        assertEquals(quinaBEsperada, coladoB.getPosicaoQuina(),
                "distância/direção entre os dois objetos colados deveria ser igual à dos originais");
    }

    @Test
    void moverObjetoColadoNaoAfetaOOriginal() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();
        editor.colarObjetosSelecionados();
        clicar(editor, new Point(700, 700));

        ObjetoLivre colado = (ObjetoLivre) editor.getCircuito().getObjetosCenario().get(1);
        colado.getPontos().add(new Point(999, 999));
        colado.setPosicaoQuina(new Point(-50, -50));

        assertEquals(3, origem.getPontos().size(), "mover/editar o colado não deveria afetar os pontos do original");
        assertFalse(origem.getPontos().contains(new Point(999, 999)));
    }

    @Test
    void colarDuasVezesSeguidas_geraDoisObjetosIndependentes() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();

        editor.colarObjetosSelecionados();
        clicar(editor, new Point(300, 300));
        editor.colarObjetosSelecionados();
        clicar(editor, new Point(900, 900));

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(3, cenario.size());
        ObjetoLivre primeiraColagem = (ObjetoLivre) cenario.get(1);
        ObjetoLivre segundaColagem = (ObjetoLivre) cenario.get(2);
        assertNotSame(primeiraColagem, segundaColagem);
        assertNotSame(primeiraColagem.getPontos(), segundaColagem.getPontos());

        segundaColagem.getPontos().add(new Point(1, 1));
        assertFalse(primeiraColagem.getPontos().contains(new Point(1, 1)));
    }

    @Test
    void colarAposTrocarDeCircuito_aindaFuncionaComAMemoriaAnterior() throws Exception {
        ObjetoLivre origem = criarObjetoLivre("Origem", new Point(0, 0), new Point(50, 0), new Point(25, 50));
        MainPanelEditorTestDouble editor = editorComListasPopuladas(new ArrayList<>(),
                new ArrayList<>(List.of(origem)));
        editor.formularioListaObjetos.selecionarSemCentralizar(origem);
        editor.copiarObjetosSelecionados();

        Circuito outroCircuito = new Circuito();
        outroCircuito.setObjetos(new ArrayList<>());
        outroCircuito.setObjetosCenario(new ArrayList<>());
        editor.setCircuito(outroCircuito);
        editor.formularioListaObjetos.listarObjetos();

        editor.colarObjetosSelecionados();
        clicar(editor, new Point(400, 400));

        assertEquals(1, outroCircuito.getObjetosCenario().size(),
                "memória copiada no circuito anterior deveria continuar disponível após trocar de circuito");
        assertTrue(outroCircuito.getObjetos() == null || outroCircuito.getObjetos().isEmpty());
    }

    private static void selecionarIndicesSemCentralizar(FormularioListaObjetos formulario, int... indices) {
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndices(indices);
        } finally {
            formulario.selecaoProgramatica = false;
        }
    }
}
