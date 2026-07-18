package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Carro;
import br.flmane.entidades.Clima;
import br.flmane.entidades.Piloto;

/**
 * ControleJogoLocal.setUpJogadorHumano passou a forçar pneu de chuva quando o
 * clima vigente é CHUVA, ignorando a escolha do jogador no pit stop — para
 * qualquer piloto, humano incluso (a IA já fazia isso via ControleBox,
 * lendo isChovendo() diretamente). O mesmo método é herdado por
 * JogoServidor (que só repassa pra super.setUpJogadorHumano), então cobre o
 * fluxo online também sem precisar de lógica separada.
 */
class ControleJogoLocalSetUpJogadorHumanoPneuChuvaTest {

    private ControleJogoLocal criarControle(String climaVigente) throws Exception {
        ControleJogoLocal controle = new ControleJogoLocal(1L);
        ControleCorrida controleCorrida = mock(ControleCorrida.class);
        ControleClima controleClima = mock(ControleClima.class);
        when(controleClima.getClima()).thenReturn(climaVigente);
        when(controleCorrida.getControleClima()).thenReturn(controleClima);
        controle.controleCorrida = controleCorrida;
        return controle;
    }

    private Piloto criarPilotoJogador(ControleJogoLocal controle) {
        Piloto piloto = new Piloto();
        piloto.setControleJogo(controle);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    @Test
    void climaChuva_forcaPneuDeChuvaMesmoComPneuSecoSelecionado() throws Exception {
        ControleJogoLocal controle = criarControle(Clima.CHUVA);
        Piloto piloto = criarPilotoJogador(controle);

        controle.setUpJogadorHumano(piloto, Carro.TIPO_PNEU_DURO, Integer.valueOf(50), null);

        assertEquals(Carro.TIPO_PNEU_CHUVA, piloto.getCarro().getTipoPneu());
    }

    @Test
    void climaForaDeChuva_mantemEscolhaDoJogador() throws Exception {
        ControleJogoLocal controle = criarControle(Clima.SOL);
        Piloto piloto = criarPilotoJogador(controle);

        controle.setUpJogadorHumano(piloto, Carro.TIPO_PNEU_DURO, Integer.valueOf(50), null);

        assertEquals(Carro.TIPO_PNEU_DURO, piloto.getCarro().getTipoPneu());
    }

    @Test
    void climaDeixaDeSerChuva_escolhaVoltaASerLivre() throws Exception {
        ControleJogoLocal controle = criarControle(Clima.NUBLADO);
        Piloto piloto = criarPilotoJogador(controle);

        controle.setUpJogadorHumano(piloto, Carro.TIPO_PNEU_MOLE, Integer.valueOf(50), null);

        assertEquals(Carro.TIPO_PNEU_MOLE, piloto.getCarro().getTipoPneu());
    }
}
