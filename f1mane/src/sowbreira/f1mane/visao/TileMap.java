package sowbreira.f1mane.visao;

import java.awt.image.BufferedImage;

public class TileMap {

	private BufferedImage backGround;
	private BufferedImage backGroundZoomed;
	private double zoom;

	public BufferedImage getBackGround() {
		return backGround;
	}

	public void setBackGround(BufferedImage backGround) {
		this.backGround = backGround;
	}

	public BufferedImage getBackGroundZoomed() {
		return backGroundZoomed;
	}

	public void setBackGroundZoomed(BufferedImage backGroundZoomed) {
		this.backGroundZoomed = backGroundZoomed;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

}
