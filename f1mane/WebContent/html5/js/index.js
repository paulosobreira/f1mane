/**
 * Controle do menu inicial
 */
$('#voltar').hide();
if (localStorage.getItem("versao") != $("#versao").val()) {
	console.log('Limpando localStorage versao: ' + $("#versao").val());
	var token = localStorage.getItem("token");
	var plataforma =  localStorage.getItem('plataforma');
	localStorage.clear();
	localStorage.setItem("versao", $("#versao").val());
	localStorage.setItem('plataforma',plataforma);
	if(token!=null){
		localStorage.setItem("token", token);
	}
}

localStorage.removeItem("idPilotoSelecionado");
localStorage.removeItem("nomeJogo");
localStorage.setItem("modoCarreira", false);

var token = getParameter('token');

var plataforma =  getParameter('plataforma');

var limpar =  getParameter('limpar');

if(limpar == 'S'){
	localStorage.removeItem("token");
}

if (token == null) {
	token = localStorage.getItem("token");
}

if (token != null) {
	localStorage.setItem("token", token);
	dadosJogador();
}

if(plataforma!=null){
	localStorage.setItem("plataforma", plataforma);
}

plataforma =  localStorage.getItem('plataforma');

if(plataforma == "android"){
	$('#voltar').show();
	$('#voltar').unbind().bind("click", function() {
		Android.exitApp();
	});
}

var userLang = navigator.language || navigator.userLanguage;
if (userLang != null && localStorage.getItem('idioma') == null) {
	if (userLang.split('-')[0] == 'pt') {
		lang_idioma('pt', true);
	} else if (userLang.split('-')[0] == 'it') {
		lang_idioma('it', true);
	} else {
		lang_idioma('en', true);
	}
} else {
	lang_idioma(localStorage.getItem('idioma'), true);
}

$('#btnJogar').html(lang_text('jogar'));
$('#btnSobre').html(lang_text('sobre'));
$('#btnControles').html(lang_text('verControles'));
$('#btnIdioma').html(lang_text('linguagem'));
$('#btnClassificacao').html(lang_text('ranking'));
$('#btnEquipe').html(lang_text('221'));
$('#btnCampeonato').html(lang_text('268'));


$('#btnSobre').bind("click", function() {
	$('#botoes').hide();
	sobre();
	$('#voltar').show();
	$('#voltar').unbind().bind("click", function() {
		$('.creditos').remove();
		$('#botoes').show();
		if(plataforma == "android"){
			$('#voltar').unbind().bind("click", function() {
				Android.exitApp();
			});
		}else{
			$('#voltar').hide();
		}
	});
});



function dadosJogador() {
	var urlServico = "/f1mane/rest/letsRace/dadosToken/";
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token")
		},
		success : function(srvPaddockPack) {
			if (srvPaddockPack) {
				if(srvPaddockPack.sessaoCliente.guest){
					localStorage.removeItem("token");
					return;
				}
				$('#nomeJogador').append(
						'<b>' + srvPaddockPack.sessaoCliente.nomeJogador
								+ '</b>');
				$('#imgJogador').attr('src',
						srvPaddockPack.sessaoCliente.imagemJogador);
				if (srvPaddockPack.sessaoCliente.jogoAtual) {
					localStorage.setItem("nomeJogo",
							srvPaddockPack.sessaoCliente.jogoAtual);
				}
			}
		},
		error : function(xhRequest, ErrorText, thrownError) {
			$('#botoes').show();
			if (xhRequest.status == 404) {
				localStorage.removeItem("token");
				token = null;
				return;
			}
			tratamentoErro(xhRequest);
			console.log('sobre() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}

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