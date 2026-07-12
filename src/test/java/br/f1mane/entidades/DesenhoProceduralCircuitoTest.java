package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Cobre a geração em memória da imagem de fundo do circuito: deve incluir os
 * objetos de cenário (Arquibancada, Construcao, GuardRails, Pneus) e excluir
 * Escapada/Transparencia, sem lançar exceção.
 */
class DesenhoProceduralCircuitoTest {

    private Circuito circuitoDeTeste() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.RETA));
        pista.add(criarNo(2000, 1000, No.CURVA_ALTA));
        pista.add(criarNo(2000, 2000, No.RETA));
        pista.add(criarNo(1000, 2000, No.CURVA_BAIXA));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());

        circuito.vetorizarPista(9, 1.5);

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(new ObjetoArquibancada());
        objetosCenario.add(new ObjetoConstrucao());
        objetosCenario.add(new ObjetoGuardRails());
        objetosCenario.add(new ObjetoPneus());
        for (ObjetoPista objetoPista : objetosCenario) {
            objetoPista.setPosicaoQuina(new Point(1200, 1200));
        }
        circuito.setObjetosCenario(objetosCenario);

        List<ObjetoPista> objetos = new ArrayList<>();
        objetos.add(new ObjetoEscapada());
        objetos.add(new ObjetoTransparencia());
        for (ObjetoPista objetoPista : objetos) {
            objetoPista.setPosicaoQuina(new Point(1200, 1200));
        }
        circuito.setObjetos(objetos);

        return circuito;
    }

    private No criarNo(int x, int y, java.awt.Color tipo) {
        No no = new No();
        no.setPoint(new Point(x, y));
        no.setTipo(tipo);
        return no;
    }

    @Test
    void desenha_naoLancaExcecao() {
        Circuito circuito = circuitoDeTeste();
        BufferedImage buffer = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = buffer.createGraphics();
        try {
            assertDoesNotThrow(() -> DesenhoProceduralCircuito.desenha(g2d, circuito, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void geraImagem_produzImagemComDimensaoMaiorQueZero() {
        Circuito circuito = circuitoDeTeste();

        BufferedImage imagem = assertDoesNotThrow(() -> DesenhoProceduralCircuito.geraImagem(circuito));

        assertTrue(imagem.getWidth() > 0);
        assertTrue(imagem.getHeight() > 0);
    }

    @Test
    void geraImagem_respeitaCorFundoECorAsfaltoDoCircuito() {
        Circuito circuito = circuitoDeTeste();
        Color fundo = new Color(10, 20, 30);
        Color asfalto = new Color(60, 70, 80);
        circuito.setCorFundo(fundo);
        circuito.setCorAsfalto(asfalto);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(fundo.getRGB(), imagem.getRGB(0, 0));
        assertEquals(asfalto.getRGB(), imagem.getRGB(1500, 1000));
    }

    // ---- níveis de desenho em relação à pista ----

    private Circuito circuitoComObjetoSobreAPista(int nivel, Color corObjeto) {
        Circuito circuito = circuitoDeTeste();
        circuito.setCorAsfalto(new Color(60, 70, 80));

        // Quadrado cobrindo (1500,1000), ponto que fica sobre o asfalto da reta
        ObjetoLivre objetoLivre = new ObjetoLivre();
        objetoLivre.setCorPimaria(corObjeto);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(1400, 900));
        pontos.add(new Point(1600, 900));
        pontos.add(new Point(1600, 1100));
        pontos.add(new Point(1400, 1100));
        objetoLivre.setPontos(pontos);
        objetoLivre.gerar();
        objetoLivre.setPosicaoQuina(objetoLivre.obterArea().getLocation());
        objetoLivre.setNivelDesenho(nivel);

        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(objetoLivre);
        circuito.setObjetosCenario(objetosCenario);
        return circuito;
    }

    @Test
    void geraImagem_objetoNivelMenosUm_ficaAbaixoDoAsfalto() {
        Color corObjeto = new Color(200, 10, 200);
        Circuito circuito = circuitoComObjetoSobreAPista(-1, corObjeto);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(circuito.getCorAsfalto().getRGB(), imagem.getRGB(1500, 1000),
                "objeto no nível -1 deveria ficar coberto pelo asfalto");
        assertTrue(imagemContemCor(imagem, corObjeto),
                "as partes do objeto fora da pista continuam visíveis");
    }

    @Test
    void geraImagem_objetoNivelZero_ficaAcimaDoAsfalto() {
        Color corObjeto = new Color(200, 10, 200);
        Circuito circuito = circuitoComObjetoSobreAPista(0, corObjeto);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(corObjeto.getRGB(), imagem.getRGB(1500, 1000),
                "objeto no nível 0 deveria ser desenhado por cima do asfalto (comportamento antigo)");
    }

    private ObjetoLivre quadradoOpaco(int x, int y, int lado, Color cor, int nivel) {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setCorPimaria(cor);
        List<Point> pontos = new ArrayList<>();
        pontos.add(new Point(x, y));
        pontos.add(new Point(x + lado, y));
        pontos.add(new Point(x + lado, y + lado));
        pontos.add(new Point(x, y + lado));
        objeto.setPontos(pontos);
        objeto.gerar();
        objeto.setPosicaoQuina(objeto.obterArea().getLocation());
        objeto.setNivelDesenho(nivel);
        return objeto;
    }

    @Test
    void geraImagem_niveisNegativosSemLimite_maisPertoDeZeroFicaPorCima() {
        // Fora dos limites da pista (que vai de 1000,1000 a 2000,2000), pra
        // isolar só a ordem entre os dois níveis negativos, sem influência
        // do asfalto por cima.
        Circuito circuito = circuitoDeTeste();
        Color corFundo = new Color(10, 10, 10);
        Color corTopo = new Color(220, 220, 10);
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(quadradoOpaco(100, 100, 100, corFundo, -20));
        objetosCenario.add(quadradoOpaco(100, 100, 100, corTopo, -1));
        circuito.setObjetosCenario(objetosCenario);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(corTopo.getRGB(), imagem.getRGB(150, 150),
                "nível -1 (mais perto de zero) deveria ficar por cima do nível -20 (mais no fundo)");
    }

    @Test
    void geraImagem_niveisPositivosSemLimite_maiorFicaPorCima() {
        Circuito circuito = circuitoDeTeste();
        Color corBase = new Color(10, 10, 10);
        Color corTopo = new Color(220, 220, 10);
        List<ObjetoPista> objetosCenario = new ArrayList<>();
        objetosCenario.add(quadradoOpaco(100, 100, 100, corBase, 1));
        objetosCenario.add(quadradoOpaco(100, 100, 100, corTopo, 20));
        circuito.setObjetosCenario(objetosCenario);

        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);

        assertEquals(corTopo.getRGB(), imagem.getRGB(150, 150),
                "nível 20 deveria ficar por cima do nível 1 — sem limite, quanto maior, mais em cima");
    }

    // ---- circuito vazio (editor sem nenhum circuito carregado) ----

    /**
     * Regressão: com o editor não carregando mais nenhum circuito ao
     * iniciar (melhorias-editor-circuito), MainPanelEditor.paintComponent()
     * passa a chamar desenhaPistaZebraEBox() com um Circuito recém-criado
     * (pistaKey/boxKey vazios) — antes disso nunca acontecia, pois o editor
     * sempre carregava um circuito real primeiro.
     */
    @Test
    void desenhaPistaZebraEBox_comCircuitoVazio_naoLancaExcecao() {
        Circuito circuito = new Circuito();

        BufferedImage imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            assertDoesNotThrow(() -> DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    // ---- zebra e box customizáveis (circuito-cores-box-zebra) ----

    @Test
    void desenhaPistaZebraEBox_usaCorZebraCustomizadaQuandoDefinida() {
        Circuito circuito = circuitoDeTeste();
        Color zebra1 = new Color(11, 22, 33);
        Color zebra2 = new Color(44, 55, 66);
        circuito.setCorZebra1(zebra1);
        circuito.setCorZebra2(zebra2);

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, zebra1), "esperava encontrar corZebra1 customizada na imagem");
        assertTrue(imagemContemCor(imagem, zebra2), "esperava encontrar corZebra2 customizada na imagem");
        assertTrue(!imagemContemCor(imagem, Color.RED), "não deveria mais usar o vermelho padrão");
        assertTrue(imagemContemCor(imagem, Color.WHITE), "a borda fora das curvas deveria continuar branca");
    }

    @Test
    void desenhaPistaZebraEBox_coresCustomizadasFicamRestritasAsCurvas() {
        Circuito circuito = circuitoDeTeste();
        Color zebra1 = new Color(11, 22, 33);
        Color zebra2 = new Color(44, 55, 66);
        circuito.setCorZebra1(zebra1);
        circuito.setCorZebra2(zebra2);

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        // No circuito de teste, o único segmento com zebra sai do nó
        // CURVA_ALTA em (2000,1000) até (2000,2000): qualquer pixel com as
        // cores customizadas deve estar perto dessa vertical (x ~ 2000,
        // folga generosa para a largura do traço), nunca nas retas.
        for (int x = 0; x < imagem.getWidth(); x++) {
            for (int y = 0; y < imagem.getHeight(); y++) {
                int rgb = imagem.getRGB(x, y);
                if (rgb == zebra1.getRGB() || rgb == zebra2.getRGB()) {
                    assertTrue(x > 1700,
                            "cor customizada fora da zona de curva, em x=" + x + " y=" + y);
                }
            }
        }
    }

    @Test
    void desenhaPistaZebraEBox_semCoresDefinidas_usaBrancoVermelhoPadrao() {
        Circuito circuito = circuitoDeTeste();

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, Color.WHITE), "esperava fallback para branco");
        assertTrue(imagemContemCor(imagem, Color.RED), "esperava fallback para vermelho");
    }

    @Test
    void desenhaPistaZebraEBox_apenasUmaCorDefinida_usaFallbackSoNaOutra() {
        Circuito circuito = circuitoDeTeste();
        Color zebra2 = new Color(44, 55, 66);
        circuito.setCorZebra2(zebra2);
        // corZebra1 permanece null: só ela cai no fallback branco; corZebra2
        // customizada já vale sozinha nas listras da curva

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, Color.WHITE), "esperava fallback para branco na cor não definida");
        assertTrue(imagemContemCor(imagem, zebra2), "corZebra2 sozinha já deveria aparecer nas listras da curva");
        assertTrue(!imagemContemCor(imagem, Color.RED), "não deveria usar o vermelho padrão com corZebra2 definida");
    }

    // ---- borda branca do box com interseção suprimida (melhorias-editor-circuito) ----

    @Test
    void desenhaPistaZebraEBox_desenhaBordaBrancaAoRedorDoTracadoDoBoxLongeDaPista() {
        Circuito circuito = circuitoComBoxDeTeste();
        // O segmento (2100,2400)-(2600,2400) do box fica bem longe do
        // retângulo da pista (1000,1000)-(2000,2000): a borda branca deveria
        // aparecer ali, fora do próprio stroke cinza central do box.
        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertEquals(Color.WHITE.getRGB(), imagem.getRGB(2350, 2370),
                "esperava borda branca do box no trecho livre, fora da linha cinza central e longe da pista");
    }

    /**
     * Regressão: diferente da pista (um loop fechado), o traçado do box vai
     * da entrada até a saída sem voltar ao primeiro nó — desenhaPistaBox()
     * original já tratava isso como caminho aberto (a linha de "fechamento"
     * ali é um no-op, do último nó pra ele mesmo). A primeira versão de
     * desenhaTintaPistaBox() fechava o caminho de volta ao primeiro nó do
     * box (mesma lógica usada pra pista), criando uma faixa branca larga e
     * espúria entre o primeiro e o último nó do box.
     */
    @Test
    void desenhaPistaZebraEBox_naoFechaOTracadoDoBoxDeVoltaAoPrimeiroNo() {
        Circuito circuito = circuitoComBoxDeTeste();
        // Box: (2100,1900) -> (2100,2400) -> (2600,2400). O "fechamento"
        // indevido ligaria (2600,2400) de volta a (2100,1900) — ponto médio
        // dessa diagonal, longe de ambos os segmentos reais do box e da
        // pista, não deveria ter nada desenhado.
        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertEquals(Color.BLACK.getRGB(), imagem.getRGB(2350, 2150),
                "não deveria haver nenhum desenho no ponto médio do fechamento indevido entrada-saída do box");
    }

    // ---- box customizável (circuito-cores-box-zebra) ----

    private Circuito circuitoComBoxDeTeste() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.RETA));
        pista.add(criarNo(2000, 1000, No.CURVA_ALTA));
        pista.add(criarNo(2000, 2000, No.RETA));
        pista.add(criarNo(1000, 2000, No.CURVA_BAIXA));
        circuito.setPista(pista);

        // Posicionado perto do canto (2000,2000) da pista (não perto do início do
        // loop, índice 0) para que o índice de entrada do box calculado por
        // vetorizarPista fique longe o bastante do início de pistaFull.
        List<No> box = new ArrayList<>();
        box.add(criarNo(2100, 1900, No.RETA));
        box.add(criarNo(2100, 2400, No.PARADA_BOX));
        box.add(criarNo(2600, 2400, No.RETA));
        circuito.setBox(box);

        circuito.vetorizarPista(9, 1.5);
        return circuito;
    }

    @Test
    void desenhaVagasBox_usaCorBoxCustomizadaQuandoDefinida() {
        Circuito circuito = circuitoComBoxDeTeste();
        Color box1 = new Color(10, 200, 30);
        Color box2 = new Color(220, 15, 90);
        circuito.setCorBox1(box1);
        circuito.setCorBox2(box2);

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, 1.0, false);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, corSobrePreto(box1, 150)),
                "esperava encontrar corBox1 customizada (com a mesma transparência) na imagem");
        assertTrue(imagemContemCor(imagem, corSobrePreto(box2, 150)),
                "esperava encontrar corBox2 customizada (com a mesma transparência) na imagem");
    }

    @Test
    void desenhaVagasBox_semCoresDefinidas_usaCianoMagentaPadrao() {
        Circuito circuito = circuitoComBoxDeTeste();

        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, 1.0, false);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, corSobrePreto(Color.CYAN, 150)), "esperava fallback para ciano");
        assertTrue(imagemContemCor(imagem, corSobrePreto(Color.MAGENTA, 150)), "esperava fallback para magenta");
    }

    /**
     * As bolinhas de "lado do box" são só do editor (modoEditor=true) — a
     * imagem final de corrida (modoEditor=false, o que {@link
     * DesenhoProceduralCircuito#desenha} sempre usa) não deve mostrá-las.
     */
    @Test
    void desenhaVagasBox_bolinhaDeLadoDoBox_soApareceEmModoEditor() {
        Circuito circuito = circuitoComBoxDeTeste();
        circuito.setLadoBox(1);

        BufferedImage imagemEditor = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dEditor = imagemEditor.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2dEditor, circuito, 1.0, true);
        } finally {
            g2dEditor.dispose();
        }
        assertTrue(imagemContemCor(imagemEditor, Color.BLUE),
                "modoEditor=true deveria desenhar a bolinha azul indicando o lado do box");

        BufferedImage imagemCorrida = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2dCorrida = imagemCorrida.createGraphics();
        try {
            DesenhoProceduralCircuito.desenhaVagasBox(g2dCorrida, circuito, 1.0, false);
        } finally {
            g2dCorrida.dispose();
        }
        assertTrue(!imagemContemCor(imagemCorrida, Color.BLUE),
                "modoEditor=false (imagem de corrida) não deveria desenhar a bolinha de lado do box");
    }

    // ---- linha de largada quadriculada (largada-bandeira-quadriculada) ----

    private Circuito circuitoComLargada() {
        Circuito circuito = new Circuito();

        List<No> pista = new ArrayList<>();
        pista.add(criarNo(1000, 1000, No.LARGADA));
        pista.add(criarNo(2000, 1000, No.RETA));
        pista.add(criarNo(2000, 2000, No.CURVA_ALTA));
        pista.add(criarNo(1000, 2000, No.CURVA_BAIXA));
        circuito.setPista(pista);
        circuito.setBox(new ArrayList<>());

        circuito.vetorizarPista(9, 1.5);
        return circuito;
    }

    @Test
    void desenhaLinhaDeLargada_naoLancaExcecao_paraCircuitoComNoDeLargada() {
        Circuito circuito = circuitoComLargada();
        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            assertDoesNotThrow(() -> DesenhoProceduralCircuito.desenhaLinhaDeLargada(g2d, circuito, 1.0));
        } finally {
            g2d.dispose();
        }
    }

    @Test
    void desenhaLinhaDeLargada_naoDesenhaNada_paraCircuitoSemNoDeLargada() {
        // circuitoDeTeste() só tem RETA/CURVA_ALTA/CURVA_BAIXA, nenhum LARGADA.
        Circuito circuito = circuitoDeTeste();
        Color corFundo = new Color(128, 128, 128);
        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            g2d.setColor(corFundo);
            g2d.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
            assertDoesNotThrow(() -> DesenhoProceduralCircuito.desenhaLinhaDeLargada(g2d, circuito, 1.0));
        } finally {
            g2d.dispose();
        }

        assertTrue(!imagemContemCor(imagem, Color.BLACK), "sem nó de largada, nada deveria ter sido desenhado");
        assertTrue(!imagemContemCor(imagem, Color.WHITE), "sem nó de largada, nada deveria ter sido desenhado");
    }

    @Test
    void desenhaLinhaDeLargada_desenhaQuadriculadoPretoEBrancoPertoDoNoDeLargada() {
        Circuito circuito = circuitoComLargada();
        Color corFundo = new Color(128, 128, 128);
        BufferedImage imagem = new BufferedImage(3000, 3000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            g2d.setColor(corFundo);
            g2d.fillRect(0, 0, imagem.getWidth(), imagem.getHeight());
            DesenhoProceduralCircuito.desenhaLinhaDeLargada(g2d, circuito, 1.0);
        } finally {
            g2d.dispose();
        }

        assertTrue(imagemContemCor(imagem, Color.BLACK), "esperava quadrados pretos na linha de largada");
        assertTrue(imagemContemCor(imagem, Color.WHITE), "esperava quadrados brancos na linha de largada");

        // O nó de largada fica em (1000,1000); a faixa quadriculada é pequena
        // (largura da pista x um par de quadrados ao longo da pista), então
        // pixels pretos/brancos só deveriam aparecer perto desse ponto, não
        // no restante da imagem (fundo cinza, sem mais nada desenhado).
        for (int x = 0; x < imagem.getWidth(); x++) {
            for (int y = 0; y < imagem.getHeight(); y++) {
                int rgb = imagem.getRGB(x, y) & 0xFFFFFF;
                if (rgb == Color.BLACK.getRGB() || rgb == (Color.WHITE.getRGB() & 0xFFFFFF)) {
                    assertTrue(Math.abs(x - 1000) < 200 && Math.abs(y - 1000) < 200,
                            "pixel do xadrez fora da vizinhança esperada do nó de largada, em x=" + x + " y=" + y);
                }
            }
        }
    }

    /** Varre a imagem inteira procurando um pixel com exatamente essa cor (RGB, ignora alpha). */
    private static boolean imagemContemCor(BufferedImage imagem, Color cor) {
        int alvo = cor.getRGB() & 0xFFFFFF;
        for (int y = 0; y < imagem.getHeight(); y++) {
            for (int x = 0; x < imagem.getWidth(); x++) {
                if ((imagem.getRGB(x, y) & 0xFFFFFF) == alvo) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Combina {@code cor} com o alpha informado sobre um fundo preto opaco, usando o mesmo compositing do Graphics2D. */
    private static Color corSobrePreto(Color cor, int alpha) {
        BufferedImage sonda = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = sonda.createGraphics();
        try {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 1, 1);
            g2d.setColor(new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), alpha));
            g2d.fillRect(0, 0, 1, 1);
        } finally {
            g2d.dispose();
        }
        return new Color(sonda.getRGB(0, 0));
    }
}
