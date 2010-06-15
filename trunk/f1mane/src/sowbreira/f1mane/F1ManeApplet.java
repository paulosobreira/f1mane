package sowbreira.f1mane;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JLabel;

import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado Em 12:05:02
 */
public class F1ManeApplet extends JApplet {
	private javax.swing.JButton jButton1;
	private javax.swing.JPanel jPanel1;
	private JLabel jLabel1;
	private JLabel jLabel2;

	public void init() {
		initComponents();
	}

	private void initComponents() {
		String param = getParameter("lang");
		if (!Util.isNullOrEmpty(param)) {
			Lang.mudarIdioma(param);
		}
		jPanel1 = new javax.swing.JPanel();
		jLabel1 = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();

		jButton1 = new javax.swing.JButton() {
			@Override
			public String getText() {
				return Lang.msg("297");
			}
		};

		jPanel1.setLayout(new java.awt.BorderLayout());

		jLabel1.setText("F1-Mane");
		jPanel1.add(jLabel1, java.awt.BorderLayout.NORTH);
		CarregadorRecursos carregadorImagens = new CarregadorRecursos(false);

		ImageIcon icon = new ImageIcon(getCodeBase() + "f1mane.jpg");
		jLabel2.setIcon(icon);
		jLabel2.setSize(icon.getIconWidth(), icon.getIconHeight());
		jPanel1.add(jLabel2, java.awt.BorderLayout.CENTER);

		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		jPanel1.add(jButton1, java.awt.BorderLayout.SOUTH);

		getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {

		try {
			final MainFrame frame = new MainFrame(true);
			frame.setVisible(true);
			frame.setSize(810, 650);

		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}
}
