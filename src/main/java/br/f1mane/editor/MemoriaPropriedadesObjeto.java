package br.f1mane.editor;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.OrientacaoGuardRails;
import br.f1mane.entidades.TipoObjetoLivre;

/**
 * Lembra, por classe concreta de {@link ObjetoPista}, os últimos valores de
 * ângulo/largura/altura/cores (o {@link TipoObjetoLivre} no caso de
 * {@link ObjetoLivre}, a {@link OrientacaoGuardRails} no caso de
 * {@link ObjetoGuardRails}) usados pelo usuário, para que criar um novo
 * objeto do mesmo tipo já comece com esses valores em vez dos padrões da
 * classe. Só dura a sessão do editor (não é persistida em disco).
 */
final class MemoriaPropriedadesObjeto {

    private static final Map<Class<? extends ObjetoPista>, Snapshot> memoria =
            new HashMap<Class<? extends ObjetoPista>, Snapshot>();

    private MemoriaPropriedadesObjeto() {
    }

    /** Só para isolamento entre testes; nada em produção precisa esquecer a memória. */
    static void limparParaTeste() {
        memoria.clear();
    }

    /** Grava o estado atual de {@code objeto} como o "último usado" para a classe dele. */
    static void lembrar(ObjetoPista objeto) {
        if (objeto == null) {
            return;
        }
        Snapshot snapshot = new Snapshot();
        snapshot.angulo = objeto.getAngulo();
        snapshot.largura = objeto.getLargura();
        snapshot.altura = objeto.getAltura();
        snapshot.corPimaria = objeto.getCorPimaria();
        snapshot.corSecundaria = objeto.getCorSecundaria();
        if (objeto instanceof ObjetoLivre) {
            snapshot.tipoObjetoLivre = ((ObjetoLivre) objeto).getTipo();
        }
        if (objeto instanceof ObjetoGuardRails) {
            snapshot.orientacaoGuardRails = ((ObjetoGuardRails) objeto).getOrientacao();
        }
        memoria.put(objeto.getClass(), snapshot);
    }

    /** Aplica em {@code objeto} os últimos valores lembrados para a classe dele, se houver. */
    static void aplicar(ObjetoPista objeto) {
        if (objeto == null) {
            return;
        }
        Snapshot snapshot = memoria.get(objeto.getClass());
        if (snapshot == null) {
            return;
        }
        objeto.setAngulo(snapshot.angulo);
        objeto.setLargura(snapshot.largura);
        objeto.setAltura(snapshot.altura);
        if (snapshot.corPimaria != null) {
            objeto.setCorPimaria(snapshot.corPimaria);
        }
        if (snapshot.corSecundaria != null) {
            objeto.setCorSecundaria(snapshot.corSecundaria);
        }
        if (objeto instanceof ObjetoLivre && snapshot.tipoObjetoLivre != null) {
            ((ObjetoLivre) objeto).setTipo(snapshot.tipoObjetoLivre);
        }
        if (objeto instanceof ObjetoGuardRails && snapshot.orientacaoGuardRails != null) {
            ((ObjetoGuardRails) objeto).setOrientacao(snapshot.orientacaoGuardRails);
        }
    }

    private static final class Snapshot {
        double angulo;
        int largura;
        int altura;
        Color corPimaria;
        Color corSecundaria;
        TipoObjetoLivre tipoObjetoLivre;
        OrientacaoGuardRails orientacaoGuardRails;
    }
}
