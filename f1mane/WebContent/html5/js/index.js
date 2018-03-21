/**
 * Controle do menu inicial
 */
$('#voltar').hide();
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
	if(userLang.split('-')[0]=='pt'){
		lang_idioma('pt',true);	
	}else if(userLang.split('-')[0]=='it'){
		lang_idioma('it',true);	
	} else{
		lang_idioma('en',true);
	}
}else{
	lang_idioma(localStorage.getItem('idioma'),true);	
}

$('#btnJogar').html(lang_text('jogar'));
$('#btnSobre').html(lang_text('sobre'));
$('#btnControles').html(lang_text('verControles'));
$('#btnIdioma').html(lang_text('linguagem'));	

$('#btnSobre').bind("click", function() {
	$('#botoes').hide();
	sobre();
	$('#voltar').show();
});

$('#voltar').bind("click", function() {
	$('.creditos').remove();
	$('#botoes').show();
	$('#voltar').hide();
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
				$('#voltar').hide();
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