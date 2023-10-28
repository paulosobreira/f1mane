$('#btnTela').bind("click", function() {

	if (localStorage.getItem('tela') == 'menor') {
		localStorage.setItem('tela', 'normal');
		$('#btnTela').html(lang_text('telaNormal'));

	} else {
		localStorage.setItem('tela', 'menor');
		$('#btnTela').html(lang_text('telaMenor'));

	}

});
if (localStorage.getItem('tela') == 'menor') {
	$('#btnTela').html(lang_text('telaMenor'));
}else {
	$('#btnTela').html(lang_text('telaNormal'));
}

