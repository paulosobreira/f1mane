package br.f1mane.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
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
import java.beans.Transient;
import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.DesenhoProceduralCircuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.PontoEscape;
import br.f1mane.recursos.CarregadorRecursos;
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
    private boolean mostraBG = true;
    private boolean creditos = false;
    private boolean pontosEscape = false;
    public final static Color oran = new Color(255, 188, 40, 180);
    public final static Color ver = new Color(255, 10, 10, 150);

    private BufferedImage backGround;
    int ultimoItemBoxSelecionado = -1;
    int ultimoItemPistaSelecionado = -1;

    private final JRadioButton largadaButton = new JRadioButton();
    private final JRadioButton retaButton = new JRadioButton();
    private final JRadioButton curvaAltaButton = new JRadioButton();
    private final JRadioButton curvaBaixaButton = new JRadioButton();
    private final JRadioButton boxButton = new JRadioButton();
    private final JRadioButton boxRetaButton = new JRadioButton();
    private final JRadioButton boxCurvaAltaButton = new JRadioButton();
    private final JRadioButton paraBoxButton = new JRadioButton();
    private final JRadioButton fimBoxButton = new JRadioButton();
    private final JRadioButton semSelecaoButton = new JRadioButton();

    private JScrollPane scrollPane;

    private static final String LADO_COMBO_1 = "BOX LADO 1";
    private static final String LADO_COMBO_2 = "BOX LADO 2";
    private static final String SAIDA_LADO_COMBO_1 = "SAIDA BOX LADO 1";
    private static final String SAIDA_LADO_COMBO_2 = "SAIDA BOX LADO 2";
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
    private FormularioListaObjetos formularioListaObjetos;
    private FormularioListaObjetos formularioListaObjetosCenario;
    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
    JCheckBox noite = new JCheckBox();
    private final JLabel corFundoLabel = criaIndicadorDeCor();
    private final JLabel corAsfaltoLabel = criaIndicadorDeCor();
    File file;

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
            JOptionPane.showMessageDialog(null, "É obrigatório ter um nó de Largada", "Operação ilegal",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (ladoBoxCombo.getSelectedItem().equals(LADO_COMBO_1)) {
            circuito.setLadoBox(1);
        } else {
            circuito.setLadoBox(2);
        }
        if (ladoBoxSaidaBoxCombo.getSelectedItem().equals(SAIDA_LADO_COMBO_1)) {
            circuito.setLadoBoxSaidaBox(1);
        } else {
            circuito.setLadoBoxSaidaBox(2);
        }
        multiplicadorLarguraPista = circuito.getMultiplicadorLarguraPista();
        if (multiplicadorLarguraPista < 1.0 || multiplicadorLarguraPista > 2.0) {
            JOptionPane.showMessageDialog(null, "Largura Pista deve ser entre 1.0 e 2.0", "Operação ilegal",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if (defaultListModel.size() > 10) {
            circuito.vetorizarPista(this.multiplicadorPista, this.multiplicadorLarguraPista);
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

        JPanel controlPanel = gerarListsNosPistaBox();

        JPanel buttonsPanel = gerarBotoesTracado();

        JPanel radiosPanel = new JPanel();
        radiosPanel.setLayout(new GridLayout(1, 10));

        ButtonGroup buttonGroup = new ButtonGroup();

        buttonGroup.add(largadaButton);
        buttonGroup.add(retaButton);
        buttonGroup.add(curvaAltaButton);
        buttonGroup.add(curvaBaixaButton);
        buttonGroup.add(boxButton);
        buttonGroup.add(boxRetaButton);
        buttonGroup.add(boxCurvaAltaButton);
        buttonGroup.add(paraBoxButton);
        buttonGroup.add(fimBoxButton);
        buttonGroup.add(semSelecaoButton);
        semSelecaoButton.setSelected(true);

        JPanel bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Largada"));
        bottonsPanel.add(largadaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Reta"));
        bottonsPanel.add(retaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Curva Alta"));
        bottonsPanel.add(curvaAltaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Curva Baixa"));
        bottonsPanel.add(curvaBaixaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Box"));
        bottonsPanel.add(boxButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Reta Box"));
        bottonsPanel.add(boxRetaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Curva Box"));
        bottonsPanel.add(boxCurvaAltaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Parada Box"));
        bottonsPanel.add(paraBoxButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel("No Fim box"));
        bottonsPanel.add(fimBoxButton);
        radiosPanel.add(bottonsPanel);

        gerarLayout(srcFrame, controlPanel, buttonsPanel, radiosPanel);
        testePista = new TestePista(this, circuito);
        adicionaEventosMouse(srcFrame);
    }

    private JPanel gerarBotoesTracado() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3));

        JButton desenhaTracadoBot = new JButton("Desenha Tracado");
        desenhaTracadoBot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desenhaTracado = !desenhaTracado;
                MainPanelEditor.this.repaint();
            }
        });

        JButton desenhaBackgroundBot = new JButton("Desenho Background");
        desenhaBackgroundBot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mostraBG = !mostraBG;
                MainPanelEditor.this.repaint();
            }
        });

        JButton apagarUltimoNoButton = new JButton("Apagar Ultimo NO");
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

        buttonsPanel.add(apagarUltimoNoButton);

        JButton apagaNoListaButton = new JButton("Apaga Nó na lista Selecionada");
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

        JButton criarObjeto = new JButton("Criar Objeto");
        criarObjeto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    semSelecaoButton.setSelected(true);
                    FormularioObjetos formularioObjetos = new FormularioObjetos(MainPanelEditor.this);
                    formularioObjetos.mostrarPainelModal();
                    TipoObjetoPista tipoSelecionado = (TipoObjetoPista) formularioObjetos
                            .getTipoComboBox().getSelectedItem();
                    objetoPista = tipoSelecionado.criar();
                    posicionaObjetoPista = true;
                    criandoObjetoCenario = tipoSelecionado.isCenario();
                    if (objetoPista instanceof ObjetoTransparencia) {
                        objetoPista.setTransparencia(125);
                        desenhandoObjetoLivre = true;
                    }
                    formularioObjetos.carregarCampos(objetoPista);
                    formularioObjetos.formularioObjetoPista(objetoPista);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    srcFrame.dialogDeErro(e2);
                }

            }
        });
        JButton creditosButton = new JButton("Créditos");
        creditosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JOptionPane.showMessageDialog(null, "Informe a posição dos créditos na imagem do circuito.",
                            "Informações", JOptionPane.INFORMATION_MESSAGE);
                    creditos();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });

        buttonsPanel.add(apagaNoListaButton);
        buttonsPanel.add(desenhaTracadoBot);
        buttonsPanel.add(desenhaBackgroundBot);
        buttonsPanel.add(creditosButton);
        buttonsPanel.add(criarObjeto);
        return buttonsPanel;
    }

    private JPanel gerarListsNosPistaBox() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(2, 1));

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
        radioPistaPanel.add(new JLabel("Nos da Pista"));
        JPanel pistas = new JPanel();
        pistas.setLayout(new BorderLayout());
        pistas.add(radioPistaPanel, BorderLayout.NORTH);
        JScrollPane pistaJListJScrollPane = new JScrollPane(pistaJList) {
            @Override
            @Transient
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().height, 160);
            }
        };
        pistas.add(pistaJListJScrollPane, BorderLayout.CENTER);
        controlPanel.add(pistas);
        JPanel boxes = new JPanel();
        boxes.setLayout(new BorderLayout());
        radioPistaPanel = new JPanel();
        radioPistaPanel.add(new JLabel("Nos do Box"));
        boxes.add(radioPistaPanel, BorderLayout.NORTH);
        JScrollPane boxJListJScrollPane = new JScrollPane(boxJList) {
            @Override
            @Transient
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().height, 160);
            }
        };
        boxes.add(boxJListJScrollPane, BorderLayout.CENTER);
        controlPanel.add(boxes);
        return controlPanel;
    }

    private void gerarLayout(JFrame frame, JPanel controlPanel, JPanel buttonsPanel, JPanel radiosPanel) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());
        if (backGround != null)
            this.setPreferredSize(new Dimension(backGround.getWidth(), backGround.getHeight()));
        else {
            this.setPreferredSize(new Dimension(10000, 10000));
        }
        frame.getContentPane().add(controlPanel, BorderLayout.WEST);
        scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        JPanel nothPanel = new JPanel(new GridLayout(2, 1));
        nothPanel.add(radiosPanel);
        nothPanel.add(buttonsPanel);
        frame.getContentPane().add(nothPanel, BorderLayout.NORTH);
        frame.getContentPane().add(iniciaEditorVetorizado(), BorderLayout.SOUTH);
        formularioListaObjetos = new FormularioListaObjetos(this);
        formularioListaObjetos.listarObjetos();
        formularioListaObjetosCenario = new FormularioListaObjetos(this, Circuito::getObjetosCenario);
        formularioListaObjetosCenario.listarObjetos();
        JPanel listasObjetosPanel = new JPanel(new GridLayout(2, 1));
        listasObjetosPanel.add(formularioListaObjetos.getObjetos());
        listasObjetosPanel.add(formularioListaObjetosCenario.getObjetos());
        frame.getContentPane().add(listasObjetosPanel, BorderLayout.EAST);

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
        MouseAdapter mouseAdapter = new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                frame.requestFocus();
            }

            public void mousePressed(MouseEvent e) {
                if (!semSelecaoButton.isSelected() || !SwingUtilities.isLeftMouseButton(e)
                        || posicionaObjetoPista || desenhandoObjetoLivre) {
                    return;
                }
                ObjetoPista encontrado = encontraObjetoPista(e.getPoint());
                if (encontrado == null || encontrado.getPosicaoQuina() == null) {
                    return;
                }
                objetoPista = encontrado;
                objetoArrastando = encontrado;
                offsetArraste = new Point(e.getX() - encontrado.getPosicaoQuina().x,
                        e.getY() - encontrado.getPosicaoQuina().y);
            }

            public void mouseDragged(MouseEvent e) {
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
                objetoArrastando = null;
            }

            public void mouseClicked(MouseEvent e) {
                if (arrastouObjeto) {
                    arrastouObjeto = false;
                    return;
                }
                if (semSelecaoButton.isSelected()) {
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
                        objetoTransparencia.setTransparencia(125);
                        if (circuito.getObjetos() == null)
                            circuito.setObjetos(new ArrayList<ObjetoPista>());
                        circuito.getObjetos().add(objetoTransparencia);
                        formularioListaObjetos.listarObjetos();
                        objetoTransparencia.setNome("Objeto " + circuito.getObjetos().size());
                        objetoTransparencia.gerar();
                        objetoPista = null;
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
                        formularioListaAlvo = formularioListaObjetosCenario;
                    } else {
                        if (circuito.getObjetos() == null) {
                            circuito.setObjetos(new ArrayList<ObjetoPista>());
                        }
                        listaAlvo = circuito.getObjetos();
                        formularioListaAlvo = formularioListaObjetos;
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
                } else if (creditos) {
                    circuito.setCreditos(e.getPoint());
                    repaint();
                    creditos = false;
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
                JPopupMenu menu = new JPopupMenu();
                menu.add(criaPainelAjusteRapido(encontrado));
                menu.show(MainPanelEditor.this, e.getX(), e.getY());
            }
        };
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
    }

    private JPanel criaPainelAjusteRapido(final ObjetoPista alvo) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JSpinner larguraSpinner = new JSpinner(new SpinnerNumberModel(alvo.getLargura(), 0, 10000, 1));
        JSpinner alturaSpinner = new JSpinner(new SpinnerNumberModel(alvo.getAltura(), 0, 10000, 1));
        JSpinner anguloSpinner = new JSpinner(
                new SpinnerNumberModel((int) alvo.getAngulo(), -360, 360, 1));
        panel.add(new JLabel("Largura"));
        panel.add(larguraSpinner);
        panel.add(new JLabel("Altura"));
        panel.add(alturaSpinner);
        panel.add(new JLabel("Angulo"));
        panel.add(anguloSpinner);
        larguraSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                alvo.setLargura(((Integer) larguraSpinner.getValue()).intValue());
                reprocessaEscapadaSeNecessario(alvo);
                repaint();
            }
        });
        alturaSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                alvo.setAltura(((Integer) alturaSpinner.getValue()).intValue());
                reprocessaEscapadaSeNecessario(alvo);
                repaint();
            }
        });
        anguloSpinner.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                alvo.setAngulo(((Integer) anguloSpinner.getValue()).doubleValue());
                reprocessaEscapadaSeNecessario(alvo);
                repaint();
            }
        });
        return panel;
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
    private ObjetoPista encontraObjetoPista(Point point) {
        ObjetoPista encontrado = encontraObjetoPistaNaLista(circuito.getObjetos(), point);
        if (encontrado != null) {
            return encontrado;
        }
        return encontraObjetoPistaNaLista(circuito.getObjetosCenario(), point);
    }

    private ObjetoPista encontraObjetoPistaNaLista(List<ObjetoPista> lista, Point point) {
        if (lista == null) {
            return null;
        }
        for (ObjetoPista objetoPista : lista) {
            if (objetoPista.obterArea().contains(point)) {
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

    private void inserirNoNasJList(No no) {
        if (boxButton.isSelected() || paraBoxButton.isSelected() || fimBoxButton.isSelected()
                || boxRetaButton.isSelected() || boxCurvaAltaButton.isSelected()) {
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
                JOptionPane.showMessageDialog(this, "Não pode inserir nos de box na psita", "Operação ilegal",
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

        // 3. Sequência de desenho existente
        if (desenhaTracado) {
            DesenhoProceduralCircuito.desenhaPistaZebraEBox(g2d, circuito, zoom);
        }
        desenhaCarroTeste(g2d);
        desenhaEntradaParadaSaidaBox(g2d);
        desenhaLargada(g2d);
        desenhaGrid(g2d);
        desenhaBoxes(g2d);
        desenhaObjetosBaixo(g2d);
        desenhaPreObjetoLivre(g2d);
        desenhaPreObjetoTransparencia(g2d);
        desenhaObjetosCima(g2d);
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
        g2d.fillRoundRect(x - 15, y - 15, 200, 180, 15, 15);
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
    }

    private void desenhaListaObjetos(Graphics2D g2d) {
        if (formularioListaObjetos != null) {
            if (formularioListaObjetos.getList().getSelectedIndex() != -1) {
                ObjetoPista objetoPista = (ObjetoPista) formularioListaObjetos.getDefaultListModelOP()
                        .get(formularioListaObjetos.getList().getSelectedIndex());
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

    private void desenhaObjetosCima(Graphics2D g2d) {
        if (circuito == null) {
            return;
        }
        for (ObjetoPista objetoPista : todosObjetos()) {
            if (!objetoPista.isPintaEmcima())
                continue;
            objetoPista.desenha(g2d, zoom);
        }
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
        g2d.setColor(Color.BLACK);
        if (objetoPista == null || !desenhandoObjetoLivre || !(objetoPista instanceof ObjetoLivre))
            return;
        ObjetoLivre objetoLivre = (ObjetoLivre) objetoPista;
        if (objetoLivre.getPontos().size() == 1) {
            return;
        }
        Point ant = null;
        for (Point p : objetoLivre.getPontos()) {
            if (ant != null) {
                g2d.drawLine(Util.inteiro(ant.x * zoom), Util.inteiro(ant.y * zoom), Util.inteiro(p.x * zoom),
                        Util.inteiro(p.y * zoom));
            }
            ant = p;
        }

    }

    private void desenhaBoxes(Graphics2D g2d) {
        DesenhoProceduralCircuito.desenhaVagasBox(g2d, circuito, zoom);
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

    private void desenhaLargada(Graphics2D g2d) {
        if (circuito.getPistaFull() == null || circuito.getPistaFull().isEmpty()) {
            return;
        }
        No n1 = (No) circuito.getPistaFull().get(0);
        No n2 = (No) circuito.getPistaFull().get(20);
        Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom), Util.inteiro(n1.getPoint().y * zoom));
        Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom), Util.inteiro(n2.getPoint().y * zoom));
        double larguraZebra = (larguraPistaPixeis * 0.01);
        RoundRectangle2D rectangle = new RoundRectangle2D.Double((p1.x - (larguraZebra / 2)),
                (p1.y - (larguraPistaPixeis / 2)), larguraZebra, larguraPistaPixeis, 5 * zoom, 5 * zoom);
        double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
        double rad = Math.toRadians((double) calculaAngulo);
        GeneralPath generalPath = new GeneralPath(rectangle);
        AffineTransform affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
        affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
        g2d.setColor(Color.white);
        g2d.fill(generalPath.createTransformedShape(affineTransformRect));

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

    private void desenhaObjetosBaixo(Graphics2D g2d) {
        if (circuito == null) {
            return;
        }
        for (ObjetoPista objetoPista : todosObjetos()) {
            if (objetoPista.isPintaEmcima())
                continue;
            objetoPista.desenha(g2d, zoom);
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
        if (circuito != null && circuito.getCreditos() != null) {
            g2d.setColor(oran);
            g2d.fillOval((int) circuito.getCreditos().getX() - 2, (int) circuito.getCreditos().getY() - 2, 8, 8);
        }

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

        Map<PontoEscape, List<No>> escapeMap = circuito.getEscapeMap();
        for (Iterator<PontoEscape> iterator = escapeMap.keySet().iterator(); iterator.hasNext(); ) {
            PontoEscape key = iterator.next();
            List<No> list = escapeMap.get(key);
            Point pOld = null;
            for (Iterator iterator2 = list.iterator(); iterator2.hasNext(); ) {
                No no2 = (No) iterator2.next();
                if (no2 == null) {
                    pOld = null;
                    continue;
                }
                if (no2.getTracado() == 4 || no2.getTracado() == 5) {
                    g2d.setColor(ObjetoEscapada.red);
                    Point pNew = new Point(Util.inteiro(no2.getX() - 5), Util.inteiro(no2.getY() - 5));
                    if (pOld != null) {
                        g2d.drawLine(pOld.x, pOld.y, pNew.x, pNew.y);
                    }
                    pOld = pNew;
                } else {
                    pOld = null;
                }
            }
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
        if (!vetorizarCircuito()) {
            return;
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XMLEncoder encoder = new XMLEncoder(byteArrayOutputStream);
        encoder.writeObject(circuito);
        encoder.flush();
        String save = new String(byteArrayOutputStream.toByteArray()) + "</java>";
        fileOutputStream.write(save.getBytes());
        fileOutputStream.close();
        JOptionPane.showMessageDialog(this.getSrcFrame(), circuito.getNome(),
                "Salvo com sucesso. ", JOptionPane.INFORMATION_MESSAGE);
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

    public void creditos() {
        semSelecaoButton.setSelected(true);
        objetoPista = null;
        if (circuito.getCreditos() != null) {
            centralizarPonto(circuito.getCreditos());
        }
        creditos = true;
    }

    public Color getTipoNo() {
        if (largadaButton.isSelected()) {
            tipoNo = No.LARGADA;
        }
        if (retaButton.isSelected()) {
            tipoNo = No.RETA;
        }
        if (curvaAltaButton.isSelected()) {
            tipoNo = No.CURVA_ALTA;
        }
        if (curvaBaixaButton.isSelected()) {
            tipoNo = No.CURVA_BAIXA;
        }
        if (boxButton.isSelected()) {
            tipoNo = No.BOX;
        }
        if (boxRetaButton.isSelected()) {
            tipoNo = No.RETA;
        }
        if (boxCurvaAltaButton.isSelected()) {
            tipoNo = No.CURVA_ALTA;
        }
        if (paraBoxButton.isSelected()) {
            tipoNo = No.PARADA_BOX;
        }
        if (fimBoxButton.isSelected()) {
            tipoNo = No.FIM_BOX;
        }
        return tipoNo;
    }

    private JPanel iniciaEditorVetorizado() {
        JPanel radioPistaPanel = new JPanel();
        radioPistaPanel.add(new JLabel("Nos da Pista"));
        JPanel radioBoxPanel = new JPanel();
        radioBoxPanel.add(new JLabel("Nos do Box"));
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));

        JPanel buttonsPanel1 = new JPanel();
        buttonsPanel1.setLayout(new GridLayout(1, 6));

        JButton testaPistaButton = new JButton("Iniciar/Parar Teste de Pista");
        testaPistaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (testePista.isAlive()) {
                        testePista.pararTeste();
                    } else {
                        vetorizarCircuito();
                        testePista.iniciarTeste(multiplicadorPista);
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });
        buttonsPanel1.add(testaPistaButton);

        JButton testaBoxButton = new JButton("Ligar/Desligar Box");
        testaBoxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testePista.testarBox();
            }
        });
        buttonsPanel1.add(testaBoxButton);

        JButton testaEscapadaButton = new JButton("Ligar/Desligar Modo Escapada");
        testaEscapadaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testePista.testarEscapada();
            }
        });
        buttonsPanel1.add(testaEscapadaButton);

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

        JPanel buttonsPanel2 = new JPanel();
        buttonsPanel2.setLayout(new GridLayout());

        nomePistaText = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 40);
            }
        };
        if (circuito != null) {
            nomePistaText.setText(circuito.getNome());
        }
        buttonsPanel2.add(new JLabel("Nome do circuito"));
        buttonsPanel2.add(nomePistaText);

        ladoBoxCombo = new JComboBox();
        ladoBoxCombo.addItem(LADO_COMBO_1);
        ladoBoxCombo.addItem(LADO_COMBO_2);
        if (circuito != null && circuito.getLadoBox() == 2) {
            ladoBoxCombo.setSelectedItem(LADO_COMBO_2);
        }
        buttonsPanel2.add(ladoBoxCombo);

        ladoBoxSaidaBoxCombo = new JComboBox();
        ladoBoxSaidaBoxCombo.addItem(SAIDA_LADO_COMBO_1);
        ladoBoxSaidaBoxCombo.addItem(SAIDA_LADO_COMBO_2);
        if (circuito != null && circuito.getLadoBoxSaidaBox() == 2) {
            ladoBoxSaidaBoxCombo.setSelectedItem(SAIDA_LADO_COMBO_2);
        }
        buttonsPanel2.add(ladoBoxSaidaBoxCombo);

        probalidadeChuvaText = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, super.getPreferredSize().height);
            }
        };
        if (circuito != null) {
            probalidadeChuvaText.setText("" + circuito.getProbalidadeChuva());
        }
        JPanel p2 = new JPanel();
        p2.add(new JLabel() {
            @Override
            public String getText() {
                return "% Chuva";
            }
        });
        p2.add(probalidadeChuvaText);
        buttonsPanel2.add(p2);

        if (multiplicadorLarguraPista < 1.0) {
            multiplicadorLarguraPista = 1.0;
        }
        if (multiplicadorLarguraPista > 2.0) {
            multiplicadorLarguraPista = 2.0;
        }
        SpinnerNumberModel model1 = new SpinnerNumberModel(multiplicadorLarguraPista, 1.0, 2.0, 0.1);
        larguraPistaSpinner = new JSpinner(model1) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(60, super.getPreferredSize().height);
            }
        };
        larguraPistaSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                circuito.setMultiplicadorLarguraPista(((Double) larguraPistaSpinner.getModel().getValue()).doubleValue());
                vetorizarCircuito();
                repaint();
            }
        });
        p2 = new JPanel();
        p2.add(new JLabel() {
            @Override
            public String getText() {
                return "Largura";
            }
        });
        p2.add(larguraPistaSpinner);
        buttonsPanel2.add(p2);

        p2 = new JPanel();
        p2.add(new JLabel("Noite") {
            @Override
            public String getText() {
                return "Noite";
            }
        });

        noite.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                circuito.setNoite(noite.isSelected());
                repaint();
            }
        });
        p2.add(noite);
        buttonsPanel2.add(p2);

        p2 = new JPanel();
        p2.add(new JLabel("Cor de Fundo"));
        atualizaCorLabel(corFundoLabel, circuito != null ? circuito.getCorFundo() : null, Color.WHITE);
        corFundoLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        "Cor de Fundo", corFundoLabel.getBackground());
                if (nova != null) {
                    circuito.setCorFundo(nova);
                    atualizaCorLabel(corFundoLabel, nova, Color.WHITE);
                    repaint();
                }
            }
        });
        p2.add(corFundoLabel);
        buttonsPanel2.add(p2);

        p2 = new JPanel();
        p2.add(new JLabel("Cor do Asfalto"));
        atualizaCorLabel(corAsfaltoLabel, circuito != null ? circuito.getCorAsfalto() : null,
                DesenhoProceduralCircuito.COR_PISTA);
        corAsfaltoLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Color nova = JColorChooser.showDialog(MainPanelEditor.this,
                        "Cor do Asfalto", corAsfaltoLabel.getBackground());
                if (nova != null) {
                    circuito.setCorAsfalto(nova);
                    atualizaCorLabel(corAsfaltoLabel, nova, DesenhoProceduralCircuito.COR_PISTA);
                    repaint();
                }
            }
        });
        p2.add(corAsfaltoLabel);
        buttonsPanel2.add(p2);

        buttonsPanel.add(buttonsPanel1);
        buttonsPanel.add(buttonsPanel2);
        return buttonsPanel;
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

    public void copiarObjeto() {
        if (objetoPista != null) {
            try {
                ObjetoPista objetoPistaNovo = objetoPista.getClass().newInstance();
                objetoPistaNovo.setAltura(objetoPista.getAltura());
                objetoPistaNovo.setAngulo(objetoPista.getAngulo());
                objetoPistaNovo.setCorPimaria(objetoPista.getCorPimaria());
                objetoPistaNovo.setCorSecundaria(objetoPista.getCorSecundaria());
                objetoPistaNovo.setLargura(objetoPista.getLargura());
                objetoPistaNovo.setPintaEmcima(objetoPista.isPintaEmcima());
                objetoPistaNovo.setTransparencia(objetoPista.getTransparencia());
                objetoPistaNovo
                        .setPosicaoQuina(new Point(objetoPista.getPosicaoQuina().x, objetoPista.getPosicaoQuina().y));

                if (objetoPista instanceof ObjetoLivre) {
                    ObjetoLivre src = (ObjetoLivre) objetoPista;
                    ObjetoLivre dst = (ObjetoLivre) objetoPistaNovo;
                    dst.setPontos(new ArrayList<Point>());
                    List<Point> pontos = src.getPontos();
                    for (Point point : pontos) {
                        dst.getPontos().add(new Point(point.x, point.y));
                    }
                    dst.gerar();
                }
                boolean origemCenario = circuito.getObjetosCenario() != null
                        && circuito.getObjetosCenario().contains(objetoPista);
                List<ObjetoPista> listaAlvo = origemCenario ? circuito.getObjetosCenario() : circuito.getObjetos();
                listaAlvo.add(objetoPistaNovo);
                objetoPistaNovo.setNome("Objeto " + listaAlvo.size());
                if (origemCenario) {
                    formularioListaObjetosCenario.listarObjetos();
                } else {
                    formularioListaObjetos.listarObjetos();
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
        semSelecaoButton.setSelected(true);
    }


    public void novo() {
        JFileChooser fileChooser = new JFileChooser(new File("src/main/resources/circuitos"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        ExampleFileFilter exampleFileFilter = new ExampleFileFilter("jpg");
        fileChooser.setFileFilter(exampleFileFilter);
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        File file = fileChooser.getSelectedFile();
        backGround = CarregadorRecursos.carregaBackGround(file.getName(), this, circuito);
        if (backGround == null) {
            JOptionPane.showMessageDialog(null,
                    "Imagem para criar circuito deve esta na pasta /f1mane/src/sowbreira/f1mane/recursos/",
                    "Operação ilegal", JOptionPane.ERROR_MESSAGE);
            return;
        }
        circuito.setBackGround(file.getName());
        testePista = new TestePista(this, circuito);
        iniciaEditor();
        atualizaListas();
        srcFrame.pack();
        srcFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public void editar() throws IOException, ClassNotFoundException {
        JFileChooser fileChooser = new JFileChooser(new File("src/main/resources/circuitos"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        ExampleFileFilter exampleFileFilter = new ExampleFileFilter("xml");
        fileChooser.setFileFilter(exampleFileFilter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        file = fileChooser.getSelectedFile();
        circuito = CarregadorRecursos.carregarCircuito(fileChooser.getSelectedFile().getName());
        testePista = new TestePista(this, circuito);
        backGround = CarregadorRecursos.carregaBackGround(circuito.getBackGround(), this, circuito);
        iniciaEditor();
        atualizaListas();
        vetorizarCircuito();
        larguraPistaSpinner.getModel().setValue(Double.valueOf(circuito.getMultiplicadorLarguraPista()));
        probalidadeChuvaText.setText(String.valueOf(circuito.getProbalidadeChuva()));
        nomePistaText.setText(circuito.getNome());
        noite.setSelected(circuito.isNoite());
        atualizaCorLabel(corFundoLabel, circuito.getCorFundo(), Color.WHITE);
        atualizaCorLabel(corAsfaltoLabel, circuito.getCorAsfalto(), DesenhoProceduralCircuito.COR_PISTA);
        srcFrame.pack();
        srcFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        if (circuito.getPistaFull() != null && !circuito.getPistaFull().isEmpty()) {
            centralizarPonto(((No) circuito.getPistaFull().get(0)).getPoint());
        }
    }
}
