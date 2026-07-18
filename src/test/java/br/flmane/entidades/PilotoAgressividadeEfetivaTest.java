package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Cobre a regra de agressividade efetiva (ver spec
 * {@code piloto-modo-pilotagem-agressividade}): piloto AGRESSIVO com
 * {@code stress > 95} continua exibido/armazenado como AGRESSIVO
 * ({@link Piloto#getModoPilotagem()}), mas os cálculos de gameplay que leem
 * {@link Piloto#getModoPilotagemEfetivo()} tratam esse caso como NORMAL. O
 * antigo auto-downgrade de {@code incStress()} em stress≥99 (que mutava o
 * campo de verdade) foi removido, substituído por esta leitura.
 */
class PilotoAgressividadeEfetivaTest {

    private InterfaceJogo controleJogo;
    private GameRandom random;
    private Piloto piloto;

    @BeforeEach
    void setUp() {
        List<No> pista = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, 100));
            pista.add(no);
        }
        List<Piloto> pilotos = new ArrayList<>();

        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any())).thenReturn(pista);
        when(controleJogo.isModoQualify()).thenReturn(false);

        random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);

        piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(10));
        pilotos.add(piloto);
    }

    // ---- getModoPilotagemEfetivo(): leitura pura ----

    @Test
    void agressivoComStressAcimaDe95_efetivoENormal() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(96);

        assertEquals(Piloto.NORMAL, piloto.getModoPilotagemEfetivo());
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "o campo armazenado não deve mudar");
    }

    @Test
    void agressivoComStressExatamente95_efetivoContinuaAgressivo() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagemEfetivo(), "regra é > 95, não >= 95");
    }

    @Test
    void agressivoComStress99_efetivoENormal_masArmazenadoContinuaAgressivo() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(99);

        assertEquals(Piloto.NORMAL, piloto.getModoPilotagemEfetivo());
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(),
                "o antigo auto-downgrade em stress>=99 foi removido — o campo nunca muta por causa do stress");
    }

    @Test
    void normalNaoEAfetadoPeloStress() {
        piloto.setModoPilotagem(Piloto.NORMAL);
        piloto.setStress(99);

        assertEquals(Piloto.NORMAL, piloto.getModoPilotagemEfetivo());
    }

    @Test
    void lentoNaoEAfetadoPeloStress() {
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(99);

        assertEquals(Piloto.LENTO, piloto.getModoPilotagemEfetivo());
    }

    @Test
    void stressVoltandoA95OuMenos_restauraEfeitoDeAgressivoNoCicloSeguinte() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(96);
        assertEquals(Piloto.NORMAL, piloto.getModoPilotagemEfetivo());

        piloto.setStress(95);

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagemEfetivo(),
                "sem precisar o piloto trocar de modo manualmente, só o stress cair já restaura o efeito");
    }

    // ---- Efeito de gameplay concreto: decStress()/incStress() escalam pelo modo efetivo ----

    @Test
    void decStress_agressivoComStressAlto_recuperaComoNormal() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(96);
        piloto.setPosicao(1);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.9);

        piloto.decStress(10);

        // Efetivo NORMAL nao escala mais o valor (era 1.1x): round(10 * 1.0) = 10.
        assertEquals(96 - 10, piloto.getStress());
    }

    @Test
    void decStress_agressivoComStressBaixo_naoRecuperaNada() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);
        piloto.setPosicao(1);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.9);

        piloto.decStress(10);

        // AGRESSIVO agora escala o valor a 0x: nao recupera estresse nenhum.
        assertEquals(50, piloto.getStress());
    }

    @Test
    void incStress_stress99_naoMutaMaisOModoArmazenado() {
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(99);
        piloto.setPosicao(1);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.incStress(1);

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(),
                "antigo comportamento mutava pra NORMAL aqui — removido, substituído pela leitura efetiva");
    }

    // ---- Escopo: jogador humano escolhendo AGRESSIVO manualmente não passa por nenhum teste ----

    @Test
    void jogadorHumanoSelecionaAgressivoManualmente_comStressAlto_funcionaImediatamenteSemTeste() {
        // ControleJogoLocal.mudarModoPilotagem()/ControleJogosServer.mudarAgressividadePiloto()
        // chamam setModoPilotagem() direto — o mesmo setter usado aqui. Stress alto não impede
        // nem atrasa a escolha do jogador; a regra de agressividade efetiva (getModoPilotagemEfetivo())
        // só afeta a leitura dos cálculos de gameplay, nunca a escrita do campo pelo jogador.
        piloto.setJogadorHumano(true);
        piloto.setStress(99);

        piloto.setModoPilotagem(Piloto.AGRESSIVO);

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem());
        org.mockito.Mockito.verify(random, org.mockito.Mockito.never()).nextDouble();
    }
}
