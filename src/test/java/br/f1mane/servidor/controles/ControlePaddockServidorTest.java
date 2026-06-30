package br.f1mane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;
import br.f1mane.servidor.entidades.TOs.SrvPaddockPack;

/**
 * Cobre criação/renovação de sessão em ControlePaddockServidor.
 * ControlePersistencia e CarregadorRecursos são mockados via o construtor
 * pacote-privado de injeção; ControleClassificacao/ControleCampeonatoServidor/
 * ControleJogosServer são as instâncias reais que o próprio construtor de
 * ControlePaddockServidor monta (não são injetáveis separadamente), todas
 * apontando para os mesmos mocks.
 */
class ControlePaddockServidorTest {

    private ControlePersistencia controlePersistencia;
    private ControlePaddockServidor controlePaddockServidor;

    @BeforeEach
    void setUp() {
        controlePersistencia = mock(ControlePersistencia.class);
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        CarregadorRecursos carregadorRecursos = mock(CarregadorRecursos.class);
        controlePaddockServidor = new ControlePaddockServidor(controlePersistencia, carregadorRecursos);
    }

    // ---- criarSessaoNome: reaproveita sessão existente vs. cria nova ----

    @Test
    void criarSessaoNome_usuarioNovo_criaSessaoComToken() {
        Object resultado = controlePaddockServidor.criarSessaoNome("Piloto1");

        assertTrue(resultado instanceof SrvPaddockPack);
        SessaoCliente sessaoCliente = ((SrvPaddockPack) resultado).getSessaoCliente();
        assertNotNull(sessaoCliente.getToken());
        assertEquals("Piloto1", sessaoCliente.getIdUsuario());
        assertEquals("Piloto1", sessaoCliente.getNomeJogador());
        assertEquals(false, sessaoCliente.isGuest());
    }

    @Test
    void criarSessaoNome_usuarioJaExistente_reaproveitaMesmaSessao() {
        Object primeiraChamada = controlePaddockServidor.criarSessaoNome("Piloto1");
        SessaoCliente primeiraSessao = ((SrvPaddockPack) primeiraChamada).getSessaoCliente();

        Object segundaChamada = controlePaddockServidor.criarSessaoNome("Piloto1");
        SessaoCliente segundaSessao = ((SrvPaddockPack) segundaChamada).getSessaoCliente();

        assertSame(primeiraSessao, segundaSessao);
        assertEquals(primeiraSessao.getToken(), segundaSessao.getToken());
        assertEquals(1, controlePaddockServidor.getDadosPaddock().getClientes().size());
    }

    @Test
    void criarSessaoNome_doisUsuariosDiferentes_criaDuasSessoesDistintas() {
        controlePaddockServidor.criarSessaoNome("Piloto1");
        controlePaddockServidor.criarSessaoNome("Piloto2");

        assertEquals(2, controlePaddockServidor.getDadosPaddock().getClientes().size());
    }

    // ---- criarSessaoVisitante ----

    @Test
    void criarSessaoVisitante_marcaSessaoComoGuest() {
        Object resultado = controlePaddockServidor.criarSessaoVisitante();

        assertTrue(resultado instanceof SrvPaddockPack);
        SessaoCliente sessaoCliente = ((SrvPaddockPack) resultado).getSessaoCliente();
        assertTrue(sessaoCliente.isGuest());
        assertNotNull(sessaoCliente.getToken());
    }

    @Test
    void criarSessaoVisitante_chamadasSucessivasGeramNomesDiferentes() {
        SessaoCliente primeiro = ((SrvPaddockPack) controlePaddockServidor.criarSessaoVisitante()).getSessaoCliente();
        SessaoCliente segundo = ((SrvPaddockPack) controlePaddockServidor.criarSessaoVisitante()).getSessaoCliente();

        assertNotEquals(primeiro.getNomeJogador(), segundo.getNomeJogador());
    }

    // ---- criarSessaoGoogle ----

    @Test
    void criarSessaoGoogle_usuarioNovo_criaSessaoNaoGuest() {
        Object resultado = controlePaddockServidor.criarSessaoGoogle("g-123", "Fulano", "url-foto", "fulano@email.com");

        assertTrue(resultado instanceof SrvPaddockPack);
        SessaoCliente sessaoCliente = ((SrvPaddockPack) resultado).getSessaoCliente();
        assertEquals("g-123", sessaoCliente.getIdUsuario());
        assertEquals("Fulano", sessaoCliente.getNomeJogador());
        assertEquals(false, sessaoCliente.isGuest());
    }

    @Test
    void criarSessaoGoogle_idGoogleJaExistente_atualizaDadosDaMesmaSessao() {
        controlePaddockServidor.criarSessaoGoogle("g-123", "Fulano", "url-antiga", "fulano@email.com");

        Object resultado = controlePaddockServidor.criarSessaoGoogle("g-123", "Fulano Atualizado", "url-nova", "novo@email.com");

        SessaoCliente sessaoCliente = ((SrvPaddockPack) resultado).getSessaoCliente();
        assertEquals("Fulano Atualizado", sessaoCliente.getNomeJogador());
        assertEquals("url-nova", sessaoCliente.getImagemJogador());
        assertEquals(1, controlePaddockServidor.getDadosPaddock().getClientes().size());
    }

    // ---- obterSessaoPorToken ----

    @Test
    void obterSessaoPorToken_tokenExistente_retornaSessao() {
        SessaoCliente sessaoCriada = ((SrvPaddockPack) controlePaddockServidor.criarSessaoVisitante()).getSessaoCliente();

        SessaoCliente encontrada = controlePaddockServidor.obterSessaoPorToken(sessaoCriada.getToken());

        assertSame(sessaoCriada, encontrada);
    }

    @Test
    void obterSessaoPorToken_tokenInexistente_retornaNull() {
        assertNull(controlePaddockServidor.obterSessaoPorToken("token-que-nao-existe"));
    }

    // ---- renovarSessaoVisitante ----

    @Test
    void renovarSessaoVisitante_tokenExistente_geraNovoToken() {
        SessaoCliente sessaoCriada = ((SrvPaddockPack) controlePaddockServidor.criarSessaoVisitante()).getSessaoCliente();
        String tokenAntigo = sessaoCriada.getToken();

        Object resultado = controlePaddockServidor.renovarSessaoVisitante(tokenAntigo);

        assertTrue(resultado instanceof SrvPaddockPack);
        SessaoCliente sessaoRenovada = ((SrvPaddockPack) resultado).getSessaoCliente();
        assertNotEquals(tokenAntigo, sessaoRenovada.getToken());
    }

    @Test
    void renovarSessaoVisitante_tokenInexistente_criaNovaSessaoVisitante() {
        int totalAntes = controlePaddockServidor.getDadosPaddock().getClientes().size();

        Object resultado = controlePaddockServidor.renovarSessaoVisitante("token-que-nao-existe");

        assertTrue(resultado instanceof SrvPaddockPack);
        assertTrue(((SrvPaddockPack) resultado).getSessaoCliente().isGuest());
        assertEquals(totalAntes + 1, controlePaddockServidor.getDadosPaddock().getClientes().size());
    }
}
