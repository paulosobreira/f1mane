package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre os novos tipos de padrão LISTRADO/XADREZ de ObjetoLivre e a correção
 * de rotação: o ângulo do objeto rotaciona só o CONTEÚDO do padrão, nunca a
 * silhueta/contorno do objeto livre em si, que permanece fixa.
 */
class ObjetoLivrePadraoRotacaoTest {

    private static final Color PRIMARIA = Color.BLACK;
    private static final Color SECUNDARIA = Color.WHITE;

    private ObjetoLivre quadradoComTipo(TipoObjetoLivre tipo, double angulo) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setTipo(tipo);
        objeto.setCorPimaria(PRIMARIA);
        objeto.setCorSecundaria(SECUNDARIA);
        objeto.setTransparencia(255);
        objeto.setAngulo(angulo);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(0, 0));
        pontos.add(new Point(120, 0));
        pontos.add(new Point(120, 120));
        pontos.add(new Point(0, 120));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(new Point(90, 90));
        return objeto;
    }

    /** Retângulo NÃO quadrado (160x60): uma rotação de 90° trocaria largura/altura se a silhueta ainda girasse. */
    private ObjetoLivre retanguloComAngulo(double angulo) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setTipo(TipoObjetoLivre.POLIGONO_SIMPLES);
        objeto.setCorPimaria(PRIMARIA);
        objeto.setCorSecundaria(SECUNDARIA);
        objeto.setTransparencia(255);
        objeto.setAngulo(angulo);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(0, 0));
        pontos.add(new Point(160, 0));
        pontos.add(new Point(160, 60));
        pontos.add(new Point(0, 60));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(new Point(70, 120));
        return objeto;
    }

    private static Rectangle limitesDaCorPintada(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = -1, maxY = -1;
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private BufferedImage renderiza(ObjetoLivre objeto) {
        BufferedImage imagem = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            objeto.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private static int contaTransicoes(BufferedImage imagem, int y, int xIni, int xFim) {
        int transicoes = 0;
        Integer corAnterior = null;
        int rgbPrimaria = PRIMARIA.getRGB();
        int rgbSecundaria = SECUNDARIA.getRGB();
        for (int x = xIni; x < xFim; x++) {
            int rgb = imagem.getRGB(x, y);
            Integer corAtual = rgb == rgbPrimaria ? Integer.valueOf(0) : rgb == rgbSecundaria ? Integer.valueOf(1) : null;
            if (corAtual != null) {
                if (corAnterior != null && !corAtual.equals(corAnterior)) {
                    transicoes++;
                }
                corAnterior = corAtual;
            }
        }
        return transicoes;
    }

    @Test
    void angulo_naoAlteraASilhuetaDoObjetoLivre() {
        ObjetoLivre semRotacao = retanguloComAngulo(0);
        ObjetoLivre comRotacao = retanguloComAngulo(90);

        Rectangle limitesSemRotacao = limitesDaCorPintada(renderiza(semRotacao), PRIMARIA);
        Rectangle limitesComRotacao = limitesDaCorPintada(renderiza(comRotacao), PRIMARIA);

        assertEquals(limitesSemRotacao, limitesComRotacao,
                "angulo não deveria mais alterar a silhueta/contorno do objeto livre, só o padrão interno");
    }

    @Test
    void tiposListradoEXadrez_existemERoundtripEmSetTipoGetTipo() {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setTipo(TipoObjetoLivre.LISTRADO);
        assertEquals(TipoObjetoLivre.LISTRADO, objeto.getTipo());
        objeto.setTipo(TipoObjetoLivre.XADREZ);
        assertEquals(TipoObjetoLivre.XADREZ, objeto.getTipo());
    }

    @Test
    void listrado_semRotacao_padraoConstanteAoLongoDeUmaLinhaHorizontal() {
        ObjetoLivre objeto = quadradoComTipo(TipoObjetoLivre.LISTRADO, 0);
        BufferedImage imagem = renderiza(objeto);

        // Faixas do padrão variam em Y, não em X (espaço local == espaço de
        // mundo com ângulo 0); uma linha horizontal fixa deveria ficar toda
        // numa única faixa (0 transições), exceto talvez 1 por ruído de borda.
        int transicoes = contaTransicoes(imagem, 90 + 60, 90 + 5, 90 + 115);
        assertTrue(transicoes <= 1,
                "com angulo=0, uma linha horizontal não deveria cruzar várias faixas do padrão listrado (transições="
                        + transicoes + ")");
    }

    @Test
    void listrado_comRotacao90_padraoAcompanhaAforma() {
        ObjetoLivre objeto = quadradoComTipo(TipoObjetoLivre.LISTRADO, 90);
        BufferedImage imagem = renderiza(objeto);

        // Com o objeto (e o padrão) girado 90°, as faixas que antes variavam
        // só em Y agora variam em X — a mesma linha horizontal deveria
        // cruzar várias faixas.
        int transicoes = contaTransicoes(imagem, 90 + 60, 90 + 5, 90 + 115);
        assertTrue(transicoes >= 3,
                "com angulo=90, o padrão listrado deveria ter girado junto com a forma (transições=" + transicoes
                        + ")");
    }

    @Test
    void xadrez_semRotacao_desenhaCorSecundariaDentroDaForma() {
        ObjetoLivre objeto = quadradoComTipo(TipoObjetoLivre.XADREZ, 0);
        BufferedImage imagem = renderiza(objeto);

        assertTrue(contemCor(imagem, SECUNDARIA), "padrão xadrez deveria desenhar células com a cor secundária");
    }

    @Test
    void xadrez_comRotacao_continuaDesenhandoSemLancarExcecao() {
        ObjetoLivre objeto = quadradoComTipo(TipoObjetoLivre.XADREZ, 45);
        BufferedImage imagem = renderiza(objeto);

        assertTrue(contemCor(imagem, SECUNDARIA),
                "padrão xadrez rotacionado deveria continuar desenhando células com a cor secundária");
    }

    private static boolean contemCor(BufferedImage imagem, Color cor) {
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

    private static boolean contemCorEmJanela(BufferedImage imagem, Color cor, int x0, int y0, int largura,
            int altura) {
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

    /**
     * Regressão: a grade do padrão precisa cobrir TODA a área do objeto
     * livre depois de rotacionada, não só a área do bounding box original
     * (que, girado, "encolhe" a cobertura em alguma direção e deixa cantos
     * sem padrão para formas não quadradas). Usa um retângulo bem alongado
     * (200x50, proporção 4:1) e BRITA (pontos densos, fáceis de detectar por
     * amostragem) para expor o problema com folga.
     */
    @Test
    void britaComRotacao_cobreTodaAAreaIncluindoOsCantos() {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setTipo(TipoObjetoLivre.BRITA);
        objeto.setCorPimaria(PRIMARIA);
        objeto.setCorSecundaria(SECUNDARIA);
        objeto.setTransparencia(255);
        objeto.setAngulo(45);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(0, 0));
        pontos.add(new Point(200, 0));
        pontos.add(new Point(200, 50));
        pontos.add(new Point(0, 50));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(new Point(50, 120));

        BufferedImage imagem = renderiza(objeto);

        // Silhueta fixa: (50,120) a (250,170). Janelas de 20x20 encostadas
        // em cada canto, com uma pequena margem pra dentro (evita pixels de
        // borda/antialiasing do clip).
        assertTrue(contemCorEmJanela(imagem, SECUNDARIA, 53, 123, 20, 20),
                "canto superior-esquerdo deveria continuar coberto por brita mesmo com o padrão rotacionado");
        assertTrue(contemCorEmJanela(imagem, SECUNDARIA, 227, 123, 20, 20),
                "canto superior-direito deveria continuar coberto por brita mesmo com o padrão rotacionado");
        assertTrue(contemCorEmJanela(imagem, SECUNDARIA, 53, 147, 20, 20),
                "canto inferior-esquerdo deveria continuar coberto por brita mesmo com o padrão rotacionado");
        assertTrue(contemCorEmJanela(imagem, SECUNDARIA, 227, 147, 20, 20),
                "canto inferior-direito deveria continuar coberto por brita mesmo com o padrão rotacionado");
    }
}
