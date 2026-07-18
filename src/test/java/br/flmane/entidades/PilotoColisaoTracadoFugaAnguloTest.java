package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Cobre o mesmo bug já corrigido em
 * {@code PainelCircuito.centralizaCarroDesenhar} (ângulo da caixa de colisão
 * calculado a partir da pista base/tracado 0, em vez de
 * {@code pista4Full}/{@code pista5Full}, quando o piloto está de fato no
 * traçado de fuga 4/5) — aqui aplicado a
 * {@link Piloto#centralizaCarroColisao()}, que orienta
 * {@code trazeiraColisao}/{@code diateiraColisao} usados por
 * {@link Piloto#processaColisao()}.
 * <p>
 * Estratégia dos testes: a pista base (retornada por
 * {@code controleJogo.obterPista}) é sempre uma reta horizontal (mesmo y,
 * x=index) — se o ângulo da caixa de colisão vier dela, traseira/dianteira
 * ficam separadas no eixo X, com Y ~igual. {@code pista4Full}/
 * {@code pista5Full}, no trecho relevante, é uma reta VERTICAL (mesmo x, y
 * cresce com o index) — se o ângulo vier dela (comportamento correto),
 * traseira/dianteira ficam separadas no eixo Y, com X ~igual.
 */
class PilotoColisaoTracadoFugaAnguloTest {

    private static final int TAMANHO_PISTA = 1000;
    private static final int X_ESCAPADA = 300;

    private InterfaceJogo controleJogo;
    private List<No> pista;
    private List<No> pista4;
    private List<No> pista5;
    private List<Piloto> pilotos;

    @BeforeEach
    void setUp() {
        pista = new ArrayList<>();
        List<No> pista1 = new ArrayList<>();
        List<No> pista2 = new ArrayList<>();
        pista4 = new ArrayList<>();
        pista5 = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            pista.add(criarNo(i, i, 100));
            pista1.add(criarNo(i, i, 76));
            pista2.add(criarNo(i, i, 124));
            pista4.add(null);
            pista5.add(null);
        }
        // Trecho de escapada (reta vertical, bem diferente da pista base horizontal)
        // cobrindo os índices traz/frente (piloto em 500 ± METADE_CARRO=20).
        for (int i = 400; i < 600; i++) {
            pista4.set(i, criarNo(i, X_ESCAPADA, i));
            pista5.set(i, criarNo(i, X_ESCAPADA, i));
        }
        pilotos = new ArrayList<>();

        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getNosDoBox()).thenReturn(new ArrayList<>());
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any())).thenReturn(pista);
        when(controleJogo.isModoQualify()).thenReturn(false);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.isAtualizacaoSuave()).thenReturn(false);
        when(controleJogo.verificaNoPitLane(any())).thenReturn(false);
        when(controleJogo.tempoCicloCircuito()).thenReturn(200L);

        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);

        Circuito circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(24.0);
        when(circuito.getIndiceTracadoForaPista()).thenReturn(84.0);
        when(circuito.getMultiplicadorLarguraPista()).thenReturn(1.0);
        when(circuito.getPista1Full()).thenReturn(pista1);
        when(circuito.getPista2Full()).thenReturn(pista2);
        when(circuito.getPista4Full()).thenReturn(pista4);
        when(circuito.getPista5Full()).thenReturn(pista5);
        when(controleJogo.getCircuito()).thenReturn(circuito);
    }

    private No criarNo(int index, int x, int y) {
        No no = new No();
        no.setIndex(index);
        no.setPoint(new Point(x, y));
        return no;
    }

    private Piloto criarPiloto(int index, int tracado) {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(index));
        piloto.setTracado(tracado);
        pilotos.add(piloto);
        return piloto;
    }

    @Test
    void tracado4_caixaDeColisaoUsaAnguloDaPista4Full_naoDaPistaBase() {
        Piloto piloto = criarPiloto(500, 4);

        piloto.centralizaCarroColisao();

        Rectangle traz = piloto.getTrazeiraColisao();
        Rectangle diant = piloto.getDiateiraColisao();
        double diffX = Math.abs(traz.getCenterX() - diant.getCenterX());
        double diffY = Math.abs(traz.getCenterY() - diant.getCenterY());

        assertTrue(diffY > diffX,
                "com o ângulo vindo de pista4Full (reta vertical), traseira/dianteira deveriam se separar no eixo Y, "
                        + "não no X (que é o que aconteceria se o ângulo ainda viesse da pista base horizontal) — "
                        + "diffX=" + diffX + " diffY=" + diffY);
        assertTrue(diffY > 30, "a separação no eixo Y deveria ser próxima de 2*METADE_CARRO (40); foi " + diffY);
    }

    @Test
    void tracado5_caixaDeColisaoUsaAnguloDaPista5Full_naoDaPistaBase() {
        Piloto piloto = criarPiloto(500, 5);

        piloto.centralizaCarroColisao();

        Rectangle traz = piloto.getTrazeiraColisao();
        Rectangle diant = piloto.getDiateiraColisao();
        double diffX = Math.abs(traz.getCenterX() - diant.getCenterX());
        double diffY = Math.abs(traz.getCenterY() - diant.getCenterY());

        assertTrue(diffY > diffX,
                "com o ângulo vindo de pista5Full (reta vertical), traseira/dianteira deveriam se separar no eixo Y, não no X — "
                        + "diffX=" + diffX + " diffY=" + diffY);
        assertTrue(diffY > 30, "a separação no eixo Y deveria ser próxima de 2*METADE_CARRO (40); foi " + diffY);
    }

    @Test
    void tracado0_continuaUsandoAnguloDaPistaBase_semAlteracao() {
        // Regressão: fora do traçado 4/5, o ângulo continua vindo da pista base (horizontal)
        // como sempre — a correção não deveria afetar esse caminho.
        Piloto piloto = criarPiloto(500, 0);

        piloto.centralizaCarroColisao();

        Rectangle traz = piloto.getTrazeiraColisao();
        Rectangle diant = piloto.getDiateiraColisao();
        double diffX = Math.abs(traz.getCenterX() - diant.getCenterX());
        double diffY = Math.abs(traz.getCenterY() - diant.getCenterY());

        assertTrue(diffX > diffY,
                "no traçado 0, a pista base é horizontal — traseira/dianteira deveriam se separar no eixo X, não no Y — "
                        + "diffX=" + diffX + " diffY=" + diffY);
    }

    @Test
    void tracado4_semZonaDeEscapadaNoIndice_caiParaFallbackPista2Full() {
        // Fora do trecho [400,600) preenchido em pista4Full, o índice fica null — deveria cair
        // no fallback (pista2Full), que também é horizontal, então volta a se separar no X.
        Piloto piloto = criarPiloto(50, 4);

        piloto.centralizaCarroColisao();

        Rectangle traz = piloto.getTrazeiraColisao();
        Rectangle diant = piloto.getDiateiraColisao();
        double diffX = Math.abs(traz.getCenterX() - diant.getCenterX());
        double diffY = Math.abs(traz.getCenterY() - diant.getCenterY());

        assertTrue(diffX > diffY,
                "sem nó de escapada no índice (fora da zona), o fallback pra pista2Full (horizontal) deveria valer — "
                        + "diffX=" + diffX + " diffY=" + diffY);
        // Regressão de um bug separado encontrado no caminho: o fallback de p4 (posição, não
        // ângulo) usava pista1Full (y=76) em vez de pista2Full (y=124) — copy-paste da linha de
        // p5 logo acima, onde p1 É o fallback correto. pista2Full é a origem certa do traçado de
        // fuga 4 (mapeamento 2→4, só retorna a 2 — ver Circuito.preencherTracadoEscapada).
        assertEquals(124.0, piloto.getCentroColisao().getCenterY(), 0.5,
                "a posição de referência (carx/cary) do traçado 4 sem zona deveria vir de pista2Full (y=124), não de pista1Full (y=76)");
    }

    @Test
    void processaColisao_naoQuebraComTracadoDeFuga() {
        // Regressão simples: processaColisao() (que chama centralizaCarroColisao() por baixo)
        // continua funcionando normalmente com um piloto no traçado de fuga.
        Piloto piloto = criarPiloto(500, 4);

        piloto.processaColisao();

        assertEquals(null, piloto.getColisao());
    }

}
