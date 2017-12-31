var carregando = false;

var limite = 1250;

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
			'token' : token
		},
		url : "/f1mane/rest/letsRace/dadosJogo?nomeJogo=" + nomeJogo,
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
			try {
				console.log('rest_dadosJogo '+xhRequest.status + '  ' + xhRequest.responseText);
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
	if (localStorage.getItem(dadosJogo.arquivoCircuito)) {
		console.log('Carregando circuito localStorage '+dadosJogo.arquivoCircuito);
		rest_processaCircuito(JSON.parse(localStorage.getItem(dadosJogo.arquivoCircuito)));
		return;
	}
	carregando = true;
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/circuito?nomeCircuito=" + dadosJogo.arquivoCircuito,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			console.log('Carregando circuito rest '+dadosJogo.arquivoCircuito);
			try {
				localStorage.setItem(dadosJogo.arquivoCircuito,JSON.stringify(response));	
			} catch (e) {
				console.log('Nao Gravou no localStorage '+dadosJogo.arquivoCircuito);
			}
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
			try {
				console.log('rest_ciruito '+xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_processaCircuito(response){
	circuito = response;
	mapaIdNos = new Map();
	mapaIdNosSuave = new Map();
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
		return;
	}
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	carregando = true;
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosParciais/" + dadosJogo.nomeJogo + "/" + idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			cpu_dadosParciaisAnterior();
			dadosParciais = response;
			cpu_dadosParciais();
			carregando = false;
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
			try {
				console.log('rest_dadosParciais '+xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}

function rest_potenciaMotor(valor) {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/potenciaMotor/" + valor + "/" + idPilotoSelecionado,
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
		url : "/f1mane/rest/letsRace/agressividadePiloto/" + valor + "/" + idPilotoSelecionado,
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
		url : "/f1mane/rest/letsRace/tracadoPiloto/" + valor + "/" + idPilotoSelecionado,
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
		url : "/f1mane/rest/letsRace/ersPiloto/" + idPilotoSelecionado,
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
		url : "/f1mane/rest/letsRace/drsPiloto/" + idPilotoSelecionado,
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
		url : "/f1mane/rest/letsRace/boxPiloto/" + idPilotoSelecionado + "/" + ativa + "/" + pneu + "/" + combustivel + "/" + asa,
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
			try {
				console.log('rest_boxPiloto ' + xhRequest.status + '  ' + xhRequest.responseText);
			} catch (e) {
				console.log(e);
			}
		}
	});
}
