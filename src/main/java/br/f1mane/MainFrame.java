package br.f1mane;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.visao.GerenciadorVisual;
import br.nnpe.Logger;
import br.f1mane.controles.ControleJogoLocal;
import br.f1mane.controles.InterfaceJogo;
import br.f1mane.entidades.Campeonato;
import br.f1mane.servidor.applet.AppletPaddock;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.ControleSom;
import br.f1mane.visao.PainelMenuLocal;
import br.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Created on 20/06/2007
 */
public class MainFrame extends JFrame {


    private static final long serialVersionUID = -284357233387917389L;
    protected InterfaceJogo controleJogo;
    private JMenuBar bar;
    private JMenu menuJogo;
    private JMenu menuInfo;
    private JCheckBoxMenuItem atualizacaoSuave;
    private JMenuItem iniciar;
    private JMenuItem verControles;
    private JFrame debugFrame;
    protected Campeonato campeonato;
    boolean adicionouPainelNarracao = false;
    boolean adicionouPainelDebug = false;

    private AppletPaddock appletPaddock = new AppletPaddock();

    public InterfaceJogo getControleJogo() {
        return controleJogo;
    }

    public MainFrame(AppletPaddock appletPaddock) throws IOException {
        this.appletPaddock = appletPaddock;
        bar = new JMenuBar();
        debugFrame = new JFrame();
        debugFrame.setJMenuBar(bar);
        menuJogo = new JMenu() {
            public String getText() {
                return Lang.msg("088");
            }

        };
        bar.add(menuJogo);

        menuInfo = new JMenu() {
            public String getText() {
                return Lang.msg("089");
            }

        };
        bar.add(menuInfo);

        gerarMenusSingle(menuJogo);
        gerarMenusInfo(menuInfo);
        pack();
        setSize(1280, 720);
        String title = "Fl-MANE " + getVersao() + " MANager & Engineer";
        setTitle(title);
        if (appletPaddock == null) {
            iniciar();
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        } else {
            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }
        removerKeyListeners();
    }

    public MainFrame() {
    }

    public String getVersao() {
        return CarregadorRecursos.getVersaoFormatado();
    }

