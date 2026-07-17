package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.ControleAutomacao;
import br.f1mane.controles.ControleJogoLocal;
import br.nnpe.Global;

/**
 * Cobre a cadeia completa da correcao de overlap: deteccao de colisao com
 * geometria real (areas dianteira/centro/traseira), penalidade de ganho
 * (incluindo carro da frente cruzando a linha), contador de preso em fila com
 * escape, e o cooldown de mudanca de tracado que cobre a animacao inteira.
 */
class PilotoPenalidadeEscapeFilaTest {

    private static final int TAMANHO_PISTA = 1000;

    private ControleJogoLocal controleJogo;
    private ControleAutomacao controleAutomacao;
    private List<No> pista;
    private List<Piloto> pilotos;

    @BeforeEach
    void setUp() {
        pista = new ArrayList<>();
        List<No> pista1 = new ArrayList<>();
        List<No> pista2 = new ArrayList<>();
        List<No> pista4 = new ArrayList<>();
        List<No> pista5 = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            pista.add(criarNo(i, i, 100));
            pista1.add(criarNo(i, i, 76));
            pista2.add(criarNo(i, i, 124));
            pista4.add(null);
            pista5.add(null);
        }
        pilotos = new ArrayList<>();

        controleJogo = mock(ControleJogoLocal.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getNosDoBox()).thenReturn(new ArrayList<>());
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any(No.class))).thenReturn(pista);
        when(controleJogo.isModoQualify()).thenReturn(false);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.isAtualizacaoSuave()).thenReturn(false);
        when(controleJogo.verificaNoPitLane(any())).thenReturn(false);
        when(controleJogo.tempoCicloCircuito()).thenReturn(200L);

        GameRandom random = mock(GameRandom.class);
        when(random.intervalo(1, 2)).thenReturn(1);
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

        controleAutomacao = new ControleAutomacao(controleJogo, null);
    }

    private No criarNo(int index, int x, int y) {
        No no = new No();
        no.setIndex(index);
        no.setPoint(new Point(x, y));
        return no;
    }

    private Piloto criarPiloto(String nome, int index, int tracado) {
        Piloto piloto = new Piloto();
        piloto.setNome(nome);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(index));
        piloto.setTracado(tracado);
        pilotos.add(piloto);
        return piloto;
    }

    // ---- deteccao de colisao com geometria real ----

    @Test
    void processaColisao_carroLogoAFrenteMesmoTracado_detecta() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 130, 0);

        atras.processaColisao();

        assertEquals(frente, atras.getColisao());
    }

    @Test
    void processaColisao_carroLongeMesmoTracado_naoDetecta() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        criarPiloto("Longe", 300, 0);

        atras.processaColisao();

        assertNull(atras.getColisao());
    }

    // ---- penalidade de ganho ----

    @Test
    void processaPenalidadeColisao_mesmoTracado_ganhoLimitadoAoDaFrente() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 130, 0);
        atras.setGanho(30);
        frente.setGanho(0);

        atras.processaColisao();
        atras.processaPenalidadeColisao();

        assertEquals(frente, atras.getColisao());
        assertEquals(0, atras.getGanho());
    }

    @Test
    void processaPenalidadeColisao_frenteCruzandoMinhaLinha_tambemLimita() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 130, 1);
        // frente acabou de sair da linha 0 e ainda esta cruzando
        frente.setTracadoAntigo(0);
        frente.setIndiceTracado(20);
        atras.setGanho(30);
        frente.setGanho(0);

        atras.processaColisao();
        atras.processaPenalidadeColisao();

        assertEquals(frente, atras.getColisao());
        assertEquals(0, atras.getGanho());
    }

    // ---- contador de preso em fila e escape ----

    private void prendeNaFila(Piloto atras, Piloto frente, int ciclos) {
        atras.setColisao(frente);
        atras.setGanho(5);
        for (int i = 0; i < ciclos; i++) {
            atras.processaPenalidadeColisao();
        }
    }

    @Test
    void tentarEscaparFilaIndiana_presoRastejando_escapaParaTracadoLivre() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0);
        prendeNaFila(atras, frente, 8);

        assertTrue(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
        assertEquals(1, atras.getTracado());
    }

    @Test
    void tentarEscaparFilaIndiana_poucosCiclosPreso_naoEscapa() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        // Frente bem além da janela de proximidade (50 índices) pra isolar só o contador de
        // colisão física literal (ciclosPresoFila) — não interferir com o contador novo
        // (ciclosPresoFilaProximidade), que teria seu próprio limiar (4) mais baixo.
        Piloto frente = criarPiloto("Frente", 400, 0);
        prendeNaFila(atras, frente, 7);

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
        assertEquals(0, atras.getTracado());
    }

    @Test
    void tentarEscaparFilaIndiana_semColisaoContadorZera() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 400, 0); // fora da janela de proximidade, ver comentário acima.
        prendeNaFila(atras, frente, 7);

        // colisao some por um ciclo: contador zera
        atras.setColisao(null);
        atras.processaPenalidadeColisao();

        prendeNaFila(atras, frente, 7);
        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
    }

    @Test
    void tentarEscaparFilaIndiana_comSafetyCarNaPista_naoEscapa() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0);
        prendeNaFila(atras, frente, 8);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
        assertEquals(0, atras.getTracado());
    }

    @Test
    void tentarEscaparFilaIndiana_ganhoNormal_naoAcumulaPreso() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0);
        atras.setColisao(frente);
        // seguindo em ritmo de corrida, nao rastejando
        for (int i = 0; i < 20; i++) {
            atras.setGanho(30);
            atras.processaPenalidadeColisao();
        }

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
    }

    @Test
    void tentarEscaparFilaIndiana_tracadoAlvoOcupado_naoEscapa() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0);
        criarPiloto("NoTracado1", 130, 1);
        criarPiloto("NoTracado2", 120, 2);
        prendeNaFila(atras, frente, 8);

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
        assertEquals(0, atras.getTracado());
    }

    // ---- fila indiana sem colisão física literal (ciclosPresoFilaProximidade) ----

    @Test
    void tentarEscaparFilaIndiana_semColisaoFisica_masPertoNaMesmaLinha_escapaComLimiarMenor() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0); // 40 índices à frente, dentro da janela de 50.
        atras.setGanho(10); // <= 15 (limite da proximidade), sem nunca chamar setColisao().

        for (int i = 0; i < Global.LIMIAR_CICLOS_FILA_SEM_COLISAO; i++) {
            atras.processaPenalidadeColisao();
        }

        assertNull(atras.getColisao(), "não deveria haver colisão física detectada — o caminho é só por proximidade");
        assertTrue(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
        assertEquals(1, atras.getTracado());
    }

    @Test
    void tentarEscaparFilaIndiana_foraDaJanelaDeProximidade_naoAcionaOCaminhoNovo() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 200, 0); // 100 índices à frente, fora da janela de 50.
        atras.setGanho(10);

        for (int i = 0; i < 10; i++) {
            atras.processaPenalidadeColisao();
        }

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
    }

    @Test
    void tentarEscaparFilaIndiana_dentroDaJanelaMasGanhoAcimaDoLimite_naoAcionaOCaminhoNovo() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto frente = criarPiloto("Frente", 140, 0);
        atras.setGanho(20); // > 15, acima do limite de ganho da proximidade.

        for (int i = 0; i < 10; i++) {
            atras.processaPenalidadeColisao();
        }

        assertFalse(controleAutomacao.decideTentarEscaparFilaIndiana(atras));
    }

    // ---- cooldown de mudanca de tracado cobre a animacao ----

    @Test
    void mudarTracado_comSuave_bloqueiaNovaMudancaLogoAposAnterior() {
        when(controleJogo.isAtualizacaoSuave()).thenReturn(true);
        Piloto piloto = criarPiloto("Piloto", 100, 0);

        assertTrue(piloto.mudarTracado(1));
        // simula a animacao concluida; o cooldown ainda deve segurar
        piloto.setIndiceTracado(0);

        assertFalse(piloto.mudarTracado(0));
        assertEquals(1, piloto.getTracado());
    }

    @Test
    void mudarTracado_forcada_ignoraCooldown() {
        when(controleJogo.isAtualizacaoSuave()).thenReturn(true);
        Piloto piloto = criarPiloto("Piloto", 100, 0);

        assertTrue(piloto.mudarTracado(1));
        piloto.setIndiceTracado(0);

        assertTrue(piloto.mudarTracado(0, true));
        assertEquals(0, piloto.getTracado());
    }
}
