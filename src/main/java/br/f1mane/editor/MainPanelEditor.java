package br.f1mane.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.DesenhoProceduralCircuito;
import br.f1mane.controles.ControleEstatisticas;
import br.f1mane.controles.ControleRecursos;
import br.f1mane.entidades.DirecaoEmpilhamento;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoConstrucao;
import br.f1mane.entidades.ObjetoDesenho;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoGuardRails;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.TipoObjetoConstrucao;
import br.f1mane.entidades.PontoCurva;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.PainelCircuito;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class MainPanelEditor extends JPanel {
    private static final long serialVersionUID = -7001602531075714400L;
    private Circuito circuito = new Circuito();
    private TestePista testePista;
    private Color tipoNo = null;
    private No ultimoNo = null;
    private JList pistaJList;
    private JList boxJList;
    private EditorCircuitos srcFrame;
    private boolean desenhaTracado = true;
    private boolean mostraBG = false;
    /** Preview do editor: liga/desliga o desenho dos objetos de desenho (Livre, Arquibancada, Construcao,
     * GuardRails, Pneus). Não afeta objetos de função (Escapada, Transparencia), sempre desenhados. */
    private boolean desenhaObjetosDesenho = true;
    private boolean pontosEscape = false;
    /** Cache de {@link #obterZonaFrenagemNos()}, invalidado quando {@code circuito.getPistaFull()} muda de referência (ex.: revetorização). */
    private List<No> zonaFrenagemCalculadaPara;
    private java.util.Set<No> zonaFrenagemNos = java.util.Collections.emptySet();
    public final static Color ver = new Color(255, 10, 10, 150);

    private BufferedImage backGround;
    int ultimoItemBoxSelecionado = -1;
    int ultimoItemPistaSelecionado = -1;

    private static final class OpcaoTipoNo {
        final String label;
        final Color tipo;
        final boolean box;

        OpcaoTipoNo(String label, Color tipo, boolean box) {
            this.label = label;
            this.tipo = tipo;
            this.box = box;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    // Não é static final: os textos vêm de Lang.msg, que resolve pelo idioma
    // corrente a cada chamada — um array estático fixaria a tradução do
    // idioma vigente no carregamento da classe.
    private static OpcaoTipoNo[] criarTiposNo() {
        return new OpcaoTipoNo[]{
                new OpcaoTipoNo(Lang.msg("noSemSelecao"), null, false),
                new OpcaoTipoNo(Lang.msg("099"), No.LARGADA, false),
                new OpcaoTipoNo(Lang.msg("100"), No.RETA, false),
                new OpcaoTipoNo(Lang.msg("101"), No.CURVA_ALTA, false),
                new OpcaoTipoNo(Lang.msg("102"), No.CURVA_BAIXA, false),
                new OpcaoTipoNo(Lang.msg("103"), No.BOX, true),
                new OpcaoTipoNo(Lang.msg("boxReta"), No.RETA, true),
                new OpcaoTipoNo(Lang.msg("boxCurvaAlta"), No.CURVA_ALTA, true),
                new OpcaoTipoNo(Lang.msg("104"), No.PARADA_BOX, true),
                new OpcaoTipoNo(Lang.msg("fimBox"), No.FIM_BOX, true),
        };
    }

    private JComboBox tipoNoCombo;

    private JScrollPane scrollPane;

    public final double zoom = 1;
    private BufferedImage carroCima;
    private int mx;
    private int my;
    private int pos = 0;
    private double multiplicadorPista = 9;
    private double multiplicadorLarguraPista = 0;
    private JSpinner larguraPistaSpinner;
    private JTextField nomePistaText;
    private JTextField probalidadeChuvaText;
    private JSpinner cicloSpinner;
    private JTextField distanciaKmText;
    private JLabel tempoVoltaEstimadoLabel;
    private final BasicStroke trilho = new BasicStroke(1);
    private int larguraPistaPixeis;
    private JComboBox ladoBoxCombo;
    private JComboBox ladoBoxSaidaBoxCombo;
    private ObjetoPista objetoPista;
    private boolean desenhandoObjetoLivre;
    private boolean posicionaObjetoPista;
    private boolean criandoObjetoCenario;
    private ObjetoPista objetoArrastando;
    private Point offsetArraste;
    private boolean arrastouObjeto;
    private Point ultimoClicado;

    /** Raio de tolerância (px de tela; o canvas do editor não tem zoom variável) para clicar num marcador. */
    private static final int RAIO_MARCADOR_EDICAO_PONTOS_PX = 8;
    /** Raio de tolerância (px de tela) para o snap de pontos de guard rails a nós/outros objetos. */
    private static final int RAIO_SNAP_GUARD_RAILS_PX = 10;
    /** Tolerância (px de tela) para validar o ponto de entrada/saída de um ObjetoEscapada contra um nó de traçado 1/2. */
    private static final int TOLERANCIA_ESCAPADA_PX = 2;
    private ObjetoLivre editandoPontosDe;
    private PontoCurva verticeArrastando;
    private boolean arrastandoHasteDoVertice;
    private boolean arrastouVertice;
    private ObjetoGuardRails editandoPontosGuardRailsDe;
    private int indicePontoGuardRailsArrastando = -1;
    private boolean arrastouPontoGuardRails;
    private ObjetoEscapada editandoPontosEscapadaDe;
    /** Índice do ponto de {@link ObjetoEscapada#getPontos()} sendo arrastado, -1 = nenhum. */
    private int indicePontoEscapadaArrastando = -1;
    private boolean arrastouPontoEscapada;
    /** Posição (espaço local) do ponto antes do arraste atual, para reverter se o solto não validar (só se aplica ao primeiro/último ponto). */
    private Point pontoEscapadaAnteriorAoArraste;
    /**
     * Pacote-privado (em vez de private) para permitir injeção direta em
     * testes. formularioListaObjetosDesenho espelha circuito.objetosCenario
     * (objetos de desenho: Livre, Arquibancada, Construcao, GuardRails,
     * Pneus — todos com nível); formularioListaObjetosFuncao espelha
     * circuito.objetos (objetos de função: Escapada, Transparencia — sem
     * nível, tratamento próprio em corrida).
     */
    FormularioListaObjetos formularioListaObjetosDesenho;
    FormularioListaObjetos formularioListaObjetosFuncao;
    /** "Área de transferência" de cor (Copiar Cor/Colar Cor); null enquanto nada foi copiado ainda. */
    private Color corCopiada1;
    private Color corCopiada2;
    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    JCheckBox noite = new JCheckBox();
    private final JLabel corFundoLabel = criaIndicadorDeCor();
    private final JLabel corAsfaltoLabel = criaIndicadorDeCor();
    private final JLabel corBox1Label = criaIndicadorDeCor();
    private final JLabel corBox2Label = criaIndicadorDeCor();
    private final JLabel corZebra1Label = criaIndicadorDeCor();
    private final JLabel corZebra2Label = criaIndicadorDeCor();
    File file;

    private final List<String> circuitosXml = new ArrayList<String>();
    private final List<String> nomesAmigaveisCircuitos = new ArrayList<String>();
    private int indiceCircuito = -1;
    private JComboBox<CircuitoComboItem> comboCircuito;
    private boolean sincronizandoComboCircuito;
    private JButton btnCircuitoAnterior;
    private JButton btnCircuitoProximo;
    private JCheckBox ativoCheckBox;
    private boolean eventosMouseAdicionados;

    /** Item do combobox de seleção de circuito: exibe o nome amigável, carrega pelo arquivo XML. */
    private static final class CircuitoComboItem {
        private final String nomeAmigavel;
        private final String arquivoXml;

        CircuitoComboItem(String nomeAmigavel, String arquivoXml) {
            this.nomeAmigavel = nomeAmigavel;
            this.arquivoXml = arquivoXml;
        }

        @Override
        public String toString() {
            return nomeAmigavel;
        }
    }

    public MainPanelEditor() {
    }

    public MainPanelEditor(EditorCircuitos frame) {
        this.srcFrame = frame;
    }

    private boolean vetorizarCircuito() {
        mx = 0;
        my = 0;
        larguraPistaPixeis = 0;
        testePista.pararTeste();

        DefaultListModel defaultListModel = (DefaultListModel) pistaJList.getModel();
        boolean temLargada = false;
        for (int i = 0; i < defaultListModel.size(); i++) {
            No no = (No) defaultListModel.get(i);
            if (No.LARGADA.equals(no.getTipo())) {
                temLargada = true;
                break;
            }
        }
        if (!temLargada) {
            JOptionPane.showMessageDialog(null, Lang.msg("obrigatorioNoLargada"), Lang.msg("039"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (ladoBoxCombo.getSelectedIndex() == 0) {
            circuito.setLadoBox(1);
        } else {
            circuito.setLadoBox(2);
        }
        if (ladoBoxSaidaBoxCombo.getSelectedIndex() == 0) {
            circuito.setLadoBoxSaidaBox(1);
        } else {
            circuito.setLadoBoxSaidaBox(2);
        }
        multiplicadorLarguraPista = circuito.getMultiplicadorLarguraPista();
        if (multiplicadorLarguraPista < 1.0 || multiplicadorLarguraPista > 2.0) {
            JOptionPane.showMessageDialog(null, Lang.msg("multiplicadorLarguraPista"), Lang.msg("039"),
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (defaultListModel.size() > 10) {
            circuito.vetorizarPista(this.multiplicadorPista, this.multiplicadorLarguraPista);
            atualizaTempoVoltaEstimado();
        }
        List l = circuito.getPistaFull();
        for (Iterator iterator = l.iterator(); iterator.hasNext(); ) {
            No no = (No) iterator.next();
            Point point = no.getPoint();
            if (point.x > mx) {
                mx = point.x;
            }
            if (point.y > my) {
                my = point.y;
            }

        }
        mx += 300;
        my += 300;
        return true;
    }

    private void atualizaListas() {
        for (Iterator iter = circuito.getPista().iterator(); iter.hasNext(); ) {
            No no = (No) iter.next();
            ((DefaultListModel) pistaJList.getModel()).addElement(no);
        }

        for (Iterator iter = circuito.getBox().iterator(); iter.hasNext(); ) {
            ((DefaultListModel) boxJList.getModel()).addElement(iter.next());
        }
    }

    private void iniciaEditor() {

        carroCima = CarregadorRecursos.pintarModeloV2("png/carro-cima-v2.png",
                new java.awt.Color(60, 60, 60), new java.awt.Color(120, 120, 120),
                br.f1mane.recursos.SpriteSheet.CIMA_W, br.f1mane.recursos.SpriteSheet.CIMA_H);

        gerarLayout(srcFrame);
        testePista = new TestePista(this, circuito);
        adicionaEventosMouse(srcFrame);
    }

    private JPanel gerarBotoesVisualizacao() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3));

        JCheckBox desenhaTracadoCheck = new JCheckBox(Lang.msg("TRACADO"));
        desenhaTracadoCheck.setSelected(desenhaTracado);
        desenhaTracadoCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desenhaTracado = desenhaTracadoCheck.isSelected();
                MainPanelEditor.this.repaint();
            }
        });

        JCheckBox desenhaBackgroundCheck = new JCheckBox(Lang.msg("mostrarBackground"));
        desenhaBackgroundCheck.setSelected(mostraBG);
        desenhaBackgroundCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostraBG = desenhaBackgroundCheck.isSelected();
                MainPanelEditor.this.repaint();
            }
        });

        // Liga/desliga só os objetos de desenho (Livre, Arquibancada, Construcao,
        // GuardRails, Pneus); Escapada/Transparencia (objetos de função) continuam
        // sempre desenhados, ver desenhaObjetosNivel().
        JCheckBox desenhaObjetosCheck = new JCheckBox(Lang.msg("mostrarObjetosDesenho"));
        desenhaObjetosCheck.setSelected(desenhaObjetosDesenho);
        desenhaObjetosCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desenhaObjetosDesenho = desenhaObjetosCheck.isSelected();
                MainPanelEditor.this.repaint();
            }
        });

        buttonsPanel.add(desenhaTracadoCheck);
        buttonsPanel.add(desenhaBackgroundCheck);
        buttonsPanel.add(desenhaObjetosCheck);
        return buttonsPanel;
    }

    private JPanel gerarBotaoCriarObjeto() {
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1));
        JButton criarObjeto = new JButton(Lang.msg("criarObjeto"));
        criarObjeto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iniciarCriacaoObjeto();
            }
        });
        buttonsPanel.add(criarObjeto);

        JButton copiarCor = new JButton(Lang.msg("copiarCor"));
        copiarCor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copiarCorObjetoSelecionado();
            }
        });
        buttonsPanel.add(copiarCor);

        JButton colarCor = new JButton(Lang.msg("colarCor"));
        colarCor.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                colarCorObjetosSelecionados();
            }
        });
        buttonsPanel.add(colarCor);

        return buttonsPanel;
    }

    /**
     * Copia a cor primária e secundária do objeto selecionado (na lista de
     * objetos ou na de cenário — a primeira das duas que tiver seleção) para
     * uso posterior em {@link #colarCorObjetosSelecionados()}. Sem seleção em
     * nenhuma das duas listas, não faz nada.
     */
    void copiarCorObjetoSelecionado() {
        ObjetoPista origem = primeiroSelecionado(formularioListaObjetosFuncao);
        if (origem == null) {
            origem = primeiroSelecionado(formularioListaObjetosDesenho);
        }
        if (origem == null) {
            return;
        }
        corCopiada1 = origem.getCorPimaria();
        corCopiada2 = origem.getCorSecundaria();
    }

    /**
     * Aplica a cor copiada por {@link #copiarCorObjetoSelecionado()} a todos
     * os objetos atualmente selecionados, somando a seleção das duas listas
     * (objetos e cenário) — suporta colar em vários objetos de uma vez. Sem
     * nada copiado antes, ou sem nenhuma seleção, não faz nada.
     */
    void colarCorObjetosSelecionados() {
        if (corCopiada1 == null && corCopiada2 == null) {
            return;
        }
        List<ObjetoPista> selecionados = new ArrayList<ObjetoPista>();
        selecionados.addAll(todosSelecionados(formularioListaObjetosFuncao));
        selecionados.addAll(todosSelecionados(formularioListaObjetosDesenho));
        if (selecionados.isEmpty()) {
            return;
        }
        for (ObjetoPista objetoPista : selecionados) {
            objetoPista.setCorPimaria(corCopiada1);
            objetoPista.setCorSecundaria(corCopiada2);
        }
        repaint();
    }

    private static ObjetoPista primeiroSelecionado(FormularioListaObjetos formulario) {
        if (formulario == null) {
            return null;
        }
        return (ObjetoPista) formulario.getList().getSelectedValue();
    }

    @SuppressWarnings("unchecked")
    private static List<ObjetoPista> todosSelecionados(FormularioListaObjetos formulario) {
        if (formulario == null) {
            return java.util.Collections.emptyList();
        }
        return formulario.getList().getSelectedValuesList();
    }

    /**
     * Abre o diálogo de escolha de tipo e prepara o objeto criado para ser
     * posicionado no canvas — mesma ação do botão "Criar Objeto", reutilizada
     * pelo atalho Insert.
     */
    public void iniciarCriacaoObjeto() {
        try {
            desSelecionaNosPista();
            TipoObjetoPista tipoSelecionado = (TipoObjetoPista) JOptionPane.showInputDialog(
                    srcFrame, Lang.msg("tipoDoObjetoPrompt"), Lang.msg("criarObjeto"), JOptionPane.QUESTION_MESSAGE,
                    null, TipoObjetoPista.values(), TipoObjetoPista.values()[0]);
            if (tipoSelecionado == null) {
                return;
            }
            objetoPista = tipoSelecionado.criar();
            // Aplica os últimos valores usados (ângulo, tamanho, cores, padrão)
            // para esta mesma classe, se houver, em vez de sempre nascer com os
            // defaults do construtor. Sem segundo diálogo aqui: o objeto já é
            // criado pronto para posicionar/desenhar; para mudar alguma
            // propriedade, o usuário edita depois com duplo-clique.
            MemoriaPropriedadesObjeto.aplicar(objetoPista);
            posicionaObjetoPista = true;
            criandoObjetoCenario = tipoSelecionado.isCenario();
            if (objetoPista instanceof ObjetoTransparencia) {
                objetoPista.setTransparencia(125);
                desenhandoObjetoLivre = true;
            } else if (objetoPista instanceof ObjetoLivre || objetoPista instanceof ObjetoGuardRails
                    || objetoPista instanceof ObjetoEscapada) {
                // GuardRails e Escapada também são desenhados ponto a ponto
                // (encadeamento de segmentos / par saída-retorno), como
                // ObjetoLivre — reaproveitam o mesmo modo.
                desenhandoObjetoLivre = true;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            srcFrame.dialogDeErro(e2);
        }
    }

    /**
     * Apaga o objeto atualmente selecionado (canvas ou lista), em qualquer
     * das duas listas do circuito (objetos ou objetosCenario) — atalho
     * Delete. Sem seleção, não faz nada.
     */
    public void apagarObjetoSelecionado() {
        if (objetoPista == null) {
            return;
        }
        boolean removidoDeCenario = circuito.getObjetosCenario() != null
                && circuito.getObjetosCenario().remove(objetoPista);
        boolean removidoDeObjetos = !removidoDeCenario && circuito.getObjetos() != null
                && circuito.getObjetos().remove(objetoPista);
        if (!removidoDeCenario && !removidoDeObjetos) {
            return;
        }
        if (editandoPontosDe == objetoPista) {
            encerrarEdicaoPontosObjetoLivre();
        }
        if (editandoPontosGuardRailsDe == objetoPista) {
            encerrarEdicaoPontosGuardRails();
        }
        objetoPista = null;
        objetoArrastando = null;
        if (removidoDeCenario && formularioListaObjetosDesenho != null) {
            formularioListaObjetosDesenho.listarObjetos();
        }
        if (removidoDeObjetos && formularioListaObjetosFuncao != null) {
            formularioListaObjetosFuncao.listarObjetos();
        }
        repaint();
    }

    private JPanel gerarSecaoNos() {
        JPanel secao = new JPanel(new BorderLayout());
        secao.add(gerarPainelControlesNos(), BorderLayout.NORTH);
        secao.add(gerarListsNosPistaBox(), BorderLayout.CENTER);
        return secao;
    }

    /**
     * Único grid de 1 coluna com os controles de nós de pista — tipo de nó,
     * lado do box, lado de saída do box, apagar último nó e apagar nó
     * selecionado —, uma linha por controle, substituindo os três
     * sub-painéis que antes misturavam arranjo empilhado com arranjo lado a
     * lado (gerarComboTipoNo/gerarCombosLadoBox/gerarBotoesAcoesNo).
     */
    private JPanel gerarPainelControlesNos() {
        JPanel painel = new JPanel(new GridLayout(6, 1));

        // Rótulo numa linha própria, acima do combo — e o combo direto no
        // grid (sem painel embrulhando), pra ocupar a linha toda como os
        // botões abaixo, em vez de ficar do tamanho natural/centralizado
        // dentro de um painel com FlowLayout padrão.
        painel.add(new JLabel(Lang.msg("tipoDeNo"), SwingConstants.CENTER));
        tipoNoCombo = new JComboBox(criarTiposNo());
        tipoNoCombo.setSelectedIndex(0);
        painel.add(tipoNoCombo);

        // Itens indexados (0 = lado 1, 1 = lado 2): a seleção é lida por
        // índice (ver vetorizarCircuito()), não pelo texto do item, já que o
        // texto vem de Lang.msg e muda conforme o idioma.
        ladoBoxCombo = new JComboBox();
        ladoBoxCombo.addItem(Lang.msg("ladoBox1"));
        ladoBoxCombo.addItem(Lang.msg("ladoBox2"));
        if (circuito != null && circuito.getLadoBox() == 2) {
            ladoBoxCombo.setSelectedIndex(1);
        }
        painel.add(ladoBoxCombo);

        ladoBoxSaidaBoxCombo = new JComboBox();
        ladoBoxSaidaBoxCombo.addItem(Lang.msg("saidaLadoBox1"));
        ladoBoxSaidaBoxCombo.addItem(Lang.msg("saidaLadoBox2"));
        if (circuito != null && circuito.getLadoBoxSaidaBox() == 2) {
            ladoBoxSaidaBoxCombo.setSelectedIndex(1);
        }
        painel.add(ladoBoxSaidaBoxCombo);

        JButton apagarUltimoNoButton = new JButton(Lang.msg("105"));
        apagarUltimoNoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    apagarUltimoNo();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });
        painel.add(apagarUltimoNoButton);

        JButton apagaNoListaButton = new JButton(Lang.msg("apagaNoListaButton"));
        apagaNoListaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    DefaultListModel boxModel = ((DefaultListModel) boxJList.getModel());
                    int selectedIndexBox = boxJList.getSelectedIndex();
                    if (selectedIndexBox >= 0 && selectedIndexBox < boxModel.getSize()) {
                        circuito.getBox().remove(boxModel.get(selectedIndexBox));
                        boxModel.remove(selectedIndexBox);
                    }
                    DefaultListModel pistaModel = ((DefaultListModel) pistaJList.getModel());
                    int selectedIndexPista = pistaJList.getSelectedIndex();
                    if (selectedIndexPista >= 0 && selectedIndexPista < pistaModel.getSize()) {
                        circuito.getPista().remove(pistaModel.get(selectedIndexPista));
                        pistaModel.remove(selectedIndexPista);
                    }
                    vetorizarCircuito();
                    repaint();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });
        painel.add(apagaNoListaButton);

        return painel;
    }

    /**
     * Duas listas: a de cima ("objetos de desenho" — Livre, Arquibancada,
     * Construcao, GuardRails, Pneus, todos com nível de desenho) ocupa o
     * espaço principal; a de baixo, menor, tem só os objetos de função
     * (Escapada, Transparencia), que não têm nível — não fazem sentido
     * "acima"/"abaixo" da pista, têm tratamento próprio em corrida.
     */
    private JPanel gerarSecaoObjetos() {
        JPanel secao = new JPanel(new BorderLayout());
        secao.add(gerarBotaoCriarObjeto(), BorderLayout.NORTH);
        formularioListaObjetosDesenho = new FormularioListaObjetos(this, Circuito::getObjetosCenario, true);
        formularioListaObjetosDesenho.listarObjetos();
        formularioListaObjetosFuncao = new FormularioListaObjetos(this, Circuito::getObjetos);
        formularioListaObjetosFuncao.listarObjetos();

        JPanel listaDesenho = new JPanel(new BorderLayout());
        listaDesenho.add(new JLabel(Lang.msg("objetosDesenhoLabel")), BorderLayout.NORTH);
        listaDesenho.add(formularioListaObjetosDesenho.getObjetos(), BorderLayout.CENTER);

        JPanel listaFuncao = new JPanel(new BorderLayout());
        listaFuncao.add(new JLabel(Lang.msg("objetosFuncaoLabel")), BorderLayout.NORTH);
        listaFuncao.add(formularioListaObjetosFuncao.getObjetos(), BorderLayout.CENTER);

        // Split vertical (não BorderLayout com altura fixa): a lista de cima
        // tem lista+5 botões (Cima/Baixo/Primeiro/Ultimo/Remover) e a de
        // baixo lista+3 (Cima/Baixo/Remover); uma altura em pixels fixa
        // espremia as listas até sumir. 70/30 garante os 30% pedidos
        // independente do conteúdo.
        JSplitPane splitListas = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                listaDesenho, listaFuncao);
        splitListas.setResizeWeight(0.7);
        splitListas.setOneTouchExpandable(true);
        secao.add(splitListas, BorderLayout.CENTER);
        return secao;
    }

    private JSplitPane gerarSplitPaneLateral() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gerarSecaoNos(), gerarSecaoObjetos());
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setPreferredSize(new Dimension(300, 10000));
        return splitPane;
    }


    private JPanel gerarTopoNavegacaoEAcoes() {
        // Três linhas: a linha 0 tem a navegação/salvar e a primeira metade
        // dos campos persistidos no circuito (nome/ativo/clima/ciclo/tempo de
        // volta/distância/largura); a linha 1 tem a outra metade (noite e as
        // cores); a linha 2 tem só os controles de teste/visualização, que
        // não alteram dado nenhum do circuito (traçado, background, objetos,
        // teste de pista, ir ao box, escapada, setinhas de posição do
        // traçado). GridBagLayout (em vez de GridLayout) porque os valores
        // não têm o mesmo tamanho natural — o campo de nome do circuito, por
        // exemplo, precisa de bem mais largura que um indicador de cor ou um
        // checkbox — então cada coluna assume a largura do maior componente
        // que a ocupa, em vez de todo campo ser esticado pro tamanho do
        // maior campo da grade toda.
        // Cada linha tem seu próprio GridBagLayout independente (em vez de
        // um só compartilhado pelas três) — colunas de um GridBagLayout são
        // globais ao container inteiro, então um componente largo numa linha
        // (ex.: o combo de circuitos) alargava a mesma coluna nas outras
        // linhas também, mesmo sem precisar.
        JPanel linha0Painel = new JPanel(new GridBagLayout());
        JPanel linha1Painel = new JPanel(new GridBagLayout());
        JPanel linha2Painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 10);
        gbc.anchor = GridBagConstraints.WEST;
        int[] colunaLinha0 = {0};

        btnCircuitoAnterior = new JButton("◀");
        btnCircuitoAnterior.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                navegarCircuito(-1);
            }
        });
        adicionaComponenteTopo(linha0Painel, gbc, 0, colunaLinha0, btnCircuitoAnterior);

        comboCircuito = new JComboBox<CircuitoComboItem>() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(Math.min(d.width, 160), d.height);
            }
        };
        comboCircuito.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (sincronizandoComboCircuito) {
                    return;
                }
                CircuitoComboItem selecionado = (CircuitoComboItem) comboCircuito.getSelectedItem();
                if (selecionado == null) {
                    return;
                }
                int novoIndice = circuitosXml.indexOf(selecionado.arquivoXml);
                if (novoIndice < 0 || novoIndice == indiceCircuito) {
                    return;
                }
                indiceCircuito = novoIndice;
                try {
                    carregarCircuitoExistente(selecionado.arquivoXml);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    srcFrame.dialogDeErro(ex);
                }
            }
        });
        adicionaComponenteTopo(linha0Painel, gbc, 0, colunaLinha0, comboCircuito);
        // gerarLayout() recria este painel (e comboCircuito) a cada carregamento
        // de circuito; sem isto o combo nasceria vazio e o setSelectedIndex feito
        // por atualizarBotoesNavegacao() no fim deste método lançaria
        // IllegalArgumentException (índice fora dos limites de um combo vazio).
        repopularComboCircuitos();

        btnCircuitoProximo = new JButton("▶");
        btnCircuitoProximo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                navegarCircuito(1);
            }
        });
        adicionaComponenteTopo(linha0Painel, gbc, 0, colunaLinha0, btnCircuitoProximo);

        JButton btnSalvar = new JButton(Lang.msg("108"));
        btnSalvar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    salvarPista();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    srcFrame.dialogDeErro(ex);
                }
            }
        });
        adicionaComponenteTopo(linha0Painel, gbc, 0, colunaLinha0, btnSalvar);

        nomePistaText = new JTextField();
        nomePistaText.setColumns(18);
        nomePistaText.setMinimumSize(new Dimension(150, nomePistaText.getPreferredSize().height));
        if (circuito != null) {
            nomePistaText.setText(circuito.getNome());
        }

        ativoCheckBox = new JCheckBox();
        ativoCheckBox.setSelected(circuito != null && circuito.isAtivo());
        ativoCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                circuito.setAtivo(ativoCheckBox.isSelected());
            }
        });

        probalidadeChuvaText = new JTextField();
        probalidadeChuvaText.setColumns(3);
        if (circuito != null) {
            probalidadeChuvaText.setText("" + circuito.getProbalidadeChuva());
        }

        cicloSpinner = new JSpinner(new SpinnerNumberModel(circuito != null ? circuito.getCiclo() : 160, 1, 10000, 1));
        cicloSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                circuito.setCiclo(((Integer) cicloSpinner.getValue()).intValue());
                atualizaTempoVoltaEstimado();
            }
        });

        tempoVoltaEstimadoLabel = new JLabel("~");

        distanciaKmText = new JTextField();
        distanciaKmText.setColumns(5);
        distanciaKmText.setMinimumSize(new Dimension(60, distanciaKmText.getPreferredSize().height));
        if (circuito != null) {
            distanciaKmText.setText("" + circuito.getDistanciaKm());
        }

        if (multiplicadorLarguraPista < 1.0) {
            multiplicadorLarguraPista = 1.0;
        }
        if (multiplicadorLarguraPista > 2.0) {
            multiplicadorLarguraPista = 2.0;
        }
        SpinnerNumberModel model1 = new SpinnerNumberModel(multiplicadorLarguraPista, 1.0, 2.0, 0.1);
        larguraPistaSpinner = new JSpinner(model1);
        // Sem isto o spinner nasce estreito demais pra ler o valor (o
        // JSpinner não tem setColumns() direto — o campo de texto fica
        // dentro do editor padrão).
        ((JSpinner.DefaultEditor) larguraPistaSpinner.getEditor()).getTextField().setColumns(5);
        larguraPistaSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                circuito.setMultiplicadorLarguraPista(((Double) larguraPistaSpinner.getModel().getValue()).doubleValue());
                vetorizarCircuito();
                repaint();
            }
        });

        adicionaParTopo(linha0Painel, gbc, 0, colunaLinha0, new JLabel(Lang.msg("nomeCircuito")), nomePistaText);
        adicionaParTopo(linha0Painel, gbc, 0, colunaLinha0, new JLabel(Lang.msg("cicloMsTick")), cicloSpinner);
        adicionaParTopo(linha0Painel, gbc, 0, colunaLinha0, new JLabel(Lang.msg("tempoVoltaEstimado")), tempoVoltaEstimadoLabel);
        adicionaParTopo(linha0Painel, gbc, 0, colunaLinha0, new JLabel(Lang.msg("distanciaKmLabel")), distanciaKmText);
        adicionaEspacoTopo(linha0Painel, gbc, 0, colunaLinha0);

        // Os indicadores de cor são campos finais reaproveitados a cada
        // reconstrução do layout (troca/recarga de circuito); sem esta
        // limpeza, cada carregamento empilhava mais um MouseAdapter no mesmo
        // label e um clique abria vários seletores de cor em sequência.
        limpaMouseListeners(corFundoLabel, corAsfaltoLabel, corBox1Label, corBox2Label,
                corZebra1Label, corZebra2Label);

        noite.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                circuito.setNoite(noite.isSelected());
                repaint();
            }
        });

        atualizaCorLabel(corFundoLabel, circuito != null ? circuito.getCorFundo() : null, Color.WHITE);
        corFundoLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corDeFundo"), corFundoLabel.getBackground());
                if (nova != null) {
                    circuito.setCorFundo(nova);
                    atualizaCorLabel(corFundoLabel, nova, Color.WHITE);
                    repaint();
                }
            }
        });

        atualizaCorLabel(corAsfaltoLabel, circuito != null ? circuito.getCorAsfalto() : null,
                DesenhoProceduralCircuito.COR_PISTA);
        corAsfaltoLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corDoAsfalto"), corAsfaltoLabel.getBackground());
                if (nova != null) {
                    circuito.setCorAsfalto(nova);
                    atualizaCorLabel(corAsfaltoLabel, nova, DesenhoProceduralCircuito.COR_PISTA);
                    repaint();
                }
            }
        });

        atualizaCorLabel(corBox1Label, circuito != null ? circuito.getCorBox1() : null, Color.LIGHT_GRAY);
        corBox1Label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corBox1"), corBox1Label.getBackground());
                if (nova != null) {
                    circuito.setCorBox1(nova);
                    atualizaCorLabel(corBox1Label, nova, Color.LIGHT_GRAY);
                    repaint();
                }
            }
        });

        atualizaCorLabel(corBox2Label, circuito != null ? circuito.getCorBox2() : null, Color.GRAY);
        corBox2Label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corBox2"), corBox2Label.getBackground());
                if (nova != null) {
                    circuito.setCorBox2(nova);
                    atualizaCorLabel(corBox2Label, nova, Color.GRAY);
                    repaint();
                }
            }
        });

        atualizaCorLabel(corZebra1Label, circuito != null ? circuito.getCorZebra1() : null, Color.WHITE);
        corZebra1Label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corZebra1"), corZebra1Label.getBackground());
                if (nova != null) {
                    circuito.setCorZebra1(nova);
                    atualizaCorLabel(corZebra1Label, nova, Color.WHITE);
                    repaint();
                }
            }
        });

        atualizaCorLabel(corZebra2Label, circuito != null ? circuito.getCorZebra2() : null, Color.RED);
        corZebra2Label.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        Lang.msg("corZebra2"), corZebra2Label.getBackground());
                if (nova != null) {
                    circuito.setCorZebra2(nova);
                    atualizaCorLabel(corZebra2Label, nova, Color.RED);
                    repaint();
                }
            }
        });

        int[] colunaLinha1 = {0};
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("circuitoAtivo")), ativoCheckBox);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("percentualChuva")), probalidadeChuvaText);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("largura")), larguraPistaSpinner);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("noite")), noite);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoFundo")), corFundoLabel);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoAsfalto")), corAsfaltoLabel);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoBox1")), corBox1Label);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoBox2")), corBox2Label);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoZebra1")), corZebra1Label);
        adicionaParTopo(linha1Painel, gbc, 0, colunaLinha1, new JLabel(Lang.msg("topoZebra2")), corZebra2Label);
        adicionaEspacoTopo(linha1Painel, gbc, 0, colunaLinha1);

        // Linha 2: só controles de teste/visualização — nada aqui é
        // persistido no circuito (ver gerarBotoesVisualizacao/gerarBotoesTestePista).
        int[] colunaLinha2 = {0};
        adicionaComponenteTopo(linha2Painel, gbc, 0, colunaLinha2, gerarBotoesVisualizacao());
        adicionaComponenteTopo(linha2Painel, gbc, 0, colunaLinha2, gerarBotoesTestePista());
        adicionaEspacoTopo(linha2Painel, gbc, 0, colunaLinha2);

        JPanel topo = new JPanel(new GridLayout(3, 1));
        topo.add(linha0Painel);
        topo.add(linha1Painel);
        topo.add(linha2Painel);

        atualizarBotoesNavegacao();
        return topo;
    }

    /**
     * Adiciona um componente avulso (botão de navegação, sub-painel de
     * checkboxes etc.) na linha {@code linha} do grid da barra superior, na
     * próxima coluna livre indicada por {@code coluna} (cursor mutável,
     * incrementado a cada chamada — permite misturar componentes avulsos com
     * pares rótulo/valor na mesma linha sem calcular índice manualmente).
     */
    private void adicionaComponenteTopo(JPanel painel, GridBagConstraints gbc, int linha, int[] coluna,
            JComponent componente) {
        gbc.gridy = linha;
        gbc.gridx = coluna[0]++;
        painel.add(componente, gbc);
    }

    /**
     * Adiciona um par rótulo/valor na linha {@code linha} do grid de dados da
     * barra superior, ocupando as próximas duas colunas livres de
     * {@code coluna}. Rótulo e valor ficam em colunas próprias do
     * {@link GridBagLayout}, então cada um assume a largura do seu próprio
     * conteúdo em vez de todos os valores da grade serem esticados pro
     * tamanho do maior campo (o nome do circuito, por exemplo, ocupa bem mais
     * espaço que um indicador de cor ou um checkbox).
     */
    private void adicionaParTopo(JPanel painel, GridBagConstraints gbc, int linha, int[] coluna,
            JComponent label, JComponent valor) {
        adicionaComponenteTopo(painel, gbc, linha, coluna, label);
        adicionaComponenteTopo(painel, gbc, linha, coluna, valor);
    }

    /**
     * Adiciona um espaço elástico (weightx = 1) como última coluna da linha
     * — sem isto, como nenhum componente real tem weightx, o
     * {@link GridBagLayout} centraliza o bloco inteiro de componentes no
     * painel em vez de grudar à esquerda. Usa um clone do {@code gbc} pra não
     * deixar weightx/fill "vazando" pras próximas chamadas (a próxima linha
     * reusa o mesmo {@code gbc} compartilhado).
     */
    private void adicionaEspacoTopo(JPanel painel, GridBagConstraints gbc, int linha, int[] coluna) {
        GridBagConstraints gbcEspaco = (GridBagConstraints) gbc.clone();
        gbcEspaco.gridy = linha;
        gbcEspaco.gridx = coluna[0]++;
        gbcEspaco.weightx = 1.0;
        gbcEspaco.fill = GridBagConstraints.HORIZONTAL;
        painel.add(Box.createHorizontalGlue(), gbcEspaco);
    }

    private void atualizarBotoesNavegacao() {
        boolean temCircuito = indiceCircuito >= 0 && indiceCircuito < circuitosXml.size();
        if (btnCircuitoAnterior != null) {
            btnCircuitoAnterior.setEnabled(temCircuito && indiceCircuito > 0);
        }
        if (btnCircuitoProximo != null) {
            btnCircuitoProximo.setEnabled(temCircuito && indiceCircuito < circuitosXml.size() - 1);
        }
        if (comboCircuito != null) {
            sincronizandoComboCircuito = true;
            try {
                int indiceValido = (temCircuito && indiceCircuito < comboCircuito.getItemCount())
                        ? indiceCircuito : -1;
                comboCircuito.setSelectedIndex(indiceValido);
            } finally {
                sincronizandoComboCircuito = false;
            }
        }
    }

    private void repopularComboCircuitos() {
        if (comboCircuito == null) {
            return;
        }
        sincronizandoComboCircuito = true;
        try {
            comboCircuito.removeAllItems();
            for (int i = 0; i < circuitosXml.size(); i++) {
                comboCircuito.addItem(new CircuitoComboItem(nomesAmigaveisCircuitos.get(i), circuitosXml.get(i)));
            }
        } finally {
            sincronizandoComboCircuito = false;
        }
        atualizarBotoesNavegacao();
    }

    private void popularCircuitos() {
        circuitosXml.clear();
        nomesAmigaveisCircuitos.clear();
        try {
            Properties p = new Properties();
            InputStream is = CarregadorRecursos.recursoComoStream("properties/circuitos.properties");
            if (is != null) {
                p.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                List<String> arquivos = new ArrayList<String>();
                for (Object k : p.stringPropertyNames()) {
                    arquivos.add((String) k);
                }
                Collections.sort(arquivos);
                circuitosXml.addAll(arquivos);
                for (String arquivo : arquivos) {
                    String valor = p.getProperty(arquivo);
                    String nomeAmigavel = (valor != null && !valor.isEmpty()) ? valor.split(",")[0] : arquivo;
                    nomesAmigaveisCircuitos.add(nomeAmigavel);
                }
            }
        } catch (Exception ex) {
            File dir = new File("src/main/resources/circuitos");
            if (dir.isDirectory()) {
                List<String> arquivos = new ArrayList<String>();
                for (File f : Objects.requireNonNull(dir.listFiles())) {
                    if (f.isFile() && f.getName().endsWith(".xml")) {
                        arquivos.add(f.getName());
                    }
                }
                Collections.sort(arquivos);
                circuitosXml.addAll(arquivos);
                nomesAmigaveisCircuitos.addAll(arquivos);
            }
        }
        repopularComboCircuitos();
    }

    public void navegarCircuito(int delta) {
        int novoIndice = indiceCircuito + delta;
        if (novoIndice < 0 || novoIndice >= circuitosXml.size()) {
            return;
        }
        indiceCircuito = novoIndice;
        String arquivo = circuitosXml.get(indiceCircuito);
        try {
            carregarCircuitoExistente(arquivo);
        } catch (Exception e) {
            e.printStackTrace();
            srcFrame.dialogDeErro(e);
        }
    }

    public void iniciarComNavegacao() {
        popularCircuitos();
        indiceCircuito = -1;
        testePista = new TestePista(this, circuito);
        iniciaEditor();
        atualizaListas();
        srcFrame.pack();
        srcFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void carregarCircuitoExistente(String arquivoXml) throws IOException, ClassNotFoundException {
        file = new File("src/main/resources/circuitos/" + arquivoXml);
        circuito = CarregadorRecursos.carregarCircuito(arquivoXml);
        testePista = new TestePista(this, circuito);
        backGround = CarregadorRecursos.carregaBackGround(circuito.getBackGround(), this, circuito);
        iniciaEditor();
        atualizaListas();
        vetorizarCircuito();
        refletirCircuitoNosCampos();
        srcFrame.pack();
        srcFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        if (circuito.getPistaFull() != null && !circuito.getPistaFull().isEmpty()) {
            centralizarPonto(((No) circuito.getPistaFull().get(0)).getPoint());
        }
        atualizarBotoesNavegacao();
    }

    private void refletirCircuitoNosCampos() {
        larguraPistaSpinner.getModel().setValue(Double.valueOf(circuito.getMultiplicadorLarguraPista()));
        probalidadeChuvaText.setText(String.valueOf(circuito.getProbalidadeChuva()));
        nomePistaText.setText(circuito.getNome());
        noite.setSelected(circuito.isNoite());
        if (ativoCheckBox != null) {
            ativoCheckBox.setSelected(circuito.isAtivo());
        }
        if (cicloSpinner != null) {
            cicloSpinner.getModel().setValue(Integer.valueOf(circuito.getCiclo()));
        }
        if (distanciaKmText != null) {
            distanciaKmText.setText(String.valueOf(circuito.getDistanciaKm()));
        }
        atualizaCorLabel(corFundoLabel, circuito.getCorFundo(), Color.WHITE);
        atualizaCorLabel(corAsfaltoLabel, circuito.getCorAsfalto(), DesenhoProceduralCircuito.COR_PISTA);
        atualizaCorLabel(corBox1Label, circuito.getCorBox1(), Color.LIGHT_GRAY);
        atualizaCorLabel(corBox2Label, circuito.getCorBox2(), Color.GRAY);
        atualizaCorLabel(corZebra1Label, circuito.getCorZebra1(), Color.WHITE);
        atualizaCorLabel(corZebra2Label, circuito.getCorZebra2(), Color.RED);
        atualizaTempoVoltaEstimado();
    }

    /**
     * Atualiza o rótulo de tempo de volta estimado (aproximado — ver
     * {@link Circuito#estimarTempoVoltaMs()}), chamado sempre que ciclo
     * muda ou o circuito é (re)vetorizado.
     */
    private void atualizaTempoVoltaEstimado() {
        if (tempoVoltaEstimadoLabel == null || circuito == null) {
            return;
        }
        tempoVoltaEstimadoLabel.setText(
                "~" + ControleEstatisticas.formatarTempo(Long.valueOf(circuito.estimarTempoVoltaMs())));
    }

    private JPanel gerarListsNosPistaBox() {
        JPanel controlPanel = new JPanel(new BorderLayout());

        pistaJList = new JList(new DefaultListModel());
        pistaJList.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                renderer.setText(value.toString() + " - " + index);
                return renderer;
            }
        });
        boxJList = new JList(new DefaultListModel());
        boxJList.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                renderer.setText(value.toString() + " - " + index);
                return renderer;
            }
        });
        pistaJList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_DELETE) {
                    if (pistaJList.getSelectedValue() == null) {
                        return;
                    }

                    circuito.getPista().remove(pistaJList.getSelectedValue());
                    ((DefaultListModel) pistaJList.getModel()).remove(pistaJList.getSelectedIndex());
                }

            }
        });
        pistaJList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                MainPanelEditor.this.repaint();
                if (pistaJList.getSelectedIndex() > -1) {
                    ultimoItemPistaSelecionado = pistaJList.getSelectedIndex();
                }

                if (ultimoItemPistaSelecionado < pistaJList.getModel().getSize()) {
                    No no = (No) ((DefaultListModel) pistaJList.getModel()).get(ultimoItemPistaSelecionado);
                    centralizarPonto(no.getPoint());
                }

                boxJList.clearSelection();
            }
        });
        pistaJList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ultimoItemPistaSelecionado = pistaJList.getSelectedIndex();
            }

        });
        boxJList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                MainPanelEditor.this.repaint();
                if (boxJList.getSelectedIndex() > -1) {
                    ultimoItemBoxSelecionado = boxJList.getSelectedIndex();
                }
                if (ultimoItemBoxSelecionado < boxJList.getModel().getSize()) {
                    No no = (No) ((DefaultListModel) boxJList.getModel()).get(ultimoItemBoxSelecionado);
                    centralizarPonto(no.getPoint());
                }

                pistaJList.clearSelection();
            }
        });

        boxJList.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                ultimoItemBoxSelecionado = boxJList.getSelectedIndex();
            }

        });
        boxJList.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_DELETE) {
                    if (boxJList.getSelectedValue() == null) {
                        return;
                    }

                    circuito.getBox().remove(boxJList.getSelectedValue());
                    ((DefaultListModel) boxJList.getModel()).remove(boxJList.getSelectedIndex());
                }
            }
        });
        JPanel radioPistaPanel = new JPanel();
        radioPistaPanel.add(new JLabel(Lang.msg("032")));
        JPanel pistas = new JPanel();
        pistas.setLayout(new BorderLayout());
        pistas.add(radioPistaPanel, BorderLayout.NORTH);
        JScrollPane pistaJListJScrollPane = new JScrollPane(pistaJList);
        pistas.add(pistaJListJScrollPane, BorderLayout.CENTER);
        JPanel boxes = new JPanel();
        boxes.setLayout(new BorderLayout());
        radioPistaPanel = new JPanel();
        radioPistaPanel.add(new JLabel(Lang.msg("033")));
        boxes.add(radioPistaPanel, BorderLayout.NORTH);
        JScrollPane boxJListJScrollPane = new JScrollPane(boxJList);
        boxes.add(boxJListJScrollPane, BorderLayout.CENTER);

        // Split vertical (não GridLayout de altura fixa): pista e box têm o
        // mesmo conteúdo (rótulo + lista, sem botões próprios — os controles
        // de nó ficam em gerarPainelControlesNos(), compartilhados acima).
        // 50/50 mantém a proporção atual como ponto de partida, mas agora
        // redimensionável pelo usuário.
        JSplitPane splitNos = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pistas, boxes);
        splitNos.setResizeWeight(0.5);
        splitNos.setOneTouchExpandable(true);
        controlPanel.add(splitNos, BorderLayout.CENTER);
        return controlPanel;
    }

    private void gerarLayout(JFrame frame) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        if (backGround != null)
            this.setPreferredSize(new Dimension(backGround.getWidth(), backGround.getHeight()));
        else {
            this.setPreferredSize(new Dimension(10000, 10000));
        }
        frame.getContentPane().add(gerarTopoNavegacaoEAcoes(), BorderLayout.NORTH);

        frame.getContentPane().add(gerarSplitPaneLateral(), BorderLayout.EAST);

        scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    public void esquerda() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point p = scrollPane.getViewport().getViewPosition();
                if (p == null) {
                    return;
                }
                p.x -= 40;
                repaint();
                scrollPane.getViewport().setViewPosition(p);
            }
        });
    }

    public void direita() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point p = scrollPane.getViewport().getViewPosition();
                if (p == null) {
                    return;
                }
                p.x += 40;
                repaint();
                scrollPane.getViewport().setViewPosition(p);

            }
        });
    }

    public void cima() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point p = scrollPane.getViewport().getViewPosition();
                if (p == null) {
                    return;
                }
                p.y -= 40;
                repaint();
                scrollPane.getViewport().setViewPosition(p);

            }
        });
    }

    public void baixo() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point p = scrollPane.getViewport().getViewPosition();
                if (p == null) {
                    return;
                }
                p.y += 40;
                repaint();
                scrollPane.getViewport().setViewPosition(p);

            }
        });
    }

    private void adicionaEventosMouse(final JFrame frame) {
        for (java.awt.event.MouseListener ml : this.getMouseListeners()) {
            this.removeMouseListener(ml);
        }
        for (java.awt.event.MouseMotionListener mml : this.getMouseMotionListeners()) {
            this.removeMouseMotionListener(mml);
        }
        MouseAdapter mouseAdapter = new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                frame.requestFocus();
            }

            public void mousePressed(MouseEvent e) {
                if (editandoPontosDe != null && isSemSelecao() && !posicionaObjetoPista && !desenhandoObjetoLivre
                        && tentarIniciarArrasteVerticeOuHaste(e)) {
                    return;
                }
                if (editandoPontosGuardRailsDe != null && isSemSelecao() && !posicionaObjetoPista
                        && !desenhandoObjetoLivre && tentarIniciarArrastePontoGuardRails(e)) {
                    return;
                }
                if (editandoPontosEscapadaDe != null && isSemSelecao() && !posicionaObjetoPista
                        && !desenhandoObjetoLivre && tentarIniciarArrastePontoEscapada(e)) {
                    return;
                }
                if (!isSemSelecao() || !SwingUtilities.isLeftMouseButton(e)
                        || posicionaObjetoPista || desenhandoObjetoLivre) {
                    return;
                }
                ObjetoPista encontrado = encontraObjetoPista(e.getPoint());
                if (encontrado == null || encontrado.getPosicaoQuina() == null) {
                    return;
                }
                objetoPista = encontrado;
                objetoArrastando = encontrado;
                selecionarNasListas(encontrado);
                offsetArraste = new Point(e.getX() - encontrado.getPosicaoQuina().x,
                        e.getY() - encontrado.getPosicaoQuina().y);
            }

            public void mouseDragged(MouseEvent e) {
                if (verticeArrastando != null) {
                    arrastouVertice = true;
                    Point offset = calculaOffsetTelaObjetoLivre(editandoPontosDe);
                    Point localAlvo = localDaTela(e.getPoint(), offset);
                    if (arrastandoHasteDoVertice) {
                        verticeArrastando.setHasteFim(localAlvo);
                    } else {
                        Point posicaoAntiga = verticeArrastando.getPosicao();
                        int deltaX = localAlvo.x - posicaoAntiga.x;
                        int deltaY = localAlvo.y - posicaoAntiga.y;
                        verticeArrastando.setPosicao(localAlvo);
                        if (verticeArrastando.getHasteFim() != null) {
                            verticeArrastando.setHasteFim(new Point(
                                    verticeArrastando.getHasteFim().x + deltaX,
                                    verticeArrastando.getHasteFim().y + deltaY));
                        }
                    }
                    editandoPontosDe.gerar();
                    repaint();
                    return;
                }
                if (indicePontoGuardRailsArrastando >= 0) {
                    arrastouPontoGuardRails = true;
                    Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
                    Point localAlvo = localDaTela(e.getPoint(), offset);
                    editandoPontosGuardRailsDe.getPontos().set(indicePontoGuardRailsArrastando, localAlvo);
                    editandoPontosGuardRailsDe.gerar();
                    repaint();
                    return;
                }
                if (indicePontoEscapadaArrastando >= 0) {
                    arrastouPontoEscapada = true;
                    Point offset = calculaOffsetTelaEscapada(editandoPontosEscapadaDe);
                    Point localAlvo = localDaTela(e.getPoint(), offset);
                    editandoPontosEscapadaDe.getPontos().set(indicePontoEscapadaArrastando, localAlvo);
                    editandoPontosEscapadaDe.gerar();
                    // Resincroniza posicaoQuina com os bounds atuais de
                    // pontos (offset volta a ficar em 0,0) — sem isso, a
                    // PRÓXIMA chamada a calculaOffsetTelaEscapada (no
                    // próximo mouseDragged, ou no mouseReleased) calcularia
                    // um deslocamento errado a partir daqui, corrompendo a
                    // posição gravada mesmo quando o índice de nó continua
                    // correto (ver finalizarArrastePontoEscapada).
                    editandoPontosEscapadaDe.setPosicaoQuina(editandoPontosEscapadaDe.obterArea().getLocation());
                    repaint();
                    return;
                }
                if (objetoArrastando == null) {
                    return;
                }
                arrastouObjeto = true;
                objetoArrastando.setPosicaoQuina(
                        new Point(e.getX() - offsetArraste.x, e.getY() - offsetArraste.y));
                reprocessaEscapadaSeNecessario(objetoArrastando);
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                verticeArrastando = null;
                if (indicePontoGuardRailsArrastando >= 0) {
                    Point telaComSnap = aplicaSnap(e.getPoint(), editandoPontosGuardRailsDe);
                    Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
                    Point localComSnap = localDaTela(telaComSnap, offset);
                    editandoPontosGuardRailsDe.moverPonto(indicePontoGuardRailsArrastando, localComSnap);
                    indicePontoGuardRailsArrastando = -1;
                    repaint();
                }
                if (indicePontoEscapadaArrastando >= 0) {
                    finalizarArrastePontoEscapada(e.getPoint());
                    indicePontoEscapadaArrastando = -1;
                    repaint();
                }
                objetoArrastando = null;
            }

            public void mouseClicked(MouseEvent e) {
                if (arrastouVertice) {
                    arrastouVertice = false;
                    return;
                }
                if (arrastouPontoGuardRails) {
                    arrastouPontoGuardRails = false;
                    return;
                }
                if (arrastouPontoEscapada) {
                    arrastouPontoEscapada = false;
                    return;
                }
                if (arrastouObjeto) {
                    arrastouObjeto = false;
                    return;
                }
                if (isSemSelecao()) {
                    clickEditarObjetos(e);
                } else {
                    No no = new No();
                    no.setTipo(getTipoNo());
                    no.setPoint(e.getPoint());
                    inserirNoNasJList(no);
                    ultimoNo = no;
                }
                repaint();
            }

            private void clickEditarObjetos(MouseEvent e) {
                ultimoClicado = e.getPoint();
                if (editandoPontosGuardRailsDe != null && !desenhandoObjetoLivre) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (!removerPontoGuardRailsSeAtingido(e.getPoint())) {
                            mostraMenuContextoObjeto(e);
                        } else {
                            repaint();
                        }
                        return;
                    }
                    if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1 && !posicionaObjetoPista) {
                        tentarInserirPontoGuardRails(e.getPoint());
                        repaint();
                        return;
                    }
                }
                if (SwingUtilities.isRightMouseButton(e) && !posicionaObjetoPista && !desenhandoObjetoLivre) {
                    mostraMenuContextoObjeto(e);
                    return;
                }
                if (e.getClickCount() > 1) {
                    editaObjetoPista(ultimoClicado);
                    return;
                }
                if (desenhandoObjetoLivre && (objetoPista instanceof ObjetoTransparencia)) {
                    ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        objetoTransparencia.getPontos().add(ultimoClicado);
                    } else {
                        desenhandoObjetoLivre = false;
                        // sem isto, posicionaObjetoPista continuava travado em true pra
                        // sempre depois desta criação (só é zerado no fluxo de "canto"
                        // logo abaixo), bloqueando mousePressed() e o menu de contexto
                        // (botão direito) pra qualquer objeto dali em diante.
                        posicionaObjetoPista = false;
                        objetoTransparencia.setTransparencia(125);
                        if (circuito.getObjetos() == null)
                            circuito.setObjetos(new ArrayList<ObjetoPista>());
                        circuito.getObjetos().add(objetoTransparencia);
                        formularioListaObjetosFuncao.listarObjetos();
                        objetoTransparencia.setNome("Objeto " + circuito.getObjetos().size());
                        objetoTransparencia.gerar();
                        // posicaoQuina precisa ficar definida (mesmo que igual aos bounds
                        // atuais, sem mudar nada visualmente) para o objeto poder ser
                        // clicado e arrastado depois — sem isso mousePressed() ignora o
                        // objeto (posicaoQuina == null é tratado como "não arrastável").
                        objetoTransparencia.setPosicaoQuina(objetoTransparencia.obterArea().getLocation());
                        objetoPista = null;
                    }
                    repaint();
                    return;
                } else if (desenhandoObjetoLivre && (objetoPista instanceof ObjetoLivre)) {
                    ObjetoLivre objetoLivre = (ObjetoLivre) objetoPista;
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        objetoLivre.getPontos().add(ultimoClicado);
                    } else {
                        desenhandoObjetoLivre = false;
                        posicionaObjetoPista = false;
                        // ObjetoLivre é objeto de cenário/desenho (ver TipoObjetoPista.LIVRE),
                        // não de função como Transparencia — vai para objetosCenario.
                        if (circuito.getObjetosCenario() == null)
                            circuito.setObjetosCenario(new ArrayList<ObjetoPista>());
                        circuito.getObjetosCenario().add(objetoLivre);
                        formularioListaObjetosDesenho.listarObjetos();
                        objetoLivre.setNome("Objeto " + circuito.getObjetosCenario().size());
                        objetoLivre.gerar();
                        objetoLivre.setPosicaoQuina(objetoLivre.obterArea().getLocation());
                        objetoPista = null;
                    }
                    repaint();
                    return;
                } else if (desenhandoObjetoLivre && (objetoPista instanceof ObjetoGuardRails)) {
                    ObjetoGuardRails guardRails = (ObjetoGuardRails) objetoPista;
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        guardRails.getPontos().add(aplicaSnap(ultimoClicado, guardRails));
                    } else {
                        desenhandoObjetoLivre = false;
                        posicionaObjetoPista = false;
                        // GuardRails é objeto de cenário/desenho (ver TipoObjetoPista.GUARD_RAILS),
                        // não de função como Transparencia — vai para objetosCenario.
                        if (circuito.getObjetosCenario() == null)
                            circuito.setObjetosCenario(new ArrayList<ObjetoPista>());
                        circuito.getObjetosCenario().add(guardRails);
                        formularioListaObjetosDesenho.listarObjetos();
                        guardRails.setNome("Objeto " + circuito.getObjetosCenario().size());
                        guardRails.gerar();
                        guardRails.setPosicaoQuina(guardRails.obterArea().getLocation());
                        objetoPista = null;
                    }
                    repaint();
                    return;
                } else if (desenhandoObjetoLivre && (objetoPista instanceof ObjetoEscapada)) {
                    ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (escapada.getPontos().isEmpty()) {
                            // Primeiro clique = nó de entrada: precisa validar
                            // contra o traçado 1 ou 2 (nunca o 0/central).
                            No noEntrada = noMaisProximoTracado1e2(ultimoClicado);
                            if (noEntrada == null) {
                                alertaPontoEscapadaInvalido();
                            } else {
                                escapada.getPontos().add(noEntrada.getPoint());
                                escapada.setTracadoOrigem(noEntrada.getTracado());
                                escapada.setIndiceEntrada(noEntrada.getIndex());
                            }
                        } else {
                            // Pontos seguintes = trajeto da zona de escapada:
                            // livres, sem validação nenhuma.
                            escapada.getPontos().add(ultimoClicado);
                        }
                    } else if (escapada.getPontos().isEmpty()) {
                        // Botão direito sem nenhum ponto de entrada ainda: não
                        // há o que finalizar.
                        alertaPontoEscapadaInvalido();
                    } else {
                        // Clique direito = nó de saída: precisa validar contra
                        // o MESMO traçado da entrada, e finaliza o objeto.
                        No noSaida = noMaisProximoDoTracado(ultimoClicado, escapada.getTracadoOrigem());
                        if (noSaida == null) {
                            alertaPontoEscapadaInvalido();
                        } else {
                            escapada.getPontos().add(noSaida.getPoint());
                            escapada.setIndiceSaida(noSaida.getIndex());
                            desenhandoObjetoLivre = false;
                            posicionaObjetoPista = false;
                            if (circuito.getObjetos() == null)
                                circuito.setObjetos(new ArrayList<ObjetoPista>());
                            circuito.getObjetos().add(escapada);
                            formularioListaObjetosFuncao.listarObjetos();
                            escapada.setNome("Objeto " + circuito.getObjetos().size());
                            escapada.gerar();
                            escapada.setPosicaoQuina(escapada.obterArea().getLocation());
                            objetoPista = null;
                        }
                    }
                    repaint();
                    return;
                } else if (posicionaObjetoPista && objetoPista != null) {
                    List<ObjetoPista> listaAlvo;
                    FormularioListaObjetos formularioListaAlvo;
                    if (criandoObjetoCenario) {
                        if (circuito.getObjetosCenario() == null) {
                            circuito.setObjetosCenario(new ArrayList<ObjetoPista>());
                        }
                        listaAlvo = circuito.getObjetosCenario();
                        formularioListaAlvo = formularioListaObjetosDesenho;
                    } else {
                        if (circuito.getObjetos() == null) {
                            circuito.setObjetos(new ArrayList<ObjetoPista>());
                        }
                        listaAlvo = circuito.getObjetos();
                        formularioListaAlvo = formularioListaObjetosFuncao;
                    }
                    Point quina = new Point(ultimoClicado);
                    quina.x -= objetoPista.getLargura() / 2;
                    quina.y -= objetoPista.getAltura() / 2;
                    objetoPista.setPosicaoQuina(quina);
                    listaAlvo.add(objetoPista);
                    formularioListaAlvo.listarObjetos();
                    objetoPista.setNome("Objeto " + listaAlvo.size());
                    reprocessaEscapadaSeNecessario(objetoPista);
                    repaint();
                    posicionaObjetoPista = false;
                    return;
                } else if (pontosEscape) {
                    repaint();
                    pontosEscape = false;
                    return;
                }

                Logger.logar("Pontos Editor :" + e.getX() + " - " + e.getY());
                if ((getTipoNo() == null) || (e.getButton() == 3)) {
                    srcFrame.requestFocus();

                    return;
                }
            }

            private void mostraMenuContextoObjeto(MouseEvent e) {
                ObjetoPista encontrado = encontraObjetoPista(e.getPoint());
                if (encontrado == null) {
                    return;
                }
                objetoPista = encontrado;
                selecionarNasListas(encontrado);
                JPopupMenu menu = new JPopupMenu();
                menu.add(criaPainelAjusteRapido(encontrado));
                menu.show(MainPanelEditor.this, e.getX(), e.getY());
            }
        };
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
    }

    private JPanel criaPainelAjusteRapido(final ObjetoPista alvo) {
        // Largura/Altura não fazem sentido para ObjetoLivre: sua área vem
        // dos vértices/pontos desenhados (ver ObjetoLivre.obterArea()), não
        // desses campos, que ficam sem efeito no desenho. GuardRails também
        // é desenhado por pontos (ver ObjetoGuardRails), mas largura ainda
        // vale — é a espessura da barreira ao longo de todo o encadeamento;
        // só altura e ângulo (por segmento, calculados a partir dos pontos)
        // ficam sem efeito.
        boolean ehObjetoLivre = alvo instanceof ObjetoLivre;
        boolean ehGuardRails = alvo instanceof ObjetoGuardRails;
        // Escapada também é definida por pontos (saída/retorno), não por um
        // retângulo largura×altura rotacionado — mesmo motivo de GuardRails
        // esconder Altura/Ângulo, mas Largura continua valendo como
        // espessura do traçado desenhado.
        boolean ehObjetoEscapada = alvo instanceof ObjetoEscapada;
        boolean mostraLargura = !ehObjetoLivre;
        boolean mostraAltura = !ehObjetoLivre && !ehGuardRails && !ehObjetoEscapada;
        boolean mostraAngulo = !ehObjetoLivre && !ehGuardRails && !ehObjetoEscapada;
        // Empilhamento (quantidade/direção/grau) é transversal a qualquer
        // tipo de ObjetoConstrucao; afunilamento só faz sentido para BARCO.
        boolean ehObjetoConstrucao = alvo instanceof ObjetoConstrucao;
        boolean mostraAfunilamento = ehObjetoConstrucao
                && ((ObjetoConstrucao) alvo).getTipo() == TipoObjetoConstrucao.BARCO;
        int linhasConstrucao = ehObjetoConstrucao ? (3 + (mostraAfunilamento ? 1 : 0)) : 0;
        int linhas = (mostraLargura ? 1 : 0) + (mostraAltura ? 1 : 0) + (mostraAngulo ? 1 : 0) + linhasConstrucao;
        JPanel panel = new JPanel(new GridLayout(Math.max(1, linhas), 2));
        // Objetos de desenho não aceitam largura/altura menor que 1 nem
        // ângulo negativo (ver ObjetoDesenho); objetos de função (Escapada,
        // Transparencia) continuam sem essa restrição.
        boolean objetoDeDesenho = alvo instanceof ObjetoDesenho;
        int larguraMinima = objetoDeDesenho ? 1 : 0;
        int anguloMinimo = objetoDeDesenho ? 0 : -360;
        if (mostraLargura) {
            JSpinner larguraSpinner = new JSpinner(new SpinnerNumberModel(alvo.getLargura(), larguraMinima, 10000, 1));
            panel.add(new JLabel(Lang.msg("largura")));
            panel.add(larguraSpinner);
            larguraSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    alvo.setLargura(((Integer) larguraSpinner.getValue()).intValue());
                    reprocessaEscapadaSeNecessario(alvo);
                    repaint();
                }
            });
        }
        if (mostraAltura) {
            JSpinner alturaSpinner = new JSpinner(new SpinnerNumberModel(alvo.getAltura(), larguraMinima, 10000, 1));
            panel.add(new JLabel(Lang.msg("altura")));
            panel.add(alturaSpinner);
            alturaSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    alvo.setAltura(((Integer) alturaSpinner.getValue()).intValue());
                    reprocessaEscapadaSeNecessario(alvo);
                    repaint();
                }
            });
        }
        if (mostraAngulo) {
            JSpinner anguloSpinner = new JSpinner(
                    new SpinnerNumberModel((int) alvo.getAngulo(), anguloMinimo, 360, 1));
            panel.add(new JLabel(Lang.msg("angulo")));
            panel.add(anguloSpinner);
            anguloSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    alvo.setAngulo(((Integer) anguloSpinner.getValue()).doubleValue());
                    reprocessaEscapadaSeNecessario(alvo);
                    repaint();
                }
            });
        }
        if (alvo instanceof ObjetoLivre) {
            final ObjetoLivre objetoLivre = (ObjetoLivre) alvo;
            boolean editandoEsteObjeto = objetoLivre.equals(editandoPontosDe);
            JButton editarPontosButton = new JButton(editandoEsteObjeto ? Lang.msg("pararDeEditarPontos") : Lang.msg("editarPontos"));
            editarPontosButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (objetoLivre.equals(editandoPontosDe)) {
                        encerrarEdicaoPontosObjetoLivre();
                    } else {
                        iniciarEdicaoPontosObjetoLivre(objetoLivre);
                    }
                }
            });
            panel.add(editarPontosButton);
            panel.add(new JLabel());
        }
        if (alvo instanceof ObjetoGuardRails) {
            final ObjetoGuardRails guardRails = (ObjetoGuardRails) alvo;
            boolean editandoEsteObjeto = guardRails.equals(editandoPontosGuardRailsDe);
            JButton editarPontosButton = new JButton(editandoEsteObjeto ? Lang.msg("pararDeEditarPontos") : Lang.msg("editarPontos"));
            editarPontosButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (guardRails.equals(editandoPontosGuardRailsDe)) {
                        encerrarEdicaoPontosGuardRails();
                    } else {
                        iniciarEdicaoPontosGuardRails(guardRails);
                    }
                }
            });
            panel.add(editarPontosButton);
            panel.add(new JLabel());
        }
        if (alvo instanceof ObjetoEscapada) {
            final ObjetoEscapada escapada = (ObjetoEscapada) alvo;
            boolean editandoEsteObjeto = escapada.equals(editandoPontosEscapadaDe);
            JButton editarPontosButton = new JButton(editandoEsteObjeto ? Lang.msg("pararDeEditarPontos") : Lang.msg("editarPontos"));
            editarPontosButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (escapada.equals(editandoPontosEscapadaDe)) {
                        encerrarEdicaoPontosEscapada();
                    } else {
                        iniciarEdicaoPontosEscapada(escapada);
                    }
                }
            });
            panel.add(editarPontosButton);
            panel.add(new JLabel());
        }
        if (alvo instanceof ObjetoConstrucao) {
            final ObjetoConstrucao objetoConstrucao = (ObjetoConstrucao) alvo;
            if (mostraAfunilamento) {
                final JSpinner afunilamentoSpinner = new JSpinner(
                        new SpinnerNumberModel(objetoConstrucao.getAfunilamento(), 0, 90, 1));
                panel.add(new JLabel(Lang.msg("afunilamento")));
                panel.add(afunilamentoSpinner);
                afunilamentoSpinner.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent e) {
                        objetoConstrucao.setAfunilamento(((Integer) afunilamentoSpinner.getValue()).intValue());
                        repaint();
                    }
                });
            }
            final JSpinner quantidadeEmpilhamentoSpinner = new JSpinner(
                    new SpinnerNumberModel(objetoConstrucao.getQuantidadeEmpilhamento(), 1, 20, 1));
            panel.add(new JLabel(Lang.msg("qtdEmpilhamento")));
            panel.add(quantidadeEmpilhamentoSpinner);
            quantidadeEmpilhamentoSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    objetoConstrucao
                            .setQuantidadeEmpilhamento(((Integer) quantidadeEmpilhamentoSpinner.getValue()).intValue());
                    repaint();
                }
            });
            final JComboBox<DirecaoEmpilhamento> direcaoEmpilhamentoCombo = new JComboBox<DirecaoEmpilhamento>(
                    DirecaoEmpilhamento.values());
            direcaoEmpilhamentoCombo.setSelectedItem(objetoConstrucao.getDirecaoEmpilhamento());
            panel.add(new JLabel(Lang.msg("direcaoEmpilhamento")));
            panel.add(direcaoEmpilhamentoCombo);
            direcaoEmpilhamentoCombo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    objetoConstrucao
                            .setDirecaoEmpilhamento((DirecaoEmpilhamento) direcaoEmpilhamentoCombo.getSelectedItem());
                    repaint();
                }
            });
            final JSpinner grauEmpilhamentoSpinner = new JSpinner(
                    new SpinnerNumberModel(objetoConstrucao.getGrauEmpilhamento(), 0, 1000, 1));
            panel.add(new JLabel(Lang.msg("grauEmpilhamento")));
            panel.add(grauEmpilhamentoSpinner);
            grauEmpilhamentoSpinner.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    objetoConstrucao.setGrauEmpilhamento(((Integer) grauEmpilhamentoSpinner.getValue()).intValue());
                    repaint();
                }
            });
        }
        return panel;
    }

    /**
     * Liga o modo de edição de pontos/hastes (estilo ferramenta de caminhos
     * do GIMP) para um ObjetoLivre já posicionado no circuito. Clique
     * esquerdo arrastando um vértice o reposiciona; clique direito
     * arrastando um vértice (ou a ponta de uma haste já ajustada) muda a
     * curvatura dos segmentos adjacentes.
     */
    public void iniciarEdicaoPontosObjetoLivre(ObjetoLivre objetoLivre) {
        objetoLivre.inicializarVerticesSeNecessario();
        objetoLivre.gerar();
        editandoPontosDe = objetoLivre;
        objetoPista = objetoLivre;
        repaint();
    }

    public void encerrarEdicaoPontosObjetoLivre() {
        editandoPontosDe = null;
        verticeArrastando = null;
        repaint();
    }

    /**
     * Deslocamento entre o espaço local dos vértices (onde eles foram
     * originalmente clicados/criados) e a tela: {@code desenha()} sempre
     * translada o path gerado a partir do zero para alinhar com
     * {@code posicaoQuina}, então esse deslocamento é
     * {@code posicaoQuina - bounds do path recém-gerado (local, sem
     * translação)} — recalculado a cada chamada para acompanhar mudanças na
     * própria geometria durante o arraste.
     */
    private Point calculaOffsetTelaObjetoLivre(ObjetoLivre objetoLivre) {
        if (objetoLivre.getPosicaoQuina() == null) {
            return new Point(0, 0);
        }
        objetoLivre.gerar();
        Rectangle boundsLocal = objetoLivre.getForma().getBounds();
        return new Point(objetoLivre.getPosicaoQuina().x - boundsLocal.x,
                objetoLivre.getPosicaoQuina().y - boundsLocal.y);
    }

    private static Point telaDoLocal(Point local, Point offset) {
        return new Point(local.x + offset.x, local.y + offset.y);
    }

    private static Point localDaTela(Point tela, Point offset) {
        return new Point(tela.x - offset.x, tela.y - offset.y);
    }

    private static double distancia(Point a, Point b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Testa se o clique atingiu um vértice ou a ponta de haste de um vértice
     * do ObjetoLivre em edição, e se sim, inicia o arraste correspondente.
     * Botão direito testa primeiro a ponta de haste já ajustada (para
     * ajuste fino) e, se não atingir nenhuma, cai para o próprio vértice
     * (iniciando uma haste nova a partir dele); botão esquerdo só testa os
     * vértices (reposiciona o ponto, arrastando a haste junto).
     */
    private boolean tentarIniciarArrasteVerticeOuHaste(MouseEvent e) {
        if (editandoPontosDe == null) {
            return false;
        }
        boolean botaoDireito = SwingUtilities.isRightMouseButton(e);
        boolean botaoEsquerdo = SwingUtilities.isLeftMouseButton(e);
        if (!botaoDireito && !botaoEsquerdo) {
            return false;
        }
        Point offset = calculaOffsetTelaObjetoLivre(editandoPontosDe);
        if (botaoDireito) {
            for (PontoCurva vertice : editandoPontosDe.getVertices()) {
                Point hasteTela = telaDoLocal(vertice.getControleSaida(), offset);
                if (distancia(e.getPoint(), hasteTela) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                    verticeArrastando = vertice;
                    arrastandoHasteDoVertice = true;
                    return true;
                }
            }
        }
        for (PontoCurva vertice : editandoPontosDe.getVertices()) {
            Point posTela = telaDoLocal(vertice.getPosicao(), offset);
            if (distancia(e.getPoint(), posTela) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                verticeArrastando = vertice;
                arrastandoHasteDoVertice = botaoDireito;
                return true;
            }
        }
        return false;
    }

    private void desenhaMarcadoresEdicaoPontos(Graphics2D g2d) {
        if (editandoPontosDe == null) {
            return;
        }
        Point offset = calculaOffsetTelaObjetoLivre(editandoPontosDe);
        for (PontoCurva vertice : editandoPontosDe.getVertices()) {
            Point posTela = telaDoLocal(vertice.getPosicao(), offset);
            Point hasteTela = telaDoLocal(vertice.getControleSaida(), offset);
            if (!hasteTela.equals(posTela)) {
                g2d.setColor(Color.CYAN);
                g2d.drawLine(posTela.x, posTela.y, hasteTela.x, hasteTela.y);
                g2d.setColor(Color.BLUE);
                g2d.fillOval(hasteTela.x - 4, hasteTela.y - 4, 8, 8);
            }
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(posTela.x - 5, posTela.y - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(posTela.x - 5, posTela.y - 5, 10, 10);
        }
    }

    /**
     * Liga o modo de edição de pontos (mover, inserir, remover) para um
     * ObjetoGuardRails já posicionado no circuito — análogo a
     * {@link #iniciarEdicaoPontosObjetoLivre(ObjetoLivre)}, mas sem hastes de
     * curvatura: clique esquerdo arrasta um ponto existente, clique esquerdo
     * perto de um segmento insere um ponto novo, clique direito sobre um
     * ponto o remove.
     */
    public void iniciarEdicaoPontosGuardRails(ObjetoGuardRails guardRails) {
        guardRails.gerar();
        editandoPontosGuardRailsDe = guardRails;
        objetoPista = guardRails;
        repaint();
    }

    public void encerrarEdicaoPontosGuardRails() {
        editandoPontosGuardRailsDe = null;
        indicePontoGuardRailsArrastando = -1;
        repaint();
    }

    /**
     * Deslocamento entre o espaço local de {@link ObjetoGuardRails#getPontos()}
     * (onde os pontos foram originalmente clicados/criados) e a tela — mesma
     * ideia de {@link #calculaOffsetTelaObjetoLivre(ObjetoLivre)}.
     */
    private Point calculaOffsetTelaGuardRails(ObjetoGuardRails guardRails) {
        if (guardRails == null || guardRails.getPosicaoQuina() == null) {
            return new Point(0, 0);
        }
        guardRails.gerar();
        Rectangle boundsLocal = guardRails.obterArea();
        return new Point(guardRails.getPosicaoQuina().x - boundsLocal.x,
                guardRails.getPosicaoQuina().y - boundsLocal.y);
    }

    /**
     * Testa se o clique atingiu um ponto existente de
     * {@link #editandoPontosGuardRailsDe} e, se sim, inicia o arraste desse
     * ponto (só botão esquerdo — direito é tratado em
     * {@link #removerPontoGuardRailsSeAtingido(Point)}).
     */
    private boolean tentarIniciarArrastePontoGuardRails(MouseEvent e) {
        if (editandoPontosGuardRailsDe == null || !SwingUtilities.isLeftMouseButton(e)) {
            return false;
        }
        Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
        List<Point> pontos = editandoPontosGuardRailsDe.getPontos();
        for (int i = 0; i < pontos.size(); i++) {
            Point posTela = telaDoLocal(pontos.get(i), offset);
            if (distancia(e.getPoint(), posTela) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                indicePontoGuardRailsArrastando = i;
                return true;
            }
        }
        return false;
    }

    /** Remove o ponto de {@link #editandoPontosGuardRailsDe} atingido por {@code pontoTela}, se houver. */
    private boolean removerPontoGuardRailsSeAtingido(Point pontoTela) {
        if (editandoPontosGuardRailsDe == null) {
            return false;
        }
        Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
        List<Point> pontos = editandoPontosGuardRailsDe.getPontos();
        for (int i = 0; i < pontos.size(); i++) {
            if (distancia(pontoTela, telaDoLocal(pontos.get(i), offset)) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                editandoPontosGuardRailsDe.removerPonto(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Se {@code pontoTela} não atingir nenhum ponto existente mas estiver
     * perto o bastante de algum segmento do encadeamento, insere um novo
     * ponto ali (com snap aplicado), aumentando o encadeamento em um ponto.
     */
    private boolean tentarInserirPontoGuardRails(Point pontoTela) {
        if (editandoPontosGuardRailsDe == null) {
            return false;
        }
        Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
        List<Point> pontos = editandoPontosGuardRailsDe.getPontos();
        for (Point ponto : pontos) {
            if (distancia(pontoTela, telaDoLocal(ponto, offset)) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                return false;
            }
        }
        for (int i = 0; i < pontos.size() - 1; i++) {
            Point aTela = telaDoLocal(pontos.get(i), offset);
            Point bTela = telaDoLocal(pontos.get(i + 1), offset);
            if (distanciaAoSegmento(pontoTela, aTela, bTela) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                Point telaComSnap = aplicaSnap(pontoTela, editandoPontosGuardRailsDe);
                Point localComSnap = localDaTela(telaComSnap, offset);
                editandoPontosGuardRailsDe.inserirPonto(i, localComSnap);
                return true;
            }
        }
        return false;
    }

    private static double distanciaAoSegmento(Point p, Point a, Point b) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double comprimentoQuadrado = dx * dx + dy * dy;
        if (comprimentoQuadrado == 0) {
            return distancia(p, a);
        }
        double t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / comprimentoQuadrado;
        t = Math.max(0, Math.min(1, t));
        double projX = a.x + t * dx;
        double projY = a.y + t * dy;
        return distancia(p, new Point((int) Math.round(projX), (int) Math.round(projY)));
    }

    private void desenhaMarcadoresEdicaoPontosGuardRails(Graphics2D g2d) {
        if (editandoPontosGuardRailsDe == null) {
            return;
        }
        Point offset = calculaOffsetTelaGuardRails(editandoPontosGuardRailsDe);
        for (Point ponto : editandoPontosGuardRailsDe.getPontos()) {
            Point posTela = telaDoLocal(ponto, offset);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(posTela.x - 5, posTela.y - 5, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawOval(posTela.x - 5, posTela.y - 5, 10, 10);
        }
    }

    /**
     * Liga o modo de edição de pontos (mover qualquer ponto do encadeamento)
     * para um ObjetoEscapada já posicionado no circuito — análogo a
     * {@link #iniciarEdicaoPontosGuardRails(ObjetoGuardRails)}. O primeiro e
     * o último ponto (entrada/saída) continuam validados contra o traçado ao
     * serem soltos; os do meio (trajeto livre) não têm validação nenhuma.
     */
    public void iniciarEdicaoPontosEscapada(ObjetoEscapada escapada) {
        escapada.gerar();
        editandoPontosEscapadaDe = escapada;
        objetoPista = escapada;
        repaint();
    }

    public void encerrarEdicaoPontosEscapada() {
        editandoPontosEscapadaDe = null;
        indicePontoEscapadaArrastando = -1;
        repaint();
    }

    /**
     * Deslocamento entre o espaço local de {@link ObjetoEscapada#getPontos()}
     * e a tela — mesma ideia de {@link #calculaOffsetTelaGuardRails(ObjetoGuardRails)}.
     */
    private Point calculaOffsetTelaEscapada(ObjetoEscapada escapada) {
        if (escapada == null || escapada.getPosicaoQuina() == null) {
            return new Point(0, 0);
        }
        escapada.gerar();
        Rectangle boundsLocal = escapada.obterArea();
        return new Point(escapada.getPosicaoQuina().x - boundsLocal.x,
                escapada.getPosicaoQuina().y - boundsLocal.y);
    }

    /**
     * Testa se o clique atingiu algum ponto de {@link #editandoPontosEscapadaDe}
     * e, se sim, inicia o arraste correspondente, guardando a posição atual
     * em {@link #pontoEscapadaAnteriorAoArraste} para permitir reverter se o
     * ponto solto for o primeiro/último e não validar (ver
     * {@link #finalizarArrastePontoEscapada(Point)}).
     */
    private boolean tentarIniciarArrastePontoEscapada(MouseEvent e) {
        if (editandoPontosEscapadaDe == null || !SwingUtilities.isLeftMouseButton(e)) {
            return false;
        }
        Point offset = calculaOffsetTelaEscapada(editandoPontosEscapadaDe);
        List<Point> pontos = editandoPontosEscapadaDe.getPontos();
        for (int i = 0; i < pontos.size(); i++) {
            Point posTela = telaDoLocal(pontos.get(i), offset);
            if (distancia(e.getPoint(), posTela) <= RAIO_MARCADOR_EDICAO_PONTOS_PX) {
                indicePontoEscapadaArrastando = i;
                pontoEscapadaAnteriorAoArraste = pontos.get(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Se o ponto arrastado ({@link #indicePontoEscapadaArrastando}) for o
     * PRIMEIRO (entrada) ou o ÚLTIMO (saída) do encadeamento, valida
     * {@code pontoTela} contra o traçado correspondente (1 ou 2 para a
     * entrada; o mesmo {@code tracadoOrigem} da entrada para a saída) — se
     * válido, ancora nesse nó (atualizando {@code tracadoOrigem} quando é a
     * entrada que está sendo movida); se inválido, mostra o alerta e reverte
     * para {@link #pontoEscapadaAnteriorAoArraste}. Pontos intermediários
     * (trajeto livre) não passam por nenhuma validação — a posição já foi
     * atualizada ao vivo durante o arraste em {@code mouseDragged}.
     */
    private void finalizarArrastePontoEscapada(Point pontoTela) {
        List<Point> pontos = editandoPontosEscapadaDe.getPontos();
        int indice = indicePontoEscapadaArrastando;
        boolean primeiro = indice == 0;
        boolean ultimo = indice == pontos.size() - 1;
        if (!primeiro && !ultimo) {
            editandoPontosEscapadaDe.gerar();
            editandoPontosEscapadaDe.setPosicaoQuina(editandoPontosEscapadaDe.obterArea().getLocation());
            return;
        }
        No noValido = primeiro ? noMaisProximoTracado1e2(pontoTela)
                : noMaisProximoDoTracado(pontoTela, editandoPontosEscapadaDe.getTracadoOrigem());
        Point offset = calculaOffsetTelaEscapada(editandoPontosEscapadaDe);
        if (noValido == null) {
            alertaPontoEscapadaInvalido();
            pontos.set(indice, pontoEscapadaAnteriorAoArraste);
        } else {
            Point localValido = localDaTela(noValido.getPoint(), offset);
            pontos.set(indice, localValido);
            if (primeiro) {
                editandoPontosEscapadaDe.setTracadoOrigem(noValido.getTracado());
                editandoPontosEscapadaDe.setIndiceEntrada(noValido.getIndex());
            } else {
                editandoPontosEscapadaDe.setIndiceSaida(noValido.getIndex());
            }
        }
        editandoPontosEscapadaDe.gerar();
        // Resincroniza posicaoQuina com os bounds atuais de pontos (offset
        // volta a 0,0) — ver comentário equivalente em mouseDragged; sem
        // isso, editar um segundo ponto na mesma sessão de edição usaria um
        // deslocamento calculado a partir de um posicaoQuina defasado,
        // gravando a posição errada mesmo com o índice de nó correto — daí a
        // escapada "não ser reconhecida" pelo Teste Pista até recarregar o
        // circuito (que sempre revetoriza e recria os objetos do zero).
        editandoPontosEscapadaDe.setPosicaoQuina(editandoPontosEscapadaDe.obterArea().getLocation());
    }

    private void desenhaMarcadoresEdicaoPontosEscapada(Graphics2D g2d) {
        if (editandoPontosEscapadaDe == null) {
            return;
        }
        Point offset = calculaOffsetTelaEscapada(editandoPontosEscapadaDe);
        for (Point ponto : editandoPontosEscapadaDe.getPontos()) {
            desenhaMarcadorAmareloPreto(g2d, telaDoLocal(ponto, offset));
        }
    }

    private void desenhaMarcadorAmareloPreto(Graphics2D g2d, Point posTela) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(posTela.x - 5, posTela.y - 5, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(posTela.x - 5, posTela.y - 5, 10, 10);
    }

    /**
     * Aproxima {@code candidatoTela} (em espaço de tela) do nó de pista ou
     * ponto de outro objeto de cenário mais próximo, se algum estiver dentro
     * de {@link #RAIO_SNAP_GUARD_RAILS_PX}; caso contrário retorna o próprio
     * candidato. {@code ignorar} é o próprio ObjetoGuardRails em criação/edição,
     * para não se auto-atrair.
     */
    Point aplicaSnap(Point candidatoTela, ObjetoPista ignorar) {
        Point melhor = candidatoTela;
        double menorDistancia = RAIO_SNAP_GUARD_RAILS_PX;
        if (circuito.getPista() != null) {
            for (No no : circuito.getPista()) {
                double d = distancia(candidatoTela, no.getPoint());
                if (d <= menorDistancia) {
                    menorDistancia = d;
                    melhor = no.getPoint();
                }
            }
        }
        if (circuito.getBox() != null) {
            for (No no : circuito.getBox()) {
                double d = distancia(candidatoTela, no.getPoint());
                if (d <= menorDistancia) {
                    menorDistancia = d;
                    melhor = no.getPoint();
                }
            }
        }
        if (circuito.getObjetosCenario() != null) {
            for (ObjetoPista outro : circuito.getObjetosCenario()) {
                if (outro == ignorar) {
                    continue;
                }
                for (Point candidatoObjeto : pontosAbsolutosParaSnap(outro)) {
                    double d = distancia(candidatoTela, candidatoObjeto);
                    if (d <= menorDistancia) {
                        menorDistancia = d;
                        melhor = candidatoObjeto;
                    }
                }
            }
        }
        return melhor;
    }

    /** Pontos/vértices de {@code objeto} convertidos para espaço de tela, usados como candidatos de snap. */
    private List<Point> pontosAbsolutosParaSnap(ObjetoPista objeto) {
        List<Point> resultado = new ArrayList<Point>();
        if (objeto instanceof ObjetoGuardRails) {
            ObjetoGuardRails guardRails = (ObjetoGuardRails) objeto;
            Point offset = calculaOffsetTelaGuardRails(guardRails);
            for (Point ponto : guardRails.getPontos()) {
                resultado.add(telaDoLocal(ponto, offset));
            }
        } else if (objeto instanceof ObjetoLivre) {
            ObjetoLivre objetoLivre = (ObjetoLivre) objeto;
            Point offset = calculaOffsetTelaObjetoLivre(objetoLivre);
            List<PontoCurva> vertices = objetoLivre.getVertices();
            if (!vertices.isEmpty()) {
                for (PontoCurva vertice : vertices) {
                    resultado.add(telaDoLocal(vertice.getPosicao(), offset));
                }
            } else {
                for (Point ponto : objetoLivre.getPontos()) {
                    resultado.add(telaDoLocal(ponto, offset));
                }
            }
        } else if (objeto.getPosicaoQuina() != null) {
            resultado.add(objeto.getPosicaoQuina());
        }
        return resultado;
    }

    /**
     * Nó mais próximo de {@code candidatoTela} dentre os traçados 1 e 2 da
     * pista (nunca o traçado 0/central), dentro de {@link #TOLERANCIA_ESCAPADA_PX};
     * {@code null} se nenhum nó dos dois traçados estiver perto o bastante.
     * Usado para validar/ancorar o nó de ENTRADA (primeiro ponto) de um
     * {@link ObjetoEscapada}.
     */
    private No noMaisProximoTracado1e2(Point candidatoTela) {
        No melhorPista1 = noMaisProximoDoTracado(candidatoTela, 1);
        No melhorPista2 = noMaisProximoDoTracado(candidatoTela, 2);
        if (melhorPista1 == null) {
            return melhorPista2;
        }
        if (melhorPista2 == null) {
            return melhorPista1;
        }
        return distancia(candidatoTela, melhorPista1.getPoint()) <= distancia(candidatoTela, melhorPista2.getPoint())
                ? melhorPista1 : melhorPista2;
    }

    /**
     * Nó mais próximo de {@code candidatoTela} dentro do traçado
     * {@code tracado} (1 ou 2), dentro de {@link #TOLERANCIA_ESCAPADA_PX};
     * {@code null} se nenhum nó desse traçado estiver perto o bastante. Usado
     * também para validar/ancorar o nó de SAÍDA (último ponto, clique
     * direito) de um {@link ObjetoEscapada}, restrito ao mesmo traçado da
     * entrada.
     */
    private No noMaisProximoDoTracado(Point candidatoTela, int tracado) {
        List<No> lista = tracado == 2 ? circuito.getPista2Full() : circuito.getPista1Full();
        if (lista == null) {
            return null;
        }
        No melhor = null;
        double menorDistancia = TOLERANCIA_ESCAPADA_PX;
        for (No no : lista) {
            double d = distancia(candidatoTela, no.getPoint());
            if (d <= menorDistancia) {
                menorDistancia = d;
                melhor = no;
            }
        }
        return melhor;
    }

    /**
     * Alerta padrão (mesmo estilo de diálogo já usado para outras validações
     * do editor) quando um clique não atinge um ponto de escapada válido.
     * Protected (em vez de private) para que testes possam sobrescrever com
     * um double que não abre diálogo Swing de verdade — ver
     * MainPanelEditorTestDouble.
     */
    protected void alertaPontoEscapadaInvalido() {
        JOptionPane.showMessageDialog(this, Lang.msg("pontoEscapadaInvalido"), Lang.msg("039"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * true se existir ao menos uma ObjetoEscapada sem ponto de saída
     * ancorado (indiceSaida == -1) — validação usada por salvarPista() para
     * bloquear o salvamento de uma escapada inacabada. Também conta uma
     * escapada sendo desenhada no momento (clique de entrada já dado, ainda
     * sem o clique direito que finaliza): esse objeto só entra em
     * circuito.getObjetos() ao ser finalizado, então sem essa checagem
     * salvarPista() não a via e descartava silenciosamente o desenho em
     * andamento (bug relatado).
     */
    private boolean existeEscapadaIncompleta() {
        if (desenhandoObjetoLivre && objetoPista instanceof ObjetoEscapada) {
            return true;
        }
        if (circuito.getObjetos() == null) {
            return false;
        }
        for (ObjetoPista objeto : circuito.getObjetos()) {
            if (objeto instanceof ObjetoEscapada && ((ObjetoEscapada) objeto).getIndiceSaida() == -1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Alerta (mesmo padrão de alertaPontoEscapadaInvalido()) quando
     * salvarPista() encontra uma ObjetoEscapada incompleta. Protected para
     * permitir override em MainPanelEditorTestDouble.
     */
    protected void alertaEscapadaIncompleta() {
        JOptionPane.showMessageDialog(this, Lang.msg("escapadaIncompleta"), Lang.msg("039"),
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Lê o campo de distância em quilômetros sem lançar
     * NumberFormatException — texto vazio ou não numérico vira 0, que
     * salvarPista() trata como "não informado".
     */
    private int parseDistanciaKmOuZero(String texto) {
        String valor = texto == null ? "" : texto.trim();
        if (valor.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /**
     * Alerta (mesmo padrão de alertaPontoEscapadaInvalido()) quando
     * salvarPista() encontra a distância do circuito não informada (campo
     * vazio ou valor <= 0). Protected para permitir override em
     * MainPanelEditorTestDouble.
     */
    protected void alertaDistanciaNaoInformada() {
        JOptionPane.showMessageDialog(this, Lang.msg("distanciaCircuitoNaoInformada"), Lang.msg("039"),
                JOptionPane.ERROR_MESSAGE);
    }

    private static void limpaMouseListeners(JLabel... labels) {
        for (JLabel label : labels) {
            for (java.awt.event.MouseListener listener : label.getMouseListeners()) {
                label.removeMouseListener(listener);
            }
        }
    }

    private static JLabel criaIndicadorDeCor() {
        JLabel label = new JLabel() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, 20);
            }
        };
        label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return label;
    }

    private void atualizaCorLabel(JLabel label, Color cor, Color corPadrao) {
        Color corExibida = cor != null ? cor : corPadrao;
        label.setOpaque(true);
        label.setBackground(corExibida);
        int luminancia = (corExibida.getRed() + corExibida.getGreen() + corExibida.getBlue()) / 3;
        label.setForeground(luminancia > 128 ? Color.BLACK : Color.WHITE);
        label.repaint();
    }

    protected void editaObjetoPista(Point point) {
        ObjetoPista encontrado = encontraObjetoPista(point);
        if (encontrado != null) {
            FormularioObjetos formularioObjetos = new FormularioObjetos(MainPanelEditor.this);
            formularioObjetos.objetoLivreFormulario(encontrado);
        }
    }

    /**
     * Busca, nas duas listas de objetos do circuito (objetos e
     * objetosCenario), o primeiro objeto cuja área contenha o ponto
     * informado. Usado por edição por duplo-clique, menu de contexto e
     * início de arraste.
     */
    ObjetoPista encontraObjetoPista(Point point) {
        ObjetoPista encontrado = encontraObjetoPistaNaLista(circuito.getObjetos(), point);
        if (encontrado != null) {
            return encontrado;
        }
        return encontraObjetoPistaNaLista(circuito.getObjetosCenario(), point);
    }

    /**
     * Percorre a lista de trás pra frente: objetos mais recentes (adicionados
     * por último) são desenhados por cima dos mais antigos quando as áreas se
     * sobrepõem, então o clique deve priorizá-los também — sem isso, um
     * objeto grande antigo "engolia" o clique em objetos menores e mais
     * novos sobrepostos por ele.
     */
    private ObjetoPista encontraObjetoPistaNaLista(List<ObjetoPista> lista, Point point) {
        if (lista == null) {
            return null;
        }
        for (int i = lista.size() - 1; i >= 0; i--) {
            ObjetoPista objetoPista = lista.get(i);
            if (objetoPista.obterAreaClique().contains(point)) {
                return objetoPista;
            }
        }
        return null;
    }

    /**
     * Concatena objetos e objetosCenario numa única lista (nunca nula), para
     * os loops de desenho do editor, que precisam considerar as duas listas.
     */
    private List<ObjetoPista> todosObjetos() {
        List<ObjetoPista> todos = new ArrayList<ObjetoPista>();
        if (circuito.getObjetos() != null) {
            todos.addAll(circuito.getObjetos());
        }
        if (circuito.getObjetosCenario() != null) {
            todos.addAll(circuito.getObjetosCenario());
        }
        return todos;
    }

    /**
     * Níveis de desenho distintos em uso pelos objetos do circuito, em ordem
     * crescente — não há limite de faixa, então o desenho por nível percorre
     * exatamente os níveis que existem, não um intervalo fixo.
     */
    private List<Integer> niveisDesenhoOrdenados() {
        java.util.TreeSet<Integer> niveis = new java.util.TreeSet<Integer>();
        for (ObjetoPista objetoPista : todosObjetos()) {
            niveis.add(objetoPista.getNivelDesenho());
        }
        return new ArrayList<Integer>(niveis);
    }

    private void inserirNoNasJList(No no) {
        if (isTipoNoAtualBox()) {
            DefaultListModel model = ((DefaultListModel) boxJList.getModel());
            if (ultimoItemBoxSelecionado > -1) {
                ultimoItemBoxSelecionado += 1;

                if (circuito.getBox().size() > ultimoItemBoxSelecionado) {
                    circuito.getBox().add(ultimoItemBoxSelecionado, no);
                    model.add(ultimoItemBoxSelecionado, no);
                } else {
                    circuito.getBox().add(no);
                    model.addElement(no);
                    ultimoItemBoxSelecionado = model.size() - 1;
                }

                boxJList.setSelectedIndex(ultimoItemBoxSelecionado);
            } else {
                circuito.getBox().add(no);
                model.addElement(no);
                ultimoItemBoxSelecionado = model.size() - 1;
                boxJList.setSelectedIndex(ultimoItemBoxSelecionado);
            }
        } else {
            if (no.isBox()) {
                JOptionPane.showMessageDialog(this, Lang.msg("038"), Lang.msg("039"),
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DefaultListModel model = ((DefaultListModel) pistaJList.getModel());

            if (ultimoItemPistaSelecionado > -1) {
                ultimoItemPistaSelecionado += 1;

                if (circuito.getPista().size() > ultimoItemPistaSelecionado) {
                    circuito.getPista().add(ultimoItemPistaSelecionado, no);
                    model.add(ultimoItemPistaSelecionado, no);
                } else {
                    circuito.getPista().add(no);
                    model.addElement(no);
                    ultimoItemPistaSelecionado = model.size() - 1;
                }
                pistaJList.setSelectedIndex(ultimoItemPistaSelecionado);
            } else {
                circuito.getPista().add(no);
                model.addElement(no);
                ultimoItemPistaSelecionado = model.size() - 1;
                pistaJList.setSelectedIndex(ultimoItemPistaSelecionado);

            }

        }
        vetorizarCircuito();
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        Util.setarHints(g2d);

        // 1. Cor de fundo
        g2d.setColor(circuito.getCorFundo() != null ? circuito.getCorFundo() : getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // 2. Imagem de referência (*_mro.jpg), opaca, por cima da cor de fundo.
        // Botão "Desenho Background" (do lado de "Desenha Tracado") alterna
        // mostraBG entre visível e escondida.
        if (mostraBG && backGround != null) {
            g2d.drawImage(backGround, 0, 0, null);
        }

        if (larguraPistaPixeis == 0)
            larguraPistaPixeis = Util.inteiro(Carro.LARGURA * 1.5 * multiplicadorLarguraPista * zoom);

        // 3. Objetos em níveis negativos ficam abaixo da pista (nível 0), do
        // mais negativo (mais no fundo) para o mais próximo de zero.
        List<Integer> niveis = niveisDesenhoOrdenados();
        for (Integer nivel : niveis) {
            if (nivel < 0) {
                desenhaObjetosNivel(g2d, nivel);
            }
        }

        // 4. Sequência de desenho existente
        if (desenhaTracado) {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, zoom);
            desenhaZonaFrenagemOverlay(g2d);
        }
        desenhaCarroTeste(g2d);
        desenhaEntradaParadaSaidaBox(g2d);
        DesenhoProceduralCircuito.desenhaLinhaDeLargada(g2d, circuito, zoom);
        desenhaGrid(g2d);
        desenhaBoxes(g2d);
        desenhaObjetosNivel(g2d, 0);
        desenhaPreObjetoLivre(g2d);
        desenhaPreObjetoGuardRails(g2d);
        desenhaPreObjetoEscapada(g2d);
        desenhaPreObjetoTransparencia(g2d);
        // Níveis positivos por cima, do menor para o maior (mais em cima).
        for (Integer nivel : niveis) {
            if (nivel > 0) {
                desenhaObjetosNivel(g2d, nivel);
            }
        }
        desenhaMarcadoresEdicaoPontos(g2d);
        desenhaMarcadoresEdicaoPontosGuardRails(g2d);
        desenhaMarcadoresEdicaoPontosEscapada(g2d);
        desenhaObjetoSelecionadoNoCanvas(g2d);
        desenhaListaObjetos(g2d);
        desenhaPainelClassico(g2d);
        desenhaInfo(g2d);
        desenhaControles(g2d);
    }

    private void desenhaControles(Graphics2D g2d) {
        Rectangle limitesViewPort = (Rectangle) limitesViewPort();
        int x = limitesViewPort.getBounds().x + limitesViewPort.width - 200;
        int y = limitesViewPort.getBounds().y + 20;
        g2d.setColor(PainelCircuito.lightWhiteRain);
        g2d.fillRoundRect(x - 15, y - 15, 200, 260, 15, 15);
        g2d.setColor(Color.black);
        g2d.drawString("Alt ativado " + (srcFrame.isAltApertado() ? "SIM" : "NÃO"), x, y);
        String esquera = "Move tela Esquerda";
        String direita = "Move tela Direita";
        String baixo = "Move tela Baixo";
        String cima = "Move tela Cima";
        if (srcFrame.isAltApertado()) {
            esquera = "Move objeto Esquerda";
            direita = "Move objeto Direita";
            baixo = "Move objeto Baixo";
            cima = "Move objeto Cima";
        }
        if (srcFrame.isShiftApertado()) {
            esquera = "Objeto mais extreito";
            direita = "Objeto mais largo";
            baixo = "Objeto mais Baixo";
            cima = "Objeto mais alto";
        }
        y += 20;
        // Esquerda
        g2d.drawString("\u2190 " + esquera, x, y);
        y += 20;
        // Baixo
        g2d.drawString("\u2193 " + baixo, x, y);
        y += 20;
        // Direira
        g2d.drawString("\u2192 " + direita, x, y);
        y += 20;
        // Cima
        g2d.drawString("\u2191 " + cima, x, y);
        y += 20;

        g2d.drawString("Z Mais Angulo", x, y);
        y += 20;
        g2d.drawString("X Menos Angulo", x, y);
        y += 20;
        g2d.drawString("Alt+C Copiar Objeto", x, y);
        y += 20;
        g2d.drawString(Lang.msg("atalhoInserir"), x, y);
        y += 20;
        g2d.drawString(Lang.msg("atalhoApagar"), x, y);
        y += 20;
        g2d.drawString(Lang.msg("atalhoSobeNivel"), x, y);
        y += 20;
        g2d.drawString(Lang.msg("atalhoDesceNivel"), x, y);
    }

    /**
     * Contorno ao redor do objeto atualmente "ativo" (o mesmo que as teclas
     * Z/X, setas e Shift+setas manipulam) — indica visualmente qual objeto
     * responde aos atalhos de teclado mesmo quando selecionado por clique
     * direto no canvas, não só pela lista lateral (que já tem seu próprio
     * indicador em {@link #desenhaListaObjetos}).
     */
    private void desenhaObjetoSelecionadoNoCanvas(Graphics2D g2d) {
        ObjetoPista ativo = objetoPista;
        if (ativo == null || ativo.getPosicaoQuina() == null) {
            return;
        }
        Rectangle area = ativo.obterArea();
        if (area == null) {
            return;
        }
        Stroke strokeAnterior = g2d.getStroke();
        Color corAnterior = g2d.getColor();
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(2f));
        if (ativo.getAngulo() == 0) {
            g2d.drawRect(area.x, area.y, area.width, area.height);
        } else {
            double rad = Math.toRadians(ativo.getAngulo());
            AffineTransform rotacao = AffineTransform.getRotateInstance(rad, area.getCenterX(), area.getCenterY());
            g2d.draw(rotacao.createTransformedShape(area));
        }
        g2d.setStroke(strokeAnterior);
        g2d.setColor(corAnterior);
    }

    private void desenhaListaObjetos(Graphics2D g2d) {
        ObjetoPista objetoPista = primeiroSelecionado(formularioListaObjetosDesenho);
        if (objetoPista == null) {
            objetoPista = primeiroSelecionado(formularioListaObjetosFuncao);
        }
        if (objetoPista == null) {
            return;
        }
        g2d.setColor(PainelCircuito.lightWhiteRain);
        Point loc = objetoPista.obterArea().getLocation();
        loc = new Point((int) (loc.x * zoom), (int) (loc.y * zoom));
        g2d.fillRect(loc.x, loc.y, 22, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawString(objetoPista.getNome().split(" ")[1], loc.x, loc.y + 10);
        if (objetoPista.getPosicaoQuina() != null) {
            g2d.setColor(Color.ORANGE);
            g2d.drawRect(objetoPista.getPosicaoQuina().x, objetoPista.getPosicaoQuina().y,
                    objetoPista.getLargura(), objetoPista.getAltura());
        }
    }

    private void desenhaInfo(Graphics2D g2d) {

        Rectangle limitesViewPort = (Rectangle) limitesViewPort();
        int x = limitesViewPort.getBounds().x + 30;
        int y = limitesViewPort.getBounds().y + 20;
        g2d.setColor(PainelCircuito.lightWhiteRain);
        g2d.fillRoundRect(x - 15, y - 15, 200, 200, 15, 15);
        g2d.setColor(Color.black);
        g2d.drawString("Zoom : " + zoom, x, y);
        y += 20;
        g2d.drawString("Multi Pista : " + multiplicadorPista, x, y);
        y += 20;
        g2d.drawString("Multi Largura Pista : " + PainelCircuito.df4.format(multiplicadorLarguraPista), x, y);
        y += 20;
        g2d.drawString("Box : " + testePista.isIrProBox(), x, y);
        if (circuito.getObjetos() != null || circuito.getObjetosCenario() != null) {
            y += 20;
            g2d.drawString("Num Objetos : " + todosObjetos().size(), x, y);
        }
        int noAlta = 0;
        int noMedia = 0;
        int noBaixa = 0;
        List list = circuito.geraPontosPista();
        for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
            No no = (No) iterator.next();
            if (no.verificaRetaOuLargada()) {
                noAlta++;
            }
            if (no.verificaCurvaAlta()) {
                noMedia++;
            }
            if (no.verificaCurvaBaixa()) {
                noBaixa++;
            }
        }
        double total = noAlta + noMedia + noBaixa;
        y += 20;
        g2d.drawString("Alta" + ":" + noAlta + " " + (int) (100 * noAlta / total) + "%", x, y);
        y += 20;
        g2d.drawString("Média" + ":" + noMedia + " " + (int) (100 * noMedia / total) + "%", x, y);
        y += 20;
        g2d.drawString("Baixa" + ":" + noBaixa + " " + (int) (100 * noBaixa / total) + "%", x, y);
        y += 20;
        g2d.setColor(Color.CYAN);
        g2d.drawLine(x + 50, y, x + 100, y);
        g2d.setColor(Color.black);
        g2d.drawString("Pista 1 ", x, y);
        y += 20;
        g2d.setColor(Color.MAGENTA);
        g2d.drawLine(x + 50, y, x + 100, y);
        g2d.setColor(Color.black);
        g2d.drawString("Pista 2 ", x, y);
    }

    /**
     * O checkbox "Objetos" só afeta objetos de desenho (Livre, Arquibancada,
     * Construcao, GuardRails, Pneus); Escapada e Transparencia são objetos de
     * função e continuam sempre desenhados, independente do checkbox.
     */
    private void desenhaObjetosNivel(Graphics2D g2d, int nivel) {
        if (circuito == null) {
            return;
        }
        for (ObjetoPista objetoPista : todosObjetos()) {
            if (objetoPista.getNivelDesenho() != nivel)
                continue;
            boolean objetoDeFuncao = objetoPista instanceof ObjetoEscapada || objetoPista instanceof ObjetoTransparencia;
            if (!objetoDeFuncao && !desenhaObjetosDesenho) {
                continue;
            }
            if (!estaVisivelNoViewport(objetoPista)) {
                continue;
            }
            objetoPista.desenha(g2d, zoom);
        }
    }

    /**
     * Testa se {@code objetoPista} pode estar visível no retângulo atual de
     * {@link #limitesViewPort()}, para pular {@code desenha()} de objetos
     * claramente fora de tela. Usa {@code obterArea()} diretamente quando o
     * objeto não está rotacionado; quando {@code angulo != 0}, usa um
     * retângulo expandido ao raio do círculo circunscrito de
     * largura/altura (centrado no centro de {@code obterArea()}) em vez de
     * rotacionar a área via {@code AffineTransform} a cada frame — mais
     * barato, e nunca sub-estima a área ocupada pela forma rotacionada.
     * {@code scrollPane} pode não estar montado ainda (ex.: testes que
     * instanciam {@code MainPanelEditor} sem a janela completa) — nesse
     * caso não há viewport pra testar, então não corta nada.
     */
    private boolean estaVisivelNoViewport(ObjetoPista objetoPista) {
        if (scrollPane == null) {
            return true;
        }
        Rectangle area = objetoPista.obterArea();
        if (area == null || area.width <= 0 || area.height <= 0) {
            // obterArea() de alguns tipos (ex.: ObjetoArquibancada,
            // ObjetoConstrucao) só reflete o desenho real depois da primeira
            // chamada a desenha() — antes disso vem vazia/zerada. Sem saber
            // a área real, não corta (mais seguro desenhar de mais uma vez
            // do que esconder um objeto que deveria aparecer); a partir do
            // próximo frame, já com a área populada, o corte volta a valer.
            return true;
        }
        Rectangle retanguloTeste = area;
        if (objetoPista.getAngulo() != 0) {
            double raio = Math.hypot(area.width, area.height) / 2.0;
            retanguloTeste = new Rectangle((int) Math.floor(area.getCenterX() - raio),
                    (int) Math.floor(area.getCenterY() - raio), (int) Math.ceil(raio * 2), (int) Math.ceil(raio * 2));
        }
        return retanguloTeste.intersects(((Rectangle) limitesViewPort()));
    }

    private void desenhaPreObjetoTransparencia(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        if (objetoPista == null || !desenhandoObjetoLivre || !(objetoPista instanceof ObjetoTransparencia))
            return;
        ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
        if (objetoTransparencia.getPontos().size() == 1) {
            return;
        }
        Point ant = null;
        for (Point p : objetoTransparencia.getPontos()) {
            if (ant != null) {
                g2d.drawLine(Util.inteiro(ant.x * zoom), Util.inteiro(ant.y * zoom), Util.inteiro(p.x * zoom),
                        Util.inteiro(p.y * zoom));
            }
            ant = p;
        }

    }

    private void desenhaPreObjetoLivre(Graphics2D g2d) {
        if (objetoPista == null || !desenhandoObjetoLivre || !(objetoPista instanceof ObjetoLivre)) {
            return;
        }
        ObjetoLivre objetoLivre = (ObjetoLivre) objetoPista;
        Stroke strokeAnterior = g2d.getStroke();
        Color corAnterior = g2d.getColor();
        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3f));
        int raioPonto = 6;
        Point ant = null;
        for (Point p : objetoLivre.getPontos()) {
            int px = Util.inteiro(p.x * zoom);
            int py = Util.inteiro(p.y * zoom);
            if (ant != null) {
                g2d.drawLine(Util.inteiro(ant.x * zoom), Util.inteiro(ant.y * zoom), px, py);
            }
            g2d.fillOval(px - raioPonto, py - raioPonto, raioPonto * 2, raioPonto * 2);
            ant = p;
        }
        g2d.setStroke(strokeAnterior);
        g2d.setColor(corAnterior);
    }

    private void desenhaPreObjetoGuardRails(Graphics2D g2d) {
        if (objetoPista == null || !desenhandoObjetoLivre || !(objetoPista instanceof ObjetoGuardRails)) {
            return;
        }
        ObjetoGuardRails guardRails = (ObjetoGuardRails) objetoPista;
        Stroke strokeAnterior = g2d.getStroke();
        Color corAnterior = g2d.getColor();
        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3f));
        int raioPonto = 6;
        Point ant = null;
        for (Point p : guardRails.getPontos()) {
            int px = Util.inteiro(p.x * zoom);
            int py = Util.inteiro(p.y * zoom);
            if (ant != null) {
                g2d.drawLine(Util.inteiro(ant.x * zoom), Util.inteiro(ant.y * zoom), px, py);
            }
            g2d.fillOval(px - raioPonto, py - raioPonto, raioPonto * 2, raioPonto * 2);
            ant = p;
        }
        g2d.setStroke(strokeAnterior);
        g2d.setColor(corAnterior);
    }

    /** Preview do ObjetoEscapada em criação: encadeamento de pontos já clicados (entrada + trajeto livre até agora), igual ao de GuardRails. */
    private void desenhaPreObjetoEscapada(Graphics2D g2d) {
        if (objetoPista == null || !desenhandoObjetoLivre || !(objetoPista instanceof ObjetoEscapada)) {
            return;
        }
        ObjetoEscapada escapada = (ObjetoEscapada) objetoPista;
        Stroke strokeAnterior = g2d.getStroke();
        Color corAnterior = g2d.getColor();
        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3f));
        int raioPonto = 6;
        Point ant = null;
        for (Point p : escapada.getPontos()) {
            int px = Util.inteiro(p.x * zoom);
            int py = Util.inteiro(p.y * zoom);
            if (ant != null) {
                g2d.drawLine(Util.inteiro(ant.x * zoom), Util.inteiro(ant.y * zoom), px, py);
            }
            g2d.fillOval(px - raioPonto, py - raioPonto, raioPonto * 2, raioPonto * 2);
            ant = p;
        }
        g2d.setStroke(strokeAnterior);
        g2d.setColor(corAnterior);
    }

    private void desenhaBoxes(Graphics2D g2d) {
        // modoEditor=true: preview do editor mostra as bolinhas de lado do
        // box; a imagem final da corrida (DesenhoProceduralCircuito.desenha)
        // não deve mostrar, ver javadoc de desenhaVagasBox.
        DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, zoom, true);
    }

    private void desenhaGrid(Graphics2D g2d) {
        if (circuito.getPistaFull() == null || circuito.getPistaFull().isEmpty()) {
            return;
        }
        for (int i = 0; i < 24; i++) {
            int iP = 50 + Util.inteiro(((Carro.LARGURA) * 0.8) * i);
            int index1 = circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA;
            if (index1 < 0) {
                return;
            }
            No n1 = (No) circuito.getPistaFull().get(index1);
            No nM = (No) circuito.getPistaFull().get(circuito.getPistaFull().size() - iP);
            No n2 = (No) circuito.getPistaFull().get(circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
            Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom), Util.inteiro(n1.getPoint().y * zoom));
            Point pm = new Point(Util.inteiro(nM.getPoint().x * zoom), Util.inteiro(nM.getPoint().y * zoom));
            Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom), Util.inteiro(n2.getPoint().y * zoom));
            double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
            Rectangle2D rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
                    (Carro.LARGURA), (Carro.ALTURA));

            Point cima = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * 1.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro(Carro.ALTURA * 1.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            if (i % 2 == 0) {
                rectangle = new Rectangle2D.Double((cima.x - (Carro.MEIA_LARGURA * zoom)),
                        (cima.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
            } else {
                rectangle = new Rectangle2D.Double((baixo.x - (Carro.MEIA_LARGURA * zoom)),
                        (baixo.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
            }

            GeneralPath generalPath = new GeneralPath(rectangle);

            AffineTransform affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
            double rad = Math.toRadians((double) calculaAngulo);
            affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fill(generalPath.createTransformedShape(affineTransformRect));

            iP += 5;
            n1 = (No) circuito.getPistaFull().get(circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
            nM = (No) circuito.getPistaFull().get(circuito.getPistaFull().size() - iP);
            n2 = (No) circuito.getPistaFull().get(circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
            p1 = new Point(Util.inteiro(n1.getPoint().x * zoom), Util.inteiro(n1.getPoint().y * zoom));
            pm = new Point(Util.inteiro(nM.getPoint().x * zoom), Util.inteiro(nM.getPoint().y * zoom));
            p2 = new Point(Util.inteiro(n2.getPoint().x * zoom), Util.inteiro(n2.getPoint().y * zoom));
            calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
            rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
                    (Carro.LARGURA), (Carro.ALTURA));

            cima = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * 1.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro(Carro.ALTURA * 1.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            if (i % 2 == 0) {
                rectangle = new Rectangle2D.Double((cima.x - (Carro.MEIA_LARGURA * zoom)),
                        (cima.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
            } else {
                rectangle = new Rectangle2D.Double((baixo.x - (Carro.MEIA_LARGURA * zoom)),
                        (baixo.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
            }

            generalPath = new GeneralPath(rectangle);

            affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
            rad = Math.toRadians((double) calculaAngulo);
            affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
            g2d.setColor(new Color(192, 192, 192, 150));
        }

    }

    private void desenhaEntradaParadaSaidaBox(Graphics2D g2d) {
        if (circuito.getPistaFull() == null || circuito.getPistaFull().isEmpty()) {
            return;
        }
        if (circuito.getBoxFull() == null || circuito.getBoxFull().isEmpty()) {
            return;
        }
        Point e = ((No) circuito.getPistaFull().get(circuito.getEntradaBoxIndex())).getPoint();
        Point p = ((No) circuito.getBoxFull().get(circuito.getParadaBoxIndex())).getPoint();
        Point s = ((No) circuito.getPistaFull().get(circuito.getSaidaBoxIndex())).getPoint();
        g2d.setColor(Color.BLACK);
        g2d.fillOval(Util.inteiro(e.x * zoom), Util.inteiro(e.y * zoom), Util.inteiro(5 * zoom),
                Util.inteiro(5 * zoom));
        g2d.fillOval(Util.inteiro(p.x * zoom), Util.inteiro(p.y * zoom), Util.inteiro(5 * zoom),
                Util.inteiro(5 * zoom));

        g2d.fillOval(Util.inteiro(s.x * zoom), Util.inteiro(s.y * zoom), Util.inteiro(5 * zoom),
                Util.inteiro(5 * zoom));

    }

    /**
     * Nós que pertencem a uma zona de frenagem detectada no circuito atual —
     * reaproveita {@link ControleRecursos#calculaZonaFrenagem(java.util.List)}
     * (a mesma detecção usada pelo motor de jogo), cacheado pela referência
     * da própria lista {@code circuito.getPistaFull()} (não pela referência
     * de {@link #circuito}): {@code circuito.vetorizarPista(...)} substitui
     * essa lista por uma nova, com instâncias de {@code No} novas, sem
     * trocar a referência de {@code circuito} — cachear pelo circuito
     * deixaria o cache "grudado" nos nós antigos (que não batem mais por
     * identidade com os nós de uma revetorização posterior).
     */
    private java.util.Set<No> obterZonaFrenagemNos() {
        List<No> pista = circuito.getPistaFull();
        if (pista != zonaFrenagemCalculadaPara) {
            zonaFrenagemNos = (pista == null || pista.isEmpty())
                    ? java.util.Collections.<No>emptySet()
                    : ControleRecursos.calculaZonaFrenagem(pista).keySet();
            zonaFrenagemCalculadaPara = pista;
        }
        return zonaFrenagemNos;
    }

    /**
     * Marcação visual (acinzentada/translúcida) sobre os nós de pista que
     * pertencem a uma zona de frenagem detectada — sempre visível quando o
     * traçado está sendo mostrado, independente do checkbox "Faísca e
     * Marcas de Pneu". Desenhada numa chamada própria do editor, separada
     * de {@code DesenhoProceduralCircuito.desenhaPistaZebraEBox} (que é
     * reaproveitada pela geração de imagem em memória usada em corrida
     * real), pra essa marcação nunca vazar pra dentro da imagem de corrida.
     */
    private void desenhaZonaFrenagemOverlay(Graphics2D g2d) {
        java.util.Set<No> zona = obterZonaFrenagemNos();
        if (zona.isEmpty()) {
            return;
        }
        g2d.setColor(new Color(120, 120, 120, 110));
        double multiplicador = multiplicadorLarguraPista > 0 ? multiplicadorLarguraPista : 1.0;
        int raio = Math.max(3, Util.inteiro(Carro.LARGURA * multiplicador * zoom / 2.0));
        for (No no : zona) {
            Point p = no.getPoint();
            g2d.fillOval(Util.inteiro(p.x * zoom) - raio, Util.inteiro(p.y * zoom) - raio, raio * 2, raio * 2);
        }
    }

    private void desenhaCarroTeste(Graphics2D g2d) {
        g2d.setColor(Color.black);
        g2d.setStroke(trilho);
        if (testePista != null && testePista.getTestCar() != null) {

            int width = (int) (carroCima.getWidth());
            int height = (int) (carroCima.getHeight());
            int w2 = width / 2;
            int h2 = height / 2;
            int carx = testePista.getTestCar().x - w2;
            int cary = testePista.getTestCar().y - h2;

            double calculaAngulo = GeoUtil.calculaAngulo(testePista.frenteCar, testePista.trazCar, 0);
            Rectangle2D rectangle = new Rectangle2D.Double((testePista.getTestCar().x - Carro.MEIA_LARGURA),
                    (testePista.getTestCar().y - Carro.MEIA_ALTURA), Carro.LARGURA, Carro.ALTURA);
            Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro(Carro.ALTURA * multiplicadorLarguraPista),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            g2d.setColor(Color.black);
            Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro(Carro.ALTURA * multiplicadorLarguraPista),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));

            if (pos == 0) {
                carx = testePista.getTestCar().x - w2;
                cary = testePista.getTestCar().y - h2;
            }
            if (pos == 1) {
                carx = Util.inteiro((p1.x - w2));
                cary = Util.inteiro((p1.y - h2));
            }
            if (pos == 2) {
                carx = Util.inteiro((p2.x - w2));
                cary = Util.inteiro((p2.y - h2));
            }

            double rad = Math.toRadians((double) calculaAngulo);
            AffineTransform afZoom = new AffineTransform();
            AffineTransform afRotate = new AffineTransform();
            afZoom.setToScale(zoom, zoom);
            afRotate.setToRotation(rad, carroCima.getWidth() / 2, carroCima.getHeight() / 2);

            BufferedImage rotateBuffer = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
            BufferedImage zoomBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            AffineTransformOp op = new AffineTransformOp(afRotate, AffineTransformOp.TYPE_BILINEAR);
            op.filter(carroCima, zoomBuffer);
            AffineTransformOp op2 = new AffineTransformOp(afZoom, AffineTransformOp.TYPE_BILINEAR);
            op2.filter(zoomBuffer, rotateBuffer);

            if (circuito.getObjetos() != null) {
                for (ObjetoPista objetoPista : circuito.getObjetos()) {
                    if (!(objetoPista instanceof ObjetoTransparencia))
                        continue;
                    ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
                    Rectangle obterArea = objetoTransparencia.obterArea();
                    Graphics2D gImage = rotateBuffer.createGraphics();
                    objetoTransparencia.desenhaCarro(gImage, zoom, carx, cary);
                }
            }

            g2d.drawImage(rotateBuffer, Util.inteiro(carx * zoom), Util.inteiro(cary * zoom), null);

            AffineTransform affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
            affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
            g2d.setColor(new Color(255, 0, 0, 140));

            g2d.fillOval(Util.inteiro(testePista.frenteCar.x * zoom), Util.inteiro(testePista.frenteCar.y * zoom),
                    Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
            g2d.fillOval(Util.inteiro(testePista.trazCar.x * zoom), Util.inteiro(testePista.trazCar.y * zoom),
                    Util.inteiro(5 * zoom), Util.inteiro(5 * zoom));
        }
    }

    public Shape limitesViewPort() {
        Rectangle rectangle = scrollPane.getViewport().getBounds();
        // rectangle.width += 50;
        // rectangle.height += 50;
        rectangle.x = scrollPane.getViewport().getViewPosition().x;
        rectangle.y = scrollPane.getViewport().getViewPosition().y;
        return rectangle;
    }

    private void desenhaPainelClassico(Graphics g2d) {
        if (!desenhaTracado) {
            return;
        }

        No oldNo = null;
        int count = 0;
        int conNoPista = 0;
        for (int i = 0; i < circuito.getPista().size(); i++) {
            No no = (No) circuito.getPista().get(i);
            g2d.drawImage(no.getBufferedImage(), no.getDrawX(), no.getDrawY(), null);
            String num = " " + conNoPista + " (" + count + ")";
            int larguraNum = Util.larguraTexto(num, (Graphics2D) g2d);
            int qX = no.getDrawX() + 10;
            int qY = no.getDrawY() - 10;
            g2d.setColor(PainelCircuito.transpMenus);
            g2d.fillRoundRect(qX, qY, larguraNum, 15, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawString(num, qX, qY + 12);
            conNoPista++;
            if (oldNo != null) {
                g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(), no.getY());
            }
            oldNo = no;

            if (i + 1 < circuito.getPista().size()) {
                No newNo = (No) circuito.getPista().get(i + 1);
                count += GeoUtil.drawBresenhamLine(newNo.getX(), newNo.getY(), no.getX(), no.getY()).size();
            }

            if (pistaJList != null && pistaJList.getSelectedValue() == no) {
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6, 6, 2, 2);
                g2d.setColor(Color.black);
            }
        }
        No oldNo1 = null;
        for (int i = 0; i < circuito.getPista1Full().size(); i += 10) {
            No no = (No) circuito.getPista1Full().get(i);
            g2d.setColor(Color.CYAN);
            conNoPista++;
            if (oldNo1 != null) {
                g2d.drawLine(oldNo1.getX(), oldNo1.getY(), no.getX(), no.getY());
            }
            oldNo1 = no;
        }
        oldNo1 = null;

        for (int i = 0; i < circuito.getBox1Full().size(); i += 10) {
            No no = (No) circuito.getBox1Full().get(i);
            g2d.setColor(Color.CYAN);
            conNoPista++;
            if (oldNo1 != null) {
                g2d.drawLine(oldNo1.getX(), oldNo1.getY(), no.getX(), no.getY());
            }
            oldNo1 = no;
        }

        No oldNo2 = null;
        for (int i = 0; i < circuito.getPista2Full().size(); i += 10) {
            No no = (No) circuito.getPista2Full().get(i);
            g2d.setColor(Color.MAGENTA);
            conNoPista++;
            if (oldNo2 != null) {
                g2d.drawLine(oldNo2.getX(), oldNo2.getY(), no.getX(), no.getY());
            }
            oldNo2 = no;

        }
        oldNo2 = null;

        for (int i = 0; i < circuito.getBox2Full().size(); i += 10) {
            No no = (No) circuito.getBox2Full().get(i);
            g2d.setColor(Color.MAGENTA);
            conNoPista++;
            if (oldNo2 != null) {
                g2d.drawLine(oldNo2.getX(), oldNo2.getY(), no.getX(), no.getY());
            }
            oldNo2 = no;

        }

        oldNo = null;
        count = 0;
        for (int i = 0; i < circuito.getBox().size(); i++) {
            No no = (No) circuito.getBox().get(i);
            g2d.drawImage(no.getBufferedImage(), no.getDrawX(), no.getDrawY(), null);
            int qX = no.getDrawX() + 10;
            int qY = no.getDrawY() + 10;
            String num = " " + conNoPista + " (" + count + ")";
            int larguraNum = Util.larguraTexto(num, (Graphics2D) g2d);
            g2d.setColor(PainelCircuito.gre);
            g2d.fillRoundRect(qX, qY, larguraNum, 15, 5, 5);
            g2d.setColor(Color.BLACK);
            g2d.drawString(num, qX, qY + 12);
            conNoPista++;
            if (oldNo != null) {
                g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(), no.getY());
            }
            oldNo = no;

            if (i + 1 < circuito.getBox().size()) {
                No newNo = (No) circuito.getBox().get(i + 1);
                count += GeoUtil.drawBresenhamLine(newNo.getX(), newNo.getY(), no.getX(), no.getY()).size();
            }

            if (boxJList != null && boxJList.getSelectedValue() == no) {
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6, 6, 2, 2);
                g2d.setColor(Color.black);
            }
        }
        int index = circuito.getBox().size() - 1;
        if (index > 0) {
            No ultNo = (No) circuito.getBox().get(index);
            index = circuito.getBox().size() - 2;
            if (index > 0 && circuito.getSaidaBoxIndex() != 0) {
                if (circuito.getLadoBoxSaidaBox() == 2) {
                    g2d.setColor(Color.ORANGE);
                    No no = circuito.getPista2Full().get(circuito.getSaidaBoxIndex());
                    Point point = no.getPoint();
                    g2d.fillOval(Util.inteiro(point.x - 5), Util.inteiro(point.y - 5), Util.inteiro(10),
                            Util.inteiro(10));
                }
                if (circuito.getLadoBoxSaidaBox() == 1) {
                    g2d.setColor(Color.ORANGE);
                    No no = circuito.getPista1Full().get(circuito.getSaidaBoxIndex());
                    Point point = no.getPoint();
                    g2d.fillOval(Util.inteiro(point.x - 5), Util.inteiro(point.y - 5), Util.inteiro(10),
                            Util.inteiro(10));
                }
            }
        }

        if ((testePista != null) && (testePista.getTestCar() != null)) {
            if (Math.random() < .5)
                g2d.setColor(Color.DARK_GRAY);
            else
                g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval(testePista.getTestCar().x - 2, testePista.getTestCar().y - 2, 8, 8);
        }

    }

    public void apagarUltimoNo() {
        circuito.getBox().remove(ultimoNo);
        circuito.getPista().remove(ultimoNo);
        ((DefaultListModel) boxJList.getModel()).removeElement(ultimoNo);
        ((DefaultListModel) pistaJList.getModel()).removeElement(ultimoNo);
        vetorizarCircuito();
        repaint();
    }

    public void salvarPista() throws IOException {
        if (existeEscapadaIncompleta()) {
            alertaEscapadaIncompleta();
            return;
        }
        int distanciaKm = parseDistanciaKmOuZero(distanciaKmText.getText());
        if (distanciaKm <= 0) {
            alertaDistanciaNaoInformada();
            return;
        }
        if (file == null) {
            JFileChooser fileChooser = new JFileChooser(new File("src/main/resources/circuitos"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            ExampleFileFilter exampleFileFilter = new ExampleFileFilter("xml");
            fileChooser.setFileFilter(exampleFileFilter);

            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.CANCEL_OPTION) {
                return;
            }
            String fileName = fileChooser.getSelectedFile().getCanonicalFile().toString();
            if (!fileName.endsWith(".xml")) {
                fileName += ".xml";
            }
            file = new File(fileName);
        }
        circuito.setUsaBkg(true);
        circuito.setMultiplicadorLarguraPista(multiplicadorLarguraPista);
        circuito.setProbalidadeChuva(Integer.parseInt(probalidadeChuvaText.getText()));
        circuito.setNome(nomePistaText.getText());
        circuito.setDistanciaKm(distanciaKm);
        if (!vetorizarCircuito()) {
            return;
        }
        salvarCircuitoEmArquivo(circuito.copiaParaArquivoObjetos(), file);
        File arquivoMeta = new File(file.getParentFile(), CarregadorRecursos.nomeArquivoMetadados(file.getName()));
        salvarCircuitoEmArquivo(circuito.copiaParaArquivoMetadados(), arquivoMeta);
        CarregadorRecursos.atualizarAtivoEmCircuitosProperties(file.getName(), circuito.isAtivo());
        JOptionPane.showMessageDialog(this.getSrcFrame(), circuito.getNome(),
                Lang.msg("salvoComSucesso"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void salvarCircuitoEmArquivo(Circuito circuitoParaSalvar, File arquivo) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(arquivo);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
        encoder.writeObject(circuitoParaSalvar);
        encoder.flush();
        String save = new String(byteArrayOutputStream.toByteArray()) + "</java>";
        fileOutputStream.write(save.getBytes());
        fileOutputStream.close();
    }

    public Dimension getPreferredSize() {
        if (backGround != null) {
            return new Dimension(backGround.getWidth(), backGround.getHeight());
        } else {
            return new Dimension(10000, 10000);
        }
    }

    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }

    public Dimension getMaximumSize() {
        return super.getPreferredSize();
    }

    private OpcaoTipoNo getOpcaoTipoNo() {
        return tipoNoCombo != null ? (OpcaoTipoNo) tipoNoCombo.getSelectedItem() : null;
    }

    private boolean isSemSelecao() {
        OpcaoTipoNo opcao = getOpcaoTipoNo();
        return opcao == null || opcao.tipo == null;
    }

    private boolean isTipoNoAtualBox() {
        OpcaoTipoNo opcao = getOpcaoTipoNo();
        return opcao != null && opcao.box;
    }

    public Color getTipoNo() {
        OpcaoTipoNo opcao = getOpcaoTipoNo();
        tipoNo = opcao != null ? opcao.tipo : null;
        return tipoNo;
    }

    private JPanel gerarBotoesTestePista() {
        JPanel buttonsPanel1 = new JPanel();
        buttonsPanel1.setLayout(new GridLayout(1, 6));

        JCheckBox testaPistaCheck = new JCheckBox(Lang.msg("testePistaCheck"));
        testaPistaCheck.setSelected(testePista != null && testePista.isAlive());
        testaPistaCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (testaPistaCheck.isSelected()) {
                        vetorizarCircuito();
                        testePista.iniciarTeste(multiplicadorPista);
                    } else {
                        testePista.pararTeste();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });
        buttonsPanel1.add(testaPistaCheck);

        JCheckBox testaBoxCheck = new JCheckBox(Lang.msg("testarIrAoBox"));
        testaBoxCheck.setSelected(testePista != null && testePista.isIrProBox());
        testaBoxCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testePista.testarBox();
            }
        });
        buttonsPanel1.add(testaBoxCheck);

        JCheckBox testaEscapadaCheck = new JCheckBox(Lang.msg("testarEscapadaCheck"));
        testaEscapadaCheck.setSelected(testePista != null && testePista.isModoEscapada());
        testaEscapadaCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testePista.testarEscapada();
            }
        });
        buttonsPanel1.add(testaEscapadaCheck);

        JButton left = new JButton() {
            @Override
            public String getText() {
                return "<";
            }
        };
        left.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pos = 1;
            }
        });
        buttonsPanel1.add(left);

        JButton center = new JButton() {
            @Override
            public String getText() {
                return "|";

            }
        };
        center.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pos = 0;
            }
        });
        buttonsPanel1.add(center);

        JButton right = new JButton() {
            @Override
            public String getText() {
                return ">";
            }
        };
        right.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pos = 2;
            }
        });
        buttonsPanel1.add(right);
        return buttonsPanel1;
    }

    public void centralizarPonto(Point pin) {
        final Point p = new Point((int) (pin.x * zoom) - (scrollPane.getViewport().getWidth() / 2),
                (int) (pin.y * zoom) - (scrollPane.getViewport().getHeight() / 2));
        if (p.x < 0) {
            p.x = 1;
        }
        double maxX = ((getWidth() * zoom) - scrollPane.getViewport().getWidth());
        if (p.x > maxX) {
            p.x = Util.inteiro(maxX) - 1;
        }
        if (p.y < 0) {
            p.y = 1;
        }
        double maxY = ((getHeight() * zoom) - (scrollPane.getViewport().getHeight()));
        if (p.y > maxY) {
            p.y = Util.inteiro(maxY) - 1;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                repaint();
                scrollPane.getViewport().setViewPosition(p);
            }
        });

    }

    /**
     * Se {@code alvo} for um objeto Escapada, reprocessa o traçado de
     * escapada do circuito (Circuito.reprocessarEscapadas) para refletir a
     * posição/largura/altura/ângulo atuais — largura é o comprimento da onda
     * de escapada e altura é a amplitude, e o ângulo (quando >= 1) multiplica
     * os dois, então mover, redimensionar ou rotacionar o objeto muda o
     * desenho dos nós de escapada.
     */
    void reprocessaEscapadaSeNecessario(ObjetoPista alvo) {
        if (alvo instanceof ObjetoEscapada) {
            circuito.reprocessarEscapadas();
        }
    }

    public void esquerdaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.x -= 5;
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void direitaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.x += 5;
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void cimaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.y -= 5;
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void baixoObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.y += 5;
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void menosAngulo() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            objetoPista.setAngulo(objetoPista.getAngulo() - 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void maisAngulo() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            objetoPista.setAngulo(objetoPista.getAngulo() + 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void maisLargura() {
        if (objetoPista != null) {
            objetoPista.setLargura(objetoPista.getLargura() + 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void menosLargura() {
        if (objetoPista != null) {
            objetoPista.setLargura(objetoPista.getLargura() - 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    public void maisAltura() {
        if (objetoPista != null) {
            objetoPista.setAltura(objetoPista.getAltura() + 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }

    }

    public void menosAltura() {
        if (objetoPista != null) {
            objetoPista.setAltura(objetoPista.getAltura() - 1);
            reprocessaEscapadaSeNecessario(objetoPista);
            repaint();
            return;
        }
    }

    /**
     * Cria uma cópia independente de {@code origem} (mesmas propriedades
     * visuais; pontos/vértices duplicados), sem adicioná-la ao circuito.
     */
    static ObjetoPista clonarObjetoPista(ObjetoPista origem)
            throws InstantiationException, IllegalAccessException {
        ObjetoPista objetoPistaNovo = origem.getClass().newInstance();
        objetoPistaNovo.setAltura(origem.getAltura());
        objetoPistaNovo.setAngulo(origem.getAngulo());
        objetoPistaNovo.setCorPimaria(origem.getCorPimaria());
        objetoPistaNovo.setCorSecundaria(origem.getCorSecundaria());
        objetoPistaNovo.setLargura(origem.getLargura());
        objetoPistaNovo.setPintaEmcima(origem.isPintaEmcima());
        objetoPistaNovo.setNivelDesenho(origem.getNivelDesenho());
        objetoPistaNovo.setTransparencia(origem.getTransparencia());
        objetoPistaNovo
                .setPosicaoQuina(new Point(origem.getPosicaoQuina().x, origem.getPosicaoQuina().y));

        if (origem instanceof ObjetoLivre) {
            ObjetoLivre src = (ObjetoLivre) origem;
            ObjetoLivre dst = (ObjetoLivre) objetoPistaNovo;
            dst.setTipo(src.getTipo());
            dst.setPontos(new ArrayList<Point>());
            for (Point point : src.getPontos()) {
                dst.getPontos().add(new Point(point.x, point.y));
            }
            List<PontoCurva> verticesCopiados = new ArrayList<PontoCurva>();
            for (PontoCurva verticeOriginal : src.getVertices()) {
                PontoCurva copia = new PontoCurva(new Point(verticeOriginal.getPosicao()));
                if (verticeOriginal.getHasteFim() != null) {
                    copia.setHasteFim(new Point(verticeOriginal.getHasteFim()));
                }
                verticesCopiados.add(copia);
            }
            dst.setVertices(verticesCopiados);
            dst.gerar();
        }
        if (origem instanceof ObjetoGuardRails) {
            ObjetoGuardRails src = (ObjetoGuardRails) origem;
            ObjetoGuardRails dst = (ObjetoGuardRails) objetoPistaNovo;
            dst.setPontos(new ArrayList<Point>());
            for (Point point : src.getPontos()) {
                dst.getPontos().add(new Point(point.x, point.y));
            }
            dst.gerar();
        }
        if (origem instanceof ObjetoConstrucao) {
            ObjetoConstrucao src = (ObjetoConstrucao) origem;
            ObjetoConstrucao dst = (ObjetoConstrucao) objetoPistaNovo;
            dst.setTipo(src.getTipo());
            dst.setAfunilamento(src.getAfunilamento());
            dst.setQuantidadeEmpilhamento(src.getQuantidadeEmpilhamento());
            dst.setDirecaoEmpilhamento(src.getDirecaoEmpilhamento());
            dst.setGrauEmpilhamento(src.getGrauEmpilhamento());
        }
        if (origem instanceof ObjetoEscapada) {
            ObjetoEscapada src = (ObjetoEscapada) origem;
            ObjetoEscapada dst = (ObjetoEscapada) objetoPistaNovo;
            dst.setPontos(new ArrayList<Point>());
            for (Point point : src.getPontos()) {
                dst.getPontos().add(new Point(point.x, point.y));
            }
            dst.setTracadoOrigem(src.getTracadoOrigem());
            dst.setIndiceEntrada(src.getIndiceEntrada());
            dst.setIndiceSaida(src.getIndiceSaida());
            dst.gerar();
        }
        return objetoPistaNovo;
    }

    public void subirNivelObjeto() {
        mudarNivelObjeto(1);
    }

    public void descerNivelObjeto() {
        mudarNivelObjeto(-1);
    }

    /**
     * Muda o nível de desenho do objeto selecionado — atalhos PageUp/PageDown.
     * Sem limite: negativo desenha cada vez mais abaixo da pista, positivo
     * cada vez mais acima. Objetos de função (Transparência e Escapada) ficam
     * fora do sistema de níveis — não são "desenhados" no sentido em que os
     * demais são, têm tratamento próprio em corrida.
     */
    private void mudarNivelObjeto(int delta) {
        if (objetoPista == null || objetoPista instanceof ObjetoTransparencia
                || objetoPista instanceof ObjetoEscapada) {
            return;
        }
        objetoPista.setNivelDesenho(objetoPista.getNivelDesenho() + delta);
        // os rótulos das listas exibem o nível entre parênteses
        if (formularioListaObjetosDesenho != null) {
            formularioListaObjetosDesenho.getList().repaint();
        }
        if (formularioListaObjetosFuncao != null) {
            formularioListaObjetosFuncao.getList().repaint();
        }
        repaint();
    }

    /**
     * Reflete a seleção feita no canvas nas listas do split lateral: marca o
     * objeto na lista que o contém e limpa a seleção da outra, sem disparar a
     * centralização do viewport (o objeto já está visível — foi clicado).
     */
    private void selecionarNasListas(ObjetoPista objeto) {
        if (formularioListaObjetosDesenho != null) {
            formularioListaObjetosDesenho.selecionarSemCentralizar(objeto);
        }
        if (formularioListaObjetosFuncao != null) {
            formularioListaObjetosFuncao.selecionarSemCentralizar(objeto);
        }
    }

    public void copiarObjeto() {
        if (objetoPista != null) {
            try {
                ObjetoPista objetoPistaNovo = clonarObjetoPista(objetoPista);
                boolean origemCenario = circuito.getObjetosCenario() != null
                        && circuito.getObjetosCenario().contains(objetoPista);
                List<ObjetoPista> listaAlvo = origemCenario ? circuito.getObjetosCenario() : circuito.getObjetos();
                listaAlvo.add(objetoPistaNovo);
                objetoPistaNovo.setNome("Objeto " + listaAlvo.size());
                if (origemCenario) {
                    formularioListaObjetosDesenho.listarObjetos();
                } else {
                    formularioListaObjetosFuncao.listarObjetos();
                }

            } catch (Exception e) {
                e.printStackTrace();
                srcFrame.dialogDeErro(e);
            }
            repaint();
            return;
        }
    }

    public ObjetoPista getObjetoPista() {
        return objetoPista;
    }

    public void setObjetoPista(ObjetoPista objetoPista) {
        this.objetoPista = objetoPista;
    }

    public JFrame getSrcFrame() {
        return srcFrame;
    }

    public Point getUltimoClicado() {
        return ultimoClicado;
    }

    public void setUltimoClicado(Point ultimoClicado) {
        this.ultimoClicado = ultimoClicado;
    }

    public Circuito getCircuito() {
        return circuito;
    }

    public void setCircuito(Circuito circuito) {
        this.circuito = circuito;
    }

    public void desSelecionaNosPista() {
        if (tipoNoCombo != null) {
            tipoNoCombo.setSelectedIndex(0);
        }
    }
}
