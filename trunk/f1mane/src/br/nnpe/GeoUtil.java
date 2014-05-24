package br.nnpe;

import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * 
 * @author Paulo Sobreira Criado em 13:47 23/4/2004
 * 
 */
public class GeoUtil {

	public static int GRAVITY = 10;
	public static int LEFT_TO_RIGHT = 0;
	public static int RIGHT_TO_LEFT = 1;

	public static double calculaAnguloRad(Point a, Point b) {
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		// double tan = Math.atan((double) dy / dx);
		double tan = Math.atan2(dy, dx);
		// Logger.logar("Tangete" + tan);
		// return tan;
		return tan;
		// 2 quadrantes, -pi/2 até pi/2
		// return Math.atan2(dy, dx); // 4 quadrantes, -pi até pi
	}

	public static double calculaAngulo(Point a, Point b, double fator) {
		int dx = b.x - a.x;
		int dy = b.y - a.y;
		// double tan = Math.atan((double) dy / dx);
		double tan = Math.atan2(dy, dx);
		// Logger.logar("Tangete" + tan);
		// return tan;
		return Math.toDegrees(tan) + fator;
		// 2 quadrantes, -pi/2 até pi/2
		// return Math.atan2(dy, dx); // 4 quadrantes, -pi até pi
	}

	public static List drawParabola(int velocity, int angle,
			Point initialPostion, int orientation, Graphics graphics) {
		List list = new ArrayList();
		double vx, vy, newx = 0, newy, oldx, oldy;
		vx = velocity * Math.cos(Math.toRadians(angle));
		vy = velocity * Math.sin(Math.toRadians(angle));
		oldx = initialPostion.x;
		oldy = initialPostion.y;
		for (int i = 0; i < velocity; i++) {
			if (orientation == LEFT_TO_RIGHT)
				newx = initialPostion.x + vx * i;
			else if (orientation == RIGHT_TO_LEFT)
				newx = initialPostion.x - vx * i;
			newy = initialPostion.y - vy * i + (GRAVITY * Math.pow(i, 2)) / 2;
			list.addAll(drawBresenhamLine((int) oldx, (int) oldy, (int) newx,
					(int) newy));
			// list.add(new Point((int)oldx,(int)oldy));
			// graphics.drawLine((int)oldx,(int)oldy,(int)newx,(int)newy);
			oldx = newx;
			oldy = newy;
		}
		return list;
	}

	public static List<Point> drawBresenhamLine(Point p1, Point p2) {
		return drawBresenhamLine(p1.x, p1.y, p2.x, p2.y);
	}

	public static List<Point> drawBresenhamLine(int x0, int y0, int x1, int y1) {
		return drawBresenhamLine(x0, y0, x1, y1, new LinkedList<Point>());
	}

	public static List<Point> drawBresenhamLineAL(int x0, int y0, int x1, int y1) {
		return drawBresenhamLine(x0, y0, x1, y1, new ArrayList<Point>());
	}

	public static List<Point> drawBresenhamLine(int x0, int y0, int x1, int y1,
			List<Point> list) {
		int dy = y1 - y0;
		int dx = x1 - x0;
		int stepx, stepy;

		if (dy < 0) {
			dy = -dy;
			stepy = -1;
		} else {
			stepy = 1;
		}
		if (dx < 0) {
			dx = -dx;
			stepx = -1;
		} else {
			stepx = 1;
		}
		// dy is now 2*dy
		dy <<= 1;
		// dx is now 2*dx
		dx <<= 1;

		list.add(new Point(x0, y0));
		if (dx > dy) {
			// same as 2*dy - dx
			int fraction = dy - (dx >> 1);
			while (x0 != x1) {
				if (fraction >= 0) {
					y0 += stepy;
					// same as fraction -= 2*dx
					fraction -= dx;
				}
				x0 += stepx;
				// same as fraction -= 2*dy
				fraction += dy;
				list.add(new Point(x0, y0));
			}
		} else {
			int fraction = dx - (dy >> 1);
			while (y0 != y1) {
				if (fraction >= 0) {
					x0 += stepx;
					fraction -= dy;
				}
				y0 += stepy;
				fraction += dx;
				list.add(new Point(x0, y0));
			}
		}

		return list;
	}

