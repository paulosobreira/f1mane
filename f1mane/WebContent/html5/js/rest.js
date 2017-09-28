function rest_dadosJogo(nomeJogo) {
	if (nomeJogo == null) {
		console.log('rest_dadosJogo nomeJogo==null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosJogo?nomeJogo=" + nomeJogo,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosJogo = response;
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_ciruito() {
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/circuito?nomeJogo=" + dadosJogo.nomeJogo,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			circuito = response;
			mapaIdNos = new Map();
			var id = 1;
			for (i = 0; i < circuito.pistaFull.length; i++) {
				mapaIdNos.set(id++, circuito.pistaFull[i]);
			}
			for (i = 0; i < circuito.boxFull.length; i++) {
				mapaIdNos.set(id++, circuito.boxFull[i]);
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_dadosParciais() {
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosParciais/" + dadosJogo.nomeJogo + "/"
				+ idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosParciais = response;
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_potenciaMotor(valor) {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/potenciaMotor/" + valor + "/"
				+ idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			console.log('rest_potenciaMotor valor: ' + valor + ' retorno :'
					+ retorno);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('rest_potenciaMotor() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});

}
function rest_agressividadePiloto(valor) {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/agressividadePiloto/" + valor + "/"
				+ idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			console.log('rest_agressividadePiloto valor: ' + valor
					+ ' retorno :' + retorno);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('rest_agressividadePiloto ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}

function rest_tracadoPiloto(valor) {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/tracadoPiloto/" + valor + "/"
				+ idPilotoSelecionado,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			console.log('rest_tracadoPiloto valor: ' + valor
					+ ' retorno :' + retorno);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('rest_tracadoPiloto ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}
