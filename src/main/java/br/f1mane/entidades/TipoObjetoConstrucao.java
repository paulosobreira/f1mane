package br.f1mane.entidades;

/**
 * Forma desenhada por um {@link ObjetoConstrucao}. Independente do
 * empilhamento ({@code quantidadeEmpilhamento}/{@code direcaoEmpilhamento}/
 * {@code grauEmpilhamento}), que se aplica igualmente a qualquer um destes
 * tipos — empilhar não é um tipo à parte, é uma propriedade transversal.
 */
public enum TipoObjetoConstrucao {
    QUADRADO,
    REDONDO,
    CAMINHAO,
    BARCO
}
