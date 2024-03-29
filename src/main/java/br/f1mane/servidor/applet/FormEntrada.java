package br.f1mane.servidor.applet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.recursos.idiomas.Lang;

public class FormEntrada extends JPanel {
    private final JComboBox comboIdiomas = new JComboBox(new String[]{Lang.msg("pt"), Lang.msg("en")});
    private JTextField nomeLogar = new JTextField(20);
    private final JTextField nomeRegistrar = new JTextField(20);
    private final JTextField nomeRecuperar = new JTextField(20);
    private final JTextField emailRegistrar = new JTextField(20);
    private final JPasswordField senha = new JPasswordField(20);
    private int conta1;
    private int conta2;
    private final JTextField resultadorConta = new JTextField(20);

    private final ControlePaddockCliente controlePaddockCliente;

    private final JLabel senhaLabel = new JLabel("Senha") {
        public String getText() {
            return Lang.msg("senha");
        }
    };


    public FormEntrada(ControlePaddockCliente controlePaddockCliente) {
        this.controlePaddockCliente = controlePaddockCliente;
        setLayout(new BorderLayout());
        JTabbedPane jTabbedPane = new JTabbedPane();
        JPanel panelAbaEntrar = new JPanel(new BorderLayout(15, 15));
        JPanel panelabaEntrarCenter = new JPanel(new BorderLayout());
        panelabaEntrarCenter.add(gerarLogin(), BorderLayout.NORTH);
        panelAbaEntrar.add(panelabaEntrarCenter, BorderLayout.CENTER);
        jTabbedPane.addTab(Lang.msg("171"), panelAbaEntrar);
        JPanel panelAbaRegistrar = new JPanel(new BorderLayout());
        panelAbaRegistrar.add(gerarRegistrar(), BorderLayout.CENTER);
        jTabbedPane.addTab(Lang.msg("registrar"), panelAbaRegistrar);
        add(jTabbedPane, BorderLayout.CENTER);
        add(gerarIdiomas(), BorderLayout.SOUTH);
        jTabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                nomeRegistrar.setText("");
                nomeRecuperar.setText("");
                emailRegistrar.setText("");
            }
        });
        setSize(300, 300);
        setVisible(true);

    }

    private JPanel gerarIdiomas() {
        comboIdiomas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                Logger.logar(Lang.key(comboIdiomas.getSelectedItem().toString()));
                String i = Lang.key(comboIdiomas.getSelectedItem().toString());
                if (i != null && !i.isEmpty()) {
                    Lang.mudarIdioma(i);
                    comboIdiomas.removeAllItems();
                    comboIdiomas.addItem(Lang.msg("pt"));
                    comboIdiomas.addItem(Lang.msg("en"));
                }
                FormEntrada.this.repaint();
                comboIdiomas.setSelectedItem(Lang.msg(i));
            }
        });
        JPanel langPanel = new JPanel(new BorderLayout());
        langPanel.setBorder(new TitledBorder("Idiomas") {
            public String getTitle() {
                return Lang.msg("219");
            }
        });
        langPanel.add(comboIdiomas, BorderLayout.CENTER);

        return langPanel;
    }

    private JPanel gerarRegistrar() {
        JPanel registrarPanel = new JPanel(new GridLayout(6, 2));
        registrarPanel.setBorder(new TitledBorder("Registrar") {
            public String getTitle() {
                return Lang.msg("218");
            }
        });
        registrarPanel.add(new JLabel("Entre com seu Nome") {
            public String getText() {
                return Lang.msg("167");
            }
        });
        registrarPanel.add(nomeRegistrar);
        registrarPanel.add(new JLabel("Entre com seu e-mail") {
            public String getText() {
                return Lang.msg("168");
            }
        });
        registrarPanel.add(emailRegistrar);
        conta1 = Util.intervalo(0, 10);
        conta2 = Util.intervalo(0, 10);
        registrarPanel.add(new JLabel("Conta Facil") {
            public String getText() {
                return Lang.msg("contaFacil", new String[]{"" + conta1, "" + conta2});
            }
        });
        registrarPanel.add(resultadorConta);
        JPanel newPanel = new JPanel(new BorderLayout());
        newPanel.add(registrarPanel, BorderLayout.NORTH);
        return newPanel;
    }

    private JPanel gerarLogin() {
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(4, 2);
        panel.setBorder(new TitledBorder("Entrar") {
            @Override
            public String getTitle() {
                return Lang.msg("171");
            }
        });
        panel.setLayout(gridLayout);
        panel.add(new JLabel("Entre com seu Nome") {
            public String getText() {
                return Lang.msg("entreNomeOuEmail");
            }
        });
        panel.add(nomeLogar);
        panel.add(senhaLabel);
        panel.add(senha);
        return panel;
    }

    public JTextField getNome() {
        if (!Util.isNullOrEmpty(nomeRegistrar.getText()))
            return nomeRegistrar;
        return nomeLogar;
    }

    public void setNome(JTextField nome) {
        this.nomeLogar = nome;
    }

    public JPasswordField getSenha() {
        return senha;
    }

    public static void main(String[] args) throws FileNotFoundException {
        // FileOutputStream fileOutputStream = new
        // FileOutputStream("teste.xml");
        // XMLEncoder encoder = new XMLEncoder(fileOutputStream);
        // String teste = "HandlerFactory";
        // encoder.writeObject(teste);
        // encoder.flush();
        // encoder.close();
        FormEntrada formEntrada = new FormEntrada(null);
        formEntrada.setToolTipText(Lang.msg("066"));
        int result = JOptionPane.showConfirmDialog(null, formEntrada, Lang.msg("066"), JOptionPane.OK_CANCEL_OPTION);

        if (JOptionPane.OK_OPTION == result) {
            Logger.logar("ok");
        }
    }

    public JTextField getNomeRegistrar() {
        return nomeRegistrar;
    }

    public JTextField getNomeRecuperar() {
        return nomeRecuperar;
    }

    public JTextField getEmailRegistrar() {
        return emailRegistrar;
    }


    public int getConta1() {
        return conta1;
    }

    public int getConta2() {
        return conta2;
    }

    public JTextField getResultadorConta() {
        return resultadorConta;
    }

}
