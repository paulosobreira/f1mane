/**
 * 
 */
package sowbreira.f1mane.paddock.servlet;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.hibernate.Session;

import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
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
	private ControleCampeonatoServidor controleCampeonatoServidor;

	public DadosPaddock getDadosPaddock() {
		return dadosPaddock;
	}

	public void setDadosPaddock(DadosPaddock dadosPaddock) {
		this.dadosPaddock = dadosPaddock;
	}

	public ControlePaddockServidor(ControlePersistencia controlePersistencia) {
		this.controlePersistencia = controlePersistencia;
		controleClassificacao = new ControleClassificacao(controlePersistencia);
		controleCampeonatoServidor = new ControleCampeonatoServidor(
				controlePersistencia);
		controleJogosServer = new ControleJogosServer(dadosPaddock,
				controleClassificacao, controleCampeonatoServidor,
				controlePersistencia);

	}

	public ControleJogosServer getControleJogosServer() {
		return controleJogosServer;
	}

	public void setControleJogosServer(ControleJogosServer controleJogosServer) {
		this.controleJogosServer = controleJogosServer;
	}

	public Object processarObjetoRecebido(Object object) {
		if (object == null) {
			return null;
		}
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
		Session session = controlePersistencia.getSession();
		boolean erroMail = false;
		String senha = null;
		try {
			if (!Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
				jogadorDadosSrv = controlePersistencia
						.carregaDadosJogadorEmail(
								clientPaddockPack.getEmailJogador(), session);
			}
			if (jogadorDadosSrv == null
					&& !Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogador(
						clientPaddockPack.getNomeJogador(), session);
			}

			if (jogadorDadosSrv == null) {
				return new MsgSrv(Lang.msg("238"));
			}
			if ((System.currentTimeMillis() - jogadorDadosSrv
					.getUltimaRecuperacao()) < 300000) {
				return new MsgSrv(Lang.msg("243"));
			}
			try {
				PassGenerator generator = new PassGenerator();
				senha = generator.generateIt();
				try {
					mandaMailSenha(jogadorDadosSrv.getNome(),
							jogadorDadosSrv.getEmail(), senha);
				} catch (Exception e) {
					Logger.logarExept(e);
					erroMail = true;
				}
				jogadorDadosSrv.setSenha(Util.md5(senha));
				jogadorDadosSrv
						.setUltimaRecuperacao(System.currentTimeMillis());
				controlePersistencia.gravarDados(session, jogadorDadosSrv);
			} catch (Exception e) {
				Logger.logarExept(e);
				if (ServletPaddock.email != null) {
					Logger.logarExept(new Exception(Lang.msg("237")));
					System.out.println("Senha Gerada para "
							+ jogadorDadosSrv.getNome() + ":" + senha);
				}
			}
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		if (erroMail) {
			return new MsgSrv(Lang.msg("senhaGerada", new String[] {
					jogadorDadosSrv.getNome(), senha }));
		}
		return new MsgSrv(Lang.msg("239",
				new String[] { jogadorDadosSrv.getEmail() }));
	}

	private boolean validaCapcha(ClientPaddockPack clientPaddockPack) {
		try {
			Boolean validateResponseForID = capcha.validateResponseForID(
					clientPaddockPack.getChaveCapcha(),
					clientPaddockPack.getTextoCapcha());
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
		Session session = controlePersistencia.getSession();
		try {

			if (!Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogador(
						clientPaddockPack.getNomeJogador(), session);
			}
			if (jogadorDadosSrv == null
					&& !Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
				jogadorDadosSrv = controlePersistencia
						.carregaDadosJogadorEmail(
								clientPaddockPack.getEmailJogador(), session);
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
						Logger.logarExept(new Exception(Lang.msg("237")));
				}
				try {
					jogadorDadosSrv.setSenha(Util.md5(senha));
					clientPaddockPack.setSenhaJogador(jogadorDadosSrv
							.getSenha());
					controlePersistencia
							.adicionarJogador(jogadorDadosSrv.getNome(),
									jogadorDadosSrv, session);
				} catch (Exception e) {
					return new ErroServ(e);
				}
			} else {
				return new MsgSrv(Lang.msg("loginIndisponivel"));
			}
		} finally {
			if (session.isOpen())
				session.close();
		}

		return criarSessao(clientPaddockPack);
	}

	private void mandaMailSenha(String nome, String email, String senha)
			throws AddressException, MessagingException {
		Logger.logar("Senha :" + senha);
		ServletPaddock.email.sendSimpleMail("F1-Mane Game Password",
				new String[] { email }, "Your game user:password is " + nome
						+ ":" + senha, false);
	}

	private Object obterDadosParciaisPilotos(String[] args) {
		return controleJogosServer.obterDadosParciaisPilotos(args);
	}

	private Object obterPosicaoPilotos(Object object) {
		return controleJogosServer.obterPosicaoPilotos((String) object);
	}

	private Object processarComando(ClientPaddockPack clientPaddockPack) {
		SessaoCliente cliente = resgatarSessao(clientPaddockPack);
		if (cliente != null) {
			cliente.setUlimaAtividade(System.currentTimeMillis());
		}
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
		} else if (Comandos.MUDAR_KERS.equals(commando)) {
			return mudarKers(clientPaddockPack);
		} else if (Comandos.MUDAR_DRS.equals(commando)) {
			return mudarDrs(clientPaddockPack);
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
		} else if (Comandos.SAIR_JOGO.equals(commando)) {
			return sairDoJogo(clientPaddockPack);
		} else if (Comandos.ENTRAR_JOGO.equals(commando)) {
			return entrarJogo(clientPaddockPack);
		} else if (Comandos.VER_DETALHES_JOGO.equals(commando)) {
			return detalhesJogo(clientPaddockPack);
		} else if (Comandos.VER_INFO_VOLTAS_JOGO.equals(commando)) {
			return detalhesVoltasJogo(clientPaddockPack);
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
		} else if (Comandos.CRIAR_CAMPEONATO.equals(commando)) {
			return criarCampeonato(clientPaddockPack);
		} else if (Comandos.LISTAR_CAMPEONATOS.equals(commando)) {
			return listarCampeonatos(clientPaddockPack);
		} else if (Comandos.OBTER_CAMPEONATO.equals(commando)) {
			return obterCampeonato(clientPaddockPack);
		} else if (Comandos.DRIVE_THRU.equals(commando)) {
			return driveThru(clientPaddockPack);
		}
		return "Comando invalido";
	}

	private Object driveThru(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.driveThru(clientPaddockPack);
	}

	private Object detalhesVoltasJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.detalhesVoltasJogo(clientPaddockPack);
	}

	private Object mudarDrs(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarDrs(clientPaddockPack);
	}

	private Object mudarKers(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarKers(clientPaddockPack);
	}

	private Object obterCampeonato(ClientPaddockPack clientPaddockPack) {
		return controleCampeonatoServidor.obterCampeonato(clientPaddockPack);
	}

	private Object listarCampeonatos(ClientPaddockPack clientPaddockPack) {
		return controleCampeonatoServidor.listarCampeonatos();
	}

	private Object criarCampeonato(ClientPaddockPack clientPaddockPack) {
		return controleCampeonatoServidor.criarCampeonato(clientPaddockPack);
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
		Session session = controlePersistencia.getSession();
		try {
			return controleClassificacao
					.verCarreira(clientPaddockPack, session);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	private Object mudarModoAutoAgressivo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoAutoAgressivo(clientPaddockPack);
	}

	private Object verCorridas(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaCorridasJogador(controleClassificacao
				.obterListaCorridas(clientPaddockPack.getNomeJogador(),
						(Integer) clientPaddockPack.getDataObject()));
		return srvPaddockPack;
	}

	private Object verClassificacao(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaDadosJogador(controleClassificacao
				.obterListaClassificacao((Integer) clientPaddockPack
						.getDataObject()));
		return srvPaddockPack;
	}

	private Object verConstrutores(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		controleClassificacao.preencherListaContrutores(srvPaddockPack,
				(Integer) clientPaddockPack.getDataObject());
		return srvPaddockPack;
	}

	private Object mudarGiroMotor(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarGiroMotor(clientPaddockPack);
	}

	private Object sairDoJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.sairDoJogo(clientPaddockPack);
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
		try {
			atualizaPilotoJogoSessaoCliente();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		return srvPaddockPack;
	}

	private void atualizaPilotoJogoSessaoCliente() throws Exception {
		List clientes = getDadosPaddock().getClientes();
		for (Iterator iterator = clientes.iterator(); iterator.hasNext();) {
			SessaoCliente sessaoCliente = (SessaoCliente) iterator.next();
			Map mapaJogosCriados = controleJogosServer.getMapaJogosCriados();
			boolean achouJogo = false;
			for (Iterator iterator2 = mapaJogosCriados.keySet().iterator(); iterator2
					.hasNext();) {
				Object key = (Object) iterator2.next();
				JogoServidor jogoServidor = (JogoServidor) controleJogosServer
						.getMapaJogosCriados().get(key);
				Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor
						.getMapJogadoresOnline();
				DadosCriarJogo participarJogo = mapJogadoresOnline
						.get(sessaoCliente.getNomeJogador());
				if (participarJogo != null) {
					sessaoCliente.setJogoAtual(jogoServidor
							.getNomeJogoServidor());
					sessaoCliente.setPilotoAtual(participarJogo.getPiloto());
					achouJogo = true;
				}
			}
			if (!achouJogo) {
				sessaoCliente.setJogoAtual(null);
				sessaoCliente.setPilotoAtual(null);
			}
		}
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
		Session session = controlePersistencia.getSession();
		JogadorDadosSrv jogadorDadosSrv = controlePersistencia
				.carregaDadosJogador(clientPaddockPack.getNomeJogador(),
						session);

		if (jogadorDadosSrv == null) {
			return new MsgSrv(Lang.msg("238"));
		} else if (jogadorDadosSrv.getSenha() == null
				|| !jogadorDadosSrv.getSenha().equals(
						clientPaddockPack.getSenhaJogador())) {
			return new MsgSrv(Lang.msg("236"));
		}
		jogadorDadosSrv.setUltimoLogon(System.currentTimeMillis());
		try {
			controlePersistencia.gravarDados(session, jogadorDadosSrv);
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		}

		SessaoCliente sessaoCliente = null;
		for (Iterator iter = dadosPaddock.getClientes().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
			if (element.getNomeJogador().equals(
					clientPaddockPack.getNomeJogador())) {
				sessaoCliente = element;
				break;
			}
		}
		if (sessaoCliente == null) {
			sessaoCliente = new SessaoCliente();
			sessaoCliente.setNomeJogador(clientPaddockPack.getNomeJogador());
			sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
			dadosPaddock.getClientes().add(sessaoCliente);
		}
		controleJogosServer.removerClienteInativo(sessaoCliente);
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(sessaoCliente);

		return srvPaddockPack;
	}

	public static void main(String[] args) {
		// String test = "#brual#llllp#";
		// Logger.logar(test.replaceAll("#", ""));

		System.out.println(Lang.decodeTexto("¢088¢ 0 2011"));
	}

	public void removerClienteInativo(SessaoCliente sessaoCliente) {
		controleJogosServer.removerClienteInativo(sessaoCliente);
		dadosPaddock.getClientes().remove(sessaoCliente);
	}
}
