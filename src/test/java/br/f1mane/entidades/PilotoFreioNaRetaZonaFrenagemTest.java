package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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

import br.f1mane.controles.ControleFreio;
import br.f1mane.controles.InterfaceJogo;

/**
 * Piloto.processaFreioNaReta passa a exigir estar dentro de uma zona de
 * frenagem detectada (InterfaceJogo.isNoZonaFrenagem), não apenas a
 * distância fixa de 300 nós até a próxima curva de qualquer tipo.
 */
class PilotoFreioNaRetaZonaFrenagemTest {

    private InterfaceJogo controleJogo;
    private ControleFreio controleFreio;
    private No noReta;
    private No noCurvaBaixa;

    @BeforeEach
    void setUp() {
        controleJogo = mock(InterfaceJogo.class);
        controleFreio = new ControleFreio(controleJogo);
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

    private Object getCampo(Piloto piloto, String campo) throws Exception {
        Field f = Piloto.class.getDeclaredField(campo);
        f.setAccessible(true);
        return f.get(piloto);
    }

    private void setCampo(Piloto piloto, String campo, Object valor) throws Exception {
        Field f = Piloto.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(piloto, valor);
    }

    @Test
    void foraDaZonaDeFrenagem_naoAtivaFreiandoRetaNemTravaRodas() {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(false);
        Piloto piloto = criarPiloto();
        piloto.setStress(60);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);

        controleFreio.processaFreioNaReta(piloto);

        assertFalse(piloto.isFreiandoReta());
        verify(controleJogo, never()).travouRodas(any(Piloto.class));
    }

    @Test
    void dentroDaZonaDeFrenagem_ativaFreiandoRetaESuavizaGanho() {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        Piloto piloto = criarPiloto();

        controleFreio.processaFreioNaReta(piloto);

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

        controleFreio.processaFreioNaReta(piloto);

        assertTrue(piloto.isFreiandoReta());
        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void dentroDaZonaDeFrenagem_pilotoForaDoTop3ComSorteioRuim_disparaFreioMalSucedido() throws Exception {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.95);
        Piloto piloto = criarPiloto();
        piloto.setPosicao(10); // fora do top-3: gatilho deixou de ser exclusivo do pódio
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        neutralizaDiffRetardatario(piloto);

        controleFreio.processaFreioNaReta(piloto);

        assertEquals(30, getCampo(piloto, "freioNaRetaMalSucedidoNesteTick"),
                "posicao fora do top-3 deve poder disparar o gatilho agora que a restricao foi removida");
    }

    @Test
    void dentroDaZonaDeFrenagem_disparaNoMaximoUmaVezPorEventoDeFrenagem() throws Exception {
        when(controleJogo.isNoZonaFrenagem(noReta)).thenReturn(true);
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.95);
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        neutralizaDiffRetardatario(piloto);

        controleFreio.processaFreioNaReta(piloto);
        assertEquals(30, getCampo(piloto, "freioNaRetaMalSucedidoNesteTick"),
                "primeiro tick dentro da zona deveria avaliar e disparar o gatilho");

        // Simula o consumo do flag por processaStress() no fim do tick, como aconteceria no jogo real.
        setCampo(piloto, "freioNaRetaMalSucedidoNesteTick", null);

        controleFreio.processaFreioNaReta(piloto);
        assertNull(getCampo(piloto, "freioNaRetaMalSucedidoNesteTick"),
                "segundo tick no mesmo evento de frenagem nao deveria reavaliar o sorteio");
    }
}
