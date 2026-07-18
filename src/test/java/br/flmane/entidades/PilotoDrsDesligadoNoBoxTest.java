package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.ControleDrs;
import br.flmane.controles.InterfaceJogo;

/**
 * Cobre um bug visual relatado pelo usuário: o indicador de DRS na tela
 * ficava piscando (ver {@code PainelCircuito.desenhaDRS()}, que pisca
 * enquanto {@code Piloto.isPodeUsarDRS()} é {@code true}) mesmo com o piloto
 * a caminho do box ou já dentro dele — porque {@code verificaPodeUsarDRS()}
 * só checava {@code getPtosBox() != 0} (fisicamente na pit lane), não
 * {@code isBox()} (decisão de ir pro box, que acontece bem antes). Corrigido
 * em {@code processaUsoDRS()}/{@code verificaPodeUsarDRS()} pra desligar DRS
 * (asa, {@code ativarDRS}, {@code podeUsarDRS}) desde a decisão.
 */
class PilotoDrsDesligadoNoBoxTest {

    private InterfaceJogo controleJogo;
    private ControleDrs controleDrs;
    private Piloto piloto;

    @BeforeEach
    void setUp() {
        List<No> pista = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            No no = new No();
            no.setIndex(i);
            no.setPoint(new Point(i, 100));
            no.setTipo(No.RETA);
            pista.add(no);
        }
        List<Piloto> pilotos = new ArrayList<>();

        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getNosDaPista()).thenReturn(pista);
        when(controleJogo.getPilotos()).thenReturn(pilotos);
        when(controleJogo.getPilotosCopia()).thenReturn(pilotos);
        when(controleJogo.obterPista(any())).thenReturn(pista);
        when(controleJogo.isDrs()).thenReturn(true);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.isCorridaTerminada()).thenReturn(false);

        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);

        controleDrs = new ControleDrs(controleJogo);

        piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        carro.setAsa(Carro.MENOS_ASA);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(pista.get(10));
        piloto.setNumeroVolta(2);
        piloto.setAtivarDRS(true);
        pilotos.add(piloto);
    }

    @Test
    void decidiuIrProBox_aindaNaPistaPrincipal_drsDesligado() {
        piloto.setBox(true); // decisão de ir pro box — getPtosBox() ainda é 0.

        controleDrs.processaUsoDRS(piloto);

        assertFalse(piloto.isPodeUsarDRS(), "indicador de DRS não deveria mais piscar assim que o piloto decide ir pro box");
        assertFalse(piloto.isAtivarDRS());
        assertEquals(Carro.MAIS_ASA, piloto.getCarro().getAsa());
    }

    @Test
    void fisicamenteNaPitLane_drsDesligado() {
        piloto.setPtosBox(5);

        controleDrs.processaUsoDRS(piloto);

        assertFalse(piloto.isPodeUsarDRS());
        assertFalse(piloto.isAtivarDRS());
        assertEquals(Carro.MAIS_ASA, piloto.getCarro().getAsa());
    }

    @Test
    void naoIndoProBox_naoDesligaDrsAToa() {
        // Controle: nem isBox() nem getPtosBox()!=0 — o novo guard de box não deveria disparar,
        // e (num nó de reta) o guard "fora de reta" também não. ativarDRS/asa continuam do setUp.
        controleDrs.processaUsoDRS(piloto);

        assertEquals(true, piloto.isAtivarDRS(), "fora do box, esta correção não deveria desligar ativarDRS à toa");
        assertEquals(Carro.MENOS_ASA, piloto.getCarro().getAsa());
    }
}
