package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Carro.calculaModificadorAsa fica explicitamente fora do escopo da rampa de
 * "molhado%" (design.md, Non-Goals) — continua binário, só refletindo o
 * clima categórico atual. Este teste trava esse comportamento: variar
 * molhado% sozinho, com o clima categórico e todo o resto fixos, não pode
 * mudar o resultado.
 */
class CarroCalculaModificadorAsaMolhadoTest {

    private Carro criarCarro(double molhado) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.5); // positivo -> testeAerodinamica/testePotencia/testeFreios (campos=0) sempre falso
        when(controleJogo.isChovendo()).thenReturn(false);
        when(controleJogo.getMolhado()).thenReturn(molhado);

        Piloto piloto = new Piloto();
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        carro.setAsa(Carro.MAIS_ASA);
        return carro;
    }

    private No criarNoCurvaAlta() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_ALTA);
        return no;
    }

    private double invocaCalculaModificadorAsa(Carro carro, double ganho, No no) throws Exception {
        Method m = Carro.class.getDeclaredMethod("calculaModificadorAsa", double.class, No.class);
        m.setAccessible(true);
        return (double) m.invoke(carro, ganho, no);
    }

    @Test
    void resultadoIdenticoParaQualquerMolhadoComMesmoClimaCategorico() throws Exception {
        No no = criarNoCurvaAlta();

        double resultadoSeco = invocaCalculaModificadorAsa(criarCarro(0.0), 100.0, no);
        double resultadoIntermediario = invocaCalculaModificadorAsa(criarCarro(0.5), 100.0, no);
        double resultadoMolhado = invocaCalculaModificadorAsa(criarCarro(1.0), 100.0, no);

        assertEquals(resultadoSeco, resultadoIntermediario, 1e-9);
        assertEquals(resultadoSeco, resultadoMolhado, 1e-9);
    }
}
