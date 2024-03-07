/**
 * Controle de classificacao geral
 */
$('#162').html(lang_text('162'));
$('#161').html(lang_text('161'));
$('#165').html(lang_text('165'));


classificacaoGeral();

function classificacaoGeral() {
	var urlServico = "/flmane/rest/letsRace/classificacaoGeral";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('classificacaoGeral() null');
				return;
			}
			var pilotos = response;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td0 = $('<td class="fontLarge textCenter" scope="row"/>');
				td0.append(i+1);
				var td1 = $('<td class="textCenter classificacao-jogador" scope="row"/>');
				var td2 = $('<td class="fontLarge textCenter" />');
				td2.append(pilotos[i].corridas);
				var td3 = $('<td class="fontLarge textCenter" />');
				td3.append(pilotos[i].pontos);
				var tr = $('<tr/>');
				var imgJogador = $('<img onerror="imgError(this);" class="img-responsive img-center userPic"/>');	
				imgJogador.attr('src', pilotos[i].imagemJogador);
				td1.append(imgJogador);
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

