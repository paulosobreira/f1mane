package br.flmane.entidades;

/**
 * Classifica a área desenhada por um {@link ObjetoLivre}, definindo qual
 * padrão de preenchimento procedural é usado dentro da forma (além do
 * preenchimento sólido padrão de {@link #POLIGONO_SIMPLES}).
 */
public enum TipoObjetoLivre {
    POLIGONO_SIMPLES,
    VEGETACAO_SIMPLES,
    VEGETACAO_DENSA,
    AGUA,
    BRITA,
    LISTRADO,
    XADREZ
}
