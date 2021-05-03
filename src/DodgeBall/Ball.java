package DodgeBall;

import java.awt.Color;
import java.awt.Point;

public class Ball extends Entity {

	public Ball(Point point, Color color) {
		super(point, color);
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void grabBall(Point newpoint) {
		Field.removeEntity(point);
		point = newpoint;
	}
	
	public void dropBall(Point newpoint) {
		Field.insertEntity(this,newpoint);
		point = newpoint;
	}

	public void moveBox(Point newpoint) {
		point = newpoint;
	}
}
