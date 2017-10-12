var carregando = false;

var limite = 250;

function rest_dadosJogo(nomeJogo) {
	if(carregando){
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
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_ciruito() {
	if(carregando){
		return;
	}
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	carregando = true;
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
			carregando = false;
		},
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_dadosParciais() {
	if(carregando){
		return;
	}
	if (dadosJogo == null || dadosJogo.nomeJogo == null) {
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	carregando = true;
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
			carregando = false;
			dadosParciais = response;
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			carregando = false;
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
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
			carregando = false;
			console.log('rest_potenciaMotor valor: ' + valor + ' retorno :'
					+ retorno);
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
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
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
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
			carregando = false;
			console.log('rest_tracadoPiloto valor: ' + valor + ' retorno :'
					+ retorno);
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log('rest_tracadoPiloto ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
			console.log('rest_ers retorno :' + retorno);
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log('rest_ers ' + xhRequest.status + '  '
					+ xhRequest.responseText);
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
			console.log('rest_drs retorno :' + retorno);
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log('rest_drs ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}

function rest_boxPiloto(ativa, pneu, combustivel, asa) {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/boxPiloto/" + idPilotoSelecionado + "/"
				+ ativa + "/" + pneu + "/" + combustivel + "/" + asa,
		headers : {
			'token' : token
		},
		contentType : "application/json",
		dataType : "json",
		success : function(retorno) {
			console.log('rest_boxPiloto retorno:' + retorno);
		},
		timeout : limite,
		error : function(xhRequest, errorText, thrownError) {
			if(errorText=='timeout'){
				return;
			}
			if(xhRequest.status = 401){
				alert('kick errorText:'+errorText+' thrownError:'+thrownError)
				window.location.href = "index.html";
			}
			console.log('rest_boxPiloto ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}
