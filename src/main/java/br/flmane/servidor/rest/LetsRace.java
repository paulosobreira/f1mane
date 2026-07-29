package br.flmane.servidor.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import br.flmane.servidor.controles.ControleJogosServer;
import br.flmane.servidor.controles.ControlePaddockServidor;
import br.flmane.servidor.netty.RespostaHttp;
import br.nnpe.Global;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.flmane.controles.ControleRecursos;
import br.flmane.controles.InterfaceJogo;
import br.flmane.entidades.Circuito;
import br.flmane.entidades.CircuitosDefault;
import br.flmane.entidades.DesenhoProceduralCircuito;
import br.flmane.entidades.TemporadasDefault;
import br.flmane.servidor.PaddockServer;
import br.flmane.servidor.entidades.TOs.CampeonatoTO;
import br.flmane.servidor.entidades.TOs.ClientPaddockPack;
import br.flmane.servidor.entidades.TOs.DadosJogo;
import br.flmane.servidor.entidades.TOs.DadosParciais;
import br.flmane.servidor.entidades.TOs.ErroServ;
import br.flmane.servidor.entidades.TOs.MsgSrv;
import br.flmane.servidor.entidades.TOs.SessaoCliente;
import br.flmane.servidor.entidades.TOs.SrvPaddockPack;
import br.flmane.servidor.entidades.persistencia.CampeonatoSrv;
import br.flmane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.flmane.recursos.CarregadorRecursos;
import br.flmane.recursos.ImagensHeadlessDisco;
import br.flmane.recursos.idiomas.Lang;
import br.flmane.visao.PainelCircuito;

/**
 * Endpoints do modo multiplayer (paths, params e content-types iguais aos
 * de antes) — despachados pelo router Netty em {@link RotasLetsRace}, sem
 * depender de anotações JAX-RS/Jersey.
 */
public class LetsRace {

    private static final String APPLICATION_JSON = "application/json";

    private final CarregadorRecursos carregadorRecursos;
    private final ControlePaddockServidor controlePaddock;

    public LetsRace() {
        this(CarregadorRecursos.getCarregadorRecursos(false),
                PaddockServer.getControlePaddock());
    }

    LetsRace(CarregadorRecursos carregadorRecursos,
             ControlePaddockServidor controlePaddock) {
        this.carregadorRecursos = carregadorRecursos;
        this.controlePaddock = controlePaddock;
    }

    private RespostaHttp processsaMensagem(Object objeto, String idioma) {
        if (objeto == null) {
            return RespostaHttp.status(500).entity(new MsgSrv("Server error."))
                    .tipo(APPLICATION_JSON).build();
        }
        if (objeto instanceof MsgSrv) {
            MsgSrv msgSrv = (MsgSrv) objeto;
            return RespostaHttp.status(400)
                    .entity(new MsgSrv(Lang
                            .decodeTextoKey(msgSrv.getMessageString(), idioma)))
                    .tipo(APPLICATION_JSON).build();
        }
        if (objeto instanceof ErroServ) {
            ErroServ erroServ = (ErroServ) objeto;
            return RespostaHttp.status(500)
                    .entity(new MsgSrv(erroServ.obterErroFormatado()))
                    .tipo(APPLICATION_JSON).build();
        }
        return null;
    }

    public RespostaHttp renovarSessaoVisitante(String token) {
        return RespostaHttp.status(200)
                .entity(controlePaddock.renovarSessaoVisitante(token)).build();
    }

    public RespostaHttp dadosToken(String token) {
        SrvPaddockPack obterDadosToken = controlePaddock.obterDadosToken(token);
        if (obterDadosToken != null
                && obterDadosToken.getSessaoCliente() != null) {
            obterDadosToken.getSessaoCliente()
                    .setUlimaAtividade(System.currentTimeMillis());
            return RespostaHttp.status(200).entity(obterDadosToken).build();
        }
        return RespostaHttp.status(404).build();
    }

    public RespostaHttp criarSessaoGoogle(String idGoogle, String nome,
                                           String urlFoto, String email) {
        Object criarSessaoGoogle = controlePaddock.criarSessaoGoogle(idGoogle,
                nome, urlFoto, email);
        if (criarSessaoGoogle instanceof ErroServ) {
            return RespostaHttp.status(500).entity(criarSessaoGoogle).build();
        } else {
            return RespostaHttp.status(200).entity(criarSessaoGoogle).build();
        }
    }

