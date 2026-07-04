package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.ObjetoArquibancada;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.TipoObjetoLivre;

/**
 * Diagnóstico: carregar um ObjetoLivre já configurado com um padrão não
 * trivial no formulário, e gravar de volta sem alterar nada, deveria
 * preservar o mesmo padrão — reproduz o relato de "o combo volta pra
 * polígono simples ao editar".
 */
class FormularioObjetosObjetoLivreTest {

    @Test
    void carregarEGravar_semAlterarNada_preservaOTipoDoObjeto() {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setTipo(TipoObjetoLivre.VEGETACAO_DENSA);

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(objeto);
        formulario.formularioObjetoPista(objeto);

        assertEquals(TipoObjetoLivre.VEGETACAO_DENSA, objeto.getTipo());
    }

    /**
     * O campo "Nível" (nivelDesenho) precisa estar presente no formulário —
     * relato de que ele não aparecia na edição. Carregar um objeto com um
     * nível não-padrão e gravar de volta sem alterar nada deve preservá-lo,
     * inclusive além da antiga faixa [-1, 1].
     */
    @Test
    void carregarEGravar_semAlterarNada_preservaONivelDesenho_objetoLivre() {
        ObjetoLivre objeto = new ObjetoLivre();
        objeto.setNivelDesenho(20);

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(objeto);
        formulario.formularioObjetoPista(objeto);

        assertEquals(20, objeto.getNivelDesenho());
    }

    @Test
    void carregarEGravar_semAlterarNada_preservaONivelDesenho_objetoDePadraoGeral() {
        ObjetoArquibancada objeto = new ObjetoArquibancada();
        objeto.setNivelDesenho(-7);

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(objeto);
        formulario.formularioObjetoPista(objeto);

        assertEquals(-7, objeto.getNivelDesenho());
    }

    /** ObjetoTransparencia fica fora do sistema de níveis: o formulário nem tenta gravar nivelDesenho para ele. */
    @Test
    void formularioObjetoPista_naoAlteraNivelDeObjetoTransparencia() {
        ObjetoTransparencia transparencia = new ObjetoTransparencia();

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(transparencia);
        formulario.formularioObjetoPista(transparencia);

        assertEquals(0, transparencia.getNivelDesenho());
    }

    /** ObjetoEscapada também é objeto de função, fora do sistema de níveis, como ObjetoTransparencia. */
    @Test
    void formularioObjetoPista_naoAlteraNivelDeObjetoEscapada() {
        ObjetoEscapada escapada = new ObjetoEscapada();

        FormularioObjetos formulario = new FormularioObjetos(null);
        formulario.carregarCampos(escapada);
        formulario.formularioObjetoPista(escapada);

        assertEquals(0, escapada.getNivelDesenho());
    }
}
