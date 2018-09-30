/**
 * Controle de classificacao geral
 */
$('#rodada').html(lang_text('rodada'));
$('#268').html(lang_text('268'));
$('#251').html(lang_text('251'));
$('#trocaPneuCheck').append(lang_text('trocaPneus'));
$('#reabastecimentoCheck').append(lang_text('reabastecimento'));
$('#nomeCircuito').append(lang_text('nomeCircuito'));
$('#nomeCampeonato').html(lang_text('nomeCampeonato'));
$('#piloto').append(lang_text('153'));
$('[id=pontos]').append(lang_text('161'));
$('#equipe').append(lang_text('277'));
$('#jogador').append(lang_text('162'));
$('#corridas').append(lang_text('165'));
$('#listaCorridas').append(lang_text('165'));
$('#classificacaoPilotos').append(lang_text('294'));
$('#classificacaoEquipes').append(lang_text('222'));
$('#classificacaoJogadores').append(lang_text('117'));

$('#voltar').bind("click", function() {
	window.location = "index.html";
});

classificacaoCampeonato();

function classificacaoCampeonato() {
	var urlServico = "/f1mane/rest/letsRace/classificacaoCampeonato";
	$.ajax({
		type : "GET",
		url : urlServico,
		contentType : "application/json",
		dataType : "json",
		success : function(response) {
			if (!response) {
				console.log('classificacaoCampeonato() null');
				return;
			}
			var campeonatos = response;
			$('#campeonatos').find('tr').remove();
			$.each(campeonatos, function(i, val) {
				var campeonato = campeonatos[i];
				var tr = $('<tr/>');
				var td1 = $('<td class="fontLarge textCenter" scope="row"/>');
				var td2 = $('<td class="fontLarge textCenter" scope="row"/>');
				var td3 = $('<td class="fontLarge textCenter" scope="row"/>');
				td1.append(campeonato.rodadaCampeonato);
				td2.append(campeonato.nome);
				td3.append(campeonato.temporada);
				tr.append(td1);
				tr.append(td2);
				tr.append(td3);
				$('#campeonatos').append(tr);
				tr.bind("click", function() {
					detalhaCampeonato(campeonato.id);
				});
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log('classificacaoCampeonato() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});

	function detalhaCampeonato(campeonatoId) {
		var urlServico = "/f1mane/rest/letsRace/campeonato/" + campeonatoId;
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
				if (response == null) {
					console.log('carregaCampeonato() null');
					return;
				}
				$('#voltar').unbind().bind("click", function() {
					$('#listarCampeonato').removeClass('hide');
					$('#detalheCampeonato').addClass('hide');
					$('#voltar').unbind().bind("click", function() {
						window.location = "index.html";
					});
				});	
				var campeonato = response;
				$('#listarCampeonato').addClass('hide');
				$('#detalheCampeonato').removeClass('hide');
				$('#nomeCampeonatoRodada').html(campeonato.nome);
				$('#rodadaCampeonato').html(lang_text('rodada') + ' ' + campeonato.rodadaCampeonato);
				$('#temporadasLabelCarregeda').html(campeonato.temporada);
				var detalheTemporada = $('#detalheTemporada');
				if (campeonato.trocaPneu) {
					detalheTemporada.find('#trocaPneuCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#trocaPneuCheck').addClass('line-through');
				}
				if (campeonato.reabastecimento) {
					detalheTemporada.find('#reabastecimentoCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#reabastecimentoCheck').addClass('line-through');
				}
				if (campeonato.ers) {
					detalheTemporada.find('#ersCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#ersCheck').addClass('line-through');
				}
				if (campeonato.drs) {
					detalheTemporada.find('#ersCheck').removeClass('line-through');
				} else {
					detalheTemporada.find('#ersCheck').addClass('line-through');
				}

				var corridas = campeonato.corridas;

				$('#corridasTO').find('tr').remove();
				$.each(corridas, function(i, val) {
					var corrida = corridas[i];
					var tr = $('<tr style="cursor: pointer; cursor: hand" />');
					var td1 = $('<td style="width: 20%;" />');
					var td2 = $('<td/>');
					td1.append(corrida.rodada);
					td2.append(corrida.nomeCircuito);
					tr.append(td1);
					tr.append(td2);
					$('#corridasTO').append(tr);
					console.log('corrida.data ' + corrida.data);
					if (corrida.data == null) {
						tr.css("color", "darkGray");
					} else {
						tr = $('<tr style="border-bottom-style: solid; border-bottom-color: darkgray;" />');
						td = $('<td style="border-top-style: none;"/>');
						var div = $('<div style="border-left-color: ' + corrida.corVencedor + ';" class="divPiloto" />');
						div.append(corrida.vencedor);
						td.append(div);
						td.append(corrida.data);
						tr.append($('<td style="border-top-style: none;"/>'));
						tr.append(td);
						$('#corridasTO').append(tr);
					}
				});

				var pilotos = campeonato.pilotos;

				$('#dadosClassificacaoPilotos').find('tr').remove();
				$.each(pilotos, function(i, val) {
					var piloto = pilotos[i];
					var tr = $('<tr style="cursor: pointer; cursor: hand" />');
					var td0 = $('<td style="width: 10%;" />');
					var td1 = $('<td style="width: 20%;" />');
					var td2 = $('<td/>');
					td0.append(i + 1);
					td1.append(piloto.pontos);
					var div = $('<div style="border-left-color: ' + piloto.cor + ';" class="divPiloto" />');
					div.append(piloto.nome);
					td2.append(div);
					tr.append(td0);
					tr.append(td1);
					tr.append(td2);
					$('#dadosClassificacaoPilotos').append(tr);
				});

				var carros = campeonato.carros;

				$('#dadosClassificacaoCarros').find('tr').remove();
				$.each(carros, function(i, val) {
					var carro = carros[i];
					var tr = $('<tr style="cursor: pointer; cursor: hand" />');
					var td0 = $('<td style="width: 10%;" />');
					var td1 = $('<td style="width: 20%;" />');
					var td2 = $('<td/>');
					td0.append(i + 1);
					td1.append(carro.pontos);
					var div = $('<div style="border-left-color: ' + carro.cor + ';" class="divPiloto" />');
					div.append(carro.nome);
					td2.append(div);
					tr.append(td0);
					tr.append(td1);
					tr.append(td2);
					$('#dadosClassificacaoCarros').append(tr);
				});

				var jogadores = campeonato.jogadores;

				$('#dadosClassificacaoJogador').find('tr').remove();
				$.each(jogadores, function(i, val) {
					var jogador = jogadores[i];
					var tr = $('<tr style="cursor: pointer; cursor: hand" />');
					var td0 = $('<td style="width: 10%;" />');
					var td1 = $('<td style="width: 15%;" />');
					var td2 = $('<td style="width: 15%;" />');
					var td3 = $('<td/>');
					td0.append(i + 1);
					td1.append(jogador.pontos);
					td2.append(jogador.corridas);
					var jogadorImagem = $('<div style="display:  inline-flex;"  />');
					if (jogador.imagemJogador != null) {
						var imgJogador = $('<img style="margin-right: 10px;" class="img-responsive img-center userPic"/>');
						imgJogador.attr('src', jogador.imagemJogador);
						jogadorImagem.append(imgJogador);
					}
					jogadorImagem.append(jogador.nome);
					td3.append(jogadorImagem);
					tr.append(td0);
					tr.append(td1);
					tr.append(td2);
					tr.append(td3);
					$('#dadosClassificacaoJogador').append(tr);
				});
			},
			error : function(xhRequest, ErrorText, thrownError) {
				if (xhRequest.status == 204) {
					toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
				} else {
					tratamentoErro(xhRequest);
				}
				console.log('carregaCampeonato() ' + xhRequest.status + '  ' + xhRequest.responseText);
			}
		});
	}
}
