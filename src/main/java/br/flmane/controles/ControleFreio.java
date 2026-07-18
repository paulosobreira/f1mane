package br.flmane.controles;

import java.util.HashMap;
import java.util.Map;

import br.flmane.entidades.No;
import br.flmane.entidades.Piloto;
import br.flmane.recursos.idiomas.Lang;
import br.nnpe.Html;

/**
 * Frenagem antes de uma curva baixa (zona de frenagem detectada) — reduz
 * ganho gradualmente, e pode disparar travada de roda ou freada mal-sucedida
 * sob pressão. Extraído de {@code Piloto}.
 *
 * @author Paulo Sobreira
 */
public class ControleFreio {
    private final InterfaceJogo controleJogo;

    /** Verdadeiro a partir do primeiro tick "atrasado" dentro do evento de frenagem atual — só uso interno desta classe. */
    private final Map<Piloto, Boolean> retardaFreiandoRetaPorPiloto = new HashMap<>();
    /** Trava o sorteio de freada mal-sucedida a uma vez por evento de frenagem — só uso interno desta classe. */
    private final Map<Piloto, Boolean> freioNaRetaAvaliadoNesteEventoPorPiloto = new HashMap<>();

    public ControleFreio(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

    public void processaFreioNaReta(Piloto piloto) {
        if (piloto.isRecebeuBanderada()) {
            return;
        }
        boolean testPilotoPneus = piloto.getCarro().testeFreios();
        /**
         * efeito freiar na reta
         */
        No obterProxCurva = controleJogo.obterProxCurva(piloto.getNoAtual());
        if (obterProxCurva != null && obterProxCurva.verificaCurvaBaixa()
                && controleJogo.isNoZonaFrenagem(piloto.getNoAtual()) && piloto.getNoAtual().verificaRetaOuLargada()) {
            int indexProxCurva = obterProxCurva.getIndex();
            if (indexProxCurva < piloto.getNoAtual().getIndex()) {
                indexProxCurva += controleJogo.getNosDaPista().size();
            }
            double val = indexProxCurva - piloto.getNoAtual().getIndex();
            double distAfrente = 300.0;
            piloto.setFreiandoReta(true);
            double multi = (val / distAfrente);

            boolean retardaFreiandoReta = retardaFreiandoRetaPorPiloto.getOrDefault(piloto, false);

            if (testPilotoPneus) {
                retardaFreiandoReta = true;
            }

            if (!retardaFreiandoReta && Piloto.AGRESSIVO.equals(piloto.getModoPilotagemEfetivo())) {
                retardaFreiandoReta = true;
            }

            double minMulti = 0.7;
            minMulti -= controleJogo.getMolhado() * 0.3;
            if (controleJogo.isChovendo()) {
                retardaFreiandoReta = false;
            }
            if (piloto.getDiffParaProximoRetardatario() < 50) {
                minMulti -= controleJogo.getRandom().intervalo(0.05, 0.15);
                retardaFreiandoReta = false;
            } else if (piloto.getDiferencaParaProximoRetardatario() < 100) {
                minMulti -= 0.1;
                retardaFreiandoReta = false;
            } else if (piloto.getDiferencaParaProximoRetardatario() < 150) {
                minMulti -= controleJogo.getRandom().intervalo(0.05, 0.1);
                retardaFreiandoReta = false;
            }
            if (retardaFreiandoReta) {
                if (piloto.getStress() > 50 && Piloto.AGRESSIVO.equals(piloto.getModoPilotagemEfetivo())) {
                    controleJogo.travouRodas(piloto);
                }
                minMulti += (testPilotoPneus) ? 0.2 : 0.1;
            }
            if (multi < minMulti)
                multi = minMulti;
            piloto.setGanho(piloto.getGanho() * multi);

            boolean freioNaRetaAvaliadoNesteEvento = freioNaRetaAvaliadoNesteEventoPorPiloto.getOrDefault(piloto,
                    false);
            if (retardaFreiandoReta && !freioNaRetaAvaliadoNesteEvento) {
                freioNaRetaAvaliadoNesteEventoPorPiloto.put(piloto, true);
                if (controleJogo.getRandom().nextDouble() > 0.9 && !piloto.testeHabilidadePilotoFreios()) {
                    piloto.setFreioNaRetaMalSucedidoNesteTick(30 - (piloto.getCarro().getPorcentagemDesgastePneus() / 100));
                    if (controleJogo.verificaInfoRelevante(piloto) && controleJogo.getRandom().nextDouble() > 0.7) {
                        controleJogo.info(Lang.msg("014",
                                new String[] { piloto.nomeJogadorFormatado(), Html.negrito(piloto.getNome()) }));
                    }
                }
            }

            retardaFreiandoRetaPorPiloto.put(piloto, retardaFreiandoReta);
        } else {
            piloto.setFreiandoReta(false);
            retardaFreiandoRetaPorPiloto.put(piloto, false);
            freioNaRetaAvaliadoNesteEventoPorPiloto.put(piloto, false);
        }
    }
}
