package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Carro.calculaDesgastePneus(No) não altera mais o estresse do piloto
 * diretamente — a decisão foi movida para Piloto.processaStressDesgastePneus(),
 * consumida por processaStress(). Com habilidade/freios/aerodinâmica/potência
 * em 0, testeHabilidadePiloto()/testeHabilidadePilotoAerodinamicaFreios()/
 * testeHabilidadePilotoCarro() resolvem consistentemente para falso, para
 * qualquer valor de random.nextDouble() usado abaixo. A magnitude do
 * incremento agora escala por modo de pilotagem (AGRESSIVO = desgaste alto
 * cheio, NORMAL = metade disso, LENTO = nenhum incremento), que soma com o
 * escalonamento genérico já existente em incStress() (AGRESSIVO/NORMAL x0.5).
 */
class CarroCalculaDesgastePneusStressTest {

    private InterfaceJogo controleJogo;

    private Piloto criarPiloto(double valorNextDouble) {
        controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(valorNextDouble);
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);

        List<No> pista = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            No n = new No();
            n.setIndex(i);
            n.setTipo(No.RETA);
            pista.add(n);
        }
        when(controleJogo.getNosDaPista()).thenReturn(pista);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    private No criarNo(int index, java.awt.Color tipo) {
        No no = new No();
        no.setIndex(index);
        no.setTipo(tipo);
        return no;
    }

    private void invocaCalculaDesgastePneus(Carro carro, No no) throws Exception {
        Method m = Carro.class.getDeclaredMethod("calculaDesgastePneus", No.class);
        m.setAccessible(true);
        m.invoke(carro, no);
    }

    private void invocaProcessaStressDesgastePneus(Piloto piloto) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("processaStressDesgastePneus");
        m.setAccessible(true);
        m.invoke(piloto);
    }

    @Test
    void calculaDesgastePneus_naoAlteraEstresseDiretamente_curvaBaixa() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(85); // > 80, dispararia o decStress original
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaCalculaDesgastePneus(piloto.getCarro(), piloto.getNoAtual());

        assertEquals(85, piloto.getStress(), "calculaDesgastePneus não deveria mais escrever estresse diretamente");
    }

    @Test
    void processaStressDesgastePneus_curvaBaixa_incrementaEstresse() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10 - 0 = 10, reduzido a 5 em modo NORMAL
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(5, piloto.getStress(),
                "curva baixa em modo NORMAL deveria incrementar só metade (5), sem caps abaixo de 70/80/90");
    }

    @Test
    void processaStressDesgastePneus_curvaBaixa_modoAgressivo_incrementaEmDobroDoNormal() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 20 (cheio), escalado por incStress() em 0.5 (AGRESSIVO) -> 10
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(10, piloto.getStress(),
                "em modo AGRESSIVO a base é o valor cheio (20), o dobro do NORMAL (10), antes do escalonamento genérico de incStress()");
    }

    @Test
    void processaStressDesgastePneus_curvaBaixa_stressAcima80_incrementaCapadoSemDecrementar() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(85);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10 (NORMAL)
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        // incStress(10) escalado (NORMAL x0.5=5) com stress=85: cap ">80"->2 (85+2=87); nunca mais decrementa
        assertEquals(87, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_curvaAlta_stressAcima70_incrementaEmVezDeDecrementar() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(75);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10 (NORMAL)
        piloto.setNoAtual(criarNo(10, No.CURVA_ALTA));

        invocaProcessaStressDesgastePneus(piloto);

        // incStress(10) escalado (NORMAL x0.5=5) com stress=75>70: cap ">70"->3 (75+3=78); curva alta não decrementa mais
        assertEquals(78, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_curvaAlta_stressAte70_naoAltera() throws Exception {
        Piloto piloto = criarPiloto(0.8);
        piloto.setStress(70);
        piloto.getCarro().setPorcentagemDesgastePneus(250);
        piloto.setNoAtual(criarNo(10, No.CURVA_ALTA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(70, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_retaOuLargada_stressAcima60_naoAltera() throws Exception {
        // Não há intenção, em nenhum cenário, de o desgaste de pneu aumentar o estresse numa reta/largada.
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(65);
        piloto.getCarro().setPorcentagemDesgastePneus(0);
        piloto.setNoAtual(criarNo(10, No.RETA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(65, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_retaOuLargada_modoAgressivo_naoAltera() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(65);
        piloto.getCarro().setPorcentagemDesgastePneus(0);
        piloto.setNoAtual(criarNo(10, No.RETA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(65, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_retaOuLargada_stressAte60_naoAltera() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(60);
        piloto.getCarro().setPorcentagemDesgastePneus(0);
        piloto.setNoAtual(criarNo(10, No.RETA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(60, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_chovendo_naoDecrementaEmCurvaBaixa_masIncrementaContinua() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        when(controleJogo.isChovendo()).thenReturn(true);
        piloto.setStress(85);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        // incStress(10) com stress=85 -> cap ">80"->2 (85+2=87); decremento bloqueado por chuva
        assertEquals(87, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_safetyCarNaPista_naoAltera() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);
        piloto.setStress(0);
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(0, piloto.getStress());
    }
}
