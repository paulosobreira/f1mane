package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoPista;

/**
 * Cobre o corte de renderização por viewport (desenhaObjetosNivel só chama
 * desenha() para objetos que intersectam limitesViewPort()): objetos fora do
 * retângulo visível não devem aparecer no desenho, objetos dentro devem
 * continuar aparecendo, e um objeto rotacionado deve usar o raio circunscrito
 * (não a área bruta) para decidir visibilidade.
 * <p>
 * Usa {@link ObjetoGuardRails} (não {@link br.f1mane.entidades.ObjetoArquibancada}/
 * {@link br.f1mane.entidades.ObjetoConstrucao}) nos testes de geometria porque
 * a área desses dois só é populada dentro de {@code desenha()} — antes da
 * primeira chamada, {@code obterArea()} vem zerada, e o corte por viewport
 * deliberadamente não corta nesse caso (ver comentário em
 * {@code estaVisivelNoViewport}). GuardRails calcula {@code obterArea()} a
 * partir de {@code gerar()}, sem essa dependência.
 */
class MainPanelEditorViewportCullingTest {

    /** Monta um scrollPane com viewport (0,0,400,400), já que o editor sem janela real não tem um. */
    private static void configuraViewport(MainPanelEditor editor, int largura, int altura) throws Exception {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setSize(largura, altura);
        scrollPane.getViewport().setViewPosition(new Point(0, 0));
        Field campo = MainPanelEditor.class.getDeclaredField("scrollPane");
        campo.setAccessible(true);
        campo.set(editor, scrollPane);
    }

    private static boolean estaVisivelNoViewport(MainPanelEditor editor, ObjetoPista objeto) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("estaVisivelNoViewport", ObjetoPista.class);
        metodo.setAccessible(true);
        return (Boolean) metodo.invoke(editor, objeto);
    }

    private static BufferedImage renderiza(MainPanelEditor editor, int largura, int altura) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaObjetosNivel", Graphics2D.class, int.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            metodo.invoke(editor, g2d, 0);
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

    /** GuardRails de um único segmento (dois pontos), com bounds == (x,y,largura,largura). */
    private ObjetoGuardRails objeto(int x, int y, int largura, Color cor) {
        ObjetoGuardRails objeto = new ObjetoGuardRails();
        objeto.setLargura(2);
        objeto.setCorPimaria(cor);
        objeto.setCorSecundaria(cor);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(x, y));
        pontos.add(new Point(x + largura, y + largura));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        return objeto;
    }

    @Test
    void objetoDentroDoViewport_continuaSendoDesenhado() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        configuraViewport(editor, 400, 400);
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objeto(100, 100, 20, new Color(10, 20, 30)));
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        BufferedImage imagem = renderiza(editor, 400, 400);

        assertTrue(imagemContemCor(imagem, new Color(10, 20, 30)),
                "objeto dentro do viewport deveria continuar sendo desenhado");
    }

    @Test
    void objetoForaDoViewport_naoEDesenhado() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        configuraViewport(editor, 400, 400);
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objeto(5000, 5000, 20, new Color(10, 20, 30)));
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        BufferedImage imagem = renderiza(editor, 400, 400);

        assertFalse(imagemContemCor(imagem, new Color(10, 20, 30)),
                "objeto totalmente fora do viewport não deveria ser desenhado");
    }

    @Test
    void semScrollPaneMontado_naoCorta_desenhaTudo() throws Exception {
        // MainPanelEditor sem janela real (ex.: outros testes que o instanciam
        // direto) não tem scrollPane; o corte deve degradar para "sempre visível".
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objeto(100, 100, 20, new Color(10, 20, 30)));
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        BufferedImage imagem = renderiza(editor, 400, 400);

        assertTrue(imagemContemCor(imagem, new Color(10, 20, 30)),
                "sem scrollPane montado, o corte por viewport não deveria suprimir nada");
    }

    @Test
    void objetoRotacionado_usaRaioCircunscritoParaDecidirVisibilidade() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        configuraViewport(editor, 400, 400);

        // obterArea() bruta = (405,405,60,60): não intersecta o viewport
        // (0,0,400,400) — mas o círculo circunscrito (raio ~42.4, centrado
        // em (435,435)) alcança de volta até x,y ~392.6, que já entra no
        // viewport. Com angulo=0 a mesma área não deveria ser visível.
        ObjetoGuardRails objetoRotacionado = objeto(405, 405, 60, Color.BLACK);
        objetoRotacionado.setAngulo(45);
        boolean visivelRotacionado = estaVisivelNoViewport(editor, objetoRotacionado);

        ObjetoGuardRails objetoSemRotacao = objeto(405, 405, 60, Color.BLACK);
        boolean visivelSemRotacao = estaVisivelNoViewport(editor, objetoSemRotacao);

        assertTrue(visivelRotacionado,
                "objeto rotacionado cujo raio circunscrito toca o viewport deveria ser considerado visível");
        assertFalse(visivelSemRotacao,
                "o mesmo retângulo sem rotação, fora do viewport, não deveria ser considerado visível");
    }
}
