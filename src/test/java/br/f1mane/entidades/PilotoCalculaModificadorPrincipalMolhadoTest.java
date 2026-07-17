package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Piloto.calculaModificadorPrincipal passou a reduzir "comparador" (o limiar
 * de sorteio da banda de ganho) na proporção de "molhado%", em vez de um
 * corte fixo de 0.2/0.3 gatilhado por isChovendo() binário. Com
 * habilidade/potência/aerodinâmica/freios em 0, todos os testes de
 * habilidade resolvem para falso para qualquer sorteio positivo — isolando
 * comparador = 0.3 * (1 - molhado%) nesse cenário (giro/modo padrão de um
 * Piloto novo, curva baixa). A saída é discreta (banda alta/baixa), então
 * "proporcional" se demonstra pelo ponto de corte do sorteio se deslocar de
 * forma monotônica com molhado%, não por um valor contínuo.
 */
class PilotoCalculaModificadorPrincipalMolhadoTest {

    private Piloto criarPiloto(double molhado, double valorNextDouble) {
        InterfaceJogo controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(valorNextDouble);
        when(controleJogo.getMolhado()).thenReturn(molhado);

        Piloto piloto = new Piloto();
        piloto.setHabilidade(0);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);

        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_BAIXA);
        piloto.setNoAtual(no);
        return piloto;
    }

    private int invocaCalculaModificadorPrincipal(Piloto piloto) throws Exception {
        Method m = Piloto.class.getDeclaredMethod("calculaModificadorPrincipal");
        m.setAccessible(true);
        return (int) m.invoke(piloto);
    }

    @Test
    void molhadoZero_reproduzBitABitOComparadorSecoDeHoje() throws Exception {
        // comparador = 0.3 (sem redução climática); 0.29 < 0.3 -> banda alta (15)
        Piloto piloto = criarPiloto(0.0, 0.29);

        assertEquals(15, invocaCalculaModificadorPrincipal(piloto));
    }

    @Test
    void molhadoUm_reproduzBitABitOComparadorDeChuvaDeHoje() throws Exception {
        // comparador = 0.3 - 1.0*0.3 = 0.0; mesmo sorteio (0.29) agora nao fica abaixo -> banda baixa (10)
        Piloto piloto = criarPiloto(1.0, 0.29);

        assertEquals(10, invocaCalculaModificadorPrincipal(piloto));
    }

    @Test
    void molhadoIntermediario_pontoDeCorteFicaEntreOsExtremos() throws Exception {
        // comparador(0.4) = 0.3*0.6 = 0.18; sorteio fixo 0.16 ainda fica abaixo -> banda alta (15)
        Piloto pilotoMenosMolhado = criarPiloto(0.4, 0.16);
        assertEquals(15, invocaCalculaModificadorPrincipal(pilotoMenosMolhado));

        // comparador(0.5) = 0.3*0.5 = 0.15; o mesmo sorteio (0.16) deixa de ficar abaixo -> banda baixa (10)
        Piloto pilotoMaisMolhado = criarPiloto(0.5, 0.16);
        assertEquals(10, invocaCalculaModificadorPrincipal(pilotoMaisMolhado));
    }
}
