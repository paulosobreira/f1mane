var mapTextoCahce;

function lang_text(texto, params) {
	var mapTexto = lang_mapTexto();
	if (mapTexto == null) {
		return texto;
	}
	var msg = mapTexto.get(texto);
	if (msg == null) {
		return texto;
	}
	if (params) {
		for (var i = 0; i < params.length; i++) {
			var er = new RegExp("\\{" + i + "\}", "g");
			msg = msg.replace(er, params[i]);
		}
	}
	return msg;
}

function lang_mapTexto() {
	if (mapTextoCahce == null) {
		var map = localStorage.getItem('mapTexto');
		if (map == null) {
			return null;
		}
		try {
			mapTextoCahce = new Map(Object.entries(JSON.parse(map)));
		} catch (e) {
			mapTextoCahce = new Map();
			mapTextoCahce.set('escolhidoPorOutroJogador',
					'{0} chosen by another player');
			mapTextoCahce.set('existeJogoEmAndamando',
					'There is a race in progress');
			mapTextoCahce.set('probChuva', 'Chance of Rain');
			mapTextoCahce.set('selecionePiloto', 'Select a driver');
			mapTextoCahce.set('iniciaEm', 'Starts in');
			mapTextoCahce.set('cancela', 'Cancel');
			mapTextoCahce.set('vol', 'Lap');
			mapTextoCahce.set('comando', 'Command');
			mapTextoCahce.set('icone', 'Icon');
			mapTextoCahce.set('jogar', 'Play');
			mapTextoCahce.set('reguladorCombustivel',
					'Fuel percentage for refueling');
			mapTextoCahce.set('reducaoArrasto', 'Drag reduction system');
			mapTextoCahce.set('bateriaPerformance',
					'Battery for performance enhancement');
			mapTextoCahce.set('ativaEntradaBox', 'Enable box entry');
			mapTextoCahce.set('ajusteMediano', 'Medium wing adjustment');
			mapTextoCahce.set('maisAjuste', 'More wing adjustment');
			mapTextoCahce.set('menosAjuste', 'Less wing adjustment');
			mapTextoCahce.set('agressividadeDeterminacao',
					'Aggressiveness and determination of the driver');
			mapTextoCahce.set('giroPotencia', 'RPM and power of the engine');
			mapTextoCahce.set('erroAcessando', 'Error accessing {0}');
			mapTextoCahce.set('sobre','About');
			mapTextoCahce.set('verControles','Controls');
			mapTextoCahce.set('pt','English');
			mapTextoCahce.set('en','English');
			mapTextoCahce.set('153','Driver');
			mapTextoCahce.set('154','Car');
			mapTextoCahce.set('reabastecimento','Refuel');
			mapTextoCahce.set('trocaPneus','Tire Change');
			mapTextoCahce.set('TIPO_PNEU_MOLE','Soft tire');
			mapTextoCahce.set('TIPO_PNEU_DURO','Hard tire');
			mapTextoCahce.set('TIPO_PNEU_CHUVA','Wet tire');
		}
	}
	return mapTextoCahce;
}

function lang_idioma(idioma, sincrono) {
	if (idioma == null) {
		idioma = 'en';
	}
	localStorage.setItem('idioma', idioma);
	$.ajax({
		type : "GET",
		url : "/f1mane/rest/letsRace/lang/" + idioma,
		contentType : "application/json",
		dataType : "json",
		async : !sincrono,
		success : function(response) {
			mapTextoCahce = null;
			localStorage.setItem('mapTexto', JSON.stringify(response));
		},
		error : function(xhRequest, errorText, thrownError) {
			tratamentoErro(xhRequest);
			console.log(xhRequest.status + '  ' + xhRequest.responseText + ' '
					+ ErrorText);
		}
	});
}