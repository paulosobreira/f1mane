package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Panel;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.MainFrame;
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
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class ControlePaddockCliente {
	private URL url;
	private Panel panel;
	private String urlSufix;
	private SessaoCliente sessaoCliente;
	private PaddockWindow paddockWindow;
	private Thread threadAtualizadora;
	private List pacotes = new LinkedList();
	private JogoCliente jogoCliente;
	private MainFrame mainFrame;
	private boolean comunicacaoServer = true;
	private int latenciaMinima = 120;
	private int latenciaReal;
	private int contadorErros = 0;

	public ControlePaddockCliente(URL url, Panel panel) {
		this.url = url;
		this.panel = panel;
		init();
	}

	public void init() {
		Properties properties = new Properties();
		try {
			mainFrame = new MainFrame(true);
			mainFrame.setVisible(false);
			mainFrame.desbilitarMenusModoOnline();

			properties.load(this.getClass().getResourceAsStream(
					"client.properties"));
			this.urlSufix = properties.getProperty("servidor");
			threadAtualizadora = new Thread(new Runnable() {

				public void run() {
					try {
						while (isComunicacaoServer()) {
							Thread.sleep((5000 + ((int) Math.random() * 1000)));
							atualizaVisao(paddockWindow);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});
			threadAtualizadora.setPriority(Thread.MIN_PRIORITY);
			paddockWindow = new PaddockWindow(this);
			atualizaVisao(paddockWindow);
			panel.setLayout(new BorderLayout());
			panel.add(paddockWindow.getMainPanel(), BorderLayout.CENTER);
			getThreadAtualizadora().start();
		} catch (Exception e) {
			e.printStackTrace();
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
				JOptionPane.showMessageDialog(panel, Lang.decodeTexto(erroServ
						.obterErroFormatado()), Lang.msg("060"),
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if (retorno instanceof MsgSrv) {
				MsgSrv msgSrv = (MsgSrv) retorno;
				JOptionPane.showMessageDialog(panel, Lang.decodeTexto(msgSrv
						.getMessageString()), Lang.msg("061"),
						JOptionPane.INFORMATION_MESSAGE);
				return null;
			}
			return retorno;
		} catch (Exception e) {
			setComunicacaoServer(false);
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(panel, retorno.toString(), Lang
					.msg("059"), JOptionPane.ERROR_MESSAGE);
			if (jogoCliente != null) {
				jogoCliente.matarTodasThreads();
			}

			e.printStackTrace();
			if (getThreadAtualizadora() != null) {
				getThreadAtualizadora().interrupt();
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
			if (media > 240) {
				setLatenciaMinima(240);
			} else {
				setLatenciaMinima(media);
			}
			if (media < 120)
				setLatenciaMinima(120);
			else if (media < 240) {
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
		if (ret == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
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
			jogoCliente = new JogoCliente(temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente controleCriacaoCorridaSimples = new PainelEntradaCliente(
					jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
					mainFrame, sessaoCliente.getNomeJogador());
			DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
			dadosCriarJogo.setTemporada(temporada);
			if (!controleCriacaoCorridaSimples
					.gerarDadosCriarJogo(dadosCriarJogo)) {
				return;
			}
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			Object ret = enviarObjeto(clientPaddockPack);
			if (ret == null) {
				JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			jogoCliente = new JogoCliente(dadosCriarJogo.getTemporada());
			jogoCliente.setMainFrame(mainFrame);
			if (srvPaddockPack == null) {
				return;
			}
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
			jogoCliente.iniciarJogoOnline(srvPaddockPack.getDadosCriarJogo(),
					srvPaddockPack.getNomeJogoCriado(), this, sessaoCliente,
					dadosCriarJogo.getPiloto());
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(panel, e.getMessage(), "Erro",
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
			if (ret == null) {
				JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			JPanel panelJogadores = paddockWindow
					.gerarPainelJogadores(srvPaddockPack.getDetalhesJogo());
			JPanel panelJogo = paddockWindow.gerarPainelJogo(srvPaddockPack
					.getDetalhesJogo());
			panelJogadores.setBorder(new TitledBorder("Jogadores") {
				@Override
				public String getTitle() {
					// TODO Auto-generated method stub
					return Lang.msg("117");
				}
			});
			panelJogo.setBorder(new TitledBorder("Dados Inicio do Jogo") {
				@Override
				public String getTitle() {
					// TODO Auto-generated method stub
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
			jogoCliente = new JogoCliente("t" + temporada);
			jogoCliente.setMainFrame(mainFrame);
			PainelEntradaCliente painelCriacaoCorridaSimples = new PainelEntradaCliente(
					jogoCliente.getPilotos(), jogoCliente.getCircuitos(),
					mainFrame, sessaoCliente.getNomeJogador());

			if (!painelCriacaoCorridaSimples.gerarDadosEntrarJogo(
					dadosParticiparJogo, panelJogoCriado)) {
				return;
			}

			clientPaddockPack.setDadosCriarJogo(dadosParticiparJogo);
			ret = enviarObjeto(clientPaddockPack);
			if (ret == null) {
				return;
			}
			srvPaddockPack = (SrvPaddockPack) ret;
			jogoCliente = new JogoCliente(dadosParticiparJogo.getTemporada());
			jogoCliente.setMainFrame(mainFrame);
			jogoCliente.iniciarJogoOnline(srvPaddockPack.getDadosCriarJogo(),
					dadosParticiparJogo.getNomeJogo(), this, sessaoCliente,
					dadosParticiparJogo.getPiloto());
			DadosPaddock dadosPaddock = srvPaddockPack.getDadosPaddock();
			paddockWindow.atualizar(dadosPaddock);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(panel, e.getMessage(), "Erro",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	public void verDetalhesJogo(Object object) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_DETALHES_JOGO, sessaoCliente);

		clientPaddockPack.setNomeJogo((String) object);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		paddockWindow.mostrarDetalhes(srvPaddockPack.getDetalhesJogo());

	}

	public void iniciarJogo() {
		if (sessaoCliente == null) {
			logar();
			return;
		}
		if (jogoCliente == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("063"), "Erro",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.INICIAR_JOGO, sessaoCliente);

		Object ret = enviarObjeto(clientPaddockPack);
	}

	public void sairPaddock() {
		if (jogoCliente != null) {
			jogoCliente.abandonar();
		}
		jogoCliente.matarTodasThreads();
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.SAIR_PADDOCK, sessaoCliente);
		getThreadAtualizadora().interrupt();
		Object ret = enviarObjeto(clientPaddockPack);

	}

	public void verDetalhesJogador(Object object) {

		paddockWindow.mostrarDetalhesJogador(object);

	}

	public boolean registrarUsuario(FormEntrada formEntrada) {
		if (Util.isNullOrEmpty(formEntrada.getNome().getText())) {
			return false;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setCommando(Comandos.RGISTRAR_LOGIN);
		clientPaddockPack.setNomeJogador(formEntrada.getNome().getText());
		if ("IA".equals(clientPaddockPack.getNomeJogador())
				|| "Ia".equals(clientPaddockPack.getNomeJogador())
				|| "ia".equals(clientPaddockPack.getNomeJogador())
				|| "iA".equals(clientPaddockPack.getNomeJogador())
				|| Util.isNullOrEmpty(clientPaddockPack.getNomeJogador())) {
			JOptionPane.showMessageDialog(panel, Lang.msg("064"), Lang
					.msg("064"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		try {
			clientPaddockPack.setSenhaJogador(Util.md5(new String(formEntrada
					.getSenha().getPassword())));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(panel, e.getMessage(), "Erro",
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
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CLASSIFICACAO, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		FormClassificacao formClassificacao = new FormClassificacao(
				srvPaddockPack.getListaDadosJogador(), this);
		JOptionPane.showMessageDialog(panel, formClassificacao,
				Lang.msg("065"), JOptionPane.PLAIN_MESSAGE);

	}

	public void verConstrutores() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CONTRUTORES, sessaoCliente);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		FormConstrutores formConstrutores = new FormConstrutores(srvPaddockPack
				.getListaConstrutoresCarros(), srvPaddockPack
				.getListaConstrutoresPilotos());
		JOptionPane.showMessageDialog(panel, formConstrutores, Lang.msg("244"),
				JOptionPane.PLAIN_MESSAGE);

	}

	public List obterListaCorridas(String jogadorSel) {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VER_CORRIDAS, sessaoCliente);
		clientPaddockPack.setNomeJogador(jogadorSel);
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			return new ArrayList();
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		return srvPaddockPack.getListaCorridasJogador();
	}

	public void logar() {
		FormEntrada formEntrada = new FormEntrada();
		formEntrada.setToolTipText(Lang.msg("066"));
		int result = JOptionPane.showConfirmDialog(panel, formEntrada, Lang
				.msg("066"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			registrarUsuario(formEntrada);
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
		int result = JOptionPane.showConfirmDialog(panel, formCarreira, Lang
				.msg("246"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			int carLen = formCarreira.getNomeCarro().getText().length();
			int piloLen = formCarreira.getNomePiloto().getText().length();
			if (carLen == 0 || carLen > 20 || piloLen == 0 || piloLen > 20) {
				JOptionPane.showMessageDialog(panel, Lang.msg("249"), "Erro",
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
		if (ret == null) {
			JOptionPane.showMessageDialog(panel, Lang.msg("062"), "Erro",
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
		Object ret = enviarObjeto(clientPaddockPack);
	}
}
