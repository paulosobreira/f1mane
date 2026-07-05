package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Point;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Os eventos que antes acionavam o bloqueio invisível de `ciclosDesconcentrado`
 * (escapar da pista sob pressão, ceder passagem, ser ultrapassado) agora só
 * geram estresse — nenhuma ação do piloto fica mais suprimida por causa deles.
 * Valor cheio reduzido pela metade, e à metade de novo se `testeHabilidadePiloto()`
 * for bem-sucedido.
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

    private void setCampo(Piloto piloto, String campo, Object valor) throws Exception {
        Field f = Piloto.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(piloto, valor);
    }

    @Test
    void processaEscapadaDaPista_escapaSobPressao_incrementaEstresseSemHabilidade() throws Exception {
        Piloto piloto = criarPiloto(0.0); // testeHabilidadePiloto() falha (habilidade default 0)
        InterfaceJogo controleJogo = piloto.getControleJogo();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95); // > Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA (90)
        piloto.setTracado(0);
        piloto.setIndiceTracado(0);
        setCampo(piloto, "pontoEscape", new Point(1, 1));
        setCampo(piloto, "distanciaEscape", 10.0);
        setCampo(piloto, "indexRefEscape", 15); // >= noAtual.getIndex() (10)
        when(controleJogo.obterLadoEscape(any())).thenReturn(1);

        piloto.processaEscapadaDaPista();

        // incStress(3): AGRESSIVO escala por 0.5 (round(1.5)=2), mas o cap "stress>90" de incStress() força val=1
        assertEquals(96, piloto.getStress(), "sem sucesso no teste de habilidade, deveria incrementar 1 (95 -> 96, capado por stress>90)");
    }

    @Test
    void processaEscapadaDaPista_escapaSobPressao_incrementaMenosComHabilidade() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        InterfaceJogo controleJogo = piloto.getControleJogo();
        piloto.setHabilidade(999); // com random=0.0, testeHabilidadePiloto() = true (0.0 < 999/1000)
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(95);
        piloto.setTracado(0);
        piloto.setIndiceTracado(0);
        setCampo(piloto, "pontoEscape", new Point(1, 1));
        setCampo(piloto, "distanciaEscape", 10.0);
        setCampo(piloto, "indexRefEscape", 15);
        when(controleJogo.obterLadoEscape(any())).thenReturn(1);

        piloto.processaEscapadaDaPista();

        // incStress(1), AGRESSIVO escala por 0.5 -> round(0.5) = 1
        assertEquals(96, piloto.getStress(), "com sucesso no teste de habilidade, deveria incrementar 1 (95 -> 96)");
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
