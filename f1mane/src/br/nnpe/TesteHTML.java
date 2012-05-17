package br.nnpe;
import java.awt.Color;
import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleConstants.ColorConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.MinimalHTMLWriter;
import javax.swing.text.html.HTML.Attribute;
import javax.swing.text.html.HTMLDocument.HTMLReader;
import javax.swing.text.html.HTMLDocument.RunElement;

public class TesteHTML {

	public static void main(String[] args) throws IOException,
			BadLocationException {

		JEditorPane infoTextual = new JEditorPane("text/html", "");

		StringReader reader = new StringReader(
				"<font id='fonte' color=red> Teste </font>");
		// infoTextual.read(reader, "");
		// JFrame frame = new JFrame();
		// frame.getContentPane().add(infoTextual);
		// frame.setSize(300, 200);
		// frame.setVisible(true);
		HTMLDocument document = new HTMLDocument();
		HTMLEditorKit editorKit = new HTMLEditorKit();
		editorKit.read(reader, document, 0);
		StringWriter writer = new StringWriter();
		editorKit.write(writer, document, 0, document.getLength());
		Element[] rootElements = document.getRootElements();
		System.out.println("");
	}
}
