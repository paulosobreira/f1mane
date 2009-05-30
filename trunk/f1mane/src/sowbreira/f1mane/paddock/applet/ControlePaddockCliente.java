package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import sowbreira.f1mane.recursos.idiomas.Lang;

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
				JOptionPane.showMessageDialog(panel, erroServ
						.obterErroFormatado(), Lang.msg("060"),
						JOptionPane.ERROR_MESSAGE);
				return null;
			}
			if (retorno instanceof MsgSrv) {
				MsgSrv msgSrv = (MsgSrv) retorno;
				JOptionPane.showMessageDialog(panel, msgSrv.getMessageString(),
						Lang.msg("061"), JOptionPane.INFORMATION_MESSAGE);
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
		if (sessaoCliente == null) {
			return;
		}
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
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.ENTRAR_JOGO, sessaoCliente);
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

			if (!painelCriacaoCorridaSimples
					.gerarDadosEntrarJogo(dadosParticiparJogo)) {
				return;
			}

			clientPaddockPack.setDadosCriarJogo(dadosParticiparJogo);
			Object ret = enviarObjeto(clientPaddockPack);
			if (ret == null) {
				return;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
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

	public boolean verificaLoginValido(FormEntradaSimples formEntrada) {
		String nome = formEntrada.getNome().getText();
		if (nome == null) {
			return false;
		}
		if ("".equals(nome.trim())) {
			return false;
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setCommando(Comandos.VALIDAR_LOGIN);
		clientPaddockPack.setNomeJogador(nome);
		// clientPaddockPack.setSenhaJogador(formEntrada.getSenha());
		Object ret = enviarObjeto(clientPaddockPack);
		if (ret == null) {
			return false;
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
		SessaoCliente cliente = srvPaddockPack.getSessaoCliente();
		this.sessaoCliente = cliente;
		return true;

	}

	public boolean registrarUsuario(FormEntradaSimples formEntrada) {
		// if (!formEntrada.getSenhaRegistrar().equals(
		// formEntrada.getSenhaRegistrarRepetir())) {
		// JOptionPane.showMessageDialog(panel, "As senhas não estão iguais",
		// "Erro na senha", JOptionPane.ERROR_MESSAGE);
		// return false;
		// }

		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setCommando(Comandos.RGISTRAR_LOGIN);
		clientPaddockPack.setNomeJogador(formEntrada.getNome().getText());
		if ("IA".equals(clientPaddockPack.getNomeJogador())
				|| "Ia".equals(clientPaddockPack.getNomeJogador())
				|| "ia".equals(clientPaddockPack.getNomeJogador())
				|| "iA".equals(clientPaddockPack.getNomeJogador())) {
			JOptionPane.showMessageDialog(panel, Lang.msg("064"), Lang
					.msg("064"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// clientPaddockPack.setSenhaJogador(formEntrada.getSenhaRegistrar());
		clientPaddockPack.setEmailJogador(formEntrada.getEmail().getText());
		Object ret = enviarObjeto(clientPaddockPack);
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
		FormEntradaSimples formEntrada = new FormEntradaSimples();
		formEntrada.setToolTipText(Lang.msg("066"));

		int result = JOptionPane.showConfirmDialog(panel, formEntrada);

		if (JOptionPane.OK_OPTION == result) {
			while (!registrarUsuario(formEntrada)
					&& JOptionPane.OK_OPTION == result) {
				result = JOptionPane.showConfirmDialog(panel, formEntrada);
			}

		}
		if (getSessaoCliente() != null) {
			paddockWindow = new PaddockWindow(this);
			atualizaVisao(paddockWindow);
			panel.setLayout(new BorderLayout());
			panel.add(paddockWindow.getMainPanel(), BorderLayout.CENTER);
			getThreadAtualizadora().start();
		} else {
			JOptionPane.showMessageDialog(panel, Lang.msg("067"));

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
}
