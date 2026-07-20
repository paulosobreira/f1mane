package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoPneus;

/**
 * "Facilitador de desenho" de ObjetoPneus (Insert &gt; Pneus): uma corrente
 * contínua de cliques, no mesmo estilo visual (marcador magenta) dos demais
 * objetos desenhados ponto a ponto — mas cada segmento consecutivo (ponto N,
 * ponto N+1) gera um {@link ObjetoPneus} **independente** e com total
 * compatibilidade com o objeto de sempre (mesmos campos: posicaoQuina,
 * largura/altura em unidades de grade, ângulo, cores), não um único objeto
 * acumulando pontos como GuardRails/Arquibancada. O primeiro clique da
 * corrente só marca o ponto pendente (nada é criado ainda); a partir do
 * segundo clique, cada clique fecha um objeto usando o clique anterior como
 * início e o atual como fim, e o próprio clique atual já vira o início
 * pendente do próximo objeto — a corrente continua até o clique direito, sem
 * desfazer os objetos já criados.
 *
 * <p>O segmento clicado vira a linha de centro do objeto (não uma de suas
 * bordas): como {@code ObjetoPneus.desenha()} rotaciona a grade em torno do
 * próprio centro, ancorar a quina exatamente no primeiro clique só ficaria
 * correto pra ângulo 0 — em qualquer outro ângulo o bloco rotacionaria em
 * torno de um centro deslocado e sairia da linha dos cliques. Colocar os
 * cliques na linha de centro é o que garante que o objeto sempre "segue o
 * ângulo dos cliques" (ponto de partida e chegada do segmento clicado caem
 * exatamente nas bordas esquerda/direita do bloco, pra qualquer ângulo).
 */
class MainPanelEditorCriarPneusSequenciaTest {

    @BeforeEach
    void limparMemoria() {
        MemoriaPropriedadesObjeto.limparParaTeste();
    }

    private MainPanelEditor editorPronto() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetosCenario(new ArrayList<>());
        circuito.setObjetos(new ArrayList<>());
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

    /** Simula o estado que iniciarCriacaoObjeto() deixaria após escolher "Pneus" (sem abrir o diálogo modal real). */
    private static void iniciarSequenciaPneus(MainPanelEditor editor) throws Exception {
        setField(editor, "posicionaObjetoPista", true);
        setField(editor, "desenhandoPneusEmSequencia", true);
        setField(editor, "primeiroCliquePneus", null);
    }

    private static void setField(MainPanelEditor editor, String nome, Object valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(MainPanelEditor editor, String nome) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField(nome);
        campo.setAccessible(true);
        return (T) campo.get(editor);
    }

    private static void clicar(MainPanelEditor editor, Point ponto, int botao) {
        MouseEvent evento = new MouseEvent(editor, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                ponto.x, ponto.y, 1, botao == MouseEvent.BUTTON3, botao);
        editor.dispatchEvent(evento);
    }

    private static void clicarEsquerdo(MainPanelEditor editor, Point ponto) {
        clicar(editor, ponto, MouseEvent.BUTTON1);
    }

