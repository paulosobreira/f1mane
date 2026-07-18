package br.flmane.entidades;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import br.flmane.controles.InterfaceJogo;

/**
 * Piloto.processaStress() é o único ponto que chama incStress/decStress.
 * Os métodos originais (processaPneusIncomaptiveis, processaPenalidadeColisao,
 * processaFreioNaReta) continuam avaliando suas próprias condições, mas não
 * escrevem mais estresse diretamente — ou sinalizam um flag consumido por
 * processaStress() (freio na reta, onde a condição não pode ser rederivada
 * depois porque o próprio método consome/reseta o estado envolvido).
 */
class PilotoProcessaStressConsolidadoTest {

    private InterfaceJogo controleJogo;
    private GameRandom random;

    private Piloto criarPiloto() {
        controleJogo = mock(InterfaceJogo.class);
        random = mock(GameRandom.class);
        when(controleJogo.getRandom()).thenReturn(random);
        when(random.nextDouble()).thenReturn(0.0);
        when(controleJogo.isChovendo()).thenReturn(false);

        Piloto piloto = new Piloto();
        piloto.setControleJogo(controleJogo);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        No no = new No();
        no.setTipo(No.RETA);
        piloto.setNoAtual(no);
        return piloto;
    }

    private void invocaPrivado(Piloto piloto, String metodo) throws Exception {
        Method m = Piloto.class.getDeclaredMethod(metodo);
        m.setAccessible(true);
        m.invoke(piloto);
    }

    private void setCampo(Piloto piloto, String campo, Object valor) throws Exception {
        Field f = Piloto.class.getDeclaredField(campo);
        f.setAccessible(true);
        f.set(piloto, valor);
    }

    private Object getCampo(Piloto piloto, String campo) throws Exception {
        Field f = Piloto.class.getDeclaredField(campo);
        f.setAccessible(true);
        return f.get(piloto);
    }

    @Test
    void processaPneusIncomaptiveis_naoAlteraEstresseDiretamente() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.getCarro().setTipoPneu(Carro.TIPO_PNEU_CHUVA); // incompativel com pista seca
        piloto.getNoAtual().setTipo(No.CURVA_BAIXA);
        piloto.setStress(0);

        invocaPrivado(piloto, "processaPneusIncomaptiveis");

