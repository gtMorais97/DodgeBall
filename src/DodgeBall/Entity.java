package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.util.Random;

/**
 * @author Rui Henriques
 */
public class Entity extends Thread {

	public Point currentPosition;
	public Color color;
	protected Random random;

	public Entity(Point point, Color color){
		this.currentPosition = point;
		this.color = color;
		this.random = new Random();
	} 

}
