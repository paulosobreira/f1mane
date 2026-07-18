package br.flmane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.entidades.TemporadasDefault;
import br.flmane.recursos.CarregadorRecursos;
import br.flmane.servidor.entidades.TOs.CampeonatoTO;
import br.flmane.servidor.entidades.TOs.ClientPaddockPack;
import br.flmane.servidor.entidades.TOs.ErroServ;
import br.flmane.servidor.entidades.TOs.MsgSrv;
import br.flmane.servidor.entidades.TOs.SessaoCliente;
import br.flmane.servidor.entidades.persistencia.CampeonatoSrv;
import br.flmane.servidor.entidades.persistencia.CorridaCampeonatoSrv;
import br.flmane.servidor.entidades.persistencia.JogadorDadosSrv;

import java.util.Map;

/**
 * Cobre o ciclo de vida de campeonato em ControleCampeonatoServidor.
 * ControlePersistencia, ControlePaddockServidor e CarregadorRecursos são
 * mockados via o construtor pacote-privado de injeção; nenhum banco real
 * nem classpath real é tocado.
 */
class ControleCampeonatoServidorTest {

    private ControlePersistencia controlePersistencia;
    private CarregadorRecursos carregadorRecursos;
    private ControleCampeonatoServidor controleCampeonatoServidor;

    @BeforeEach
    void setUp() {
        controlePersistencia = mock(ControlePersistencia.class);
        carregadorRecursos = mock(CarregadorRecursos.class);
        ControlePaddockServidor controlePaddockServidor = mock(ControlePaddockServidor.class);
        controleCampeonatoServidor = new ControleCampeonatoServidor(controlePersistencia, controlePaddockServidor,
                carregadorRecursos);
    }

    private CampeonatoSrv campeonatoComCorridas(int qtdeCorridas) {
        CampeonatoSrv campeonato = new CampeonatoSrv();
        campeonato.setNome("Campeonato Teste");
        campeonato.setIdPiloto("piloto1");
        List<CorridaCampeonatoSrv> corridas = new ArrayList<>();
        for (int i = 0; i < qtdeCorridas; i++) {
            corridas.add(new CorridaCampeonatoSrv());
        }
        campeonato.setCorridaCampeonatos(corridas);
        return campeonato;
    }

    // ---- criarCampeonato(ClientPaddockPack): exige sessão de cliente ----

    @Test
    void criarCampeonato_sessaoNula_retornaMsgSrvSemPersistir() {
        ClientPaddockPack clientPaddockPack = new ClientPaddockPack();

        Object resultado = controleCampeonatoServidor.criarCampeonato(clientPaddockPack);

        assertTrue(resultado instanceof MsgSrv);
        verifyNenhumaPersistencia();
    }

    // ---- criarCampeonato(CampeonatoSrv, idUsuario): validações de entrada ----

    @Test
    void criarCampeonato_idUsuarioNulo_retornaMsgSrv() {
        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonatoComCorridas(5), null);

