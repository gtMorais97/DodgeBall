package entities;

import java.awt.Color;
import java.awt.Point;

import entities.Block.Shape;
import utils.*;
import main.*;

public class Ball extends MovingEntity {

	public boolean beingHeld = false;

	public Ball(Point point, Color color, int direction) {
		super(point, color, direction);
	}
	
	/*****************************
	 ***** AUXILIARY METHODS ***** 
	 *****************************/

	public void grabBall(Point newpoint) {
		Field.removeEntity(currentPosition);
		beingHeld = true;
		nextPosition = newpoint;
	}
	
	public void dropBall(Point newpoint) {
		
		if(Field.getEntity(newpoint) instanceof Agent){
			Agent agent = (Agent) Field.getEntity(newpoint);
			Field.agentsToKill.add(agent);
		}
		beingHeld = false;
		currentPosition = newpoint;
		nextPosition = Utils.copyPoint(currentPosition);
		
		Field.insertEntity(this, nextPosition);	
	}

	/* move ball to specific position */
	public void moveBall(Point newpoint) {
		nextPosition = newpoint;
	}

	public void getNextPosition(int step){
		this.aheadPosition = aheadPosition(step);
		if(aheadPosition == null) return; //ball is not moving

		if(!Field.isWall(aheadPosition)){
			if(isFreeCell(aheadPosition)){
				moveAhead();
			}else this.direction = -1; //stop moving
		}
		else this.direction = -1; //stop moving
	}

	public boolean isFreeCell(Point p) {
		if(Field.getBlock(p).shape.equals(Shape.free))
			if(Field.getEntity(p) == null || Field.getEntity(p) instanceof Agent) 
				return true;
		return false;
	}

	@Override
	public void moveAhead(){
		int step = 1;
		do{
			nextPosition = new Point(aheadPosition.x, aheadPosition.y);
			if(!isFreeCell(nextPosition)){
				aheadPosition = aheadPosition(step);
				step++;
			}
		}while(!isFreeCell(nextPosition));
		
	
	}
}
