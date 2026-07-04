package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * ObjetoGuardRails é um misto de ObjetoArquibancada (linhas finas repetidas)
 * com ObjetoLivre (desenhado ponto a ponto no editor, um clique de cada vez,
 * botão direito finaliza): é um encadeamento de segmentos retos entre pontos
 * consecutivos, com o padrão de linhas finas percorrendo a extensão inteira
 * do encadeamento continuamente, sem reiniciar a cada segmento/vértice.
 */
class ObjetoGuardRailsTest {

    private ObjetoGuardRails criarGuardRails(Point... pontos) {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();
        guardRails.setPontos(new ArrayList<>(Arrays.asList(pontos)));
        guardRails.setCorPimaria(new Color(50, 50, 50));
        guardRails.setCorSecundaria(new Color(220, 220, 220));
        guardRails.gerar();
        guardRails.setPosicaoQuina(guardRails.obterArea().getLocation());
        return guardRails;
    }

    private BufferedImage renderiza(ObjetoGuardRails guardRails, int largura, int altura) {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            guardRails.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private boolean[] colunaDeLinhas(BufferedImage imagem, int x, Color corLinha) {
        int alvo = corLinha.getRGB();
        boolean[] coluna = new boolean[imagem.getHeight()];
        for (int y = 0; y < imagem.getHeight(); y++) {
            coluna[y] = imagem.getRGB(x, y) == alvo;
        }
        return coluna;
    }

    private int contaSequenciasDeLinha(boolean[] coluna) {
        int quantidade = 0;
        boolean dentro = false;
        for (boolean pixel : coluna) {
            if (pixel && !dentro) {
                quantidade++;
            }
            dentro = pixel;
        }
        return quantidade;
    }

    @Test
    void semPontosSuficientes_naoLancaExcecaoENaoDesenhaNada() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();
        guardRails.setPontos(new ArrayList<>(Arrays.asList(new Point(10, 10))));
        guardRails.setPosicaoQuina(new Point(10, 10));

        BufferedImage imagem = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            assertDoesNotThrow(() -> guardRails.desenha(g2d, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void desenha_segmentoRetoVertical_linhasSaoBemMaisFinasQueAArquibancada() {
        ObjetoGuardRails guardRails = criarGuardRails(new Point(10, 10), new Point(10, 110));

        BufferedImage imagem = renderiza(guardRails, 30, 130);
        boolean[] coluna = colunaDeLinhas(imagem, 10, guardRails.getCorSecundaria());

        int maiorSequencia = 0;
        int atual = 0;
        for (boolean pixel : coluna) {
            atual = pixel ? atual + 1 : 0;
            maiorSequencia = Math.max(maiorSequencia, atual);
        }

        assertTrue(maiorSequencia > 0, "deveria ter desenhado ao menos uma linha");
        assertTrue(maiorSequencia <= 4,
                "linha deveria ter no máximo 4px de espessura (arquibancada usa 10px), mas teve " + maiorSequencia);
    }

    @Test
    void desenha_segmentoRetoVertical_temMuitasLinhas() {
        ObjetoGuardRails guardRails = criarGuardRails(new Point(10, 10), new Point(10, 110));

        BufferedImage imagem = renderiza(guardRails, 30, 130);
        boolean[] coluna = colunaDeLinhas(imagem, 10, guardRails.getCorSecundaria());

        // A arquibancada, no mesmo comprimento (100), desenha no máximo 5 linhas (período fixo de 20px).
        assertTrue(contaSequenciasDeLinha(coluna) > 5,
                "guard rails deveria ter muito mais linhas que a arquibancada no mesmo comprimento");
    }

    @Test
    void desenha_preencheDoComecoAoFimDoSegmentoSemSobrarVaoGrande() {
        ObjetoGuardRails guardRails = criarGuardRails(new Point(10, 10), new Point(10, 107));

        BufferedImage imagem = renderiza(guardRails, 30, 130);
        boolean[] coluna = colunaDeLinhas(imagem, 10, guardRails.getCorSecundaria());

        assertTrue(coluna[10], "a primeira linha deveria começar bem no início do segmento");
        assertTrue(coluna[106], "a última linha deveria terminar bem no final do segmento, sem sobrar vão");
    }

    @Test
    void desenha_encadeamentoDeDoisSegmentos_padraoDeLinhasContinuaAtravesDoVertice() {
        // "L": desce e depois vira, formando um encadeamento de dois segmentos.
        ObjetoGuardRails guardRails = criarGuardRails(
                new Point(50, 10), new Point(50, 110), new Point(150, 110));

        BufferedImage imagem = renderiza(guardRails, 200, 130);

        // Sobre o segmento vertical (x=51) e sobre o horizontal (y=111)
        // devem ambos ter várias linhas fininhas, sem um vão anormalmente
        // grande bem no vértice (x=50,y=110) que denunciaria reinício do
        // padrão em vez de continuidade.
        boolean[] colunaVertical = colunaDeLinhas(imagem, 50, guardRails.getCorSecundaria());
        assertTrue(contaSequenciasDeLinha(colunaVertical) > 3, "segmento vertical deveria ter várias linhas");

        int alvo = guardRails.getCorSecundaria().getRGB();
        int quantidadeNoHorizontal = 0;
        boolean dentro = false;
        for (int x = 0; x < imagem.getWidth(); x++) {
            boolean pixel = imagem.getRGB(x, 110) == alvo;
            if (pixel && !dentro) {
                quantidadeNoHorizontal++;
            }
            dentro = pixel;
        }
        assertTrue(quantidadeNoHorizontal > 3, "segmento horizontal deveria ter várias linhas");
    }

    @Test
    void obterArea_semPontos_naoLancaExcecao() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();

        assertDoesNotThrow(guardRails::obterArea);
    }

    @Test
    void getOrientacao_padraoEhVertical() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();

        assertEquals(OrientacaoGuardRails.VERTICAL, guardRails.getOrientacao());
    }

    @Test
    void setOrientacao_comNull_voltaParaVertical() {
        ObjetoGuardRails guardRails = new ObjetoGuardRails();
        guardRails.setOrientacao(OrientacaoGuardRails.HORIZONTAL);

        guardRails.setOrientacao(null);

        assertEquals(OrientacaoGuardRails.VERTICAL, guardRails.getOrientacao());
    }

    /**
     * VERTICAL desenha barras cruzando o percurso (largas na perpendicular,
     * finas ao longo dele); HORIZONTAL desenha traços ao longo do percurso
     * (finos na perpendicular, largos ao longo dele) — pra um segmento
     * vertical, isso significa que VERTICAL ocupa várias colunas de largura
     * (a espessura da barreira) enquanto HORIZONTAL ocupa só uma.
     */
    @Test
    void desenha_orientacaoHorizontal_marcasSaoMaisEstreitasNaLarguraQueVertical() {
        ObjetoGuardRails vertical = criarGuardRails(new Point(20, 10), new Point(20, 110));
        vertical.setLargura(6);
        vertical.setOrientacao(OrientacaoGuardRails.VERTICAL);

        ObjetoGuardRails horizontal = criarGuardRails(new Point(20, 10), new Point(20, 110));
        horizontal.setLargura(6);
        horizontal.setOrientacao(OrientacaoGuardRails.HORIZONTAL);

        BufferedImage imagemVertical = renderiza(vertical, 40, 130);
        BufferedImage imagemHorizontal = renderiza(horizontal, 40, 130);

        int colunasVertical = contarColunasComCor(imagemVertical, vertical.getCorSecundaria());
        int colunasHorizontal = contarColunasComCor(imagemHorizontal, horizontal.getCorSecundaria());

        assertTrue(colunasVertical > colunasHorizontal,
                "VERTICAL deveria ocupar mais colunas de largura que HORIZONTAL (vertical=" + colunasVertical
                        + ", horizontal=" + colunasHorizontal + ")");
    }

    private int contarColunasComCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        int colunas = 0;
        for (int x = 0; x < imagem.getWidth(); x++) {
            for (int y = 0; y < imagem.getHeight(); y++) {
                if (imagem.getRGB(x, y) == alvo) {
                    colunas++;
                    break;
                }
            }
        }
        return colunas;
    }
}
