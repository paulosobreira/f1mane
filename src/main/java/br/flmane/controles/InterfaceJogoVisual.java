package br.flmane.controles;

import br.flmane.MainFrame;
import br.flmane.visao.PainelTabelaResultadoFinal;

import javax.swing.JPanel;

/**
 * Parte gráfica do contrato de jogo, separada de {@link InterfaceJogo} para que
 * o caminho de corrida do servidor headless não precise resolver — e portanto
 * carregar — as classes Swing e de {@code br.flmane.visao}.
 * <p>
 * Enquanto esses métodos moravam em {@link InterfaceJogo}, bastava o servidor
 * usar o contrato para o verificador de bytecode carregar {@code MainFrame},
 * {@code PainelTabelaResultadoFinal} e a árvore Swing por trás deles, num
 * processo que nunca desenha nada — o desenho acontece no navegador do cliente.
 * <p>
 * Implementado pelos modos com interface gráfica ({@link ControleJogoLocal} e
 * o cliente Java do multiplayer) e consumido por {@code MainFrame},
 * {@code MainFrameSimulacao} e pelo applet.
 */
public interface InterfaceJogoVisual extends InterfaceJogo {

    MainFrame getMainFrame();

    void setMainFrame(MainFrame mainFrame);

    PainelTabelaResultadoFinal obterResultadoFinal();

    JPanel painelNarracao();

    JPanel painelDebug();
}