    public RespostaHttp criarSessaoVisitante() {
        return RespostaHttp.status(200)
                .entity(controlePaddock.criarSessaoVisitante()).build();
    }

    public RespostaHttp criarSessaoNome(String nome) {
        Object criarSessaoNome = controlePaddock.criarSessaoNome(nome);
        if (criarSessaoNome instanceof ErroServ) {
            return RespostaHttp.status(500).entity(criarSessaoNome).build();
        } else {
            return RespostaHttp.status(200).entity(criarSessaoNome).build();
        }
    }

    public RespostaHttp circuito(String nomeCircuito) {
        Logger.logar("String nomeCircuito " + nomeCircuito);
        Circuito circuito;
        try {
            circuito = CarregadorRecursos.carregarCircuito(nomeCircuito);
            if (circuito != null) {
                circuito.vetorizarPista();
                circuito.gerarObjetosNoTransparencia();
            }
        } catch (Exception e) {
            Logger.logarExept(e);
            ErroServ erroServ = new ErroServ(e);
            return RespostaHttp.status(500).entity(erroServ)
                    .tipo(APPLICATION_JSON).build();
        }
        if (circuito == null) {
            return RespostaHttp.status(404).build();
        }
        return RespostaHttp.status(200).entity(circuito).build();
    }

    public RespostaHttp circuitoClassificacao(String arquivoCircuito) {
        return RespostaHttp.status(200)
                .entity(controlePaddock
                        .obterClassificacaoCircuito(ControleRecursos
                                .nomeArquivoCircuitoParaPista(arquivoCircuito)))
                .build();
    }

    public RespostaHttp temporadaClassificacao(String temporadaSelecionada) {
        return RespostaHttp.status(200)
                .entity(controlePaddock
                        .obterClassificacaoTemporada(temporadaSelecionada))
                .build();
    }

    public RespostaHttp classificacaoGeral() {
        return RespostaHttp.status(200)
                .entity(controlePaddock.obterClassificacaoGeral()).build();
    }

    public RespostaHttp classificacaoEquipes() {
        return RespostaHttp.status(200)
                .entity(controlePaddock.obterClassificacaoEquipes()).build();
    }

    public RespostaHttp classificacaoCampeonato() {
        return RespostaHttp.status(200)
                .entity(controlePaddock.obterClassificacaoCampeonato()).build();
    }

