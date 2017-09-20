function rest_dadosJogo(nomeJogo) {
	if(nomeJogo ==null){
		console.log('rest_dadosJogo nomeJogo==null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosJogo?nomeJogo="+nomeJogo,
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
	if(dadosJogo ==null || dadosJogo.nomeJogo == null){
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/circuito?nomeJogo="+dadosJogo.nomeJogo,
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
	if(dadosJogo ==null || dadosJogo.nomeJogo == null){
		console.log('dadosJogo ==null || dadosJogo.nomeJogo == null');
		return;
	}
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/dadosParciais?nomeJogo="+dadosJogo.nomeJogo,
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