        assertEquals(0, piloto.getStress(), "o método original não deveria mais escrever estresse diretamente");
    }

    @Test
    void processaStressPneusIncompativeis_curvaBaixa_incrementaEstresse() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.getCarro().setTipoPneu(Carro.TIPO_PNEU_CHUVA);
        piloto.getNoAtual().setTipo(No.CURVA_BAIXA);
        piloto.setStress(0);

        invocaPrivado(piloto, "processaStressPneusIncompativeis");

        // incStress(4) em modo NORMAL e escalado por incStress() em 0.5 -> 2
        assertEquals(2, piloto.getStress(), "curva baixa com pneu incompatível deveria incrementar 4, escalado a 2 em modo NORMAL");
    }

    @Test
    void processaPenalidadeColisao_naoAlteraEstresseDiretamente() {
        Piloto piloto = criarPiloto();
        Piloto pilotoFrente = criarPiloto();
        piloto.setColisao(pilotoFrente);
        piloto.setStress(0);

        piloto.processaPenalidadeColisao();

        assertEquals(0, piloto.getStress(), "o método original não deveria mais escrever estresse diretamente");
    }

    @Test
    void processaStressColisao_incrementaEstresseEmColisao() throws Exception {
        Piloto piloto = criarPiloto();
        Piloto pilotoFrente = criarPiloto();
        piloto.setColisao(pilotoFrente);
        piloto.setStress(0);

        invocaPrivado(piloto, "processaStressColisao");

        // incStress(12) (60% dos 20 originais) em modo NORMAL e escalado por incStress() em 0.5 -> round(6.0) = 6
        assertEquals(6, piloto.getStress(), "colisão em andamento deveria incrementar 12 (60% do total, nível similar ao desgaste de pneu), escalado a 6 em modo NORMAL");
    }

    @Test
    void processaStressColisao_evitaBaterCarroFrente_incrementaFracaoAdicional() throws Exception {
        Piloto piloto = criarPiloto();
        Piloto pilotoFrente = criarPiloto();
        piloto.setColisao(pilotoFrente);
        piloto.setStress(0);
        setCampo(piloto, "evitaBaterCarroFrente", true);

        invocaPrivado(piloto, "processaStressColisao");

        // evitaBaterCarroFrente tem prioridade exclusiva sobre a colisão: incStress(8) (40% dos 20 originais)
        // em modo NORMAL e escalado por incStress() em 0.5 -> round(4.0) = 4
        assertEquals(4, piloto.getStress(),
                "evitaBaterCarroFrente deveria incrementar só 8 (40%, exclusivo — não soma com a colisão), escalado a 4 em modo NORMAL");
    }

    @Test
    void processaStressColisao_semColisao_naoAltera() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setColisao(null);
        piloto.setStress(0);

        invocaPrivado(piloto, "processaStressColisao");

        assertEquals(0, piloto.getStress());
    }

    @Test
    void processaFreioNaRetaMalSucedido_naoAlteraEstresseDiretamente_masSinalizaFlag() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);
        setCampo(piloto, "freioNaRetaMalSucedidoNesteTick", 7);

        assertEquals(0, piloto.getStress(), "sinalizar o flag não deveria por si só alterar o estresse");

        invocaPrivado(piloto, "processaStressFreioNaRetaMalSucedido");

        // incStress(7) em modo NORMAL e escalado por incStress() em 0.5 -> round(3.5) = 4
        assertEquals(4, piloto.getStress(), "processaStress() deveria consumir o flag e aplicar o incremento sinalizado, escalado pelo modo");
        assertNull(getCampo(piloto, "freioNaRetaMalSucedidoNesteTick"), "o flag deveria ser limpo após consumido");
    }

    @Test
    void processaStressFreioNaRetaMalSucedido_semFlag_naoAltera() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);

        invocaPrivado(piloto, "processaStressFreioNaRetaMalSucedido");

        assertEquals(0, piloto.getStress());
    }

    /**
     * Chamado diretamente por ControleBox.processarPilotoBox() (não faz
     * parte de processaStress(), que nunca roda enquanto getPtosBox() != 0).
     */
    @Test
    void processaStressFilaBox_decrementaEstresseEmDois() {
        Piloto piloto = criarPiloto();
        piloto.setStress(50);
        when(random.nextDouble()).thenReturn(0.99); // satisfaz o sorteio interno de decStress

        piloto.processaStressFilaBox();

        // decStress(2) em modo NORMAL (padrao) e escalado por decStress() em 1x -> 2
        assertEquals(48, piloto.getStress());
    }

    @Test
    void processaStressFilaBox_modoAgressivo_naoRecuperaMais() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);
        when(random.nextDouble()).thenReturn(0.99); // satisfaz o sorteio interno de decStress

        piloto.processaStressFilaBox();

        // decStress(2) em modo AGRESSIVO -> escalado a 0x: essa era a unica via de
        // recuperacao que sobrava pro AGRESSIVO (o decaimento passivo por tick ja o excluia)
        assertEquals(50, piloto.getStress(),
                "AGRESSIVO nao deveria mais recuperar estresse nem mesmo avancando na fila do box");
    }

    /**
     * ControleCorrida.danificaAreofolio() sinaliza este flag ao invés de
     * chamar incStress(15) diretamente; processaStress() consome e aplica.
     */
    @Test
    void sinalizaDanoAereofolio_naoAlteraEstresseDiretamente_masSinalizaFlag() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);

        piloto.sinalizaDanoAereofolio();

        assertEquals(0, piloto.getStress(), "sinalizar o flag não deveria por si só alterar o estresse");
        assertEquals(true, getCampo(piloto, "sofreuDanoAereofolioNesteTick"));
    }

    @Test
    void processaStressDanoAereofolio_flagAtivo_incrementaETrintaEConsome() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);
        piloto.sinalizaDanoAereofolio();

        invocaPrivado(piloto, "processaStressDanoAereofolio");

        // incStress(30) em modo NORMAL e escalado por incStress() em 0.5 -> round(15.0) = 15
        assertEquals(15, piloto.getStress());
        assertEquals(false, getCampo(piloto, "sofreuDanoAereofolioNesteTick"), "o flag deveria ser limpo após consumido");
    }

    @Test
    void processaStressDanoAereofolio_semFlag_naoAltera() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);

        invocaPrivado(piloto, "processaStressDanoAereofolio");

        assertEquals(0, piloto.getStress());
    }

    /**
     * incStress()/decStress() são o único lugar que escala a magnitude por
     * modo de pilotagem — aplica automaticamente a qualquer gatilho que passe
     * por eles, sem precisar duplicar a escala em cada um.
     */
    @Test
    void incStress_modoAgressivo_reduzEm50Porcento_maximoSemZerarMenorIncremento() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(0);

        piloto.incStress(10);
        assertEquals(5, piloto.getStress(), "AGRESSIVO deveria reduzir o incremento em 50% (10 -> 5)");

        piloto.setStress(0);
        piloto.incStress(1); // menor incremento existente no código (colisão/desconcentração)
        assertEquals(1, piloto.getStress(),
                "com fator 0.5 (o máximo redutível), o menor incremento (1) ainda arredonda pra 1, não zera");
    }

    @Test
    void incStress_modoNormal_reduzPelaMetade() {
        Piloto piloto = criarPiloto();
        piloto.setStress(0);

        piloto.incStress(10);

        assertEquals(5, piloto.getStress(), "NORMAL deveria reduzir o incremento pela metade (10 -> 5)");
    }

    @Test
    void incStress_modoLento_naoAltera() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(0);

        piloto.incStress(10);

        assertEquals(10, piloto.getStress(), "LENTO não deveria alterar o incremento (nunca foi pedida redução ali)");
    }

    @Test
    void decStress_modoNormal_naoEscala() {
        Piloto piloto = criarPiloto();
        piloto.setStress(50);
        when(random.nextDouble()).thenReturn(0.99); // satisfaz o sorteio interno de decStress

        piloto.decStress(10);

        assertEquals(40, piloto.getStress(), "NORMAL deveria aplicar o decremento em 1x (10 -> 10), sem o bonus de 10% que existia antes");
    }

    @Test
    void decStress_modoLento_aumentaEm50Porcento() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(50);
        when(random.nextDouble()).thenReturn(0.99);

        piloto.decStress(10);

        assertEquals(35, piloto.getStress(), "LENTO deveria aumentar o decremento em 50% (10 -> 15), mais que o NORMAL (10)");
    }

    @Test
    void decStress_modoAgressivo_naoRecuperaMais() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);
        when(random.nextDouble()).thenReturn(0.99);

        piloto.decStress(10);

        assertEquals(50, piloto.getStress(), "AGRESSIVO deveria escalar o decremento a 0x — nao recupera estresse nenhum (10 -> 0)");
    }

    /**
     * A diferença entre 1º e último colocado no sorteio de incStress/decStress
     * foi reduzida pela metade (coeficiente de posição: 35->17.5 no
     * incremento, 20->10 na recuperação), mas o efeito continua existindo
     * (líder ainda pega mais estresse e recupera menos que o último colocado).
     */
    @Test
    void incStress_liderTemMaisChanceQueUltimoColocado_masDiferencaReduzida() {
        Piloto lider = criarPiloto();
        lider.setPosicao(1);
        lider.setStress(0);
        when(random.nextDouble()).thenReturn(0.6); // < gate do líder (0.8825), > gate do último (0.55)

        lider.incStress(10);

        Piloto ultimo = criarPiloto();
        ultimo.setPosicao(20);
        ultimo.setStress(0);
        when(ultimo.getControleJogo().getRandom().nextDouble()).thenReturn(0.6);

        ultimo.incStress(10);

        // incStress(10) em modo NORMAL (padrao) e escalado por incStress() em 0.5 -> 5
        assertEquals(5, lider.getStress(), "líder (posição 1) deveria pegar o incremento com esse sorteio");
        assertEquals(0, ultimo.getStress(), "último colocado (posição 20) não deveria pegar o incremento com o mesmo sorteio");
    }

    @Test
    void decStress_ultimoColocadoRecuperaMaisFacilQueLider() {
        Piloto lider = criarPiloto();
        lider.setPosicao(1);
        lider.setStress(50);
        when(random.nextDouble()).thenReturn(0.6); // > gate do líder (0.69)? não; < gate do último (0.50)? não

        lider.decStress(10);

        Piloto ultimo = criarPiloto();
        ultimo.setPosicao(20);
        ultimo.setStress(50);
        when(ultimo.getControleJogo().getRandom().nextDouble()).thenReturn(0.6);

        ultimo.decStress(10);

        // decStress(10) em modo NORMAL (padrao) e escalado por decStress() em 1x -> 10
        assertEquals(50, lider.getStress(), "líder (posição 1, gate 0.69) não deveria recuperar com esse sorteio");
        assertEquals(40, ultimo.getStress(), "último colocado (posição 20, gate 0.50) deveria recuperar com o mesmo sorteio");
    }

    /**
     * Tetos de incStress() por faixa de stress: 50/70/90 -> max 3/2/1.
     * A faixa 50-70 (antes sem nenhum teto) e a extensao do teto de 2 pra
     * cobrir 70-90 inteiro (antes só 80-90) são os dois pontos que mudaram.
     */
    @Test
    void incStress_stressAcimaDe50_limitaATres() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO); // LENTO nao escala o valor bruto, isola o teste do teto
        piloto.setStress(60);

        piloto.incStress(20);

        assertEquals(63, piloto.getStress(), "stress>50 deveria limitar o incremento a 3 (era sem teto nenhum nessa faixa)");
    }

    @Test
    void incStress_stressAcimaDe70_limitaADois() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(75);

        piloto.incStress(20);

        assertEquals(77, piloto.getStress(), "stress>70 deveria limitar o incremento a 2 (era limitado a 3 nessa faixa, ate stress 80)");
    }

    @Test
    void incStress_stressAcimaDe90_limitaAUm() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(91);

        piloto.incStress(20);

        assertEquals(92, piloto.getStress(), "stress>90 deveria continuar limitando o incremento a 1, sem mudanca nessa faixa");
    }

    @Test
    void incStress_stressAbaixoDe50_semTeto() {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.LENTO);
        piloto.setStress(40);

        piloto.incStress(20);

        assertEquals(60, piloto.getStress(), "stress<=50 continua sem nenhum teto de incStress");
    }

    /**
     * O decaimento passivo por tick em processaStress() (nao incStress/decStress
     * diretamente) ja excluia AGRESSIVO antes desta revisao — continua excluindo.
     */
    @Test
    void processaStress_modoAgressivo_naoRecuperaPassivamente() throws Exception {
        Piloto piloto = criarPiloto();
        piloto.setModoPilotagem(Piloto.AGRESSIVO);
        piloto.setStress(50);

        invocaPrivado(piloto, "processaStress");

        assertEquals(50, piloto.getStress(),
                "o decaimento passivo por tick nao chama decStress para AGRESSIVO, com ou sem essa revisao");
    }
}
