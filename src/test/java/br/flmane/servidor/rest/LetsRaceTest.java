package br.flmane.servidor.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.recursos.CarregadorRecursos;
import br.flmane.servidor.controles.ControleJogosServer;
import br.flmane.servidor.controles.ControlePaddockServidor;
import br.flmane.servidor.entidades.TOs.CampeonatoTO;
import br.flmane.servidor.entidades.TOs.ErroServ;
import br.flmane.servidor.entidades.TOs.MsgSrv;
import br.flmane.servidor.entidades.TOs.SessaoCliente;
import br.flmane.servidor.entidades.TOs.SrvPaddockPack;
import br.flmane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.flmane.recursos.idiomas.Lang;

/**
 * Testes da camada REST LetsRace: autenticação/autorização, mapeamento de
 * MsgSrv/ErroServ para status HTTP e delegação correta de parâmetros para
 * os controllers. ControlePaddockServidor e ControleJogosServer são
 * mockados via Mockito; nenhum teste aqui sobe banco, thread ou Tomcat.
 */
class LetsRaceTest {

    private ControlePaddockServidor controlePaddock;
    private ControleJogosServer controleJogosServer;
    private LetsRace letsRace;

    private static final String TOKEN = "token-123";
    private static final String IDIOMA = "pt";

    @BeforeEach
    void setUp() {
        controlePaddock = mock(ControlePaddockServidor.class);
        controleJogosServer = mock(ControleJogosServer.class);
        when(controlePaddock.getControleJogosServer()).thenReturn(controleJogosServer);
        CarregadorRecursos carregadorRecursos = mock(CarregadorRecursos.class);
        letsRace = new LetsRace(carregadorRecursos, controlePaddock);
    }

    private SessaoCliente sessaoValida(boolean guest) {
        SessaoCliente sessaoCliente = new SessaoCliente();
        sessaoCliente.setGuest(guest);
        sessaoCliente.setIdUsuario("usuario1");
        return sessaoCliente;
    }

    // ---- verificaServico: endpoint sem nenhuma dependência ----

    @Test
    void verificaServico_naoChamaControlePaddock() {
        Response response = letsRace.verificaServico();

        assertEquals(200, response.getStatus());
        assertEquals("ok", response.getEntity());
    }

    // ---- 401 quando a sessão é inválida ----

    @Test
    void jogar_tokenInvalido_retorna401ENaoDelegaParaController() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.jogar(TOKEN, IDIOMA, "2024", "1", "interlagos",
                "10", "MACIO", "50", "NORMAL", "false");

        assertEquals(401, response.getStatus());
        verify(controlePaddock, never()).jogar(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void equipe_tokenInvalido_retorna401() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.equipe(TOKEN, IDIOMA);

        assertEquals(401, response.getStatus());
        verify(controleJogosServer, never()).equipe(any());
    }

    @Test
    void dadosParciais_tokenInvalido_retorna401() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.dadosParciais(TOKEN, IDIOMA, "jogo1", "piloto1");

