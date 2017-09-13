$('#temporadasDD').on('show.bs.dropdown', function() {
	listaTemporadas();
});
$('#circuitosDD').on('show.bs.dropdown', function() {
	listaCircuitos();
});

function listaCircuitos() {
	var urlServico = "/f1mane/rest/letsRace/circuitos";
	$.ajax({
		type : "GET",
		url : urlServico,
		// headers: { 'token': token },
		contentType : "application/json",
		dataType : "json",
		success : function(circuitosMap) {
			var circuitos = Object.keys(circuitosMap);
			if (circuitos.length == 0) {
				console.log('listaCircuitos() response.length==0');
				return;
			}
			$('#circuitosList').find('li').remove();
			$.each(circuitos, function(i, val) {
				var li = $('<li><a>' + circuitos[i] + '</a></li>');
				li.bind("click", function() {
					console.log('circuitosLabel click');
					$('#circuitosLabel').data('circuitos', circuitos[i]);
					$('#circuitosLabel').html(circuitos[i]);
					$('#imgCircuito').attr('src','/f1mane/rest/letsRace/circuitoMini/'+circuitosMap[circuitos[i]]);
				});
				$('#circuitosList').append(li);
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('listaCircuitos() response.length==0')
		}
	});
}

function listaTemporadas() {
	var urlServico = "/f1mane/rest/letsRace/temporadas";
	$.ajax({
		type : "GET",
		url : urlServico,
		// headers: { 'token': token },
		contentType : "application/json",
		dataType : "json",
		success : function(temporadas) {
			if (temporadas.length == 0) {
				console.log('listaTemporadas() response.length==0');
				return;
			}
			$('#temporadasList').find('li').remove();
			$.each(temporadas, function(i, val) {
				var li = $('<li><a>' + temporadas[i] + '</a></li>');
				li.bind("click", function() {
					console.log('temporadasLabel click');
					$('#temporadasLabel').data('temporada', temporadas[i]);
					$('#temporadasLabel').html(temporadas[i]);
				});
				$('#temporadasList').append(li);
			});

		},
		error : function(xhRequest, ErrorText, thrownError) {
			console.log('listaTemporadas() response.length==0')
		}
	});
}