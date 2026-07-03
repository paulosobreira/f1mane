package br.f1mane.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.ObjetoLivre;
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
}
