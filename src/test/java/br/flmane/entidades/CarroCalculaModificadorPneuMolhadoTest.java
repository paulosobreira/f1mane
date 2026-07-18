package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Carro.calculaModificadorPneu passou a interpolar por "molhado%" entre o
 * resultado seco (bônus de pneu MOLE/DURO) e o resultado molhado (multiplicador
 * de chuva) de hoje, em vez de escolher um dos dois extremos via
 * isChovendo() binário. Cenário escolhido (pneu MOLE, curva baixa, pista
 * emborrachada) é determinístico — não depende de nenhum sorteio, isolando a
 * interpolação em si.
 */
class CarroCalculaModificadorPneuMolhadoTest {

    private InterfaceJogo controleJogo;

    private Carro criarCarro(double molhado) {
        controleJogo = mock(InterfaceJogo.class);
        when(controleJogo.getMolhado()).thenReturn(molhado);
        when(controleJogo.verificaPistaEmborrachada()).thenReturn(true);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        carro.setTipoPneu(Carro.TIPO_PNEU_MOLE);
        carro.setPorcentagemDesgastePneus(50); // dentro da faixa 10-90 que habilita o bônus/penalidade
        return carro;
    }

    private No criarNoCurvaBaixa() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_BAIXA);
        return no;
    }

    private double invocaCalculaModificadorPneu(Carro carro, double ganho, No no) throws Exception {
        Method m = Carro.class.getDeclaredMethod("calculaModificadorPneu", double.class, No.class);
        m.setAccessible(true);
        return (double) m.invoke(carro, ganho, no);
    }

    @Test
    void molhadoZero_reproduzBitABitOResultadoSecoDeHoje() throws Exception {
        Carro carro = criarCarro(0.0);

        double resultado = invocaCalculaModificadorPneu(carro, 100.0, criarNoCurvaBaixa());

        // pista emborrachada -> ganho *= 1.2, sem depender de sorteio
        assertEquals(120.0, resultado, 1e-9);
    }

    @Test
    void molhadoUm_reproduzBitABitOResultadoDeChuvaDeHoje() throws Exception {
        Carro carro = criarCarro(1.0);

        double resultado = invocaCalculaModificadorPneu(carro, 100.0, criarNoCurvaBaixa());

        // pneu MOLE (nao-chuva) em curva, chovendo -> ganho *= 0.85
        assertEquals(85.0, resultado, 1e-9);
    }

    @Test
    void molhadoIntermediario_produzResultadoEntreOsDoisExtremos() throws Exception {
        Carro carro = criarCarro(0.4);

        double resultado = invocaCalculaModificadorPneu(carro, 100.0, criarNoCurvaBaixa());

        // seco=120, molhado=85 -> 120 + 0.4*(85-120) = 106
        assertEquals(106.0, resultado, 1e-9);
    }
}
