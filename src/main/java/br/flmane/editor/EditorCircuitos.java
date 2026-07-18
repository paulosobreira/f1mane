package br.flmane.editor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import br.flmane.controles.InterfaceJogo;
import br.flmane.recursos.CarregadorRecursos;

/**
 * @author Paulo Sobreira Created on 14/06/2014
 */
public class EditorCircuitos extends JFrame {
    /**
     *
     */
    private static final long serialVersionUID = -284357233387917389L;
    private MainPanelEditor editor;
    private InterfaceJogo controleJogo;

    protected boolean altApertado;
    protected boolean shiftApertado;

    public InterfaceJogo getControleJogo() {
        return controleJogo;
    }

    public EditorCircuitos() throws IOException {
        String title = "Fl-MANE " + getVersao() + " MANager & Engineer Editor";
        setTitle(title);
        removerListeners();
        removerKeyListeners();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1024, 768);
        try {
            editor = new MainPanelEditor(this);
            editor.iniciarComNavegacao();
            ativarKeysEditor();
        } catch (Exception e1) {
            e1.printStackTrace();
            dialogDeErro(e1);
        }
        this.setVisible(true);
    }

    private String getVersao() {
        return CarregadorRecursos.getVersaoFormatado();
    }

    public void dialogDeErro(Exception e) {
        StackTraceElement[] trace = e.getStackTrace();
        StringBuilder retorno = new StringBuilder();
        int size = ((trace.length > 10) ? 10 : trace.length);

        for (int i = 0; i < size; i++)
            retorno.append(trace[i]).append("\n");
        JOptionPane.showMessageDialog(null, retorno.toString(),
                "Conexão Perdida. Erro enviando dados.", JOptionPane.ERROR_MESSAGE);

    }

    private void ativarKeysEditor() {
        removerKeyListeners();
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();
                if (editor == null) {
                    return;
                }
                if (keyCode == KeyEvent.VK_F8) {
                    try {
                        editor.salvarPista();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                if (e.isControlDown() && keyCode == KeyEvent.VK_Z) {
                    editor.desfazerUltimaInclusao();
                    return;
                }
                if (e.isAltDown()) {
                    altApertado = true;
                    if (keyCode == KeyEvent.VK_LEFT) {
                        editor.esquerdaObj();
                    } else if (keyCode == KeyEvent.VK_RIGHT) {
                        editor.direitaObj();
                    } else if (keyCode == KeyEvent.VK_UP) {
                        editor.cimaObj();
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        editor.baixoObj();
                    } else if (keyCode == KeyEvent.VK_C) {
                        editor.copiarObjeto();
                    }
                    return;
                } else {
                    altApertado = false;
                }
                if (e.isShiftDown()) {
                    shiftApertado = true;
                    if (keyCode == KeyEvent.VK_RIGHT) {
                        editor.maisLargura();
                    } else if (keyCode == KeyEvent.VK_LEFT) {
                        editor.menosLargura();
                    } else if (keyCode == KeyEvent.VK_UP) {
                        editor.maisAltura();
                    } else if (keyCode == KeyEvent.VK_DOWN) {
                        editor.menosAltura();
                    }
                    return;
                } else {
                    shiftApertado = false;
                }
                if (keyCode == KeyEvent.VK_LEFT) {
                    editor.esquerda();
                } else if (keyCode == KeyEvent.VK_RIGHT) {
                    editor.direita();
                } else if (keyCode == KeyEvent.VK_UP) {
                    editor.cima();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    editor.baixo();
                } else if (keyCode == KeyEvent.VK_Z) {
                    editor.menosAngulo();
                } else if (keyCode == KeyEvent.VK_X) {
                    editor.maisAngulo();
                } else if (keyCode == KeyEvent.VK_PAGE_UP) {
                    editor.subirNivelObjeto();
                } else if (keyCode == KeyEvent.VK_PAGE_DOWN) {
                    editor.descerNivelObjeto();
                } else if (keyCode == KeyEvent.VK_DELETE) {
                    editor.apagarObjetoSelecionado();
                } else if (keyCode == KeyEvent.VK_INSERT) {
                    editor.iniciarCriacaoObjeto();
                }
            }
        });
    }

    public boolean isAltApertado() {
        return altApertado;
    }

    public boolean isShiftApertado() {
        return shiftApertado;
    }

    private void removerKeyListeners() {
        KeyListener[] listeners = getKeyListeners();
        for (int i = 0; i < listeners.length; i++) {
            removeKeyListener(listeners[i]);
        }
    }

    public static void main(String[] args) throws IOException {
        EditorCircuitos frame = new EditorCircuitos();
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

    public void setControleJogo(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

}
