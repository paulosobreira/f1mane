function rest_dadosJogo() {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosJogo",
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
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/circuito",
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

function rest_posicaoPilotos() {
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/posicaoPilotos",
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			posicaoPilotos = response;
			setInterval(rest_posicaoPilotos(), 1000);
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
			rest_posicaoPilotos();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			alert(xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}