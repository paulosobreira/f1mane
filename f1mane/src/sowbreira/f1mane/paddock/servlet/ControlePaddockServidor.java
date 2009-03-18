/**
 * 
 */
package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogador;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.PaddockDadosSrv;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockServidor {

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
			if (Comandos.VALIDAR_LOGIN.equals(clientPaddockPack.getCommando())) {
				return criarSessao(clientPaddockPack);
			} else if (Comandos.RGISTRAR_LOGIN.equals(clientPaddockPack
					.getCommando())) {
				return registrarLogin(clientPaddockPack);
			}
			return processarComando(clientPaddockPack);
		}
		return new MsgSrv("Comando Invalido");
	}

	private Object registrarLogin(ClientPaddockPack clientPaddockPack) {
		synchronized (controlePersistencia.getPaddockDados()) {

			PaddockDadosSrv paddockDadosSrv = controlePersistencia
					.getPaddockDados();
			JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) paddockDadosSrv
					.getJogadoresMap().get(clientPaddockPack.getNomeJogador());

			// if (jogadorDadosSrv != null) {
			// return new MsgSrv("O Jogador "
			// + clientPaddockPack.getNomeJogador()
			// + " Já esta registrado");
			// }
			if (jogadorDadosSrv == null) {
				jogadorDadosSrv = new JogadorDadosSrv();
				jogadorDadosSrv.setNome(clientPaddockPack.getNomeJogador());
				jogadorDadosSrv.setSenha(clientPaddockPack.getSenhaJogador());
				jogadorDadosSrv.setEmail(clientPaddockPack.getEmailJogador());
				paddockDadosSrv.getJogadoresMap().put(
						jogadorDadosSrv.getNome(), jogadorDadosSrv);
			}
			return criarSessao(clientPaddockPack);
		}
	}

	private Object obterDadosParciaisPilotos(String[] args) {
		return controleJogosServer.obterDadosParciaisPilotos(args);
	}

	private Object obterPosicaoPilotos(Object object) {
		return controleJogosServer.obterPosicaoPilotos((String) object);
	}

	private Object processarComando(ClientPaddockPack clientPaddockPack) {
		SessaoCliente cliente = resgatarSessao(clientPaddockPack);
		if (cliente == null) {
			return (new MsgSrv("Usuario não tem Sessão"));
		}
		clientPaddockPack.setSessaoCliente(cliente);
		String commando = clientPaddockPack.getCommando();
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
		} else if (Comandos.MUDAR_MODO_BOX.equals(commando)) {
			return mudarModoBox(clientPaddockPack);
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
		} else if (Comandos.VER_CORRIDAS.equals(commando)) {
			return verCorridas(clientPaddockPack);
		}
		return "Comando invalido";
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
			return verificaUsuarioSessao(clientPaddockPack.getSessaoCliente()
					.getNomeJogador());
		} catch (Exception e) {
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
		if ("IA".equals(clientPaddockPack.getNomeJogador())
				|| "Ia".equals(clientPaddockPack.getNomeJogador())
				|| "ia".equals(clientPaddockPack.getNomeJogador())
				|| "iA".equals(clientPaddockPack.getNomeJogador())
				|| "".equals(clientPaddockPack.getNomeJogador())) {
			return null;
		}
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
		} else {
			return (new MsgSrv("já exite uma sessão ativa para este usuario"));
		}
		synchronized (controlePersistencia.getPaddockDados()) {
			PaddockDadosSrv paddockDadosSrv = controlePersistencia
					.getPaddockDados();
			JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) paddockDadosSrv
					.getJogadoresMap().get(clientPaddockPack.getNomeJogador());
			if (jogadorDadosSrv == null) {
				// return new MsgSrv("O Jogador "
				// + clientPaddockPack.getNomeJogador()
				// + " NÃO está registrado.");
				registrarLogin(clientPaddockPack);
				jogadorDadosSrv = (JogadorDadosSrv) paddockDadosSrv
						.getJogadoresMap().get(
								clientPaddockPack.getNomeJogador());
			}
			// if (!jogadorDadosSrv.getSenha().equals(
			// clientPaddockPack.getSenhaJogador())) {
			// return new MsgSrv("Senha incorreta.");
			// }
			jogadorDadosSrv.setUltimoLogon(System.currentTimeMillis());

		}
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(cliente);

		return srvPaddockPack;
	}

	public static void main(String[] args) {
		String test = "#brual#llllp#";
		System.out.println(test.replaceAll("#", ""));
	}

	public void removerClienteInativo(SessaoCliente sessaoCliente) {
		controleJogosServer.removerClienteInativo(sessaoCliente);
		dadosPaddock.getClientes().remove(sessaoCliente);
	}
}
