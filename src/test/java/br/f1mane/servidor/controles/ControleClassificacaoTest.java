package br.f1mane.servidor.controles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.servidor.entidades.TOs.ErroServ;
import br.f1mane.servidor.entidades.TOs.MsgSrv;
import br.f1mane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.servidor.entidades.persistencia.JogadorDadosSrv;

import java.util.List;
import java.util.Map;

/**
 * Cobre as regras de pontuação e validação de carreira de ControleClassificacao.
 * ControlePersistencia e CarregadorRecursos são mockados via o construtor
 * pacote-privado de injeção; nenhum banco real nem classpath real é tocado.
 */
class ControleClassificacaoTest {

    private ControlePersistencia controlePersistencia;
    private CarregadorRecursos carregadorRecursos;
    private ControleClassificacao controleClassificacao;

    @BeforeEach
    void setUp() {
        controlePersistencia = mock(ControlePersistencia.class);
        carregadorRecursos = mock(CarregadorRecursos.class);
        ControleCampeonatoServidor controleCampeonatoServidor = mock(ControleCampeonatoServidor.class);
        controleClassificacao = new ControleClassificacao(controlePersistencia, controleCampeonatoServidor,
                carregadorRecursos);
    }

    private Piloto pilotoNaPosicao(int posicao) {
        Piloto piloto = new Piloto();
        piloto.setPosicao(posicao);
        return piloto;
    }

    // ---- gerarPontos: tabela de pontuação por posição ----

    @ParameterizedTest(name = "posicao {0} vale {1} pontos")
    @CsvSource({
            "1, 25", "2, 18", "3, 15", "4, 12", "5, 10",
            "6, 8", "7, 6", "8, 4", "9, 2", "10, 1",
            "11, 0", "20, 0"
    })
    void gerarPontos_seguePorPosicao(int posicao, int pontosEsperados) {
        assertEquals(pontosEsperados, controleClassificacao.gerarPontos(pilotoNaPosicao(posicao)));
    }

    // ---- validadeDistribuicaoPontos: orquestra Util.processaValorPontosCarreira ----

    private CarreiraDadosSrv carreiraComBase(int construtores, int aero, int carro, int freio, int piloto) {
        CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
        carreiraDadosSrv.setPtsConstrutores(construtores);
        carreiraDadosSrv.setPtsAerodinamica(aero);
        carreiraDadosSrv.setPtsCarro(carro);
        carreiraDadosSrv.setPtsFreio(freio);
        carreiraDadosSrv.setPtsPiloto(piloto);
        return carreiraDadosSrv;
    }

    @Test
    void validadeDistribuicaoPontos_upgradeUnico_debitaCustoDaFaixa() {
        CarreiraDadosSrv base = carreiraComBase(1000, 100, 100, 100, 100);

        int saldo = ControleClassificacao.validadeDistribuicaoPontos(base, 101, 100, 100, 100);

        assertEquals(999, saldo);
    }

    @Test
    void validadeDistribuicaoPontos_downgradeUnico_creditaRefundDaFaixa() {
        CarreiraDadosSrv base = carreiraComBase(1000, 100, 100, 100, 100);

        int saldo = ControleClassificacao.validadeDistribuicaoPontos(base, 99, 100, 100, 100);

        assertEquals(1001, saldo);
    }

    @Test
    void validadeDistribuicaoPontos_mudancaSimultaneaEmMultiplosAtributos_somaCustosERefunds() {
        // aerodinamica sobe 1 (custo 1), carro sobe 1 (custo 1), freio desce 1 (refund 1)
        CarreiraDadosSrv base = carreiraComBase(1000, 100, 100, 100, 100);

        int saldo = ControleClassificacao.validadeDistribuicaoPontos(base, 101, 101, 99, 100);

        assertEquals(1000 - 1 - 1 + 1, saldo);
    }

    @Test
    void validadeDistribuicaoPontos_upgrade998Para999_debita50Pontos() {
        CarreiraDadosSrv base = carreiraComBase(1000, 998, 100, 100, 100);

        int saldo = ControleClassificacao.validadeDistribuicaoPontos(base, 999, 100, 100, 100);

        assertEquals(950, saldo);
    }

    // ---- atualizaCarreira: validação antes de gravar ----

    private CarreiraDadosSrv carreiraValida() {
        CarreiraDadosSrv carreiraDados = new CarreiraDadosSrv();
        carreiraDados.setNomeCarro("Carro Teste");
        carreiraDados.setNomePiloto("Piloto Teste");
        carreiraDados.setNomePilotoAbreviado("PTE");
        carreiraDados.setPtsAerodinamica(100);
        carreiraDados.setPtsCarro(100);
        carreiraDados.setPtsFreio(100);
        carreiraDados.setPtsPiloto(100);
        return carreiraDados;
    }

    private void stubCarreiraExistente(int construtoresBase) {
        CarreiraDadosSrv carreiraExistente = carreiraComBase(construtoresBase, 100, 100, 100, 100);
        JogadorDadosSrv jogadorDadosSrv = mock(JogadorDadosSrv.class);
        when(jogadorDadosSrv.getId()).thenReturn(1L);
        carreiraExistente.setJogadorDadosSrv(jogadorDadosSrv);
        Session session = mock(Session.class);
        when(session.isOpen()).thenReturn(true);
        when(controlePersistencia.getSession()).thenReturn(session);
        when(controlePersistencia.carregaCarreiraJogador(eq("usuario1"), eq(false), any(Session.class)))
                .thenReturn(carreiraExistente);
    }

