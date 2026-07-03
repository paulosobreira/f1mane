package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.entidades.ObjetoArquibancada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPneus;
import br.f1mane.entidades.TipoObjetoLivre;

/**
 * Cobre a memória de propriedades por classe: criar um objeto novo do mesmo
 * tipo de um já configurado deve herdar ângulo/tamanho/cores (e o padrão, no
 * caso de ObjetoLivre) em vez dos defaults do construtor; tipos diferentes
 * não interferem entre si; sem histórico, aplicar() não faz nada.
 */
class MemoriaPropriedadesObjetoTest {

    @BeforeEach
    void limparMemoria() {
        MemoriaPropriedadesObjeto.limparParaTeste();
    }

    @Test
    void aplicar_semHistoricoParaAClasse_naoAlteraOObjeto() {
        ObjetoPneus objeto = new ObjetoPneus();
        double anguloOriginal = objeto.getAngulo();
        int larguraOriginal = objeto.getLargura();

        MemoriaPropriedadesObjeto.aplicar(objeto);

        assertEquals(anguloOriginal, objeto.getAngulo());
        assertEquals(larguraOriginal, objeto.getLargura());
    }

    @Test
    void lembrarEAplicar_mesmaClasse_propagaAnguloTamanhoECores() {
        ObjetoPneus configurado = new ObjetoPneus();
        configurado.setAngulo(45);
        configurado.setLargura(77);
        configurado.setAltura(33);
        configurado.setCorPimaria(Color.BLUE);
        configurado.setCorSecundaria(Color.BLACK);

        MemoriaPropriedadesObjeto.lembrar(configurado);

        ObjetoPneus novo = new ObjetoPneus();
        MemoriaPropriedadesObjeto.aplicar(novo);

        assertEquals(45.0, novo.getAngulo());
        assertEquals(77, novo.getLargura());
        assertEquals(33, novo.getAltura());
        assertEquals(Color.BLUE, novo.getCorPimaria());
        assertEquals(Color.BLACK, novo.getCorSecundaria());
    }

    @Test
    void lembrarEAplicar_objetoLivre_propagaTambemOPadrao() {
        ObjetoLivre configurado = new ObjetoLivre();
        configurado.setTipo(TipoObjetoLivre.AGUA);
        configurado.setCorPimaria(Color.CYAN);
        configurado.setCorSecundaria(Color.DARK_GRAY);

        MemoriaPropriedadesObjeto.lembrar(configurado);

        ObjetoLivre novo = new ObjetoLivre();
        MemoriaPropriedadesObjeto.aplicar(novo);

        assertEquals(TipoObjetoLivre.AGUA, novo.getTipo());
        assertEquals(Color.CYAN, novo.getCorPimaria());
        assertEquals(Color.DARK_GRAY, novo.getCorSecundaria());
    }

    @Test
    void memoriaDeUmaClasse_naoAfetaOutraClasse() {
        ObjetoPneus pneus = new ObjetoPneus();
        pneus.setCorPimaria(Color.RED);
        MemoriaPropriedadesObjeto.lembrar(pneus);

        ObjetoArquibancada arquibancada = new ObjetoArquibancada();
        Color corOriginalArquibancada = arquibancada.getCorPimaria();

        MemoriaPropriedadesObjeto.aplicar(arquibancada);

        assertEquals(corOriginalArquibancada, arquibancada.getCorPimaria());
    }

    @Test
    void lembrar_comObjetoNulo_naoLancaExcecao() {
        MemoriaPropriedadesObjeto.lembrar(null);
    }

    @Test
    void aplicar_comObjetoNulo_naoLancaExcecao() {
        MemoriaPropriedadesObjeto.aplicar(null);
    }
}
