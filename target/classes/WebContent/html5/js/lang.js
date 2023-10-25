$('#btnPt').bind("click", function() {
	lang_idioma('pt', true);
	location.reload();
});
$('#btnEn').bind("click", function() {
	lang_idioma('en', true);
	location.reload();
});
$('#btnIt').bind("click", function() {
	lang_idioma('it', true);
	location.reload();
});
$('#btnEs').bind("click", function() {
	lang_idioma('es', true);
	location.reload();
});


$('#btnPt').html(lang_text('pt'));
$('#btnEn').html(lang_text('en'));
$('#btnIt').html(lang_text('it'));
$('#btnEs').html(lang_text('es'));

function lang_text(texto, params) {
	var msg = localStorage.getItem('mapTexto_' +texto);
	if (msg == null) {
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

function lang_idioma(idioma, sincrono) {
	if (idioma == null) {
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
			for (var i = 0; i < response.keys.length; i++) {
				localStorage.setItem('mapTexto_' + response.keys[i], response.values[i]);
			}
		},
		error : function(xhRequest, errorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log(xhRequest.status + '  ' + xhRequest.responseText + ' ' + ErrorText);
		}
	});
}