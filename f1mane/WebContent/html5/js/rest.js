function rest_dadosJogo() {
	if(criarJogo ==null || criarJogo.nomeJogoCriado == null){
		console.log('criarJogo ==null || criarJogo.nomeJogoCriado == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosJogo?nomeJogo="+criarJogo.nomeJogoCriado,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosJogo = response;
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_ciruito() {
	if(criarJogo ==null || criarJogo.nomeJogoCriado == null){
		console.log('criarJogo ==null || criarJogo.nomeJogoCriado == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/circuito?nomeJogo="+criarJogo.nomeJogoCriado,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			circuito = response;
			rest_iniciarJogo();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_dadosPiloto() {
	if(criarJogo ==null || criarJogo.nomeJogoCriado == null){
		console.log('criarJogo ==null || criarJogo.nomeJogoCriado == null');
		return;
	}	
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosPiloto?nomeJogo="+criarJogo.nomeJogoCriado,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosPiloto = response;
			setInterval(rest_dadosPiloto(), 1000);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_criarJogo() {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/criarJogo",
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			criarJogo = response;
			rest_ciruito();
			rest_dadosJogo();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function rest_iniciarJogo() {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/iniciarJogo",
		success : function(response) {
			rest_dadosPiloto();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}