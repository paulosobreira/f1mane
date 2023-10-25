/**
 * Controle de classificacao por temporadas
 */
$('#162').html(lang_text('162'));
$('#161').html(lang_text('161'));
$('#165').html(lang_text('165'));


listaTemporadas();

$('#temporadaCarousel').on('slide.bs.carousel', function(event) {
	var temporadaSelecionada = $(event.relatedTarget).prop('temporada');
	temporadaClassificacao(temporadaSelecionada);
	$('#temporadaCarousel').carousel('pause');
});

$('#temporadaCarousel').carousel('pause');

function temporadaClassificacao(temporadaSelecionada) {
	var urlServico = "/f1mane/rest/letsRace/temporadaClassificacao/" + temporadaSelecionada;
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('temporadaClassificacao() null');
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
			$('#temporadaCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('temporadaClassificacao() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function listaTemporadas() {
	var urlServico = "/f1mane/rest/letsRace/temporadas";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(temporadasRes) {
			if (temporadasRes.length == 0) {
				console.log('listaTemporadas() response.length==0');
				return;
			}
			for (var i = 0; i < temporadasRes.length-1; i++) {
				var temp = temporadasRes[i];
				var dv = $('<div class="item"></div>');
				var h1 = $('<h1 class="text-center"></h1>');
				dv.prop('temporada', temp);
				h1.append(temp);
				dv.append(h1);
				$('#temporadaCarousel-inner').append(dv);
			}
			var temporadaSelecionada = temporadasRes[temporadasRes.length-1];
			$('#temporadaActive').prop('temporada', temporadaSelecionada);
			$('#temporadasLabel').html(temporadaSelecionada);
			$('#temporadaCarousel').carousel('pause');
			temporadaClassificacao(temporadaSelecionada);
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaTemporadas() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}
