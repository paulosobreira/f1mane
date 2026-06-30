package br.f1mane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Piloto;
import br.f1mane.servidor.entidades.TOs.MsgSrv;
import br.f1mane.servidor.entidades.persistencia.CarreiraDadosSrv;

/**
 * Cobre a lógica pura de ControlePersistencia. A maior parte da classe é
 * wrapper de CRUD do Hibernate (HQL direto em cima de Session) sem
 * ramificação de regra - fora de escopo aqui. carreiraDadosParaPiloto é pura
 * (mapeamento de objeto) e modoCarreira tem validação real testável via spy,
 * estubando apenas as chamadas que tocam Session/Hibernate.
 */
class ControlePersistenciaTest {

    // ---- carreiraDadosParaPiloto: mapeamento puro de carreira para piloto ----

    private CarreiraDadosSrv carreiraBasica() {
        CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
        carreiraDadosSrv.setNomePiloto("Piloto Teste");
        carreiraDadosSrv.setNomeCarro("Carro Teste");
        carreiraDadosSrv.setPtsPiloto(700);
        carreiraDadosSrv.setPtsAerodinamica(600);
        carreiraDadosSrv.setPtsCarro(650);
        carreiraDadosSrv.setPtsFreio(620);
        carreiraDadosSrv.setPtsConstrutoresGanhos(123);
        return carreiraDadosSrv;
    }

    @Test
    void carreiraDadosParaPiloto_semLiveryDefinido_usaCorComoFallback() {
        ControlePersistencia controlePersistencia = new ControlePersistencia(null, null);
        CarreiraDadosSrv carreiraDadosSrv = carreiraBasica();
        Piloto piloto = new Piloto();

        controlePersistencia.carreiraDadosParaPiloto(carreiraDadosSrv, piloto);

        assertEquals("Piloto Teste", piloto.getNome());
        assertEquals("Carro Teste", piloto.getNomeCarro());
        assertEquals(700, piloto.getHabilidade());
        assertEquals(123, piloto.getPontosCorrida());
        assertNotNull(piloto.getCarro());
        assertEquals(600, piloto.getCarro().getAerodinamica());
        assertEquals(650, piloto.getCarro().getPotencia());
        assertEquals(620, piloto.getCarro().getFreios());
        // sem temporada de livery definida, cai no fallback de cor gerada
        assertNotNull(piloto.getTemporadaCapaceteLivery());
        assertNotNull(piloto.getTemporadaCarroLivery());
    }

    @Test
    void carreiraDadosParaPiloto_comLiveryDefinido_usaIdDaLivery() {
        ControlePersistencia controlePersistencia = new ControlePersistencia(null, null);
        CarreiraDadosSrv carreiraDadosSrv = carreiraBasica();
        carreiraDadosSrv.setTemporadaCapaceteLivery(2024);
        carreiraDadosSrv.setIdCapaceteLivery(5);
        carreiraDadosSrv.setTemporadaCarroLivery(2024);
        carreiraDadosSrv.setIdCarroLivery(7);
        Piloto piloto = new Piloto();

        controlePersistencia.carreiraDadosParaPiloto(carreiraDadosSrv, piloto);

        assertEquals("2024", piloto.getTemporadaCapaceteLivery());
        assertEquals("5", piloto.getIdCapaceteLivery());
        assertEquals("2024", piloto.getTemporadaCarroLivery());
        assertEquals("7", piloto.getIdCarroLivery());
    }

    // ---- modoCarreira: validação antes de gravar (spy para estubar chamadas internas) ----

    @Test
    void modoCarreira_ativandoComDadosIncompletos_retornaMsgSrvSemGravar() throws Exception {
        ControlePersistencia controlePersistencia = spy(new ControlePersistencia(null, null));
        Session session = mock(Session.class);
        doReturn(session).when(controlePersistencia).getSession();
        CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
        carreiraDadosSrv.setNomeCarro("");
        doReturn(carreiraDadosSrv).when(controlePersistencia)
                .carregaCarreiraJogador(eq("usuario1"), eq(false), any(Session.class));

        MsgSrv resultado = controlePersistencia.modoCarreira("usuario1", true);

        assertNotNull(resultado);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void modoCarreira_desativandoModoCarreira_naoExigeDadosCompletos() throws Exception {
        ControlePersistencia controlePersistencia = spy(new ControlePersistencia(null, null));
        Session session = mock(Session.class);
        doReturn(session).when(controlePersistencia).getSession();
        CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
        carreiraDadosSrv.setNomeCarro("");
        doReturn(carreiraDadosSrv).when(controlePersistencia)
                .carregaCarreiraJogador(eq("usuario1"), eq(false), any(Session.class));
        doNothing().when(controlePersistencia).gravarDados(any(), any());

        controlePersistencia.modoCarreira("usuario1", false);

        verify(controlePersistencia).gravarDados(eq(session), eq(carreiraDadosSrv));
        assertEquals(false, carreiraDadosSrv.isModoCarreira());
    }
}
