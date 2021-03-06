// Converts from degrees to radians.
Math.radians = function(degrees) {
	return degrees * Math.PI / 180;
};

// Converts from radians to degrees.
Math.degrees = function(radians) {
	return radians * 180 / Math.PI;
};

function gu_bline(startCoordinates, endCoordinates) {
	if (startCoordinates == null || endCoordinates == null) {
		return;
	}
	var x0 = startCoordinates.x;
	var y0 = startCoordinates.y;
	var x1 = endCoordinates.x;
	var y1 = endCoordinates.y;
	if (x0 == null || y0 == null || x1 == null || y1 == null) {
		return;
	}
	var coordinatesArray = new Array();
	var dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
	var dy = Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
	var err = (dx > dy ? dx : -dy) / 2;
	while (true) {
		coordinatesArray.push({
			x : x0,
			y : y0
		});
		if (x0 == x1 && y0 == y1)
			break;
		var e2 = err;
		if (e2 > -dx) {
			err -= dy;
			x0 += sx;
		}
		if (e2 < dy) {
			err += dx;
			y0 += sy;
		}
	}
	return coordinatesArray;
}

function gu_distancia(startCoordinates, endCoordinates) {
	if (startCoordinates == null || endCoordinates == null) {
		return;
	}
	var x0 = startCoordinates.x;
	var y0 = startCoordinates.y;
	var x1 = endCoordinates.x;
	var y1 = endCoordinates.y;
	if (x0 == null || y0 == null || x1 == null || y1 == null) {
		return;
	}
	return Math.sqrt((x0 - x1) * (x0 - x1) + (y0 - y1) * (y0 - y1));
}

function gu_calculaAngulo(startCoordinates, endCoordinates, inc) {
	if (!startCoordinates || !endCoordinates) {
		return 0;
	}
	var dx = endCoordinates.x - startCoordinates.x;
	var dy = endCoordinates.y - startCoordinates.y;
	var tan = Math.atan2(dy, dx) + Math.radians(inc);
	var rads = tan + Math.PI;
	return rads;
}


function gu_calculaPonto(angulo, comprimento, p1) {
    var result = {};

    result.x = Math.round(Math.cos(angulo) * comprimento + p1.x);
    result.y = Math.round(Math.sin(angulo) * comprimento + p1.y);

    return result;
}
