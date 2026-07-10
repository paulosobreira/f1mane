package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoArquibancada;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;

/**
 * Checkbox "Objetos" do editor: liga/desliga o desenho dos objetos de
 * desenho (Livre/Arquibancada/Construcao/GuardRails/Pneus) sem afetar os
 * objetos de função (Escapada/Transparencia), que continuam sempre
 * desenhados — testado via reflexão sobre o campo privado
 * desenhaObjetosDesenho, já que não há display interativo neste ambiente
 * para clicar o checkbox de verdade.
 */
class MainPanelEditorDesenhaObjetosCheckboxTest {

    private static void setDesenhaObjetosDesenho(MainPanelEditor editor, boolean valor) throws Exception {
        Field campo = MainPanelEditor.class.getDeclaredField("desenhaObjetosDesenho");
        campo.setAccessible(true);
        campo.set(editor, valor);
    }

    private ObjetoArquibancada objetoDeDesenho(int x, int y) {
        ObjetoArquibancada objeto = new ObjetoArquibancada();
        objeto.setPosicaoQuina(new Point(x, y));
        objeto.setCorPimaria(new Color(200, 10, 200));
        return objeto;
    }

    private BufferedImage renderiza(MainPanelEditor editor, Circuito circuito) throws Exception {
        java.lang.reflect.Method metodo = MainPanelEditor.class.getDeclaredMethod(
                "desenhaObjetosNivel", Graphics2D.class, int.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
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

    @Test
    void checkboxDesligado_naoDesenhaObjetoDeDesenho() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objetoDeDesenho(100, 100));
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        setDesenhaObjetosDesenho(editor, false);
        BufferedImage imagem = renderiza(editor, circuito);

        assertTrue(!imagemContemCor(imagem, new Color(200, 10, 200)),
                "com o checkbox desligado, o objeto de desenho não deveria aparecer");
    }

    @Test
    void checkboxLigado_desenhaObjetoDeDesenho() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objetoDeDesenho(100, 100));
        circuito.setObjetosCenario(objetosCenario);
        editor.setCircuito(circuito);

        setDesenhaObjetosDesenho(editor, true);
        BufferedImage imagem = renderiza(editor, circuito);

        assertTrue(imagemContemCor(imagem, new Color(200, 10, 200)),
                "com o checkbox ligado, o objeto de desenho deveria aparecer normalmente");
    }

    @Test
    void checkboxDesligado_naoAfetaObjetosDeFuncao() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = new Circuito();

        ObjetoEscapada escapada = new ObjetoEscapada();
        List<Point> pontosEscapada = new ArrayList<>();
        pontosEscapada.add(new Point(40, 40));
        pontosEscapada.add(new Point(60, 60));
        escapada.setPontos(pontosEscapada);
        escapada.gerar();
        escapada.setPosicaoQuina(escapada.obterArea().getLocation());
        ObjetoTransparencia transparencia = new ObjetoTransparencia();
        List<java.awt.Point> pontos = new ArrayList<>();
        pontos.add(new Point(200, 200));
        pontos.add(new Point(260, 200));
        pontos.add(new Point(230, 250));
        transparencia.setPontos(pontos);
        transparencia.gerar();
        transparencia.setPosicaoQuina(new Point(200, 200));

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(escapada);
        objetos.add(transparencia);
        circuito.setObjetos(objetos);
        editor.setCircuito(circuito);

        setDesenhaObjetosDesenho(editor, false);
        BufferedImage imagem = renderiza(editor, circuito);

        assertTrue(temPixelPintado(imagem, escapada.obterArea()),
                "Escapada deveria continuar desenhada mesmo com o checkbox Objetos desligado");
        assertTrue(temPixelPintado(imagem, transparencia.obterArea()),
                "Transparencia deveria continuar desenhada mesmo com o checkbox Objetos desligado");
    }

    private static boolean temPixelPintado(BufferedImage imagem, Rectangle area) {
        int x0 = Math.max(0, area.x);
        int y0 = Math.max(0, area.y);
        int x1 = Math.min(imagem.getWidth(), area.x + area.width);
        int y1 = Math.min(imagem.getHeight(), area.y + area.height);
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                if ((imagem.getRGB(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