    /** Quina esperada pro segmento (a,b): pré-rotação, o ponto médio da aresta esquerda cai em "a". */
    private static Point quinaEsperada(Point a, Point b, int alturaGrade) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double larguraPx = Math.sqrt(dx * dx + dy * dy);
        double alturaPx = alturaGrade * 10.0;
        double rad = Math.atan2(dy, dx);
        double centroX = a.x + (larguraPx / 2.0) * Math.cos(rad);
        double centroY = a.y + (larguraPx / 2.0) * Math.sin(rad);
        // Math.round (não truncar), mesma semântica de Util.inteiro(double) usada em produção.
        return new Point((int) Math.round(centroX - larguraPx / 2.0), (int) Math.round(centroY - alturaPx / 2.0));
    }

    @Test
    void primeiroClique_soMarcaOPontoPendente_naoCriaNada() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        clicarEsquerdo(editor, new Point(10, 10));

        assertTrue(editor.getCircuito().getObjetosCenario().isEmpty());
        assertEquals(new Point(10, 10), getField(editor, "primeiroCliquePneus"),
                "primeiro clique deveria só marcar o ponto pendente (marcador magenta), sem criar objeto ainda");
    }

    @Test
    void doisCliques_criamUmObjetoPneusComLarguraEAnguloDosCliques_naLinhaDeCentro() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        Point p1 = new Point(10, 10);
        Point p2 = new Point(110, 10);
        clicarEsquerdo(editor, p1);
        clicarEsquerdo(editor, p2);

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(1, cenario.size());
        ObjetoPneus criado = (ObjetoPneus) cenario.get(0);
        assertEquals(10, criado.getLargura(), "distância de 100px deveria virar 10 unidades de grade (10px cada)");
        assertEquals(0.0, criado.getAngulo(), 0.001, "cliques na horizontal deveriam dar ângulo 0");
        assertEquals(quinaEsperada(p1, p2, criado.getAltura()), criado.getPosicaoQuina(),
                "quina deveria ser calculada pra o segmento clicado virar a linha de centro do objeto");
    }

    @Test
    void doisCliques_emAnguloNaoHorizontal_aindaSeguemOAnguloDosCliques() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        Point p1 = new Point(100, 100);
        Point p2 = new Point(100, 200);
        clicarEsquerdo(editor, p1);
        clicarEsquerdo(editor, p2);

        ObjetoPneus criado = (ObjetoPneus) editor.getCircuito().getObjetosCenario().get(0);
        assertEquals(10, criado.getLargura(), "distância de 100px deveria virar 10 unidades de grade");
        assertEquals(90.0, criado.getAngulo(), 0.001, "cliques na vertical (pra baixo) deveriam dar ângulo 90");
        assertEquals(quinaEsperada(p1, p2, criado.getAltura()), criado.getPosicaoQuina());
    }

    /**
     * Regressão do relato do usuário (sessão real no circuito SPA, via log
     * [PNEUS-DEBUG]): quando o segmento clicado dá um ângulo negativo em
     * {@code GeoUtil.calculaAngulo} (ex.: clique da direita pra esquerda e
     * de cima pra baixo, 3º/4º quadrante), o objeto nascia com ângulo 0 em
     * vez do equivalente positivo — a causa era
     * {@code ObjetoDesenho.setAngulo} usar {@code Math.max(0, angulo)}
     * (clamp) em vez de normalizar módulo 360, contradizendo o próprio
     * javadoc da classe ("equivalente ao ângulo positivo correspondente,
     * módulo 360").
     */
    @Test
    void doisCliques_comAnguloNegativoDoAtan2_naoZeraOAngulo() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        Point p1 = new Point(100, 100);
        Point p2 = new Point(0, 50);
        clicarEsquerdo(editor, p1);
        clicarEsquerdo(editor, p2);

        ObjetoPneus criado = (ObjetoPneus) editor.getCircuito().getObjetosCenario().get(0);
        double anguloBrutoEsperado = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        assertTrue(anguloBrutoEsperado < 0, "pré-condição do teste: o ângulo cru precisa ser negativo");
        double anguloNormalizadoEsperado = anguloBrutoEsperado + 360;
        assertEquals(anguloNormalizadoEsperado, criado.getAngulo(), 0.01,
                "ângulo negativo deveria virar o equivalente positivo (módulo 360), não zerar");
    }

    /**
     * Regressão do relato do usuário: o objeto não estava "seguindo o
     * ângulo dos cliques" — a causa era ancorar a quina direto no primeiro
     * clique, o que só ficava correto pra ângulo 0 (ObjetoPneus rotaciona
     * em torno do próprio centro, não da quina).
     */
    @Test
    void anguloDiferenteDeZero_naoDeixaAQuinaExatamenteNoPrimeiroClique() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        Point p1 = new Point(100, 100);
        Point p2 = new Point(100, 200);
        clicarEsquerdo(editor, p1);
        clicarEsquerdo(editor, p2);

        ObjetoPneus criado = (ObjetoPneus) editor.getCircuito().getObjetosCenario().get(0);
        assertFalse(p1.equals(criado.getPosicaoQuina()),
                "com ângulo != 0, a quina não deveria mais coincidir com o primeiro clique (rotação é em torno do centro)");
    }

    @Test
    void correnteContinua_terceiroCliqueFechaUmSegundoObjetoUsandoOSegundoClique() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);

        Point p1 = new Point(10, 10);
        Point p2 = new Point(110, 10);
        Point p3 = new Point(110, 110);
        clicarEsquerdo(editor, p1);
        clicarEsquerdo(editor, p2);

        assertTrue((Boolean) getField(editor, "posicionaObjetoPista"),
                "sequência deveria continuar ativa depois do primeiro objeto, sem precisar reabrir Criar Objeto");
        assertEquals(p2, getField(editor, "primeiroCliquePneus"),
                "o clique que acabou de fechar o primeiro objeto já deveria virar a ponta pendente do próximo");

        clicarEsquerdo(editor, p3);

        List<br.flmane.entidades.ObjetoPista> cenario = editor.getCircuito().getObjetosCenario();
        assertEquals(2, cenario.size(), "terceiro clique deveria fechar um segundo objeto (segmento ponto2-ponto3)");
        ObjetoPneus primeiro = (ObjetoPneus) cenario.get(0);
        ObjetoPneus segundo = (ObjetoPneus) cenario.get(1);
        assertNotSame(primeiro, segundo);
        assertEquals(10, segundo.getLargura(), "distância de 100px (p2-p3) deveria virar 10 unidades de grade");
        assertEquals(90.0, segundo.getAngulo(), 0.001, "p2 -> p3 é um segmento vertical pra baixo: ângulo 90");
        assertEquals(quinaEsperada(p2, p3, segundo.getAltura()), segundo.getPosicaoQuina());
        assertEquals(p3, getField(editor, "primeiroCliquePneus"),
                "depois do segundo objeto, a corrente continua com o terceiro clique como próxima ponta pendente");
    }

    @Test
    void cliqueDireito_encerraASequenciaSemCriarObjeto() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);
        clicarEsquerdo(editor, new Point(10, 10));

        clicar(editor, new Point(999, 999), MouseEvent.BUTTON3);

        assertFalse((Boolean) getField(editor, "posicionaObjetoPista"));
        assertFalse((Boolean) getField(editor, "desenhandoPneusEmSequencia"));
        assertNull(getField(editor, "primeiroCliquePneus"));
        assertTrue(editor.getCircuito().getObjetosCenario().isEmpty(),
                "clique direito antes de completar um segmento não deveria criar nenhum objeto");
    }

    @Test
    void cliqueDireito_encerraSemDesfazerOsObjetosJaCriados() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);
        clicarEsquerdo(editor, new Point(10, 10));
        clicarEsquerdo(editor, new Point(110, 10));
        clicarEsquerdo(editor, new Point(110, 110));

        clicar(editor, new Point(999, 999), MouseEvent.BUTTON3);

        assertEquals(2, editor.getCircuito().getObjetosCenario().size(),
                "clique direito encerra a corrente, mas os dois objetos já criados devem continuar no circuito");
        assertFalse((Boolean) getField(editor, "desenhandoPneusEmSequencia"));
    }

    @Test
    void alturaECoresVemDoTemplateLembrado_largutaEAnguloVemDosCliques() throws Exception {
        ObjetoPneus configurado = new ObjetoPneus();
        configurado.setAltura(9);
        configurado.setCorPimaria(Color.BLUE);
        configurado.setCorSecundaria(Color.RED);
        MemoriaPropriedadesObjeto.lembrar(configurado);

        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);
        clicarEsquerdo(editor, new Point(0, 0));
        clicarEsquerdo(editor, new Point(50, 0));

        ObjetoPneus criado = (ObjetoPneus) editor.getCircuito().getObjetosCenario().get(0);
        assertEquals(9, criado.getAltura(), "altura deveria vir do template lembrado, não do clique");
        assertEquals(Color.BLUE, criado.getCorPimaria());
        assertEquals(Color.RED, criado.getCorSecundaria());
        assertEquals(5, criado.getLargura(), "largura continua vindo da distância entre os cliques");
    }

    /** Pedido do usuário: marcador magenta no ponto pendente, mesmo estilo dos demais objetos desenhados ponto a ponto. */
    @Test
    void pontoPendente_ficaMarcadoEmMagenta_ateOProximoCliqueFecharUmObjeto() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);
        clicarEsquerdo(editor, new Point(50, 50));

        BufferedImage imagem = renderizaPreObjetoPneus(editor, 200, 200);

        assertTrue(regiaoContemCor(imagem, 40, 40, 20, 20, Color.MAGENTA),
                "ponto pendente deveria aparecer marcado em magenta, mesmo estilo dos demais previews ponto a ponto");
    }

    @Test
    void semSequenciaAtiva_naoDesenhaMarcador() throws Exception {
        MainPanelEditor editor = editorPronto();

        BufferedImage imagem = renderizaPreObjetoPneus(editor, 200, 200);

        assertFalse(regiaoContemCor(imagem, 0, 0, 200, 200, Color.MAGENTA));
    }

    private static BufferedImage renderizaPreObjetoPneus(MainPanelEditor editor, int largura, int altura)
            throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaPreObjetoPneus", Graphics2D.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            metodo.invoke(editor, g2d);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private static boolean regiaoContemCor(BufferedImage imagem, int x0, int y0, int largura, int altura,
            Color cor) {
        int alvo = cor.getRGB();
        for (int y = y0; y < y0 + altura; y++) {
            for (int x = x0; x < x0 + largura; x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void objetoCriadoNaSequencia_mantemTotalCompatibilidadeComObjetoPneusComum() throws Exception {
        MainPanelEditor editor = editorPronto();
        iniciarSequenciaPneus(editor);
        clicarEsquerdo(editor, new Point(10, 10));
        clicarEsquerdo(editor, new Point(60, 10));

        ObjetoPneus criado = (ObjetoPneus) editor.getCircuito().getObjetosCenario().get(0);
        // Mesma classe, mesmo desenho: nenhum campo/estado novo, só os
        // getters/setters que ObjetoPneus já tinha antes deste facilitador.
        assertEquals("Objeto 1", criado.getNome());
        assertTrue(criado.getAltura() > 0);
        assertTrue(criado.getLargura() > 0);
    }
}
