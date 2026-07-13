package br.f1mane.editor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.recursos.idiomas.Lang;

public class FormularioListaObjetos {

	private final MainPanelEditor editor;
	private final Function<Circuito, List<ObjetoPista>> listaAccessor;
	private DefaultListModel defaultListModelOP;
	private JList list;
	private final JFrame frame = new JFrame();
	private JPanel objetos;
	/**
	 * Suprime os efeitos do ListSelectionListener quando a seleção vem do
	 * canvas, não do usuário na lista. Pacote-privado (em vez de private)
	 * para permitir simular seleção múltipla programática em testes, sem
	 * depender do editor estar totalmente montado (scrollPane etc.).
	 */
	boolean selecaoProgramatica;
	/**
	 * Modo unificado (ver {@link #unificada(MainPanelEditor)}): em vez de
	 * espelhar uma única coleção do {@link Circuito} via {@code listaAccessor},
	 * lê concatenando {@code objetosCenario} + {@code objetos} (filtrados por
	 * {@link MainPanelEditor#tipoVisivel(ObjetoPista)}), e ao gravar separa
	 * cada item de volta pra coleção certa conforme
	 * {@link TipoObjetoPista#isCenario()}.
	 */
	private boolean unificada;

	public DefaultListModel getDefaultListModelOP() {
		return defaultListModelOP;
	}

	public void setDefaultListModelOP(DefaultListModel defaultListModelOP) {
		this.defaultListModelOP = defaultListModelOP;
	}

	public JList getList() {
		return list;
	}

	public void setList(JList list) {
		this.list = list;
	}

	public FormularioListaObjetos(MainPanelEditor editor) {
		this(editor, Circuito::getObjetos);
	}

	public FormularioListaObjetos(MainPanelEditor editor,
			Function<Circuito, List<ObjetoPista>> listaAccessor) {
		this(editor, listaAccessor, false);
	}

	/**
	 * @param mostrarMoverPrimeiroEUltimo liga os botões "Primeiro"/"Ultimo"
	 *                                    (mover o item selecionado direto pro
	 *                                    início/fim da lista) — só faz
	 *                                    sentido pros objetos de desenho, cuja
	 *                                    ordem na lista é a ordem de desenho
	 *                                    dentro do mesmo nível; objetos de
	 *                                    função (Escapada/Transparência)
	 *                                    continuam só com Cima/Baixo/Remover.
	 */
	public FormularioListaObjetos(MainPanelEditor editor,
			Function<Circuito, List<ObjetoPista>> listaAccessor, boolean mostrarMoverPrimeiroEUltimo) {
		this.editor = editor;
		this.listaAccessor = listaAccessor;
		defaultListModelOP = new DefaultListModel();
		iniciarComponentes(mostrarMoverPrimeiroEUltimo);
	}

	/**
	 * Lista única de objetos do editor: substitui as duas instâncias
	 * separadas (uma para {@code objetosCenario}, outra para {@code objetos})
	 * por uma só, que lê/escreve nas duas coleções do {@link Circuito} ao
	 * mesmo tempo — ver {@link #listarObjetos()}/{@link #atualizarCircuito()}.
	 * Sempre com Primeiro/Ultimo habilitados (útil pros itens de cenário
	 * misturados na lista, harmless pros de função).
	 */
	public static FormularioListaObjetos unificada(MainPanelEditor editor) {
		FormularioListaObjetos formulario = new FormularioListaObjetos(editor, Circuito::getObjetos, true);
		formulario.unificada = true;
		return formulario;
	}

