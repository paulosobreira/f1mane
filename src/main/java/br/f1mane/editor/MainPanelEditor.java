package br.f1mane.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import br.nnpe.Constantes;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.MainFrameEditor;
import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.No;
import br.f1mane.entidades.ObjetoEscapada;
import br.f1mane.entidades.ObjetoLivre;
import br.f1mane.entidades.ObjetoPista;
import br.f1mane.entidades.ObjetoTransparencia;
import br.f1mane.entidades.PontoEscape;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.PainelCircuito;

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
    private MainFrameEditor srcFrame;
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
    private static final Color COR_PISTA = new Color(192, 192, 192);
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
    private JTextField velocidadePistaText;
    private final BasicStroke trilho = new BasicStroke(1);
    private BasicStroke pista;
    private BasicStroke pistaTinta;
    private BasicStroke box;
    private int larguraPistaPixeis;
    private BasicStroke zebra;
    private JComboBox ladoBoxCombo;
    private JComboBox ladoBoxSaidaBoxCombo;
    private ObjetoPista objetoPista;
    private boolean desenhandoObjetoLivre;
    private boolean posicionaObjetoPista;
    private Point ultimoClicado;
    private FormularioListaObjetos formularioListaObjetos;
    protected final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

    public MainPanelEditor() {
    }

    public MainPanelEditor(String backGroundStr, MainFrameEditor frame) {
        backGround = CarregadorRecursos.carregaBackGround(backGroundStr, this, circuito);
        if (backGround == null) {
            JOptionPane.showMessageDialog(null, Lang.msg("backGroundNull"), Lang.msg("039"), JOptionPane.ERROR_MESSAGE);
            return;

        }
        this.srcFrame = frame;
        iniciaEditor(frame);
        atualizaListas();
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    public MainPanelEditor(MainFrameEditor frame) throws IOException, ClassNotFoundException {
        JFileChooser fileChooser = new JFileChooser(
                CarregadorRecursos.class.getResource("CarregadorRecursos.class").getFile());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        ExampleFileFilter exampleFileFilter = new ExampleFileFilter("xml");
        fileChooser.setFileFilter(exampleFileFilter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        circuito = CarregadorRecursos.carregarCircuito(fileChooser.getSelectedFile().getName());
//		FileInputStream inputStream = new FileInputStream(fileChooser.getSelectedFile());
//		ObjectInputStream ois = new ObjectInputStream(inputStream);
//		circuito = (Circuito) ois.readObject();
        testePista = new TestePista(this, circuito);
        // if("jacarepagua.jpg".equals(circuito.getBackGround())){
        // circuito.setBackGround("jacarepagua_mro.jpg");
        // }
        backGround = CarregadorRecursos.carregaBackGround(circuito.getBackGround(), this, circuito);
        this.srcFrame = frame;
        iniciaEditor(frame);
        atualizaListas();
        vetorizarCircuito(false);
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        if (circuito.getPistaFull() != null && !circuito.getPistaFull().isEmpty()) {
            centralizarPonto(((No) circuito.getPistaFull().get(0)).getPoint());
        }
    }

    private void vetorizarCircuito(boolean reprocessa) {
        mx = 0;
        my = 0;
        pista = null;
        pistaTinta = null;
        box = null;
        zebra = null;
        larguraPistaPixeis = 0;
        testePista.pararTeste();

        DefaultListModel defaultListModel = (DefaultListModel) pistaJList.getModel();
        if (defaultListModel.size() < 10) {
            JOptionPane.showMessageDialog(null, Lang.msg("pelomenos10NosParaProcesar"), Lang.msg("039"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean temLargada = false;
        for (int i = 0; i < defaultListModel.size(); i++) {
            No no = (No) defaultListModel.get(i);
            if (No.LARGADA.equals(no.getTipo())) {
                temLargada = true;
            }
        }
        if (!temLargada) {
            JOptionPane.showMessageDialog(null, Lang.msg("noLargadaObrigatorio"), Lang.msg("039"),
                    JOptionPane.ERROR_MESSAGE);
            return;
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
        if (!reprocessa && circuito != null) {
            multiplicadorPista = Constantes.MULTI_CURVA;
            // multiplicadorPista = circuito.getMultiplciador();
        }
        if (!reprocessa && circuito != null) {
            multiplicadorLarguraPista = circuito.getMultiplicadorLarguraPista();
        }
        if (multiplicadorLarguraPista < 1.0 || multiplicadorLarguraPista > 2.0) {
            JOptionPane.showMessageDialog(null, Lang.msg("multiplicadorLarguraPista"), Lang.msg("039"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        circuito.setUsaBkg(true);
        circuito.vetorizarPista(this.multiplicadorPista, this.multiplicadorLarguraPista);
        circuito.setProbalidadeChuva(Integer.parseInt(probalidadeChuvaText.getText()));
        circuito.setVelocidadePista(Double.parseDouble(velocidadePistaText.getText()));
        probalidadeChuvaText.setText(String.valueOf(circuito.getProbalidadeChuva()));
        velocidadePistaText.setText(String.valueOf(circuito.getVelocidadePista()));
        circuito.setNome(nomePistaText.getText());
        larguraPistaSpinner.getModel().setValue(Double.valueOf(multiplicadorLarguraPista));
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
        No n1 = (No) l.get(0);
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

    private void iniciaEditor(JFrame frame) {

        carroCima = CarregadorRecursos.carregaBufferedImage("png/CarroCima.png");

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
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("099");
            }
        });
        bottonsPanel.add(largadaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("100");
            }
        });
        bottonsPanel.add(retaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("101");
            }
        });
        bottonsPanel.add(curvaAltaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("102");
            }
        });
        bottonsPanel.add(curvaBaixaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("103");
            }
        });
        bottonsPanel.add(boxButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("boxReta");
            }
        });
        bottonsPanel.add(boxRetaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("boxCurvaAlta");
            }
        });
        bottonsPanel.add(boxCurvaAltaButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("104");
            }
        });
        bottonsPanel.add(paraBoxButton);
        radiosPanel.add(bottonsPanel);

        bottonsPanel = new JPanel();
        bottonsPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("fimBox");
            }
        });
        bottonsPanel.add(fimBoxButton);
        radiosPanel.add(bottonsPanel);

        gerarLayout(frame, controlPanel, buttonsPanel, radiosPanel);
        testePista = new TestePista(this, circuito);
        adicionaEventosMouse(frame);
    }

    private JPanel gerarBotoesTracado() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(1, 3));

        JButton desenhaTracadoBot = new JButton("Desenha Tracado") {
            @Override
            public String getText() {
                return Lang.msg("037");
            }
        };
        desenhaTracadoBot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                desenhaTracado = !desenhaTracado;
                MainPanelEditor.this.repaint();
            }
        });

        JButton apagarUltimoNoButton = new JButton() {
            @Override
            public String getText() {
                return Lang.msg("105");
            }
        };
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

        JButton apagaNoListaButton = new JButton() {
            @Override
            public String getText() {
                return Lang.msg("apagaNoListaButton");
            }
        };
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
                    if (FormularioObjetos.OBJETO_ESCAPADA
                            .equals(formularioObjetos.getTipoComboBox().getSelectedItem())) {
                        objetoPista = new ObjetoEscapada();
                        posicionaObjetoPista = true;
                    } else if (FormularioObjetos.OBJETO_TRANSPARENCIA
                            .equals(formularioObjetos.getTipoComboBox().getSelectedItem())) {
                        objetoPista = new ObjetoTransparencia();
                        objetoPista.setTransparencia(125);
                        posicionaObjetoPista = true;
                        desenhandoObjetoLivre = true;
                    }
                    formularioObjetos.formularioObjetoPista(objetoPista);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    srcFrame.dialogDeErro(e2);
                }

            }
        });
        JButton creditosButton = new JButton() {
            @Override
            public String getText() {
                return Lang.msg("creditos");
            }
        };
        creditosButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JOptionPane.showMessageDialog(null, Lang.msg("informePontoCreditos"), Lang.msg("089"),
                            JOptionPane.INFORMATION_MESSAGE);
                    creditos();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });

        buttonsPanel.add(apagaNoListaButton);
        buttonsPanel.add(desenhaTracadoBot);
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
                int keyCoode = e.getKeyCode();

                if (keyCoode == KeyEvent.VK_DELETE) {
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
                int keyCoode = e.getKeyCode();

                if (keyCoode == KeyEvent.VK_DELETE) {
                    if (boxJList.getSelectedValue() == null) {
                        return;
                    }

                    circuito.getBox().remove(boxJList.getSelectedValue());
                    ((DefaultListModel) boxJList.getModel()).remove(boxJList.getSelectedIndex());
                }
            }
        });
        JPanel radioPistaPanel = new JPanel();
        radioPistaPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("032");
            }
        });
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
        radioPistaPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("033");
            }
        });
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
        frame.getContentPane().add(formularioListaObjetos.getObjetos(), BorderLayout.EAST);

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
        this.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                frame.requestFocus();
            }

            public void mouseClicked(MouseEvent e) {
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
                    if (circuito.getObjetos() == null) {
                        circuito.setObjetos(new ArrayList<ObjetoPista>());
                    }
                    Point quina = new Point(ultimoClicado);
                    quina.x -= objetoPista.getLargura() / 2;
                    quina.y -= objetoPista.getAltura() / 2;
                    objetoPista.setPosicaoQuina(quina);
                    circuito.getObjetos().add(objetoPista);
                    formularioListaObjetos.listarObjetos();
                    objetoPista.setNome("Objeto " + circuito.getObjetos().size());
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
        });
    }

    protected void editaObjetoPista(Point point) {
        if (circuito.getObjetos() == null) {
            return;
        }
        for (ObjetoPista objetoPista : circuito.getObjetos()) {
            if (objetoPista.obterArea().contains(point)) {
                FormularioObjetos formularioObjetos = new FormularioObjetos(MainPanelEditor.this);
                formularioObjetos.objetoLivreFormulario(objetoPista);
                break;
            }
        }
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
                JOptionPane.showMessageDialog(this, Lang.msg("038"), Lang.msg("039"), JOptionPane.INFORMATION_MESSAGE);
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
    }

    private void setarHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        setarHints(g2d);
        if (mostraBG && backGround != null) {
            g2d.drawImage(backGround, 0, 0, null);
        }

        if (larguraPistaPixeis == 0)
            larguraPistaPixeis = Util.inteiro(Carro.LARGURA * 1.5 * multiplicadorLarguraPista * zoom);

        if (!mostraBG) {
            if (pista == null)
                pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            if (pistaTinta == null)
                pistaTinta = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05), BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND);
            if (box == null)
                box = new BasicStroke(Util.inteiro(larguraPistaPixeis * .4), BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND);
            if (zebra == null)
                zebra = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05), BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10f, new float[]{10, 10}, 0);
            desenhaTintaPistaEZebra(g2d);
            desenhaPista(g2d);
            desenhaPistaBox(g2d);
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
        if (circuito.getObjetos() != null) {
            y += 20;
            g2d.drawString("Num Objetos : " + circuito.getObjetos().size(), x, y);
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
        g2d.drawString(Lang.msg("ALTA") + ":" + noAlta + " " + (int) (100 * noAlta / total) + "%", x, y);
        y += 20;
        g2d.drawString(Lang.msg("MEDIA") + ":" + noMedia + " " + (int) (100 * noMedia / total) + "%", x, y);
        y += 20;
        g2d.drawString(Lang.msg("BAIXA") + ":" + noBaixa + " " + (int) (100 * noBaixa / total) + "%", x, y);
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

        // limitesViewPort.width -= 100;
        // limitesViewPort.height -= 100;

        // g2d.draw(limitesViewPort);
    }

    private void desenhaObjetosCima(Graphics2D g2d) {
        if (circuito == null) {
            return;
        }
        if (circuito.getObjetos() == null) {
            return;
        }
        for (ObjetoPista objetoPista : circuito.getObjetos()) {
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
        if (circuito.getBoxFull() == null || circuito.getBoxFull().isEmpty()) {
            return;
        }
        double multi = 1.5;
        double maxBox = Util.inteiro(Carro.LARGURA * multi * 12) + Carro.LARGURA;
        if (circuito.getFimParadaBoxIndex() != 0) {
            No ini = (No) circuito.getBoxFull().get(circuito.getParadaBoxIndex());
            No fim = (No) circuito.getBoxFull().get(circuito.getFimParadaBoxIndex());
            int distaciaInicioFim = GeoUtil.distaciaEntrePontos(ini.getPoint(), fim.getPoint());
            if (distaciaInicioFim < maxBox) {
                multi = (multi * distaciaInicioFim) / maxBox;
            }
        }

        int paradas = circuito.getParadaBoxIndex();
        for (int i = 0; i < 12; i++) {
            int iP = paradas + Util.inteiro(Carro.LARGURA * multi * i) + Carro.LARGURA;
            int n1Idx = iP - Carro.MEIA_LARGURA;
            int n2Idx = iP + Carro.MEIA_LARGURA;
            if (n1Idx > circuito.getBoxFull().size()) {
                continue;
            }
            if (iP > circuito.getBoxFull().size()) {
                continue;
            }
            if (n2Idx > circuito.getBoxFull().size()) {
                continue;
            }
            No n1 = (No) circuito.getBoxFull().get(n1Idx);
            No nM = (No) circuito.getBoxFull().get(iP);
            No n2 = (No) circuito.getBoxFull().get(n2Idx);
            Point p1 = new Point(Util.inteiro(n1.getPoint().x * zoom), Util.inteiro(n1.getPoint().y * zoom));
            Point pm = new Point(Util.inteiro(nM.getPoint().x * zoom), Util.inteiro(nM.getPoint().y * zoom));
            Point p2 = new Point(Util.inteiro(n2.getPoint().x * zoom), Util.inteiro(n2.getPoint().y * zoom));
            double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
            Rectangle2D rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
                    (Carro.LARGURA), (Carro.ALTURA));

            Point cima = GeoUtil.calculaPonto(calculaAngulo,
                    Util.inteiro(Carro.ALTURA * getCircuito().getMultiplicadorLarguraPista() * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180,
                    Util.inteiro(Carro.ALTURA * getCircuito().getMultiplicadorLarguraPista() * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point cimaBoxC1 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point baixoBoxC1 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro((Carro.ALTURA) * 3.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point cimaBoxC2 = GeoUtil.calculaPonto(calculaAngulo, Util.inteiro((Carro.ALTURA) * 3.5 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));
            Point baixoBoxC2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inteiro((Carro.ALTURA) * 3.2 * zoom),
                    new Point(Util.inteiro(rectangle.getCenterX()), Util.inteiro(rectangle.getCenterY())));

            RoundRectangle2D retC1;
            RoundRectangle2D retC2;
            if (circuito.getLadoBox() == 1) {
                rectangle = new Rectangle2D.Double((cima.x - (Carro.MEIA_LARGURA * zoom)),
                        (cima.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
                retC1 = new RoundRectangle2D.Double((cimaBoxC1.x - (Carro.LARGURA * zoom)),
                        (cimaBoxC1.y - (Carro.ALTURA * zoom)), (Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom), 5,
                        5);
                retC2 = new RoundRectangle2D.Double((cimaBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
                        (cimaBoxC2.y + (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5,
                        5);
            } else {
                rectangle = new Rectangle2D.Double((baixo.x - (Carro.MEIA_LARGURA * zoom)),
                        (baixo.y - (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
                retC1 = new RoundRectangle2D.Double((baixoBoxC1.x - (Carro.LARGURA * zoom)),
                        (baixoBoxC1.y - (Carro.ALTURA * zoom)), (Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom),
                        5, 5);
                retC2 = new RoundRectangle2D.Double((baixoBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
                        (baixoBoxC2.y + (Carro.MEIA_ALTURA * zoom)), (Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5,
                        5);
            }

            GeneralPath generalPath = new GeneralPath(rectangle);
            AffineTransform affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
            double rad = Math.toRadians((double) calculaAngulo);
            affineTransformRect.setToRotation(rad, rectangle.getCenterX(), rectangle.getCenterY());
            g2d.setColor(new Color(255, 0, 255, 150));
            g2d.fill(generalPath.createTransformedShape(affineTransformRect));
            generalPath = new GeneralPath(retC1);
            affineTransformRect.setToRotation(rad, retC1.getCenterX(), retC1.getCenterY());
            g2d.setColor(new Color(0, 255, 255, 150));
            g2d.fill(generalPath.createTransformedShape(affineTransformRect));

            generalPath = new GeneralPath(retC2);
            affineTransformRect.setToRotation(rad, retC2.getCenterX(), retC2.getCenterY());
            g2d.setColor(new Color(255, 0, 255, 150));
            g2d.fill(generalPath.createTransformedShape(affineTransformRect));

            if (circuito.getLadoBox() == 1)
                g2d.setColor(Color.BLUE);
            else
                g2d.setColor(PainelCircuito.transpMenus);
            g2d.fillOval((int) cimaBoxC1.x, (int) cimaBoxC1.y, 10, 10);

            if (circuito.getLadoBox() == 2)
                g2d.setColor(Color.BLUE);
            else
                g2d.setColor(PainelCircuito.transpMenus);
            g2d.fillOval((int) baixoBoxC1.x, (int) baixoBoxC1.y, 10, 10);

        }

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
        if (circuito.getObjetos() == null) {
            return;
        }
        for (ObjetoPista objetoPista : circuito.getObjetos()) {
            if (objetoPista.isPintaEmcima())
                continue;
            objetoPista.desenha(g2d, zoom);
        }

    }

    private void desenhaPista(Graphics2D g2d) {
        No oldNo = null;
        g2d.setColor(COR_PISTA);
        g2d.setStroke(pista);
        for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
            No no = (No) iter.next();
            if (oldNo != null) {
                g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                        Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));

            }
            oldNo = no;
        }

        No noFinal = (No) circuito.getPistaKey().get(0);
        g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
    }

    public Shape limitesViewPort() {
        Rectangle rectangle = scrollPane.getViewport().getBounds();
        // rectangle.width += 50;
        // rectangle.height += 50;
        rectangle.x = scrollPane.getViewport().getViewPosition().x;
        rectangle.y = scrollPane.getViewport().getViewPosition().y;
        return rectangle;
    }

    private void desenhaPistaBox(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(box);
        No oldNo = null;
        for (Iterator iter = circuito.getBoxKey().iterator(); iter.hasNext(); ) {
            No no = (No) iter.next();
            if (oldNo != null) {
                g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                        Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));

            }
            oldNo = no;
        }
        if (circuito.getBoxKey() != null && !circuito.getBoxKey().isEmpty()) {
            No noFinal = (No) circuito.getBoxKey().get(circuito.getBoxKey().size() - 1);

            g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                    Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
        }
    }

    private void desenhaTintaPistaEZebra(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setStroke(pistaTinta);

        No oldNo = null;
        for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
            No no = (No) iter.next();
            if (oldNo != null) {
                g2d.setColor(Color.WHITE);
                g2d.setStroke(pistaTinta);
                g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                        Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
                if (No.CURVA_ALTA.equals(oldNo.getTipo()) || No.CURVA_BAIXA.equals(oldNo.getTipo())) {
                    g2d.setColor(Color.RED);
                    g2d.setStroke(zebra);
                    g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                            Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));

                }
            }
            oldNo = no;
        }
        No noFinal = (No) circuito.getPistaKey().get(0);
        g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
                Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
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
        repaint();
    }

    public void salvarPista() throws IOException {
        JFileChooser fileChooser = new JFileChooser(
                CarregadorRecursos.class.getResource("CarregadorRecursos.class").getFile());
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        ExampleFileFilter exampleFileFilter = new ExampleFileFilter("f1mane");
        fileChooser.setFileFilter(exampleFileFilter);

        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        String fileName = file.getCanonicalFile().toString();
        if (!fileName.endsWith(".f1mane")) {
            fileName += ".f1mane";
        }
        file = new File(fileName);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
        oos.writeObject(circuito);
        oos.flush();
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
        radioPistaPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("032");
            }
        });
        JPanel radioBoxPanel = new JPanel();
        radioBoxPanel.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("033");
            }
        });
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));

        JPanel buttonsPanel1 = new JPanel();
        buttonsPanel1.setLayout(new GridLayout(1, 5));

        JButton testaPistaButton = new JButton() {
            @Override
            public String getText() {
                return Lang.msg("034");
            }
        };
        testaPistaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (testePista.isAlive()) {
                        testePista.pararTeste();
                    } else {
                        vetorizarCircuito(false);
                        testePista.iniciarTeste(multiplicadorPista);
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                    srcFrame.dialogDeErro(e1);
                }
            }
        });
        buttonsPanel1.add(testaPistaButton);

        JButton testaBoxButton = new JButton("Ligar/Desligar Box") {
            @Override
            public String getText() {
                return Lang.msg("035");
            }
        };
        testaBoxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testePista.testarBox();
            }
        });
        buttonsPanel1.add(testaBoxButton);

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

        JButton reprocessar = new JButton("reprocessarPista") {
            @Override
            public String getText() {
                return Lang.msg("reprocessarPista");
            }
        };
        reprocessar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    multiplicadorLarguraPista = ((Double) larguraPistaSpinner.getModel().getValue()).doubleValue();
                    vetorizarCircuito(true);
                    desSelecionaNosPista();
                    repaint();
                } catch (Exception e2) {
                    e2.printStackTrace();
                    srcFrame.dialogDeErro(e2);
                }

            }
        });
        buttonsPanel1.add(reprocessar);

        JPanel buttonsPanel2 = new JPanel();
        buttonsPanel2.setLayout(new FlowLayout());

        nomePistaText = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 40);
            }
        };
        if (circuito != null) {
            nomePistaText.setText(circuito.getNome());
        }
        buttonsPanel2.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("nomeCircuito");
            }
        });
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
                return Lang.msg("probalidaDeChuva");
            }
        });
        p2.add(probalidadeChuvaText);
        buttonsPanel2.add(p2);

        p2 = new JPanel();

        velocidadePistaText = new JTextField() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(30, super.getPreferredSize().height);
            }
        };
        if (circuito != null) {
            velocidadePistaText.setText("" + circuito.getVelocidadePista());
        }

        p2.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("velocidadePista");
            }
        });
        p2.add(velocidadePistaText);
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
        p2 = new JPanel();
        p2.add(new JLabel() {
            @Override
            public String getText() {
                return Lang.msg("larguraPista");
            }
        });
        p2.add(larguraPistaSpinner);
        buttonsPanel2.add(p2);

        p2 = new JPanel();
        p2.add(new JLabel("Noite") {
            @Override
            public String getText() {
                return Lang.msg("noite");
            }
        });

        final JCheckBox noite = new JCheckBox();
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
        p2.add(new JLabel("UsaBkg") {
            @Override
            public String getText() {
                return Lang.msg("UsaBkg");
            }
        });
        final JCheckBox usaBkg = new JCheckBox();
        if (circuito != null && mostraBG) {
            usaBkg.setSelected(true);
        }
        usaBkg.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                mostraBG = usaBkg.isSelected();
                repaint();
            }
        });
        p2.add(usaBkg);
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

    public void esquerdaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.x -= 5;
            repaint();
            return;
        }
    }

    public void direitaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.x += 5;
            repaint();
            return;
        }
    }

    public void cimaObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.y -= 5;
            repaint();
            return;
        }
    }

    public void baixoObj() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            Point p = objetoPista.getPosicaoQuina();
            p.y += 5;
            repaint();
            return;
        }
    }

    public void menosAngulo() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            objetoPista.setAngulo(objetoPista.getAngulo() - 1);
            repaint();
            return;
        }
    }

    public void maisAngulo() {
        if (objetoPista != null && objetoPista.getPosicaoQuina() != null) {
            objetoPista.setAngulo(objetoPista.getAngulo() + 1);
            repaint();
            return;
        }
    }

    public void maisLargura() {
        if (objetoPista != null) {
            objetoPista.setLargura(objetoPista.getLargura() + 1);
            repaint();
            return;
        }
    }

    public void menosLargura() {
        if (objetoPista != null) {
            objetoPista.setLargura(objetoPista.getLargura() - 1);
            repaint();
            return;
        }
    }

    public void maisAltura() {
        if (objetoPista != null) {
            objetoPista.setAltura(objetoPista.getAltura() + 1);
            repaint();
            return;
        }

    }

    public void menosAltura() {
        if (objetoPista != null) {
            objetoPista.setAltura(objetoPista.getAltura() - 1);
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
                circuito.getObjetos().add(objetoPistaNovo);
                objetoPistaNovo.setNome("Objeto " + circuito.getObjetos().size());

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


    public static void main(String[] args) throws IOException, ClassNotFoundException {
        Circuito circuito1 = CarregadorRecursos.carregarCircuito("indianapoles_mro.xml");
        System.out.println(circuito1);
    }
}
