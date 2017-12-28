function safeArray(arr, i) {
	if (arr.length == 0) {
		return null;
	}
	if (i < 0) {
		return arr[0];
	}
	if (i >= arr.length) {
		var virou = i - arr.length;
		if(virou>= arr.length){
			virou = 0;
		}
		return arr[virou];
	}
	
	return arr[i];
}

function intervalo(val1, val2) {
	return (val1 + (Math.random() * (0.1 + val2 - val1)));
}

function getParameter(val) {
	var result = null, tmp = [];
	var items = location.search.substr(1).split("&");
	for (var index = 0; index < items.length; index++) {
		tmp = items[index].split("=");
		if (tmp[0] == val)
			result = decodeURIComponent(tmp[1]);
	}
	return result;
}

function formatarTempo(value) {

	var minu = Math.floor(value / 60000);
	var seg = Math.floor((value - (minu * 60000)) / 1000);
	var mili = Math.floor(value - ((minu * 60000) + (seg * 1000)));
	if (minu > 0)
		return minu + ":" + pad(seg, 2) + "." + pad(mili, 3);
	else
		return seg + "." + pad(mili, 3);
}

function pad(n, width, z) {
	z = z || '0';
	n = n + '';
	return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
}

function tratamentoErro( xhRequest) {
	if (xhRequest.status == 401) {
		toaster('Sem sessão voltando ao inicio...',2000,'alert alert-danger');
		setTimeout(function(){ 
			localStorage.clear();
			window.location = "index.html";
		}, 3000);
	} else {
		var erroMsg = xhRequest.status + '  ' + xhRequest.responseText; ;
		if(xhRequest.responseJSON!=null && xhRequest.responseJSON.messageString!=null){
			erroMsg = xhRequest.responseJSON.messageString;
		}
		toaster('Mensagem do servidor : '+  erroMsg,3000,'alert alert-danger');
	}
}


function toaster(msg,tempo,classe) {
	if(classe==null){
		classe = 'alert alert-info';
	}
	$('#snackbar').remove();
	var toast = $('<div id="snackbar" class="show '+classe+'" role="alert">'
			+  msg + '</div>');
	$('#head').append(toast);
	setTimeout(function(){$('#snackbar').remove(); }, tempo);
}


