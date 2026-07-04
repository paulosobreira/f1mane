package br.f1mane.entidades;

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

import org.junit.jupiter.api.Test;

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
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setPontos(new ArrayList<>(Arrays.asList(
                new Point(5050, 732), new Point(4995, 1633), new Point(6056, 1686),
                new Point(6675, 1658), new Point(6788, 1660), new Point(6871, 1665),
                new Point(7265, 650), new Point(7266, 16), new Point(5059, 21))));
        objetoLivre.setTipo(TipoObjetoLivre.VEGETACAO_SIMPLES);
        objetoLivre.setCorPimaria(fundo);
        objetoLivre.setCorSecundaria(padrao);
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

        assertTrue(imagemContemCor(imagem, fundo));
        assertTrue(imagemContemCor(imagem, padrao),
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
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();
        boolean[][] visitado = new boolean[largura][altura];
        int[] filaX = new int[largura * altura];
        int[] filaY = new int[largura * altura];
        List<Rectangle> blobs = new ArrayList<>();
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                if (visitado[x][y] || imagem.getRGB(x, y) != alvo) {
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
                                && imagem.getRGB(vx, vy) == alvo) {
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
    void desenha_vegetacaoDensaESimples_naoFicamAlinhadasNumaGrade() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        for (TipoObjetoLivre tipo : new TipoObjetoLivre[] {
                TipoObjetoLivre.VEGETACAO_DENSA, TipoObjetoLivre.VEGETACAO_SIMPLES }) {
            ObjetoLivre objetoLivre = criarObjetoLivre(tipo, fundo, padrao);

            BufferedImage imagem = renderiza(objetoLivre);
            int alvo = padrao.getRGB();
            java.util.Set<Integer> colunasComPadrao = new java.util.HashSet<>();
            for (int y = 0; y < imagem.getHeight(); y++) {
                for (int x = 0; x < imagem.getWidth(); x++) {
                    if (imagem.getRGB(x, y) == alvo) {
                        colunasComPadrao.add(x);
                    }
                }
            }
            // Numa grade alinhada com deslocamento fixo por linha, as marcas
            // cairiam num conjunto pequeno de colunas periódicas; espalhadas,
            // ocupam muitas colunas diferentes dentro da forma.
            assertTrue(colunasComPadrao.size() > 50,
                    tipo + ": esperava as marcas espalhadas por muitas colunas diferentes, não alinhadas em grade"
                            + " (colunas=" + colunasComPadrao.size() + ")");
        }
    }

    @Test
    void desenha_vegetacaoDensa_temTamanhoBemVariadoEntreAsMarcas() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, fundo, padrao);

        List<Rectangle> blobs = bordasDosComponentes(renderiza(objetoLivre), padrao);
        assertTrue(blobs.size() >= 3, "esperava várias touceiras para medir variação de tamanho");

        int menor = Integer.MAX_VALUE;
        int maior = Integer.MIN_VALUE;
        for (Rectangle blob : blobs) {
            int area = blob.width * blob.height;
            menor = Math.min(menor, area);
            maior = Math.max(maior, area);
        }
        assertTrue(maior > menor * 2,
                "esperava touceiras de tamanhos bem diferentes entre si (menor=" + menor + ", maior=" + maior + ")");
    }

    @Test
    void desenha_vegetacaoSimples_temVariacaoDeTamanhoBemMaisLeveQueADensa() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre simples = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, fundo, padrao);
        ObjetoLivre densa = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_DENSA, fundo, padrao);

        double amplitudeSimples = amplitudeRelativaDeTamanho(bordasDosComponentes(renderiza(simples), padrao));
        double amplitudeDensa = amplitudeRelativaDeTamanho(bordasDosComponentes(renderiza(densa), padrao));

        assertTrue(amplitudeSimples < amplitudeDensa,
                "esperava a vegetação simples ter variação de tamanho bem mais leve que a densa"
                        + " (simples=" + amplitudeSimples + ", densa=" + amplitudeDensa + ")");
    }

    private static double amplitudeRelativaDeTamanho(List<Rectangle> blobs) {
        int menor = Integer.MAX_VALUE;
        int maior = Integer.MIN_VALUE;
        for (Rectangle blob : blobs) {
            int area = blob.width * blob.height;
            menor = Math.min(menor, area);
            maior = Math.max(maior, area);
        }
        return menor == 0 ? maior : (double) maior / menor;
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
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        ObjetoLivre objetoLivre = criarObjetoLivre(TipoObjetoLivre.VEGETACAO_SIMPLES, fundo, padrao);

        objetoLivre.setNivelDesenho(-9);
        int pixelsNivelNegativo = contarPixelsComCor(renderiza(objetoLivre), padrao);

        objetoLivre.setNivelDesenho(0);
        int pixelsNivelZero = contarPixelsComCor(renderiza(objetoLivre), padrao);

        objetoLivre.setNivelDesenho(5);
        int pixelsNivelPositivo = contarPixelsComCor(renderiza(objetoLivre), padrao);

        assertEquals(pixelsNivelNegativo, pixelsNivelZero,
                "nivelDesenho não deveria afetar se/como o padrão é desenhado");
        assertEquals(pixelsNivelZero, pixelsNivelPositivo,
                "nivelDesenho não deveria afetar se/como o padrão é desenhado");
    }

    /**
     * Água e vegetação simples desenham suas linhas/arcos sem nunca setar o
     * próprio traço, então herdavam o que sobrasse de um desenho anterior no
     * mesmo Graphics2D (ex.: o traço grosso de
     * DesenhoProceduralCircuito.desenhaPistaBox, que fica valendo até o
     * próximo setStroke) — o que fazia o padrão aparecer errado (traços
     * grossos/borrados em vez de finos) só quando algo mais grosso era
     * desenhado antes do objeto na mesma imagem. Como isso depende de quando
     * o objeto é desenhado em relação a outras coisas, dava a falsa
     * impressão de que era o próprio nivelDesenho (que só controla a ordem)
     * quem decidia se o padrão aparecia certo.
     */
    @Test
    void desenha_comTracoGrossoJaSetadoNoGraphics2D_naoAlteraOPadraoDeAguaOuVegetacaoSimples() {
        Color fundo = new Color(10, 20, 30, 255);
        Color padrao = new Color(200, 210, 220, 255);
        for (TipoObjetoLivre tipo : new TipoObjetoLivre[] { TipoObjetoLivre.AGUA, TipoObjetoLivre.VEGETACAO_SIMPLES }) {
            ObjetoLivre objetoLivre = criarObjetoLivre(tipo, fundo, padrao);

            int pixelsComTracoFino = contarPixelsComCor(
                    renderizaComTracoPrevio(objetoLivre, new java.awt.BasicStroke(1f)), padrao);
            int pixelsComTracoGrosso = contarPixelsComCor(
                    renderizaComTracoPrevio(objetoLivre, new java.awt.BasicStroke(40f)), padrao);

            assertEquals(pixelsComTracoFino, pixelsComTracoGrosso,
                    "o traço herdado de outro desenho no mesmo Graphics2D não deveria mudar o padrão de " + tipo);
        }
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
}
