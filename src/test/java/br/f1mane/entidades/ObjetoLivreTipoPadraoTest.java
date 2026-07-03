package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre a propriedade `tipo` de ObjetoLivre e os padrões de preenchimento
 * procedurais (vegetação densa, água, brita): tipo padrão, POLIGONO_SIMPLES
 * preserva o preenchimento sólido de antes, os três tipos novos desenham
 * sem exceção usando corSecundaria restrito à área da forma, e o padrão é
 * determinístico entre duas renderizações sucessivas.
 */
class ObjetoLivreTipoPadraoTest {

    // Triângulo grande o bastante para conter várias células da grade do padrão.
    private static final List<Point> TRIANGULO_GRANDE = Arrays.asList(
            new Point(0, 0), new Point(300, 0), new Point(150, 300));

    private ObjetoLivre criarObjetoLivre(TipoObjetoLivre tipo, Color fundo, Color padrao) {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(TRIANGULO_GRANDE));
        objetoLivre.setTipo(tipo);
        objetoLivre.setCorPimaria(fundo);
        objetoLivre.setCorSecundaria(padrao);
        objetoLivre.gerar();
        return objetoLivre;
    }

    private BufferedImage renderiza(ObjetoLivre objetoLivre) {
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            objetoLivre.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        return contarPixelsComCor(imagem, cor) > 0;
    }

    private static int contarPixelsComCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        int total = 0;
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    total++;
                }
            }
        }
        return total;
    }

    @Test
    void construtor_tipoPadraoEhPoligonoSimples() {
        ObjetoLivre objetoLivre = new ObjetoLivre();

        assertEquals(TipoObjetoLivre.POLIGONO_SIMPLES, objetoLivre.getTipo());
    }

    @Test
    void setTipo_comNull_voltaParaPoligonoSimples() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setTipo(TipoObjetoLivre.AGUA);

        objetoLivre.setTipo(null);

        assertEquals(TipoObjetoLivre.POLIGONO_SIMPLES, objetoLivre.getTipo());
    }

    @Test
    void desenha_poligonoSimples_preenchimentoSolidoSemPadrao() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.POLIGONO_SIMPLES, fundo, padrao);

        BufferedImage imagem = renderiza(objetoLivre);

        assertTrue(imagemContemCor(imagem, fundo), "esperava o preenchimento sólido com a cor de fundo");
        assertFalse(imagemContemCor(imagem, padrao), "POLIGONO_SIMPLES não deveria desenhar nenhum padrão");
    }

    @Test
    void desenha_vegetacaoDensa_desenhaSemExcecaoEUsaCorSecundaria() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, fundo, padrao);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemCor(imagem, fundo), "esperava o fundo ainda visível entre os traços do padrão");
        assertTrue(imagemContemCor(imagem, padrao), "esperava o padrão de vegetação usando a cor secundária");
    }

    @Test
    void desenha_vegetacaoSimples_desenhaSemExcecaoEUsaCorSecundaria() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, fundo, padrao);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemCor(imagem, fundo), "esperava o fundo ainda visível entre os traços do padrão");
        assertTrue(imagemContemCor(imagem, padrao), "esperava os traços diagonais usando a cor secundária");
    }

    @Test
    void desenha_agua_desenhaSemExcecaoEUsaCorSecundaria() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.AGUA, fundo, padrao);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemCor(imagem, fundo));
        assertTrue(imagemContemCor(imagem, padrao), "esperava o padrão de ondas usando a cor secundária");
    }

    @Test
    void desenha_brita_desenhaSemExcecaoEUsaCorSecundaria() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.BRITA, fundo, padrao);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemCor(imagem, fundo));
        assertTrue(imagemContemCor(imagem, padrao), "esperava o padrão de brita usando a cor secundária");
    }

    /**
     * Conta blobs (componentes conexos, 4-vizinhança) da cor alvo via
     * flood-fill — a medida exata de "quantidade de marcas separadas",
     * diferente de contar pixels (onde um traço comprido pesa mais que um
     * ponto pequeno mesmo havendo muito menos traços do que pontos).
     */
    private static int contarComponentesConectados(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        boolean[][] visitado = new boolean[largura][altura];
        int[] filaX = new int[largura * altura];
        int[] filaY = new int[largura * altura];
        int componentes = 0;
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (visitado[x][y] || imagem.getRGB(x, y) != alvo) {
                    continue;
                }
                componentes++;
                int inicio = 0;
                int fim = 0;
                filaX[fim] = x;
                filaY[fim] = y;
                fim++;
                visitado[x][y] = true;
                while (inicio < fim) {
                    int cx = filaX[inicio];
                    int cy = filaY[inicio];
                    inicio++;
                    int[][] vizinhos = { { cx + 1, cy }, { cx - 1, cy }, { cx, cy + 1 }, { cx, cy - 1 } };
                    for (int[] v : vizinhos) {
                        int vx = v[0];
                        int vy = v[1];
                        if (vx >= 0 && vx < largura && vy >= 0 && vy < altura && !visitado[vx][vy]
                                && imagem.getRGB(vx, vy) == alvo) {
                            visitado[vx][vy] = true;
                            filaX[fim] = vx;
                            filaY[fim] = vy;
                            fim++;
                        }
                    }
                }
            }
        }
        return componentes;
    }

    @Test
    void desenha_brita_temQuantidadeDePontosMuitoMaiorQueOsOutrosPadroes() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre brita = criarObjetoLivre(TipoObjetoLivre.BRITA, fundo, padrao);
        ObjetoLivre vegetacaoDensa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, fundo, padrao);

        int pontosBrita = contarComponentesConectados(renderiza(brita), padrao);
        int marcasVegetacaoDensa = contarComponentesConectados(renderiza(vegetacaoDensa), padrao);

        assertTrue(pontosBrita > marcasVegetacaoDensa * 2,
                "esperava a brita ter muito mais pontos individuais que a grade de vegetação"
                        + " (brita=" + pontosBrita + ", vegetacaoDensa=" + marcasVegetacaoDensa + ")");
    }

    @Test
    void desenha_vegetacaoDensa_cobreMaisAreaQueAgua_gradeMaisApertada() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre vegetacaoDensa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, fundo, padrao);
        ObjetoLivre agua = criarObjetoLivre(TipoObjetoLivre.AGUA, fundo, padrao);

        // Grade mais apertada = marcas mais próximas, cobrindo mais área (menos
        // "espaço vazio" entre elas) — algumas chegam a se tocar, então contar
        // pixels reflete melhor "densidade" aqui do que contar blobs isolados.
        int pixelsVegetacaoDensa = contarPixelsComCor(renderiza(vegetacaoDensa), padrao);
        int pixelsAgua = contarPixelsComCor(renderiza(agua), padrao);

        assertTrue(pixelsVegetacaoDensa > pixelsAgua,
                "esperava a vegetação densa cobrir mais área de padrão que a água na mesma forma"
                        + " (vegetacaoDensa=" + pixelsVegetacaoDensa + ", agua=" + pixelsAgua + ")");
    }

    @Test
    void desenha_brita_pontosNaoFicamAlinhadosNumaGrade() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre brita = criarObjetoLivre(TipoObjetoLivre.BRITA, fundo, padrao);

        BufferedImage imagem = renderiza(brita);
        int alvo = padrao.getRGB();
        java.util.Set<Integer> colunasComPadrao = new java.util.HashSet<>();
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (imagem.getRGB(x, y) == alvo) {
                    colunasComPadrao.add(x);
                }
            }
        }
        // Numa grade alinhada, os pontos cairiam num conjunto pequeno e fixo de
        // colunas (múltiplos do passo); espalhados aleatoriamente, praticamente
        // toda coluna dentro da forma acaba tendo pelo menos um ponto.
        assertTrue(colunasComPadrao.size() > 50,
                "esperava os pontos de brita espalhados por muitas colunas diferentes, não alinhados em grade");
    }

    @Test
    void desenha_padraoEhDeterministico_entreDuasRenderizacoesSucessivas() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.BRITA, fundo, padrao);

        BufferedImage primeira = renderiza(objetoLivre);
        objetoLivre.gerar();
        BufferedImage segunda = renderiza(objetoLivre);

        int[] pixelsPrimeira = ((DataBufferInt) primeira.getRaster().getDataBuffer()).getData();
        int[] pixelsSegunda = ((DataBufferInt) segunda.getRaster().getDataBuffer()).getData();
        assertArrayEquals(pixelsPrimeira, pixelsSegunda,
                "o padrão procedural não deveria mudar entre duas renderizações do mesmo objeto");
    }
}
