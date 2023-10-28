function FpsCtrl(fps, callback) {

	var delay = 1000 / fps, time = null, frame = -1, tref, fpscont = 0, frames = 0, lastTime = 0;

	function loop(timestamp) {
		if (time === null)
			time = timestamp;
		var diff = (timestamp - time);
		var seg = Math.floor(diff / delay);
		if (seg > frame) {
			frame = seg;
			callback({
				time : timestamp,
				frame : frame
			})
			frames++;
		}
		tref = requestAnimationFrame(loop)
		if ((timestamp - lastTime) > 1000) {
			lastTime = timestamp;
			fpscont = frames;
			frames = 0;
		}
	}

	this.isPlaying = false;

	this.frameRate = function(newfps) {
		if (!arguments.length)
			return fpscont;
		fps = newfps;
		delay = 1000 / fps;
		frame = -1;
		time = null;
	};

	this.start = function() {
		if (!this.isPlaying) {
			this.isPlaying = true;
			tref = requestAnimationFrame(loop);
		}
	};

	this.pause = function() {
		if (this.isPlaying) {
			cancelAnimationFrame(tref);
			this.isPlaying = false;
			time = null;
			frame = -1;
		}
	};
}
