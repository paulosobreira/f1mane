package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class PainelCampeonato extends JPanel {

	private Campeonato campeonato;

	private MainFrame mainFrame;

	public PainelCampeonato(Campeonato campeonato, MainFrame mainFrame) {
		super();
		this.campeonato = campeonato;
		this.mainFrame = mainFrame;
		this.setLayout(new BorderLayout());
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(3, 2));

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("272");
			}
		});
		p1.add(new JLabel(campeonato.getTemporada()));

		p1.add(new JLabel() {
			public String getText() {
				return Lang.msg("212");
			}
		});
		p1.add(new JLabel(campeonato.getNivel()));

		p1.add(new JLabel() {

			public String getText() {
				return Lang.msg("110");
			}
		});
		p1.add(new JLabel(campeonato.getQtdeVoltas().toString()));
		this.add(p1);
		JOptionPane.showMessageDialog(mainFrame, p1);

	}
}
