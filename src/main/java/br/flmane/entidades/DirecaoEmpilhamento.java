package br.flmane.entidades;

/**
 * Direção em que cada repetição extra de um {@link ObjetoConstrucao}
 * empilhado ({@code quantidadeEmpilhamento > 1}) é deslocada em relação à
 * repetição anterior. Cada valor guarda um vetor unitário {@code (dx, dy)},
 * multiplicado por {@code grauEmpilhamento} (pixels) para obter o
 * deslocamento efetivo de cada repetição — deslocamento cumulativo, não
 * proporcional a largura/altura da forma. Y positivo aponta para baixo
 * (mesma convenção do restante do desenho em {@code Graphics2D}).
 */
public enum DirecaoEmpilhamento {
    CIMA(0, -1),
    BAIXO(0, 1),
    ESQUERDA(-1, 0),
    DIREITA(1, 0),
    CIMA_ESQUERDA(-0.70710678, -0.70710678),
    CIMA_DIREITA(0.70710678, -0.70710678),
    BAIXO_ESQUERDA(-0.70710678, 0.70710678),
    BAIXO_DIREITA(0.70710678, 0.70710678);

    private final double dx;
    private final double dy;

    DirecaoEmpilhamento(double dx, double dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }
}
