/**
 * 
 */
package sowbreira.f1mane.paddock.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;
import br.nnpe.PassGenerator;
import br.nnpe.Util;

import com.octo.captcha.service.image.DefaultManageableImageCaptchaService;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockServidor {
	private DefaultManageableImageCaptchaService capcha = new DefaultManageableImageCaptchaService();
	private DadosPaddock dadosPaddock = new DadosPaddock();
	private ControleJogosServer controleJogosServer;
	private ControlePersistencia controlePersistencia;
	private ControleClassificacao controleClassificacao;

	public DadosPaddock getDadosPaddock() {
		return dadosPaddock;
	}

	public void setDadosPaddock(DadosPaddock dadosPaddock) {
		this.dadosPaddock = dadosPaddock;
	}

	public ControlePaddockServidor(ControlePersistencia controlePersistencia) {
		this.controlePersistencia = controlePersistencia;
		controleClassificacao = new ControleClassificacao(controlePersistencia);
		controleJogosServer = new ControleJogosServer(dadosPaddock,
				controleClassificacao);

	}

	public ControleJogosServer getControleJogosServer() {
		return controleJogosServer;
	}

	public void setControleJogosServer(ControleJogosServer controleJogosServer) {
		this.controleJogosServer = controleJogosServer;
	}

	public Object processarObjetoRecebido(Object object) {
		if (object instanceof String) {
			String pedido = (String) object;
			if (pedido.indexOf("#") != -1) {
				String[] args = pedido.split("#");
				return obterDadosParciaisPilotos(args);
			} else {
				return obterPosicaoPilotos(object);
			}

		}
		if (object instanceof ClientPaddockPack) {
			ClientPaddockPack clientPaddockPack = (ClientPaddockPack) object;
			if (Comandos.REGISTRAR_LOGIN.equals(clientPaddockPack.getComando())) {

				if ("IA".equals(clientPaddockPack.getNomeJogador())
						|| "Ia".equals(clientPaddockPack.getNomeJogador())
						|| "ia".equals(clientPaddockPack.getNomeJogador())
						|| "iA".equals(clientPaddockPack.getNomeJogador())
						|| clientPaddockPack.getNomeJogador().contains("@")
						|| clientPaddockPack.getNomeJogador().contains("§")) {
					return new MsgSrv(Lang.msg("242"));
				}

				if (clientPaddockPack.isRecuperar()) {
					return resetaSenha(clientPaddockPack);

				}
				if (!Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())
						&& !Util.isNullOrEmpty(clientPaddockPack
								.getEmailJogador())) {
					return registrarLogin(clientPaddockPack);
				}

				if (!Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())
						&& !Util.isNullOrEmpty(clientPaddockPack
								.getSenhaJogador())) {
					return criarSessao(clientPaddockPack);
				}

				return new MsgSrv(Lang.msg("242"));
			}
			return processarComando(clientPaddockPack);
		}
		return new MsgSrv(Lang.msg("209"));
	}

	private Object resetaSenha(ClientPaddockPack clientPaddockPack) {
		if (!validaCapcha(clientPaddockPack)) {
			return new MsgSrv(Lang.msg("capchaInvalido"));
		}
		JogadorDadosSrv jogadorDadosSrv = null;
		if (!Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
			jogadorDadosSrv = controlePersistencia
					.carregaDadosJogadorEmail(clientPaddockPack
							.getEmailJogador());
		}

		if (jogadorDadosSrv == null) {
			return new MsgSrv(Lang.msg("email404"));
		}
		if ((System.currentTimeMillis() - jogadorDadosSrv
				.getUltimaRecuperacao()) < 300000) {
			return new MsgSrv(Lang.msg("243"));
		}
		try {
			PassGenerator generator = new PassGenerator();
			String senha = generator.generateIt();
			mandaMailSenha(jogadorDadosSrv.getNome(), jogadorDadosSrv
					.getEmail(), senha);
			jogadorDadosSrv.setSenha(Util.md5(senha));
			jogadorDadosSrv.setUltimaRecuperacao(System.currentTimeMillis());
			controlePersistencia.gravarDados(jogadorDadosSrv);
		} catch (Exception e) {
			Logger.logarExept(e);
			if (ServletPaddock.email != null)
				return new MsgSrv(Lang.msg("237"));
		}
		return new MsgSrv(Lang.msg("239", new String[] { jogadorDadosSrv
				.getEmail() }));
	}

	private boolean validaCapcha(ClientPaddockPack clientPaddockPack) {
		try {
			Boolean validateResponseForID = capcha.validateResponseForID(
					clientPaddockPack.getChaveCapcha(), clientPaddockPack
							.getTextoCapcha());
			return validateResponseForID;
		} catch (Exception e) {
			Logger.logarExept(e);
			return false;
		}
	}

	private Object registrarLogin(ClientPaddockPack clientPaddockPack) {
		if (!validaCapcha(clientPaddockPack)) {
			return new MsgSrv(Lang.msg("capchaInvalido"));
		}
		JogadorDadosSrv jogadorDadosSrv = null;
		if (!Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
			jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(clientPaddockPack.getNomeJogador());
		}
		if (jogadorDadosSrv == null
				&& !Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
			jogadorDadosSrv = controlePersistencia
					.carregaDadosJogadorEmail(clientPaddockPack
							.getEmailJogador());
		}
		if (jogadorDadosSrv == null) {
			jogadorDadosSrv = new JogadorDadosSrv();
			jogadorDadosSrv.setNome(clientPaddockPack.getNomeJogador());
			jogadorDadosSrv.setEmail(clientPaddockPack.getEmailJogador());

			PassGenerator generator = new PassGenerator();
			String senha = generator.generateIt();
			try {
				mandaMailSenha(clientPaddockPack.getNomeJogador(),
						clientPaddockPack.getEmailJogador(), senha);
			} catch (Exception e1) {
				Logger.logarExept(e1);
				if (ServletPaddock.email != null)
					return new MsgSrv(Lang.msg("237"));
			}
			try {
				jogadorDadosSrv.setSenha(Util.md5(senha));
				clientPaddockPack.setSenhaJogador(jogadorDadosSrv.getSenha());
				controlePersistencia.adicionarJogador(
						jogadorDadosSrv.getNome(), jogadorDadosSrv);
			} catch (Exception e) {
				return new ErroServ(e);
			}
		} else {
			return new MsgSrv(Lang.msg("loginIndisponivel"));
		}

		return criarSessao(clientPaddockPack);
	}

	private void mandaMailSenha(String nome, String email, String senha)
			throws AddressException, MessagingException {
		Logger.logar("Senha :" + senha);
		ServletPaddock.email.sendSimpleMail("F1-Mane Game Password",
				new String[] { email }, "admin@f1mane.com",
				"Your game user:password is " + nome + ":" + senha, false);
	}

	private Object obterDadosParciaisPilotos(String[] args) {
		return controleJogosServer.obterDadosParciaisPilotos(args);
	}

	private Object obterPosicaoPilotos(Object object) {
		return controleJogosServer.obterPosicaoPilotos((String) object);
	}

	private Object processarComando(ClientPaddockPack clientPaddockPack) {
		SessaoCliente cliente = resgatarSessao(clientPaddockPack);
		clientPaddockPack.setSessaoCliente(cliente);
		String commando = clientPaddockPack.getComando();
		if (Comandos.OBTER_DADOS_JOGO.equals(commando)) {
			return obterDadosJogo(clientPaddockPack);
		} else if (Comandos.VERIFICA_ESTADO_JOGO.equals(commando)) {
			return verificaEstadoJogo(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_AGRESSIVO.equals(commando)) {
			return mudarModoAgressivo(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_PILOTAGEM.equals(commando)) {
			return mudarModoAutoAgressivo(clientPaddockPack);
		} else if (Comandos.MUDAR_GIRO_MOTOR.equals(commando)) {
			return mudarGiroMotor(clientPaddockPack);
		} else if (Comandos.MUDAR_TRACADO.equals(commando)) {
			return mudarTracado(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_BOX.equals(commando)) {
			return mudarModoBox(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_AUTOPOS.equals(commando)) {
			return mudarModoAutoPos(clientPaddockPack);
		} else if (Comandos.ATUALIZAR_VISAO.equals(commando)) {
			return atualizarDadosVisao(clientPaddockPack, cliente);
		} else if (Comandos.SAIR_PADDOCK.equals(commando)) {
			return sairPaddock(clientPaddockPack, cliente);
		} else if (Comandos.ENVIAR_TEXTO.equals(commando)) {
			return receberTexto(clientPaddockPack, cliente);
		} else if (Comandos.CRIAR_JOGO.equals(commando)) {
			return criarJogo(clientPaddockPack);
		} else if (Comandos.ABANDONAR.equals(commando)) {
			return abandonarJogo(clientPaddockPack);
		} else if (Comandos.ENTRAR_JOGO.equals(commando)) {
			return entrarJogo(clientPaddockPack);
		} else if (Comandos.VER_DETALHES_JOGO.equals(commando)) {
			return detalhesJogo(clientPaddockPack);
		} else if (Comandos.INICIAR_JOGO.equals(commando)) {
			return iniciaJogo(clientPaddockPack);
		} else if (Comandos.VER_CLASSIFICACAO.equals(commando)) {
			return verClassificacao(clientPaddockPack);
		} else if (Comandos.VER_CONTRUTORES.equals(commando)) {
			return verConstrutores(clientPaddockPack);
		} else if (Comandos.VER_CARREIRA.equals(commando)) {
			return verCarreira(clientPaddockPack);
		} else if (Comandos.ATUALIZA_CARREIRA.equals(commando)) {
			return atualizaCarreira(clientPaddockPack);
		} else if (Comandos.VER_CORRIDAS.equals(commando)) {
			return verCorridas(clientPaddockPack);
		} else if (Comandos.DADOS_PILOTOS_JOGO.equals(commando)) {
			return dadosPilotosJogo(clientPaddockPack);
		} else if (Comandos.OBTER_NOVO_CAPCHA.equals(commando)) {
			return obterNovoCapcha(clientPaddockPack);
		}
		return "Comando invalido";
	}

	private Object obterNovoCapcha(ClientPaddockPack clientPaddockPack) {
		try {
			ByteArrayOutputStream jpegstream = new ByteArrayOutputStream();
			String chave = String.valueOf(System.currentTimeMillis());
			BufferedImage challenge = capcha.getImageChallengeForID(chave);
			ImageIO.write(challenge, "jpg", jpegstream);
			clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setComando(Comandos.OBTER_NOVO_CAPCHA);
			clientPaddockPack.setChaveCapcha(chave);
			clientPaddockPack.setDataBytes(jpegstream.toByteArray());
			return clientPaddockPack;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return new ErroServ(Lang.msg("erroCapcha"));
	}

	private Object dadosPilotosJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.dadosPilotosJogo(clientPaddockPack);
	}

	private Object mudarTracado(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarTracado(clientPaddockPack);
	}

	private Object mudarModoAutoPos(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoAutoPos(clientPaddockPack);
	}

	private Object atualizaCarreira(ClientPaddockPack clientPaddockPack) {
		if (controleJogosServer.verificaJaEmAlgumJogo(clientPaddockPack
				.getSessaoCliente())) {
			return new MsgSrv(Lang.msg("248"));
		}
		return controleClassificacao.atualizaCarreira(clientPaddockPack);
	}

	private Object verCarreira(ClientPaddockPack clientPaddockPack) {
		return controleClassificacao.verCarreira(clientPaddockPack);
	}

	private Object mudarModoAutoAgressivo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoAutoAgressivo(clientPaddockPack);
	}

	private Object verCorridas(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaCorridasJogador(controleClassificacao
				.obterListaCorridas(clientPaddockPack.getNomeJogador()));
		return srvPaddockPack;
	}

	private Object verClassificacao(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaDadosJogador(controleClassificacao
				.obterListaClassificacao());
		return srvPaddockPack;
	}

	private Object verConstrutores(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		controleClassificacao.preencherListaContrutores(srvPaddockPack);
		return srvPaddockPack;
	}

	private Object mudarGiroMotor(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarGiroMotor(clientPaddockPack);
	}

	private Object abandonarJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.abandonarJogo(clientPaddockPack);
	}

	private Object mudarModoBox(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoBox(clientPaddockPack);
	}

	private Object mudarModoAgressivo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoAgressivo(clientPaddockPack);
	}

	private Object obterDadosJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.obterDadosJogo(clientPaddockPack);
	}

	private Object iniciaJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.iniciaJogo(clientPaddockPack);
	}

	private Object verificaEstadoJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.verificaEstadoJogo(clientPaddockPack);
	}

	private Object detalhesJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.detalhesJogo(clientPaddockPack);
	}

	private Object entrarJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.entrarJogo(clientPaddockPack);
	}

	private Object criarJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.criarJogo(clientPaddockPack);
	}

	private Object receberTexto(ClientPaddockPack clientPaddockPack,
			SessaoCliente cliente) {
		if (cliente == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		dadosPaddock.setLinhaChat(cliente.getNomeJogador() + " : "
				+ clientPaddockPack.getTexto());
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		dadosPaddock.setDataTime(new Long(System.currentTimeMillis()));
		return srvPaddockPack;
	}

	private Object atualizarDadosVisao(ClientPaddockPack clientPaddockPack,
			SessaoCliente cliente) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		return srvPaddockPack;
	}

	private SessaoCliente resgatarSessao(ClientPaddockPack clientPaddockPack) {
		try {
			if (clientPaddockPack == null
					|| clientPaddockPack.getSessaoCliente() == null) {
				return null;
			}
			return verificaUsuarioSessao(clientPaddockPack.getSessaoCliente()
					.getNomeJogador());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public SessaoCliente verificaUsuarioSessao(String apelido) {
		for (Iterator iter = dadosPaddock.getClientes().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
			if (element.getNomeJogador().equals(apelido)) {
				element.setUlimaAtividade(System.currentTimeMillis());
				return element;
			}
		}

		return null;
	}

	private Object sairPaddock(ClientPaddockPack clientPaddockPack,
			SessaoCliente cliente) {
		dadosPaddock.getClientes().remove(cliente);
		return null;
	}

	private Object criarSessao(ClientPaddockPack clientPaddockPack) {

		JogadorDadosSrv jogadorDadosSrv = controlePersistencia
				.carregaDadosJogador(clientPaddockPack.getNomeJogador());

		if (jogadorDadosSrv == null) {
			return new MsgSrv(Lang.msg("238"));
		} else if (jogadorDadosSrv.getSenha() == null
				|| !jogadorDadosSrv.getSenha().equals(
						clientPaddockPack.getSenhaJogador())) {
			return new MsgSrv(Lang.msg("236"));
		}
		jogadorDadosSrv.setUltimoLogon(System.currentTimeMillis());
		controlePersistencia.gravarDados(jogadorDadosSrv);

		SessaoCliente cliente = null;
		for (Iterator iter = dadosPaddock.getClientes().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
			if (element.getNomeJogador().equals(
					clientPaddockPack.getNomeJogador())) {
				cliente = element;
				break;
			}
		}
		if (cliente == null) {
			cliente = new SessaoCliente();
			cliente.setNomeJogador(clientPaddockPack.getNomeJogador());
			cliente.setUlimaAtividade(System.currentTimeMillis());
			dadosPaddock.getClientes().add(cliente);
		}
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(cliente);

		return srvPaddockPack;
	}

	public static void main(String[] args) {
		String test = "#brual#llllp#";
		Logger.logar(test.replaceAll("#", ""));
	}

	public void removerClienteInativo(SessaoCliente sessaoCliente) {
		controleJogosServer.removerClienteInativo(sessaoCliente);
		dadosPaddock.getClientes().remove(sessaoCliente);
	}
}
