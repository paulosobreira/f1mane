package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Cobre o limite de avanco contra o carro da frente (anti-atravessamento) e o
 * escape de fila indiana: verificaTracadoLivreParaEscapar e a variante de
 * mudarTracado que ignora a verificacao antiga de mudanca de tracado.
 */
class PilotoColisaoFilaTest {

    private static final int TAMANHO_PISTA = 1000;
    private static final int COMPRIMENTO_CARRO = Piloto.METADE_CARRO * 2;

    private InterfaceJogo controleJogo;
    private List<No> pista;
    private List<Piloto> pilotos;

    @BeforeEach
    void setUp() {
        pista = new ArrayList<>();
        for (int i = 0; i < TAMANHO_PISTA; i++) {
            No no = new No();
            no.setIndex(i);
            pista.add(no);
        }
        pilotos = new ArrayList<>();

        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any())).thenReturn(pista);
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
        when(controleJogo.getCircuito()).thenReturn(circuito);
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

    // ---- limitaAvancoCarroFrente ----

    @Test
    void limitaAvanco_carroParadoLogoAFrente_naoEntraNaAreaDele() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        criarPiloto("Parado", 130, 0);

        assertEquals(0, atras.limitaAvancoCarroFrente(50));
    }

    @Test
    void limitaAvanco_carroAFrenteDentroDoAlcance_paraAUmCarroDeDistancia() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        criarPiloto("Frente", 170, 0);

        // avanca ate 170 - 100 - COMPRIMENTO_CARRO = 30
        assertEquals(70 - COMPRIMENTO_CARRO, atras.limitaAvancoCarroFrente(50));
    }

    @Test
    void limitaAvanco_carroDistante_naoLimita() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        criarPiloto("Longe", 200, 0);

        assertEquals(50, atras.limitaAvancoCarroFrente(50));
    }

    @Test
    void limitaAvanco_carroEmOutroTracado_naoLimita() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        criarPiloto("Lado", 130, 1);

        assertEquals(50, atras.limitaAvancoCarroFrente(50));
    }

    @Test
    void limitaAvanco_carroAFrenteAposVoltaCompleta_respeitaWrap() {
        Piloto atras = criarPiloto("Atras", TAMANHO_PISTA - 10, 0);
        criarPiloto("Frente", 20, 0);

        assertEquals(0, atras.limitaAvancoCarroFrente(50));
    }

    @Test
    void limitaAvanco_carroCruzandoMinhaLinha_limita() {
        Piloto atras = criarPiloto("Atras", 100, 0);
        Piloto cruzando = criarPiloto("Cruzando", 130, 1);
        cruzando.setTracadoAntigo(0);
        cruzando.setIndiceTracado(20);

        assertEquals(0, atras.limitaAvancoCarroFrente(50));
    }

    // ---- verificaTracadoLivreParaEscapar ----

    @Test
    void tracadoLivre_filaIndianaNoMesmoTracado_naoBloqueiaEscape() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        criarPiloto("FrenteFila", 140, 0);
        criarPiloto("TrasFila", 60, 0);

        assertTrue(preso.verificaTracadoLivreParaEscapar(1));
    }

    @Test
    void tracadoLivre_carroNoAlvoAFrente_bloqueia() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        criarPiloto("NoAlvo", 150, 1);

        assertFalse(preso.verificaTracadoLivreParaEscapar(1));
    }

    @Test
    void tracadoLivre_carroNoAlvoVindoAtras_bloqueia() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        criarPiloto("VindoAtras", 30, 1);

        assertFalse(preso.verificaTracadoLivreParaEscapar(1));
    }

    @Test
    void tracadoLivre_carroNoAlvoBemLonge_naoBloqueia() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        criarPiloto("Longe", 250, 1);

        assertTrue(preso.verificaTracadoLivreParaEscapar(1));
    }

    @Test
    void tracadoLivre_carroCruzandoParaOAlvo_bloqueia() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        Piloto cruzando = criarPiloto("Cruzando", 130, 2);
        cruzando.setTracadoAntigo(1);
        cruzando.setIndiceTracado(20);

        assertFalse(preso.verificaTracadoLivreParaEscapar(1));
    }

    // ---- mudarTracado escapando da fila ----

    // ---- mudanca de tracado durante a animacao anterior ----

    @Test
    void mudarTracado_naoForcadaComAnimacaoEmAndamento_bloqueia() {
        Piloto piloto = criarPiloto("Piloto", 100, 1);
        piloto.setTracadoAntigo(0);
        piloto.setIndiceTracado(10);

        assertFalse(piloto.mudarTracado(0));
        assertEquals(1, piloto.getTracado());
    }

    @Test
    void mudarTracado_forcadaRevertendoNoMeioDaAnimacao_continuaDaPosicaoAtual() {
        Piloto piloto = criarPiloto("Piloto", 100, 1);
        piloto.setTracadoAntigo(0);
        piloto.setIndiceTracado(10);

        assertTrue(piloto.mudarTracado(0, true));
        assertEquals(0, piloto.getTracado());
        assertEquals(1, piloto.getTracadoAntigo());
        // espelha o progresso: 24 (cheio) - 10 (restante) = 14
        assertEquals(14, piloto.getIndiceTracado());
    }

    @Test
    void mudarTracado_forcadaParaTerceiraLinhaNoMeioDaAnimacao_reiniciaAnimacao() {
        Piloto piloto = criarPiloto("Piloto", 100, 0);
        piloto.setTracadoAntigo(1);
        piloto.setIndiceTracado(10);

        assertTrue(piloto.mudarTracado(2, true));
        assertEquals(2, piloto.getTracado());
        assertEquals(0, piloto.getTracadoAntigo());
        assertEquals(24, piloto.getIndiceTracado());
    }

    @Test
    void mudarTracado_presoNaFila_verificacaoAntigaBloqueiaEscapeNao() {
        Piloto preso = criarPiloto("Preso", 100, 0);
        criarPiloto("FrenteFila", 140, 0);
        criarPiloto("TrasFila", 60, 0);

        // a verificacao antiga conta os vizinhos da propria fila e bloqueia
        assertFalse(preso.mudarTracado(1));
        assertEquals(0, preso.getTracado());

        // escapando da fila a mudanca e permitida
        assertTrue(preso.mudarTracado(1, false, true));
        assertEquals(1, preso.getTracado());
        assertEquals(0, preso.getTracadoAntigo());
    }
}
