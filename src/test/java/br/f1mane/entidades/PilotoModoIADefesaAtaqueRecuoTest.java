package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Cobre o recuo proativo da decisão automática de agressividade
 * ({@code Piloto.modoIADefesaAtaque()}) quando o stress já está acima de 95%
 * — ver spec {@code piloto-modo-pilotagem-agressividade}. Escopo exclusivo
 * da decisão automática (bot, ou piloto automático dirigindo o carro do
 * jogador humano); não afeta o jogador humano escolhendo AGRESSIVO
 * manualmente (ver {@code PilotoAutopilotModoTest} pra essa distinção).
 */
class PilotoModoIADefesaAtaqueRecuoTest {

    private InterfaceJogo controleJogo;
    private Piloto piloto;

    @BeforeEach
    void setUp() throws Exception {
        List<No> pista = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, 100));
            pista.add(no);
        }
        No curva = pista.get(10);
        curva.setTipo(No.CURVA_BAIXA);

        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.isDrs()).thenReturn(false);

        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);

        piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(curva);
        setTemPneu(piloto, true);
    }

    private void setTemPneu(Piloto piloto, boolean valor) throws Exception {
        Field campo = Piloto.class.getDeclaredField("temPneu");
        campo.setAccessible(true);
        campo.set(piloto, valor);
    }

    @Test
    void stressAcimaDe95_testeDeHabilidadeBemSucedido_recuaParaNormal() {
        piloto.setStress(96);
        piloto.setHabilidade(1000);
        piloto.getCarro().setPotencia(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.modoIADefesaAtaque();

        assertEquals(Piloto.NORMAL, piloto.getModoPilotagem(),
                "teste de habilidade bem-sucedido deveria fazer a decisão automática recuar pra NORMAL");
    }

    @Test
    void stressAcimaDe95_testeDeHabilidadeMalsucedido_aindaEscolheAgressivo() {
        piloto.setStress(96);
        piloto.setHabilidade(1000);
        // potência padrão (0): testeHabilidadePilotoCarro() do recuo falha sempre, mesmo com o
        // teste de habilidade genérico do topo do método bem-sucedido (habilidade alta).
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.modoIADefesaAtaque();

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(),
                "sem sucesso no teste de habilidade do recuo, a decisão automática ainda escolhe AGRESSIVO — não é garantia");
    }

    @Test
    void stressDentroDoLimite_naoConsomeRNGExtraDoRecuo() {
        piloto.setStress(50);
        piloto.setHabilidade(1000);
        piloto.getCarro().setPotencia(1000);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.0);

        piloto.modoIADefesaAtaque();

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem(), "stress dentro do limite, AGRESSIVO normalmente");
        // Só 1 chamada: o teste de habilidade genérico no topo do método. O recuo (stress>95)
        // nem chega a ser avaliado, então testeHabilidadePilotoCarro() não é chamado aqui.
        verify(controleJogo.getRandom(), times(1)).nextDouble();
    }
}