        assertEquals(401, response.getStatus());
    }

    @Test
    void potenciaMotor_tokenInvalido_retorna401() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.potenciaMotor(TOKEN, "GIRO_MAX", "piloto1");

        assertEquals(401, response.getStatus());
        verify(controleJogosServer, never()).mudarGiroMotor(any(), any(), any());
    }

    // ---- 403 para sessão de visitante em endpoint restrito ----

    @Test
    void campeonato_sessaoVisitante_retorna403() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(true));

        Response response = letsRace.campeonato(TOKEN, IDIOMA);

        assertEquals(403, response.getStatus());
        verify(controlePaddock, never()).obterCampeonatoEmAberto(any());
    }

    @Test
    void campeonato_sessaoNaoVisitante_retorna200ComCampeonato() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);
        CampeonatoTO campeonatoTO = new CampeonatoTO();
        when(controlePaddock.obterCampeonatoEmAberto("usuario1")).thenReturn(campeonatoTO);

        Response response = letsRace.campeonato(TOKEN, IDIOMA);

        assertEquals(200, response.getStatus());
        assertEquals(campeonatoTO, response.getEntity());
    }

    @Test
    void campeonato_semCampeonatoEmAberto_retorna204() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        when(controlePaddock.obterCampeonatoEmAberto("usuario1")).thenReturn(null);

        Response response = letsRace.campeonato(TOKEN, IDIOMA);

        assertEquals(204, response.getStatus());
    }

    // ---- criarSessaoNome: regressão do bug de serialização de ErroServ ----

    @Test
    void criarSessaoNome_sucesso_retorna200ComPayload() {
        SrvPaddockPack pack = new SrvPaddockPack();
        when(controlePaddock.criarSessaoNome("Piloto")).thenReturn(pack);

        Response response = letsRace.criarSessaoNome("Piloto");

        assertEquals(200, response.getStatus());
        assertEquals(pack, response.getEntity());
    }

    @Test
    void criarSessaoNome_erroNoController_retorna500ComErroServ() {
        ErroServ erroServ = new ErroServ(new RuntimeException("falha de banco"));
        when(controlePaddock.criarSessaoNome("Piloto")).thenReturn(erroServ);

        Response response = letsRace.criarSessaoNome("Piloto");

        assertEquals(500, response.getStatus());
        assertEquals(erroServ, response.getEntity());
    }

    @Test
    void criarSessaoGoogle_erroNoController_retorna500() {
        ErroServ erroServ = new ErroServ(new RuntimeException("falha"));
        when(controlePaddock.criarSessaoGoogle("g1", "Nome", "url", "a@b.com"))
                .thenReturn(erroServ);

        Response response = letsRace.criarSessaoGoogle("g1", "Nome", "url", "a@b.com");

        assertEquals(500, response.getStatus());
    }

    // ---- processsaMensagem: MsgSrv -> 400, ErroServ -> 500, sucesso -> 200 ----

    @Test
    void jogar_controllerRetornaMsgSrv_retorna400ComMensagemTraduzida() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        when(controlePaddock.jogar(eq("2024"), eq("interlagos"), eq("1"), eq("10"),
                eq("MACIO"), eq("50"), eq("NORMAL"), any(SessaoCliente.class), eq("false")))
                .thenReturn(new MsgSrv("Carro indisponivel"));

        Response response = letsRace.jogar(TOKEN, IDIOMA, "2024", "1", "interlagos",
                "10", "MACIO", "50", "NORMAL", "false");

        assertEquals(400, response.getStatus());
        assertEquals("Carro indisponivel", ((MsgSrv) response.getEntity()).getMessageString());
    }

    @Test
    void jogar_controllerRetornaErroServ_retorna500ComMensagemFormatada() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        ErroServ erroServ = new ErroServ(new RuntimeException("boom"));
        when(controlePaddock.jogar(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(erroServ);

        Response response = letsRace.jogar(TOKEN, IDIOMA, "2024", "1", "interlagos",
                "10", "MACIO", "50", "NORMAL", "false");

        assertEquals(500, response.getStatus());
        assertEquals(erroServ.obterErroFormatado(),
                ((MsgSrv) response.getEntity()).getMessageString());
    }

    @Test
    void jogar_sucesso_retorna200ComPayloadDoController() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);
        Object dadosJogo = new Object();
        when(controlePaddock.jogar(eq("2024"), eq("interlagos"), eq("1"), eq("10"),
                eq("MACIO"), eq("50"), eq("NORMAL"), eq(sessaoCliente), eq("false")))
                .thenReturn(dadosJogo);

        Response response = letsRace.jogar(TOKEN, IDIOMA, "2024", "1", "interlagos",
                "10", "MACIO", "50", "NORMAL", "false");

        assertEquals(200, response.getStatus());
        assertEquals(dadosJogo, response.getEntity());
    }

    @Test
    void equipe_controllerRetornaNull_retorna204() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        when(controleJogosServer.equipe(any())).thenReturn(null);

        Response response = letsRace.equipe(TOKEN, IDIOMA);

        assertEquals(204, response.getStatus());
    }

    @Test
    void equipe_controllerRetornaErroServ_retorna500() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        ErroServ erroServ = new ErroServ(new RuntimeException("falha"));
        when(controleJogosServer.equipe(any())).thenReturn(erroServ);

        Response response = letsRace.equipe(TOKEN, IDIOMA);

        assertEquals(500, response.getStatus());
    }

    @Test
    void equipe_sucesso_retorna200() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        Object equipe = new Object();
        when(controleJogosServer.equipe(any())).thenReturn(equipe);

        Response response = letsRace.equipe(TOKEN, IDIOMA);

        assertEquals(200, response.getStatus());
        assertEquals(equipe, response.getEntity());
    }

    // ---- gravarEquipe: caminho de sucesso usa comparação literal com Lang.msg("250") ----

    @Test
    void gravarEquipe_tokenInvalido_retorna401() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.gravarEquipe(TOKEN, IDIOMA, new CarreiraDadosSrv());

        assertEquals(401, response.getStatus());
    }

    @Test
    void gravarEquipe_sucesso_retorna200() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        MsgSrv sucesso = new MsgSrv(Lang.msg("250"));
        when(controleJogosServer.gravarEquipe(any(), eq(IDIOMA), any())).thenReturn(sucesso);

        Response response = letsRace.gravarEquipe(TOKEN, IDIOMA, new CarreiraDadosSrv());

        assertEquals(200, response.getStatus());
    }

    @Test
    void gravarEquipe_falhaDeValidacao_retorna400() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoValida(false));
        when(controleJogosServer.gravarEquipe(any(), eq(IDIOMA), any()))
                .thenReturn(new MsgSrv("Nome de equipe invalido"));

        Response response = letsRace.gravarEquipe(TOKEN, IDIOMA, new CarreiraDadosSrv());

        assertEquals(400, response.getStatus());
    }

    // ---- delegação simples de controles de piloto ----

    @Test
    void potenciaMotor_delegaParaControleJogosServerComParametrosCorretos() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);
        when(controleJogosServer.mudarGiroMotor(sessaoCliente, "piloto1", "GIRO_MAX"))
                .thenReturn(Boolean.TRUE);

        Response response = letsRace.potenciaMotor(TOKEN, "GIRO_MAX", "piloto1");

        assertEquals(200, response.getStatus());
        assertEquals(Boolean.TRUE, response.getEntity());
        verify(controleJogosServer).mudarGiroMotor(sessaoCliente, "piloto1", "GIRO_MAX");
    }

    @Test
    void agressividadePiloto_delegaComParametrosCorretos() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        letsRace.agressividadePiloto(TOKEN, "AGRESSIVO", "piloto1");

        verify(controleJogosServer).mudarAgressividadePiloto(sessaoCliente, "piloto1", "AGRESSIVO");
    }

    @Test
    void tracadoPiloto_delegaComParametrosCorretos() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        letsRace.tracadoPiloto(TOKEN, "INTERNO", "piloto1");

        verify(controleJogosServer).mudarTracadoPiloto(sessaoCliente, "piloto1", "INTERNO");
    }

    @Test
    void drsPiloto_delegaComParametrosCorretos() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        letsRace.drsPiloto(TOKEN, "piloto1");

        verify(controleJogosServer).mudarDrs(sessaoCliente, "piloto1");
    }

    @Test
    void ersPiloto_delegaComParametrosCorretos() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        letsRace.ersPiloto(TOKEN, "piloto1");

        verify(controleJogosServer).mudarErs(sessaoCliente, "piloto1");
    }

    @Test
    void boxPiloto_delegaTodosOsParametrosDePitStop() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        letsRace.boxPiloto(TOKEN, "piloto1", true, "MACIO", 50, "BAIXA");

        verify(controleJogosServer).boxPiloto(sessaoCliente, "piloto1", true, "MACIO", 50, "BAIXA");
    }

    // ---- endpoints de leitura simples ----

    @Test
    void obterJogos_retorna200ComListaDoController() {
        when(controlePaddock.obterJogos()).thenReturn(java.util.List.of("jogo1", "jogo2"));

        Response response = letsRace.obterJogos();

        assertEquals(200, response.getStatus());
        assertEquals(java.util.List.of("jogo1", "jogo2"), response.getEntity());
    }

    @Test
    void classificacaoGeral_retorna200ComResultadoDoController() {
        Object classificacao = new Object();
        when(controlePaddock.obterClassificacaoGeral()).thenReturn(classificacao);

        Response response = letsRace.classificacaoGeral();

        assertEquals(200, response.getStatus());
        assertEquals(classificacao, response.getEntity());
    }

    @Test
    void campeonatoPorId_naoExiste_retorna204() {
        when(controlePaddock.obterCampeonatoId("99")).thenReturn(null);

        Response response = letsRace.campeonatoPorId("99", TOKEN, IDIOMA);

        assertEquals(204, response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    void classificacaoEquipes_retorna200ComResultadoDoController() {
        Object classificacao = new Object();
        when(controlePaddock.obterClassificacaoEquipes()).thenReturn(classificacao);

        Response response = letsRace.classificacaoEquipes();

        assertEquals(200, response.getStatus());
        assertEquals(classificacao, response.getEntity());
    }

    @Test
    void classificacaoCampeonato_retorna200ComResultadoDoController() {
        Object classificacao = new Object();
        when(controlePaddock.obterClassificacaoCampeonato()).thenReturn(classificacao);

        Response response = letsRace.classificacaoCampeonato();

        assertEquals(200, response.getStatus());
        assertEquals(classificacao, response.getEntity());
    }

    @Test
    void atualizarDadosVisao_retorna200ComResultadoDoController() {
        Object dadosVisao = new Object();
        when(controlePaddock.atualizarDadosVisao()).thenReturn(dadosVisao);

        Response response = letsRace.atualizarDadosVisao();

        assertEquals(200, response.getStatus());
        assertEquals(dadosVisao, response.getEntity());
    }

    @Test
    void dadosToken_tokenValido_retorna200ComPack() {
        SrvPaddockPack pack = new SrvPaddockPack();
        pack.setSessaoCliente(sessaoValida(false));
        when(controlePaddock.obterDadosToken(TOKEN)).thenReturn(pack);

        Response response = letsRace.dadosToken(TOKEN);

        assertEquals(200, response.getStatus());
        assertEquals(pack, response.getEntity());
    }

    @Test
    void dadosToken_semSessao_retorna404() {
        when(controlePaddock.obterDadosToken(TOKEN)).thenReturn(null);

        Response response = letsRace.dadosToken(TOKEN);

        assertEquals(404, response.getStatus());
    }

    @Test
    void sairJogo_tokenInvalido_retorna401() {
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(null);

        Response response = letsRace.sairJogo(TOKEN, "jogo1");

        assertEquals(401, response.getStatus());
        verify(controlePaddock, never()).sairJogoToken(any(), any(), any());
    }

    @Test
    void sairJogo_tokenValido_delegaParaController() {
        SessaoCliente sessaoCliente = sessaoValida(false);
        when(controlePaddock.obterSessaoPorToken(TOKEN)).thenReturn(sessaoCliente);

        Response response = letsRace.sairJogo(TOKEN, "jogo1");

        assertEquals(200, response.getStatus());
        verify(controlePaddock).sairJogoToken("jogo1", TOKEN, sessaoCliente);
    }

    @Test
    void circuitos_retorna200ComListaDoCarregadorRecursos() throws Exception {
        CarregadorRecursos carregadorRecursosMock = mock(CarregadorRecursos.class);
        LetsRace letsRaceComCarregador = new LetsRace(carregadorRecursosMock, controlePaddock);
        when(carregadorRecursosMock.carregarCircuitosDefaults())
                .thenReturn(java.util.List.of());

        Response response = letsRaceComCarregador.circuitos();

        assertEquals(200, response.getStatus());
    }
}
