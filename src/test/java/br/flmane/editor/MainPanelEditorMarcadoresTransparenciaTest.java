package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.No;
import br.flmane.entidades.ObjetoPista;
import br.flmane.entidades.ObjetoTransparencia;

/**
 * Cobre a mudança editor-marcadores-transparencia: nível de desenho padrão
 * 100 ao finalizar o desenho, cálculo automático de inicioTransparencia/
 * fimTransparencia a partir dos nós da pista cobertos pela área do objeto
 * (recalculado ao criar e ao reposicionar), e os marcadores tracejados
 * magenta/verde desenhados só para o objeto selecionado.
 */
class MainPanelEditorMarcadoresTransparenciaTest {

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

    private static void ativarMouseListener(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("adicionaEventosMouse", JFrame.class);
        metodo.setAccessible(true);
        metodo.invoke(editor, (JFrame) null);
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private static void clicar(MainPanelEditor editor, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, botao == MouseEvent.BUTTON3, botao);
        editor.dispatchEvent(evento);
    }

    private static void enviarEvento(MainPanelEditor editor, int tipo, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, tipo, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, false, botao);
        editor.dispatchEvent(evento);
    }

    // --- 5.1: finalizar o desenho define nivelDesenho == 100 e já calcula o intervalo ---

    @Test
    void finalizarDesenho_defineNivel100ERecalculaIntervalo() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        ObjetoTransparencia objeto = new ObjetoTransparencia();
        editor.setObjetoPista(objeto);
        setField(editor, "desenhandoObjetoLivre", true);
        setField(editor, "posicionaObjetoPista", true);

        // Banda fina sobre o trecho reto do topo da pista (y constante em 1000).
        clicar(editor, new Point(1500, 990), MouseEvent.BUTTON1);
        clicar(editor, new Point(2000, 990), MouseEvent.BUTTON1);
        clicar(editor, new Point(2000, 1010), MouseEvent.BUTTON1);
        clicar(editor, new Point(1500, 1010), MouseEvent.BUTTON3);

        assertTrue(circuito.getObjetos().contains(objeto), "objeto deveria ter sido finalizado e adicionado ao circuito");
        assertEquals(100, objeto.getNivelDesenho(), "nível de desenho padrão deveria ser 100 (sempre por cima)");
        assertTrue(objeto.getInicioTransparencia() > 0, "deveria ter calculado um início de intervalo > 0");
        assertTrue(objeto.getFimTransparencia() >= objeto.getInicioTransparencia(),
                "fim do intervalo deveria ser >= início");
    }

    // --- 5.2: cálculo automático a partir dos nós cobertos pela área ---

    @Test
    void recalculaIntervalo_definePeloMenorEMaiorIndiceCobertosPelaArea() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        List<No> pistaFull = circuito.getPistaFull();
        Rectangle area = areaEntreNos(pistaFull.get(50), pistaFull.get(80));

        ObjetoTransparencia objeto = objetoComArea(area);

        editor.recalculaIntervaloTransparencia(objeto);

        assertEquals(50, objeto.getInicioTransparencia());
        assertEquals(80, objeto.getFimTransparencia());
    }

    /**
     * Retângulo que cobre exatamente os nós entre {@code noA} e {@code noB}
     * (ambos sobre o trecho reto do topo da pista, y constante) — só uma
     * margem pequena na direção perpendicular (y), sem folga na direção ao
     * longo da pista (x), e +1 na largura para incluir o nó final (o
     * retângulo do AWT trata a borda direita/inferior como exclusiva).
     */
    private Rectangle areaEntreNos(No noA, No noB) {
        int margemPerpendicular = 5;
        return new Rectangle(
                Math.min(noA.getX(), noB.getX()), noA.getY() - margemPerpendicular,
                Math.abs(noB.getX() - noA.getX()) + 1, margemPerpendicular * 2);
    }

    @Test
    void recalculaIntervalo_semNenhumNoNaArea_ficaZeroZero() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);

        ObjetoTransparencia objeto = objetoComArea(new Rectangle(50000, 50000, 100, 100));
        objeto.setInicioTransparencia(999);
        objeto.setFimTransparencia(999);

        editor.recalculaIntervaloTransparencia(objeto);

        assertEquals(0, objeto.getInicioTransparencia());
        assertEquals(0, objeto.getFimTransparencia());
    }

    private ObjetoTransparencia objetoComArea(Rectangle area) {
        ObjetoTransparencia objeto = new ObjetoTransparencia();
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(area.x, area.y));
        pontos.add(new Point(area.x + area.width, area.y));
        pontos.add(new Point(area.x + area.width, area.y + area.height));
        pontos.add(new Point(area.x, area.y + area.height));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        return objeto;
    }

    // --- 5.3: reposicionar (arrastar) recalcula o intervalo, sobrescrevendo um valor manual ---

    @Test
    void arrastarObjeto_recalculaIntervaloParaANovaPosicao() throws Exception {
        Circuito circuito = circuitoVetorizado();
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);
        ativarMouseListener(editor);

        List<No> pistaFull = circuito.getPistaFull();
        Rectangle areaInicial = areaEntreNos(pistaFull.get(50), pistaFull.get(80));
        ObjetoTransparencia objeto = objetoComArea(areaInicial);
        // Valor manual "errado" de propósito, para confirmar que o arraste sobrescreve.
        objeto.setInicioTransparencia(999);
        objeto.setFimTransparencia(999);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        Point dentroDoObjeto = new Point(areaInicial.x + areaInicial.width / 2, areaInicial.y + areaInicial.height / 2);
        enviarEvento(editor, MouseEvent.MOUSE_PRESSED, dentroDoObjeto, MouseEvent.BUTTON1);
        // Arrasta bem para longe de qualquer nó da pista principal.
        Point longeDaPista = new Point(50000, 50000);
        enviarEvento(editor, MouseEvent.MOUSE_DRAGGED, longeDaPista, MouseEvent.BUTTON1);

        assertEquals(0, objeto.getInicioTransparencia(),
                "arrastar para fora da pista deveria recalcular e zerar o intervalo, sobrescrevendo o valor manual");
        assertEquals(0, objeto.getFimTransparencia());
    }

    // --- 5.4: marcadores tracejados só para o objeto selecionado, com intervalo configurado ---

    private BufferedImage renderizaMarcadores(MainPanelEditor editor) throws Exception {
        Field campoLargura = MainPanelEditor.class.getDeclaredField("larguraPistaPixeis");
        campoLargura.setAccessible(true);
        campoLargura.set(editor, 40);

        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaMarcadoresIntervaloTransparencia",
                Graphics2D.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(6000, 6000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            metodo.invoke(editor, g2d);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    private MainPanelEditor editorComObjetoSelecionado(Circuito circuito, ObjetoTransparencia objeto) throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        editor.setCircuito(circuito);
        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(objeto);
        circuito.setObjetos(objetos);

        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
        editor.formularioListaObjetos.listarObjetos();
        editor.formularioListaObjetos.selecionarSemCentralizar(objeto);
        return editor;
    }

    @Test
    void marcadores_apareceMagentaEVerdeQuandoSelecionadoComIntervalo() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComArea(new Rectangle(1500, 950, 500, 100));
        objeto.setInicioTransparencia(50);
        objeto.setFimTransparencia(80);
        MainPanelEditor editor = editorComObjetoSelecionado(circuito, objeto);

        BufferedImage imagem = renderizaMarcadores(editor);

        assertTrue(imagemContemCor(imagem, Color.MAGENTA), "marcador de início deveria aparecer em magenta");
        assertTrue(imagemContemCor(imagem, new Color(0, 170, 0)), "marcador de fim deveria aparecer em verde");
    }

    @Test
    void marcadores_naoAparecemQuandoObjetoNaoEstaSelecionado() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComArea(new Rectangle(1500, 950, 500, 100));
        objeto.setInicioTransparencia(50);
        objeto.setFimTransparencia(80);
        MainPanelEditor editor = editorComObjetoSelecionado(circuito, objeto);
        editor.formularioListaObjetos.selecionarSemCentralizar(null);

        BufferedImage imagem = renderizaMarcadores(editor);

        assertFalse(imagemContemCor(imagem, Color.MAGENTA), "sem seleção, marcador de início não deveria aparecer");
        assertFalse(imagemContemCor(imagem, new Color(0, 170, 0)), "sem seleção, marcador de fim não deveria aparecer");
    }

    @Test
    void marcadores_naoAparecemQuandoIntervaloEhZeroZero() throws Exception {
        Circuito circuito = circuitoVetorizado();
        ObjetoTransparencia objeto = objetoComArea(new Rectangle(1500, 950, 500, 100));
        objeto.setInicioTransparencia(0);
        objeto.setFimTransparencia(0);
        MainPanelEditor editor = editorComObjetoSelecionado(circuito, objeto);

        BufferedImage imagem = renderizaMarcadores(editor);

        assertFalse(imagemContemCor(imagem, Color.MAGENTA), "sem intervalo configurado, marcador de início não deveria aparecer");
        assertFalse(imagemContemCor(imagem, new Color(0, 170, 0)), "sem intervalo configurado, marcador de fim não deveria aparecer");
    }
}
