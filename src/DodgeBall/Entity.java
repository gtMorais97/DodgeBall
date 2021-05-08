package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

/**
 * @author Rui Henriques
 */
public class Entity extends Thread {

	public Point point;
	public Color color;
	protected Random random;
	public int direction;

	public Entity(Point point, Color color){
		this.point = point;
		this.color = color;
		this.random = new Random();
	} 

	/* Position ahead */
	protected Point aheadPosition(int step) {
		Point newpoint = new Point(point.x,point.y);
		switch(direction) {
			case 0: newpoint.y += step; break;
			case 90: newpoint.x += step; break;
			case 180: newpoint.y -= step; break;
			case 270: newpoint.x -= step; break;
			default: return null;
		}
		return newpoint;
	}

	protected boolean isWall(Point p){
		return p.x < 0 || p.x > Field.nX-1 || p.y < 0 || p.y > Field.nY -1;
	}
}
