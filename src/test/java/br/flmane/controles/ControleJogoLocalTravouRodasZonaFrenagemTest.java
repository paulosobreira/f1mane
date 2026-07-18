package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import br.flmane.entidades.Carro;
import br.flmane.entidades.GameRandom;
import br.flmane.entidades.No;
import br.flmane.entidades.Piloto;

/**
 * ControleJogoLocal.travouRodas SÓ gera TravadaRoda dentro de uma zona de
 * frenagem detectada (fora dela, é sempre vetada, qualquer que seja o tipo
 * do nó); dentro da zona, a intensidade varia conforme a posição relativa
 * (maior no início, menor no final — Global.INTENSIDADE_MARCA_INICIO/FIM_ZONA_FRENAGEM).
 */
class ControleJogoLocalTravouRodasZonaFrenagemTest {

    private ControleJogoLocal criarControle(double valorNextDouble) throws Exception {
        ControleJogoLocal controle = new ControleJogoLocal(1L);
        GameRandom random = mock(GameRandom.class);
        when(random.nextDouble()).thenReturn(valorNextDouble);
        when(random.intervalo(anyInt(), anyInt())).thenReturn(15);
        Field campoRandom = ControleRecursos.class.getDeclaredField("random");
        campoRandom.setAccessible(true);
        campoRandom.set(controle, random);
        return controle;
    }

    private No registrarNo(ControleJogoLocal controle, int index, java.awt.Color tipo) {
        No no = new No();
        no.setIndex(index);
        no.setTipo(tipo);
        controle.mapaNosIds.put(no, index + 1);
        controle.mapaIdsNos.put(index + 1, no);
        return no;
    }

    private Piloto criarPiloto(ControleJogoLocal controle, No noAtual) {
        Piloto piloto = new Piloto();
        piloto.setControleJogo(controle);
        piloto.setNoAtual(noAtual);
        Carro carro = new Carro();
        carro.setPiloto(piloto);
        piloto.setCarro(carro);
        return piloto;
    }

    @Test
    void curvaBaixaForaDaZonaDeFrenagem_nuncaGeraTravadaDeRoda() throws Exception {
        // random=0.1 passaria facilmente no limite base (0.3), mas fora da zona a marca é vetada por completo.
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.CURVA_BAIXA);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodas(piloto);

