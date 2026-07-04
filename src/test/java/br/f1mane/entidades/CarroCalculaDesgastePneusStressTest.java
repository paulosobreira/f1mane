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
 * consumida por processaStress(). Com habilidade/freios/aerodinâmica em 0,
 * testeHabilidadePiloto()/testeHabilidadePilotoAerodinamicaFreios() resolvem
 * consistentemente para falso, para qualquer valor de random.nextDouble()
 * usado abaixo.
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
    void processaStressDesgastePneus_curvaBaixa_modoAgressivo_incrementaComRedução50Porcento() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(0);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10, escalado por incStress() em 0.5 (AGRESSIVO) -> 5
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(5, piloto.getStress(), "em modo AGRESSIVO o incremento é reduzido em 50% (igual ao NORMAL)");
    }

    @Test
    void processaStressDesgastePneus_curvaBaixa_stressAcima80_incrementaCapadoEDecrementaTambem() throws Exception {
        // nextDouble=0.8 satisfaz tanto o sorteio interno de incStress (< 0.9) quanto o de decStress (> 0.7)
        Piloto piloto = criarPiloto(0.8);
        piloto.setStress(85);
        piloto.getCarro().setPorcentagemDesgastePneus(250); // incStress base=8, decStress base=2 (valores artificiais so pra teste)
        piloto.setNoAtual(criarNo(10, No.CURVA_BAIXA));

        invocaProcessaStressDesgastePneus(piloto);

        // incStress(8) escalado (NORMAL x0.5=4) com stress=85: cap ">80"->2 (85+2=87);
        // decStress(2/2=1) escalado (NORMAL x1.25=round(1.25)=1) com stress=87>80: 87-1=86
        assertEquals(86, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_curvaAlta_stressAcima70_decrementa() throws Exception {
        Piloto piloto = criarPiloto(0.8); // > 0.7, satisfaz o sorteio interno de decStress
        piloto.setStress(75);
        piloto.getCarro().setPorcentagemDesgastePneus(250); // decStress base=2, ternario (habilidade falsa) -> 2/2=1
        piloto.setNoAtual(criarNo(10, No.CURVA_ALTA));

        invocaProcessaStressDesgastePneus(piloto);

        // decStress(1) escalado por decStress() em 1.25 (NORMAL) -> round(1.25) = 1
        assertEquals(74, piloto.getStress());
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
    void processaStressDesgastePneus_retaOuLargada_stressAcima60_incrementa() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setStress(65); // >60, mas abaixo de 70/80/90 -> nenhum cap interno se aplica
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10, reduzido a 5 em modo NORMAL
        piloto.setNoAtual(criarNo(10, No.RETA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(70, piloto.getStress());
    }

    @Test
    void processaStressDesgastePneus_retaOuLargada_modoAgressivo_incrementaComRedução50Porcento() throws Exception {
        Piloto piloto = criarPiloto(0.0);
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(65);
        piloto.getCarro().setPorcentagemDesgastePneus(0); // incStress base = 10, escalado por incStress() em 0.5 (AGRESSIVO) -> 5
        piloto.setNoAtual(criarNo(10, No.RETA));

        invocaProcessaStressDesgastePneus(piloto);

        assertEquals(70, piloto.getStress());
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
