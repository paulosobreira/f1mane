package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;

/**
 * @author Paulo Sobreira Criado em 28/07/2007 as 15:51:36
 */
public class ClientPaddockPack implements Serializable {

	private static final long serialVersionUID = 6938384085272885074L;
	private String comando;
	private String nomeJogador;
	private String senhaJogador;
	private String emailJogador;
	private String chaveCapcha;
	private String textoCapcha;
	private SessaoCliente sessaoCliente;
	private String texto;
	private String nomeJogo;
	private String giroMotor;
	private String modoPilotagem;
	private String tpPneuBox;
	private String asaBox;
	private int combustBox;
	private int tracado;
	private boolean recuperar = false;
	private DadosCriarJogo dadosJogoCriado;
	private CarreiraDadosSrv carreiraDadosSrv;
	private Object dataObject;
	private byte[] dataBytes;

	public Object getDataObject() {
		return dataObject;
	}

	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}

	public String getChaveCapcha() {
		return chaveCapcha;
	}

	public void setChaveCapcha(String chaveCapcha) {
		this.chaveCapcha = chaveCapcha;
	}

	public String getTextoCapcha() {
		return textoCapcha;
	}

	public void setTextoCapcha(String textoCapcha) {
		this.textoCapcha = textoCapcha;
	}

	public CarreiraDadosSrv getJogadorDadosSrv() {
		return carreiraDadosSrv;
	}

	public void setJogadorDadosSrv(CarreiraDadosSrv jogadorDadosSrv) {
		this.carreiraDadosSrv = jogadorDadosSrv;
	}

	public int getTracado() {
		return tracado;
	}

	public void setTracado(int tracado) {
		this.tracado = tracado;
	}

	public ClientPaddockPack(String commando, SessaoCliente sessaoCliente) {
		super();
		this.comando = commando;
		this.sessaoCliente = sessaoCliente;
	}

	public String getTexto() {
		return texto;
	}

	public byte[] getDataBytes() {
		return dataBytes;
	}

	public void setDataBytes(byte[] dataBytes) {
		this.dataBytes = dataBytes;
	}

	public String getNomeJogo() {
		return nomeJogo;
	}

	public void setNomeJogo(String nomeJogo) {
		this.nomeJogo = nomeJogo;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public SessaoCliente getSessaoCliente() {
		return sessaoCliente;
	}

	public void setSessaoCliente(SessaoCliente sessaoCliente) {
		this.sessaoCliente = sessaoCliente;
	}

	public ClientPaddockPack() {

	}

	public String getNomeJogador() {
		return nomeJogador;
	}

	public void setNomeJogador(String apelido) {
		this.nomeJogador = apelido;
	}

	public String getComando() {
		return comando;
	}

	public void setComando(String commando) {
		this.comando = commando;
	}

	public DadosCriarJogo getDadosJogoCriado() {
		return dadosJogoCriado;
	}

	public void setDadosCriarJogo(DadosCriarJogo dadosParticiparJogo) {
		this.dadosJogoCriado = dadosParticiparJogo;
	}

	public String getGiroMotor() {
		return giroMotor;
	}

	public void setGiroMotor(String giroMotor) {
		this.giroMotor = giroMotor;
	}

	public int getCombustBox() {
		return combustBox;
	}

	public void setCombustBox(int combustBox) {
		this.combustBox = combustBox;
	}

	public String getTpPneuBox() {
		return tpPneuBox;
	}

	public void setTpPneuBox(String tpPneuBox) {
		this.tpPneuBox = tpPneuBox;
	}

	public String getSenhaJogador() {
		return senhaJogador;
	}

	public void setSenhaJogador(String senhaJogador) {
		this.senhaJogador = senhaJogador;
	}

	public String getEmailJogador() {
		return emailJogador;
	}

	public void setEmailJogador(String emailJogador) {
		this.emailJogador = emailJogador;
	}

	public String getAsaBox() {
		return asaBox;
	}

	public void setAsaBox(String asaBox) {
		this.asaBox = asaBox;
	}

	public String getModoPilotagem() {
		return modoPilotagem;
	}

	public void setModoPilotagem(String modoPilotagem) {
		this.modoPilotagem = modoPilotagem;
	}

	public boolean isRecuperar() {
		return recuperar;
	}

	public void setRecuperar(boolean recuperar) {
		this.recuperar = recuperar;
	}
}
