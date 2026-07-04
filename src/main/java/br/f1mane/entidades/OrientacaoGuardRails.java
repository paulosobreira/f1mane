package br.f1mane.entidades;

/**
 * Orientação de cada marca do padrão de linhas finas de
 * {@link ObjetoGuardRails}: {@link #VERTICAL} desenha barras cruzando o
 * percurso (perpendiculares à direção do encadeamento, como os degraus de
 * uma escada); {@link #HORIZONTAL} desenha traços ao longo do percurso
 * (paralelos à direção do encadeamento, como uma linha tracejada). Em ambos
 * os casos as marcas continuam evenly distribuídas pela extensão total do
 * encadeamento, do início ao fim.
 */
public enum OrientacaoGuardRails {
    VERTICAL,
    HORIZONTAL
}
