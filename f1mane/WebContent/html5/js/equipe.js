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


carregaEquipe();


$('#btnSalvar').bind("click", function() {
	salvarEquipe();
});

$('#btnJogar').bind("click", function() {
	localStorage.setItem("modoCarreira", true);
	window.location = "menus.html";
});


$(document).on('click', '.number-spinner button', function() {
	var btn = $(this), oldValue = btn.closest('.number-spinner').find('input').val().trim(), newVal = 0;
	if (btn.attr('data-dir') == 'up') {
		newVal = parseInt(oldValue) + 10;
	} else {
		if (oldValue > 10) {
			newVal = parseInt(oldValue) - 10;
		} else {
			newVal = 10;
		}
	}
	btn.closest('.number-spinner').find('input').val(newVal);
});

function carregaEquipe() {
	var urlServico = "/f1mane/rest/letsRace/equipe";
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
			if (!response) {
				console.log('carregaEquipe() null');
				return;
			}
			if(response.nomeCarro!=null){
				$('#nomeEquipeValor').val(response.nomeCarro);
			}
			if(response.nomePiloto!=null){
				$('#nomePilotoValor').val(response.nomePiloto);
			}
			if(response.nomePilotoAbreviado!=null){
				$('#nomePilotoAbreviadoValor').val(response.nomePilotoAbreviado);
			}
			$('#pontosConstrutoresValor').html(response.ptsConstrutores);
			$('#pontosGanhosValor').html(response.ptsConstrutoresGanhos);
			$('#habilidadePilotoValor').val(response.ptsPiloto);
			$('#potenciaCarroValor').val(response.ptsCarro);
			$('#aerodinamicaCarroValor').val(response.ptsAerodinamica);
			$('#freioCarroValor').val(response.ptsFreio);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('carregaEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}	
	
function salvarEquipe() {
		var c1 = hexToRgb($('#corEquipe1').val())
		var c2 = hexToRgb($('#corEquipe2').val())
		var dataObj = {
			nomeCarro : $('#nomeEquipeValor').val(),
			nomePiloto : $('#nomePilotoValor').val(),
			nomePilotoAbreviadoValor : $('#nomePilotoAbreviadoValor').val(),
			pontosConstrutoresValor : $('#pontosConstrutoresValor').val(),
			ptsPiloto : $('#habilidadePilotoValor').val(),
			ptsCarro : $('#potenciaCarroValor').val(),
			ptsAerodinamica : $('#aerodinamicaCarroValor').val(),
			ptsFreio : $('#freioCarroValor').val(),
			c1R : c1.r,
			c1G : c1.g,
			c1B : c1.b,
			c2R : c2.r,
			c2G : c2.g,
			c2B : c2.b
		};
		var urlServico = "/f1mane/rest/letsRace/equipe";
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
			},
			error : function(xhRequest, ErrorText, thrownError) {
				tratamentoErro(xhRequest);
				console.log('salvarEquipe() ' + xhRequest.status + '  ' + xhRequest.responseText);
			}
		});
}

