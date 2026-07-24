package br.flmane.servidor.rest;

import io.netty.handler.codec.http.HttpMethod;

import br.flmane.servidor.entidades.persistencia.CampeonatoSrv;
import br.flmane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.flmane.servidor.entidades.TOs.CampeonatoTO;
import br.flmane.servidor.netty.Roteador;

/**
 * Registra, no {@link Roteador}, os mesmos paths/métodos/content-types que
 * antes eram declarados via anotações JAX-RS em {@link LetsRace} — inclusive
 * quais endpoints tinham {@code @Compress} (gzip). Faz a ponte entre o
 * {@code ContextoRequisicao} do Netty e os parâmetros tipados dos métodos de
 * {@link LetsRace}, que continuam iguais aos de antes (e testáveis do mesmo
 * jeito, sem HTTP).
 */
public final class RotasLetsRace {

    private static final String JSON = "application/json";
    private static final String IMG_JPG = "image/jpg";
    private static final String IMG_PNG = "image/png";
    private static final String HTML = "text/html;charset=UTF-8";

    private RotasLetsRace() {
    }

    public static Roteador criar(LetsRace letsRace) {
        Roteador roteador = new Roteador();

        roteador.registrar(HttpMethod.GET, "/renovarSessaoVisitante/{token}", JSON, false,
                ctx -> letsRace.renovarSessaoVisitante(ctx.path("token")));

        roteador.registrar(HttpMethod.GET, "/dadosToken", JSON, false,
                ctx -> letsRace.dadosToken(ctx.header("token")));

        roteador.registrar(HttpMethod.GET, "/criarSessaoGoogle", JSON, false,
                ctx -> letsRace.criarSessaoGoogle(ctx.header("idGoogle"),
                        ctx.header("nome"), ctx.header("urlFoto"), ctx.header("email")));

        roteador.registrar(HttpMethod.GET, "/criarSessaoVisitante", JSON, false,
                ctx -> letsRace.criarSessaoVisitante());

        roteador.registrar(HttpMethod.GET, "/criarSessaoNome", JSON, false,
                ctx -> letsRace.criarSessaoNome(ctx.header("nome")));

        roteador.registrar(HttpMethod.GET, "/circuito", JSON, true,
                ctx -> letsRace.circuito(ctx.query("nomeCircuito")));

        roteador.registrar(HttpMethod.GET, "/circuitoClassificacao/{arquivoCircuito}", JSON, true,
                ctx -> letsRace.circuitoClassificacao(ctx.path("arquivoCircuito")));

        roteador.registrar(HttpMethod.GET, "/temporadaClassificacao/{temporadaSelecionada}", JSON, true,
                ctx -> letsRace.temporadaClassificacao(ctx.path("temporadaSelecionada")));

        roteador.registrar(HttpMethod.GET, "/classificacaoGeral", JSON, true,
                ctx -> letsRace.classificacaoGeral());

        roteador.registrar(HttpMethod.GET, "/classificacaoEquipes", JSON, true,
                ctx -> letsRace.classificacaoEquipes());

        roteador.registrar(HttpMethod.GET, "/classificacaoCampeonato", JSON, true,
                ctx -> letsRace.classificacaoCampeonato());

        roteador.registrar(HttpMethod.GET, "/sairJogo/{nomeJogo}", JSON, false,
                ctx -> letsRace.sairJogo(ctx.header("token"), ctx.path("nomeJogo")));

        roteador.registrar(HttpMethod.GET, "/dadosParciais/{nomeJogo}/{idPiloto}", JSON, true,
                ctx -> letsRace.dadosParciais(ctx.header("token"), ctx.header("idioma"),
                        ctx.path("nomeJogo"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/dadosJogo", JSON, true,
                ctx -> letsRace.dadosJogo(ctx.header("token"), ctx.header("idioma"),
                        ctx.query("nomeJogo"), ctx.query("modoCarreira")));

        roteador.registrar(HttpMethod.GET, "/obterJogos", JSON, false,
                ctx -> letsRace.obterJogos());

        roteador.registrar(HttpMethod.GET, "/atualizarDadosVisao", JSON, false,
                ctx -> letsRace.atualizarDadosVisao());

        roteador.registrar(HttpMethod.GET, "/verificaServico", JSON, false,
                ctx -> letsRace.verificaServico());

        roteador.registrar(HttpMethod.GET,
                "/jogar/{temporada}/{idPiloto}/{circuito}/{numVoltas}/{tipoPneu}/{combustivel}/{asa}/{modoCarreira}",
                JSON, true,
                ctx -> letsRace.jogar(ctx.header("token"), ctx.header("idioma"),
                        ctx.path("temporada"), ctx.path("idPiloto"), ctx.path("circuito"),
                        ctx.path("numVoltas"), ctx.path("tipoPneu"), ctx.path("combustivel"),
                        ctx.path("asa"), ctx.path("modoCarreira")));

        roteador.registrar(HttpMethod.GET, "/jogarCampeonato/{tipoPneu}/{combustivel}/{asa}", JSON, true,
                ctx -> letsRace.jogarCampeonato(ctx.header("token"), ctx.header("idioma"),
                        ctx.path("tipoPneu"), ctx.path("combustivel"), ctx.path("asa")));

        roteador.registrar(HttpMethod.GET, "/circuitos", JSON, false,
                ctx -> letsRace.circuitos());

        roteador.registrar(HttpMethod.GET, "/circuitoJpg/{nmCircuito}", IMG_JPG, false,
                ctx -> letsRace.circuitoJpg(ctx.path("nmCircuito")));

        roteador.registrar(HttpMethod.GET, "/circuitoBg/{nmCircuito}", IMG_JPG, false,
                ctx -> letsRace.circuitoBg(ctx.path("nmCircuito")));

        roteador.registrar(HttpMethod.GET, "/circuitoMini/{nmCircuito}", IMG_PNG, false,
                ctx -> letsRace.circuitoMini(ctx.path("nmCircuito")));

        roteador.registrar(HttpMethod.GET, "/objetoPista/{nmCircuito}/{indice}", IMG_PNG, false,
                ctx -> letsRace.objetoPista(ctx.path("nmCircuito"), ctx.path("indice")));

        roteador.registrar(HttpMethod.GET, "/carroCima/{temporada}/{carro}", IMG_PNG, false,
                ctx -> letsRace.carroCimaTemporadaCarro(ctx.path("temporada"), ctx.path("carro")));

        roteador.registrar(HttpMethod.GET, "/carroCimaSemAreofolio/{temporada}/{carro}", IMG_PNG, false,
                ctx -> letsRace.carroCimaSemAreofolioTemporadaCarro(ctx.path("temporada"), ctx.path("carro")));

        roteador.registrar(HttpMethod.GET, "/capacete/{temporada}/{piloto}", IMG_PNG, false,
                ctx -> letsRace.capaceteTemporadaPiloto(ctx.path("temporada"), ctx.path("piloto")));

        roteador.registrar(HttpMethod.GET, "/carroLado/{temporada}/{carro}", IMG_PNG, false,
                ctx -> letsRace.carroLadoTemporadaCarro(ctx.path("temporada"), ctx.path("carro")));

        roteador.registrar(HttpMethod.GET, "/png/{recurso}", IMG_PNG, false,
                ctx -> letsRace.png(ctx.path("recurso")));

        roteador.registrar(HttpMethod.GET, "/png/{recurso}/{trasnparencia}", IMG_PNG, false,
                ctx -> letsRace.png(ctx.path("recurso"), ctx.path("trasnparencia")));

        roteador.registrar(HttpMethod.GET, "/temporadasPilotos", JSON, true,
                ctx -> letsRace.temporadasPilotos());

        roteador.registrar(HttpMethod.GET, "/temporadas/{temporada}", JSON, true,
                ctx -> letsRace.temporadasDefaults(ctx.path("temporada")));

        roteador.registrar(HttpMethod.GET, "/temporadas", JSON, true,
                ctx -> letsRace.temporadas());

        roteador.registrar(HttpMethod.GET, "/sobre", HTML, true,
                ctx -> letsRace.sobre());

        roteador.registrar(HttpMethod.GET, "/potenciaMotor/{potencia}/{idPiloto}", JSON, true,
                ctx -> letsRace.potenciaMotor(ctx.header("token"), ctx.path("potencia"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/agressividadePiloto/{agresividade}/{idPiloto}", JSON, true,
                ctx -> letsRace.agressividadePiloto(ctx.header("token"), ctx.path("agresividade"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/tracadoPiloto/{tracado}/{idPiloto}", JSON, true,
                ctx -> letsRace.tracadoPiloto(ctx.header("token"), ctx.path("tracado"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/drsPiloto/{idPiloto}", JSON, true,
                ctx -> letsRace.drsPiloto(ctx.header("token"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/ersPiloto/{idPiloto}", JSON, true,
                ctx -> letsRace.ersPiloto(ctx.header("token"), ctx.path("idPiloto")));

        roteador.registrar(HttpMethod.GET, "/boxPiloto/{idPiloto}/{ativa}/{pneu}/{combustivel}/{asa}", JSON, true,
                ctx -> letsRace.boxPiloto(ctx.header("token"), ctx.path("idPiloto"),
                        Boolean.valueOf(ctx.path("ativa")), ctx.path("pneu"),
                        Integer.valueOf(ctx.path("combustivel")), ctx.path("asa")));

        roteador.registrar(HttpMethod.GET, "/lang/{lang}", JSON, true,
                ctx -> letsRace.lang(ctx.path("lang")));

        roteador.registrar(HttpMethod.GET, "/equipe", JSON, true,
                ctx -> letsRace.equipe(ctx.header("token"), ctx.header("idioma")));

        roteador.registrar(HttpMethod.GET, "/equipePilotoCarro", JSON, true,
                ctx -> letsRace.equipePilotoCarro(ctx.header("token"), ctx.header("idioma")));

        roteador.registrar(HttpMethod.POST, "/equipe", JSON, true,
                ctx -> letsRace.gravarEquipe(ctx.header("token"), ctx.header("idioma"),
                        ctx.corpoComo(CarreiraDadosSrv.class)));

        roteador.registrar(HttpMethod.POST, "/campeonato", JSON, true,
                ctx -> letsRace.gravarCampeonato(ctx.header("token"), ctx.header("idioma"),
                        ctx.corpoComo(CampeonatoSrv.class)));

        roteador.registrar(HttpMethod.POST, "/finalizaCampeonato", JSON, true,
                ctx -> letsRace.finalizaCampeonato(ctx.header("token"), ctx.header("idioma"),
                        ctx.corpoComo(CampeonatoTO.class)));

        roteador.registrar(HttpMethod.GET, "/campeonato", JSON, true,
                ctx -> letsRace.campeonato(ctx.header("token"), ctx.header("idioma")));

        roteador.registrar(HttpMethod.GET, "/campeonato/{id}", JSON, true,
                ctx -> letsRace.campeonatoPorId(ctx.path("id"), ctx.header("token"), ctx.header("idioma")));

        return roteador;
    }
}
