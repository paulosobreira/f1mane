package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * processaFaiscas() já restringe faíscas a nós de reta/largada (nunca dispara
 * em nó de curva); o bônus de probabilidade por frenagem (isFreiandoReta)
 * agora só se aplica dentro de uma zona de frenagem, como consequência do
 * gate feito em processaFreioNaReta.
 */
class PilotoProcessaFaiscasZonaFrenagemTest {

    private InterfaceJogo controleJogo;

    @BeforeEach
    void setUp() {
        controleJogo = mock(InterfaceJogo.class);
        GameRandom random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.999);
        when(random.intervalo(40, 50)).thenReturn(40);
    }

    private Piloto criarPiloto(No noAtual) {
        Piloto piloto = new Piloto();
        piloto.setNome("Piloto");
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        carro.setGiro(Carro.GIRO_MAX_VAL);
        carro.setPorcentagemCombustivel(100);
        piloto.setCarro(carro);
        piloto.setControleJogo(controleJogo);
        piloto.setNoAtual(noAtual);
        piloto.setVelocidade(100);
        return piloto;
    }

    @Test
    void noDeCurvaBaixa_nuncaGeraFaisca() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_BAIXA);
        Piloto piloto = criarPiloto(no);

        piloto.processaFaiscas();

        assertFalse(piloto.isFaiscas());
    }

    @Test
    void noDeCurvaAlta_nuncaGeraFaisca() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.CURVA_ALTA);
        Piloto piloto = criarPiloto(no);

        piloto.processaFaiscas();

        assertFalse(piloto.isFaiscas());
    }

    @Test
    void retaComFreiandoRetaFalso_naoRecebeBonusDeProbabilidade() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        Piloto piloto = criarPiloto(no);
        piloto.setFreiandoReta(false);
        // sem bonus, mod fica em .995; random=0.7 nao supera .995 => nao gera faisca
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.7);

        piloto.processaFaiscas();

        assertFalse(piloto.isFaiscas(), "sem o bonus de frenagem (fora da zona de frenagem), o limiar base nao deveria ser superado");
    }

    @Test
    void retaComFreiandoRetaVerdadeiro_recebeBonusDeProbabilidade() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        Piloto piloto = criarPiloto(no);
        piloto.setFreiandoReta(true);
        // com bonus (tracado 0), mod cai pra .495; random=0.7 supera .495 => gera faisca
        when(controleJogo.getRandom().nextDouble()).thenReturn(0.7);

        piloto.processaFaiscas();

        assertTrue(piloto.isFaiscas(), "com o bonus de frenagem (dentro da zona de frenagem), o limiar reduzido deveria ser superado");
    }
}
