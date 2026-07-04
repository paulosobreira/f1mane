package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Piloto.processaFreioNaReta passa a exigir estar dentro de uma zona de
 * frenagem detectada (InterfaceJogo.isNoZonaFrenagem), não apenas a
 * distância fixa de 300 nós até a próxima curva de qualquer tipo.
 */
class PilotoFreioNaRetaZonaFrenagemTest {

    private InterfaceJogo controleJogo;
    private No noReta;
    private No noCurvaBaixa;

    @BeforeEach
    void setUp() {
        controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(controleJogo.isChovendo()).thenReturn(false);

        noReta = new No();
        noReta.setIndex(100);
        noReta.setTipo(No.RETA);

        noCurvaBaixa = new No();
        noCurvaBaixa.setIndex(150);
        noCurvaBaixa.setTipo(No.CURVA_BAIXA);

        when(controleJogo.obterProxCurva(noReta)).thenReturn(noCurvaBaixa);
    }

    private Piloto criarPiloto() {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(noReta);
        piloto.setGanho(100);
        return piloto;
    }

    /** Neutraliza os campos privados de diferença pro retardatário (default 0, que forçaria retardaFreiandoReta=false). */
    private void neutralizaDiffRetardatario(Piloto piloto) throws Exception {
        for (String nomeCampo : new String[] { "calculaDiffParaProximoRetardatario",
                "calculaDiffParaProximoRetardatarioMesmoTracado" }) {
            Field campo = Piloto.class.getDeclaredField(nomeCampo);
            campo.setAccessible(true);
            campo.setInt(piloto, 200);
        }
    }

    @Test
    void foraDaZonaDeFrenagem_naoAtivaFreiandoRetaNemTravaRodas() {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(false);
        Piloto piloto = criarPiloto();
        piloto.setStress(60);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);

        piloto.processaFreioNaReta();

        assertFalse(piloto.isFreiandoReta());
        verify(controleJogo, never()).travouRodas(any(Piloto.class));
    }

    @Test
    void dentroDaZonaDeFrenagem_ativaFreiandoRetaESuavizaGanho() {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        Piloto piloto = criarPiloto();

        piloto.processaFreioNaReta();

        assertTrue(piloto.isFreiandoReta());
        // val=50, distAfrente=300 -> multi=1/6 < minMulti(0.7), entao ganho e puxado pro minimo
        assertTrue(piloto.getGanho() < 100.0, "ganho deveria ser suavizado (reduzido) dentro da zona de frenagem");
    }

    @Test
    void dentroDaZonaDeFrenagem_comStressEModoAgressivo_acionaTravadaDeRoda() throws Exception {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        Piloto piloto = criarPiloto();
        piloto.setStress(60);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        neutralizaDiffRetardatario(piloto);

        piloto.processaFreioNaReta();

        assertTrue(piloto.isFreiandoReta());
        verify(controleJogo, times(1)).travouRodas(piloto);
    }
}
