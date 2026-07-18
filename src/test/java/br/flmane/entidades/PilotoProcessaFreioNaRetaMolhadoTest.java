package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import br.flmane.controles.ControleFreio;
import br.flmane.controles.InterfaceJogo;

/**
 * Piloto.processaFreioNaReta passou a reduzir o piso "minMulti" na proporção
 * de "molhado%" (controleJogo.getMolhado() * 0.3), em vez de um corte fixo de
 * 0.3 gatilhado por isChovendo() binário. O cenário (val=50, distAfrente=300)
 * mantém o multiplicador bruto (0.1667) sempre abaixo do piso, isolando o
 * piso em si na saída de ganho.
 */
class PilotoProcessaFreioNaRetaMolhadoTest {

    private ControleFreio controleFreio;

    private Piloto criarPiloto(double molhado) throws Exception {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(controleJogo.getMolhado()).thenReturn(molhado);

        No noReta = new No();
        noReta.setIndex(100);
        noReta.setTipo(No.RETA);

        No noCurvaBaixa = new No();
        noCurvaBaixa.setIndex(150);
        noCurvaBaixa.setTipo(No.CURVA_BAIXA);

        when(controleJogo.obterProxCurva(noReta)).thenReturn(noCurvaBaixa);
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        controleFreio = new ControleFreio(controleJogo);

        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(noReta);
        piloto.setGanho(100);

        // neutraliza as reduções de minMulti por proximidade de retardatário, isolando o termo de molhado%
        for (String nomeCampo : new String[] { "calculaDiffParaProximoRetardatario",
                "calculaDiffParaProximoRetardatarioMesmoTracado" }) {
            Field campo = Piloto.class.getDeclaredField(nomeCampo);
            campo.setAccessible(true);
            campo.setInt(piloto, 200);
        }
        return piloto;
    }

    @Test
    void molhadoZero_reproduzBitABitOPisoSecoDeHoje() throws Exception {
        Piloto piloto = criarPiloto(0.0);

        controleFreio.processaFreioNaReta(piloto);

        // minMulti = 0.7 - 0.0*0.3 = 0.7; multi bruto (50/300=0.1667) fica abaixo, entao ganho = 100*0.7
        assertEquals(70.0, piloto.getGanho(), 1e-9);
    }

    @Test
    void molhadoUm_reproduzBitABitOPisoDeChuvaDeHoje() throws Exception {
        Piloto piloto = criarPiloto(1.0);

        controleFreio.processaFreioNaReta(piloto);

        // minMulti = 0.7 - 1.0*0.3 = 0.4; ganho = 100*0.4
        assertEquals(40.0, piloto.getGanho(), 1e-9);
    }

    @Test
    void molhadoIntermediario_produzPisoEntreOsDoisExtremos() throws Exception {
        Piloto piloto = criarPiloto(0.5);

        controleFreio.processaFreioNaReta(piloto);

        // minMulti = 0.7 - 0.5*0.3 = 0.55; ganho = 100*0.55
        assertEquals(55.0, piloto.getGanho(), 1e-9);
    }
}
