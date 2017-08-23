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
			carrosImgMap = new Map();
			for (i = 0; i < dadosJogo.pilotosList.length; i++) {
				var pilotos = dadosJogo.pilotosList[i];
				var imgCarro =  new Image();
				imgCarro.src = "/f1mane/rest/letsRace/carroCima?nomeJogo="+criarJogo.nomeJogoCriado+"&idPiloto="+pilotos.id;
				carrosImgMap.set(pilotos.id, imgCarro);
			}
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

function rest_dadosParciais() {
	if(criarJogo ==null || criarJogo.nomeJogoCriado == null){
		console.log('criarJogo ==null || criarJogo.nomeJogoCriado == null');
		return;
	}	
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosParciais?nomeJogo="+criarJogo.nomeJogoCriado,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			dadosParciais = response;
			setInterval(rest_dadosParciais(), 1000);
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
			rest_dadosParciais();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}