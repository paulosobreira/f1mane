package br.flmane.controles;

import br.nnpe.Html;
import br.nnpe.Logger;
import br.flmane.entidades.Piloto;
import br.flmane.entidades.SafetyCar;
import br.flmane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class ThreadRecolhimentoCarro extends Thread {
    private final ControleJogoLocal controleJogo;
    private final Piloto piloto;
    private final SafetyCar safetyCar;
    private final int delayRecolhimento;

    /**
     * @param controleJogo
     * @param piloto
     * @param safetyCar
     */
    public ThreadRecolhimentoCarro(ControleJogoLocal controleJogo,
                                   Piloto piloto, SafetyCar safetyCar) {
        super();
        this.controleJogo = controleJogo;
        this.piloto = piloto;
        this.safetyCar = safetyCar;
        delayRecolhimento = controleJogo.totalVoltasCorrida() * 7000
                + ((int) (controleJogo.getRandom().nextDouble() * 50000));
    }

    public void run() {
        try {
            sleep(delayRecolhimento);
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        int index = safetyCar.getNoAtual().getIndex();
        int size = controleJogo.getNosDaPista().size();
        double div = ((double) index) / ((double) size);
        while (div < 0.3 || div > 0.7) {
            try {
                sleep(500);
            } catch (InterruptedException e) {
            }
            index = safetyCar.getNoAtual().getIndex();
            size = controleJogo.getNosDaPista().size();
            div = ((double) index) / ((double) size);
        }

        piloto.getCarro().setRecolhido(true);
        safetyCar.setVaiProBox(true);
        controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("031",
                new String[]{piloto.getNome()})));

    }
}
