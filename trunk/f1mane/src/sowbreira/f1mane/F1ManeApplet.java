package sowbreira.f1mane;

import javax.swing.JApplet;

import sowbreira.f1mane.recursos.idiomas.Lang;

import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado Em 12:05:02
 */
public class F1ManeApplet extends JApplet {
	private javax.swing.JButton jButton1;
	private javax.swing.JPanel jPanel1;

	public void init() {
		initComponents();
	}

	private void initComponents() {
		String param = getParameter("lang");
		if (!Util.isNullOrEmpty(param)) {
			Lang.mudarIdioma(param);
		}
		jPanel1 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton() {
			@Override
			public String getText() {
				return Lang.msg("297");
			}
		};
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		jPanel1.add(jButton1);
		getContentPane().add(jPanel1);
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