	public static int distaciaEntrePontos(Point p1, Point p2) {
		return distaciaEntrePontos(p1.x, p1.y, p2.x, p2.y);
	}

	public static int distaciaEntrePontos(int x1, int y1, int x2, int y2) {
		// return drawBresenhamLine(x1, y1, x2, y2).size();
		return (int) Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	public static Point calculaPonto(int angulo, int comprimento, Point p1) {
		return calculaPonto((double) angulo, comprimento, p1);
	}

	public static Point calculaPonto(double angulo, int comprimento, Point p1) {
		int x = 0;
		int y = 0;
		double ang = Math.toRadians(angulo);
		x = (int) (comprimento * Math.sin(ang));
		y = (int) (comprimento * Math.cos(ang));
		// System.out.println("Angulo : "+ang);
		// System.out.println("Pontos x="+x+" y="+y);
		return new Point(p1.x + x, p1.y - y);
	}

	public static List fillCircle(int centerX, int centerY, int raio) {
		List circlePoints = drawCircle(centerX, centerY, raio);
		// long s = System.currentTimeMillis();
		HashSet set = new HashSet(circlePoints);
		boundaryFill4con(new Point(centerX, centerY), set);
		/*
		 * Logger.logar("tempo "+(System.currentTimeMillis()-s));
		 * Logger.logar(set.size());
		 */
		return new ArrayList(set);
	}

	public static void boundaryFill4con(Point point, HashSet limitArea) {
		Stack stack = new Stack();
		stack.add(point);
		while (!stack.isEmpty()) {
			Point point2 = (Point) stack.pop();
			if (!limitArea.contains(point2)) {
				limitArea.add(point2);
				Point p = new Point(point2.x + 1, point2.y);
				if (!limitArea.contains(p))
					stack.push(p);
				p = new Point(point2.x, point2.y + 1);
				if (!limitArea.contains(p))
					stack.push(p);
				p = new Point(point2.x - 1, point2.y);
				if (!limitArea.contains(p))
					stack.push(p);
				p = new Point(point2.x, point2.y - 1);
				if (!limitArea.contains(p))
					stack.push(p);
			}
		}
	}

	public static List drawCircle(int centerX, int centerY, int raio) {
		List points = new ArrayList();
		for (int x = 0; x <= (int) raio / Math.sqrt(2); x++) {
			int y = (int) Math.sqrt(Math.pow(raio, 2) - Math.pow(x, 2));
			points.add(new Point(x + centerX, -y + centerY));
			points.add(new Point(-x + centerX, -y + centerY));
			points.add(new Point(x + centerX, y + centerY));
			points.add(new Point(-x + centerX, y + centerY));
		}

		for (int y = 0; y <= (int) raio / Math.sqrt(2); y++) {
			int x = (int) Math.sqrt(Math.pow(raio, 2) - Math.pow(y, 2));
			points.add(new Point(x + centerX, -y + centerY));
			points.add(new Point(-x + centerX, -y + centerY));
			points.add(new Point(x + centerX, y + centerY));
			points.add(new Point(-x + centerX, y + centerY));
		}

		return points;
	}

	public static void main(String[] args) {
		// int x2 = 10;
		// int y2 = 10;
		// int x1 = 0;
		// int y1 = 0;
		// System.out.println("drawBresenhamLine "
		// + drawBresenhamLine(x1, y1, x2, y2).size());
		// System.out.println("distaciaEntrePontos "
		// + distaciaEntrePontos(x1, y1, x2, y2));
		int media = 0;
		for (int i = 0; i < 100; i++) {
			int x1 = Util.intervalo(10, 20000);
			int x2 = Util.intervalo(10, 20000);
			int y1 = Util.intervalo(10, 20000);
			int y2 = Util.intervalo(10, 20000);
			int drawBresenhamLine = drawBresenhamLine(x1, y1, x2, y2).size();
			int distaciaEntrePontos = (int) distaciaEntrePontos(x1, y1, x2, y2);
			Logger.logar("distaciaEntrePontos " + distaciaEntrePontos);
			media += (distaciaEntrePontos - drawBresenhamLine);
		}
		Logger.logar("media " + (media / 100));
	}

}
