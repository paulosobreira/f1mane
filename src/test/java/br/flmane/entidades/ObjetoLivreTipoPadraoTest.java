package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.nnpe.Global;
import br.nnpe.Util;

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

    // VEGETACAO_DENSA e VEGETACAO_SIMPLES precisam de cores saturadas e com
    // matiz bem distinta entre si (tronco/copa, relva/arbusto) para os
    // testes de matiz próxima funcionarem — ver comentário de TOLERANCIA_MATIZ.
    private static final Color TRONCO_SATURADO = new Color(139, 90, 43, 255);
    private static final Color COPA_SATURADA = new Color(34, 139, 34, 255);
    private static final Color RELVA_SATURADA = new Color(200, 140, 20, 255);
    private static final Color ARBUSTO_SATURADO = new Color(20, 90, 200, 255);

    /** Flag estática global — sempre restaurada ao valor padrão entre testes. */
    @AfterEach
    void restauraPadraoObjetoLivreCompleto() {
        Global.padraoObjetoLivreCompleto = true;
    }

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

    /**
     * VEGETACAO_DENSA agora desenha sprites (tronco/copa) recoloridos por
     * substituição de matiz+saturação preservando o brilho original de cada
     * pixel (ver CarregadorRecursos.pintarMonocromatico) — o redimensiona-
     * mento bicúbico do sprite borra o pixel de brilho máximo do template,
     * então a cor exata do alvo raramente sobrevive pixel a pixel. Testes
     * dessa vegetação usam MATIZ próxima (com tolerância), não igualdade
     * exata de RGB, e por isso precisam de cores bem saturadas e distintas
     * entre si (tronco/copa) — as cores acinzentadas/quase-cinza usadas nos
     * demais testes (tipos ainda procedurais) não têm matiz estável o
     * bastante para esse critério.
     */
    private static final float TOLERANCIA_MATIZ = 0.06f;
    private static final float SATURACAO_MINIMA_MATIZ = 0.25f;
    private static final int ALPHA_MINIMO_MATIZ = 200;

    private static boolean pixelTemMatizProxima(int argb, float[] hsbAlvo) {
        int a = argb >>> 24;
        if (a < ALPHA_MINIMO_MATIZ) {
            return false;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float[] hsbPixel = Color.RGBtoHSB(r, g, b, null);
        if (hsbPixel[1] < SATURACAO_MINIMA_MATIZ) {
            return false;
        }
        float diferenca = Math.abs(hsbPixel[0] - hsbAlvo[0]);
        diferenca = Math.min(diferenca, 1f - diferenca);
        return diferenca <= TOLERANCIA_MATIZ;
    }

    private static int contarPixelsComMatizProxima(BufferedImage imagem, Color cor) {
        float[] hsbAlvo = Color.RGBtoHSB(cor.getRed(), cor.getGreen(), cor.getBlue(), null);
        int total = 0;
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (pixelTemMatizProxima(imagem.getRGB(x, y), hsbAlvo)) {
                    total++;
                }
            }
        }
        return total;
    }

    private static boolean imagemContemMatizProxima(BufferedImage imagem, Color cor) {
        return contarPixelsComMatizProxima(imagem, cor) > 0;
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
    void desenha_vegetacaoDensa_desenhaSemExcecaoEUsaAsDuasCores() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemMatizProxima(imagem, TRONCO_SATURADO),
                "esperava o tronco das árvores usando a cor primária (sprite recolorido)");
        assertTrue(imagemContemMatizProxima(imagem, COPA_SATURADA),
                "esperava a copa das árvores usando a cor secundária (sprite recolorido)");
    }

    /**
     * VEGETACAO_DENSA deixou de preencher o fundo da forma (ver
     * ObjetoLivre.desenha()) — diferente de todos os outros tipos, a área
     * some entre as árvores em vez de ficar coberta por corPimaria. Medido
     * indiretamente: a soma de pixels de tronco+copa fica bem abaixo da área
     * do triângulo (que ficaria 100% coberta se ainda houvesse fundo sólido).
     */
    @Test
    void desenha_vegetacaoDensa_fundoNaoEhPreenchido_areaFicaTransparenteEntreAsArvores() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        BufferedImage imagem = renderiza(objetoLivre);
        int pixelsTronco = contarPixelsComMatizProxima(imagem, TRONCO_SATURADO);
        int pixelsCopa = contarPixelsComMatizProxima(imagem, COPA_SATURADA);
        int areaAproximadaTriangulo = (300 * 300) / 2;

        assertTrue(pixelsTronco + pixelsCopa < areaAproximadaTriangulo,
                "esperava a área do objeto livre não inteiramente coberta (fundo transparente entre as árvores)"
                        + " (tronco=" + pixelsTronco + ", copa=" + pixelsCopa + ")");
    }

    @Test
    void desenha_vegetacaoSimples_desenhaSemExcecaoEUsaAsDuasCores() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, RELVA_SATURADA,
                ARBUSTO_SATURADO);

        BufferedImage imagem = assertDoesNotThrow(() -> renderiza(objetoLivre));

        assertTrue(imagemContemMatizProxima(imagem, RELVA_SATURADA),
                "esperava a relva usando a cor primária (sprite recolorido)");
        assertTrue(imagemContemMatizProxima(imagem, ARBUSTO_SATURADO),
                "esperava o arbusto usando a cor secundária (sprite recolorido)");
    }

    /**
     * VEGETACAO_SIMPLES também deixou de preencher o fundo (mesmo motivo da
     * densa: corPimaria agora colore só a relva, então um fundo sólido com
     * essa cor deixaria a relva invisível contra o próprio fundo).
     */
    @Test
    void desenha_vegetacaoSimples_fundoNaoEhPreenchido() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, RELVA_SATURADA,
                ARBUSTO_SATURADO);

        BufferedImage imagem = renderiza(objetoLivre);
        int pixelsRelva = contarPixelsComMatizProxima(imagem, RELVA_SATURADA);
        int pixelsArbusto = contarPixelsComMatizProxima(imagem, ARBUSTO_SATURADO);
        int areaAproximadaTriangulo = (300 * 300) / 2;

        assertTrue(pixelsRelva + pixelsArbusto < areaAproximadaTriangulo,
                "esperava a área do objeto livre não inteiramente coberta (fundo transparente entre as marcas)"
                        + " (relva=" + pixelsRelva + ", arbusto=" + pixelsArbusto + ")");
    }

    /**
     * Marcas de vegetação simples (relva/arbusto) devem ficar bem menores
     * que a árvore da vegetação densa — a pedido do usuário, 1/3 do tamanho
     * do padrão da densa (ver ObjetoLivre.FATOR_TAMANHO_VEGETACAO_SIMPLES).
     * Comparado pela maior dimensão de cada blob (largura ou altura), já que
     * relva/arbusto não esticam e podem ter proporções bem diferentes entre
     * si.
     */
    @Test
    void desenha_vegetacaoSimples_marcasSaoBemMenoresQueAArvoreDaDensa() {
        ObjetoLivre densa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);
        ObjetoLivre simples = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, RELVA_SATURADA, ARBUSTO_SATURADO);

        List<Rectangle> copas = bordasDosComponentesPorMatiz(renderiza(densa), COPA_SATURADA);
        List<Rectangle> marcasSimples = new ArrayList<>();
        marcasSimples.addAll(bordasDosComponentesPorMatiz(renderiza(simples), RELVA_SATURADA));
        marcasSimples.addAll(bordasDosComponentesPorMatiz(renderiza(simples), ARBUSTO_SATURADO));

        assertTrue(!copas.isEmpty(), "esperava ao menos uma árvore de vegetação densa pra comparar tamanho");
        assertTrue(!marcasSimples.isEmpty(), "esperava ao menos uma marca de vegetação simples pra comparar tamanho");

        double somaMaiorDimensaoDensa = 0;
        for (Rectangle copa : copas) {
            somaMaiorDimensaoDensa += Math.max(copa.width, copa.height);
        }
        double mediaDensa = somaMaiorDimensaoDensa / copas.size();

        double somaMaiorDimensaoSimples = 0;
        for (Rectangle marca : marcasSimples) {
            somaMaiorDimensaoSimples += Math.max(marca.width, marca.height);
        }
        double mediaSimples = somaMaiorDimensaoSimples / marcasSimples.size();

        assertTrue(mediaSimples < mediaDensa / 2,
                "esperava marcas de vegetação simples bem menores que a árvore da densa (densa=" + mediaDensa
                        + ", simples=" + mediaSimples + ")");
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
     * Regressão: um ObjetoLivre com muitos vértices editados manualmente pode
     * ter segmentos que se autointerceptam (comum depois de várias edições de
     * ponto/haste no editor). Usado como clip (g2d.clip(formaFinal)) com
     * antialiasing ligado — exatamente como acontece na geração da imagem
     * real de corrida, DesenhoProceduralCircuito.geraImagem() via
     * Util.setarHints() — esse tipo de forma simplesmente não pintava nenhum
     * ponto do padrão (permanecia só o preenchimento sólido), enquanto o
     * preenchimento em si sempre funcionou. Reproduz com a geometria real de
     * um objeto encontrado num circuito de produção (Albert Park) que caía
     * exatamente nesse caso. Corrigido usando Area(formaFinal) como clip, que
     * normaliza o caminho antes de aplicar.
     */
    @Test
    void desenha_comFormaAutointerceptanteEAntialiasing_padraoAindaAparece() {
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(Arrays.asList(
                new Point(5050, 732), new Point(4995, 1633), new Point(6056, 1686),
                new Point(6675, 1658), new Point(6788, 1660), new Point(6871, 1665),
                new Point(7265, 650), new Point(7266, 16), new Point(5059, 21))));
        objetoLivre.setTipo(TipoObjetoLivre.VEGETACAO_SIMPLES);
        objetoLivre.setCorPimaria(RELVA_SATURADA);
        objetoLivre.setCorSecundaria(ARBUSTO_SATURADO);
        objetoLivre.gerar();
        objetoLivre.setPosicaoQuina(objetoLivre.obterArea().getLocation());

        Rectangle area = objetoLivre.obterArea();
        BufferedImage imagem = new BufferedImage(area.x + area.width + 10, area.y + area.height + 10,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        Util.setarHints(g2d);
        try {
            objetoLivre.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }

        int totalPixelsPadrao = contarPixelsComMatizProxima(imagem, RELVA_SATURADA)
                + contarPixelsComMatizProxima(imagem, ARBUSTO_SATURADO);
        assertTrue(totalPixelsPadrao > 0,
                "o padrão de vegetação deveria aparecer mesmo numa forma autointerceptante, com antialiasing ligado");
    }

    /**
     * Conta blobs (componentes conexos, 4-vizinhança) da cor alvo via
     * flood-fill — a medida exata de "quantidade de marcas separadas",
     * diferente de contar pixels (onde um traço comprido pesa mais que um
     * ponto pequeno mesmo havendo muito menos traços do que pontos).
     */
    private static int contarComponentesConectados(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        return contarComponentesConectados(imagem, argb -> argb == alvo);
    }

    private static int contarComponentesConectadosPorMatiz(BufferedImage imagem, Color cor) {
        float[] hsbAlvo = Color.RGBtoHSB(cor.getRed(), cor.getGreen(), cor.getBlue(), null);
        return contarComponentesConectados(imagem, argb -> pixelTemMatizProxima(argb, hsbAlvo));
    }

    private static int contarComponentesConectados(BufferedImage imagem, java.util.function.IntPredicate pertence) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        boolean[][] visitado = new boolean[largura][altura];
        int[] filaX = new int[largura * altura];
        int[] filaY = new int[largura * altura];
        int componentes = 0;
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (visitado[x][y] || !pertence.test(imagem.getRGB(x, y))) {
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
                                && pertence.test(imagem.getRGB(vx, vy))) {
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
        ObjetoLivre vegetacaoDensa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        int pontosBrita = contarComponentesConectados(renderiza(brita), padrao);
        int marcasVegetacaoDensa = contarComponentesConectadosPorMatiz(renderiza(vegetacaoDensa), COPA_SATURADA);

        assertTrue(pontosBrita > marcasVegetacaoDensa * 2,
                "esperava a brita ter muito mais pontos individuais que a grade de vegetação"
                        + " (brita=" + pontosBrita + ", vegetacaoDensa=" + marcasVegetacaoDensa + ")");
    }

    @Test
    void desenha_vegetacaoDensa_cobreMaisAreaQueAgua_gradeMaisApertada() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre vegetacaoDensa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);
        ObjetoLivre agua = criarObjetoLivre(TipoObjetoLivre.AGUA, fundo, padrao);

        // Grade mais apertada = marcas mais próximas, cobrindo mais área (menos
        // "espaço vazio" entre elas) — algumas chegam a se tocar, então contar
        // pixels reflete melhor "densidade" aqui do que contar blobs isolados.
        // Vegetação densa usa contagem por matiz próxima (sprite recolorido,
        // ver TOLERANCIA_MATIZ); água ainda é procedural, contagem exata.
        int pixelsVegetacaoDensa = contarPixelsComMatizProxima(renderiza(vegetacaoDensa), COPA_SATURADA);
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

    @Test
    void desenha_vegetacaoDensaESimples_saoDeterministicasEntreDuasRenderizacoes() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        for (TipoObjetoLivre tipo : new TipoObjetoLivre[] {
                TipoObjetoLivre.VEGETACAO_DENSA, TipoObjetoLivre.VEGETACAO_SIMPLES }) {
            ObjetoLivre objetoLivre = criarObjetoLivre(tipo, fundo, padrao);

            BufferedImage primeira = renderiza(objetoLivre);
            objetoLivre.gerar();
            BufferedImage segunda = renderiza(objetoLivre);

            int[] pixelsPrimeira = ((DataBufferInt) primeira.getRaster().getDataBuffer()).getData();
            int[] pixelsSegunda = ((DataBufferInt) segunda.getRaster().getDataBuffer()).getData();
            assertArrayEquals(pixelsPrimeira, pixelsSegunda,
                    tipo + ": o padrão não deveria mudar entre duas renderizações do mesmo objeto");
        }
    }

    /**
     * Mede a bounding box de cada blob (componente conexo) da cor de padrão —
     * usado para comprovar dispersão fora de grade (posições muito variadas)
     * e variação de tamanho (áreas de blob bem diferentes entre si).
     */
    private static List<Rectangle> bordasDosComponentes(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB();
        return bordasDosComponentes(imagem, argb -> argb == alvo);
    }

    private static List<Rectangle> bordasDosComponentesPorMatiz(BufferedImage imagem, Color cor) {
        float[] hsbAlvo = Color.RGBtoHSB(cor.getRed(), cor.getGreen(), cor.getBlue(), null);
        return bordasDosComponentes(imagem, argb -> pixelTemMatizProxima(argb, hsbAlvo));
    }

    private static List<Rectangle> bordasDosComponentes(BufferedImage imagem, java.util.function.IntPredicate pertence) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        boolean[][] visitado = new boolean[largura][altura];
        int[] filaX = new int[largura * altura];
        int[] filaY = new int[largura * altura];
        List<Rectangle> blobs = new ArrayList<>();
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (visitado[x][y] || !pertence.test(imagem.getRGB(x, y))) {
                    continue;
                }
                int inicio = 0;
                int fim = 0;
                filaX[fim] = x;
                filaY[fim] = y;
                fim++;
                visitado[x][y] = true;
                int minX = x, maxX = x, minY = y, maxY = y;
                while (inicio < fim) {
                    int cx = filaX[inicio];
                    int cy = filaY[inicio];
                    inicio++;
                    minX = Math.min(minX, cx);
                    maxX = Math.max(maxX, cx);
                    minY = Math.min(minY, cy);
                    maxY = Math.max(maxY, cy);
                    int[][] vizinhos = { { cx + 1, cy }, { cx - 1, cy }, { cx, cy + 1 }, { cx, cy - 1 } };
                    for (int[] v : vizinhos) {
                        int vx = v[0];
                        int vy = v[1];
                        if (vx >= 0 && vx < largura && vy >= 0 && vy < altura && !visitado[vx][vy]
                                && pertence.test(imagem.getRGB(vx, vy))) {
                            visitado[vx][vy] = true;
                            filaX[fim] = vx;
                            filaY[fim] = vy;
                            fim++;
                        }
                    }
                }
                blobs.add(new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1));
            }
        }
        return blobs;
    }

    @Test
    void desenha_vegetacaoSimples_naoFicaAlinhadaNumaGrade() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, RELVA_SATURADA,
                ARBUSTO_SATURADO);

        BufferedImage imagem = renderiza(objetoLivre);
        float[] hsbRelva = Color.RGBtoHSB(RELVA_SATURADA.getRed(), RELVA_SATURADA.getGreen(), RELVA_SATURADA.getBlue(),
                null);
        float[] hsbArbusto = Color.RGBtoHSB(ARBUSTO_SATURADO.getRed(), ARBUSTO_SATURADO.getGreen(),
                ARBUSTO_SATURADO.getBlue(), null);
        java.util.Set<Integer> colunasComPadrao = new java.util.HashSet<>();
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                int argb = imagem.getRGB(x, y);
                if (pixelTemMatizProxima(argb, hsbRelva) || pixelTemMatizProxima(argb, hsbArbusto)) {
                    colunasComPadrao.add(x);
                }
            }
        }
        // Numa grade alinhada com deslocamento fixo por linha, as marcas
        // cairiam num conjunto pequeno de colunas periódicas; espalhadas,
        // ocupam muitas colunas diferentes dentro da forma.
        assertTrue(colunasComPadrao.size() > 50,
                "esperava as marcas espalhadas por muitas colunas diferentes, não alinhadas em grade"
                        + " (colunas=" + colunasComPadrao.size() + ")");
    }

    @Test
    void desenha_vegetacaoDensa_naoFicaAlinhadaNumaGrade() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        BufferedImage imagem = renderiza(objetoLivre);
        float[] hsbAlvo = Color.RGBtoHSB(COPA_SATURADA.getRed(), COPA_SATURADA.getGreen(), COPA_SATURADA.getBlue(),
                null);
        java.util.Set<Integer> colunasComPadrao = new java.util.HashSet<>();
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if (pixelTemMatizProxima(imagem.getRGB(x, y), hsbAlvo)) {
                    colunasComPadrao.add(x);
                }
            }
        }
        assertTrue(colunasComPadrao.size() > 50,
                "esperava as árvores espalhadas por muitas colunas diferentes, não alinhadas em grade (colunas="
                        + colunasComPadrao.size() + ")");
    }

    /**
     * A ALTURA da árvore continua a mesma para todas (sem variação de
     * escala — vista lateral não tem a mesma justificativa de profundidade
     * que a vista de topo anterior). A LARGURA da copa, porém, passou a
     * variar de propósito: a pedido do usuário (gostou do esticamento do
     * sprite pra caber no envelope quadrado, mas quis variedade), cada
     * árvore sorteia um fator de esticamento entre a proporção original do
     * sprite (mais estreita) e o esticamento total de antes (mais larga) —
     * ver ObjetoLivre.desenhaArvoreSeCouber. Medido pela LARGURA MÁXIMA
     * observada ser bem maior que a MÍNIMA (evidência de que o sorteio
     * realmente varia, não só ruído de arredondamento).
     */
    @Test
    void desenha_vegetacaoDensa_alturaUniformeMasLarguraDaCopaVariaAleatoriamente() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        List<Rectangle> copas = bordasDosComponentesPorMatiz(renderiza(objetoLivre), COPA_SATURADA);
        assertTrue(copas.size() >= 5, "esperava várias árvores para comparar tamanho (encontradas="
                + copas.size() + ")");

        int alturaMinimaGlobal = Integer.MAX_VALUE;
        int larguraMinima = Integer.MAX_VALUE;
        int larguraMaxima = Integer.MIN_VALUE;
        for (Rectangle copa : copas) {
            alturaMinimaGlobal = Math.min(alturaMinimaGlobal, copa.height);
            larguraMinima = Math.min(larguraMinima, copa.width);
            larguraMaxima = Math.max(larguraMaxima, copa.width);
        }
        // Altura: comparada só entre blobs próximos da menor altura global
        // (blobs bem mais altos são presumivelmente duas árvores grudadas
        // uma sobre a outra, não uma árvore individual mais alta).
        for (Rectangle copa : copas) {
            if (copa.height <= alturaMinimaGlobal * 1.3) {
                assertTrue(Math.abs(copa.height - alturaMinimaGlobal) <= 3,
                        "esperava a altura da árvore igual entre si, sem variação de escala (menor="
                                + alturaMinimaGlobal + ", outra=" + copa.height + ")");
            }
        }
        assertTrue(larguraMaxima > larguraMinima * 1.3,
                "esperava variação real de largura entre as copas (sorteio aleatório do fator de esticamento)"
                        + " (menor=" + larguraMinima + ", maior=" + larguraMaxima + ")");
    }

    /**
     * nivelDesenho só deve influenciar a ORDEM em que os objetos são
     * desenhados (ver DesenhoProceduralCircuito/MainPanelEditor, que só o
     * usam para decidir quando chamar desenha()) — nunca o resultado de
     * desenha() em si. Relato de que o padrão "sumia" com nível zero: o
     * próprio desenha() nem lê nivelDesenho, então o resultado tem que ser
     * idêntico pixel a pixel pra qualquer valor do campo.
     */
    @Test
    void desenha_comQualquerNivelDesenho_produzOMesmoResultado() {
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, RELVA_SATURADA,
                ARBUSTO_SATURADO);

        objetoLivre.setNivelDesenho(-9);
        BufferedImage imagemNivelNegativo = renderiza(objetoLivre);
        int pixelsNivelNegativo = contarPixelsComMatizProxima(imagemNivelNegativo, RELVA_SATURADA)
                + contarPixelsComMatizProxima(imagemNivelNegativo, ARBUSTO_SATURADO);

        objetoLivre.setNivelDesenho(0);
        BufferedImage imagemNivelZero = renderiza(objetoLivre);
        int pixelsNivelZero = contarPixelsComMatizProxima(imagemNivelZero, RELVA_SATURADA)
                + contarPixelsComMatizProxima(imagemNivelZero, ARBUSTO_SATURADO);

        objetoLivre.setNivelDesenho(5);
        BufferedImage imagemNivelPositivo = renderiza(objetoLivre);
        int pixelsNivelPositivo = contarPixelsComMatizProxima(imagemNivelPositivo, RELVA_SATURADA)
                + contarPixelsComMatizProxima(imagemNivelPositivo, ARBUSTO_SATURADO);

        assertEquals(pixelsNivelNegativo, pixelsNivelZero,
                "nivelDesenho não deveria afetar se/como o padrão é desenhado");
        assertEquals(pixelsNivelZero, pixelsNivelPositivo,
                "nivelDesenho não deveria afetar se/como o padrão é desenhado");
    }

    /**
     * Água desenha seus arcos sem nunca setar o próprio traço, então herdava
     * o que sobrasse de um desenho anterior no mesmo Graphics2D (ex.: o
     * traço grosso de DesenhoProceduralCircuito.desenhaPistaBox, que fica
     * valendo até o próximo setStroke) — o que fazia o padrão aparecer
     * errado (traços grossos/borrados em vez de finos) só quando algo mais
     * grosso era desenhado antes do objeto na mesma imagem. Como isso
     * depende de quando o objeto é desenhado em relação a outras coisas,
     * dava a falsa impressão de que era o próprio nivelDesenho (que só
     * controla a ordem) quem decidia se o padrão aparecia certo.
     * VEGETACAO_SIMPLES saiu deste teste: agora desenha sprites via
     * drawImage, que não é afetado por Stroke.
     */
    @Test
    void desenha_comTracoGrossoJaSetadoNoGraphics2D_naoAlteraOPadraoDeAgua() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.AGUA, fundo, padrao);

        int pixelsComTracoFino = contarPixelsComCor(
                renderizaComTracoPrevio(objetoLivre, new java.awt.BasicStroke(1f)), padrao);
        int pixelsComTracoGrosso = contarPixelsComCor(
                renderizaComTracoPrevio(objetoLivre, new java.awt.BasicStroke(40f)), padrao);

        assertEquals(pixelsComTracoFino, pixelsComTracoGrosso,
                "o traço herdado de outro desenho no mesmo Graphics2D não deveria mudar o padrão de água");
    }

    private BufferedImage renderizaComTracoPrevio(ObjetoLivre objetoLivre, java.awt.Stroke tracoPrevio) {
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            g2d.setStroke(tracoPrevio);
            objetoLivre.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
        return imagem;
    }

    /**
     * Checkbox "Padrão" do editor (Global.padraoObjetoLivreCompleto):
     * desligado, qualquer ObjetoLivre não sólido passa a desenhar só UMA
     * marca do padrão, centralizada, em vez do preenchimento completo.
     */
    @Test
    void desenha_comPadraoObjetoLivreCompletoFalse_naoDesenhaNenhumaMarcaParaTiposNaoVegetais() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre brita = criarObjetoLivre(TipoObjetoLivre.BRITA, fundo, padrao);

        Global.padraoObjetoLivreCompleto = false;
        int marcasCentralizado = contarComponentesConectados(renderiza(brita), padrao);

        Global.padraoObjetoLivreCompleto = true;
        int marcasCompleto = contarComponentesConectados(renderiza(brita), padrao);

        assertEquals(0, marcasCentralizado,
                "esperava nenhuma marca de exemplo com o modo de preview desligado — só a borda magenta"
                        + " (marcas=" + marcasCentralizado + ")");
        assertTrue(marcasCompleto > marcasCentralizado,
                "esperava bem mais marcas com o preenchimento completo ligado (completo=" + marcasCompleto
                        + ", centralizado=" + marcasCentralizado + ")");
    }

    @Test
    void desenha_vegetacaoDensaComPadraoObjetoLivreCompletoFalse_desenhaUmaArvoreCentralizada() {
        ObjetoLivre densa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, TRONCO_SATURADO, COPA_SATURADA);

        Global.padraoObjetoLivreCompleto = false;
        BufferedImage imagem = renderiza(densa);

        assertEquals(1, contarComponentesConectadosPorMatiz(imagem, COPA_SATURADA),
                "esperava só uma árvore (copa) centralizada com o modo de preview desligado");
        assertTrue(imagemContemMatizProxima(imagem, TRONCO_SATURADO), "esperava o tronco da árvore centralizada");
    }

    /**
     * A regra de "não desenhar se cortar a borda" (cabeInteiraNaSilhueta)
     * também vale no modo centralizado: numa forma menor que a árvore, nem a
     * marca única deveria aparecer.
     */
    @Test
    void desenha_vegetacaoDensaComPadraoObjetoLivreCompletoFalse_naoDesenhaArvoreQueNaoCabe() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre densa = new ObjetoLivre();
        densa.setPontos(new ArrayList<>(Arrays.asList(
                new Point(0, 0), new Point(15, 0), new Point(7, 15))));
        densa.setTipo(TipoObjetoLivre.VEGETACAO_DENSA);
        densa.setCorPimaria(fundo);
        densa.setCorSecundaria(padrao);
        densa.gerar();

        Global.padraoObjetoLivreCompleto = false;
        BufferedImage imagem = renderiza(densa);

        assertFalse(imagemContemCor(imagem, padrao),
                "árvore centralizada maior que a forma não deveria ser desenhada (copa cortada pela borda)");
        assertFalse(imagemContemCor(imagem, fundo),
                "árvore centralizada maior que a forma não deveria ser desenhada (tronco cortado pela borda)");
    }
}
