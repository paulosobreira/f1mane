/**
 * Controle de menus
 */


$('#162').html(lang_text('162'));
$('#161').html(lang_text('161'));
$('#165').html(lang_text('165'));

listaCircuitos();


$('#circuitoCarousel').on('slide.bs.carousel', function(event) {
	circuitoSelecionado = $(event.relatedTarget).prop('circuito');
	circuitoClassificacao(circuitoSelecionado);
	$('#temporadaCarousel').carousel('pause');
	$('#circuitoCarousel').carousel('pause');
});

$('#circuitoCarousel').carousel('pause');

function circuitoClassificacao(circuitoSelecionado) {
	var urlServico = "/f1mane/rest/letsRace/circuitoClassificacao/" + circuitoSelecionado;
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('circuitoClassificacao() null');
				return;
			}
			var pilotos = response;
			$('#pilotos').find('tr').remove();
			$.each(pilotos, function(i, val) {
				var td1 = $('<td scope="row"/>');
				var td2 = $('<td style="font-size: large;"/>');
				td2.append(pilotos[i].corridas);
				var td3 = $('<td style="font-size: large;"/>');
				td3.append(pilotos[i].pontos);
				var tr = $('<tr/>');
				var imgJogador = $('<img class="img-responsive img-center userPic"/>');	
				imgJogador.attr('src', pilotos[i].imagemJogador);
				td1.append(imgJogador);
				td1.append(pilotos[i].nome);
				tr.append(td1);
				tr.append(td2);
				tr.append(td3);
				$('#pilotos').append(tr);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('circuitoClassificacao() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

function listaCircuitos() {
	var urlServico = "/f1mane/rest/letsRace/circuitos";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(circuitosRes) {
			if (circuitosRes.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			circuitos = circuitosRes;
			var circuito = circuitosRes[0];
			$('#circuitosLabel').html(circuito.nome);
			$('#imgCircuito').attr('src', '/f1mane/rest/letsRace/circuitoMini/' + circuito.arquivo);
			circuitoSelecionado = circuito.arquivo;
			$('#circuitoActive').prop('circuito', circuito.arquivo);
			$.each(circuitosRes, function(i, val) {
				if (i == 0) {
					return;
				}
				var dv = $('<div class="item"></div>');
				var img = $('<img class="img-responsive center-block"/>');
				img.attr('src', '/f1mane/rest/letsRace/circuitoMini/' + this.arquivo);
				var h3 = $('<h3 class="text-center"></h3>');
				dv.prop('circuito', this.arquivo);
				h3.append(this.nome);
				dv.append(h3);
				dv.append(img);
				$('#circuitoCarousel-inner').append(dv);
			});
			$('#temporadaCarousel').carousel('pause');
			$('#circuitoCarousel').carousel('pause');
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('listaCircuitos() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

