package DodgeBall;

import java.awt.Color;
import java.awt.Point;

import DodgeBall.Block.Shape;

public class Ball extends Entity {

	public Ball(Point point, Color color) {
		super(point, color);
		this.direction = -1; //not moving
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

	public void moveBall(Point newpoint) {
		point = newpoint;
	}

	public void updatePosition(int step){
		Point nextPoint = aheadPosition(step);
		if(nextPoint == null) return; //ball is not moving

		if(!isWall(nextPoint)){
			if(isFreeCell(nextPoint)){
				Field.updateEntityPosition(point, nextPoint);
				moveBall(nextPoint);
			}else this.direction = -1; //stop moving
		}
		else this.direction = -1; //stop moving
	}

	public boolean isFreeCell(Point p) {
		if(Field.getBlock(p).shape.equals(Shape.free))
			if(Field.getEntity(p) == null) 
				return true;
		return false;
	}

	

	
}
