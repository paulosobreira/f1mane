package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Os eventos que antes acionavam o bloqueio invisível de `ciclosDesconcentrado`
 * (ceder passagem, ser ultrapassado) agora só geram estresse — nenhuma ação do
 * piloto fica mais suprimida por causa deles.
 */
class PilotoDesconcentracaoConvertidaTest {

    private Piloto criarPiloto(double valorNextDouble) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(valorNextDouble);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.isModoQualify()).thenReturn(false);
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.verificaInfoRelevante(any())).thenReturn(false);
        when(controleJogo.isCorridaTerminada()).thenReturn(false);
        Circuito circuito = mock(Circuito.class);
        when(circuito.getIndiceTracado()).thenReturn(0.0);
        when(controleJogo.getCircuito()).thenReturn(circuito);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_ALTA);
        piloto.setNoAtual(no);
        return piloto;
    }

    @Test
    void desviaPilotoNaFrente_cedePassagem_incrementaEstresseDoRetardatario() {
        Piloto piloto = criarPiloto(0.0);
        piloto.setNumeroVolta(3);
        piloto.setPtosPista(1000);

        Piloto pilotoNaFrente = criarPiloto(0.0);
        pilotoNaFrente.setNumeroVolta(2);
        pilotoNaFrente.setPtosPista(500);
        pilotoNaFrente.setPtosBox(0);
        pilotoNaFrente.setStress(0);
        pilotoNaFrente.setModoPilotagem(Piloto.NORMAL);
        pilotoNaFrente.getCarro().setGiro(Carro.GIRO_NOR_VAL);

        piloto.desviaPilotoNaFrente(piloto, pilotoNaFrente);

        // o modo já vira LENTO antes do incStress ser chamado (mesmo método), entao nao ha escala automatica de 0.5
        assertEquals(2, pilotoNaFrente.getStress(), "sem sucesso no teste de habilidade, deveria incrementar 2 (sem escala, ja esta em LENTO)");
        assertEquals(Piloto.LENTO, pilotoNaFrente.getModoPilotagem());
    }

    @Test
    void mensagemRetardatario_ultrapassado_incrementaEstresse() {
        Piloto piloto = criarPiloto(0.95); // > 0.9, aciona o gatilho
        InterfaceJogo controleJogoDoPiloto = piloto.getControleJogo();
        Piloto pilotoNaFrente = criarPiloto(0.0);
        pilotoNaFrente.setTracado(0);
        piloto.setTracado(0);
        pilotoNaFrente.setStress(0);
        when(controleJogoDoPiloto.verificaInfoRelevante(piloto)).thenReturn(true);

        piloto.mensagemRetardatario(piloto, pilotoNaFrente);

        // incStress(5), mas NORMAL já escala por 0.5 em incStress() -> round(2.5) = 3
        assertEquals(3, pilotoNaFrente.getStress(), "sem sucesso no teste de habilidade, deveria incrementar 3");
    }
}
