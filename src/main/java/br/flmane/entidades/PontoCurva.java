package br.flmane.entidades;

import java.awt.Point;
import java.io.Serializable;

/**
 * Um vértice de {@link ObjetoLivre} com uma haste de curvatura simétrica: uma
 * ponta ({@code hasteFim}) controla os dois segmentos adjacentes ao vértice
 * (a ponta oposta, usada pelo segmento que chega, é o espelho matemático de
 * {@code hasteFim} em torno de {@code posicao} — nunca persistida). Quando
 * {@code hasteFim} é {@code null} ou coincide com {@code posicao}, os dois
 * segmentos adjacentes são retos, exatamente como o polígono usado antes da
 * introdução das curvas.
 */
public class PontoCurva implements Serializable {

    private static final long serialVersionUID = 1L;

    private Point posicao;
    private Point hasteFim;

    public PontoCurva() {
    }

    public PontoCurva(Point posicao) {
        this.posicao = posicao;
    }

    public Point getPosicao() {
        return posicao;
    }

    public void setPosicao(Point posicao) {
        this.posicao = posicao;
    }

    public Point getHasteFim() {
        return hasteFim;
    }

    public void setHasteFim(Point hasteFim) {
        this.hasteFim = hasteFim;
    }

    private boolean temHasteAjustada() {
        return hasteFim != null && !hasteFim.equals(posicao);
    }

    /** Ponto de controle do segmento que sai deste vértice em direção ao próximo. */
    public Point getControleSaida() {
        return temHasteAjustada() ? hasteFim : posicao;
    }

    /** Ponto de controle (espelho de {@code hasteFim}) do segmento que chega neste vértice. */
    public Point getControleEntrada() {
        if (!temHasteAjustada()) {
            return posicao;
        }
        return new Point(2 * posicao.x - hasteFim.x, 2 * posicao.y - hasteFim.y);
    }
}
