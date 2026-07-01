package br.f1mane.entidades;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.recursos.CarregadorRecursos;

/**
 * Cobre o traçado suave das zonas de escapada (Circuito.gerarEscapeMap):
 * o afastamento lateral deve subir e descer de forma contínua (sem picos
 * nem oscilação dupla — nem mesmo em curvas fechadas, onde a estimativa de
 * curvatura por 3 pontos é ruidosa), e a zona deve começar e terminar rente
 * ao traçado original da pista.
 */
class CircuitoEscapeMapTest {

    private static final String CIRCUITO_COM_ESCAPADAS = "austin_mro.xml";

    /**
     * Reversão de direção acima disso indica um pico/serra real e visível;
     * o ruído inerente de arredondamento para pixel inteiro fica bem abaixo
     * (~1.3px, medido varrendo todas as zonas de escapada de todos os
     * circuitos do repositório).
     */
    private static final double LIMITE_REVERSAO_PX = 3.0;

    @Test
    void vetorizarPista_comObjetosEscapada_naoLancaExcecao() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_COM_ESCAPADAS);

        assertDoesNotThrow(() -> circuito.vetorizarPista());

        assertFalse(circuito.getEscapeMap().isEmpty(),
                "circuito de teste deveria ter pelo menos uma zona de escapada");
    }

    @Test
    void tracadoDeEscapada_afastaESuavementeRetornaAoOriginal_semPicos() throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(CIRCUITO_COM_ESCAPADAS);
        circuito.vetorizarPista();

        Map<PontoEscape, List<No>> escapeMap = circuito.getEscapeMap();
        assertFalse(escapeMap.isEmpty());
        verificaZonasSemPicos(circuito, CIRCUITO_COM_ESCAPADAS);
    }

    /**
     * Varre todos os circuitos empacotados no repositório (não só um), já
     * que o efeito de serra reportado só aparecia em curvas fechadas
     * específicas de alguns traçados — um único circuito de teste não é
     * suficiente para pegar esse tipo de regressão.
     */
    @Test
    void todosOsCircuitosComEscapada_naoTemPicos() throws Exception {
        File dir = new File("src/main/resources/circuitos");
        File[] arquivos = dir.listFiles((d, nome) -> nome.endsWith(".xml"));
        assertTrue(arquivos != null && arquivos.length > 0, "nenhum circuito encontrado em " + dir);

        int circuitosComEscapada = 0;
        for (File arquivo : arquivos) {
            Circuito circuito = CarregadorRecursos.carregarCircuito(arquivo.getName());
            assertDoesNotThrow(() -> circuito.vetorizarPista(), "vetorizarPista falhou em " + arquivo.getName());
            if (circuito.getEscapeMap() == null || circuito.getEscapeMap().isEmpty()) {
                continue;
            }
            circuitosComEscapada++;
            verificaZonasSemPicos(circuito, arquivo.getName());
        }
        assertTrue(circuitosComEscapada > 0, "nenhum circuito com zona de escapada foi encontrado para testar");
    }

    private void verificaZonasSemPicos(Circuito circuito, String nomeCircuito) {
        for (Map.Entry<PontoEscape, List<No>> entrada : circuito.getEscapeMap().entrySet()) {
            List<No> tracado = entrada.getValue();
            List<No> bordaOriginal = entrada.getKey().getPista() == 5
                    ? circuito.getPista1Full()
                    : circuito.getPista2Full();

            List<Double> offsets = new ArrayList<>();
            for (int i = 0; i < tracado.size(); i++) {
                No no = tracado.get(i);
                if (no == null) {
                    continue;
                }
                Point original = bordaOriginal.get(i).getPoint();
                offsets.add(original.distance(no.getPoint()));
            }

            assertFalse(offsets.isEmpty(), nomeCircuito + ": zona de escapada sem nenhum nó preenchido");

            // Começa e termina rente ao traçado original (offset ~0 nas duas pontas)
            assertTrue(offsets.get(0) < 5.0,
                    nomeCircuito + ": primeiro nó da zona deveria começar colado no traçado original, offset="
                            + offsets.get(0));
            assertTrue(offsets.get(offsets.size() - 1) < 5.0,
                    nomeCircuito + ": último nó da zona deveria terminar colado no traçado original, offset="
                            + offsets.get(offsets.size() - 1));

            // Sobe e desce uma única vez (sem oscilação/pico duplo): procura reversões
            // de direção cuja magnitude, nos dois lados, ultrapasse o ruído inerente
            // de arredondamento para pixel inteiro.
            double maiorReversao = 0;
            for (int i = 1; i < offsets.size() - 1; i++) {
                double deltaAntes = offsets.get(i) - offsets.get(i - 1);
                double deltaDepois = offsets.get(i + 1) - offsets.get(i);
                if (Math.signum(deltaAntes) != 0 && Math.signum(deltaDepois) != 0
                        && Math.signum(deltaAntes) != Math.signum(deltaDepois)) {
                    maiorReversao = Math.max(maiorReversao, Math.min(Math.abs(deltaAntes), Math.abs(deltaDepois)));
                }
            }
            assertTrue(maiorReversao < LIMITE_REVERSAO_PX,
                    nomeCircuito
                            + ": traçado de escapada não deveria ter reversões de direção visíveis (pico extra), maior reversão="
                            + maiorReversao + "px");
        }
    }
}
