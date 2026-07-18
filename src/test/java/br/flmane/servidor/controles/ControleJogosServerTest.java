package br.flmane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.flmane.entidades.Carro;
import br.flmane.entidades.Piloto;
import br.flmane.servidor.JogoServidor;
import br.flmane.servidor.entidades.TOs.DadosCriarJogo;
import br.flmane.servidor.entidades.TOs.SessaoCliente;

/**
 * Cobre os controles de pilotagem (giro de motor, agressividade, DRS/ERS,
 * box) em ControleJogosServer, via obterPilotoPorId. JogoServidor é mockado;
 * o "jogo em andamento" é simulado injetando diretamente no mapaJogosCriados.
 */
class ControleJogosServerTest {

    private static final String ID_PILOTO = "7";
    private static final String NOME_JOGO = "jogo1";

    private ControleJogosServer controleJogosServer;
    private SessaoCliente sessaoCliente;
    private Piloto piloto;

    @BeforeEach
    void setUp() {
        ControlePersistencia controlePersistencia = mock(ControlePersistencia.class);
        ControleCampeonatoServidor controleCampeonatoServidor = mock(ControleCampeonatoServidor.class);
        ControleClassificacao controleClassificacao = mock(ControleClassificacao.class);
        ControlePaddockServidor controlePaddockServidor = mock(ControlePaddockServidor.class);
        controleJogosServer = new ControleJogosServer(new br.flmane.servidor.entidades.TOs.DadosPaddock(),
                controleClassificacao, controleCampeonatoServidor, controlePersistencia, controlePaddockServidor);

        sessaoCliente = new SessaoCliente();
        sessaoCliente.setIdUsuario("usuario1");
        sessaoCliente.setJogoAtual(NOME_JOGO);

        piloto = new Piloto();
        piloto.setId(7);
        piloto.setCarro(new Carro());

        registrarJogoComPiloto(sessaoCliente, piloto);
    }

    private void registrarJogoComPiloto(SessaoCliente sessao, Piloto pilotoDoJogo) {
        JogoServidor jogoServidor = mock(JogoServidor.class);
        when(jogoServidor.getNomeJogoServidor()).thenReturn(NOME_JOGO);
        DadosCriarJogo dadosCriarJogo = mock(DadosCriarJogo.class);
        when(dadosCriarJogo.getIdPiloto()).thenReturn(pilotoDoJogo.getId());
        Map<String, DadosCriarJogo> mapJogadoresOnline = new HashMap<>();
        mapJogadoresOnline.put(sessao.getIdUsuario(), dadosCriarJogo);
        when(jogoServidor.getMapJogadoresOnline()).thenReturn(mapJogadoresOnline);
        when(jogoServidor.getPilotos()).thenReturn(List.of(pilotoDoJogo));

        Map<SessaoCliente, JogoServidor> mapaJogosCriados = new HashMap<>();
        mapaJogosCriados.put(sessao, jogoServidor);
        controleJogosServer.setMapaJogosCriados(mapaJogosCriados);
    }

    // ---- obterPilotoPorId: base de todos os controles de pilotagem ----

    @Test
    void obterPilotoPorId_pilotoRegistradoNoJogoAtual_retornaPiloto() {
        assertEquals(piloto, controleJogosServer.obterPilotoPorId(sessaoCliente, ID_PILOTO));
    }

    @Test
    void obterPilotoPorId_idPilotoNaoCorrespondeAoDaSessao_retornaNull() {
        assertNull(controleJogosServer.obterPilotoPorId(sessaoCliente, "999"));
    }

    @Test
    void obterPilotoPorId_sessaoSemJogoCorrespondente_retornaNull() {
        sessaoCliente.setJogoAtual("outro-jogo");

        assertNull(controleJogosServer.obterPilotoPorId(sessaoCliente, ID_PILOTO));
    }

    // ---- mudarGiroMotor ----

    @Test
    void mudarGiroMotor_pilotoValido_aplicaMudancaERetornaTrue() {
        Boolean resultado = controleJogosServer.mudarGiroMotor(sessaoCliente, ID_PILOTO, Carro.GIRO_MAX);

        assertTrue(resultado);
        assertEquals(Carro.GIRO_MAX_VAL, piloto.getCarro().getGiro());
        assertTrue(piloto.isAtivarDRS());
    }

    @Test
    void mudarGiroMotor_pilotoInexistente_retornaNull() {
        assertNull(controleJogosServer.mudarGiroMotor(sessaoCliente, "999", Carro.GIRO_MAX));
    }

    @Test
    void mudarGiroMotor_mesmoGiroDeAntes_retornaFalse() {
        // giro padrão já é GIRO_NOR
        Boolean resultado = controleJogosServer.mudarGiroMotor(sessaoCliente, ID_PILOTO, Carro.GIRO_NOR);

        assertFalse(resultado);
    }

    // ---- mudarAgressividadePiloto ----

    @Test
    void mudarAgressividadePiloto_valorValido_aplicaMudanca() {
        Boolean resultado = controleJogosServer.mudarAgressividadePiloto(sessaoCliente, ID_PILOTO, Piloto.AGRESSIVO);

        assertTrue(resultado);
        assertEquals(Piloto.AGRESSIVO, piloto.getModoPilotagem());
    }

    @Test
    void mudarAgressividadePiloto_valorInvalido_retornaFalseSemMudar() {
        String modoAntes = piloto.getModoPilotagem();

        Boolean resultado = controleJogosServer.mudarAgressividadePiloto(sessaoCliente, ID_PILOTO, "VALOR_INVALIDO");

        assertFalse(resultado);
        assertEquals(modoAntes, piloto.getModoPilotagem());
    }

    @Test
    void mudarAgressividadePiloto_pilotoInexistente_retornaFalse() {
        assertFalse(controleJogosServer.mudarAgressividadePiloto(sessaoCliente, "999", Piloto.AGRESSIVO));
    }

    // ---- boxPiloto: piloto inexistente não lança exceção ----

    @Test
    void boxPiloto_pilotoInexistente_retornaFalseSemExcecao() {
        Object resultado = controleJogosServer.boxPiloto(sessaoCliente, "999", true, "MACIO", 50, Carro.ASA_NORMAL);

        assertEquals(Boolean.FALSE, resultado);
    }
}
