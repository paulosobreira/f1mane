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
    private final ControlePaddockCliente controlePaddockCliente;

    public FormEntrada(ControlePaddockCliente controlePaddockCliente) {
        this.controlePaddockCliente = controlePaddockCliente;
        setLayout(new BorderLayout());
        JPanel panelAbaEntrar = new JPanel(new BorderLayout(15, 15));
        JPanel panelabelEntrarCenter = new JPanel(new BorderLayout());
        panelabelEntrarCenter.add(gerarLogin(), BorderLayout.NORTH);
        panelAbaEntrar.add(panelabelEntrarCenter, BorderLayout.CENTER);
        add(panelAbaEntrar, BorderLayout.CENTER);
        setSize(300, 300);
        setVisible(true);
    }

    private JPanel gerarLogin() {
        JPanel panel = new JPanel();
        GridLayout gridLayout = new GridLayout(2, 2);
        panel.setBorder(new TitledBorder("Entrar") {
            @Override
            public String getTitle() {
                return Lang.msg("171");
            }
        });
        panel.setLayout(gridLayout);
        panel.add(new JLabel("Entre com seu Nome") {
            public String getText() {
                return Lang.msg("nomeJogadorSessao");
            }
        });
        panel.add(nomeLogar);
        return panel;
    }

    public void setNome(JTextField nome) {
        this.nomeLogar = nome;
    }

    public String getNome() {
        return this.nomeLogar.getText();
    }


    public static void main(String[] args) throws FileNotFoundException {
    }


}
