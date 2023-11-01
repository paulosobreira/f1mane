package br.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import br.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Constantes;
import br.nnpe.Dia;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import br.f1mane.paddock.PaddockConstants;
import br.f1mane.paddock.ZipUtil;
import br.f1mane.paddock.entidades.Comandos;
import br.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import br.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import br.f1mane.paddock.entidades.TOs.DadosPaddock;
import br.f1mane.paddock.entidades.TOs.ErroServ;
import br.f1mane.paddock.entidades.TOs.MsgSrv;
import br.f1mane.paddock.entidades.TOs.SessaoCliente;
import br.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import br.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import br.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockCliente {
	private final URL url;
	private final AppletPaddock applet;
	private final String urlSufix = "/f1mane/ServletPaddock";
	private SessaoCliente sessaoCliente;
	private PaddockWindow paddockWindow;
	private Thread threadAtualizadora;
	private final List pacotes = new LinkedList();
	private JogoCliente jogoCliente;
	private MainFrame mainFrame;
	private boolean comunicacaoServer = true;
	private int latenciaMinima = Constantes.LATENCIA_MIN;
	private int latenciaReal;
	private long ultRetornoSucedido;
	private String versaoServidor = "";
	final DecimalFormat decimalFormat = new DecimalFormat("#,###");

	public ControlePaddockCliente(URL url, AppletPaddock panel) {
		this.url = url;
		this.applet = panel;
	}

	public void init() {
		try {
			mainFrame = new MainFrame(applet, applet.getCodeBase().toString());
			mainFrame.setVisible(false);
			mainFrame.desbilitarMenusModoOnline();
			threadAtualizadora = new Thread(new Runnable() {
				public void run() {
					boolean interrupt = false;
					try {
						while (isComunicacaoServer()) {
							Thread.sleep(Util.intervalo(4000, 6000));
							atualizaVisao(paddockWindow);
						}
					} catch (Exception e) {
						interrupt = true;
						Logger.logarExept(e);
					}
				}
			});
			threadAtualizadora.setPriority(Thread.MIN_PRIORITY);
			paddockWindow = new PaddockWindow(this);
			atualizaVisao(paddockWindow);
			applet.getFrame().setLayout(new BorderLayout());
			applet.getFrame().add(paddockWindow.getMainPanel(), BorderLayout.CENTER);
			applet.getFrame().setSize(800, 410);
			applet.getFrame().pack();
			applet.getFrame().setTitle("Fl-Mane Paddock");
			applet.getFrame().setResizable(false);
			applet.getFrame().setVisible(true);
			threadAtualizadora.start();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public Object enviarObjeto(Object enviar) {
		return enviarObjeto(enviar, false);
	}

	public Object enviarObjeto(Object enviar, boolean timeout) {
		try {
            String protocol = url.getProtocol();
			String host = url.getHost();
			int port = url.getPort();
			URL dataUrl;
			long envioT = System.currentTimeMillis();
			// Gerar Lag
			// Thread.sleep(Util.intervalo(1500, 2000));
			Object retorno = null;
			dataUrl = new URL(protocol, host, port, urlSufix);

			URLConnection connection = dataUrl.openConnection();

			try {
				connection.setUseCaches(false);
				connection.setDoOutput(true);

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream stream = new ObjectOutputStream(byteArrayOutputStream);
				if (latenciaReal > 0 && timeout && latenciaReal > latenciaMinima) {
					connection.setReadTimeout(latenciaReal);
				}
				stream.writeObject(enviar);
				stream.flush();
				connection.setRequestProperty("Content-Length", String.valueOf(byteArrayOutputStream.size()));
				connection.setRequestProperty("Content-Length", "application/x-www-form-urlencoded");
				connection.getOutputStream().write(byteArrayOutputStream.toByteArray());
				if (PaddockConstants.modoZip) {
					retorno = ZipUtil.descompactarObjeto(connection.getInputStream());
				} else {
					ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
					retorno = ois.readObject();
				}
			} catch (java.net.SocketTimeoutException e) {
				return null;
			} catch (java.io.IOException e) {
				return null;
			}
			long retornoT = System.currentTimeMillis();
			if (!timeout) {
				atualizarLantenciaMinima(envioT, retornoT);
			}
			if (retorno instanceof ErroServ) {
				ErroServ erroServ = (ErroServ) retorno;
				JOptionPane.showMessageDialog(applet.getFrame(), Lang.decodeTexto(erroServ.obterErroFormatado()),
						Lang.msg("060"), JOptionPane.ERROR_MESSAGE);
			}
			if (retorno instanceof MsgSrv) {
				MsgSrv msgSrv = (MsgSrv) retorno;
				JOptionPane.showMessageDialog(applet.getFrame(), Lang.decodeTexto(msgSrv.getMessageString()),
						Lang.msg("061"), JOptionPane.INFORMATION_MESSAGE);
			}
			ultRetornoSucedido = retornoT;
			return retorno;
		} catch (Exception e) {
			e.printStackTrace();
			if ((System.currentTimeMillis() - ultRetornoSucedido) > 120000) {
				setComunicacaoServer(false);
				StackTraceElement[] trace = e.getStackTrace();
				StringBuilder retorno = new StringBuilder();
				int size = ((trace.length > 10) ? 10 : trace.length);

				for (int i = 0; i < size; i++)
					retorno.append(trace[i]).append("\n");
				JOptionPane.showMessageDialog(applet.getFrame(), retorno.toString(), Lang.msg("059"),
						JOptionPane.ERROR_MESSAGE);
				if (jogoCliente != null) {
					jogoCliente.matarTodasThreads();
				}
				if (getThreadAtualizadora() != null) {
					getThreadAtualizadora().interrupt();
				}
			}
		}

		return null;
	}

	private void atualizarLantenciaMinima(long envioT, long retornoT) {
		if (pacotes.size() > 10) {
			pacotes.remove(0);
		}
		pacotes.add(new Long(retornoT - envioT));
		if (pacotes.size() >= 10) {
			long somatorio = 0;
			for (Iterator iter = pacotes.iterator(); iter.hasNext();) {
				Long longElement = (Long) iter.next();
				somatorio += longElement.longValue();
			}
			int media = (int) (somatorio / 10);
			if (media > Constantes.LATENCIA_MAX) {
				setLatenciaMinima(Constantes.LATENCIA_MAX);
			} else {
				setLatenciaMinima(media);
			}
			if (media < Constantes.LATENCIA_MIN)
				setLatenciaMinima(Constantes.LATENCIA_MIN);
			else if (media < Constantes.LATENCIA_MAX) {
				setLatenciaMinima(media);
			}
			setLatenciaReal(media);
			paddockWindow.atualizaInfo();
		}
	}

	public void atualizaVisao(PaddockWindow paddockWindow) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.ATUALIZAR_VISAO, sessaoCliente);

		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			Logger.logar("ATUALIZAR_VISAO ret == null");
			return;
		}
		if (retornoNaoValido(ret)) {
			Logger.logar("ATUALIZAR_VISAO ret == null");
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;

		DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
		if (paddockWindow != null) {
			paddockWindow.atualizar(dadosPaddock);
		}

	}

	public PaddockWindow getPaddockWindow() {
		return paddockWindow;
	}

	public void setPaddockWindow(PaddockWindow paddockWindow) {
		this.paddockWindow = paddockWindow;
	}

	public void enviarTexto(String text) {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.ENVIAR_TEXTO, sessaoCliente);
		clientPaddockPack.setTexto(text);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			Logger.logar("ENVIAR_TEXTO ret=null");
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;

		DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
		paddockWindow.atualizar(dadosPaddock);
	}

	public void criarJogo() {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.CRIAR_JOGO, sessaoCliente);
			PainelEntradaCliente controleCriacaoCorridaSimples = new PainelEntradaCliente(mainFrame,
					sessaoCliente.getNomeJogador());
			DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
			if (!controleCriacaoCorridaSimples.gerarDadosCriarJogo(dadosCriarJogo)) {
				return;
			}
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			Object ret = enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				Logger.logar("CRIAR_JOGO ret == null");
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
			JogoCliente jogoCliente = new JogoCliente(dadosCriarJogo.getTemporada());
			jogoCliente.setMainFrame(mainFrame);
			Thread.sleep(500);
			entarJogo(srvPaddockPack.getNomeJogoCriado());
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet.getFrame(), e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	public void entarJogo(Object object) {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}

			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_DETALHES_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo((String) object);
			Object ret = enviarObjeto(clientPaddockPack);

			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				Logger.logar("VER_DETALHES_JOGO ret == null");
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			JPanel panelJogadores = paddockWindow.gerarPainelJogadores(srvPaddockPack.getDetalhesJogo());
			JPanel panelJogo = paddockWindow.gerarPainelJogo(srvPaddockPack.getDetalhesJogo());
			String circuito = srvPaddockPack.getDetalhesJogo().getDadosCriarJogo().getCircuitoSelecionado();
			panelJogadores.setBorder(new TitledBorder("Jogadores") {
				@Override
				public String getTitle() {
					return Lang.msg("117");
				}
			});
			panelJogo.setBorder(new TitledBorder("Dados Inicio do Jogo") {
				@Override
				public String getTitle() {
					return Lang.msg("122");
				}
			});
			JPanel panelJogoCriado = new JPanel(new GridLayout(1, 2));
			panelJogoCriado.add(panelJogo);
			panelJogoCriado.add(panelJogadores);

			clientPaddockPack = new ClientPaddockPack(Comandos.ENTRAR_JOGO, sessaoCliente);
			DadosCriarJogo dadosParticiparJogo = new DadosCriarJogo();
			String infoJogo = (String) object;
			String temporada = infoJogo.split("-")[1];
			dadosParticiparJogo.setNomeJogo(infoJogo);
			Logger.logar("Temporada cliente Entrar jogo " + "t" + temporada);
			jogoCliente = new JogoCliente("t" + temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(jogoCliente.getPilotos(),
					jogoCliente.getCircuitos(), mainFrame, sessaoCliente.getNomeJogador());

			if (!painelEntradaCliente.gerarDadosEntrarJogo(dadosParticiparJogo, panelJogoCriado, circuito, temporada,
					srvPaddockPack.getDetalhesJogo().getDadosCriarJogo().getClima())) {
				return;
			}
			if ((Carro.TIPO_PNEU_CHUVA.equals(dadosParticiparJogo.getTpPneu())
					&& !Clima.CHUVA.equals(srvPaddockPack.getDetalhesJogo().getDadosCriarJogo().getClima()))
					|| (!Carro.TIPO_PNEU_CHUVA.equals(dadosParticiparJogo.getTpPneu())
							&& Clima.CHUVA.equals(srvPaddockPack.getDetalhesJogo().getDadosCriarJogo().getClima()))) {
				int showConfirmDialog = JOptionPane.showConfirmDialog(applet.getFrame(), Lang.msg("pneuIncompativel"),
						Lang.msg("alerta"), JOptionPane.YES_NO_OPTION);
				if (JOptionPane.YES_OPTION != showConfirmDialog) {
					return;
				}
			}
			clientPaddockPack.setDadosCriarJogo(dadosParticiparJogo);
			ret = enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				return;
			}
			srvPaddockPack = (SrvPaddockPack) ret;
			DadosCriarJogo dadosCriarJogo = srvPaddockPack.getDadosCriarJogo();
			dadosCriarJogo.setAsa(dadosParticiparJogo.getAsa());
			dadosCriarJogo.setCombustivel(dadosParticiparJogo.getCombustivel());
			dadosCriarJogo.setTpPneu(dadosParticiparJogo.getTpPneu());
			jogoCliente.iniciarJogoOnline(srvPaddockPack.getDadosCriarJogo(), dadosParticiparJogo.getNomeJogo(), this,
					sessaoCliente, dadosParticiparJogo.getPiloto());
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet.getFrame(), e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	public void verDetalhesJogo(Object object) throws Exception {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_DETALHES_JOGO, sessaoCliente);

		clientPaddockPack.setNomeJogo((String) object);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			Logger.logar("VER_DETALHES_JOGO ret == null");
			return;
		}

		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		String temporada = srvPaddockPack.getDetalhesJogo().getDadosCriarJogo().getTemporada();
		JogoCliente jogoCliente = new JogoCliente(temporada);
		jogoCliente.setMainFrame(mainFrame);
		PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(jogoCliente.getPilotos(),
				jogoCliente.getCircuitos(), mainFrame, sessaoCliente.getNomeJogador());
		paddockWindow.mostrarDetalhes(srvPaddockPack.getDetalhesJogo(), painelEntradaCliente);
	}

	public void iniciarJogo() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		if (jogoCliente == null) {
			JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("063"), "Erro", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.INICIAR_JOGO, sessaoCliente);

		Object ret = enviarObjeto(clientPaddockPack);
	}

	public void sairPaddock() {
		if (threadAtualizadora != null) {
			threadAtualizadora.interrupt();
		}
		if (mainFrame != null) {
			WindowListener[] windowListeners = mainFrame.getWindowListeners();
			for (int i = 0; i < windowListeners.length; i++) {
				mainFrame.removeWindowListener(windowListeners[i]);
			}
		}
		if (jogoCliente != null) {
			jogoCliente.abandonar();
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.SAIR_PADDOCK, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
	}

	public void verDetalhesJogador(Object object) {
		paddockWindow.mostrarDetalhesJogador(object);

	}

	public void logarGuest() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setComando(Comandos.GUEST_LOGIN_APPLET);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			Logger.logar("GUEST_LOGIN_APPLET ret == null");
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		sessaoCliente = srvPaddockPack.getSessaoCliente();
		atualizaVisao(paddockWindow);
	}

	public boolean registrarUsuario(FormEntrada formEntrada) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setRecuperar(false);
		clientPaddockPack.setComando(Comandos.REGISTRAR_LOGIN);
		clientPaddockPack.setNomeJogador(formEntrada.getNome().getText());
		if ("IA".equals(clientPaddockPack.getNomeJogador()) || "Ia".equals(clientPaddockPack.getNomeJogador())
				|| "ia".equals(clientPaddockPack.getNomeJogador()) || "iA".equals(clientPaddockPack.getNomeJogador())) {
			JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("064"), Lang.msg("064"),
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			if (!Util.isNullOrEmpty(new String(formEntrada.getSenha().getPassword()))) {
				clientPaddockPack.setSenhaJogador(Util.md5(new String(formEntrada.getSenha().getPassword())));
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet.getFrame(), e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
		if (!Util.isNullOrEmpty(formEntrada.getNomeRecuperar().getText())
				|| !Util.isNullOrEmpty(formEntrada.getEmailRecuperar().getText())) {
			clientPaddockPack.setNomeJogador(formEntrada.getNomeRecuperar().getText());
			clientPaddockPack.setEmailJogador(formEntrada.getEmailRecuperar().getText());
			clientPaddockPack.setRecuperar(true);
		}
		if (!Util.isNullOrEmpty(formEntrada.getNomeRegistrar().getText())
				|| !Util.isNullOrEmpty(formEntrada.getEmailRegistrar().getText())) {
			int resultado = 0;
			try {
				resultado = Integer.parseInt(formEntrada.getResultadorConta().getText());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("resultadoContaErrado"), Lang.msg("erro"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}

			if ((formEntrada.getConta1() + formEntrada.getConta2()) != resultado) {
				JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("resultadoContaErrado"), Lang.msg("erro"),
						JOptionPane.ERROR_MESSAGE);
				return false;

			}
			clientPaddockPack.setNomeJogador(formEntrada.getNomeRegistrar().getText());
			clientPaddockPack.setEmailJogador(formEntrada.getEmailRegistrar().getText());
		}
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			Logger.logar("REGISTRAR_LOGIN ret == null");
			return false;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		SessaoCliente cliente = srvPaddockPack.getSessaoCliente();
		this.sessaoCliente = cliente;
		if (srvPaddockPack.getSenhaCriada() != null) {
			lembrarSenha(cliente.getNomeJogador(), srvPaddockPack.getSenhaCriada());
			JOptionPane.showMessageDialog(applet.getFrame(),
					Lang.msg("senhaGerada", new String[] { cliente.getNomeJogador(), srvPaddockPack.getSenhaCriada() }),
					Lang.msg("guardeSenhaGerada"), JOptionPane.INFORMATION_MESSAGE);
		}
		return true;
	}

	public SessaoCliente getSessaoCliente() {
		return sessaoCliente;
	}

	public void verClassificacao() {
		JComboBox anos = new JComboBox();
		Dia dia = new Dia();
		int anoAutual = dia.getYear();
		while (anoAutual >= 2009) {
			anos.addItem(new Integer(anoAutual));
			anoAutual--;
		}
		JOptionPane.showMessageDialog(this.mainFrame, anos, Lang.msg("anoRanking"), JOptionPane.QUESTION_MESSAGE);
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_CLASSIFICACAO, sessaoCliente);
		clientPaddockPack.setDataObject(anos.getSelectedItem());
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			Logger.logar("VER_CLASSIFICACAO ret == null");
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		List listaDadosJogador = srvPaddockPack.getListaDadosJogador();
		clientPaddockPack = new ClientPaddockPack(Comandos.VER_CONTRUTORES, sessaoCliente);
		clientPaddockPack.setDataObject(anos.getSelectedItem());
		ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("062"), "Erro", JOptionPane.ERROR_MESSAGE);
			return;
		}
		srvPaddockPack = (SrvPaddockPack) ret;
		List listaConstrutoresCarros = srvPaddockPack.getListaConstrutoresCarros();
		List listaConstrutoresPilotos = srvPaddockPack.getListaConstrutoresPilotos();
		FormClassificacao formClassificacao = new FormClassificacao(listaDadosJogador, this, listaConstrutoresCarros,
				listaConstrutoresPilotos);
		formClassificacao.setAnoClassificacao((Integer) anos.getSelectedItem());
		JOptionPane.showMessageDialog(applet.getFrame(), formClassificacao, Lang.msg("065"), JOptionPane.PLAIN_MESSAGE);

	}

	public void verConstrutores() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_CONTRUTORES, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			Logger.logar("VER_CONTRUTORES ret == null");
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		FormConstrutores formConstrutores = new FormConstrutores(srvPaddockPack.getListaConstrutoresCarros(),
				srvPaddockPack.getListaConstrutoresPilotos());
		JOptionPane.showMessageDialog(applet.getFrame(), formConstrutores, Lang.msg("244"), JOptionPane.PLAIN_MESSAGE);

	}

	public List obterListaCorridas(String jogadorSel, Integer anoClassificacao) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_CORRIDAS, sessaoCliente);
		clientPaddockPack.setNomeJogador(jogadorSel);
		clientPaddockPack.setDataObject(anoClassificacao);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return new ArrayList();
		}
		if (ret == null) {
			Logger.logar("VER_CORRIDAS ret == null");
			return new ArrayList();
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		return srvPaddockPack.getListaCorridasJogador();
	}

	public void logar() {

		FormEntrada formEntrada = new FormEntrada(this);
		try {
//			PersistenceService persistenceService = (PersistenceService) ServiceManager
//					.lookup("javax.jnlp.PersistenceService");
//			FileContents fileContents = persistenceService.get(applet.getCodeBase());
//			if (fileContents == null) {
//				Logger.logar(" fileContents == null  ");
//			}
//			ObjectInputStream ois = new ObjectInputStream(fileContents.getInputStream());
//			Map map = (Map) ois.readObject();
//			String login = (String) map.get("login");
//			String pass = (String) map.get("pass");
//			if (!Util.isNullOrEmpty(pass) && !Util.isNullOrEmpty(login)) {
//				formEntrada.getNome().setText(login);
//				formEntrada.getSenha().setText(pass);
//				formEntrada.getLembrar().setSelected(true);
//			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}

		formEntrada.setToolTipText(Lang.msg("066"));
		int result = JOptionPane.showConfirmDialog(applet.getFrame(), formEntrada, Lang.msg("066"),
				JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			boolean registrarUsuario = registrarUsuario(formEntrada);
			if (registrarUsuario) {
				if (formEntrada.getLembrar().isSelected()) {
					lembrarSenha(formEntrada.getNome().getText(),
							String.valueOf((formEntrada.getSenha().getPassword())));
				}
				atualizaVisao(paddockWindow);
			}
		}

	}

	private void lembrarSenha(String nome, String senha) {
		if (Util.isNullOrEmpty(nome) || Util.isNullOrEmpty(senha)) {
			return;
		}
		try {
//			PersistenceService persistenceService = (PersistenceService) ServiceManager
//					.lookup("javax.jnlp.PersistenceService");
//			FileContents fileContents = null;
//			try {
//				fileContents = persistenceService.get(applet.getCodeBase());
//			} catch (Exception e) {
//				persistenceService.create(applet.getCodeBase(), 1024);
//				fileContents = persistenceService.get(applet.getCodeBase());
//			}
//
//			if (fileContents == null) {
//				Logger.logar(" fileContents == null  ");
//
//			}
//
//			Map map = new HashMap();
//			map.put("login", nome);
//			map.put("pass", senha);
//			ObjectOutputStream stream = new ObjectOutputStream(fileContents.getOutputStream(true));
//			stream.writeObject(map);
//			stream.flush();

		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public boolean isComunicacaoServer() {
		return comunicacaoServer;
	}

	public void setComunicacaoServer(boolean comunicacaoServer) {
		this.comunicacaoServer = comunicacaoServer;
	}

	public int getLatenciaMinima() {
		return latenciaMinima;
	}

	public void setLatenciaMinima(int latenciaMinima) {
		this.latenciaMinima = latenciaMinima;
	}

	public int getLatenciaReal() {
		return latenciaReal;
	}

	public void setLatenciaReal(int latenciaReal) {
		this.latenciaReal = latenciaReal;
	}

	public Thread getThreadAtualizadora() {
		return threadAtualizadora;
	}

	public void modoCarreira() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		FormCarreira formCarreira = new FormCarreira();
		formCarreira.setToolTipText(Lang.msg("246"));
		carregaCarreira(formCarreira);
		int result = JOptionPane.showConfirmDialog(applet.getFrame(), formCarreira, Lang.msg("246"),
				JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			int carLen = formCarreira.getNomeCarro().getText().length();
			int piloLen = formCarreira.getNomePiloto().getText().length();
			if (carLen == 0 || carLen > 20 || piloLen == 0 || piloLen > 20) {
				JOptionPane.showMessageDialog(applet.getFrame(), Lang.msg("249"), "Erro", JOptionPane.ERROR_MESSAGE);
			} else {
				atualizaCarreira(formCarreira);
			}

		}

	}

	private void carregaCarreira(FormCarreira formCarreira) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.VER_CARREIRA, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			Logger.logar("VER_CARREIRA ret == null");
			return;
		}
		CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) ret;
		formCarreira.getNomePiloto().setText(carreiraDadosSrv.getNomePiloto());
		formCarreira.getNomeCarro().setText(carreiraDadosSrv.getNomeCarro());
		formCarreira.getModoCarreira().setSelected(carreiraDadosSrv.isModoCarreira());
		formCarreira.getPtsPiloto().setValue(new Integer((int) carreiraDadosSrv.getPtsPiloto()));
		formCarreira.getPtsAeroDinamica().setValue(new Integer((int) carreiraDadosSrv.getPtsAerodinamica()));
		formCarreira.getPtsFreio().setValue(new Integer((int) carreiraDadosSrv.getPtsFreio()));
		formCarreira.getPtsCarro().setValue(new Integer((int) carreiraDadosSrv.getPtsCarro()));
		formCarreira.setPtsCarreira(carreiraDadosSrv.getPtsConstrutores());
		formCarreira.getNomePiloto().setText(carreiraDadosSrv.getNomePiloto());
		formCarreira.setCor1(carreiraDadosSrv.geraCor1());
		formCarreira.setCor2(carreiraDadosSrv.geraCor2());
		formCarreira.gerarCarroCima();
		formCarreira.gerarCarroLado();
	}

	public boolean retornoNaoValido(Object ret) {
		if (ret instanceof ErroServ || ret instanceof MsgSrv) {
			return true;
		}
		return false;
	}

	private void atualizaCarreira(FormCarreira formCarreira) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.ATUALIZA_CARREIRA, sessaoCliente);
		CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
		carreiraDadosSrv.setNomePiloto(formCarreira.getNomePiloto().getText());
		carreiraDadosSrv.setNomeCarro(formCarreira.getNomeCarro().getText());
		carreiraDadosSrv.setPtsCarro((Integer) formCarreira.getPtsCarro().getValue());
		carreiraDadosSrv.setPtsAerodinamica((Integer) formCarreira.getPtsAeroDinamica().getValue());
		carreiraDadosSrv.setPtsFreio((Integer) formCarreira.getPtsFreio().getValue());
		carreiraDadosSrv.setPtsPiloto((Integer) formCarreira.getPtsPiloto().getValue());
		carreiraDadosSrv.setPtsConstrutores(formCarreira.getPtsCarreira());
		carreiraDadosSrv.setModoCarreira(formCarreira.getModoCarreira().isSelected());
		carreiraDadosSrv.setC1B(formCarreira.getCor1().getBlue());
		carreiraDadosSrv.setC1R(formCarreira.getCor1().getRed());
		carreiraDadosSrv.setC1G(formCarreira.getCor1().getGreen());
		carreiraDadosSrv.setC2R(formCarreira.getCor2().getRed());
		carreiraDadosSrv.setC2G(formCarreira.getCor2().getGreen());
		carreiraDadosSrv.setC2B(formCarreira.getCor2().getBlue());
		clientPaddockPack.setJogadorDadosSrv(carreiraDadosSrv);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(6, 2));
		panel.add(new JLabel("Pontos Carreira:") {
			@Override
			public String getText() {
				return Lang.msg("266");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv.getPtsConstrutores())));
		panel.add(new JLabel("Habilidade Piloto:") {
			@Override
			public String getText() {
				return Lang.msg("255");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv.getPtsPiloto())));
		panel.add(new JLabel("Pontencia Carro:") {
			@Override
			public String getText() {
				return Lang.msg("256");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv.getPtsCarro())));

		panel.add(new JLabel("Aero dinâmica Carro:") {
			@Override
			public String getText() {
				return Lang.msg("aerodinamicaCarro");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv.getPtsAerodinamica())));

		panel.add(new JLabel("Freio Carro:") {
			@Override
			public String getText() {
				return Lang.msg("freioCarro");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv.getPtsFreio())));

		int result = JOptionPane.showConfirmDialog(null, panel);
		if (JOptionPane.OK_OPTION == result) {
			enviarObjeto(clientPaddockPack);
		}
	}

	public void adicionaTextoJogo(String linhaChat) {
		if (jogoCliente != null) {
			jogoCliente.adicionarInfoDireto(linhaChat);
		}

	}

	public void criarCampeonato() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		ControleCampeonatoCliente controleCampeonato = new ControleCampeonatoCliente(paddockWindow.getMainPanel(),
				this);
		try {
			controleCampeonato.criarCampeonato();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void verCampeonato() {
		ControleCampeonatoCliente controleCampeonato = new ControleCampeonatoCliente(paddockWindow.getMainPanel(),
				this);
		try {
			controleCampeonato.verCampeonato();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void criarJogo(CampeonatoSrv campeonato, String nomeCircuito) {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}
			String temporada = "t" + campeonato.getTemporada();
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.CRIAR_JOGO, sessaoCliente);
			Logger.logar("criarJogo cliente " + temporada);
			JogoCliente jogoCliente = new JogoCliente(temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(jogoCliente.getPilotos(),
					jogoCliente.getCircuitos(), mainFrame, sessaoCliente.getNomeJogador());
			// campeonato.setCircuitoAtual(nomeCircuito);
			painelEntradaCliente.setCampeonato(campeonato);
			DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
			dadosCriarJogo.setNomeCampeonato(campeonato.getNome());
			dadosCriarJogo.setTemporada(temporada);
			if (!painelEntradaCliente.gerarDadosCriarJogo(dadosCriarJogo)) {
				return;
			}
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			Object ret = enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				Logger.logar("CRIAR_JOGO ret == null");
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
			Thread.sleep(500);
			entarJogo(srvPaddockPack.getNomeJogoCriado());
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet.getFrame(), e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void sairJogo() {
		if (jogoCliente == null) {
			return;
		}
		int result = JOptionPane.showConfirmDialog(applet.getFrame(), Lang.msg("sairJogo"), Lang.msg("095"),
				JOptionPane.OK_CANCEL_OPTION);
		if (JOptionPane.OK_OPTION == result) {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(Comandos.SAIR_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			if (jogoCliente != null) {
				jogoCliente.matarTodasThreads();
			}
			enviarObjeto(clientPaddockPack);
		}
		return;

	}

	public String getVersaoFormatado() {
		AppletPaddock appletPaddock = (AppletPaddock) applet;
		return appletPaddock.getVersaoFormatado() + " Srv" + versaoServidor;
	}

	public String getVersao() {
		AppletPaddock appletPaddock = (AppletPaddock) applet;
		return appletPaddock.getVersao();
	}

	public void verificaVersao() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setComando(Comandos.VERIFICA_VERSAO);
		clientPaddockPack.setVersao(Integer.parseInt(getVersao()));
		Logger.logar("Versao : " + clientPaddockPack.getVersao());
		Object ret = enviarObjeto(clientPaddockPack);
		if (!retornoNaoValido(ret) && ret != null) {
			versaoServidor = ":" + decimalFormat.format((Integer) ret);
		}
	}
}
