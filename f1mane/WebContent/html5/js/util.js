function safeArray(arr, i) {
	if (arr.length == 0) {
		return null;
	}
	if (i < 0) {
		return arr[0];
	}
	if (i >= arr.length) {
		var virou = i - arr.length;
		if (virou >= arr.length) {
			virou = 0;
		}
		return arr[virou];
	}

	return arr[i];
}

function intervalo(val1, val2) {
	return (val1 + (Math.random() * (0.1 + val2 - val1)));
}

function intervaloInt(val1, val2) {
	return Math.round(intervalo(val1, val2));
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

function formatarDiferenca(value) {
	if(value == null){
		return '';
	}
	value = new String(value);
	if(value.length<2){
		value = '00'+value;
	}
	if(value.length<3){
		value = '0'+value;
	}
	return value.substr(0,value.length-3) +'.'+ value.substr(value.length-3,value.length);
}

function formatarTempo(value) {
	if(value == null){
		return '';
	}
	var minu = Math.floor(value / 60000);
	var seg = Math.floor((value - (minu * 60000)) / 1000);
	var mili = value - ((minu * 60000) + (seg * 1000));
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

function tratamentoErro(xhRequest) {
	if (xhRequest.status == 401) {
		toaster(lang_text('tratamentoErro'), 4000, 'alert alert-danger');
		setTimeout(function() {
			window.location = "index.html";
		}, 3500);
	} else {
		var erroMsg = xhRequest.status + '  ' + xhRequest.responseText;
		if (xhRequest.responseJSON != null && xhRequest.responseJSON.messageString != null) {
			erroMsg = xhRequest.responseJSON.messageString;
		}
		toaster(erroMsg, 3500, 'alert alert-danger');
	}
}

function toaster(msg, tempo, classe) {
	$('#snackbar').remove();
	var toast = $('<div id="snackbar" class="show" role="alert">' + msg + '</div>');
	$('#head').append(toast);
	setTimeout(function() {
		$('#snackbar').remove();
	}, 4000);
}

function hexToRgb(hex) {
    // Expand shorthand form (e.g. "03F") to full form (e.g. "0033FF")
    var shorthandRegex = /^#?([a-f\d])([a-f\d])([a-f\d])$/i;
    hex = hex.replace(shorthandRegex, function(m, r, g, b) {
        return r + r + g + g + b + b;
    });

    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
    return result ? {
        r: parseInt(result[1], 16),
        g: parseInt(result[2], 16),
        b: parseInt(result[3], 16)
    } : null;
}

function componentToHex(c) {
    var hex = c.toString(16);
    return hex.length == 1 ? "0" + hex : hex;
}

function rgbToHex(r, g, b) {
    return "#" + componentToHex(r) + componentToHex(g) + componentToHex(b);
}

function rgbToHexUrlSafe(r, g, b) {
    return componentToHex(r) + componentToHex(g) + componentToHex(b);
}