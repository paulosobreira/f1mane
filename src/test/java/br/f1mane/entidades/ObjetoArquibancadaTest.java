package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * ObjetoArquibancada passou a ser desenhada ponto a ponto no editor (um
 * clique de cada vez, botão direito finaliza), como {@link ObjetoGuardRails}
 * — um encadeamento de segmentos retos entre pontos consecutivos, com o
 * lance de arquibancada (faixa sólida + listras internas) acompanhando toda
 * a extensão do encadeamento, em vez de um único retângulo largura×altura.
 */
class ObjetoArquibancadaTest {

    private ObjetoArquibancada criarArquibancada(Point... pontos) {
        ObjetoArquibancada arquibancada = new ObjetoArquibancada();
        arquibancada.setPontos(new ArrayList<>(Arrays.asList(pontos)));
        arquibancada.setCorPimaria(new Color(150, 150, 150));
        arquibancada.setCorSecundaria(new Color(90, 90, 90));
        arquibancada.gerar();
        arquibancada.setPosicaoQuina(arquibancada.obterArea().getLocation());
        return arquibancada;
    }

    private BufferedImage renderiza(ObjetoArquibancada arquibancada, int largura, int altura) {
        BufferedImage imagem = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            arquibancada.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    @Test
    void semPontosSuficientes_naoLancaExcecaoENaoDesenhaNada() {
        ObjetoArquibancada arquibancada = new ObjetoArquibancada();
        arquibancada.setPontos(new ArrayList<>(Arrays.asList(new Point(10, 10))));
        arquibancada.setPosicaoQuina(new Point(10, 10));

        BufferedImage imagem = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            assertDoesNotThrow(() -> arquibancada.desenha(g2d, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void obterArea_semPontos_naoLancaExcecao() {
        ObjetoArquibancada arquibancada = new ObjetoArquibancada();

        assertDoesNotThrow(arquibancada::obterArea);
    }

    @Test
    void desenha_segmentoReto_pintaFaixaSolidaComCorPrimaria() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(20, 10), new Point(20, 110));
        arquibancada.setLargura(20);

        BufferedImage imagem = renderiza(arquibancada, 60, 130);

        assertTrue(imagemContemCor(imagem, arquibancada.getCorPimaria()),
                "deveria pintar a faixa sólida com a cor primária ao longo do encadeamento");
    }

    @Test
    void desenha_segmentoReto_pintaListrasComCorSecundaria() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(20, 10), new Point(20, 110));
        arquibancada.setLargura(20);

        BufferedImage imagem = renderiza(arquibancada, 60, 130);

        assertTrue(imagemContemCor(imagem, arquibancada.getCorSecundaria()),
                "deveria pintar as listras internas com a cor secundária ao longo do encadeamento");
    }

    /** Mesma extensão do percurso, largura maior => mais colunas cobertas pela faixa sólida. */
    @Test
    void desenha_larguraMaior_cobreMaisColunas() {
        ObjetoArquibancada estreita = criarArquibancada(new Point(30, 10), new Point(30, 110));
        estreita.setLargura(10);
        ObjetoArquibancada larga = criarArquibancada(new Point(30, 10), new Point(30, 110));
        larga.setLargura(40);

        BufferedImage imagemEstreita = renderiza(estreita, 80, 130);
        BufferedImage imagemLarga = renderiza(larga, 80, 130);

        assertTrue(contarColunasComCor(imagemLarga, larga.getCorPimaria()) > contarColunasComCor(imagemEstreita,
                estreita.getCorPimaria()), "faixa mais larga deveria cobrir mais colunas que a faixa estreita");
    }

    @Test
    void desenha_encadeamentoDeDoisSegmentos_faixaContinuaAtravesDoVertice() {
        // "L": desce e depois vira, formando um encadeamento de dois segmentos.
        ObjetoArquibancada arquibancada = criarArquibancada(
                new Point(50, 10), new Point(50, 110), new Point(150, 110));
        arquibancada.setLargura(20);

        BufferedImage imagem = renderiza(arquibancada, 200, 130);

        assertTrue(imagemContemCor(imagem, arquibancada.getCorPimaria()));
        // Sobre o segmento horizontal também deve haver faixa, não só no vertical.
        boolean encontrouNoHorizontal = false;
        int alvo = arquibancada.getCorPimaria().getRGB();
        for (int x = 60; x < 150 && !encontrouNoHorizontal; x++) {
            if (imagem.getRGB(x, 111) == alvo) {
                encontrouNoHorizontal = true;
            }
        }
        assertTrue(encontrouNoHorizontal, "a faixa deveria continuar através do vértice, cobrindo o segmento horizontal");
    }

    /**
     * Reproduz o relato: num vértice de curva acentuada, as listras (não a
     * faixa sólida externa, que já fecha via junta arredondada do
     * BasicStroke) deixavam um vão no lado mais aberto do ângulo, porque
     * cada listra era construída como retângulos independentes por
     * segmento, deslocados pela normal de cada trecho — normais que
     * divergem no vértice. Uma listra fora da centerline (offset != 0)
     * precisa continuar pintada bem perto do vértice, do lado de fora da
     * curva.
     */
    @Test
    void desenha_verticeDeCurvaAcentuada_listraForaDaCenterlineNaoDeixaVao() {
        ObjetoArquibancada arquibancada = criarArquibancada(
                new Point(50, 10), new Point(50, 110), new Point(150, 110));
        arquibancada.setLargura(60);

        BufferedImage imagem = renderiza(arquibancada, 200, 200);

        // Ponto próximo da bissetriz do vértice (50,110) deslocada ~25px
        // para o lado de fora da curva (uma das listras não-centrais do
        // lance de largura 60) — sem o fechamento correto, essa região
        // ficava sem nenhum pixel da cor secundária.
        assertTrue(regiaoContemCor(imagem, 32, 128, 4, arquibancada.getCorSecundaria()),
                "a listra deveria continuar pintada perto do vértice, sem vão do lado de fora da curva");
    }

