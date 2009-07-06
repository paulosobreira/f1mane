package sowbreira.f1mane;

import javax.swing.JApplet;

import br.nnpe.Logger;

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
		jPanel1 = new javax.swing.JPanel();
		jButton1 = new javax.swing.JButton();
		jButton1.setText("Iniciar Jogo");
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
