var mapTextoCahce;

function lang_text(texto, params) {
	var mapTexto = lang_mapTexto();
	if (mapTexto == null) {
		return texto;
	}
	var msg = mapTexto.get(texto);
	if(msg==null){
		return texto;
	}
	if (params) {
		for (var i = 0; i < params.length; i++) {
			var er = new RegExp("\\{" + i + "\}", "g");
			msg = msg.replace(er, params[i]);
		}
	}
	return msg;
}

function lang_mapTexto() {
	if (mapTextoCahce == null) {
		var map = localStorage.getItem('mapTexto');
		if (map == null) {
			return null;
		}
		mapTextoCahce = new Map(Object.entries(JSON.parse(map)));
	}
	return mapTextoCahce;
}

function lang_idioma(idioma, sincrono) {
	if(idioma==null){
		idioma = 'en';
	}
	localStorage.setItem('idioma', idioma);
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/lang/" + idioma,
		contentType : "application/json",
		dataType : "json",
		async : !sincrono,
		success : function(response) {
			mapTextoCahce = null;
			localStorage.setItem('mapTexto', JSON.stringify(response));
		},
		error : function(xhRequest, errorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log(xhRequest.status + '  ' + xhRequest.responseText + ' '
					+ ErrorText);
		}
	});
}