    private static boolean regiaoContemCor(BufferedImage imagem, int cx, int cy, int raio, Color cor) {
        int alvo = cor.getRGB();
        for (int y = Math.max(0, cy - raio); y <= Math.min(imagem.getHeight() - 1, cy + raio); y++) {
            for (int x = Math.max(0, cx - raio); x <= Math.min(imagem.getWidth() - 1, cx + raio); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * CAP_BUTT: a faixa termina exatamente no primeiro/último ponto
     * clicado, sem nenhuma extensão além deles. Antes usava CAP_SQUARE, que
     * projetava meia largura pra fora das pontas (só trocando o formato
     * dessa borda indesejada de arredondada pra quadrada, sem removê-la).
     */
    @Test
    void desenha_naoProjetaFaixaAlemDasPontasDoEncadeamento() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(20, 50), new Point(20, 150));
        arquibancada.setLargura(20);

        BufferedImage imagem = renderiza(arquibancada, 60, 200);

        assertTrue(regiaoContemCor(imagem, 20, 55, 2, arquibancada.getCorPimaria()),
                "sanity check: a faixa deveria estar pintada logo dentro do início do segmento");
        assertFalse(regiaoContemCor(imagem, 20, 45, 2, arquibancada.getCorPimaria()),
                "a faixa não deveria se estender além do primeiro ponto clicado (y=50)");
        assertFalse(regiaoContemCor(imagem, 20, 155, 2, arquibancada.getCorPimaria()),
                "a faixa não deveria se estender além do último ponto clicado (y=150)");
    }

    /**
     * obterAreaVisual() é a base tanto do contorno de seleção desenhado no
     * editor quanto de obterAreaClique() — precisa cobrir o lance visível (a
     * faixa de espessura {@link ObjetoArquibancada#getLargura()}), não só o
     * retângulo bruto da centerline que obterArea() guarda (necessário para
     * o alinhamento de posicaoQuina/translação, mas bem menor que o desenho
     * real).
     */
    @Test
    void obterAreaVisual_cobreALarguraDoLanceNaoSoACenterline() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(0, 0), new Point(0, 100));
        arquibancada.setLargura(60);

        Point pontoDentroDoLanceLongeDaCenterline = new Point(25, 50);

        assertTrue(arquibancada.obterAreaVisual().contains(pontoDentroDoLanceLongeDaCenterline),
                "obterAreaVisual() deveria cobrir o lance inteiro (largura 60), não só a linha fina da centerline");
    }

    /**
     * A área de clique/arraste do objeto inteiro precisa cobrir o lance
     * visível (a faixa de espessura {@link ObjetoArquibancada#getLargura()}),
     * não só uma linha fina em cima da centerline (que era, na prática, a
     * área de um "guardrail" — quase impossível de acertar clicando fora da
     * própria linha dos pontos originais).
     */
    @Test
    void obterAreaClique_cobreALarguraDoLanceNaoSoACenterline() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(0, 0), new Point(0, 100));
        arquibancada.setLargura(60);

        Point pontoDentroDoLanceLongeDaCenterline = new Point(25, 50);

        assertTrue(arquibancada.obterAreaClique().contains(pontoDentroDoLanceLongeDaCenterline),
                "a área de clique deveria cobrir o lance inteiro (largura 60), não só a linha fina da centerline");
    }

    @Test
    void moverPonto_atualizaPosicaoDoPontoNoIndice() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(10, 10), new Point(10, 110));

        arquibancada.moverPonto(1, new Point(50, 200));

        assertEquals(new Point(50, 200), arquibancada.getPontos().get(1));
    }

    @Test
    void inserirPonto_adicionaPontoEntreOsVizinhos() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(10, 10), new Point(10, 110));

        arquibancada.inserirPonto(0, new Point(10, 60));

        assertEquals(3, arquibancada.getPontos().size());
        assertEquals(new Point(10, 60), arquibancada.getPontos().get(1));
    }

    @Test
    void removerPonto_removeQuandoRestamMaisDeDoisPontos() {
        ObjetoArquibancada arquibancada = criarArquibancada(
                new Point(10, 10), new Point(10, 60), new Point(10, 110));

        arquibancada.removerPonto(1);

        assertEquals(2, arquibancada.getPontos().size());
    }

    @Test
    void removerPonto_bloqueiaQuandoRestariamMenosDeDoisPontos() {
        ObjetoArquibancada arquibancada = criarArquibancada(new Point(10, 10), new Point(10, 110));

        arquibancada.removerPonto(0);

        assertEquals(2, arquibancada.getPontos().size());
    }

    @Test
    void construtor_defineDefaultsDeCorETransparencia() {
        ObjetoArquibancada arquibancada = new ObjetoArquibancada();

        assertEquals(new Color(150, 150, 150), arquibancada.getCorPimaria());
        assertEquals(new Color(90, 90, 90), arquibancada.getCorSecundaria());
        assertEquals(255, arquibancada.getTransparencia());
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

    private static int contarColunasComCor(BufferedImage imagem, Color cor) {
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
