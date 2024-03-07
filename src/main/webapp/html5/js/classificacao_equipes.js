/**
 * Controle de classificacao Equipes
 */
$('#277').html(lang_text('277'));
$('#161').html(lang_text('161'));
$('#165').html(lang_text('165'));
$('#153').html(lang_text('153'));



classificacaoEquipes();

function classificacaoEquipes() {
	var urlServico = "/flmane/rest/letsRace/classificacaoEquipes";
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
				var td1 = $('<td class="textCenter classificacao-jogador" scope="row"/>');
				var td2 = $('<td class="fontLarge textCenter" />');
				var td3 = $('<td class="fontLarge textCenter" />');
				
				var tr = $('<tr/>');
				tr.append(td0);
				tr.append(td1);
				tr.append(td2);
				tr.append(td3);
				
				//style="height: 25px;"
				var imgCapacete = $('<img class="img-responsive img-center"  />');	
				imgCapacete.attr('src', '/flmane/rest/letsRace/capacete/' + pilotos[i].temporadaCapaceteLivery + '/' + pilotos[i].idCapaceteLivery);
				//style="height: 25px;"
				var imgCarro = $('<img class="img-responsive img-center"  />');	
				imgCarro.attr('src', '/flmane/rest/letsRace/carroLado/' + pilotos[i].temporadaCarroLivery + '/' + pilotos[i].idCarroLivery);

				td0.append(i+1);
				td1.append(imgCapacete);
				td1.append(pilotos[i].nome);
				td2.append(imgCarro);
				td2.append(pilotos[i].nomeCarro);
				td3.append(pilotos[i].pontosCorrida);

				$('#pilotos').append(tr);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('classificacaoGeral() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

