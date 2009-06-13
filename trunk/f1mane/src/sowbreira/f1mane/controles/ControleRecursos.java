package sowbreira.f1mane.controles;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;

/**
 * @author Paulo Sobreira
 */
public abstract class ControleRecursos {
	protected Circuito circuito;
	protected List pilotos = new ArrayList();
	protected List nosDaPista = new ArrayList();
	protected List nosDoBox = new ArrayList();
	protected List carros;
	protected Map circuitos = new HashMap();
	protected Map mapaIdsNos = new HashMap();
	protected Map mapaNosIds = new HashMap();
	private String temporarada;

	public Map getMapaIdsNos() {
		return mapaIdsNos;
	}

	public void setMapaIdsNos(Map mapaIdsNos) {
		this.mapaIdsNos = mapaIdsNos;
	}

	public Map getMapaNosIds() {
		return mapaNosIds;
	}

	public void setMapaNosIds(Map mapaNosIds) {
		this.mapaNosIds = mapaNosIds;
	}

	public ControleRecursos(String temporada) throws Exception {
		if (temporada != null) {
			this.temporarada = temporada;
		} else {
			this.temporarada = "t2009";
		}
		carros = carregarListaCarros();
		pilotos = carregarListaPilotos();
		ligarPilotosCarros();
		carregarCircuitos();
	}

	public void carregaRecursos(String circuitoStr) throws Exception {

		CarregadorRecursos rec = new CarregadorRecursos();
		ObjectInputStream ois = new ObjectInputStream(rec.getClass()
				.getResourceAsStream(circuitoStr));

		circuito = (Circuito) ois.readObject();
		nosDaPista = circuito.geraPontosPista();
		nosDoBox = circuito.geraPontosBox();
		int contId = 1;
		for (Iterator iter = nosDaPista.iterator(); iter.hasNext();) {
			No noPsita = (No) iter.next();
			Integer pistaId = new Integer(contId++);
			mapaIdsNos.put(pistaId, noPsita);
			mapaNosIds.put(noPsita, pistaId);
		}
		for (Iterator iter = nosDoBox.iterator(); iter.hasNext();) {
			No noDoBox = (No) iter.next();
			Integer boxId = new Integer(contId++);
			mapaIdsNos.put(boxId, noDoBox);
			mapaNosIds.put(noDoBox, boxId);
		}
	}

	protected List carregarListaPilotos() throws IOException {
		List retorno = new ArrayList();
		Properties properties = new Properties();

		properties.load(CarregadorRecursos.recursoComoStream("properties/"
				+ temporarada + "/pilotos.properties"));

		Enumeration propNames = properties.propertyNames();
		int cont = 1;
		while (propNames.hasMoreElements()) {
			Piloto piloto = new Piloto();
			piloto.setId(cont++);
			String name = (String) propNames.nextElement();
			String prop = properties.getProperty(name);
			piloto.setNome(name);
			piloto.setNomeCarro(prop.split(",")[0]);
			piloto.setHabilidade(Integer.parseInt((prop.split(",")[1]))
					+ (Math.random() > .5 ? -1 : 1));
			retorno.add(piloto);
		}

		Collections.sort(retorno, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;

				return new Integer(piloto1.getHabilidade())
						.compareTo(new Integer(piloto0.getHabilidade()));
			}
		});

		return retorno;
	}

	protected List carregarListaCarros() throws IOException {
		List retorno = new ArrayList();
		Properties properties = new Properties();

		properties.load(CarregadorRecursos.recursoComoStream("properties/"
				+ temporarada + "/carros.properties"));

		Enumeration propNames = properties.propertyNames();

		while (propNames.hasMoreElements()) {
			Carro carro = new Carro();
			String name = (String) propNames.nextElement();
			String prop = properties.getProperty(name);
			carro.setNome(name);
			String[] values = prop.split(",");
			carro.setPotencia(Integer.parseInt(values[0]));

			String red = values[1];
			String green = values[2];
			String blue = values[3];
			carro.setImg("carros/" + temporarada + "/" + values[4]);
			carro.setCor1(new Color(Integer.parseInt(red), Integer
					.parseInt(green), Integer.parseInt(blue)));

			red = values[5];
			green = values[6];
			blue = values[7];
			carro.setCor2(new Color(Integer.parseInt(red), Integer
					.parseInt(green), Integer.parseInt(blue)));

			retorno.add(carro);
		}

		return retorno;
	}

	protected void ligarPilotosCarros() {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();

			for (Iterator iterator = carros.iterator(); iterator.hasNext();) {
				Carro carro = (Carro) iterator.next();

				if (piloto.getNomeCarro().equals(carro.getNome())) {
					piloto.setCarro(criarCopiaCarro(carro, piloto));
				}
			}
		}
	}

	protected Carro criarCopiaCarro(Carro carro, Piloto piloto) {
		Carro carroNovo = new Carro();
		carroNovo.setNome(carro.getNome());
		carroNovo.setCor1(carro.getCor1());
		carroNovo.setCor2(carro.getCor2());
		carroNovo.setImg(carro.getImg());
		carroNovo.setPiloto(piloto);
		carroNovo.setPotencia(carro.getPotencia()
				+ (Math.random() > .5 ? -5 : 5));

		return carroNovo;
	}

	protected void definirHabilidadePadraoPilotos(int value) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.setHabilidade(value);
		}
	}

	protected void definirPotenciaPadraoCarros(int value) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.getCarro().setPotencia(value);
		}
	}

	protected void carregarCircuitos() {
		final Properties properties = new Properties();

		try {
			properties.load(CarregadorRecursos
					.recursoComoStream("properties/pistas.properties"));

			Enumeration propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String name = (String) propName.nextElement();
				circuitos.put(name, properties.getProperty(name));

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
