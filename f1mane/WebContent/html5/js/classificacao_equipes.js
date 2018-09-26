/**
 * Controle de classificacao Equipes
 */
$('#277').html(lang_text('277'));
$('#161').html(lang_text('161'));
$('#165').html(lang_text('165'));


classificacaoEquipes();

function classificacaoEquipes() {
	var urlServico = "/f1mane/rest/letsRace/classificacaoEquipes";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('classificacaoEquipes() null');
				return;
			}
			var pilotos = response;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td0 = $('<td class="fontLarge textCenter" scope="row"/>');
				td0.append(i+1);
				var td1 = $('<td class="textCenter classificacao-jogador" scope="row"/>');
				var td3 = $('<td class="fontLarge textCenter" />');
				td3.append(pilotos[i].pontosCorrida);
				var tr = $('<tr/>');
				var imgJogador = $('<img class="img-responsive img-center userPic"/>');	
				imgJogador.attr('src', pilotos[i].imagemJogador);
				td1.append(pilotos[i].nome);
				tr.append(td0);
				tr.append(td1);
				tr.append(td2);
				tr.append(td3);
				$('#pilotos').append(tr);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('classificacaoGeral() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

