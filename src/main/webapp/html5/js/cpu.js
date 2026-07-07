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
var pilotosDnfMap = new Map();
var pilotosBandeirada = new Map();
var pilotosFaiscaMap = new Map();
var pilotosTravadaFumacaMap = new Map();
var marcasPneuAcumuladas = new Map();
var pilotosAereofolioMap = new Map();
var idNoAnterior = new Map();
var sleepDefault = 600;
var sleepMain = sleepDefault;

var loader = $('<div class="loader"></div>');
$('body').prepend(loader);
var $loading = loader.hide();

function cpu_main() {
	sleepMain = sleepDefault;
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
		rest_ciruito();
	}
	if (dadosJogo != null && circuito != null && !carregouMidia) {
		mid_caregaMidia();
		carregouMidia = true;
		ctl_gerarControles();
		contCarregouMidia = 10;
	}
	if (dadosJogo != null && circuito != null && ativo && contCarregouMidia == 0) {
		// O loader só some quando o cvBg existe: é ele que indica que o jpg da
		// pista terminou de carregar e que o próximo frame do vdp_desenha já
		// mostra a pista + contador (vdp_ctl). Esconder antes deixava a tela
		// preta por 2-3s enquanto o circuitoJpg baixava.
		if (cvBg != null) {
			$loading.hide();
		}
		//$('body').removeClass('body');
		// MOSTRANDO_QUALIFY 10
		// ESPERANDO_JOGO_COMECAR 07
		// O background é pedido em todos os estados, inclusive na sala de
		// espera (07): sem cvBg o vdp_desenha retorna antes de vdp_ctl e a
		// tela de espera (pista + contador "Inicia em" + msgCarregando de
		// ctl_desenhaInfoSegundosParaIniciar) nunca aparece. O custo de
		// geração no servidor é amortizado pelo preload disparado no clique
		// de "Jogar" (jogar_preCarregaBackGround) + Cache-Control do
		// endpoint circuitoJpg.
		mid_carregaBackGroundCorrida();
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
	ctl_desenha();
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
		if (piloto.humano) {
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
		if (status == "A") {
			pilotosAereofolioMap.set(piloto.idPiloto, true);
		} else if (status == "F") {
			pilotosFaiscaMap.set(piloto.idPiloto, 15);
		} else if (status == "T" ) {
			if (no.tipoJson == 'R') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 10);
			} else if (no.tipoJson == 'A') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 7);
			} else if (no.tipoJson == 'B') {
				pilotosTravadaFumacaMap.set(piloto.idPiloto, 3);
			}
		} else if (status == "R" ) {
			pilotosDnfMap.set(piloto.idPiloto, true);
		} else if (status == "B"  && pilotosBandeirada.get(piloto.idPiloto) == null) {
			pilotosBandeirada.set(piloto.idPiloto, (i + 1));
		} else if (status == "BA" ) {
			pilotosAereofolioMap.set(piloto.idPiloto, true);
			if (pilotosBandeirada.get(piloto.idPiloto) == null) {
				pilotosBandeirada.set(piloto.idPiloto, (i + 1));
			}
		}
	}
	cpu_processaMarcasPneu(posicaoPilotos.travadaRodas);
	if (contadorJogadores != contadorJogadoresLocal) {
//		console.log(' contadorJogadores ' + contadorJogadores);
//		console.log(' contadorJogadoresLocal ' + contadorJogadoresLocal);
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

function cpu_processaMarcasPneu(travadaRodas) {
	if (!travadaRodas) {
		return;
	}
	for (var i = 0; i < travadaRodas.length; i++) {
		var marca = travadaRodas[i];
		var chave = marca.idNo + "_" + marca.tracado;
		if (marcasPneuAcumuladas.get(chave)) {
			continue;
		}
		marcasPneuAcumuladas.set(chave, true);
		vdp_registraMarcaPneuNo(marca.idNo, marca.tracado);
	}
}

function cpu_atualizaJogadores() {
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
	window.location.href = "index.html?token=" + token;
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

var main = setInterval(cpu_main, sleepMain);

var fila = setInterval(function() {
	if (funqueue.length > 0) {
		(funqueue.shift())();
	}
}, 100);

// update canvas with some information and animation
var fps = new FpsCtrl(30, function(e) {
	vdp_desenha(fps);
})

vdp_setup();
// start the loop
fps.start();