    private void gerarMenusInfo(JMenu menuInfo2) {
        JMenuItem resFinal = new JMenuItem("Resultado Corrida") {
            public String getText() {
                return Lang.msg("092");
            }

        };
        resFinal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (controleJogo != null) {
                        exibirResultadoFinal(controleJogo.obterResultadoFinal());
                    }
                } catch (Exception ex) {
                    Logger.logarExept(ex);
                }
            }
        });
        menuInfo2.add(resFinal);
        JMenuItem logs = new JMenuItem("Ver Logs") {
            public String getText() {
                return Lang.msg("267");
            }

        };
        logs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    JTextArea area = new JTextArea(20, 50);
                    Set top = Logger.topExceptions.keySet();
                    for (Iterator iterator = top.iterator(); iterator.hasNext(); ) {
                        String exept = (String) iterator.next();
                        area.append("Quantidade : " + Logger.topExceptions.get(exept));
                        area.append("\n");
                        area.append(exept.replaceAll("<br>", "\n"));
                        area.append("\n");
                    }
                    area.setCaretPosition(0);
                    JOptionPane.showMessageDialog(MainFrame.this, new JScrollPane(area), Lang.msg("listaDeErros"),
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    Logger.logarExept(ex);
                }
            }
        });
        menuInfo2.add(logs);

        JMenuItem ligarLogs = new JMenuItem("ativarLogs") {
            public String getText() {
                return Lang.msg("ativarLogs");
            }

        };
        ligarLogs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Logger.ativo = !Logger.ativo;
            }
        });
        menuInfo2.add(ligarLogs);

    }


    private void gerarMenusSingle(JMenu menu1) {
        iniciar = new JMenuItem("Iniciar Jogo") {
            public String getText() {
                return Lang.msg("094");
            }

        };

        iniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    removerKeyListeners();

                    if (!verificaCriarJogo()) {
                        return;
                    }
                    controleJogo.setMainFrame(MainFrame.this);
                    controleJogo.iniciarJogo();
                } catch (Exception ex) {
                    Logger.logarExept(ex);
                }
            }

        });
        menu1.add(iniciar);

        atualizacaoSuave = new JCheckBoxMenuItem("atualizacaoSuave") {
            public String getText() {
                return Lang.msg("atualizacaoSuave");
            }

        };
        atualizacaoSuave.setSelected(true);
        atualizacaoSuave.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                controleJogo.setAtualizacaoSuave(atualizacaoSuave.isSelected());
            }
        });
        menu1.add(atualizacaoSuave);

        verControles = new JMenuItem("verControles") {
            public String getText() {
                return Lang.msg("verControles");
            }

        };
        verControles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                controleJogo.ativaVerControles();
            }
        });
        menu1.add(verControles);

    }

    private void removerKeyListeners() {
        KeyListener[] listeners = getKeyListeners();
        for (int i = 0; i < listeners.length; i++) {
            removeKeyListener(listeners[i]);
        }
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCoode = e.getKeyCode();
                if (keyCoode == KeyEvent.VK_F1) {
                    mostraDebugFrame();
                }
                super.keyPressed(e);
            }
        });

    }

    public static void main(String[] args) throws IOException {
        if (args != null && args.length > 0) {
            if ("real".equals(args[0])) {
                Util.substVogais = false;
            }
        }
        MainFrame frame = new MainFrame(null);
    }

    public void iniciar() {
        removerListeners();
        setVisible(true);
        try {
            controleJogo = new ControleJogoLocal();
            controleJogo.setMainFrame(this);
            PainelMenuLocal painelMenuSigle = new PainelMenuLocal(this);
            removePainelNarracaoDebug();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
    }

    private void removerListeners() {
        getContentPane().removeAll();
        MouseWheelListener[] mouseWheelListeners = getMouseWheelListeners();
        for (int i = 0; i < mouseWheelListeners.length; i++) {
            removeMouseWheelListener(mouseWheelListeners[i]);
        }
        KeyListener[] keyListeners = getKeyListeners();
        for (int i = 0; i < keyListeners.length; i++) {
            removeKeyListener(keyListeners[i]);
        }
        MouseListener[] mouseListeners = getMouseListeners();
        for (int i = 0; i < mouseListeners.length; i++) {
            removeMouseListener(mouseListeners[i]);
        }
    }

    public void exibirResultadoFinal(PainelTabelaResultadoFinal resultadoFinal) {

        JOptionPane.showMessageDialog(this, new JScrollPane(resultadoFinal), "Resultado Final. ",
                JOptionPane.INFORMATION_MESSAGE);

    }

    public String getCodeBase() {
        if (appletPaddock.getCodeBase() != null) {
            return appletPaddock.getCodeBase().toString();
        }
        return null;
    }

    public void desbilitarMenusModoOnline() {
        if (iniciar != null) {
            iniciar.setEnabled(false);
        }
    }

    public void setControleJogo(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

    public boolean verificaCriarJogo() throws Exception {
        if (controleJogo != null) {
            if (controleJogo.isCorridaIniciada()) {
                int ret = JOptionPane.showConfirmDialog(MainFrame.this, Lang.msg("095"), Lang.msg("094"),
                        JOptionPane.YES_NO_OPTION);
                if (ret == JOptionPane.NO_OPTION) {
                    return false;
                }
            }
            controleJogo.matarTodasThreads();
        }
        controleJogo = new ControleJogoLocal(this);
        controleJogo.setMainFrame(this);
        removePainelNarracaoDebug();
        return true;
    }

    private void removePainelNarracaoDebug() {
        if (debugFrame != null) {
            debugFrame.getContentPane().removeAll();
        }
        adicionouPainelDebug = false;
        adicionouPainelNarracao = false;
    }

    public Campeonato getCampeonato() {
        return campeonato;
    }

    public void setCampeonato(Campeonato campeonato) {
        this.campeonato = campeonato;
    }

    public Graphics2D obterGraficos() {
        BufferStrategy strategy = getBufferStrategy();
        if (strategy == null) {
            createBufferStrategy(2);
            strategy = getBufferStrategy();
        }
        if (strategy == null) {
            return null;
        }
        return (Graphics2D) strategy.getDrawGraphics();
    }

    public void mostrarGraficos() {
        try {
            BufferStrategy strategy = getBufferStrategy();
            if (strategy != null && strategy.getDrawGraphics() != null) {
                strategy.getDrawGraphics().dispose();
                strategy.show();
            }
        } catch (Exception e) {
            Logger.logarExept(e);
        }
    }

    public void mostraDebugFrame() {
        if (debugFrame == null) {
            return;
        }
        if (debugFrame.isVisible()) {
            return;
        }
        debugFrame.setVisible(true);
        debugFrame.setLocation(MainFrame.this.getWidth(), 0);
        posicionaJanelaDebug();
        debugFrame.setLayout(new BorderLayout());
        Thread atualizaDebug = new Thread(new Runnable() {
            @Override
            public void run() {
                while (debugFrame.isVisible()) {
                    try {
                        adicionaPainelNarracaoDebug();
                        if (controleJogo != null) {
                            controleJogo.atualizaInfoDebug();
                        }
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        atualizaDebug.start();
        debugFrame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        debugFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                removePainelNarracaoDebug();
                super.windowClosing(e);
            }
        });
    }

    private void posicionaJanelaDebug() {
        debugFrame.pack();
        debugFrame.setSize(300, MainFrame.this.getHeight());
        debugFrame.setTitle("Fl-Mane Debug");
    }

    private void adicionaPainelNarracaoDebug() {
        JPanel painelNarracao = controleJogo.painelNarracao();
        JPanel painelDebug = controleJogo.painelDebug();
        if (painelNarracao != null && !adicionouPainelNarracao) {
            adicionouPainelNarracao = true;
            debugFrame.add(painelNarracao, BorderLayout.SOUTH);
            debugFrame.setSize(debugFrame.getWidth() + 1, MainFrame.this.getHeight());
        }

        if (painelDebug != null && !adicionouPainelDebug) {
            adicionouPainelDebug = true;
            debugFrame.add(painelDebug, BorderLayout.CENTER);
            debugFrame.setSize(debugFrame.getWidth() + 1, MainFrame.this.getHeight());
        }
    }
}