        assertFalse(piloto.isTravouRodas(), "fora da zona de frenagem, a marca de pneu nunca deveria ocorrer");
    }

    @Test
    void retaForaDaZonaDeFrenagem_nuncaGeraTravadaDeRoda() throws Exception {
        // reta fora de qualquer zona detectada: antes só curvas eram vetadas/reduzidas, agora nenhum no fora da zona gera marca.
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.RETA);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodas(piloto);

        assertFalse(piloto.isTravouRodas(), "fora da zona de frenagem, nenhum tipo de nó deveria gerar marca de pneu");
    }

    @Test
    void inicioDaZonaDeFrenagem_usaIntensidadeMaxima() throws Exception {
        // no inicio (posicao 0.0), lim = 0.3 * INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM (1.0) = 0.3; random=0.1 < 0.3 -> ocorre.
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.RETA);
        controle.zonaFrenagemPosicoes.put(no, 0.0);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodas(piloto);

        assertTrue(piloto.isTravouRodas(), "no início da zona de frenagem, a intensidade máxima deveria permitir a marca");
    }

    @Test
    void finalDaZonaDeFrenagem_usaIntensidadeReduzida() throws Exception {
        // no final (posicao 1.0), lim = 0.3 * INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM (0.3) = 0.09; random=0.25 > 0.09 -> nao ocorre.
        ControleJogoLocal controle = criarControle(0.25);
        No no = registrarNo(controle, 10, No.CURVA_BAIXA);
        controle.zonaFrenagemPosicoes.put(no, 1.0);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodas(piloto);

        assertFalse(piloto.isTravouRodas(),
                "no final da zona de frenagem, a intensidade reduzida não deveria permitir a marca com esse sorteio");
    }

    @Test
    void mesmoSorteio_ocorreNoInicioENaoOcorreNoFinalDaZona() throws Exception {
        // random=0.25: inicio (lim=0.3) permite; final (lim=0.09) nao permite -- demonstra o gradiente.
        ControleJogoLocal controleInicio = criarControle(0.25);
        No noInicio = registrarNo(controleInicio, 10, No.RETA);
        controleInicio.zonaFrenagemPosicoes.put(noInicio, 0.0);
        Piloto pilotoInicio = criarPiloto(controleInicio, noInicio);
        controleInicio.travouRodas(pilotoInicio);

        ControleJogoLocal controleFinal = criarControle(0.25);
        No noFinal = registrarNo(controleFinal, 10, No.CURVA_BAIXA);
        controleFinal.zonaFrenagemPosicoes.put(noFinal, 1.0);
        Piloto pilotoFinal = criarPiloto(controleFinal, noFinal);
        controleFinal.travouRodas(pilotoFinal);

        assertTrue(pilotoInicio.isTravouRodas(), "com o mesmo sorteio, o início da zona (intensidade máxima) deveria gerar marca");
        assertFalse(pilotoFinal.isTravouRodas(), "com o mesmo sorteio, o final da zona (intensidade reduzida) não deveria gerar marca");
    }

    /**
     * Piloto.processaTravouRodas() (chamado a cada tick, completamente
     * independente de Carro.calculaDesgastePneus) reavalia estresse/clima/
     * asfalto por conta própria a partir do nó atual do piloto, mas o
     * gatilho final ainda passa por ControleJogoLocal.travouRodas — então
     * herda o mesmo veto por zona de frenagem automaticamente, mesmo sendo
     * acionado por condições que não sabem nada sobre zona de frenagem.
     */
    @Test
    void processaTravouRodas_curvaAltaForaDaZona_nuncaGeraMarca() throws Exception {
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.CURVA_ALTA);
        Piloto piloto = criarPiloto(controle, no);
        piloto.setStress(85); // acima do limiar de 70 que aciona a travada por desgaste em curva alta

        piloto.processaTravouRodas();

        assertFalse(piloto.isMarcaPneu(),
                "acionado por desgaste de pneus num nó de curva alta fora de qualquer zona de frenagem, "
                        + "não deveria gerar marca de pneu");
    }

    @Test
    void processaTravouRodas_curvaBaixaDentroDaZona_podeGerarMarca() throws Exception {
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.CURVA_BAIXA);
        controle.zonaFrenagemPosicoes.put(no, 0.0);
        Piloto piloto = criarPiloto(controle, no);
        piloto.setStress(85); // acima do limiar de 80 que aciona a travada por desgaste em curva baixa

        piloto.processaTravouRodas();

        assertTrue(piloto.isMarcaPneu(),
                "acionado por desgaste de pneus num nó de curva baixa dentro da zona de frenagem, "
                        + "deveria poder gerar marca de pneu");
    }

    /**
     * ControleJogoLocal.travouRodasPorColisao, diferente de travouRodas, não
     * é vetado por estar fora de uma zona de frenagem — uma colisão pode
     * acontecer em qualquer ponto da pista. Fora de zona, usa a intensidade
     * máxima (freada de emergência); dentro da zona, mantém o mesmo
     * gradiente de posição de travouRodas.
     */
    @Test
    void travouRodasPorColisao_foraDaZonaDeFrenagem_aindaGeraMarca() throws Exception {
        ControleJogoLocal controle = criarControle(0.1);
        No no = registrarNo(controle, 10, No.RETA);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodasPorColisao(piloto);

        assertTrue(piloto.isTravouRodas(),
                "colisão fora de qualquer zona de frenagem ainda deveria poder gerar marca de pneu");
    }

    @Test
    void travouRodasPorColisao_foraDaZona_usaIntensidadeMaxima() throws Exception {
        // fora da zona, lim = 0.3 * INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM (1.0) = 0.3; random=0.25 < 0.3 -> ocorre.
        ControleJogoLocal controle = criarControle(0.25);
        No no = registrarNo(controle, 10, No.CURVA_ALTA);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodasPorColisao(piloto);

        assertTrue(piloto.isTravouRodas(),
                "fora da zona, a colisão deveria usar a intensidade máxima, não a reduzida");
    }

    @Test
    void travouRodasPorColisao_dentroDaZonaNoFinal_mantemGradienteReduzido() throws Exception {
        // no final da zona (posicao 1.0), lim = 0.3 * 0.3 = 0.09; random=0.25 > 0.09 -> nao ocorre.
        ControleJogoLocal controle = criarControle(0.25);
        No no = registrarNo(controle, 10, No.CURVA_BAIXA);
        controle.zonaFrenagemPosicoes.put(no, 1.0);
        Piloto piloto = criarPiloto(controle, no);

        controle.travouRodasPorColisao(piloto);

        assertFalse(piloto.isTravouRodas(),
                "dentro da zona, a colisão deveria manter o gradiente de posição, não sempre a intensidade máxima");
    }
}
