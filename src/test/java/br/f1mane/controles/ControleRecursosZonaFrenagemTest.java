package br.f1mane.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.recursos.CarregadorRecursos;

/**
 * Zona de frenagem: reta considerável seguida de um cluster relevante de
 * curva baixa, sem muitas curvas altas no meio (senão é mais provável uma
 * sequência de esses/chicane que uma reta de frenagem de verdade). Ver
 * ControleRecursos.calculaZonaFrenagem().
 */
class ControleRecursosZonaFrenagemTest {

    private No criarNo(int index, java.awt.Color tipo) {
        No no = new No();
        no.setIndex(index);
        no.setTipo(tipo);
        return no;
    }

    private List<No> retas(int quantidade) {
        List<No> nos = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            nos.add(criarNo(nos.size(), No.RETA));
        }
        return nos;
    }

    private List<No> curvasBaixas(List<No> destino, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            destino.add(criarNo(destino.size(), No.CURVA_BAIXA));
        }
        return destino;
    }

    private ControleJogoLocal criarControle() throws Exception {
        return new ControleJogoLocal(1L);
    }

    @Test
    void retaLongaSeguidaDeCurvaBaixaExtensa_formaZonaDeFrenagem() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = retas(50);
        curvasBaixas(nos, 10);
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        for (No no : nos) {
            assertTrue(controle.isNoZonaFrenagem(no),
                    "todos os nós (reta + cluster de curva baixa) deveriam estar na zona de frenagem");
        }
    }

    @Test
    void clusterDeCurvaBaixaCurtoEIsolado_naoFormaZonaDeFrenagem() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = retas(50);
        curvasBaixas(nos, 3);
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        for (No no : nos) {
            assertFalse(controle.isNoZonaFrenagem(no),
                    "cluster de curva baixa curto demais não deveria formar zona de frenagem");
        }
    }

    /**
     * Uma única curva alta longa e sinuosa (dezenas/centenas de nós, como o
     * complexo da curva 1 de Albert Park) é só UM trecho de curva alta — o
     * limite é sobre a quantidade de trechos (esses/chicanes), não sobre a
     * quantidade de nós. Regressão: antes dessa correção, uma curva alta
     * longa era rejeitada por exceder uma contagem de nós, mesmo sendo uma
     * única entrada de curva antes da frenagem forte.
     */
    @Test
    void umUnicoTrechoLongoDeCurvaAlta_aindaFormaZonaDeFrenagem() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = retas(50);
        for (int i = 0; i < 123; i++) {
            nos.add(criarNo(nos.size(), No.CURVA_ALTA));
        }
        curvasBaixas(nos, 10);
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        boolean algumNaZona = false;
        for (No no : nos) {
            if (controle.isNoZonaFrenagem(no)) {
                algumNaZona = true;
                break;
            }
        }
        assertTrue(algumNaZona,
                "um único trecho longo de curva alta (mesmo com muitos nós) ainda deveria formar zona de frenagem");
    }

    /**
     * Vários trechos SEPARADOS de curva alta (intercalados com reta) antes
     * da curva baixa é o padrão real de uma sequência de esses/chicane, e
     * não deveria formar zona de frenagem.
     */
    @Test
    void variosTrechosSeparadosDeCurvaAlta_naoFormaZonaDeFrenagem() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = retas(20);
        for (int i = 0; i < 5; i++) {
            nos.add(criarNo(nos.size(), No.CURVA_ALTA));
            nos.add(criarNo(nos.size(), No.RETA));
            nos.add(criarNo(nos.size(), No.RETA));
        }
        curvasBaixas(nos, 10);
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        for (No no : nos) {
            assertFalse(controle.isNoZonaFrenagem(no),
                    "sequência tipo esses/chicane (vários trechos separados de curva alta) não deveria formar zona de frenagem");
        }
    }

    @Test
    void umTrechoDeCurvaAlta_aindaFormaZonaDeFrenagem() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = retas(20);
        nos.add(criarNo(nos.size(), No.CURVA_ALTA));
        curvasBaixas(nos, 10);
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        boolean algumNaZona = false;
        for (No no : nos) {
            if (controle.isNoZonaFrenagem(no)) {
                algumNaZona = true;
                break;
            }
        }
        assertTrue(algumNaZona, "um único trecho de curva alta (dentro do limite) ainda deveria formar zona de frenagem");
    }

    /**
     * A pista é uma volta fechada: quando a curva fechada vem logo depois da
     * largada (poucos nós de reta entre o índice 0 e o cluster), o resto da
     * reta de frenagem está no FIM do array (a reta que antecede a linha de
     * largada/chegada, fisicamente contígua a ela dando a volta completa).
     * A varredura precisa ser circular (voltar do índice 0 pro fim do
     * array) pra contar esse trecho — sem isso, a zona ficaria bem menor
     * (só os poucos nós entre a largada e a curva).
     */
    @Test
    void curvaLogoAposALargada_contaRetaDoFimDoArrayViaWraparound() throws Exception {
        ControleJogoLocal controle = criarControle();
        List<No> nos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            nos.add(criarNo(nos.size(), No.LARGADA)); // 0-4
        }
        for (int i = 0; i < 5; i++) {
            nos.add(criarNo(nos.size(), No.RETA)); // 5-9
        }
        curvasBaixas(nos, 10); // 10-19: primeira curva, logo apos a largada
        for (int i = 0; i < 16; i++) {
            nos.add(criarNo(nos.size(), No.RETA)); // 20-35: fim da reta dos boxes/largada, antes de fechar a volta
        }
        controle.nosDaPista = nos;

        controle.calculaZonaFrenagem();

        No noNoFimDoArray = nos.get(30);
        assertTrue(controle.isNoZonaFrenagem(noNoFimDoArray),
                "a reta do fim do array (antes de fechar a volta pro índice 0) deveria contar pra zona de frenagem "
                        + "da curva logo após a largada, via wraparound circular");
    }

    /**
     * Regressão: a primeira curva de Albert Park (um trecho longo e sinuoso
     * de curva alta — 123 nós — seguido de um cluster de curva baixa) não
     * era detectada como zona de frenagem antes desta correção, porque o
     * limite antigo contava nós de curva alta em vez de trechos.
     */
    @Test
    void albertPark_primeiraCurvaAposALargada_formaZonaDeFrenagem() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito("albert_park_mro.xml");
        circuito.vetorizarPista();
        List<No> pista = circuito.getPistaFull();

        // Sequência real (medida em diagnóstico): LARGADA[0-256] RETA[257-1329]
        // ALTA[1330-1452:123 nós, um único trecho sinuoso] BAIXA[1453-1874] —
        // a primeira curva fechada do circuito, logo após a reta da largada.
        No primeiroNoDaPrimeiraCurvaBaixa = pista.get(1453);
        assertTrue(primeiroNoDaPrimeiraCurvaBaixa.verificaCurvaBaixa(),
                "pré-condição do teste: index 1453 deveria ser o início do cluster de curva baixa da primeira curva "
                        + "(se o XML do circuito mudou, ajustar este índice)");

        java.util.Map<No, Double> zona = ControleRecursos.calculaZonaFrenagem(pista);

        assertTrue(zona.containsKey(primeiroNoDaPrimeiraCurvaBaixa),
                "a primeira curva de Albert Park (curva alta longa de 123 nós + cluster de curva baixa, logo após "
                        + "a reta da largada) deveria formar zona de frenagem");
    }

    /**
     * Posição relativa dentro da zona: 0.0 no nó mais distante da curva
     * (início da zona) até 1.0 no último nó do cluster de curva baixa
     * (final da zona), crescendo ao longo do percurso.
     */
    @Test
    void posicaoNaZona_cresceDoInicioAoFinal() throws Exception {
        List<No> nos = retas(50);
        curvasBaixas(nos, 10);

        Map<No, Double> zona = ControleRecursos.calculaZonaFrenagem(nos);

        No primeiraRetaDaZona = nos.get(0); // mais distante do cluster: início da zona
        No ultimoNoDoCluster = nos.get(nos.size() - 1); // fim do cluster: final da zona

        assertEquals(0.0, zona.get(primeiraRetaDaZona), 0.0001,
                "o nó mais distante da curva deveria estar na posição 0.0 (início da zona)");
        assertEquals(1.0, zona.get(ultimoNoDoCluster), 0.0001,
                "o último nó do cluster de curva baixa deveria estar na posição 1.0 (final da zona)");

        double posicaoAnterior = -1.0;
        for (No no : nos) {
            double posicaoAtual = zona.get(no);
            assertTrue(posicaoAtual >= posicaoAnterior,
                    "a posição relativa deveria crescer (ou manter) ao longo do percurso da zona");
            posicaoAnterior = posicaoAtual;
        }
    }
}