    public RespostaHttp sairJogo(String token, String nomeJogo) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        controlePaddock.sairJogoToken(nomeJogo, token, sessaoCliente);
        return RespostaHttp.status(200).build();
    }

    public RespostaHttp dadosParciais(String token, String idioma,
                                       String nomeJogo, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        DadosParciais dadosParciais = controlePaddock.obterDadosParciaisPilotos(
                nomeJogo, sessaoCliente.getIdUsuario(), idPiloto);
        if (dadosParciais == null) {
            return RespostaHttp.status(401).build();
        }
        try {
            dadosParciais.texto = Lang.decodeTextoKey(dadosParciais.texto,
                    idioma);
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return RespostaHttp.status(200).entity(dadosParciais).build();
    }

    public RespostaHttp dadosJogo(String token, String idioma,
                                   String nomeJogo, String modoCarreira) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        DadosJogo dadosJogo = null;
        try {
            ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
            clientPaddockPack.setNomeJogo(nomeJogo);
            clientPaddockPack.setSessaoCliente(sessaoCliente);
            Object dadosJogoObj = controlePaddock
                    .obterDadosJogo(clientPaddockPack);
            dadosJogo = (DadosJogo) dadosJogoObj;
            if (dadosJogoObj == null) {
                if (!controlePaddock.obterJogos().isEmpty()) {
                    String jogo = controlePaddock.obterJogos().get(0);
                    clientPaddockPack = new ClientPaddockPack();
                    clientPaddockPack.setNomeJogo(jogo);
                    clientPaddockPack.setSessaoCliente(sessaoCliente);
                    dadosJogoObj = controlePaddock
                            .obterDadosJogo(clientPaddockPack);
                    if ("true".equals(modoCarreira) && controlePaddock
                            .obterJogoPeloNome(jogo).isCorridaIniciada()) {
                        dadosJogoObj = null;
                    }
                }
                if (dadosJogoObj == null) {
                    DadosJogo nenhum = new DadosJogo();
                    nenhum.setEstado("NENHUM");
                    dadosJogoObj = nenhum;
                }
                dadosJogo = (DadosJogo) dadosJogoObj;
            }
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return RespostaHttp.status(200).entity(dadosJogo).build();
    }

    public RespostaHttp obterJogos() {
        return RespostaHttp.status(200).entity(controlePaddock.obterJogos())
                .build();
    }

    public RespostaHttp atualizarDadosVisao() {
        return RespostaHttp.status(200)
                .entity(controlePaddock.atualizarDadosVisao()).build();
    }

    public RespostaHttp verificaServico() {
        return RespostaHttp.status(200).entity("ok").build();
    }

    public RespostaHttp jogar(String token, String idioma, String temporada,
                               String idPiloto, String circuito, String numVoltas,
                               String tipoPneu, String combustivel, String asa,
                               String modoCarreira) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        Object jogar = controlePaddock.jogar(temporada, circuito, idPiloto,
                numVoltas, tipoPneu, combustivel, asa, sessaoCliente,
                modoCarreira);
        RespostaHttp processsaMensagem = processsaMensagem(jogar, idioma);
        if (processsaMensagem != null) {
            return processsaMensagem;
        } else {
            return RespostaHttp.status(200).entity(jogar).build();
        }
    }

    public RespostaHttp jogarCampeonato(String token, String idioma,
                                         String tipoPneu, String combustivel, String asa) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        Object jogar = controlePaddock.jogarCampeonato(tipoPneu, combustivel,
                asa, sessaoCliente);
        RespostaHttp processsaMensagem = processsaMensagem(jogar, idioma);
        if (processsaMensagem != null) {
            return processsaMensagem;
        } else {
            return RespostaHttp.status(200).entity(jogar).build();
        }
    }

    public RespostaHttp circuitos() {
        List<CircuitosDefault> circuitosDefauts;
        try {
            circuitosDefauts = carregadorRecursos.carregarCircuitosDefaults();
            List<CircuitosDefault> shuffle = new ArrayList<>(circuitosDefauts);
            Collections.shuffle(shuffle);
            return RespostaHttp.status(200).entity(shuffle).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp circuitoJpg(String nmCircuito) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco()
                    && Global.MODO_HOMENAGEM && nmCircuito.endsWith(".jpg")) {
                String nomeArquivoCircuito = nomeXmlDoCircuito(nmCircuito);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCircuitoFundo(nmCircuito), "jpg",
                        () -> gerarFundoCircuito(nomeArquivoCircuito));
            } else {
                BufferedImage buffer;
                if (Global.MODO_HOMENAGEM && nmCircuito.endsWith(".jpg")) {
                    String nomeArquivoCircuito = nomeXmlDoCircuito(nmCircuito);
                    Circuito circuito = CarregadorRecursos.carregarCircuito(nomeArquivoCircuito);
                    circuito.vetorizarPista();
                    buffer = DesenhoProceduralCircuito.geraImagem(circuito);
                } else {
                    buffer = CarregadorRecursos.carregaBufferedImage("circuitos/" + nmCircuito);
                }
                imageData = buffer == null ? null : codificarJpg(buffer);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData)
                    .header("Cache-Control", "max-age=120")
                    .build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp circuitoBg(String nmCircuito) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco() && nmCircuito.endsWith(".jpg")) {
                String nomeArquivoCircuito = nomeXmlDoCircuito(nmCircuito);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCircuitoFundo(nmCircuito), "jpg",
                        () -> gerarFundoCircuito(nomeArquivoCircuito));
            } else {
                String nomeArquivoCircuito = nmCircuito.replace("jpg", "flmane");
                Circuito circuito = CarregadorRecursos.carregarCircuito(nomeArquivoCircuito);
                circuito.vetorizarPista();
                InterfaceJogo jogo = null;
                if (controlePaddock.obterJogos() != null
                        && !controlePaddock.obterJogos().isEmpty()) {
                    jogo = controlePaddock
                            .obterJogoPeloNome(controlePaddock.obterJogos().get(0));
                }

                PainelCircuito painelCircuito = new PainelCircuito(circuito, jogo);
                BufferedImage bg = painelCircuito.desenhaCircuito();
                imageData = bg == null ? null : codificarJpg(bg);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp circuitoMini(String nmCircuito) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco()) {
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCircuitoMini(nmCircuito), "png",
                        () -> gerarMiniCircuito(nmCircuito));
            } else {
                Circuito circuito = CarregadorRecursos.carregarCircuito(nmCircuito);
                BufferedImage mini = circuito.desenhaMiniCircuito();
                imageData = mini == null ? null : codificarPng(mini);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    /**
     * Deriva o nome do XML do circuito a partir do nome do jpg de fundo
     * (convenção de {@code CarregadorRecursos.aplicarBackGroundPorConvencao}:
     * {@code <nome>.xml -> <nome>.jpg}).
     */
    private static String nomeXmlDoCircuito(String nomeJpg) {
        return nomeJpg.substring(0, nomeJpg.length() - ".jpg".length()) + ".xml";
    }

    /**
     * Gera a imagem de fundo do circuito e libera seus objetos de desenho
     * em seguida — usado tanto pela pré-geração preguiçosa de
     * {@code circuitoJpg}/{@code circuitoBg} quanto por qualquer chamador
     * que precise da mesma imagem servida no boot headless.
     */
    private static BufferedImage gerarFundoCircuito(String nomeArquivoXml) throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(nomeArquivoXml);
        BufferedImage imagem = DesenhoProceduralCircuito.geraImagem(circuito);
        circuito.liberarObjetosDesenho();
        return imagem;
    }

    private static BufferedImage gerarMiniCircuito(String nomeArquivoXml) throws Exception {
        Circuito circuito = CarregadorRecursos.carregarCircuito(nomeArquivoXml);
        BufferedImage imagem = circuito.desenhaMiniCircuito();
        circuito.liberarObjetosDesenho();
        return imagem;
    }

    private static byte[] codificarJpg(BufferedImage imagem) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagem, "jpg", baos);
        return baos.toByteArray();
    }

    private static byte[] codificarPng(BufferedImage imagem) throws java.io.IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imagem, "png", baos);
        return baos.toByteArray();
    }

    public RespostaHttp objetoPista(String nmCircuito, String indice) {
        try {
            Circuito circuito = CarregadorRecursos.carregarCircuito(nmCircuito);
            BufferedImage carroCima = circuito.desenhaObjetoPista(indice);
            if (carroCima == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(carroCima, "png", baos);
            byte[] imageData = baos.toByteArray();
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    /**
     * {@code temporada}/{@code identificador} de 6 caracteres cada é a
     * variante de cor customizada (hex), usada em telas de carreira para
     * pré-visualizar combinações de cor não enumeráveis de antemão — nunca
     * passa pelo disco pré-gerado (só o par temporada-ano/id-numérico faz),
     * mesmo em modo headless.
     */
    private static boolean isVarianteCorCustomizada(String temporada, String identificador) {
        return temporada != null && temporada.length() == 6
                && identificador != null && identificador.length() == 6;
    }

    public RespostaHttp carroCimaTemporadaCarro(String temporada, String carro) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco() && !isVarianteCorCustomizada(temporada, carro)) {
                int idCarro = Util.intOr0(carro);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCarroCima(temporada, idCarro, false), "png",
                        () -> controlePaddock.carroCimaTemporadaCarro(temporada, carro));
            } else {
                BufferedImage carroCima = controlePaddock
                        .carroCimaTemporadaCarro(temporada, carro);
                imageData = carroCima == null ? null : codificarPng(carroCima);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp carroCimaSemAreofolioTemporadaCarro(String temporada, String carro) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco() && !isVarianteCorCustomizada(temporada, carro)) {
                int idCarro = Util.intOr0(carro);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCarroCima(temporada, idCarro, true), "png",
                        () -> controlePaddock.carroCimaSemAreofolioTemporadaCarro(temporada, carro));
            } else {
                BufferedImage carroCima = controlePaddock
                        .carroCimaSemAreofolioTemporadaCarro(temporada, carro);
                imageData = carroCima == null ? null : codificarPng(carroCima);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp capaceteTemporadaPiloto(String temporada, String piloto) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco() && !isVarianteCorCustomizada(temporada, piloto)) {
                int idPiloto = Util.intOr0(piloto);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCapacete(temporada, idPiloto), "png",
                        () -> controlePaddock.capaceteTemporadaPiloto(temporada, piloto));
            } else {
                BufferedImage capacete = controlePaddock
                        .capaceteTemporadaPiloto(temporada, piloto);
                imageData = capacete == null ? null : codificarPng(capacete);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp carroLadoTemporadaCarro(String temporada, String carro) {
        try {
            byte[] imageData;
            if (CarregadorRecursos.isModoHeadlessDisco() && !isVarianteCorCustomizada(temporada, carro)) {
                int idCarro = Util.intOr0(carro);
                imageData = ImagensHeadlessDisco.obterOuGerarBytes(
                        ImagensHeadlessDisco.arquivoCarroLado(temporada, idCarro), "png",
                        () -> controlePaddock.carroLadoTemporadaCarro(temporada, carro));
            } else {
                BufferedImage carroLado = controlePaddock
                        .carroLadoTemporadaCarro(temporada, carro);
                imageData = carroLado == null ? null : codificarPng(carroLado);
            }
            if (imageData == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp png(String recurso) {
        try {
            BufferedImage buffer = CarregadorRecursos
                    .carregaBufferedImage("png/" + recurso + ".png");
            if (buffer == null) {
                return RespostaHttp.status(200).entity("null").build();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buffer, "png", baos);
            byte[] imageData = baos.toByteArray();
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp png(String recurso, String trasnparencia) {
        try {
            BufferedImage buffer = ImageUtil.geraTransparenciaAlpha(
                    CarregadorRecursos.carregaBufferedImage("png/" + recurso + ".png"),
                    Integer.parseInt(trasnparencia));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(buffer, "png", baos);
            byte[] imageData = baos.toByteArray();
            return RespostaHttp.status(200).entity(imageData).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp temporadasPilotos() {
        return RespostaHttp.status(200)
                .entity(carregadorRecursos.carregarTemporadasPilotos()).build();
    }

    public RespostaHttp temporadasDefaults(String temporada) {
        TemporadasDefault temporadasDefault;
        try {
            temporada = "t" + temporada;
            Map<String, TemporadasDefault> carregarTemporadasPilotosDefauts = carregadorRecursos
                    .carregarTemporadasPilotosDefauts();
            temporadasDefault = carregarTemporadasPilotosDefauts.get(temporada);
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
        return RespostaHttp.status(200).entity(temporadasDefault).build();
    }

    public RespostaHttp temporadas() {
        Vector<String> carregarTemporadas;
        try {
            carregarTemporadas = carregadorRecursos.carregarTemporadas();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
        return RespostaHttp.status(200).entity(carregarTemporadas).build();
    }

    public RespostaHttp sobre() {
        List<String> carregarCreditosJogo = CarregadorRecursos
                .carregarCreditosJogo();
        StringBuilder buffer = new StringBuilder();
        for (Iterator<String> iterator = carregarCreditosJogo
                .iterator(); iterator.hasNext(); ) {
            String string = iterator.next();
            buffer.append("<br>");
            buffer.append(string);
        }
        return RespostaHttp.status(200).entity(buffer.toString()).build();
    }

    /**
     * potencia : GIRO_MIN , GIRO_NOR , GIRO_MAX
     */
    public RespostaHttp potenciaMotor(String token, String potencia, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp
                .status(200).entity(controleJogosServer
                        .mudarGiroMotor(sessaoCliente, idPiloto, potencia))
                .build();
    }

    /**
     * agresividade : LENTO , NORMAL , AGRESSIVO
     */
    public RespostaHttp agressividadePiloto(String token, String agresividade, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp.status(200)
                .entity(controleJogosServer.mudarAgressividadePiloto(
                        sessaoCliente, idPiloto, agresividade))
                .build();
    }

    public RespostaHttp tracadoPiloto(String token, String tracado, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp
                .status(200).entity(controleJogosServer
                        .mudarTracadoPiloto(sessaoCliente, idPiloto, tracado))
                .build();
    }

    public RespostaHttp drsPiloto(String token, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp.status(200)
                .entity(controleJogosServer.mudarDrs(sessaoCliente, idPiloto))
                .build();
    }

    public RespostaHttp ersPiloto(String token, String idPiloto) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp.status(200)
                .entity(controleJogosServer.mudarErs(sessaoCliente, idPiloto))
                .build();
    }

    public RespostaHttp boxPiloto(String token, String idPiloto, Boolean ativa,
                                   String pneu, Integer combustivel, String asa) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        return RespostaHttp.status(200)
                .entity(controleJogosServer.boxPiloto(sessaoCliente, idPiloto,
                        ativa, pneu, combustivel, asa))
                .build();
    }

    public RespostaHttp lang(String lang) {
        try {
            PropertyResourceBundle bundle = Lang.carregraBundleMensagens(lang);
            Set<String> keySet = bundle.keySet();
            LinkedList<String> values = new LinkedList<String>();
            LinkedList<String> keys = new LinkedList<String>();
            for (Iterator iterator = keySet.iterator(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                values.add(bundle.getString(key));
                keys.add(key);
            }
            Map<String, LinkedList> retorno = new HashMap<String, LinkedList>();
            retorno.put("keys", keys);
            retorno.put("values", values);
            return RespostaHttp.status(200).entity(retorno).build();
        } catch (Exception e) {
            Logger.topExecpts(e);
            return RespostaHttp.status(500)
                    .entity(new ErroServ(e).obterErroFormatado())
                    .tipo(APPLICATION_JSON).build();
        }
    }

    public RespostaHttp equipe(String token, String idioma) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        Object ret = controleJogosServer.equipe(sessaoCliente);
        if (ret == null) {
            return RespostaHttp.status(204).build();
        }
        RespostaHttp erro = processsaMensagem(ret, idioma);
        if (erro != null) {
            return erro;
        }
        return RespostaHttp.status(200).entity(ret).build();
    }

    public RespostaHttp equipePilotoCarro(String token, String idioma) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        Object ret = controleJogosServer.equipePilotoCarro(sessaoCliente);
        if (ret == null) {
            return RespostaHttp.status(204).build();
        }
        RespostaHttp erro = processsaMensagem(ret, idioma);
        if (erro != null) {
            return erro;
        }
        return RespostaHttp.status(200).entity(ret).build();
    }

    public RespostaHttp gravarEquipe(String token, String idioma, CarreiraDadosSrv equipe) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        ControleJogosServer controleJogosServer = controlePaddock
                .getControleJogosServer();
        Object ret = controleJogosServer.gravarEquipe(sessaoCliente, idioma,
                equipe);
        if (ret.equals(new MsgSrv(Lang.msg("250")))) {
            return RespostaHttp.status(200).entity(ret).build();
        }
        return processsaMensagem(ret, idioma);
    }

    public RespostaHttp gravarCampeonato(String token, String idioma, CampeonatoSrv campeonato) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        Object ret = null;
        try {
            ret = controlePaddock.criarCampeonato(campeonato, sessaoCliente.getIdUsuario());
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        if (ret.equals(new MsgSrv(Lang.msg("campeonatoCriado")))) {
            return RespostaHttp.status(200).entity(ret).build();
        }
        return processsaMensagem(ret, idioma);
    }

    public RespostaHttp finalizaCampeonato(String token, String idioma, CampeonatoTO campeonato) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
        try {
            controlePaddock.finalizaCampeonato(campeonato, sessaoCliente.getIdUsuario());
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return RespostaHttp.status(200).entity(campeonato).build();
    }

    public RespostaHttp campeonato(String token, String idioma) {
        SessaoCliente sessaoCliente = controlePaddock
                .obterSessaoPorToken(token);
        if (sessaoCliente == null) {
            return RespostaHttp.status(401).build();
        }
        if (sessaoCliente.isGuest()) {
            return RespostaHttp.status(403).build();
        }
        sessaoCliente.setUlimaAtividade(System.currentTimeMillis());

        CampeonatoTO campeonato = null;
        try {
            campeonato = controlePaddock.obterCampeonatoEmAberto(sessaoCliente.getIdUsuario());
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        if (campeonato == null) {
            return RespostaHttp.status(204).build();
        }

        return RespostaHttp.status(200).entity(campeonato).build();
    }

    public RespostaHttp campeonatoPorId(String id, String token, String idioma) {
        CampeonatoTO campeonato = null;
        try {
            campeonato = controlePaddock.obterCampeonatoId(id);
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        if (campeonato == null) {
            return RespostaHttp.status(204).build();
        }
        return RespostaHttp.status(200).entity(campeonato).build();
    }

}
