/**
 * 
 */
package br.f1mane.paddock.servlet;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.hibernate.Session;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.PassGenerator;
import br.nnpe.TokenGenerator;
import br.nnpe.Util;
import br.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.TemporadasDefauts;
import br.f1mane.paddock.PaddockConstants;
import br.f1mane.paddock.entidades.Comandos;
import br.f1mane.paddock.entidades.TOs.CampeonatoTO;
import br.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import br.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import br.f1mane.paddock.entidades.TOs.DadosJogo;
import br.f1mane.paddock.entidades.TOs.DadosPaddock;
import br.f1mane.paddock.entidades.TOs.DadosParciais;
import br.f1mane.paddock.entidades.TOs.ErroServ;
import br.f1mane.paddock.entidades.TOs.MsgSrv;
import br.f1mane.paddock.entidades.TOs.SessaoCliente;
import br.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import br.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import br.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockServidor {
	private DadosPaddock dadosPaddock = new DadosPaddock();
	private ControleJogosServer controleJogosServer;
	private ControlePersistencia controlePersistencia;
	private ControleClassificacao controleClassificacao;
	private ControleCampeonatoServidor controleCampeonatoServidor;
	private int versao;
	private int contadorVistantes = 1;
	private CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);

	public DadosPaddock getDadosPaddock() {
		return dadosPaddock;
	}

	public void setDadosPaddock(DadosPaddock dadosPaddock) {
		this.dadosPaddock = dadosPaddock;
	}

	public ControlePaddockServidor(ControlePersistencia controlePersistencia) {
		this.controlePersistencia = controlePersistencia;
		controleCampeonatoServidor = new ControleCampeonatoServidor(controlePersistencia, this);
		controleClassificacao = new ControleClassificacao(controlePersistencia, controleCampeonatoServidor);
		controleJogosServer = new ControleJogosServer(dadosPaddock, controleClassificacao, controleCampeonatoServidor,
				controlePersistencia, this);
		try {
			initProperties();
		} catch (IOException e) {
			Logger.logarExept(e);
		}
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
			if (Comandos.VERIFICA_VERSAO.equals(clientPaddockPack.getComando())) {
				return versao;
			} else if (Comandos.GUEST_LOGIN_APPLET.equals(clientPaddockPack.getComando())) {
				return criarSessaoVisitante();
			}
			return processarComando(clientPaddockPack);
		}
		return new MsgSrv(Lang.msg("209"));
	}

	@Deprecated
	private Object resetaSenha(ClientPaddockPack clientPaddockPack) {
		JogadorDadosSrv jogadorDadosSrv = null;
		Session session = controlePersistencia.getSession();
		boolean erroMail = false;
		String senha = null;
		try {
			if (!Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogadorEmail(clientPaddockPack.getEmailJogador(),
						session);
			}
			if (jogadorDadosSrv == null && !Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogador(clientPaddockPack.getNomeJogador(), session);
			}

			if (jogadorDadosSrv == null) {
				return new MsgSrv(Lang.msg("238"));
			}
			if ((System.currentTimeMillis() - jogadorDadosSrv.getUltimaRecuperacao()) < 300000) {
				return new MsgSrv(Lang.msg("243"));
			}
			try {
				PassGenerator generator = new PassGenerator();
				senha = generator.generateIt();
				try {
					mandaMailSenha(jogadorDadosSrv.getNome(), jogadorDadosSrv.getEmail(), senha);
				} catch (Exception e) {
					Logger.logarExept(e);
					erroMail = true;
				}
				jogadorDadosSrv.setSenha(Util.md5(senha));
				jogadorDadosSrv.setUltimaRecuperacao(System.currentTimeMillis());
				controlePersistencia.gravarDados(session, jogadorDadosSrv);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
		if (erroMail) {
			return new MsgSrv(Lang.msg("senhaGerada", new String[] { jogadorDadosSrv.getNome(), senha }));
		}
		return new MsgSrv(Lang.msg("239", new String[] { jogadorDadosSrv.getEmail() }));
	}

	@Deprecated
	private Object criarLogin(ClientPaddockPack clientPaddockPack) {
		JogadorDadosSrv jogadorDadosSrv = null;
		Session session = controlePersistencia.getSession();
		String senha;
		try {

			if (!Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogadorNome(clientPaddockPack.getNomeJogador(),
						session);
			}
			if (jogadorDadosSrv == null && !Util.isNullOrEmpty(clientPaddockPack.getEmailJogador())) {
				jogadorDadosSrv = controlePersistencia.carregaDadosJogadorEmail(clientPaddockPack.getEmailJogador(),
						session);
			}
			if (jogadorDadosSrv == null) {
				jogadorDadosSrv = new JogadorDadosSrv();
				jogadorDadosSrv.setNome(clientPaddockPack.getNomeJogador());
				jogadorDadosSrv.setEmail(clientPaddockPack.getEmailJogador());

				PassGenerator generator = new PassGenerator();
				senha = generator.generateIt();
				try {
					mandaMailSenha(clientPaddockPack.getNomeJogador(), clientPaddockPack.getEmailJogador(), senha);
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
				try {
					jogadorDadosSrv.setSenha(Util.md5(senha));
					clientPaddockPack.setSenhaJogador(jogadorDadosSrv.getSenha());
					jogadorDadosSrv.setIdGoogle("local - " + System.currentTimeMillis());
					controlePersistencia.adicionarJogador(jogadorDadosSrv.getNome(), jogadorDadosSrv, session);
				} catch (Exception e) {
					return new ErroServ(e);
				}
			} else {
				return new MsgSrv(Lang.msg("loginIndisponivel"));
			}
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}

		return criarSessao(clientPaddockPack, senha);
	}

	private void mandaMailSenha(String nome, String email, String senha) throws AddressException, MessagingException {
		Logger.logar("Senha :" + senha);
	}

	public Object obterDadosParciaisPilotos(String[] args) {
		return controleJogosServer.obterDadosParciaisPilotos(args);
	}

	public Object obterPosicaoPilotos(Object object) {
		return controleJogosServer.obterPosicaoPilotos((String) object);
	}

	public Object obterCircuito(Object object) {
		JogoServidor jogo = controleJogosServer.obterJogoPeloNome((String) object);
		try {
			if (jogo.getCircuito() == null) {
				jogo.carregaRecursos(
						(String) jogo.getCircuitos().get(jogo.getDadosCriarJogo().getCircuitoSelecionado()));
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
		Circuito circuito = jogo.getCircuito();
		return circuito;
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
		} else if (Comandos.MUDAR_PILOTO_MAXIMO.equals(commando)) {
			return mudarPilotoMaximo(clientPaddockPack);
		} else if (Comandos.MUDAR_PILOTO_NORMAL.equals(commando)) {
			return mudarPilotoNormal(clientPaddockPack);
		} else if (Comandos.MUDAR_PILOTO_MINIMO.equals(commando)) {
			return mudarPilotoMinimo(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_PILOTAGEM.equals(commando)) {
			return mudarModoPilotagem(clientPaddockPack);
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
		} else if (Comandos.ALTERAR_OPCOES_BOX.equals(commando)) {
			return alterarOpcoesBox(clientPaddockPack);
		} else if (Comandos.MUDAR_MODO_AUTOPOS_S.equals(commando)) {
			return mudarModoAutoPos(clientPaddockPack, true);
		} else if (Comandos.MUDAR_MODO_AUTOPOS_N.equals(commando)) {
			return mudarModoAutoPos(clientPaddockPack, false);
		} else if (Comandos.ATUALIZAR_VISAO.equals(commando)) {
			return atualizarDadosVisao();
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

	private Object alterarOpcoesBox(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.alterarOpcoesBox(clientPaddockPack);
	}

	private Object mudarPilotoMinimo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarPilotoMinimo(clientPaddockPack);
	}

	private Object mudarPilotoNormal(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarPilotoNormal(clientPaddockPack);
	}

	private Object mudarPilotoMaximo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarPilotoMaximo(clientPaddockPack);
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

	public Object obterCampeonato(String idCampeonato) {
		return controleCampeonatoServidor.obterCampeonato(idCampeonato);
	}

	private Object listarCampeonatos(ClientPaddockPack clientPaddockPack) {
		return controleCampeonatoServidor.listarCampeonatos();
	}

	private Object criarCampeonato(ClientPaddockPack clientPaddockPack) {
		return controleCampeonatoServidor.criarCampeonato(clientPaddockPack);
	}

	public Object criarCampeonato(CampeonatoSrv campeonato, String token) {
		return controleCampeonatoServidor.criarCampeonato(campeonato, token);
	}

	private Object dadosPilotosJogo(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.dadosPilotosJogo(clientPaddockPack);
	}

	private Object mudarTracado(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarTracado(clientPaddockPack);
	}

	private Object mudarModoAutoPos(ClientPaddockPack clientPaddockPack, boolean autoPos) {
		return controleJogosServer.mudarModoAutoPos(clientPaddockPack, autoPos);
	}

	private Object atualizaCarreira(ClientPaddockPack clientPaddockPack) {
		if (controleJogosServer.verificaJaEmAlgumJogo(clientPaddockPack.getSessaoCliente())) {
			return new MsgSrv(Lang.msg("248"));
		}
		return controleClassificacao.atualizaCarreira(clientPaddockPack);
	}

	private Object verCarreira(ClientPaddockPack clientPaddockPack) {
		Session session = controlePersistencia.getSession();
		try {
			return controleClassificacao.verCarreira(clientPaddockPack, session);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	private Object mudarModoPilotagem(ClientPaddockPack clientPaddockPack) {
		return controleJogosServer.mudarModoPilotagem(clientPaddockPack);
	}

	private Object verCorridas(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaCorridasJogador(controleClassificacao
				.obterListaCorridas(clientPaddockPack.getNomeJogador(), (Integer) clientPaddockPack.getDataObject()));
		return srvPaddockPack;
	}

	private Object verClassificacao(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setListaDadosJogador(
				controleClassificacao.obterListaClassificacao((Integer) clientPaddockPack.getDataObject()));
		return srvPaddockPack;
	}

	private Object verConstrutores(ClientPaddockPack clientPaddockPack) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		controleClassificacao.preencherListaContrutores(srvPaddockPack, (Integer) clientPaddockPack.getDataObject());
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

	public Object obterDadosJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.obterDadosJogo(clientPaddockPack);
	}

	public Object iniciaJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.iniciaJogo(clientPaddockPack);
	}

	private Object verificaEstadoJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.verificaEstadoJogo(clientPaddockPack);
	}

	private Object detalhesJogo(ClientPaddockPack clientPaddockPack) {

		return controleJogosServer.detalhesJogo(clientPaddockPack);
	}

	public Object entrarJogo(ClientPaddockPack clientPaddockPack) {
		try {
			return controleJogosServer.entrarJogo(clientPaddockPack);
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
	}

	public Object criarJogo(ClientPaddockPack clientPaddockPack) {
		try {
			return controleJogosServer.criarJogo(clientPaddockPack);
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
	}

	private Object receberTexto(ClientPaddockPack clientPaddockPack, SessaoCliente cliente) {
		if (cliente == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		dadosPaddock.setLinhaChat(cliente.getNomeJogador() + " : " + clientPaddockPack.getTexto());
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		dadosPaddock.setDataTime(new Long(System.currentTimeMillis()));
		return srvPaddockPack;
	}

	public Object atualizarDadosVisao() {
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
			for (Iterator iterator2 = mapaJogosCriados.keySet().iterator(); iterator2.hasNext();) {
				Object key = (Object) iterator2.next();
				JogoServidor jogoServidor = (JogoServidor) controleJogosServer.getMapaJogosCriados().get(key);
				Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor.getMapJogadoresOnline();
				DadosCriarJogo participarJogo = mapJogadoresOnline.get(sessaoCliente.getToken());
				if (participarJogo != null) {
					sessaoCliente.setJogoAtual(jogoServidor.getNomeJogoServidor());
					List<Piloto> pilotosCopia = jogoServidor.getPilotosCopia();
					for (Iterator iterator3 = pilotosCopia.iterator(); iterator3.hasNext();) {
						Piloto piloto = (Piloto) iterator3.next();
						if (piloto.getId() == participarJogo.getIdPiloto()) {
							sessaoCliente.setPilotoAtual(piloto.getNome());
						}
					}
					sessaoCliente.setIdPilotoAtual(participarJogo.getIdPiloto());
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
			if (clientPaddockPack == null || clientPaddockPack.getSessaoCliente() == null) {
				return null;
			}
			return verificaUsuarioSessao(clientPaddockPack.getSessaoCliente().getToken());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public SessaoCliente obterSessaoPorToken(String token) {
		try {
			if (Util.isNullOrEmpty(token)) {
				return null;
			}
			for (Iterator iter = dadosPaddock.getClientes().iterator(); iter.hasNext();) {
				SessaoCliente element = (SessaoCliente) iter.next();
				if (token.equals(element.getToken())) {
					return element;
				}
			}
			return null;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public SessaoCliente verificaUsuarioSessao(String token) {
		for (Iterator<SessaoCliente> iter = dadosPaddock.getClientes().iterator(); iter.hasNext();) {
			SessaoCliente element = iter.next();
			if (element.getToken().equals(token)) {
				return element;
			}
		}
		return null;
	}

	private Object sairPaddock(ClientPaddockPack clientPaddockPack, SessaoCliente cliente) {
		dadosPaddock.remove(cliente);
		return null;
	}

	@Deprecated
	private Object criarSessao(ClientPaddockPack clientPaddockPack) {
		return criarSessao(clientPaddockPack, null);
	}

	@Deprecated
	private Object criarSessao(ClientPaddockPack clientPaddockPack, String senha) {
		Session session = controlePersistencia.getSession();
		JogadorDadosSrv jogadorDadosSrv = controlePersistencia
				.carregaDadosJogadorNome(clientPaddockPack.getNomeJogador(), session);
		if (jogadorDadosSrv == null) {
			jogadorDadosSrv = controlePersistencia.carregaDadosJogadorEmail(clientPaddockPack.getNomeJogador(),
					session);
		}
		if (jogadorDadosSrv == null) {
			return new MsgSrv(Lang.msg("238"));
		} else if (jogadorDadosSrv.getSenha() == null
				|| !jogadorDadosSrv.getSenha().equals(clientPaddockPack.getSenhaJogador())) {
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
		synchronized (dadosPaddock.getClientes()) {
			for (Iterator<SessaoCliente> iter = dadosPaddock.getClientes().iterator(); iter.hasNext();) {
				SessaoCliente element = iter.next();
				if (element.getId().equals(jogadorDadosSrv.getIdGoogle())) {
					sessaoCliente = element;
					break;
				}
			}
		}
		if (sessaoCliente == null) {
			sessaoCliente = new SessaoCliente();
			TokenGenerator tokenGenerator = new TokenGenerator();
			sessaoCliente.setToken(tokenGenerator.nextSessionId());
			sessaoCliente.setNomeJogador(jogadorDadosSrv.getNome());
			sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
			dadosPaddock.add(sessaoCliente);
		}
		controleJogosServer.removerCliente(sessaoCliente);
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(sessaoCliente);
		srvPaddockPack.setSenhaCriada(senha);

		return srvPaddockPack;
	}

	public Object criarSessaoVisitante() {
		try {
			SessaoCliente sessaoCliente = new SessaoCliente();
			TokenGenerator tokenGenerator = new TokenGenerator();
			sessaoCliente.setToken(tokenGenerator.nextSessionId());
			sessaoCliente.setNomeJogador("ManE-" + (contadorVistantes++));
			sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
			sessaoCliente.setGuest(true);
			dadosPaddock.add(sessaoCliente);
			SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
			srvPaddockPack.setSessaoCliente(sessaoCliente);
			return srvPaddockPack;
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
	}

	public Object criarSessaoGoogle(String idGoogle, String nome, String urlFoto, String email) {
		try {
			List<SessaoCliente> clientes = dadosPaddock.getClientes();
			for (Iterator iterator = clientes.iterator(); iterator.hasNext();) {
				SessaoCliente sessaoCliente = (SessaoCliente) iterator.next();
				if (idGoogle.equals(sessaoCliente.getId())) {
					SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
					sessaoCliente.setNomeJogador(nome);
					sessaoCliente.setImagemJogador(urlFoto);
					sessaoCliente.setEmail(email);
					sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
					srvPaddockPack.setSessaoCliente(sessaoCliente);
					if (Util.isNullOrEmpty(sessaoCliente.getNomeJogador())) {
						return new MsgSrv(Lang.msg("064"));
					}
					salvarAcessoSessaoGoogle(sessaoCliente);
					return srvPaddockPack;
				}
			}
			SessaoCliente sessaoCliente = new SessaoCliente();
			TokenGenerator tokenGenerator = new TokenGenerator();
			sessaoCliente.setToken(tokenGenerator.nextSessionId());
			sessaoCliente.setNomeJogador(nome);
			sessaoCliente.setId(idGoogle);
			sessaoCliente.setImagemJogador(urlFoto);
			sessaoCliente.setEmail(email);
			sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
			sessaoCliente.setGuest(false);
			dadosPaddock.add(sessaoCliente);
			SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
			srvPaddockPack.setSessaoCliente(sessaoCliente);
			if (Util.isNullOrEmpty(sessaoCliente.getNomeJogador())) {
				return new MsgSrv(Lang.msg("064"));
			}
			salvarAcessoSessaoGoogle(sessaoCliente);
			return srvPaddockPack;
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
	}

	private void salvarAcessoSessaoGoogle(SessaoCliente sessaoCliente) {
		if (!Constantes.DATABASE) {
			return;
		}
		JogadorDadosSrv jogadorDadosSrv = null;
		Session session = controlePersistencia.getSession();
		try {
			jogadorDadosSrv = controlePersistencia.carregaDadosJogadorIdGoogle(sessaoCliente.getId(), session);
			boolean novo = false;
			if (jogadorDadosSrv == null) {
				jogadorDadosSrv = new JogadorDadosSrv();
				jogadorDadosSrv.setIdGoogle(sessaoCliente.getId());
				novo = true;
			} else {
				if (controleClassificacao.obterCarreiraSrv(jogadorDadosSrv.getToken()) == null) {
					novo = true;
				}
			}
			jogadorDadosSrv.setNome(sessaoCliente.getNomeJogador());
			jogadorDadosSrv.setEmail(sessaoCliente.getEmail());
			jogadorDadosSrv.setToken(sessaoCliente.getToken());
			jogadorDadosSrv.setImagemJogador(sessaoCliente.getImagemJogador());
			jogadorDadosSrv.setUltimoLogon(sessaoCliente.getUlimaAtividade());
			if (novo) {
				controlePersistencia.adicionarJogador(null, jogadorDadosSrv, session);
			} else {
				controlePersistencia.gravarDados(session, jogadorDadosSrv);
			}

		} catch (Exception e) {
			Logger.logarExept(e);
		} finally {
			if (session != null && session.isOpen())
				session.close();
		}

	}

	public static void main(String[] args) {
		System.out.println("#babaca".startsWith("#"));
	}

	public boolean removerCliente(SessaoCliente sessaoCliente) {
		return controleJogosServer.removerCliente(sessaoCliente);
	}

	public void removerSessao(SessaoCliente sessaoCliente) {
		removerCliente(sessaoCliente);
		dadosPaddock.remove(sessaoCliente);
	}

	public void initProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(PaddockConstants.class.getResourceAsStream("client.properties"));
		String versao = properties.getProperty("versao");
		if (versao.contains(".")) {
			this.versao = Integer.parseInt(versao.replaceAll("\\.", ""));
		}
	}

	public List<String> obterJogos() {
		try {
			if (dadosPaddock != null) {
				return dadosPaddock.getJogosCriados();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;

	}

	public SrvPaddockPack obterJogoPeloNome(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack == null || clientPaddockPack.getDadosJogoCriado() == null) {
			return null;
		}
		String nomeJogo = clientPaddockPack.getDadosJogoCriado().getNomeJogo();
		if (nomeJogo == null) {
			nomeJogo = clientPaddockPack.getNomeJogo();
		}
		JogoServidor jogoServidor = controleJogosServer.obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return null;
		}
		CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
				.obterCarreiraSrv(clientPaddockPack.getSessaoCliente().getToken());
		if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
			if (jogoServidor.isCorridaIniciada()) {
				return null;
			}
		}
		return controleJogosServer.preparaSrvPaddockPack(clientPaddockPack, jogoServidor);
	}

	public InterfaceJogo obterJogoPeloNome(String nomeJogo) {
		JogoServidor jogoServidor = controleJogosServer.obterJogoPeloNome(nomeJogo);
		return jogoServidor;
	}

	public BufferedImage obterCarroCima(String nomeJogo, String idPiloto) {
		JogoServidor jogoServidor = controleJogosServer.obterJogoPeloNome(nomeJogo);
		return jogoServidor.obterCarroCima(jogoServidor.obterPilotoPorId(idPiloto));
	}

	public BufferedImage obterCarroCimaSemAreofolio(String nomeJogo, String idPiloto) {
		JogoServidor jogoServidor = controleJogosServer.obterJogoPeloNome(nomeJogo);
		return jogoServidor.obterCarroCimaSemAreofolio(jogoServidor.obterPilotoPorId(idPiloto));
	}

	public DadosParciais obterDadosParciaisPilotos(String nomeJogo, String tokenJogador, String idPiloto) {
		try {
			return controleJogosServer.obterDadosParciaisPilotos(nomeJogo, tokenJogador, idPiloto);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public void sairJogoToken(String nomeJogo, String token, SessaoCliente sessaoCliente) {
		try {
			controleJogosServer.sairJogoToken(nomeJogo, token);
			sessaoCliente.limpaSelecao();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public SrvPaddockPack obterDadosToken(String token) {
		return controleJogosServer.obterDadosToken(token);
	}

	public List obterClassificacaoCircuito(String nomeCircuito) {
		return controleClassificacao.obterClassificacaoCircuito(nomeCircuito);
	}

	public Object obterClassificacaoTemporada(String temporadaSelecionada) {
		return controleClassificacao.obterClassificacaoTemporada(temporadaSelecionada);
	}

	public Object obterClassificacaoGeral() {
		return controleClassificacao.obterClassificacaoGeral();
	}

	public Object obterClassificacaoCampeonato() {
		return controleClassificacao.obterClassificacaoCampeonato();
	}

	public Object obterClassificacaoEquipes() {
		return controleClassificacao.obterClassificacaoEquipes();
	}

	public MsgSrv modoCarreira(String token, boolean modo) {
		return controlePersistencia.modoCarreira(token, modo);
	}

	public BufferedImage carroCimaTemporadaCarro(String temporada, String carro) {
		int idCarro = Util.intOr0(carro);
		if (temporada != null && temporada.length() == 6 && carro != null && carro.length() == 6) {
			return carroCimaCor(temporada, carro);
		} else {
			temporada = "t" + temporada;
			List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos().get(temporada);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getCarro().getId() == idCarro) {
					return carregadorRecursos.obterCarroCima(piloto, temporada);
				}
			}
			return null;
		}
	}

	public BufferedImage carroCimaSemAreofolioTemporadaCarro(String temporada, String carro) {
		int idCarro = Util.intOr0(carro);
		if (temporada != null && temporada.length() == 6 && carro != null && carro.length() == 6) {
			return carroCimaSemAreofolioCor(temporada, carro);
		} else {
			temporada = "t" + temporada;
			List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos().get(temporada);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getCarro().getId() == Util.intOr0(carro)) {
					return carregadorRecursos.obterCarroCimaSemAreofolio(piloto, temporada);
				}
			}
			return null;
		}
	}

	public BufferedImage capaceteTemporadaPiloto(String temporada, String pilotoId) {
		int idPiloto = Util.intOr0(pilotoId);
		if (temporada != null && temporada.length() == 6 && pilotoId != null && pilotoId.length() == 6) {
			return carreiraCapaceteCor(temporada, pilotoId);
		} else {
			temporada = "t" + temporada;
			List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos().get(temporada);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getId() == idPiloto) {
					return carregadorRecursos.obterCapacete(piloto, temporada);
				}
			}
			return null;
		}
	}

	public BufferedImage carroLadoTemporadaCarro(String temporada, String carro) {
		int idCarro = Util.intOr0(carro);
		if (temporada != null && temporada.length() == 6 && carro != null && carro.length() == 6) {
			return carroLadoCor(temporada, carro);
		} else {
			temporada = "t" + temporada;
			List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos().get(temporada);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getCarro().getId() == idCarro) {
					return carregadorRecursos.obterCarroLado(piloto, temporada);
				}
			}
			return null;
		}
	}

	public BufferedImage atualizarJogadoresOnlineCarreiraCarroLado(String token) {
		Piloto piloto = new Piloto();
		if (controleClassificacao.atualizarJogadoresOnlineCarreira(piloto, token, false)) {
			return carregadorRecursos.obterCarroLado(piloto, null);
		} else {
			return null;
		}
	}

	private BufferedImage carroCimaCor(String temporada, String carro) {
		Piloto piloto = gerarPilotoCarroCor(temporada, carro);
		return carregadorRecursos.obterCarroCima(piloto, null);
	}

	private BufferedImage carroLadoCor(String temporada, String carro) {
		Piloto piloto = gerarPilotoCarroCor(temporada, carro);
		return carregadorRecursos.obterCarroLado(piloto, null);
	}

	private BufferedImage carreiraCapaceteCor(String temporada, String idPiloto) {
		Piloto piloto = gerarPilotoCarroCor(temporada, idPiloto);
		return carregadorRecursos.obterCapacete(piloto, null);
	}

	private BufferedImage carroCimaSemAreofolioCor(String temporada, String carro) {
		Piloto piloto = gerarPilotoCarroCor(temporada, carro);
		return carregadorRecursos.obterCarroCimaSemAreofolio(piloto, null);
	}

	public Piloto gerarPilotoCarroCor(String cor1, String cor2) {
		Piloto piloto = new Piloto();
		Carro carro = new Carro();
		carro.setCor1(Util.hex2Rgb("#" + cor1));
		carro.setCor2(Util.hex2Rgb("#" + cor2));
		piloto.setCarro(carro);
		return piloto;
	}

	public CampeonatoTO obterCampeonatoEmAberto(String token) {
		return controleCampeonatoServidor.obterCampeonatoEmAbertoTO(token);
	}

	public CampeonatoTO obterCampeonatoId(String id) {
		return controleCampeonatoServidor.obterCampeonatoIdTO(id);
	}

	public CarreiraDadosSrv obterCarreiraSrv(String token) {
		return controleClassificacao.obterCarreiraSrv(token);
	}

	public CampeonatoSrv pesquisaCampeonato(String string) {
		Session session = controlePersistencia.getSession();
		try {
			return controlePersistencia.pesquisaCampeonato(session, string, true);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
		}
	}

	public Object finalizaCampeonato(CampeonatoTO campeonato, String token) {
		return controleCampeonatoServidor.finalizaCampeonato(campeonato, token);
	}

	public Object jogar(String temporada, String circuito, String idPiloto, String numVoltas, String tipoPneu,
			String combustivel, String asa, SessaoCliente sessaoCliente, String modoCarreira) {
		try {
			MsgSrv modoCarreiraRet = modoCarreira(sessaoCliente.getToken(), "true".equals(modoCarreira));
			if (modoCarreiraRet != null) {
				return modoCarreiraRet;
			}

			ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setSessaoCliente(sessaoCliente);
			DadosCriarJogo dadosCriarJogo = DadosCriarJogo.gerarJogoLetsRace(temporada, circuito, idPiloto, numVoltas,
					tipoPneu, combustivel, asa);
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			List<String> obterJogos = obterJogos();
			if (!obterJogos.isEmpty()) {
				clientPaddockPack.setNomeJogo(obterJogos.get(0));
			}
			SrvPaddockPack srvPaddockPack = null;
			Object statusJogo = null;
			statusJogo = obterJogoPeloNome(clientPaddockPack);
			/**
			 * Criar Jogo
			 */
			if (statusJogo == null) {
				statusJogo = criarJogo(clientPaddockPack);
				if (statusJogo instanceof MsgSrv || statusJogo instanceof ErroServ) {
					return statusJogo;
				}
			}

			srvPaddockPack = (SrvPaddockPack) statusJogo;

			if (srvPaddockPack != null && srvPaddockPack.getDadosCriarJogo() != null
					&& (!srvPaddockPack.getDadosCriarJogo().getTemporada().equals(dadosCriarJogo.getTemporada())
							|| !dadosCriarJogo.getCircuitoSelecionado()
									.equals(srvPaddockPack.getDadosCriarJogo().getCircuitoSelecionado()))) {
				return new MsgSrv(Lang.msg("existeJogoEmAndamando"));
			}

			/**
			 * Preenchento todos possiveis campos para nome do jogo Bagunça...
			 */
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			clientPaddockPack.getDadosJogoCriado().setNomeJogo(srvPaddockPack.getNomeJogoCriado());
			clientPaddockPack.setNomeJogo(srvPaddockPack.getNomeJogoCriado());

			/**
			 * Entrar Jogo
			 */
			if (statusJogo != null) {
				statusJogo = entrarJogo(clientPaddockPack);
				if (statusJogo instanceof MsgSrv || statusJogo instanceof ErroServ) {
					return statusJogo;
				}
				atualizarDadosVisao();
			}
			DadosJogo dadosJogo = (DadosJogo) obterDadosJogo(clientPaddockPack);
			return dadosJogo;
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
	}

	public Object jogarCampeonato(String tipoPneu, String combustivel, String asa, SessaoCliente sessaoCliente) {

		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setSessaoCliente(sessaoCliente);

			CampeonatoTO campeonato = obterCampeonatoEmAberto(sessaoCliente.getToken());

			String arquivoCircuito = campeonato.getArquivoCircuitoAtual();
			String idPiloto = campeonato.getIdPiloto();
			String numVoltas = campeonato.getQtdeVoltas().toString();
			String temporada = campeonato.getTemporada();

			if (campeonato.isModoCarreira()) {
				MsgSrv modoCarreiraRet = modoCarreira(sessaoCliente.getToken(), campeonato.isModoCarreira());
				if (modoCarreiraRet != null) {
					return modoCarreiraRet;
				} else {
					Map<String, TemporadasDefauts> tempDefsMap = carregadorRecursos.carregarTemporadasPilotosDefauts();
					TemporadasDefauts temporadasDefauts = tempDefsMap.get("t" + campeonato.getTemporada());
					List<Piloto> pilotos = temporadasDefauts.getPilotos();
					idPiloto = String.valueOf(pilotos.get(pilotos.size() - 1).getId());
				}
			}

			DadosCriarJogo dadosCriarJogo = DadosCriarJogo.gerarJogoLetsRace(temporada, arquivoCircuito, idPiloto,
					numVoltas, tipoPneu, combustivel, asa);

			dadosCriarJogo.setNomeCampeonato(campeonato.getNome());
			dadosCriarJogo.setRodadaCampeonato(campeonato.getRodadaCampeonato());
			dadosCriarJogo.setIdCampeonato(campeonato.getId());
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			SrvPaddockPack srvPaddockPack = null;
			Object statusJogo = null;
			/**
			 * Criar Jogo
			 */
			if (statusJogo == null) {
				statusJogo = criarJogo(clientPaddockPack);
				if (statusJogo instanceof MsgSrv || statusJogo instanceof ErroServ) {
					return statusJogo;
				}
			}

			srvPaddockPack = (SrvPaddockPack) statusJogo;

			/**
			 * Preenchento todos possiveis campos para nome do jogo Bagunça...
			 */
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			clientPaddockPack.getDadosJogoCriado().setNomeJogo(srvPaddockPack.getNomeJogoCriado());
			clientPaddockPack.setNomeJogo(srvPaddockPack.getNomeJogoCriado());

			/**
			 * Entrar Jogo
			 */
			if (statusJogo != null) {
				statusJogo = entrarJogo(clientPaddockPack);
				if (statusJogo instanceof MsgSrv || statusJogo instanceof ErroServ) {
					return statusJogo;
				}
				atualizarDadosVisao();
			}
			DadosJogo dadosJogo = (DadosJogo) obterDadosJogo(clientPaddockPack);
			return dadosJogo;
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}

	}

	public Object renovarSessaoVisitante(String token) {
		List<SessaoCliente> clientes = dadosPaddock.getClientes();
		for (Iterator iterator = clientes.iterator(); iterator.hasNext();) {
			SessaoCliente sessaoCliente = (SessaoCliente) iterator.next();
			if (token.equals(sessaoCliente.getToken())) {
				SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
				sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
				srvPaddockPack.setSessaoCliente(sessaoCliente);
				if (Util.isNullOrEmpty(sessaoCliente.getNomeJogador())) {
					return new MsgSrv(Lang.msg("064"));
				}
				return srvPaddockPack;
			}
		}
		return criarSessaoVisitante();
	}
}
