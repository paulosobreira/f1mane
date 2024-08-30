package br.f1mane.controles;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.*;

import br.nnpe.Global;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.Piloto;

/**
 * @author Paulo Sobreira
 */
public class ControleQualificacao {

    private final InterfaceJogo controleJogo;
    private final ControleBox controleBox;
    private final ControleCorrida controleCorrida;
    private boolean modoQualify = false;

    /**
     * @param controleJogo
     */
    public ControleQualificacao(InterfaceJogo controleJogo,
                                ControleBox controleBox, ControleCorrida controleCorrida) {
        super();
        this.controleJogo = controleJogo;
        this.controleBox = controleBox;
        this.controleCorrida = controleCorrida;
    }

    private void gerarQualificacaoAleatoria() {
        List<Piloto> pilotos = controleJogo.getPilotos();
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto piloto = pilotos.get(i);
            controleBox.setupCorridaQualificacao(piloto);
        }
    }

    public void gerarGridLargada() {
        modoQualify = true;
        gerarQualificacaoAleatoria();
        gerarVoltaQualificacaoAleatoria();
        posicionarCarrosLargada();
        modoQualify = false;
    }

    private void gerarVoltaQualificacaoAleatoria() {
        int position = controleJogo.getNosDaPista().size() - 1;
        No noLargada = (No) controleJogo.getNosDaPista().get(position);
        List<Piloto> pilotos = controleJogo.getPilotos();
        double consumoMedioPneu = 0;
        double consumoMedioCombustivel = 0;
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto piloto = pilotos.get(i);
            piloto.setNoAtual(noLargada);
            int contCiclosQualificacao = 0;
            int pneus = piloto.getCarro().getPneus();
            int combustivel = piloto.getCarro().getCombustivel();
            while ((Double.valueOf(piloto.getPtosPista()).doubleValue() / Double
                    .valueOf(controleJogo.getNosDaPista().size()).doubleValue()) <= 1) {
                piloto.processarCiclo(controleJogo);
                contCiclosQualificacao++;
            }
            consumoMedioPneu += (pneus - piloto.getCarro().getPneus());
            consumoMedioCombustivel += (combustivel - piloto.getCarro().getCombustivel());
            piloto.setCiclosVoltaQualificacao(Util.inteiro(
                    contCiclosQualificacao * controleJogo.tempoCicloCircuito()));
            piloto.setNumeroVolta(-1);
            piloto.setUltimaVolta(null);
            piloto.setVoltaAtual(null);
            piloto.setTravouRodas(0);
            piloto.setVoltas(new ArrayList());
            controleJogo.zerarMelhorVolta();
        }
        consumoMedioPneu = consumoMedioPneu / pilotos.size();
        consumoMedioCombustivel /= pilotos.size();
        Logger.logar("consumoMedioPneu " + consumoMedioPneu);
        Logger.logar("consumoMedioCombustivel " + consumoMedioCombustivel);
        Logger.logar("Consumo estimado Combustivel " + consumoMedioCombustivel * controleJogo.totalVoltasCorrida());
        Logger.logar("Tanque Cheio " + (controleCorrida.getTanqueCheio() * 0.9));
        Logger.logar("setFatorConsumoCombustivelSemReabastecimento " + (controleCorrida.getTanqueCheio()) / (consumoMedioCombustivel * controleJogo.totalVoltasCorrida()));
        Logger.logar("60% controleCorrida.getDistaciaCorrida() " + (controleCorrida.getDistaciaCorrida() * 0.6));
        Logger.logar("Consumo estimado Pneu " + (consumoMedioPneu * controleJogo.totalVoltasCorrida()));
        Logger.logar("setFatorConsumoPneuSemTroca " + ((controleCorrida.getDistaciaCorrida() * 0.6) / (consumoMedioPneu * controleJogo.totalVoltasCorrida())));
        controleCorrida.setFatorConsumoPneuSemTroca((controleCorrida.getDistaciaCorrida() * 0.6) / (consumoMedioPneu * controleJogo.totalVoltasCorrida()));
        controleCorrida.setFatorConsumoCombustivelSemReabastecimento((controleCorrida.getTanqueCheio()) / (consumoMedioCombustivel * controleJogo.totalVoltasCorrida()));
        nivelaHabilidade(pilotos);
        nivelaCarro(pilotos);
        Collections.sort(pilotos, new ComparatorVoltaQualyAleatoria());

        for (int i = 0; i < pilotos.size(); i++) {
            if (i == 0) {
                continue;
            }
            Piloto pilotoAnt = pilotos.get(i - 1);
            Piloto piloto = pilotos.get(i);
            piloto.setCiclosVoltaQualificacao(
                    pilotoAnt.getCiclosVoltaQualificacao()
                            + (i < (pilotos.size() / 2)
                            ? Util.intervalo(0, 150)
                            : Util.intervalo(0, 750)));
        }
    }

    private void nivelaHabilidade(List<Piloto> pilotos) {
        int valor = 0;
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            piloto.setHabilidadeAntesQualify(piloto.getHabilidade());
            valor += piloto.getHabilidade();
        }
        valor = valor / pilotos.size();

        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            int diff;
            if (piloto.getHabilidade() > valor) {
                diff = piloto.getHabilidade() - valor;
                piloto.setHabilidade(piloto.getHabilidade()
                        - Util.intervalo(diff / 2, diff));
            } else {
                diff = valor - piloto.getHabilidade();
                piloto.setHabilidade(piloto.getHabilidade()
                        + Util.intervalo(diff / 2, diff));
            }

        }
        Logger.logar(
                "-----------------=====nivelaHabilidade=====----------------");
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            Logger.logar(piloto.toString() + " HabilidadeAntesQualify : "
                    + piloto.getHabilidadeAntesQualify() + " Habilidade: "
                    + piloto.getHabilidade());
        }
    }

    private void nivelaCarro(List<Piloto> pilotos) {
        int valorPotencia = 0;
        int valorAero = 0;
        int valorFreios = 0;
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            piloto.getCarro()
                    .setPotenciaAntesQualify(piloto.getCarro().getPotencia());
            piloto.getCarro()
                    .setAeroAntesQualify(piloto.getCarro().getAerodinamica());
            piloto.getCarro()
                    .setFreiosAntesQualify(piloto.getCarro().getFreios());
            valorPotencia += piloto.getCarro().getPotencia();
            valorAero += piloto.getCarro().getAerodinamica();
            valorFreios += piloto.getCarro().getFreios();
        }
        valorPotencia = valorPotencia / pilotos.size();
        valorAero = valorAero / pilotos.size();
        valorFreios = valorFreios / pilotos.size();
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            int diffPotencia;
            if (piloto.getCarro().getPotencia() > valorPotencia) {
                diffPotencia = piloto.getCarro().getPotencia() - valorPotencia;
                piloto.getCarro().setPotencia(piloto.getCarro().getPotencia()
                        - Util.intervalo(diffPotencia / 2, diffPotencia));
            } else {
                diffPotencia = valorPotencia - piloto.getCarro().getPotencia();
                piloto.getCarro().setPotencia(piloto.getCarro().getPotencia()
                        + Util.intervalo(diffPotencia / 2, diffPotencia));
            }
            int diffAero;
            if (piloto.getCarro().getPotencia() > valorAero) {
                diffAero = piloto.getCarro().getAerodinamica() - valorAero;
                piloto.getCarro().setAerodinamica(piloto.getCarro().getAerodinamica()
                        - Util.intervalo(diffAero / 2, diffAero));
            } else {
                diffAero = valorAero - piloto.getCarro().getAerodinamica();
                piloto.getCarro().setAerodinamica(piloto.getCarro().getAerodinamica()
                        + Util.intervalo(diffAero / 2, diffAero));
            }
            int diffFreios;
            if (piloto.getCarro().getFreios() > valorFreios) {
                diffFreios = piloto.getCarro().getFreios() - valorFreios;
                piloto.getCarro().setFreios(piloto.getCarro().getFreios()
                        - Util.intervalo(diffFreios / 2, diffFreios));
            } else {
                diffFreios = valorFreios - piloto.getCarro().getFreios();
                piloto.getCarro().setFreios(piloto.getCarro().getFreios()
                        + Util.intervalo(diffFreios / 2, diffFreios));
            }
        }
        Logger.logar(
                "-----------------=====nivelaPontecia=====----------------");
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            Logger.logar(piloto.toString() + " getPotenciaAntesQualify : "
                    + piloto.getCarro().getPotenciaAntesQualify()
                    + " getPotencia: " + piloto.getCarro().getPotencia());
        }
    }

    public boolean isModoQualify() {
        return modoQualify;
    }

    public void posicionarCarrosLargada() {
        Circuito circuito = controleJogo.getCircuito();
        List<Piloto> pilotos = controleJogo.getPilotos();
        int iFim = 50
                + Util.inteiro((Carro.LARGURA * .8) * (pilotos.size() - 1));
        No noFim = (No) circuito.getPistaFull()
                .get(circuito.getPistaFull().size() - iFim);
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto piloto = (Piloto) pilotos.get(i);
            int iP = 50 + Util.inteiro((Carro.LARGURA * .8) * i);
            No n1 = (No) circuito.getPistaFull().get(
                    circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
            No nM = (No) circuito.getPistaFull()
                    .get(circuito.getPistaFull().size() - iP);
            No n2 = (No) circuito.getPistaFull().get(
                    circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
            Point p1 = new Point(Util.inteiro(n1.getPoint().x),
                    Util.inteiro(n1.getPoint().y));
            Point pm = new Point(Util.inteiro(nM.getPoint().x),
                    Util.inteiro(nM.getPoint().y));
            Point p2 = new Point(Util.inteiro(n2.getPoint().x),
                    Util.inteiro(n2.getPoint().y));

            double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
            Rectangle2D rectangle = new Rectangle2D.Double(
                    (pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
                    (Carro.LARGURA), (Carro.ALTURA));

            Point cima = GeoUtil.calculaPonto(calculaAngulo,
                    Util.inteiro(Carro.ALTURA * 1.2),
                    new Point(Util.inteiro(rectangle.getCenterX()),
                            Util.inteiro(rectangle.getCenterY())));
            Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180,
                    Util.inteiro(Carro.ALTURA * 1.2),
                    new Point(Util.inteiro(rectangle.getCenterX()),
                            Util.inteiro(rectangle.getCenterY())));
            if (i % 2 == 0) {
                rectangle = new Rectangle2D.Double(
                        (cima.x - (Carro.MEIA_LARGURA)),
                        (cima.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
                        (Carro.ALTURA));
                piloto.setTracado(2);
            } else {
                rectangle = new Rectangle2D.Double(
                        (baixo.x - (Carro.MEIA_LARGURA)),
                        (baixo.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
                        (Carro.ALTURA));
                piloto.setTracado(1);
            }
            piloto.setIndiceTracado(0);
            piloto.setNoAtual(nM);
            piloto.setPosicao(i + 1);
            piloto.setPosicaoInicial(piloto.getPosicao());
            piloto.zerarGanhoEVariaveisUlt();
            piloto.setPtosPista(nM.getIndex() - noFim.getIndex());
            if (!piloto.isJogadorHumano()
                    && !piloto.testeHabilidadePilotoCarro()
                    && Math.random() > 0.95) {
                piloto.setCiclosDesconcentrado(Util.intervalo(500, 700));
                piloto.setProblemaLargada(true);
            }
            Carro carro = piloto.getCarro();
            carro.setTempMax(carro.getPotencia() / 4);
            carro.setDurabilidadeAereofolio(
                    Global.DURABILIDADE_AREOFOLIO);
            piloto.calculaCarrosAdjacentes(controleJogo);
            Logger.logar(" Posição Largada :" + piloto.getPosicao() + " Nome : "
                    + piloto.getNome() + " Pneu : "
                    + piloto.getCarro().getTipoPneu() + " Combustivel : "
                    + piloto.getCarro().getPorcentagemCombustivel() + " Asa : "
                    + piloto.getCarro().getAsa() + " Tempo Qualificação : "
                    + ControleEstatisticas.formatarTempo(
                    Long.valueOf(piloto.getCiclosVoltaQualificacao())));
        }

    }

    private static class ComparatorVoltaQualyAleatoria implements Comparator {
        public int compare(Object arg0, Object arg1) {
            Piloto piloto0 = (Piloto) arg0;
            Piloto piloto1 = (Piloto) arg1;
            return new Long(piloto0.getCiclosVoltaQualificacao()).compareTo(
                    new Long(piloto1.getCiclosVoltaQualificacao()));
        }
    }
}
