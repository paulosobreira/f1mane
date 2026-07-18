package br.flmane.controles;

import br.flmane.entidades.Carro;
import br.flmane.entidades.No;
import br.flmane.entidades.Piloto;
import br.nnpe.Global;

/**
 * Uso de DRS pelo piloto durante a corrida — ligar/desligar a asa
 * ({@link Carro#setAsa}) e decidir se a zona de DRS está disponível
 * ({@link #verificaPodeUsarDRS}). Extraído de {@code Piloto}.
 *
 * @author Paulo Sobreira
 */
public class ControleDrs {
    private final InterfaceJogo controleJogo;

    public ControleDrs(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

    public void processaUsoDRS(Piloto piloto) {
        if (!controleJogo.isDrs()) {
            return;
        }
        if (piloto.isBox() || piloto.getPtosBox() != 0) {
            /**
             * Sem DRS a caminho do box nem dentro dele — desde a decisão
             * (isBox()), não só fisicamente na pit lane (getPtosBox() != 0).
             * Sem isso, o indicador de DRS na tela continuava piscando
             * (Piloto.isPodeUsarDRS()) até o piloto entrar fisicamente na
             * pit lane, mesmo já a caminho do box.
             */
            piloto.getCarro().setAsa(Carro.MAIS_ASA);
            piloto.setAtivarDRS(false);
            piloto.setPodeUsarDRS(false);
            return;
        }
        if (verificaPodeUsarDRS(piloto) && piloto.isAtivarDRS()) {
            piloto.getCarro().setAsa(Carro.MENOS_ASA);
        }
        if (!piloto.getNoAtual().verificaRetaOuLargada()) {
            piloto.getCarro().setAsa(Carro.MAIS_ASA);
            piloto.setAtivarDRS(false);
            piloto.setPodeUsarDRS(false);
        }
    }

    private boolean verificaPodeUsarDRS(Piloto piloto) {
        if (piloto.isBox() || piloto.getPtosBox() != 0) {
            piloto.setPodeUsarDRS(false);
            return false;
        }
        if (controleJogo.isDrs() && piloto.getNumeroVolta() > 1 && !controleJogo.isSafetyCarNaPista()
                && !controleJogo.isChovendo() && !controleJogo.isCorridaTerminada()
                && piloto.getCarroPilotoDaFrenteRetardatario() != null && piloto.getNoAtual().verificaRetaOuLargada()
                && piloto.getDiffParaProximoRetardatario() < Global.LIMITE_DRS) {
            No obterCurvaAnterior = controleJogo.obterCurvaAnterior(piloto.getNoAtual());
            No obterProxCurva = controleJogo.obterProxCurva(piloto.getNoAtual());
            if (obterCurvaAnterior == null || obterProxCurva == null) {
                return false;
            }
            int indexProxCurva = obterProxCurva.getIndex();
            int indexCurvaAnterior = obterCurvaAnterior.getIndex();
            if (indexProxCurva < indexCurvaAnterior) {
                indexProxCurva += controleJogo.getNosDaPista().size();
            }
            if ((indexProxCurva - indexCurvaAnterior) >= Global.TAMANHO_RETA_DRS) {
                if (piloto.getNoAtual().getIndex() > indexCurvaAnterior
                        && piloto.getNoAtual().getIndex() < indexCurvaAnterior + 40) {
                    piloto.setPodeUsarDRS(true);
                }
            }
        }
        return piloto.isPodeUsarDRS();
    }
}
