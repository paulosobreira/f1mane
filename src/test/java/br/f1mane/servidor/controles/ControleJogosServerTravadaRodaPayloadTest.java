package br.f1mane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.No;
import br.f1mane.entidades.Piloto;
import br.f1mane.servidor.JogoServidor;
import br.f1mane.servidor.entidades.TOs.PosisPack;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;
import br.f1mane.servidor.entidades.TOs.TravadaRoda;

/**
 * ControleJogosServer.obterPosicaoPilotos deve incluir, no payload
 * codificado, o snapshot completo das marcas de pneu (TravadaRoda)
 * acumuladas em JogoServidor.getMarcasPneuGeradas() — não um delta desde o
 * último poll, já que múltiplos clientes fazem polling independente do
 * mesmo jogo (ver design.md da mudança
 * corrigir-background-marcas-pneu-web).
 */
class ControleJogosServerTravadaRodaPayloadTest {

    private static final String NOME_JOGO = "jogo1";

    private ControleJogosServer criarControleJogosServer(JogoServidor jogoServidor) {
        ControlePersistencia controlePersistencia = mock(ControlePersistencia.class);
        ControleCampeonatoServidor controleCampeonatoServidor = mock(ControleCampeonatoServidor.class);
        ControleClassificacao controleClassificacao = mock(ControleClassificacao.class);
        ControlePaddockServidor controlePaddockServidor = mock(ControlePaddockServidor.class);
        ControleJogosServer controleJogosServer = new ControleJogosServer(
                new br.f1mane.servidor.entidades.TOs.DadosPaddock(), controleClassificacao,
                controleCampeonatoServidor, controlePersistencia, controlePaddockServidor);

        SessaoCliente sessaoCliente = new SessaoCliente();
        sessaoCliente.setIdUsuario("usuario1");
        Map<SessaoCliente, JogoServidor> mapaJogosCriados = new HashMap<>();
        mapaJogosCriados.put(sessaoCliente, jogoServidor);
        controleJogosServer.setMapaJogosCriados(mapaJogosCriados);
        return controleJogosServer;
    }

    private JogoServidor criarJogoServidorComPiloto(Piloto piloto, No noAtual,
            List<TravadaRoda> marcasPneuGeradas) {
        JogoServidor jogoServidor = mock(JogoServidor.class);
        when(jogoServidor.getNomeJogoServidor()).thenReturn(NOME_JOGO);
        Map<No, Integer> mapaNosIds = new HashMap<>();
        mapaNosIds.put(noAtual, 11);
        when(jogoServidor.getMapaNosIds()).thenReturn(mapaNosIds);
        when(jogoServidor.getPilotosCopia()).thenReturn(List.of(piloto));
        when(jogoServidor.getMarcasPneuGeradas()).thenReturn(marcasPneuGeradas);
        return jogoServidor;
    }

    private Piloto criarPiloto(No noAtual) {
        Piloto piloto = new Piloto();
        piloto.setId(7);
        piloto.setCarro(new Carro());
        piloto.setNoAtual(noAtual);
        return piloto;
    }

    @Test
    void payloadIncluiMarcasDePneuAcumuladasNoBackend() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        Piloto piloto = criarPiloto(no);

        TravadaRoda marca = new TravadaRoda();
        marca.setIdNo(11);
        marca.setTracado(2);

        JogoServidor jogoServidor = criarJogoServidorComPiloto(piloto, no, List.of(marca));
        ControleJogosServer controleJogosServer = criarControleJogosServer(jogoServidor);

        Object resultado = controleJogosServer.obterPosicaoPilotos(NOME_JOGO);

        assertNotNull(resultado, "com um piloto registrado, o payload não deveria ser nulo");
        PosisPack pack = new PosisPack();
        pack.decode((String) resultado);

        assertEquals(1, pack.getTravadaRodas().length);
        assertEquals(11, pack.getTravadaRodas()[0].getIdNo());
        assertEquals(2, pack.getTravadaRodas()[0].getTracado());
    }

    @Test
    void payloadSemMarcasDePneuAcumuladasTemListaVazia() {
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        Piloto piloto = criarPiloto(no);

        JogoServidor jogoServidor = criarJogoServidorComPiloto(piloto, no, List.of());
        ControleJogosServer controleJogosServer = criarControleJogosServer(jogoServidor);

        Object resultado = controleJogosServer.obterPosicaoPilotos(NOME_JOGO);

        assertNotNull(resultado);
        PosisPack pack = new PosisPack();
        pack.decode((String) resultado);

        assertEquals(0, pack.getTravadaRodas().length);
    }

    @Test
    void marcaJaAcumuladaContinuaNoSnapshotEmPollsSeguintes() {
        // Simula dois polls independentes (dois clientes, ou o mesmo cliente duas vezes):
        // a marca já produzida continua aparecendo em ambos, não é "consumida" no primeiro poll.
        No no = new No();
        no.setIndex(10);
        no.setTipo(No.RETA);
        Piloto piloto = criarPiloto(no);

        TravadaRoda marca = new TravadaRoda();
        marca.setIdNo(11);
        marca.setTracado(1);

        JogoServidor jogoServidor = criarJogoServidorComPiloto(piloto, no, List.of(marca));
        ControleJogosServer controleJogosServer = criarControleJogosServer(jogoServidor);

        PosisPack primeiroPoll = new PosisPack();
        primeiroPoll.decode((String) controleJogosServer.obterPosicaoPilotos(NOME_JOGO));
        PosisPack segundoPoll = new PosisPack();
        segundoPoll.decode((String) controleJogosServer.obterPosicaoPilotos(NOME_JOGO));

        assertEquals(1, primeiroPoll.getTravadaRodas().length);
        assertEquals(1, segundoPoll.getTravadaRodas().length,
                "um segundo poll (outro cliente) ainda deveria ver a marca já produzida");
    }
}
