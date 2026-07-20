package br.flmane.editor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Circuito;
import br.flmane.entidades.ObjetoGuardRails;
import br.flmane.entidades.ObjetoPista;
import br.flmane.visao.PainelCircuito;

/**
 * Indicador de seleção desenhado no canvas a partir da seleção da lista
 * única ({@code desenhaListaObjetos}): com um único objeto selecionado, o
 * contorno é laranja e acompanha a rotação (ângulo) do objeto, igual ao
 * indicador de "objeto ativo" ({@code desenhaObjetoSelecionadoNoCanvas}).
 * Com vários objetos selecionados de uma vez (seleção múltipla, ex. antes de
 * "Copiar Objetos"), cada um ganha um contorno numa cor diferente (ciano),
 * já que nenhum atalho de edição de objeto único se aplica nesse modo — o
 * laranja fica reservado exclusivamente para seleção única.
 */
class MainPanelEditorIndicadorSelecaoTest {

    /** GuardRails de um único segmento (dois pontos), com bounds == (x,y,largura,altura). */
    private ObjetoGuardRails objeto(int x, int y, int largura, int altura) {
        ObjetoGuardRails objeto = new ObjetoGuardRails();
        objeto.setNome("Objeto 1");
        objeto.setLargura(2);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(x, y));
        pontos.add(new Point(x + largura, y + altura));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        return objeto;
    }

    private MainPanelEditor editorComObjetosCenario(List<ObjetoPista> objetos) {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        circuito.setObjetosCenario(objetos);
        circuito.setObjetos(new ArrayList<>());
        editor.setCircuito(circuito);
        editor.formularioListaObjetos = FormularioListaObjetos.unificada(editor);
        editor.formularioListaObjetos.listarObjetos();
        return editor;
    }

    private static void selecionarIndices(FormularioListaObjetos formulario, int... indices) {
        formulario.selecaoProgramatica = true;
        try {
            formulario.getList().setSelectedIndices(indices);
        } finally {
            formulario.selecaoProgramatica = false;
        }
    }

    private static BufferedImage renderizaListaObjetos(MainPanelEditor editor, int largura, int altura)
            throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaListaObjetos", Graphics2D.class);
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

    private static boolean regiaoContemCor(BufferedImage imagem, Rectangle regiao, Color cor) {
        int alvo = cor.getRGB();
        int x0 = Math.max(0, regiao.x);
        int y0 = Math.max(0, regiao.y);
        int x1 = Math.min(imagem.getWidth(), regiao.x + regiao.width);
        int y1 = Math.min(imagem.getHeight(), regiao.y + regiao.height);
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        return regiaoContemCor(imagem, new Rectangle(0, 0, imagem.getWidth(), imagem.getHeight()), cor);
    }

    @Test
    void selecaoUnica_semRotacao_desenhaContornoLaranjaNaAreaBruta() throws Exception {
        ObjetoGuardRails objeto = objeto(100, 100, 200, 20);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.formularioListaObjetos.selecionarSemCentralizar(objeto);

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertTrue(regiaoContemCor(imagem, new Rectangle(95, 95, 20, 30), Color.ORANGE),
                "borda esquerda do retângulo bruto (sem rotação) deveria estar laranja");
    }

    /**
     * Regressão: o contorno de seleção da lista, diferente do contorno de
     * "objeto ativo" (que já rotacionava antes deste change), desenhava um
     * retângulo sempre alinhado aos eixos, ignorando o ângulo do objeto —
     * ficava visualmente errado (torto em relação ao objeto de verdade) pra
     * qualquer objeto rotacionado selecionado só pela lista.
     */
    @Test
    void selecaoUnica_comRotacao_contornoAcompanhaOAnguloDoObjeto() throws Exception {
        // area bruta (100,100,200,20), centro (200,110). Rotacionado 90°, o
        // contorno real passa a ocupar x∈[190,210], y∈[10,210] — bem longe
        // da borda esquerda original (x≈100), que não deveria mais ter nada
        // desenhado ali se a rotação for aplicada corretamente.
        ObjetoGuardRails objeto = objeto(100, 100, 200, 20);
        objeto.setAngulo(90);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.formularioListaObjetos.selecionarSemCentralizar(objeto);

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertFalse(regiaoContemCor(imagem, new Rectangle(95, 95, 10, 30), Color.ORANGE),
                "borda esquerda do retângulo NÃO rotacionado não deveria estar desenhada; o contorno deveria ter girado com o objeto");
        assertTrue(regiaoContemCor(imagem, new Rectangle(185, 5, 30, 30), Color.ORANGE),
                "contorno rotacionado 90° deveria aparecer perto do topo do novo retângulo (x~190-210, y~10-210)");
    }

    @Test
    void selecaoMultipla_desenhaContornoCianoEmCadaObjetoSelecionado_semNenhumLaranja() throws Exception {
        ObjetoGuardRails a = objeto(50, 50, 30, 30);
        ObjetoGuardRails b = objeto(200, 200, 30, 30);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(a, b)));
        selecionarIndices(editor.formularioListaObjetos, 0, 1);

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertTrue(regiaoContemCor(imagem, new Rectangle(40, 40, 60, 60), Color.CYAN),
                "primeiro objeto selecionado deveria ter contorno ciano");
        assertTrue(regiaoContemCor(imagem, new Rectangle(190, 190, 60, 60), Color.CYAN),
                "segundo objeto selecionado deveria ter contorno ciano");
        assertFalse(imagemContemCor(imagem, Color.ORANGE),
                "seleção múltipla não deveria desenhar nenhum contorno laranja (reservado pra seleção única)");
    }

    /**
     * Pedido do usuário: a etiqueta com a posição/número do objeto na lista
     * deve aparecer em todos os casos, inclusive na seleção múltipla — antes
     * só aparecia com um único objeto selecionado.
     */
    @Test
    void selecaoMultipla_desenhaEtiquetaEmCadaObjetoSelecionado() throws Exception {
        ObjetoGuardRails a = objeto(50, 50, 30, 30);
        ObjetoGuardRails b = objeto(200, 200, 30, 30);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(a, b)));
        selecionarIndices(editor.formularioListaObjetos, 0, 1);

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertTrue(regiaoContemCor(imagem, new Rectangle(50, 50, 22, 12), PainelCircuito.lightWhiteRain),
                "primeiro objeto selecionado deveria ter etiqueta de número");
        assertTrue(regiaoContemCor(imagem, new Rectangle(200, 200, 22, 12), PainelCircuito.lightWhiteRain),
                "segundo objeto selecionado deveria ter etiqueta de número");
    }

    /**
     * Regressão de um NullPointerException real, capturado em produção:
     * {@code desenhaEtiquetaObjeto} fazia {@code objeto.getNome().split(" ")[1]}
     * sem checar nulo — objetos legados/corrompidos sem nome (ver bug
     * "Objeto 70: null" no circuito SPA, causado por uma race já corrigida
     * em {@code clickEditarObjetos}) travavam TODO o desenho da seleção a
     * cada repaint, assim que selecionados.
     */
    @Test
    void objetoSemNome_naoTravaODesenhoDaEtiqueta() throws Exception {
        ObjetoGuardRails objeto = objeto(100, 100, 30, 30);
        objeto.setNome(null);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(objeto)));
        editor.formularioListaObjetos.selecionarSemCentralizar(objeto);

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertTrue(regiaoContemCor(imagem, new Rectangle(100, 100, 22, 12), PainelCircuito.lightWhiteRain),
                "objeto sem nome ainda deveria desenhar a etiqueta (com um marcador em vez do número), sem lançar exceção");
    }

    @Test
    void semSelecao_naoDesenhaNenhumContorno() throws Exception {
        ObjetoGuardRails a = objeto(50, 50, 30, 30);
        MainPanelEditor editor = editorComObjetosCenario(new ArrayList<>(List.of(a)));

        BufferedImage imagem = renderizaListaObjetos(editor, 400, 400);

        assertFalse(imagemContemCor(imagem, Color.ORANGE));
        assertFalse(imagemContemCor(imagem, Color.CYAN));
    }
}
