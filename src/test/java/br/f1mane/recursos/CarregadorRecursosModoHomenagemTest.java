package br.f1mane.recursos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.nnpe.Global;

/**
 * Cobre a resolução de nome de carro/piloto conforme Global.MODO_HOMENAGEM
 * (substitui as antigas chamadas a Util.substVogais pra esses dois tipos —
 * ver modo-homenagem-toggle) e o fallback quando nomeHomenagem está ausente
 * do .properties.
 */
class CarregadorRecursosModoHomenagemTest {

    private static final String TEMPORADA_COM_HOMENAGEM = "tcomhomenagem";
    private static final String TEMPORADA_SEM_HOMENAGEM = "tsemhomenagem";

    @AfterEach
    void resetFlag() {
        Global.MODO_HOMENAGEM = true;
    }

    @Test
    void modoHomenagemAtivo_usaNomeHomenagemDoCarroEDoPiloto() throws Exception {
        Global.MODO_HOMENAGEM = true;
        CarregadorRecursos cr = CarregadorRecursos.getCarregadorRecursos(false);

        List<Carro> carros = cr.carregarListaCarros(TEMPORADA_COM_HOMENAGEM);
        assertEquals(1, carros.size());
        assertEquals("testcanon", carros.get(0).getNome());
        assertEquals("TestTeam-T1", carros.get(0).getNomeOriginal());

        List<Piloto> pilotos = cr.carregarListaPilotos(TEMPORADA_COM_HOMENAGEM);
        assertEquals(1, pilotos.size());
        assertEquals("T.Homenagem", pilotos.get(0).getNome());
        assertEquals("T.Test", pilotos.get(0).getNomeOriginal());
    }

    @Test
    void modoHomenagemInativo_usaNomeRealSemSubstVogais() throws Exception {
        Global.MODO_HOMENAGEM = false;
        CarregadorRecursos cr = CarregadorRecursos.getCarregadorRecursos(false);

        List<Carro> carros = cr.carregarListaCarros(TEMPORADA_COM_HOMENAGEM);
        assertEquals("TestTeam-T1", carros.get(0).getNome(),
                "com o modo desativado, o nome exibido deve ser o real, mesmo havendo nomeHomenagem cadastrado");

        List<Piloto> pilotos = cr.carregarListaPilotos(TEMPORADA_COM_HOMENAGEM);
        assertEquals("T.Test", pilotos.get(0).getNome(),
                "com o modo desativado, o nome exibido deve ser o real (chave), sem qualquer ofuscação por vogal");
    }

    @Test
    void temporadaSemNomeHomenagem_carregaSemQuebrar_comFallbackParaNomeReal() throws Exception {
        Global.MODO_HOMENAGEM = true;
        CarregadorRecursos cr = CarregadorRecursos.getCarregadorRecursos(false);

        List<Carro> carros = cr.carregarListaCarros(TEMPORADA_SEM_HOMENAGEM);
        assertEquals(1, carros.size());
        assertEquals("TestTeam-T1", carros.get(0).getNome(),
                "sem nomeHomenagem cadastrado, cai no nome real mesmo com o modo ativo");
        assertEquals("TestTeam-T1", carros.get(0).getNomeOriginal());

        List<Piloto> pilotos = cr.carregarListaPilotos(TEMPORADA_SEM_HOMENAGEM);
        assertEquals(1, pilotos.size());
        assertEquals("T.Test", pilotos.get(0).getNome());
        assertEquals("T.Test", pilotos.get(0).getNomeOriginal());
    }

    @Test
    void ligarPilotosCarros_pareiaPeloNomeOriginal_naoPeloNomeExibido() throws Exception {
        Global.MODO_HOMENAGEM = true;
        CarregadorRecursos cr = CarregadorRecursos.getCarregadorRecursos(false);

        List<Piloto> pilotos = cr.carregarListaPilotos(TEMPORADA_COM_HOMENAGEM);
        List<Carro> carros = cr.carregarListaCarros(TEMPORADA_COM_HOMENAGEM);
        cr.ligarPilotosCarros(pilotos, carros);

        Piloto piloto = pilotos.get(0);
        assertNotNull(piloto.getCarro(), "o piloto deveria ter sido pareado com o carro mesmo com nomes-homenagem diferentes da chave real");
        assertEquals("testcanon", piloto.getCarro().getNome());
    }

    @Test
    void globalGerarImagemCircuitoEmMemoria_naoExisteMais() {
        boolean encontrado = false;
        for (Field f : Global.class.getDeclaredFields()) {
            if (f.getName().equals("GERAR_IMAGEM_CIRCUITO_EM_MEMORIA")) {
                encontrado = true;
            }
        }
        assertFalse(encontrado, "Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA deveria ter sido removido, substituído por MODO_HOMENAGEM");
    }

    @Test
    void globalForceModeloV2_naoExisteMais() {
        boolean encontrado = false;
        for (Field f : Global.class.getDeclaredFields()) {
            if (f.getName().equals("FORCE_MODELO_V2")) {
                encontrado = true;
            }
        }
        assertFalse(encontrado, "Global.FORCE_MODELO_V2 deveria ter sido removido, substituído por MODO_HOMENAGEM");
    }

}
