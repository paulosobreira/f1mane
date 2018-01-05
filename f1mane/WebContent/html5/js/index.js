/**
 * Controle do menu inicial
 */

if (localStorage.getItem("versao") != $("#versao").val()) {
	console.log('Limpando localStorage versao: '+$("#versao").val());
	localStorage.clear();
	localStorage.setItem("versao", $("#versao").val());
} else {
	localStorage.removeItem("token");
	localStorage.removeItem("idPilotoSelecionado");
}

var userLang = navigator.language || navigator.userLanguage;
if(userLang!=null && localStorage.getItem('idioma')==null){
	lang_idioma(userLang.split('-')[0],true);
}else{
	lang_idioma(localStorage.getItem('idioma'),true);	
}

$('#btnJogar').html(lang_text('jogar'));
$('#btnSobre').html(lang_text('sobre'));
$('#btnControles').html(lang_text('verControles'));
if(localStorage.getItem('idioma')=='pt'){
	$('#btnIdioma').html(lang_text('en'));	
}else{
	$('#btnIdioma').html(lang_text('pt'));
}

$('#btnSobre').bind("click", function() {
	$('#botoes').hide();
	sobre();
});

$('#btnIdioma').bind("click", function() {
	if(localStorage.getItem('idioma')=='pt'){
		lang_idioma('en');	
	}else{
		lang_idioma('pt');
	}
	location.reload();
});

function sobre() {
	var urlServico = "/f1mane/rest/letsRace/sobre/";
	$.ajax({
		type : "GET",
		url : urlServico,
		success : function(sobreRes) {
			var dv = $('<div class="creditos"></div>');
			dv.append(sobreRes);
			$('#mainContainer').append(dv);
			dv.bind("click", function() {
				dv.remove();
				$('#botoes').show();
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			$('#botoes').show();
			tratamentoErro(xhRequest);
			console.log('sobre() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}