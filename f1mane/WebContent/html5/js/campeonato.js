/**
 * Controle de Campeonato
 */
if(localStorage.getItem("token")!=null){
	carregaCampeonato();
}else{
	toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
}



function carregaCampeonato() {
	var urlServico = "/f1mane/rest/letsRace/campeonato";
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
			if (response==null) {
				console.log('carregaCampeonato() null');
				toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');
				return;
			}
			
		},
		error : function(xhRequest, ErrorText, thrownError) {
			if (xhRequest.status == 204) {
				toaster(lang_text('precisaEstaLogado'), 4000, 'alert alert-danger');	
			}else{
				tratamentoErro(xhRequest);
			}
			console.log('carregaCampeonato() ' + xhRequest.status + '  ' + xhRequest.responseText);
		}
	});
}	
