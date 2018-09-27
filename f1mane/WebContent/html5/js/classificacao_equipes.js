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
				var td2 = $('<td class="fontLarge textCenter" />');
				td2.append(pilotos[i].pontosCorrida);
				var tr = $('<tr/>');
				
				tr.append(td0);
				
				
				var table =  $('<table class="table table-hover table-responsive tableInTable"/>');
				var trIn1 = $('<tr/>');
				var trIn2 = $('<tr/>');
				var tdIn1 = $('<td/>');
				var tdIn2 = $('<td/>');
				var tdIn3 = $('<td/>');
				var tdIn4 = $('<td/>');
				trIn1.append(tdIn1);
				trIn1.append(tdIn2);
				trIn2.append(tdIn3);
				trIn2.append(tdIn4);
				table.append(trIn1);
				table.append(trIn2);
				td1.append(table);

				var imgCapacete = $('<img class="img-responsive img-center"/>');	
				imgCapacete.attr('src', '/f1mane/rest/letsRace/capacete/' + pilotos[i].temporadaCapaceteLivery + '/' + pilotos[i].idCapaceteLivery);
				
				var imgCarro = $('<img class="img-responsive img-center"/>');	
				imgCarro.attr('src', '/f1mane/rest/letsRace/carroLado/' + pilotos[i].temporadaCarroLivery + '/' + pilotos[i].idCarroLivery);
				
				
				tdIn1.append(imgCapacete);
				tdIn2.append(imgCarro);
				
				tdIn3.append(pilotos[i].nome);
				tdIn4.append(pilotos[i].nomeCarro);
				
				tr.append(td1);
				tr.append(td2);
				$('#pilotos').append(tr);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('classificacaoGeral() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}

