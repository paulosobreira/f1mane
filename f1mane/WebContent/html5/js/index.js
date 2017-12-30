/**
 * Controle do menu inicial
 */
if (localStorage.getItem("versao") != $("#versao").val()) {
	console.log('Limpando localStorage versao: '+$("#versao").val());
	localStorage.clear();
	localStorage.setItem("versao", $("#versao").val());
} else {
	localStorage.removeItem("token");
	localStorage.removeItem("idPilotoSelecionado");
}


$('#btnSobre').bind("click", function() {
	$('#botoes').hide();
	sobre();
});

function sobre() {
	var urlServico = "/f1mane/rest/letsRace/sobre/";
	$.ajax({
		type : "GET",
		url : urlServico,
		success : function(sobreRes) {
			var dv = $('<div class="creditos"></div>');
			dv.append(sobreRes);
			$('#mainContainer').append(dv);
			dv.bind("click", function() {
				dv.remove();
				$('#botoes').show();
			});
		},
		error : function(xhRequest, ErrorText, thrownError) {
			$('#botoes').show();
			tratamentoErro(xhRequest);
			console.log('sobre() ' + xhRequest.status + '  '
					+ xhRequest.responseText);
		}
	});
}