    @Test
    void atualizaCarreira_nomeCarroVazio_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        CarreiraDadosSrv carreiraDados = carreiraValida();
        carreiraDados.setNomeCarro("");

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraDados);

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void atualizaCarreira_nomeCarroMuitoLongo_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        CarreiraDadosSrv carreiraDados = carreiraValida();
        carreiraDados.setNomeCarro("Um Nome De Carro Com Mais De Vinte Caracteres");

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraDados);

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void atualizaCarreira_nomeCarroDuplicado_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomeCarro(any(Session.class), eq("Carro Teste"), anyLong()))
                .thenReturn(true);

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraValida());

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void atualizaCarreira_nomePilotoDuplicado_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomePiloto(any(Session.class), eq("Piloto Teste"), anyLong()))
                .thenReturn(true);

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraValida());

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void atualizaCarreira_dadosValidos_gravaERetornaSucesso() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomeCarro(any(Session.class), any(), anyLong())).thenReturn(false);
        when(controlePersistencia.existeNomePiloto(any(Session.class), any(), anyLong())).thenReturn(false);

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraValida());

        verify(controlePersistencia).gravarDados(any(Session.class), any(CarreiraDadosSrv.class));
        assertFalse(resultado instanceof ErroServ);
    }

    // ---- atualizaCarreira: validação de livery/capacete (CarregadorRecursos mockado) ----

    private Piloto pilotoModeloComCarro(int id, int habilidadeReal, int carroId, int potenciaReal, int aerodinamica,
                                        int freios) {
        Piloto piloto = new Piloto();
        piloto.setId(id);
        piloto.setHabilidadeReal(habilidadeReal);
        Carro carro = new Carro();
        carro.setId(carroId);
        carro.setPotenciaReal(potenciaReal);
        carro.setAerodinamica(aerodinamica);
        carro.setFreios(freios);
        piloto.setCarro(carro);
        return piloto;
    }

    @Test
    void atualizaCarreira_capaceteDeNivelMaisAltoQueOJogador_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomeCarro(any(Session.class), any(), anyLong())).thenReturn(false);
        when(controlePersistencia.existeNomePiloto(any(Session.class), any(), anyLong())).thenReturn(false);
        Piloto pilotoModelo = pilotoModeloComCarro(5, 999, 1, 0, 0, 0);
        when(carregadorRecursos.carregarTemporadasPilotos()).thenReturn(Map.of("t2024", List.of(pilotoModelo)));

        CarreiraDadosSrv carreiraDados = carreiraValida();
        carreiraDados.setTemporadaCapaceteLivery(2024);
        carreiraDados.setIdCapaceteLivery(5);
        carreiraDados.setPtsPiloto(100); // muito abaixo da habilidadeReal (999) do piloto modelo

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraDados);

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }

    @Test
    void atualizaCarreira_capaceteDeNivelCompativel_naoBloqueia() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomeCarro(any(Session.class), any(), anyLong())).thenReturn(false);
        when(controlePersistencia.existeNomePiloto(any(Session.class), any(), anyLong())).thenReturn(false);
        Piloto pilotoModelo = pilotoModeloComCarro(5, 50, 1, 0, 0, 0);
        when(carregadorRecursos.carregarTemporadasPilotos()).thenReturn(Map.of("t2024", List.of(pilotoModelo)));

        CarreiraDadosSrv carreiraDados = carreiraValida();
        carreiraDados.setTemporadaCapaceteLivery(2024);
        carreiraDados.setIdCapaceteLivery(5);
        carreiraDados.setPtsPiloto(100); // igual ou acima da habilidadeReal (50) do piloto modelo

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraDados);

        verify(controlePersistencia).gravarDados(any(Session.class), any(CarreiraDadosSrv.class));
        assertFalse(resultado instanceof ErroServ);
    }

    @Test
    void atualizaCarreira_carroDeNivelMaisAltoQueOJogador_retornaMsgSrvSemGravar() throws Exception {
        stubCarreiraExistente(1000);
        when(controlePersistencia.existeNomeCarro(any(Session.class), any(), anyLong())).thenReturn(false);
        when(controlePersistencia.existeNomePiloto(any(Session.class), any(), anyLong())).thenReturn(false);
        Piloto pilotoModelo = pilotoModeloComCarro(1, 0, 7, 999, 0, 0);
        when(carregadorRecursos.carregarTemporadasPilotos()).thenReturn(Map.of("t2024", List.of(pilotoModelo)));

        CarreiraDadosSrv carreiraDados = carreiraValida();
        carreiraDados.setTemporadaCarroLivery(2024);
        carreiraDados.setIdCarroLivery(7);
        carreiraDados.setPtsCarro(100); // muito abaixo da potenciaReal (999) do carro modelo

        Object resultado = controleClassificacao.atualizaCarreira("usuario1", carreiraDados);

        assertTrue(resultado instanceof MsgSrv);
        verify(controlePersistencia, never()).gravarDados(any(), any());
    }
}
