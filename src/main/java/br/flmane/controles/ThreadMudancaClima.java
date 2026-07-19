package br.flmane.controles;

import br.nnpe.Global;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.flmane.entidades.Clima;

/**
 * @author Paulo Sobreira
 */
public class ThreadMudancaClima extends Thread {
    private final ControleClima controleClima;
    private boolean processada;

    public boolean isProcessada() {
        return processada;
    }

    public void setProcessada(boolean processada) {
        this.processada = processada;
    }

    /**
     * @param controleClima
     */
    public ThreadMudancaClima(ControleClima controleClima) {
        super();
        this.controleClima = controleClima;
    }

    public void run() {
        try {
            int atrasoMs = controleClima.getControleJogo().getRandom()
                    .intervalo(0, (int) Global.ATRASO_MAX_MUDANCA_CLIMA_MS);
            Logger.logar("[ThreadMudancaClima] Disparada, dormindo " + atrasoMs
                    + "ms antes de efetivar a mudanca de clima");
            sleep(atrasoMs);
            String climaAntes = controleClima.getClima();
            if (Clima.SOL.equals(climaAntes) || Clima.CHUVA.equals(climaAntes)) {
                controleClima.intervaloNublado();
            } else if (Clima.NUBLADO.equals(climaAntes)) {
                if (controleClima.verificaPossibilidadeChoverNaPista()) {
                    controleClima.intervaloChuva();
                } else {
                    controleClima.intervaloSol();
                }
            }
            String climaDepois = controleClima.getClima();
            if (java.util.Objects.equals(climaAntes, climaDepois)) {
                Logger.logar("[ThreadMudancaClima] MUDOU: clima permaneceu " + climaDepois
                        + " (nenhum dos ramos de transicao se aplicou)");
            } else {
                Logger.logar("[ThreadMudancaClima] MUDOU: clima de " + climaAntes + " para " + climaDepois);
            }
            controleClima.informaMudancaClima();
        } catch (Exception e) {
            Logger.logar("[ThreadMudancaClima] FALHOU ao tentar mudar o clima: " + e);
            Logger.logarExept(e);
        } finally {
            // Sempre marca como processada, sucesso ou falha — senão uma única exceção
            // (ex.: InterruptedException) trava ControleClima.processaPossivelMudancaClima()
            // em "ADIADA" pra sempre, já que ele só cria uma nova thread quando a anterior
            // termina de processar.
            processada = true;
        }
    }

}