	private void iniciarComponentes(boolean mostrarMoverPrimeiroEUltimo) {
		list = new JList(defaultListModelOP);
		list.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (selecaoProgramatica) {
					return;
				}
				FormularioListaObjetos.this.editor.repaint();
				FormularioListaObjetos.this.editor.desSelecionaNosPista();
				int selectedIndex = list.getSelectedIndex();
				if (selectedIndex > -1
						&& selectedIndex < defaultListModelOP.size()) {
					ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
							.get(selectedIndex);
					if (objetoPista.getPosicaoQuina() == null) {
						Rectangle obterArea = objetoPista.obterArea();
						objetoPista.setAltura((int) obterArea.getBounds().getHeight());
						objetoPista.setLargura((int) obterArea.getBounds().getWidth());
						objetoPista.setPosicaoQuina(new Point(obterArea.getBounds().x,
								obterArea.getBounds().y));
					}
					Point centro = new Point(objetoPista.getPosicaoQuina());
					centro.x += objetoPista.getLargura()/2;
					centro.y += objetoPista.getAltura()/2;
					FormularioListaObjetos.this.editor
							.centralizarPonto(centro);
				}

			}
		});
		objetos = new JPanel(new BorderLayout());
		JPanel botoes = new JPanel(new GridLayout(0, 1));
		objetos.add(new JScrollPane(list), BorderLayout.CENTER);
		objetos.add(botoes, BorderLayout.SOUTH);
		JButton cima = new JButton(Lang.msg("287"));
		cima.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel == 0)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(sel - 1, objetoPista);
				list.setSelectedIndex(sel - 1);
				atualizarCircuito();
			}
		});
		JButton baixo = new JButton(Lang.msg("288"));
		baixo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel >= defaultListModelOP.getSize() - 1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(sel + 1, objetoPista);
				list.setSelectedIndex(sel + 1);
				atualizarCircuito();
			}
		});
		JButton primeiro = new JButton(Lang.msg("moverParaPrimeiro"));
		primeiro.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel == 0)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(0, objetoPista);
				list.setSelectedIndex(0);
				atualizarCircuito();
			}
		});
		JButton ultimo = new JButton(Lang.msg("moverParaUltimo"));
		ultimo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = list.getSelectedIndex();
				if (sel == -1 || sel >= defaultListModelOP.getSize() - 1)
					return;
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP
						.remove(sel);
				defaultListModelOP.add(defaultListModelOP.getSize(), objetoPista);
				list.setSelectedIndex(defaultListModelOP.getSize() - 1);
				atualizarCircuito();
			}
		});
		JButton remover = new JButton(Lang.msg("removerObjetoLista"));
		remover.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removerObjetoSelecionado();
			}
		});

		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					removerObjetoSelecionado();
				}
			}
		});

		// Editar passou a ser por duplo-clique no item da lista (sem botão
		// dedicado) — mesmo padrão do duplo-clique no objeto desenhado no
		// canvas (editaObjetoPista em MainPanelEditor).
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() != 2) {
					return;
				}
				int indice = list.locationToIndex(e.getPoint());
				if (indice < 0) {
					return;
				}
				ObjetoPista objetoPista = (ObjetoPista) defaultListModelOP.get(indice);
				FormularioObjetos formularioObjetos = new FormularioObjetos(
						FormularioListaObjetos.this.editor);
				formularioObjetos.objetoLivreFormulario(objetoPista);
			}
		});

		botoes.add(cima);
		botoes.add(baixo);
		if (mostrarMoverPrimeiroEUltimo) {
			botoes.add(primeiro);
			botoes.add(ultimo);
		}
		botoes.add(remover);
		frame.add(objetos);
	}

	/**
	 * Seleciona {@code objeto} na lista (ou limpa a seleção se ele não
	 * pertence a ela) sem centralizar o viewport nele — usado quando a
	 * seleção veio de um clique direto no canvas, onde o objeto já está
	 * visível e centralizar atrapalharia o arraste.
	 */
	public void selecionarSemCentralizar(ObjetoPista objeto) {
		int indice = defaultListModelOP.indexOf(objeto);
		selecaoProgramatica = true;
		try {
			if (indice < 0) {
				list.clearSelection();
			} else if (list.getSelectedIndex() != indice) {
				list.setSelectedIndex(indice);
				list.ensureIndexIsVisible(indice);
			}
		} finally {
			selecaoProgramatica = false;
		}
	}

	/**
	 * Reentrância própria: {@code defaultListModelOP.clear()} dispara um
	 * instante de "seleção vazia" que pode fazer um listener externo (ver
	 * {@code MainPanelEditor.atualizarEstadoSelecaoParaFiltro()}) reagir
	 * chamando {@code listarObjetos()} de novo antes desta chamada (a de
	 * fora) terminar de repopular — sem este guard, a chamada de fora
	 * retomaria depois da de dentro e adicionaria tudo de novo por cima,
	 * duplicando o conteúdo do model. A chamada reentrante simplesmente não
	 * faz nada: a de fora, ao retomar, lê o estado (já atualizado pela
	 * reentrante) direto do Circuito/tipoVisivel, então o resultado final é
	 * o mesmo de uma única execução.
	 */
	private boolean processandoListagem;

	public void listarObjetos() {
		if (processandoListagem) {
			return;
		}
		processandoListagem = true;
		try {
			if (unificada) {
				Circuito circuito = editor.getCircuito();
				defaultListModelOP.clear();
				adicionarVisiveis(circuito.getObjetosCenario());
				adicionarVisiveis(circuito.getObjetos());
				return;
			}
			List<ObjetoPista> objetoPista = listaAccessor.apply(editor.getCircuito());
			if (objetoPista != null) {
				defaultListModelOP.clear();
				for (ObjetoPista op : objetoPista) {
					defaultListModelOP.addElement(op);
				}
			}
		} finally {
			processandoListagem = false;
		}
	}

	/**
	 * Modo unificado: só entram no {@code DefaultListModel} os objetos
	 * atualmente visíveis pelo filtro do editor (tipo ou "Somente
	 * Selecionado") — objetos escondidos continuam em {@code lista}
	 * (Circuito), só não aparecem na lista do editor nem no desenho.
	 */
	private void adicionarVisiveis(List<ObjetoPista> lista) {
		if (lista == null) {
			return;
		}
		for (ObjetoPista op : lista) {
			if (editor.tipoVisivel(op)) {
				defaultListModelOP.addElement(op);
			}
		}
	}

	/**
	 * Remove o objeto atualmente selecionado na lista (usado tanto pelo botão
	 * "Remover" quanto pela tecla Delete com foco na lista) — sem efeito se
	 * não houver seleção.
	 */
	private void removerObjetoSelecionado() {
		int sel = list.getSelectedIndex();
		if (sel == -1) {
			return;
		}
		defaultListModelOP.remove(sel);
		atualizarCircuito();
	}

	protected void atualizarCircuito() {
		Circuito circuito = editor.getCircuito();
		if (unificada) {
			List<ObjetoPista> cenario = new ArrayList<ObjetoPista>();
			List<ObjetoPista> funcao = new ArrayList<ObjetoPista>();
			for (int i = 0; i < defaultListModelOP.getSize(); i++) {
				ObjetoPista op = (ObjetoPista) defaultListModelOP.getElementAt(i);
				if (TipoObjetoPista.de(op).isCenario()) {
					cenario.add(op);
				} else {
					funcao.add(op);
				}
			}
			// Objetos escondidos pelo filtro (tipoVisivel == false) nunca
			// entram em defaultListModelOP, então uma reordenação/remoção
			// feita só sobre o subconjunto visível não deve apagá-los do
			// circuito — são reanexados ao final de cada coleção, na ordem
			// relativa original entre si. Um objeto removido pelo botão
			// "Remover" continua tipoVisivel==true (não foi escondido, foi
			// deletado do model), então não é reanexado aqui.
			reanexarEscondidos(circuito.getObjetosCenario(), cenario);
			reanexarEscondidos(circuito.getObjetos(), funcao);
			circuito.setObjetosCenario(cenario);
			circuito.setObjetos(funcao);
			circuito.vetorizarPista();
			editor.repaint();
			return;
		}
		List<ObjetoPista> objetos = listaAccessor.apply(circuito);
		objetos.clear();
		for (int i = 0; i < defaultListModelOP.getSize(); i++) {
			objetos.add((ObjetoPista) defaultListModelOP.getElementAt(i));
		}
		circuito.vetorizarPista();
		editor.repaint();
	}

	/**
	 * Reanexa em {@code destino}, na ordem original entre si, os itens de
	 * {@code original} atualmente escondidos pelo filtro do editor — ver
	 * comentário em {@link #atualizarCircuito()}.
	 */
	private void reanexarEscondidos(List<ObjetoPista> original, List<ObjetoPista> destino) {
		if (original == null) {
			return;
		}
		for (ObjetoPista objeto : original) {
			if (!editor.tipoVisivel(objeto)) {
				destino.add(objeto);
			}
		}
	}

	public JPanel getObjetos() {
		return objetos;
	}

	public void setObjetos(JPanel objetos) {
		this.objetos = objetos;
	}
}
