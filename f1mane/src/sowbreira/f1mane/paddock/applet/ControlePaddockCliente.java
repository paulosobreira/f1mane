package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.WindowListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jnlp.FileContents;
import javax.jnlp.PersistenceService;
import javax.jnlp.ServiceManager;
import javax.swing.JApplet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.paddock.PaddockConstants;
import sowbreira.f1mane.paddock.ZipUtil;
import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
import br.nnpe.Dia;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockCliente {
	private URL url;
	private JApplet applet;
	private String urlSufix;
	private SessaoCliente sessaoCliente;
	private PaddockWindow paddockWindow;
	private Thread threadAtualizadora;
	private List pacotes = new LinkedList();
	private JogoCliente jogoCliente;
	private MainFrame mainFrame;
	private boolean comunicacaoServer = true;
	private int latenciaMinima = 50;
	private int latenciaReal;
	private long ultRetornoSucedido;

	public ControlePaddockCliente(URL url, JApplet panel) {
		this.url = url;
		this.applet = panel;
		init();
	}

	public void init() {
		try {
			mainFrame = new MainFrame(applet, applet.getCodeBase().toString());
			mainFrame.setVisible(false);
			mainFrame.desbilitarMenusModoOnline();
			loadSufx();
			threadAtualizadora = new Thread(new Runnable() {
				public void run() {
					boolean interrupt = false;
					try {
						while (!interrupt && isComunicacaoServer()) {
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
			applet.setLayout(new BorderLayout());
			applet.add(paddockWindow.getMainPanel(), BorderLayout.CENTER);
			threadAtualizadora.start();
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	private void loadSufx() throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass()
				.getResourceAsStream("client.properties"));
		this.urlSufix = properties.getProperty("servidor");
	}

	public Object enviarObjeto(Object enviar) {
		return enviarObjeto(enviar, false);
	}

	public static void main(String[] args) throws Exception {
		// SimpleDateFormat dateFormat = new SimpleDateFormat(
		// "EEE, d MMM yyyy HH:mm:ss");
		// Date parse = dateFormat.parse("Sex, 18 Mar 2011 14:52:33");
		// System.out.println(parse);
		System.out.println((5000 + ((int) Math.random() * 1000)));

	}

	public Object enviarObjeto(Object enviar, boolean timeout) {
		try {
			if (urlSufix == null) {
				loadSufx();
			}
			String protocol = url.getProtocol();
			String host = url.getHost();
			int port = url.getPort();
			URL dataUrl;
			long envioT = System.currentTimeMillis();
			Object retorno = null;
			dataUrl = new URL(protocol, host, port, urlSufix);

			URLConnection connection = dataUrl.openConnection();

			try {
				connection.setUseCaches(false);
				connection.setDoOutput(true);

				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				ObjectOutputStream stream = new ObjectOutputStream(
						byteArrayOutputStream);
				if (latenciaReal > 0 && timeout
						&& latenciaReal > latenciaMinima)
					connection.setReadTimeout(latenciaReal);
				stream.writeObject(enviar);
				stream.flush();
				connection.setRequestProperty("Content-Length", String
						.valueOf(byteArrayOutputStream.size()));
				connection.setRequestProperty("Content-Length",
						"application/x-www-form-urlencoded");
				connection.getOutputStream().write(
						byteArrayOutputStream.toByteArray());
				if (PaddockConstants.modoZip) {
					retorno = ZipUtil.descompactarObjeto(connection
							.getInputStream());
				} else {
					ObjectInputStream ois = new ObjectInputStream(connection
							.getInputStream());
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
				JOptionPane.showMessageDialog(applet, Lang.decodeTexto(erroServ
						.obterErroFormatado()), Lang.msg("060"),
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if (retorno instanceof MsgSrv) {
				MsgSrv msgSrv = (MsgSrv) retorno;
				JOptionPane.showMessageDialog(applet, Lang.decodeTexto(msgSrv
						.getMessageString()), Lang.msg("061"),
						JOptionPane.INFORMATION_MESSAGE);
				return null;
			}
			ultRetornoSucedido = retornoT;
			return retorno;
		} catch (Exception e) {
			e.printStackTrace();
			if ((System.currentTimeMillis() - ultRetornoSucedido) > 120000) {
				setComunicacaoServer(false);
				StackTraceElement[] trace = e.getStackTrace();
				StringBuffer retorno = new StringBuffer();
				int size = ((trace.length > 10) ? 10 : trace.length);

				for (int i = 0; i < size; i++)
					retorno.append(trace[i] + "\n");
				JOptionPane.showMessageDialog(applet, retorno.toString(), Lang
						.msg("059"), JOptionPane.ERROR_MESSAGE);
				if (jogoCliente != null) {
					jogoCliente.matarTodasThreads();
				}
				Logger.logarExept(e);
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
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.ATUALIZAR_VISAO, sessaoCliente);

		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
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
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.ENVIAR_TEXTO, sessaoCliente);
		clientPaddockPack.setTexto(text);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;

		DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
		paddockWindow.atualizar(dadosPaddock);
	}

	public void criarJogo(String temporada) {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.CRIAR_JOGO, sessaoCliente);
			Logger.logar("criarJogo cliente " + temporada);
			JogoCliente jogoCliente = new JogoCliente(temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente controleCriacaoCorridaSimples = new PainelEntradaCliente(
					jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
					mainFrame, sessaoCliente.getNomeJogador(), jogoCliente);
			DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
			dadosCriarJogo.setTemporada(temporada);
			if (!controleCriacaoCorridaSimples
					.gerarDadosCriarJogo(dadosCriarJogo)) {
				return;
			}
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			Object ret = enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			if (srvPaddockPack == null) {
				return;
			}
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
			Thread.sleep(500);
			entarJogo(srvPaddockPack.getNomeJogoCriado());
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet, e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void entarJogo(Object object) {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}

			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.VER_DETALHES_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo((String) object);
			Object ret = enviarObjeto(clientPaddockPack);

			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;

			JPanel panelJogadores = paddockWindow
					.gerarPainelJogadores(srvPaddockPack.getDetalhesJogo());
			JPanel panelJogo = paddockWindow.gerarPainelJogo(srvPaddockPack
					.getDetalhesJogo());
			String circuito = srvPaddockPack.getDetalhesJogo()
					.getDadosCriarJogo().getCircuitoSelecionado();
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

			clientPaddockPack = new ClientPaddockPack(Comandos.ENTRAR_JOGO,
					sessaoCliente);
			DadosCriarJogo dadosParticiparJogo = new DadosCriarJogo();
			String infoJogo = (String) object;
			String nomeJogo = infoJogo.split("-")[0];
			String temporada = infoJogo.split("-")[1];
			dadosParticiparJogo.setNomeJogo(infoJogo);
			Logger.logar("TEmporada cliente Entrar jogo " + "t" + temporada);
			jogoCliente = new JogoCliente("t" + temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(
					jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
					mainFrame, sessaoCliente.getNomeJogador(), jogoCliente);

			if (!painelEntradaCliente.gerarDadosEntrarJogo(dadosParticiparJogo,
					panelJogoCriado, circuito, srvPaddockPack.getDetalhesJogo()
							.getDadosCriarJogo().getClima())) {
				return;
			}
			if ((Carro.TIPO_PNEU_CHUVA.equals(dadosParticiparJogo.getTpPnueu()) && !Clima.CHUVA
					.equals(srvPaddockPack.getDetalhesJogo()
							.getDadosCriarJogo().getClima().getClima()))
					|| (!Carro.TIPO_PNEU_CHUVA.equals(dadosParticiparJogo
							.getTpPnueu()) && Clima.CHUVA.equals(srvPaddockPack
							.getDetalhesJogo().getDadosCriarJogo().getClima()
							.getClima()))) {
				int showConfirmDialog = JOptionPane.showConfirmDialog(applet,
						Lang.msg("pneuIncompativel"), Lang.msg("alerta"),
						JOptionPane.YES_NO_OPTION);
				if (JOptionPane.YES_OPTION != showConfirmDialog) {
					return;
				}
			}
			clientPaddockPack.setDadosCriarJogo(dadosParticiparJogo);
			ret = enviarObjeto(clientPaddockPack);
			if (ret == null) {
				return;
			}
			srvPaddockPack = (SrvPaddockPack) ret;
			DadosCriarJogo dadosCriarJogo = srvPaddockPack.getDadosCriarJogo();
			dadosCriarJogo.setAsa(dadosParticiparJogo.getAsa());
			dadosCriarJogo.setCombustivel(dadosParticiparJogo.getCombustivel());
			dadosCriarJogo.setTpPnueu(dadosParticiparJogo.getTpPnueu());
			jogoCliente.iniciarJogoOnline(srvPaddockPack.getDadosCriarJogo(),
					dadosParticiparJogo.getNomeJogo(), this, sessaoCliente,
					dadosParticiparJogo.getPiloto());
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet, e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void verDetalhesJogo(Object object) throws Exception {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_DETALHES_JOGO, sessaoCliente);

		clientPaddockPack.setNomeJogo((String) object);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		String temporada = srvPaddockPack.getDetalhesJogo().getDadosCriarJogo()
				.getTemporada();
		JogoCliente jogoCliente = new JogoCliente(temporada);
		jogoCliente.setMainFrame(mainFrame);
		PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(
				jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
				mainFrame, sessaoCliente.getNomeJogador(), jogoCliente);
		paddockWindow.mostrarDetalhes(srvPaddockPack.getDetalhesJogo(),
				painelEntradaCliente);
	}

	public void iniciarJogo() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		if (jogoCliente == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("063"), "Erro",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.INICIAR_JOGO, sessaoCliente);

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
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.SAIR_PADDOCK, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
	}

	public void verDetalhesJogador(Object object) {
		paddockWindow.mostrarDetalhesJogador(object);

	}

	public boolean registrarUsuario(FormEntrada formEntrada) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setTextoCapcha(formEntrada.getCapchaTexto());
		clientPaddockPack.setChaveCapcha(formEntrada.getCapchaChave());
		clientPaddockPack.setComando(Comandos.REGISTRAR_LOGIN);
		clientPaddockPack.setNomeJogador(formEntrada.getNome().getText());
		if ("IA".equals(clientPaddockPack.getNomeJogador())
				|| "Ia".equals(clientPaddockPack.getNomeJogador())
				|| "ia".equals(clientPaddockPack.getNomeJogador())
				|| "iA".equals(clientPaddockPack.getNomeJogador())) {
			JOptionPane.showMessageDialog(applet, Lang.msg("064"), Lang
					.msg("064"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			if (!Util.isNullOrEmpty(new String(formEntrada.getSenha()
					.getPassword()))) {
				clientPaddockPack.setSenhaJogador(Util.md5(new String(
						formEntrada.getSenha().getPassword())));
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet, e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
		clientPaddockPack.setEmailJogador(formEntrada.getEmail().getText());
		clientPaddockPack.setRecuperar(formEntrada.getRecuperar().isSelected());
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			return false;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		SessaoCliente cliente = srvPaddockPack.getSessaoCliente();
		this.sessaoCliente = cliente;
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
		JOptionPane.showMessageDialog(this.mainFrame, anos, Lang
				.msg("anoRanking"), JOptionPane.QUESTION_MESSAGE);
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CLASSIFICACAO, sessaoCliente);
		clientPaddockPack.setDataObject(anos.getSelectedItem());
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		List listaDadosJogador = srvPaddockPack.getListaDadosJogador();
		clientPaddockPack = new ClientPaddockPack(Comandos.VER_CONTRUTORES,
				sessaoCliente);
		clientPaddockPack.setDataObject(anos.getSelectedItem());
		ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		srvPaddockPack = (SrvPaddockPack) ret;
		List listaConstrutoresCarros = srvPaddockPack
				.getListaConstrutoresCarros();
		List listaConstrutoresPilotos = srvPaddockPack
				.getListaConstrutoresPilotos();
		FormClassificacao formClassificacao = new FormClassificacao(
				listaDadosJogador, this, listaConstrutoresCarros,
				listaConstrutoresPilotos);
		formClassificacao.setAnoClassificacao((Integer) anos.getSelectedItem());
		JOptionPane.showMessageDialog(applet, formClassificacao, Lang
				.msg("065"), JOptionPane.PLAIN_MESSAGE);

	}

	public void verConstrutores() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CONTRUTORES, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		FormConstrutores formConstrutores = new FormConstrutores(srvPaddockPack
				.getListaConstrutoresCarros(), srvPaddockPack
				.getListaConstrutoresPilotos());
		JOptionPane.showMessageDialog(applet, formConstrutores,
				Lang.msg("244"), JOptionPane.PLAIN_MESSAGE);

	}

	public List obterListaCorridas(String jogadorSel, Integer anoClassificacao) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CORRIDAS, sessaoCliente);
		clientPaddockPack.setNomeJogador(jogadorSel);
		clientPaddockPack.setDataObject(anoClassificacao);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			return new ArrayList();
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		return srvPaddockPack.getListaCorridasJogador();
	}

	public void logar() {

		FormEntrada formEntrada = new FormEntrada(this);
		try {
			PersistenceService persistenceService = (PersistenceService) ServiceManager
					.lookup("javax.jnlp.PersistenceService");
			FileContents fileContents = persistenceService.get(applet
					.getCodeBase());
			if (fileContents == null) {
				Logger.logar(" fileContents == null  ");
			}
			ObjectInputStream ois = new ObjectInputStream(fileContents
					.getInputStream());
			Map map = (Map) ois.readObject();
			String login = (String) map.get("login");
			String pass = (String) map.get("pass");
			if (!Util.isNullOrEmpty(pass) && !Util.isNullOrEmpty(login)) {
				formEntrada.getNome().setText(login);
				formEntrada.getSenha().setText(pass);
				formEntrada.getLembrar().setSelected(true);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}

		formEntrada.setToolTipText(Lang.msg("066"));
		int result = JOptionPane.showConfirmDialog(applet, formEntrada, Lang
				.msg("066"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			registrarUsuario(formEntrada);
			if (formEntrada.getLembrar().isSelected()) {
				try {
					PersistenceService persistenceService = (PersistenceService) ServiceManager
							.lookup("javax.jnlp.PersistenceService");
					FileContents fileContents = null;
					try {
						fileContents = persistenceService.get(applet
								.getCodeBase());
					} catch (Exception e) {
						persistenceService.create(applet.getCodeBase(), 1024);
						fileContents = persistenceService.get(applet
								.getCodeBase());
					}

					if (fileContents == null) {
						Logger.logar(" fileContents == null  ");

					}

					Map map = new HashMap();
					map.put("login", formEntrada.getNome().getText());
					map.put("pass", String.valueOf((formEntrada.getSenha()
							.getPassword())));
					ObjectOutputStream stream = new ObjectOutputStream(
							fileContents.getOutputStream(true));
					stream.writeObject(map);
					stream.flush();

				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
			atualizaVisao(paddockWindow);
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
		int result = JOptionPane.showConfirmDialog(applet, formCarreira, Lang
				.msg("246"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			int carLen = formCarreira.getNomeCarro().getText().length();
			int piloLen = formCarreira.getNomePiloto().getText().length();
			if (carLen == 0 || carLen > 20 || piloLen == 0 || piloLen > 20) {
				JOptionPane.showMessageDialog(applet, Lang.msg("249"), "Erro",
						JOptionPane.ERROR_MESSAGE);
			} else {
				atualizaCarreira(formCarreira);
			}

		}

	}

	private void carregaCarreira(FormCarreira formCarreira) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CARREIRA, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret == null) {
			JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) ret;
		formCarreira.getNomePiloto().setText(carreiraDadosSrv.getNomePiloto());
		formCarreira.getNomeCarro().setText(carreiraDadosSrv.getNomeCarro());
		formCarreira.getModoCarreira().setSelected(
				carreiraDadosSrv.isModoCarreira());
		formCarreira.getPtsPiloto().setValue(
				new Integer((int) carreiraDadosSrv.getPtsPiloto()));
		formCarreira.getPtsCarro().setValue(
				new Integer((int) carreiraDadosSrv.getPtsCarro()));
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
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.ATUALIZA_CARREIRA, sessaoCliente);
		CarreiraDadosSrv carreiraDadosSrv = new CarreiraDadosSrv();
		carreiraDadosSrv.setNomePiloto(formCarreira.getNomePiloto().getText());
		carreiraDadosSrv.setNomeCarro(formCarreira.getNomeCarro().getText());
		carreiraDadosSrv.setPtsCarro((Integer) formCarreira.getPtsCarro()
				.getValue());
		carreiraDadosSrv.setPtsPiloto((Integer) formCarreira.getPtsPiloto()
				.getValue());
		carreiraDadosSrv.setPtsConstrutores(formCarreira.getPtsCarreira());
		carreiraDadosSrv.setModoCarreira(formCarreira.getModoCarreira()
				.isSelected());
		carreiraDadosSrv.setC1B(formCarreira.getCor1().getBlue());
		carreiraDadosSrv.setC1R(formCarreira.getCor1().getRed());
		carreiraDadosSrv.setC1G(formCarreira.getCor1().getGreen());
		carreiraDadosSrv.setC2R(formCarreira.getCor2().getRed());
		carreiraDadosSrv.setC2G(formCarreira.getCor2().getGreen());
		carreiraDadosSrv.setC2B(formCarreira.getCor2().getBlue());
		clientPaddockPack.setJogadorDadosSrv(carreiraDadosSrv);
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 2));
		panel.add(new JLabel("Pontos Carreira:") {
			@Override
			public String getText() {
				return Lang.msg("266");
			}
		});
		panel.add(new JLabel(String.valueOf(carreiraDadosSrv
				.getPtsConstrutores())));
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
		int result = JOptionPane.showConfirmDialog(null, panel);
		if (JOptionPane.OK_OPTION == result) {
			Object ret = enviarObjeto(clientPaddockPack);
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
		ControleCampeonatoCliente controleCampeonato = new ControleCampeonatoCliente(
				paddockWindow.getMainPanel(), this);
		try {
			controleCampeonato.criarCampeonato();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void verCampeonato() {
		ControleCampeonatoCliente controleCampeonato = new ControleCampeonatoCliente(
				paddockWindow.getMainPanel(), this);
		try {
			controleCampeonato.verCampeonato();
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void criarJogo(Campeonato campeonato, String nomeCircuito) {
		try {
			if (sessaoCliente == null) {
				logar();
				return;
			}
			String temporada = "t" + campeonato.getTemporada();
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.CRIAR_JOGO, sessaoCliente);
			Logger.logar("criarJogo cliente " + temporada);
			JogoCliente jogoCliente = new JogoCliente(temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente painelEntradaCliente = new PainelEntradaCliente(
					jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
					mainFrame, sessaoCliente.getNomeJogador(), jogoCliente);
			campeonato.setCircuitoAtual(nomeCircuito);
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
				JOptionPane.showMessageDialog(applet, Lang.msg("062"), "Erro",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			if (srvPaddockPack == null) {
				return;
			}
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
			Thread.sleep(500);
			entarJogo(srvPaddockPack.getNomeJogoCriado());
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(applet, e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public void sairJogo() {
		if (jogoCliente == null) {
			return;
		}
		int result = JOptionPane.showConfirmDialog(applet,
				Lang.msg("sairJogo"), Lang.msg("095"),
				JOptionPane.OK_CANCEL_OPTION);
		if (JOptionPane.OK_OPTION == result) {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.SAIR_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			if (jogoCliente != null) {
				jogoCliente.matarTodasThreads();
			}
			enviarObjeto(clientPaddockPack);
		}
		return;

	}

	public String getVersao() {
		AppletPaddock appletPaddock = (AppletPaddock) applet;
		return appletPaddock.getVersao();
	}
}
