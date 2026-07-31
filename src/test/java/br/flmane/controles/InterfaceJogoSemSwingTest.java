package br.flmane.controles;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * O contrato usado pelo servidor headless não pode mencionar Swing nem
 * {@code br.flmane.visao}: enquanto mencionava, bastava o servidor resolver um
 * método para o verificador de bytecode carregar {@code MainFrame},
 * {@code PainelTabelaResultadoFinal} e a árvore Swing por trás deles, num
 * processo que nunca desenha.
 * <p>
 * {@code java.awt.image.BufferedImage} é aceito de propósito — é Java2D puro,
 * gerado e servido em modo headless sem componente de UI.
 */
class InterfaceJogoSemSwingTest {

    @Test
    void contratoDeCorridaNaoMencionaTiposGraficos() {
        List<String> ofensores = new ArrayList<>();

        for (Method metodo : InterfaceJogo.class.getDeclaredMethods()) {
            verificar(metodo.getReturnType(), metodo, ofensores);
            for (Class<?> parametro : metodo.getParameterTypes()) {
                verificar(parametro, metodo, ofensores);
            }
        }

        assertTrue(ofensores.isEmpty(),
                "InterfaceJogo deve ficar livre de Swing/br.flmane.visao: " + ofensores);
    }

    /**
     * A contraparte: os pontos gráficos continuam existindo, só que no contrato
     * separado — se alguém "resolver" o teste acima apagando funcionalidade em
     * vez de movê-la, este teste falha.
     */
    @Test
    void contratoVisualMantemOsPontosGraficos() {
        List<String> nomes = new ArrayList<>();
        for (Method metodo : InterfaceJogoVisual.class.getDeclaredMethods()) {
            nomes.add(metodo.getName());
        }

        assertTrue(nomes.containsAll(List.of(
                        "getMainFrame", "setMainFrame", "obterResultadoFinal",
                        "painelNarracao", "painelDebug")),
                "InterfaceJogoVisual deve declarar os pontos gráficos: " + nomes);
    }

    private static void verificar(Class<?> tipo, Method metodo, List<String> ofensores) {
        String nome = tipo.getName();
        boolean grafico = nome.startsWith("javax.swing.")
                || nome.startsWith("br.flmane.visao.")
                || nome.equals("br.flmane.MainFrame")
                || (nome.startsWith("java.awt.") && !nome.equals("java.awt.image.BufferedImage"));
        if (grafico) {
            ofensores.add(metodo.getName() + " -> " + nome);
        }
    }
}
