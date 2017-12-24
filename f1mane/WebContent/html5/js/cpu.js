/**
 * Controle central de jogo
 */
var circuito;
var dadosJogo;
var dadosParciais;
var nomeJogo;

var ativo = true;
var token;
var idPilotoSelecionado;
var posicaoCentraliza = 0;

var alternador = true;
var alternadorValor = 0;

var pilotosMap = new Map();
var pilotosDnfMap = new Map();
var pilotosFaiscaMap = new Map();
var pilotosTravadaMap = new Map();
var pilotosTravadaFumacaMap = new Map();
var pilotosAereofolioMap = new Map();
var ptsPistaMap = new Map();
var ptsPistaMapAnterior = new Map();

var loader = $('<div class="loader"></div>');
$('body').prepend(loader);
var $loading = loader.hide();

function cpu_main() {
	if (nomeJogo == null) {
		$loading.show();
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
	if (dadosJogo != null && circuito != null && !carregouMidia) {
		mid_caregaMidia();
		carregouMidia = true;
		ctl_gerarControles();
		contCarregouMidia = 10;
	}
	if (dadosJogo != null && circuito != null && ativo
			&& contCarregouMidia == 0) {
		$loading.hide();
		if ('07' == dadosJogo.estado || '10' == dadosJogo.estado) {
			rest_dadosJogo(nomeJogo);
		} else {
			rest_dadosParciais();
		}
		cpu_altenador();
	}
	if (carregouMidia && contCarregouMidia > 0) {
		if (imgBg != null && !imgBg.complete) {
			contCarregouMidia = 5;
		} else {
			contCarregouMidia--;
		}
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
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace(
					"P", "")));
		} else if (status.startsWith("T")) {
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace(
					"T", "")));
		} else if (status.startsWith("F")) {
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace(
					"F", "")));
		} else if (status.startsWith("A")) {
			ptsPistaMapAnterior.set(piloto.idPiloto, parseInt(status.replace(
					"A", "")));
		}
	}
}

function cpu_dadosParciais() {
	if (!dadosParciais) {
		return;
	}
	if (dadosParciais.posisPack.safetyNoId != 0) {
		var sc = {
			idPiloto : 'SC',
			idNo : dadosParciais.posisPack.safetyNoId,
			tracado : dadosParciais.posisPack.safetyTracado,
			status : 'SC'
		};
		dadosParciais.posisPack.posis.push(sc);
	}

	var posicaoPilotos = dadosParciais.posisPack;

	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
//		console.log(piloto.idPiloto+'  '+piloto.tracado+'  '+piloto.idNo);
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
		} else if (status.startsWith("T")) {
			ptsPistaMap.set(piloto.idPiloto, parseInt(status.replace("T", "")));
			pilotosTravadaMap.set(piloto.idPiloto, true);
			pilotosTravadaFumacaMap.set(piloto.idPiloto, 10);
		} else if (status.startsWith("R")) {
			pilotosDnfMap.set(piloto.idPiloto, true);
		}
	}
	if (dadosParciais.texto) {
		$('#info').html(dadosParciais.texto);
	}
	// console.log('dadosParciais.estado: ' + dadosParciais.estado);
	if ('24' == dadosParciais.estado) {
		ativo = false;
		clearInterval(main);
		window.location.href = "resultado.html?token=" + token + "&nomeJogo="
				+ nomeJogo;
	}
}

function cpu_carregaDadosPilotos() {
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		pilotosMap.set(dadosJogo.pilotos[i].id, dadosJogo.pilotos[i]);
	}
}

function cpu_sair() {
	ativo = false;
	clearInterval(main);
	window.location.href = "index.html";
}

function cpu_altenador() {
	if (alternador) {
		alternadorValor++;
		if (alternadorValor > 6) {
			alternador = false;
		}
	} else {
		alternadorValor--;
		if (alternadorValor < -6) {
			alternador = true;
		}
	}
}

var main = setInterval(cpu_main, 350);

// update canvas with some information and animation
var fps = new FpsCtrl(30, function(e) {
	vdp_desenha(fps);
})

// start the loop
fps.start();
