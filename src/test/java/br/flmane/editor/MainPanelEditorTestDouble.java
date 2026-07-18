package br.flmane.editor;

/**
 * Double de {@link MainPanelEditor} para testes que disparam MouseEvent de
 * verdade no editor: sobrescreve o alerta de ponto de escapada inválido para
 * apenas registrar a chamada, em vez de abrir um JOptionPane real (que trava
 * a suíte esperando interação, já que os testes rodam headless).
 *
 * Nenhum teste deve instanciar javax.swing.JOptionPane/JFrame/JDialog reais
 * nem chamar métodos que os exibam — sempre passar por um double como este.
 */
class MainPanelEditorTestDouble extends MainPanelEditor {

    private int alertasPontoEscapadaInvalido = 0;
    private int alertasEscapadaIncompleta = 0;
    private int alertasDistanciaNaoInformada = 0;

    @Override
    protected void alertaPontoEscapadaInvalido() {
        alertasPontoEscapadaInvalido++;
    }

    int getAlertasPontoEscapadaInvalido() {
        return alertasPontoEscapadaInvalido;
    }

    @Override
    protected void alertaEscapadaIncompleta() {
        alertasEscapadaIncompleta++;
    }

    int getAlertasEscapadaIncompleta() {
        return alertasEscapadaIncompleta;
    }

    @Override
    protected void alertaDistanciaNaoInformada() {
        alertasDistanciaNaoInformada++;
    }

    int getAlertasDistanciaNaoInformada() {
        return alertasDistanciaNaoInformada;
    }
}
