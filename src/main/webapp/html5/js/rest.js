var carregando = false;

var limite = 1500;
var contRelaod = 0;

function rest_dadosJogo(nomeJogo) {
	if (carregando) {
		return;
	}
	if (nomeJogo == null) {
		console.log('rest_dadosJogo nomeJogo==null');
		return;
	}
	carregando = true;
	$.ajax({
		type : "GET",
		headers : {
			'token' : token,
			'idioma' : localStorage.getItem('idioma')
		},
		url : "/flmane/rest/letsRace/dadosJogo?nomeJogo=" + nomeJogo,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosJogo = response;
			carregando = false;
		},
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_dadosJogo 503');
				return;
			}
			try {
				console.log('rest_dadosJogo ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_dadosJogo_jogadores(nomeJogo) {
	if (nomeJogo == null) {
		console.log('rest_dadosJogo nomeJogo==null');
		return;
	}
	$.ajax({
		type : "GET",
		headers : {
			'token' : token,
			'idioma' : localStorage.getItem('idioma')
		},
		url : "/flmane/rest/letsRace/dadosJogo?nomeJogo=" + nomeJogo,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosJogo = response;
			mid_atualizaJogadores();
		},
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 503) {
				console.log('rest_dadosJogo_jogadores 503');
				return;
			}
			try {
				console.log('rest_dadosJogo_jogadores ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_ciruito() {
	if (carregando) {
		return;
	}
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	carregando = true;
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/circuito?nomeCircuito=" + dadosJogo.arquivoCircuito,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			rest_processaCircuito(response);
			carregando = false;
		},
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_ciruito 503');
				return;
			}
			try {
				console.log('rest_ciruito ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_processaCircuito(response) {
	circuito = response;
	mapaIdNos = new Map();
	mapaIdPilotosNosSuave = new Map();
	var id = 1;
	for (var i = 0; i < circuito.pistaFull.length; i++) {
		mapaIdNos.set(id++, circuito.pistaFull[i]);
	}
	for (var i = 0; i < circuito.boxFull.length; i++) {
		mapaIdNos.set(id++, circuito.boxFull[i]);
	}
}

function rest_dadosParciais() {
	if (carregando) {
		sleepMain = 10;
		contRelaod++;
		//console.log('contRelaod ' + contRelaod);
		if (contRelaod > 10) {
			clearInterval(main);
			setTimeout(function() {
				window.location.reload();
			}, 1000);
		}
		return;
	}
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	carregando = true;
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/dadosParciais/" + dadosJogo.nomeJogo + "/" + idPilotoSelecionado,
		headers : {
			'token' : token,
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			contRelaod = 0;
			cpu_dadosParciaisAnterior();
			dadosParciais = response;
			cpu_dadosParciais();
			carregando = false;
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if (errorText == 'timeout') {
				// console.log('rest_dadosParciais timeout');
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_dadosParciais 503');
				return;
			}
			try {
				console.log('rest_dadosParciais ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_sairJogo() {
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/sairJogo/" + dadosJogo.nomeJogo,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_sairJogo 503');
				return;
			}
			try {
				console.log('rest_sairJogo ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_potenciaMotor(valor) {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/potenciaMotor/" + valor + "/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			carregando = false;
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_potenciaMotor 503');
				return;
			}
			try {
				console.log('rest_potenciaMotor ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});

}
function rest_agressividadePiloto(valor) {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/agressividadePiloto/" + valor + "/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_agressividadePiloto 503');
				return;
			}
			try {
				console.log('rest_agressividadePiloto ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_tracadoPiloto(valor) {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/tracadoPiloto/" + valor + "/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			carregando = false;
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_tracadoPiloto 503');
				return;
			}
			try {
				console.log('rest_tracadoPiloto ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}

		}
	});
}

function rest_ers() {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/ersPiloto/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			carregando = false;
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_ers 503');
				return;
			}
			try {
				console.log('rest_ers ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_drs() {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/drsPiloto/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_drs 503');
				return;
			}
			try {
				console.log('rest_drs ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}

		}
	});
}

function rest_boxPiloto(ativa, pneu, combustivel, asa) {
	$.ajax({
		type : "GET",
		url : "/flmane/rest/letsRace/boxPiloto/" + idPilotoSelecionado + "/" + ativa + "/" + pneu + "/" + combustivel + "/" + asa,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if (errorText == 'timeout') {
				return;
			}
			if (xhRequest.status == 401) {
				window.location.href = "index.html";
			}
			if (xhRequest.status == 503) {
				console.log('rest_boxPiloto 503');
				return;
			}
			try {
				console.log('rest_boxPiloto ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}
