package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import br.nnpe.Logger;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Default;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class PainelCampeonato extends JPanel {

	private Campeonato campeonato;

	private MainFrame mainFrame;

	protected ControleJogoLocal controleJogo;

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
				return Lang.msg("284");
			}
		});
		p1.add(new JLabel(campeonato.getTemporada()));

		p1.add(new JLabel() {
			public String getText() {
				return Lang.msg("191");
			}
		});
		p1.add(new JLabel(campeonato.getNivel()));

		p1.add(new JLabel() {

			public String getText() {
				return Lang.msg("285");
			}
		});

		DefaultListModel jogListModel = new DefaultListModel();
		for (Iterator iterator = campeonato.getPilotos().iterator(); iterator
				.hasNext();) {
			String jogador = (String) iterator.next();
			jogListModel.addElement(jogador);

		}
		JList jogadores = new JList(jogListModel);
		jogadores.setEnabled(false);
		JScrollPane jogPane = new JScrollPane(jogadores);
		jogPane.setBorder(new TitledBorder(Lang.msg("")));

		p1.add(new JLabel(campeonato.getQtdeVoltas().toString()));
		this.add(p1, BorderLayout.CENTER);

		JPanel p2 = new JPanel();
		p2.add(jogPane, BorderLayout.CENTER);
		this.add(p2, BorderLayout.SOUTH);

		int ret = JOptionPane.showConfirmDialog(mainFrame, this, Lang
				.msg("286"), JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (ret != JOptionPane.NO_OPTION) {
			try {
				controleJogo = new ControleJogoLocal("t"
						+ PainelCampeonato.this.campeonato.getTemporada());
				controleJogo.setMainFrame(PainelCampeonato.this.mainFrame);
				controleJogo.iniciarJogo(PainelCampeonato.this.campeonato);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}
	}
}