        assertTrue(resultado instanceof MsgSrv);
        verifyNenhumaPersistencia();
    }

    @Test
    void criarCampeonato_nomeVazio_retornaMsgSrv() {
        CampeonatoSrv campeonato = campeonatoComCorridas(5);
        campeonato.setNome("");

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonato, "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verifyNenhumaPersistencia();
    }

    @Test
    void criarCampeonato_semPilotoSelecionado_retornaMsgSrv() {
        CampeonatoSrv campeonato = campeonatoComCorridas(5);
        campeonato.setIdPiloto(null);

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonato, "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verifyNenhumaPersistencia();
    }

    @Test
    void criarCampeonato_menosDe5Corridas_retornaMsgSrv() {
        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonatoComCorridas(4), "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verifyNenhumaPersistencia();
    }

    @Test
    void criarCampeonato_jogadorJaTemCampeonatoEmAberto_retornaMsgSrvSemGravar() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of(new CampeonatoSrv()));

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonatoComCorridas(5), "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void criarCampeonato_nomeJaExistente_retornaMsgSrvSemGravar() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of());
        when(controlePersistencia.existeNomeCampeonato(any(Session.class), eq("Campeonato Teste")))
                .thenReturn(true);

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonatoComCorridas(5), "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void criarCampeonato_jogadorNaoEncontrado_retornaMsgSrvSemGravar() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of());
        when(controlePersistencia.existeNomeCampeonato(any(Session.class), eq("Campeonato Teste")))
                .thenReturn(false);
        when(controlePersistencia.carregaDadosJogador(eq("usuario1"), any(Session.class)))
                .thenReturn(null);

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonatoComCorridas(5), "usuario1");

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    private void verifyNenhumaPersistencia() {
        try {
            verify(controlePersistencia, never()).getSession();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ---- verificaCampeonatoConcluido: pura ----

    @Test
    void verificaCampeonatoConcluido_todasCorridasComTempoFim_retornaTrue() {
        CampeonatoSrv campeonato = new CampeonatoSrv();
        CorridaCampeonatoSrv corrida1 = new CorridaCampeonatoSrv();
        corrida1.setTempoFim(1000L);
        CorridaCampeonatoSrv corrida2 = new CorridaCampeonatoSrv();
        corrida2.setTempoFim(2000L);
        campeonato.setCorridaCampeonatos(List.of(corrida1, corrida2));

        assertTrue(controleCampeonatoServidor.verificaCampeonatoConcluido(campeonato));
    }

    @Test
    void verificaCampeonatoConcluido_corridaSemTempoFim_retornaFalse() {
        CampeonatoSrv campeonato = new CampeonatoSrv();
        CorridaCampeonatoSrv corrida1 = new CorridaCampeonatoSrv();
        corrida1.setTempoFim(1000L);
        CorridaCampeonatoSrv corrida2 = new CorridaCampeonatoSrv();
        corrida2.setTempoFim(null);
        campeonato.setCorridaCampeonatos(List.of(corrida1, corrida2));

        assertFalse(controleCampeonatoServidor.verificaCampeonatoConcluido(campeonato));
    }

    // ---- finalizaCampeonato ----

    @Test
    void finalizaCampeonato_semCampeonatoEmAberto_retornaNull() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of());
        CampeonatoTO campeonatoTO = new CampeonatoTO();
        campeonatoTO.setId(1);

        Object resultado = controleCampeonatoServidor.finalizaCampeonato(campeonatoTO, "usuario1");

        assertNull(resultado);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void finalizaCampeonato_idDiferenteDoCampeonatoEmAberto_retornaNull() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        CampeonatoSrv campeonatoEmAberto = mock(CampeonatoSrv.class);
        when(campeonatoEmAberto.getId()).thenReturn(99L);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of(campeonatoEmAberto));
        CampeonatoTO campeonatoTO = new CampeonatoTO();
        campeonatoTO.setId(1);

        Object resultado = controleCampeonatoServidor.finalizaCampeonato(campeonatoTO, "usuario1");

        assertNull(resultado);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void finalizaCampeonato_idCorrespondente_marcaFinalizadoEGrava() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        CampeonatoSrv campeonatoEmAberto = mock(CampeonatoSrv.class);
        when(campeonatoEmAberto.getId()).thenReturn(1L);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of(campeonatoEmAberto));
        CampeonatoTO campeonatoTO = new CampeonatoTO();
        campeonatoTO.setId(1);

        Object resultado = controleCampeonatoServidor.finalizaCampeonato(campeonatoTO, "usuario1");

        assertEquals(campeonatoTO, resultado);
        verify(campeonatoEmAberto).setFinalizado(true);
        verify(controlePersistencia).gravarDados(eq(session), eq(campeonatoEmAberto));
    }

    @Test
    void finalizaCampeonato_excecaoNaPersistencia_retornaErroServ() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenThrow(new RuntimeException("falha de banco"));
        CampeonatoTO campeonatoTO = new CampeonatoTO();
        campeonatoTO.setId(1);

        Object resultado = controleCampeonatoServidor.finalizaCampeonato(campeonatoTO, "usuario1");

        assertTrue(resultado instanceof ErroServ);
    }

    // ---- criarCampeonato: caminho de sucesso completo, com CarregadorRecursos mockado ----

    @Test
    void criarCampeonato_dadosValidos_gravaERetornaSucesso() throws Exception {
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.pesquisaCampeonatosEmAberto(eq("usuario1"), any(Session.class), eq(false)))
                .thenReturn(List.of());
        when(controlePersistencia.existeNomeCampeonato(any(Session.class), eq("Campeonato Teste")))
                .thenReturn(false);
        JogadorDadosSrv jogadorDadosSrv = mock(JogadorDadosSrv.class);
        when(controlePersistencia.carregaDadosJogador(eq("usuario1"), any(Session.class)))
                .thenReturn(jogadorDadosSrv);
        when(controlePersistencia.pesquisaCampeonatos(eq(jogadorDadosSrv), any(Session.class)))
                .thenReturn(List.of());

        TemporadasDefault temporadasDefault = new TemporadasDefault();
        temporadasDefault.setTrocaPneu(true);
        temporadasDefault.setDrs(true);
        temporadasDefault.setErs(false);
        temporadasDefault.setReabastecimento(true);
        when(carregadorRecursos.carregarTemporadasPilotosDefauts())
                .thenReturn(Map.of("t2024", temporadasDefault));

        CampeonatoSrv campeonato = campeonatoComCorridas(5);
        campeonato.setTemporada("2024");

        Object resultado = controleCampeonatoServidor.criarCampeonato(campeonato, "usuario1");

        assertTrue(campeonato.isTrocaPneus());
        assertTrue(campeonato.isDrs());
        assertFalse(campeonato.isErs());
        assertEquals(jogadorDadosSrv, campeonato.getJogadorDadosSrv());
        verify(controlePersistencia).gravarDados(eq(session), eq(campeonato));
        assertTrue(resultado instanceof MsgSrv);
    }
}
