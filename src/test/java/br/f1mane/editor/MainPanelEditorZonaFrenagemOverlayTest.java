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
import java.util.Set;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.DesenhoProceduralCircuito;
import br.f1mane.entidades.No;

/**
 * Marcação visual da zona de frenagem no editor: sempre visível (não
 * depende do checkbox de teste de pista), desenhada numa rotina própria do
 * editor separada de DesenhoProceduralCircuito.desenhaPistaZebraEBox — e
 * portanto ausente da imagem gerada em memória usada em corrida real.
 */
class MainPanelEditorZonaFrenagemOverlayTest {

    private static final Color COR_OVERLAY = new Color(120, 120, 120, 110);

    private List<No> construirPistaComZonaDeFrenagem() {
        List<No> pista = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            No no = new No();
            no.setIndex(i);
            no.setTipo(No.RETA);
            no.setPoint(new Point(20 + i * 3, 100));
            pista.add(no);
        }
        for (int i = 0; i < 10; i++) {
            No no = new No();
            no.setIndex(pista.size());
            no.setTipo(No.CURVA_BAIXA);
            no.setPoint(new Point(20 + pista.size() * 3, 100));
            pista.add(no);
        }
        return pista;
    }

    private Circuito construirCircuito(List<No> pistaFull) throws Exception {
        Circuito circuito = new Circuito();
        Field campoPistaFull = Circuito.class.getDeclaredField("pistaFull");
        campoPistaFull.setAccessible(true);
        campoPistaFull.set(circuito, pistaFull);
        // DesenhoProceduralCircuito.geraImagem usa pistaKey (não pistaFull) para desenhar o traçado.
        Field campoPistaKey = Circuito.class.getDeclaredField("pistaKey");
        campoPistaKey.setAccessible(true);
        campoPistaKey.set(circuito, pistaFull);
        return circuito;
    }

    /**
     * Regressão: {@code circuito.vetorizarPista(...)} substitui
     * {@code pistaFull} por uma lista nova (com instâncias de {@code No}
     * novas), sem trocar a referência de {@code circuito}. Cachear
     * {@link MainPanelEditor#obterZonaFrenagemNos()} pela referência de
     * {@code circuito} (em vez da lista) deixava o cache preso aos nós
     * antigos depois de uma revetorização — a zona detectada na primeira
     * chamada nunca era atualizada, então nada era desenhado/simulado no
     * teste de pista depois de revetorizar (ex.: ao clicar "Teste Pista").
     */
    @Test
    void revetorizarPista_invalidaOCachoDaZonaDeFrenagem() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = construirCircuito(new ArrayList<>()); // primeira "vetorização": pista vazia, sem zona
        editor.setCircuito(circuito);

        Method metodo = MainPanelEditor.class.getDeclaredMethod("obterZonaFrenagemNos");
        metodo.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<No> zonaAntesDeRevetorizar = (Set<No>) metodo.invoke(editor);
        assertTrue(zonaAntesDeRevetorizar.isEmpty(), "pré-condição: sem pista, não deveria haver zona de frenagem");

        // revetoriza: pistaFull passa a ser uma lista NOVA (novas instancias de No) com uma zona de verdade
        Field campoPistaFull = Circuito.class.getDeclaredField("pistaFull");
        campoPistaFull.setAccessible(true);
        List<No> novaPista = construirPistaComZonaDeFrenagem();
        campoPistaFull.set(circuito, novaPista);

        @SuppressWarnings("unchecked")
        Set<No> zonaDepoisDeRevetorizar = (Set<No>) metodo.invoke(editor);

        assertFalse(zonaDepoisDeRevetorizar.isEmpty(),
                "depois de revetorizar (pistaFull virou uma lista nova), a zona de frenagem deveria ser recalculada, "
                        + "não continuar presa ao resultado vazio da vetorização anterior");
    }

    @Test
    void obterZonaFrenagemNos_circuitoComZona_exposNosCorretosParaOOverlay() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = construirCircuito(construirPistaComZonaDeFrenagem());
        editor.setCircuito(circuito);

        Method metodo = MainPanelEditor.class.getDeclaredMethod("obterZonaFrenagemNos");
        metodo.setAccessible(true);
        @SuppressWarnings("unchecked")
        Set<No> zona = (Set<No>) metodo.invoke(editor);

        assertFalse(zona.isEmpty(), "circuito com reta+curva baixa qualificada deveria ter zona de frenagem detectada");
    }

    @Test
    void desenhaZonaFrenagemOverlay_circuitoSemZona_naoDesenhaNada() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        List<No> pistaSemZona = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            No no = new No();
            no.setIndex(i);
            no.setTipo(No.RETA);
            no.setPoint(new Point(20 + i * 3, 100));
            pistaSemZona.add(no);
        }
        Circuito circuito = construirCircuito(pistaSemZona);
        editor.setCircuito(circuito);

        BufferedImage imagem = renderizaOverlay(editor);

        assertFalse(imagemContemCor(imagem, COR_OVERLAY), "sem zona de frenagem detectada, o overlay não deveria desenhar nada");
    }

    @Test
    void desenhaZonaFrenagemOverlay_circuitoComZona_desenhaMarcacao() throws Exception {
        MainPanelEditor editor = new MainPanelEditor();
        Circuito circuito = construirCircuito(construirPistaComZonaDeFrenagem());
        editor.setCircuito(circuito);

        BufferedImage imagem = renderizaOverlay(editor);

        assertTrue(imagemContemCor(imagem, COR_OVERLAY), "com zona de frenagem detectada, o overlay deveria marcar o trecho");
    }

    @Test
    void imagemGeradaEmMemoriaParaCorridaReal_naoContemAMarcacaoDoEditor() throws Exception {
        Circuito circuito = construirCircuito(construirPistaComZonaDeFrenagem());
        // campos usados por DesenhoProceduralCircuito.geraImagem para dimensionar a imagem
        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertFalse(imagemContemCor(imagem, COR_OVERLAY),
                "a imagem gerada em memória para corrida real não deveria conter a marcação de zona de frenagem do editor");
    }

    private BufferedImage renderizaOverlay(MainPanelEditor editor) throws Exception {
        Method metodo = MainPanelEditor.class.getDeclaredMethod("desenhaZonaFrenagemOverlay", Graphics2D.class);
        metodo.setAccessible(true);
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            metodo.invoke(editor, g2d);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    /**
     * Compara com tolerância: composição alfa sobre pixel transparente
     * arredonda o RGB em +-1 por canal (ex.: 120 vira 121), então uma
     * igualdade exata de {@code getRGB()} é frágil pra cores translúcidas.
     */
    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (corProxima(imagem.getRGB(x, y), cor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean corProxima(int pixelArgb, Color alvo) {
        int a = (pixelArgb >>> 24) & 0xFF;
        int r = (pixelArgb >>> 16) & 0xFF;
        int g = (pixelArgb >>> 8) & 0xFF;
        int b = pixelArgb & 0xFF;
        int tolerancia = 3;
        return Math.abs(a - alvo.getAlpha()) <= tolerancia
                && Math.abs(r - alvo.getRed()) <= tolerancia
                && Math.abs(g - alvo.getGreen()) <= tolerancia
                && Math.abs(b - alvo.getBlue()) <= tolerancia;
    }
}
