package br.f1mane.entidades;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import br.f1mane.controles.InterfaceJogo;

/**
 * Piloto.processaTravouRodas() é o único ponto de decisão de travada de roda
 * por desgaste de pneus, completamente independente de Carro.calculaDesgastePneus
 * (nenhum estado compartilhado) — reavalia por conta própria tipo de nó (via
 * getNoAtual()), estresse, clima, safety car e asfalto abrasivo.
 */
class PilotoProcessaTravouRodasPorDesgastePneusTest {

    private InterfaceJogo controleJogo;
    private GameRandom random;

    private Piloto criarPiloto(int stress, boolean chovendo, boolean asfaltoAbrasivo) {
        controleJogo = mock(InterfaceJogo.class);
        random = mock(GameRandom.class);
        when(controleJogo.isChovendo()).thenReturn(chovendo);
        when(controleJogo.asfaltoAbrasivo()).thenReturn(asfaltoAbrasivo);
        when(controleJogo.isSafetyCarNaPista()).thenReturn(false);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.0);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        piloto.setStress(stress);
        return piloto;
    }

    private No criarNo(java.awt.Color tipo) {
        No no = new No();
        no.setTipo(tipo);
        return no;
    }

    @Test
    void curvaBaixa_comStressAcimaDe80_acionaTravouRodas() {
        Piloto piloto = criarPiloto(85, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void curvaBaixa_comStressAte80_naoAciona() {
        Piloto piloto = criarPiloto(80, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(piloto);
    }

    @Test
    void curvaBaixa_asfaltoAbrasivoComStressAcimaDe70_acionaMesmoSemStressAcimaDe80() {
        Piloto piloto = criarPiloto(75, false, true);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void curvaBaixa_stressAcimaDe80EAsfaltoAbrasivoAcimaDe70_acionaDuasVezes() {
        Piloto piloto = criarPiloto(85, false, true);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(2)).travouRodas(piloto);
    }

    @Test
    void curvaAlta_comStressAcimaDe70_acionaTravouRodas() {
        Piloto piloto = criarPiloto(75, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_ALTA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void curvaAlta_asfaltoAbrasivoStressAcimaDe50ERandomFavoravel_aciona() {
        Piloto piloto = criarPiloto(55, false, true);
        when(random.nextDouble()).thenReturn(0.9);
        piloto.setNoAtual(criarNo(No.CURVA_ALTA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void curvaAlta_asfaltoAbrasivoStressAcimaDe50ERandomDesfavoravel_naoAciona() {
        Piloto piloto = criarPiloto(55, false, true);
        when(random.nextDouble()).thenReturn(0.1);
        piloto.setNoAtual(criarNo(No.CURVA_ALTA));

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(piloto);
    }

    @Test
    void retaOuLargada_comStressAcimaDe60_acionaTravouRodas() {
        Piloto piloto = criarPiloto(65, false, false);
        piloto.setNoAtual(criarNo(No.RETA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodas(piloto);
    }

    @Test
    void retaOuLargada_stressAcimaDe60EAsfaltoAbrasivoAcimaDe80_acionaDuasVezes() {
        Piloto piloto = criarPiloto(85, false, true);
        piloto.setNoAtual(criarNo(No.LARGADA));

        piloto.processaTravouRodas();

        verify(controleJogo, times(2)).travouRodas(piloto);
    }

    @Test
    void chovendo_nuncaAcionaMesmoComStressAlto() {
        Piloto piloto = criarPiloto(95, true, true);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(piloto);
    }

    @Test
    void banderada_nuncaAciona() {
        Piloto piloto = criarPiloto(95, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));
        piloto.setRecebeuBanderada(true);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(any());
    }

    @Test
    void noPitLane_nuncaAciona() {
        Piloto piloto = criarPiloto(95, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));
        piloto.setPtosBox(1);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(any());
    }

    @Test
    void safetyCarNaPista_nuncaAciona() {
        Piloto piloto = criarPiloto(95, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_BAIXA));
        when(controleJogo.isSafetyCarNaPista()).thenReturn(true);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodas(any());
    }

    private boolean setColisao(Piloto piloto, boolean diantera, boolean centro) throws Exception {
        java.lang.reflect.Field campoDiantera = Piloto.class.getDeclaredField("colisaoDiantera");
        campoDiantera.setAccessible(true);
        campoDiantera.set(piloto, diantera);
        java.lang.reflect.Field campoCentro = Piloto.class.getDeclaredField("colisaoCentro");
        campoCentro.setAccessible(true);
        campoCentro.set(piloto, centro);
        return true;
    }

    @Test
    void colisaoDiantera_acionaTravouRodasPorColisao() throws Exception {
        Piloto piloto = criarPiloto(0, false, false); // stress zerado: nenhuma das condicoes de desgaste aciona
        piloto.setNoAtual(criarNo(No.RETA));
        setColisao(piloto, true, false);

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodasPorColisao(piloto);
        verify(controleJogo, never()).travouRodas(piloto);
    }

    @Test
    void colisaoCentro_acionaTravouRodasPorColisao() throws Exception {
        Piloto piloto = criarPiloto(0, false, false);
        piloto.setNoAtual(criarNo(No.CURVA_ALTA));
        setColisao(piloto, false, true);

        piloto.processaTravouRodas();

        verify(controleJogo, times(1)).travouRodasPorColisao(piloto);
    }

    @Test
    void semColisao_naoAcionaTravouRodasPorColisao() throws Exception {
        Piloto piloto = criarPiloto(0, false, false);
        piloto.setNoAtual(criarNo(No.RETA));
        setColisao(piloto, false, false);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodasPorColisao(piloto);
    }

    @Test
    void chovendo_colisaoNaoAciona() throws Exception {
        Piloto piloto = criarPiloto(0, true, false);
        piloto.setNoAtual(criarNo(No.RETA));
        setColisao(piloto, true, false);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodasPorColisao(any());
    }

    @Test
    void banderada_colisaoNaoAciona() throws Exception {
        Piloto piloto = criarPiloto(0, false, false);
        piloto.setNoAtual(criarNo(No.RETA));
        piloto.setRecebeuBanderada(true);
        setColisao(piloto, true, true);

        piloto.processaTravouRodas();

        verify(controleJogo, never()).travouRodasPorColisao(any());
    }
}
