/**
 * Controle central de jogo
 */
var circuito;
var dadosJogo;
var dadosParciais;
var nomeJogo;

var ativo = true;
var delay = 500;

var token;
var idPilotoSelecionado;
var posicaoCentraliza = 0;

var alternador = true;
var alternadorValor = 0;

var pilotosMap = new Map();
var pilotosDnfMap = new Map();
var pilotosFaiscaMap = new Map();
var pilotosAereofolioMap = new Map();
var carrosImgMap;
var carrosImgSemAereofolioMap;
var capaceteImgMap;
var ptsPistaMap = new Map();
var ptsPistaMapAnterior = new Map();
var objImgPistaMap = new Map();
var carrosLadoImgMap;
var imgPneuM, imgPneuD, imgPneuC;
var menosAsa, maisAsa, normalAsa;
var motor, capacete;

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
			alert('nomeJogo == null');
			window.location.href = "index.html";
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
		cpu_caregaMidia();
		vdp_carregaBackGround();
	}
	if (dadosJogo != null && circuito != null && ativo && imgBg.complete) {
		rest_dadosParciais();
		cpu_altenador();
	} 
}

function cpu_caregaMidia() {
	imgPneuM = new Image();
	imgPneuM.src = "img/pneuMole.png"
	imgPneuD = new Image();
	imgPneuD.src = "img/pneuDuro.png"
	imgPneuC = new Image();
	imgPneuC.src = "img/pneuChuva.png"
	motor = new Image();
	motor.src = "img/motor.png"
	capacete = new Image();
	capacete.src = "img/capacete.png"
	menosAsa = new Image();
	menosAsa.src = "/f1mane/rest/letsRace/png/menosAsa";
	maisAsa = new Image();
	maisAsa.src = "/f1mane/rest/letsRace/png/maisAsa";
	normalAsa = new Image();
	normalAsa.src = "/f1mane/rest/letsRace/png/normalAsa"

	ctl_gerarControles();
	console.log('cpu_main vdp_carregaBackGround()');

	for (var i = 0; i < circuito.objetosNoTransparencia.length; i++) {
		var img = new Image();
		img.src = "/f1mane/rest/letsRace/objetoPista/" + dadosJogo.arquivoCircuito + "/" + i;
		objImgPistaMap.set(i, img);
	}

}

function cpu_dadosParciaisAnterior() {
	if (!dadosParciais) {
		return;
	}
	var posicaoPilotosAnt = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotosAnt.posis.length; i++) {
		var piloto = posicaoPilotosAnt.posis[i];
		var status = new String(piloto.status);
		if (status.startsWith("P")) {
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace("P", "")));
		} else if (status.startsWith("A")) {
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace("A", "")));
		}
	}
}

function cpu_dadosParciais() {
	if (!dadosParciais) {
		return;
	}
	var posicaoPilotos = dadosParciais.posisPack;
	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		if (piloto.idPiloto == idPilotoSelecionado) {
			posicaoCentraliza = i;
		}
		var status = new String(piloto.status);
		pilotosAereofolioMap.set(piloto.idPiloto, false);
		if (status.startsWith("P")) {
			ptsPistaMap.set(piloto.idPiloto, parseInt(status.replace("P", "")));
		} else if (status.startsWith("A")) {
			ptsPistaMap.set(piloto.idPiloto, parseInt(status.replace("A", "")));
			pilotosAereofolioMap.set(piloto.idPiloto, true);
		} else if (status.startsWith("F")) {
			ptsPistaMap.set(piloto.idPiloto, parseInt(status.replace("F", "")));
			pilotosFaiscaMap.set(piloto.idPiloto, 15);
		}else if (status.startsWith("R")) {
			pilotosDnfMap.set(piloto.idPiloto, true);
		}
	}
	if (dadosParciais.texto) {
		$('#info').html(dadosParciais.texto);
	}
	// console.log('dadosParciais.estado: ' + dadosParciais.estado);
	if ('24' == dadosParciais.estado) {
		ativo = false;
		window.location.href = "resultado.html?token=" + token + "&nomeJogo=" + nomeJogo;
	}
}

function cpu_carregaDadosPilotos() {
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		pilotosMap.set(dadosJogo.pilotos[i].id, dadosJogo.pilotos[i]);
	}
	carrosImgMap = new Map();
	carrosImgSemAereofolioMap = new Map();
	carrosLadoImgMap = new Map();
	capaceteImgMap = new Map();
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		var pilotos = dadosJogo.pilotos[i];
		var imgCarro = new Image();
		imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
		carrosImgMap.set(pilotos.id, imgCarro);

		var imgSemAereofolio = new Image();
		imgSemAereofolio.src = "/f1mane/rest/letsRace/carroCimaSemAreofolio?nomeJogo=" + dadosJogo.nomeJogo + "&idPiloto=" + pilotos.id;
		carrosImgSemAereofolioMap.set(pilotos.id, imgSemAereofolio);

		var imgCarroLado = new Image();
		imgCarroLado.src = "/f1mane/rest/letsRace/carroLado?id=" + pilotos.id + "&temporada=" + dadosJogo.temporada;
		carrosLadoImgMap.set(pilotos.id, imgCarroLado);

		var imgCapacete = new Image();
		imgCapacete.src = "/f1mane/rest/letsRace/capacete?id=" + pilotos.id + "&temporada=" + dadosJogo.temporada
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
var render = setInterval(vdp_desenha, 30);