/**
 * Controle de equipe
 */
$('#nomeEquipe').html(lang_text('254'));
$('#corEquipe1').html(lang_text('corEquipeCarro')+' 1 :');
$('#corEquipe2').html(lang_text('corEquipeCarro')+' 2 :');
$('#nomePiloto').html(lang_text('253'));
$('#nomePilotoAbreviado').html(lang_text('nomePilotoAbreviado'));
$('#pontosGanhos').html(lang_text('pontosGanhos'));
$('#pontosDisponiveis').html(lang_text('pontosDisponiveis'));
$('#habilidadePiloto').html(lang_text('255'));
$('#potenciaCarro').html(lang_text('256'));
$('#freioCarro').html(lang_text('freioCarro'));
$('#aerodinamicaCarro').html(lang_text('aerodinamicaCarro'));
$('#somaPontosCarro').html(lang_text('somaPontosCarro'));
$('#bonus').html(lang_text('bonus'));

if(localStorage.getItem("token")!=null){
	carregaEquipe();
}else{
	toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
}

$('#btnSalvar').bind("click", function() {
	salvarEquipe();
});

$('#btnJogar').bind("click", function() {
	localStorage.setItem("modoCarreira", true);
	window.location = "jogar.html";
});

$(document).on('click', '.number-spinner button', function() {
	var ptsConstrutores = parseInt($('#pontosConstrutoresValor').html());
	var btn = $(this), oldValue = btn.closest('.number-spinner').find('input').val().trim(), newVal = 0;
	oldValue =parseInt(oldValue);
	
	var tp = 0;

	if (btn.attr('data-dir') == 'up') {
		newVal = oldValue + 1;
		tp=1;
	} else {
		if (oldValue > 1) {
			newVal = oldValue - 1;
		} else {
			newVal = 0;
		}
		tp=-1;
	}

	var inc = 0;
	if (newVal < 600) {
		inc = 1;
		if (oldValue == 600) {
			inc = 2;
		}
	} else if (newVal >= 600 && newVal < 700) {
		inc = 2;
		if (oldValue == 700) {
			inc = 10;
		}
	} else if (newVal >= 700 && newVal < 800) {
		inc = 10;
		if (oldValue == 800) {
			inc = 20;
		}
	} else if (newVal >= 800 && newVal < 900) {
		inc = 20;
		if (oldValue == 900) {
			inc = 50;
		}
	} else if (newVal >= 900 && newVal < 999) {
		inc = 50;
	}
	if(tp==1){
		ptsConstrutores-=inc;
	}
	if(tp==-1){
		ptsConstrutores+=inc;
	}
	
	$('#pontosConstrutoresValor').html(ptsConstrutores);
	btn.closest('.number-spinner').find('input').val(newVal);
});


function carregaEquipe() {
	var urlServico = "/flmane/rest/letsRace/equipe";
	$.ajax({
		type : "GET",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token"),
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (response==null) {
				console.log('carregaEquipe() null');
				toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
				return;
			}
			$('#btnSalvar').removeClass('hidden');
			$('#btnJogar').removeClass('hidden');
			if(response.nomeCarro!=null){
				$('#nomeEquipeValor').val(response.nomeCarro);
			}
			if(response.nomePiloto!=null){
				$('#nomePilotoValor').val(response.nomePiloto);
			}
			if(response.nomePilotoAbreviado!=null){
				$('#nomePilotoAbreviadoValor').val(response.nomePilotoAbreviado);
			}
			$('#habilidadePilotoValor').val(response.ptsPiloto);
			$('#pontosConstrutoresValor').html(response.ptsConstrutores);
			$('#pontosGanhosValor').html(response.ptsConstrutoresGanhos);
			$('#potenciaCarroValor').val(response.ptsCarro);
			$('#aerodinamicaCarroValor').val(response.ptsAerodinamica);
			$('#freioCarroValor').val(response.ptsFreio);
			$('#corEquipeValue1').val(rgbToHex(response.c1R,response.c1G,response.c1B));
			$('#corEquipeValue2').val(rgbToHex(response.c2R,response.c2G,response.c2B));
			$('#bonusValor').html(response.bonus);
			$('#somaPontosCarroValor').html((response.ptsCarro)+(response.ptsAerodinamica)+(response.ptsFreio));
			$('#idImgCapacete').attr('src', '/flmane/rest/letsRace/capacete/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
			$('#idImgCarroLado').attr('src', '/flmane/rest/letsRace/carroLado/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
			$('#idImgCarroCima').attr('src', '/flmane/rest/letsRace/carroCima/' + rgbToHexUrlSafe(response.c1R,response.c1G,response.c1B) + '/' + rgbToHexUrlSafe(response.c2R,response.c2G,response.c2B));
		},
		error : function(xhRequest, ErrorText, thrownError) {
			if (xhRequest.status == 204) {
				toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');	
			}else{
				tratamentoErro(xhRequest);
			}
			console.log('carregaEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}	

function objetoEquipe(){
	var c1 = hexToRgb($('#corEquipeValue1').val())
	var c2 = hexToRgb($('#corEquipeValue2').val())
	var dataObj = {
		nomeCarro : $('#nomeEquipeValor').val(),
		nomePiloto : $('#nomePilotoValor').val(),
		nomePilotoAbreviado : $('#nomePilotoAbreviadoValor').val(),
		ptsPiloto : $('#habilidadePilotoValor').val(),
		ptsCarro : $('#potenciaCarroValor').val(),
		ptsAerodinamica : $('#aerodinamicaCarroValor').val(),
		ptsFreio : $('#freioCarroValor').val(),
		ptsConstrutores: $('#pontosConstrutoresValor').html(),
		c1R : c1.r,
		c1G : c1.g,
		c1B : c1.b,
		c2R : c2.r,
		c2G : c2.g,
		c2B : c2.b
	};
	return dataObj;
}

function salvarEquipe() {
	var dataObj = objetoEquipe();
	var urlServico = "/flmane/rest/letsRace/equipe";
	$.ajax({
		type : "POST",
		url : urlServico,
		headers : {
			'token' : localStorage.getItem("token"),
			'idioma' : localStorage.getItem('idioma')
		},
		contentType : "application/json",
		dataType : "json",
		data : JSON.stringify(dataObj),
		success : function(response) {
			toaster(lang_text('250'), 3000, 'alert alert-success');
			carregaEquipe();
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('salvarEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

