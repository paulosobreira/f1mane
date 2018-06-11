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

var contadorJogadores = 0;

var pilotosMap = new Map();
var pilotosMapReta = new Map();
var pilotosDnfMap = new Map();
var pilotosBandeirada = new Map();
var pilotosFaiscaMap = new Map();
var pilotosTravadaMap = new Map();
var pilotosTravadaFumacaMap = new Map();
var pilotosAereofolioMap = new Map();
var idNoAnterior = new Map();
var showFps = false;

var loader = $('<div class="loader"></div>');
$('body').prepend(loader);
var $loading = loader.hide();
var pisca = false;

function cpu_main() {
	if (nomeJogo == null) {
		$loading.show();
		nomeJogo = localStorage.getItem("nomeJogo");
		idPilotoSelecionado = localStorage.getItem("idPilotoSelecionado");
		token = localStorage.getItem("token");
		if (nomeJogo == null) {
			console.log('cpu_main nomeJogo == null');
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
	if (dadosJogo != null && circuito != null && ativo && contCarregouMidia == 0) {
		$loading.hide();
		//MOSTRANDO_QUALIFY 10
		//ESPERANDO_JOGO_COMECAR 07
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
		idNoAnterior.set(piloto.idPiloto, piloto.idNo);
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
	var contadorJogadoresLocal = 0;

	for (var i = 0; i < posicaoPilotos.posis.length; i++) {
		var piloto = posicaoPilotos.posis[i];
		if(piloto.humano){
			contadorJogadoresLocal++;
		}
		// console.log(dadosParciais.estado+' '+piloto.idPiloto+'
		// '+piloto.tracado+' '+piloto.idNo);
		if (piloto.idPiloto == idPilotoSelecionado) {
			posicaoCentraliza = i;
		}
		var no = mapaIdPilotosNosSuave.get(piloto.idPiloto);
		if (no == null) {
			no = mapaIdNos.get(piloto.idNo);
		}
		var status = new String(piloto.status);
		pilotosAereofolioMap.set(piloto.idPiloto, false);
		if (status.startsWith("P")) {
		} else if (status.startsWith("A")) {
			pilotosAereofolioMap.set(piloto.idPiloto, true);
		} else if (status.startsWith("F")) {
			pilotosFaiscaMap.set(piloto.idPiloto, 15);
		} else if (status.startsWith("T")) {
			pilotosTravadaMap.set(piloto.idPiloto, true);
			if (no.tipoJson == 'R') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 10);
			} else if (no.tipoJson == 'A') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 7);
			} else if (no.tipoJson == 'B') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 3);
			}
		} else if(status.startsWith("M")){
			pilotosTravadaMap.set(piloto.idPiloto, true);
		} else if (status.startsWith("R")) {
			pilotosDnfMap.set(piloto.idPiloto, true);
		} else if (status.startsWith("B") && pilotosBandeirada.get(piloto.idPiloto)==null) {
			pilotosBandeirada.set(piloto.idPiloto, (i+1));
		} else if (status.startsWith("BA")) {
			pilotosAereofolioMap.set(piloto.idPiloto, true);
			if(pilotosBandeirada.get(piloto.idPiloto)==null){
				pilotosBandeirada.set(piloto.idPiloto, (i+1));
			}
		}
		no = mapaIdNos.get(piloto.idNo);
		if (no.tipoJson == 'R'){
			var cont = pilotosMapReta.get(piloto.idPiloto);
			if(cont == null){
				cont = 0;
			}
			cont++;
			pilotosMapReta.set(piloto.idPiloto, cont);
		}else{
			pilotosMapReta.set(piloto.idPiloto, null);
		}
		
	}
	if(contadorJogadores != contadorJogadoresLocal){
		console.log(' contadorJogadores ' + contadorJogadores);
		console.log(' contadorJogadoresLocal ' + contadorJogadoresLocal);
		cpu_atualizaJogadores();
	}
	contadorJogadores = contadorJogadoresLocal;
	if (dadosParciais.texto) {
		$('#info').html(dadosParciais.texto);
	}
	// console.log('dadosParciais.estado: ' + dadosParciais.estado);
	if ('24' == dadosParciais.estado) {
		ativo = false;
		clearInterval(main);
		window.location.href = "resultado.html?token=" + token + "&nomeJogo=" + nomeJogo;
	}
}

function cpu_atualizaJogadores(){
	rest_dadosJogo_jogadores(nomeJogo);
}

function cpu_carregaDadosPilotos() {
	for (var i = 0; i < dadosJogo.pilotos.length; i++) {
		pilotosMap.set(dadosJogo.pilotos[i].id, dadosJogo.pilotos[i]);
	}
}

function cpu_sair() {
	rest_sairJogo();
	ativo = false;
	clearInterval(main);
	window.location.href = "index.html?token="+token;
}

function cpu_altenador() {
	if (alternador) {
		alternadorValor++;
		if (alternadorValor > 6) {
			cpu_viradaAlterador();
		}
	} else {
		alternadorValor--;
		if (alternadorValor < -6) {
			cpu_viradaAlterador();
		}
	}
}

function cpu_viradaAlterador() {
	alternador = !alternador;
	vdp_precessaCorCeu();
}

var main = setInterval(cpu_main, 500);

// update canvas with some information and animation
var fps = new FpsCtrl(30, function(e) {
	pisca = !pisca;
	vdp_desenha(fps);
})

vdp_setup();
// start the loop
fps.start();
