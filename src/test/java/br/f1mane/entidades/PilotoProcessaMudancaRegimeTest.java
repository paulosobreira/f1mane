package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * processaMudancaRegime() não força mais o piloto pra modo NORMAL/giro normal
 * por causa de estresse alto — essa era a única exceção que tinha sobrevivido
 * da mecânica de desconcentração removida (um limiar de estresse >= 90 no
 * lugar do antigo `verificaDesconcentrado()`), e foi removida também: o
 * piloto nunca mais é forçado a sair do modo agressivo/giro máximo por isso,
 * mesmo com estresse no máximo.
 */
class PilotoProcessaMudancaRegimeTest {

    private Piloto criarPiloto() {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.0);
        // faz mensangesModoAgressivo()/mensagemPilotoLento() retornarem cedo, sem precisar simular o resto da cadeia de mensagens
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    private void invocaProcessaMudancaRegime(Piloto piloto) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("processaMudancaRegime");
        m.setAccessible(true);
        m.invoke(piloto);
    }

    @Test
    void stressNoMaximo_naoForcaDowngradeDeModoOuGiro() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        piloto.setStress(100);

        invocaProcessaMudancaRegime(piloto);

        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem());
        assertEquals(Carro.GIRO_MAX_VAL, piloto.getCarro().getGiro());
    }

    @Test
    void bandeirada_continuaForcandoLentoEGiroMinimo() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.getCarro().setGiro(Carro.GIRO_MAX_VAL);
        piloto.setStress(0);
        piloto.setRecebeuBanderada(true);

        invocaProcessaMudancaRegime(piloto);

        assertEquals(Piloto.LENTO, piloto.getModoPilotagem());
        assertEquals(Carro.GIRO_MIN_VAL, piloto.getCarro().getGiro());
    }
}
