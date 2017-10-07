/**
 * Controle central de jogo
 */
var circuito;
var dadosJogo;
var dadosParciais;
var nomeJogo;

var ativo = true;
var delay = 100;

var token;
var idPilotoSelecionado;
var posicaoCentraliza = 0;

var alternador = true;
var alternadorValor = 0;

var pilotosMap = new Map();
var carrosImgMap;
var capaceteImgMap;
var ptsPistaMap = new Map();
var carrosLadoImgMap;
var imgPneuM,imgPneuD,imgPneuC;

// update canvas with some information and animation
// var fps = new FpsCtrl(20, function(e) {
// vdp_desenha();
// })

// start the loop
// fps.start();

function cpu_main() {
	if (nomeJogo == null) {
		nomeJogo = localStorage.getItem("nomeJogo");
		idPilotoSelecionado = localStorage.getItem("idPilotoSelecionado");
		token = localStorage.getItem("token");
		if (nomeJogo == null) {
			console.log('nomeJogo==null');
			return;
		}
	}
	if (dadosJogo == null) {
		rest_dadosJogo(nomeJogo);
	}
	if (dadosJogo != null && circuito == null) {
		cpu_carregaDadosPilotos();
		console.log('cpu_main rest_ciruito()');
		rest_ciruito();
	}
	if (dadosJogo != null && circuito != null && imgBg.src == "") {
		imgPneuM = new Image();
		imgPneuM.src = "img/pneuMole.png"
		imgPneuD = new Image();
		imgPneuD.src = "img/pneuDuro.png"
		imgPneuC = new Image();
		imgPneuC.src = "img/pneuChuva.png"
		console.log('cpu_main vdp_carregaBackGround()');
		ctl_gerarControles();
		vdp_carregaBackGround();
	}
	if (dadosJogo != null && circuito != null && ativo && imgBg.complete) {
		delay = 500;
		rest_dadosParciais();
		cpu_dadosParciais();
		vdp_desenha();
		ctl_desenha();
		cpu_altenador();
	} else {
		console.log('cpu_main dadosJogo:' + dadosJogo + ' circuito:' + circuito
				+ ' ativo:' + ativo + ' imgBg.complete:' + imgBg.complete);
		delay = 100;
	}
}


function cpu_dadosParciais(){
	if (!dadosParciais) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		if (piloto.idPiloto == idPilotoSelecionado) {
			posicaoCentraliza = i;
		}
		var status = new String(piloto.status);
		if (status.startsWith("P")) {
			ptsPistaMap.set(piloto.idPiloto, parseInt(status.replace(
					"P", "")));
		}
	}
	if(dadosParciais.texto){
		console.log('dadosParciais.texto: '+dadosParciais.texto);				
	}
}

function cpu_carregaDadosPilotos(){
	for (i = 0; i < dadosJogo.pilotos.length; i++) {
		pilotosMap.set(dadosJogo.pilotos[i].id, dadosJogo.pilotos[i]);
	}
	carrosImgMap = new Map();
	carrosLadoImgMap = new Map();
	capaceteImgMap = new Map();
	for (i = 0; i < dadosJogo.pilotos.length; i++) {
		var pilotos = dadosJogo.pilotos[i];
		var imgCarro = new Image();
		imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo="
				+ dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
		carrosImgMap.set(pilotos.id, imgCarro);
		var imgCarroLado = new Image();
		imgCarroLado.src = "/f1mane/rest/letsRace/carroLado?id="
				+ pilotos.id + "&temporada=" + dadosJogo.temporada;
		carrosLadoImgMap.set(pilotos.id, imgCarroLado);
		
		var imgCapacete = new Image();
		imgCapacete.src = "/f1mane/rest/letsRace/capacete?id="
			+ pilotos.id  + "&temporada=" + dadosJogo.temporada
		if(imgCapacete.width==0){
			imgCapacete = null;
		}
		capaceteImgMap.set(pilotos.id, imgCapacete);
	}
}


function cpu_altenador() {
	if (alternador) {
		alternadorValor++;
		if (alternadorValor > 20) {
			alternador = false;
		}
	} else {
		alternadorValor--;
		if (alternadorValor < -20) {
			alternador = true;
		}
	}
}

var main = setInterval(cpu_main, delay);