package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

/**
 * Cobre os quatro tipos de forma de {@link ObjetoConstrucao}
 * ({@link TipoObjetoConstrucao#QUADRADO}, {@code REDONDO}, {@code CAMINHAO},
 * {@code BARCO}) e o empilhamento (quantidade/direção/grau), que é uma
 * propriedade transversal a qualquer tipo — não um tipo à parte ("prédio").
 */
class ObjetoConstrucaoTest {

    private ObjetoConstrucao criarObjeto(TipoObjetoConstrucao tipo) {
        ObjetoConstrucao objeto = new ObjetoConstrucao();
        objeto.setTipo(tipo);
        objeto.setPosicaoQuina(new Point(20, 20));
        objeto.setLargura(100);
        objeto.setAltura(60);
        return objeto;
    }

    private void renderizar(ObjetoConstrucao objeto) {
        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            objeto.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }
    }

    /** Vão entre os dois módulos do CAMINHAO — mesmo valor de ObjetoConstrucao.MARGEM_INTERNA (privado). */
    private static final int GAP_CAMINHAO = 10;

    /**
     * Largura de uma única forma para o tipo dado: igual a {@code largura}
     * para a maioria, mas CAMINHAO soma o vão entre os dois módulos (cabine
     * + vão + carroceria), que fica fora do valor de {@code largura} em si.
     */
    private int larguraEsperada(TipoObjetoConstrucao tipo, int largura, int altura) {
        return tipo == TipoObjetoConstrucao.CAMINHAO ? largura + GAP_CAMINHAO : largura;
    }

    @Test
    void construtor_padraoEhQuadradoComQuantidadeEmpilhamentoUm() {
        ObjetoConstrucao objeto = new ObjetoConstrucao();

        assertEquals(TipoObjetoConstrucao.QUADRADO, objeto.getTipo());
        assertEquals(1, objeto.getQuantidadeEmpilhamento());
    }

    @Test
    void setTipo_comNull_voltaParaQuadrado() {
        ObjetoConstrucao objeto = new ObjetoConstrucao();
        objeto.setTipo(TipoObjetoConstrucao.BARCO);

        objeto.setTipo(null);

        assertEquals(TipoObjetoConstrucao.QUADRADO, objeto.getTipo());
    }

    @Test
    void setDirecaoEmpilhamento_comNull_voltaParaPadrao() {
        ObjetoConstrucao objeto = new ObjetoConstrucao();
        objeto.setDirecaoEmpilhamento(DirecaoEmpilhamento.BAIXO_ESQUERDA);

        objeto.setDirecaoEmpilhamento(null);

        assertEquals(DirecaoEmpilhamento.CIMA_DIREITA, objeto.getDirecaoEmpilhamento());
    }

    @Test
    void setQuantidadeEmpilhamento_naoAceitaMenorQueUm() {
        ObjetoConstrucao objeto = new ObjetoConstrucao();

        objeto.setQuantidadeEmpilhamento(0);

        assertEquals(1, objeto.getQuantidadeEmpilhamento());
    }

    @Test
    void obterArea_antesDoPrimeiroDesenho_naoLancaExcecaoParaQualquerTipo() {
        for (TipoObjetoConstrucao tipo : TipoObjetoConstrucao.values()) {
            ObjetoConstrucao objeto = new ObjetoConstrucao();
            objeto.setTipo(tipo);

            assertDoesNotThrow(objeto::obterArea, "tipo " + tipo + " não deveria lançar exceção");
        }
    }

    @Test
    void desenhaEObterArea_todosOsQuatroTipos_semQuantidadeEmpilhamento_naoLancaExcecao() {
        for (TipoObjetoConstrucao tipo : TipoObjetoConstrucao.values()) {
            ObjetoConstrucao objeto = criarObjeto(tipo);

            assertDoesNotThrow(() -> renderizar(objeto), "tipo " + tipo + " não deveria lançar exceção ao desenhar");

            Rectangle area = objeto.obterArea();
            assertEquals(new Rectangle(20, 20, larguraEsperada(tipo, 100, 60), 60), area,
                    "tipo " + tipo + " deveria cobrir uma única forma");
        }
    }

    @Test
    void empilhamento_funcionaIgualParaQualquerTipo_naoSoQuadrado() {
        for (TipoObjetoConstrucao tipo : TipoObjetoConstrucao.values()) {
            ObjetoConstrucao objeto = criarObjeto(tipo);
            objeto.setQuantidadeEmpilhamento(4);
            objeto.setDirecaoEmpilhamento(DirecaoEmpilhamento.CIMA);
            objeto.setGrauEmpilhamento(10);

            assertDoesNotThrow(() -> renderizar(objeto),
                    "empilhar tipo " + tipo + " não deveria lançar exceção");

            Rectangle area = objeto.obterArea();
            // 4 repetições deslocadas 10px pra cima (cumulativo: 0,10,20,30) -> altura total = 60 + 30 = 90.
            assertEquals(90, area.height,
                    "tipo " + tipo + " empilhado 4x com grau 10 deveria somar 30px extras de altura");
            assertEquals(larguraEsperada(tipo, 100, 60), area.width,
                    "empilhamento CIMA não deveria alterar a largura de cada forma");
        }
    }

    @Test
    void quantidadeEmpilhamentoUm_naoGeraRepeticao_paraQualquerTipo() {
        for (TipoObjetoConstrucao tipo : TipoObjetoConstrucao.values()) {
            ObjetoConstrucao objeto = criarObjeto(tipo);
            objeto.setQuantidadeEmpilhamento(1);
            objeto.setGrauEmpilhamento(50);

            renderizar(objeto);

            assertEquals(new Rectangle(20, 20, larguraEsperada(tipo, 100, 60), 60), objeto.obterArea(),
                    "tipo " + tipo + " com quantidadeEmpilhamento=1 não deveria ter área maior que uma forma única");
        }
    }

    @Test
    void aumentarLarguraOuAltura_naoAlteraQuantidadeDeRepeticoes() {
        ObjetoConstrucao objeto = criarObjeto(TipoObjetoConstrucao.QUADRADO);
        objeto.setQuantidadeEmpilhamento(3);
        objeto.setDirecaoEmpilhamento(DirecaoEmpilhamento.DIREITA);
        objeto.setGrauEmpilhamento(5);
        renderizar(objeto);
        int larguraAntes = objeto.obterArea().width;

        objeto.setLargura(200);
        renderizar(objeto);

        // 3 repetições, deslocamento total de 2*5=10px + a largura de cada forma (agora 200).
        assertEquals(210, objeto.obterArea().width);
        assertTrue(objeto.obterArea().width > larguraAntes,
                "aumentar largura deveria aumentar a área total (forma maior), não a quantidade de repetições");
    }

    @Test
    void aumentarQuantidadeEmpilhamento_naoAlteraTamanhoDeCadaForma() {
        ObjetoConstrucao objeto = criarObjeto(TipoObjetoConstrucao.QUADRADO);
        objeto.setDirecaoEmpilhamento(DirecaoEmpilhamento.DIREITA);
        objeto.setGrauEmpilhamento(0);
        objeto.setQuantidadeEmpilhamento(1);
        renderizar(objeto);
        int larguraUmaForma = objeto.obterArea().width;

        objeto.setQuantidadeEmpilhamento(10);
        renderizar(objeto);

        // grauEmpilhamento=0: todas as repetições se sobrepõem exatamente, então a largura total continua sendo a de uma forma só.
        assertEquals(larguraUmaForma, objeto.obterArea().width,
                "com grauEmpilhamento=0, empilhar não deveria mudar o tamanho da área (formas sobrepostas)");
    }

    @Test
    void grauEmpilhamento_deslocamentoEmPixels_naoEmFracaoDeLarguraAltura() {
        ObjetoConstrucao objetoGrauPequeno = criarObjeto(TipoObjetoConstrucao.QUADRADO);
        objetoGrauPequeno.setDirecaoEmpilhamento(DirecaoEmpilhamento.BAIXO);
        objetoGrauPequeno.setQuantidadeEmpilhamento(2);
        objetoGrauPequeno.setGrauEmpilhamento(5);
        renderizar(objetoGrauPequeno);

        ObjetoConstrucao objetoGrauGrande = criarObjeto(TipoObjetoConstrucao.QUADRADO);
        objetoGrauGrande.setDirecaoEmpilhamento(DirecaoEmpilhamento.BAIXO);
        objetoGrauGrande.setQuantidadeEmpilhamento(2);
        objetoGrauGrande.setGrauEmpilhamento(50);
        renderizar(objetoGrauGrande);

        // Mesma largura/altura (100x60) nos dois casos: só grauEmpilhamento muda,
        // e a diferença de altura total deve refletir exatamente essa diferença em pixels (45px).
        int alturaPequena = objetoGrauPequeno.obterArea().height;
        int alturaGrande = objetoGrauGrande.obterArea().height;
        assertEquals(45, alturaGrande - alturaPequena,
                "a diferença de área entre grauEmpilhamento=5 e grauEmpilhamento=50 (2 repetições) deveria ser exatamente 45px, não uma fração da forma");
    }

    @Test
    void barco_afunilamentoMaior_naoLancaExcecaoEContinuaDentroDaLargura() {
        ObjetoConstrucao barco = criarObjeto(TipoObjetoConstrucao.BARCO);
        barco.setAfunilamento(80);

        assertDoesNotThrow(() -> renderizar(barco));
        assertEquals(new Rectangle(20, 20, 100, 60), barco.obterArea());
    }

    @Test
    void setAfunilamento_ficaLimitadoEntreZeroENoventa() {
        ObjetoConstrucao objeto = new ObjetoConstrucao();

        objeto.setAfunilamento(150);
        assertEquals(90, objeto.getAfunilamento());

        objeto.setAfunilamento(-10);
        assertEquals(0, objeto.getAfunilamento());
    }

    /**
     * Caminhão: cabine (módulo 1) é largura/3, carroceria (módulo 2) é o
     * dobro disso (2×largura/3), com um vão de 10px entre os dois — tanto
     * largura quanto altura afetam o tamanho (nenhum dos dois fica sem
     * efeito).
     */
    @Test
    void caminhao_larguraEAlturaAmbasAfetamOTamanho() {
        ObjetoConstrucao caminhao = criarObjeto(TipoObjetoConstrucao.CAMINHAO);
        caminhao.setLargura(90);
        renderizar(caminhao);
        Rectangle areaLargura90 = caminhao.obterArea();

        caminhao.setLargura(180);
        renderizar(caminhao);
        Rectangle areaLargura180 = caminhao.obterArea();

        assertEquals(90 + GAP_CAMINHAO, areaLargura90.width);
        assertEquals(180 + GAP_CAMINHAO, areaLargura180.width);
        assertTrue(areaLargura180.width > areaLargura90.width,
                "aumentar largura deveria aumentar a largura total do caminhão");

        caminhao.setAltura(120);
        renderizar(caminhao);

        assertEquals(120, caminhao.obterArea().height, "altura continua controlando a altura de cada módulo");
    }

    /**
     * Cada módulo do caminhão desenha a mesma composição aninhada
     * (externa em corPimaria, interna com margem em corSecundaria) que
     * QUADRADO/REDONDO — não um retângulo liso — então a cor secundária
     * deve aparecer no centro de cada um dos dois módulos, com um vão de
     * 10px (a mesma distância da margem interna/externa) entre eles.
     */
    @Test
    void caminhao_cadaModuloTemDesenhoInternoComCorSecundaria() {
        ObjetoConstrucao caminhao = new ObjetoConstrucao();
        caminhao.setTipo(TipoObjetoConstrucao.CAMINHAO);
        caminhao.setPosicaoQuina(new Point(20, 20));
        caminhao.setLargura(90);
        caminhao.setAltura(60);
        caminhao.setCorPimaria(new Color(10, 10, 10));
        caminhao.setCorSecundaria(new Color(200, 200, 200));

        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            caminhao.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }

        int larguraCabine = 30; // 90/3
        int larguraCarroceria = 60; // 90-30
        // Centro da cabine (módulo 1, 30x60 em x=20..50).
        int corNoCentroCabine = imagem.getRGB(20 + larguraCabine / 2, 20 + 30);
        // Centro da carroceria (módulo 2, 60x60, logo após o vão de 10px: x=60..120).
        int corNoCentroCarroceria = imagem.getRGB(20 + larguraCabine + GAP_CAMINHAO + larguraCarroceria / 2, 20 + 30);
        // No vão entre os dois módulos: não deveria ter nada desenhado (transparente).
        int corNoVao = imagem.getRGB(20 + larguraCabine + GAP_CAMINHAO / 2, 20 + 30);

        assertEquals(caminhao.getCorSecundaria().getRGB(), corNoCentroCabine,
                "centro da cabine deveria ter a cor secundária (desenho interno), não a primária sólida");
        assertEquals(caminhao.getCorSecundaria().getRGB(), corNoCentroCarroceria,
                "centro da carroceria deveria ter a cor secundária (desenho interno), não a primária sólida");
        assertEquals(new Color(0, 0, 0, 0).getRGB(), corNoVao,
                "o vão entre os dois módulos não deveria ter nada desenhado");
    }

    /**
     * Barco: o objeto interior segue o perfil do exterior (mesma proa
     * afunilada, mesma popa arredondada), como um preenchimento — a cor
     * secundária deve aparecer no meio do corpo do barco, e a cor primária
     * continua visível perto da borda (a "moldura" de 10px).
     */
    @Test
    void barco_temPreenchimentoInternoSeguindoOPerfilDaProaEDaPopa() {
        ObjetoConstrucao barco = new ObjetoConstrucao();
        barco.setTipo(TipoObjetoConstrucao.BARCO);
        barco.setPosicaoQuina(new Point(20, 20));
        barco.setLargura(120);
        barco.setAltura(60);
        barco.setAfunilamento(30);
        barco.setCorPimaria(new Color(10, 10, 10));
        barco.setCorSecundaria(new Color(200, 200, 200));

        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            barco.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }

        // Meio do corpo do barco (longe da popa arredondada e da proa afunilada): deveria ser a cor secundária (preenchimento interno).
        int corNoMeio = imagem.getRGB(20 + 40, 20 + 30);
        // Bem perto da borda esquerda (popa), dentro da "moldura" de 10px: deveria ser a cor primária.
        int corNaBorda = imagem.getRGB(20 + 3, 20 + 30);

        assertEquals(barco.getCorSecundaria().getRGB(), corNoMeio,
                "meio do corpo do barco deveria ter a cor secundária (preenchimento interno)");
        assertEquals(barco.getCorPimaria().getRGB(), corNaBorda,
                "perto da borda (moldura de 10px) deveria continuar com a cor primária");
    }

    /**
     * A popa (extremidade sem afunilamento) do barco deveria ter cantos
     * arredondados, como as demais formas ("borda arredondada e tudo
     * mais") — o pixel exatamente na quina superior-esquerda não deveria
     * pertencer ao barco (arredondado corta a quina), mas um pixel um
     * pouco mais pra dentro, sim.
     */
    @Test
    void barco_popaTemCantosArredondados() {
        ObjetoConstrucao barco = new ObjetoConstrucao();
        barco.setTipo(TipoObjetoConstrucao.BARCO);
        barco.setPosicaoQuina(new Point(20, 20));
        barco.setLargura(120);
        barco.setAltura(60);

        BufferedImage imagem = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imagem.createGraphics();
        try {
            barco.desenha(g2d, 1.0);
        } finally {
            g2d.dispose();
        }

        int transparente = new Color(0, 0, 0, 0).getRGB();
        int corNaQuina = imagem.getRGB(20, 20);
        int corUmPoucoParaDentro = imagem.getRGB(20 + 8, 20 + 8);

        assertEquals(transparente, corNaQuina,
                "a quina exata da popa deveria estar fora da forma (canto arredondado corta a quina)");
        assertTrue(corUmPoucoParaDentro != transparente,
                "um pouco mais pra dentro da quina arredondada já deveria estar dentro da forma");
    }
}
