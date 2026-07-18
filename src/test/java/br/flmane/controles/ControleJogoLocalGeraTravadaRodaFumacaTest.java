package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Carro;
import br.flmane.entidades.GameRandom;
import br.flmane.entidades.No;
import br.flmane.entidades.Piloto;
import br.nnpe.Global;

/**
 * ControleJogoLocal.geraTravadaRoda deve poder gerar travada de roda só com
 * marca (sem fumaça) de forma independente de travada com marca + fumaça —
 * toda travada marca o pneu, mas a fumaça só ocorre numa fração dos casos
 * (Global.CHANCE_FUMACA_TRAVADA_RODA), não em 100% deles.
 */
class ControleJogoLocalGeraTravadaRodaFumacaTest {

    private ControleJogoLocal criarControle(double... valoresNextDouble) throws Exception {
        ControleJogoLocal controle = new ControleJogoLocal(1L);
        GameRandom random = mock(GameRandom.class);
        Double primeiro = valoresNextDouble[0];
        Double[] restantes = new Double[valoresNextDouble.length - 1];
        for (int i = 1; i < valoresNextDouble.length; i++) {
            restantes[i - 1] = valoresNextDouble[i];
        }
        when(random.nextDouble()).thenReturn(primeiro, restantes);
        when(random.intervalo(anyInt(), anyInt())).thenReturn(15);
        Field campoRandom = ControleRecursos.class.getDeclaredField("random");
        campoRandom.setAccessible(true);
        campoRandom.set(controle, random);
        return controle;
    }

    private Piloto criarPiloto(ControleJogoLocal controle) {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        controle.mapaNosIds.put(no, 11);
        controle.mapaIdsNos.put(11, no);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controle);
        piloto.setNoAtual(no);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    @Test
    void travadaSemFumaca_aindaMarcaPneuMasNaoAtivaContador() throws Exception {
        // 1ª chamada de nextDouble(): veto por colisão (intensidade máxima, sempre passa com 0.0).
        // 2ª chamada: sorteio da fumaça, acima da chance configurada -> sem fumaça.
        double semFumaca = Global.CHANCE_FUMACA_TRAVADA_RODA + 0.01;
        ControleJogoLocal controle = criarControle(0.0, semFumaca);
        Piloto piloto = criarPiloto(controle);

        controle.travouRodasPorColisao(piloto);

        assertTrue(piloto.isMarcaPneu(), "toda travada de roda deveria marcar o pneu, com ou sem fumaça");
        assertFalse(piloto.isTravouRodas(), "sem fumaça, o contador de fumaça não deveria ficar ativo");
        assertEquals(0, piloto.getContTravouRodas());
    }

    @Test
    void travadaComFumaca_marcaPneuEAtivaContador() throws Exception {
        // 1ª chamada de nextDouble(): veto por colisão, sempre passa com 0.0.
        // 2ª chamada: sorteio da fumaça, abaixo da chance configurada -> com fumaça.
        double comFumaca = Global.CHANCE_FUMACA_TRAVADA_RODA - 0.01;
        ControleJogoLocal controle = criarControle(0.0, comFumaca);
        Piloto piloto = criarPiloto(controle);

        controle.travouRodasPorColisao(piloto);

        assertTrue(piloto.isMarcaPneu(), "toda travada de roda deveria marcar o pneu");
        assertTrue(piloto.isTravouRodas(), "com fumaça, o contador de fumaça deveria ficar ativo");
    }

    @Test
    void nemTodaTravadaDeRodaGeraFumaca() throws Exception {
        // 3 travadas de roda seguidas (veto sempre passa com 0.0): fumaça, sem fumaça, fumaça.
        ControleJogoLocal controle = criarControle(
                0.0, Global.CHANCE_FUMACA_TRAVADA_RODA - 0.01,
                0.0, Global.CHANCE_FUMACA_TRAVADA_RODA + 0.01,
                0.0, Global.CHANCE_FUMACA_TRAVADA_RODA - 0.01);
        Piloto piloto1 = criarPiloto(controle);
        Piloto piloto2 = criarPiloto(controle);
        Piloto piloto3 = criarPiloto(controle);

        controle.travouRodasPorColisao(piloto1);
        controle.travouRodasPorColisao(piloto2);
        controle.travouRodasPorColisao(piloto3);

        assertTrue(piloto1.isTravouRodas(), "primeira travada deveria ter fumaça");
        assertFalse(piloto2.isTravouRodas(), "segunda travada não deveria ter fumaça");
        assertTrue(piloto3.isTravouRodas(), "terceira travada deveria ter fumaça");
        assertTrue(piloto1.isMarcaPneu() && piloto2.isMarcaPneu() && piloto3.isMarcaPneu(),
                "todas as três travadas deveriam marcar o pneu, com ou sem fumaça");
    }
}
