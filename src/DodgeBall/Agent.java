package DodgeBall;

import java.awt.Color;
import java.awt.Point;

import DodgeBall.Block.Shape;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class Agent extends Entity {

	public int direction = 90;
	public Ball ball;
	private Point ahead;
	public static int NUM_BOXES = 8;

	public Agent(Point point, Color color){ 
		super(point, color);
	} 
	
	
	/**********************
	 **** A: decision ***** 
	 **********************/
	
	public void agentDecision() {
	  ahead = aheadPosition();
	  if(isWall()) rotateRandomly();
	  else if(isBallAhead() && !hasBall()) grabBall();
	  else if(isCover() && !isBallAhead() && hasBall() && shelfColor().equals(cargoColor())) dropBall();
	  else if(!isFreeCell()) rotateRandomly();
	  else if(random.nextInt(5) == 0) rotateRandomly();
	  else moveAhead();
	}
	
	/********************/
	/**** B: sensors ****/
	/********************/
	/* Check if agent is carrying box */
	public boolean hasBall() {
		return ball != null;
	}
	
	/* Return the color of the box */
	public Color cargoColor() {
	  return ball.color;
	}

	/* Return the color of the shelf ahead or 0 otherwise */
	public Color shelfColor(){
		Point ahead = aheadPosition();
		return Field.getBlock(ahead).color;
	}

	/* Check if the cell ahead is floor (which means not a wall, not a shelf nor a ramp) and there are any robot there */
	public boolean isFreeCell() {
	  if(Field.getBlock(ahead).shape.equals(Shape.free))
		  if(Field.getEntity(ahead)==null) return true;
	  return false;
	}

	/* Check if the cell ahead contains a box */
	public boolean isBallAhead(){
		Entity entity = Field.getEntity(ahead);
		return entity!=null && entity instanceof Ball;
	}

	/* Check if the cell ahead is a shelf */
	public boolean isCover() {
	  Block block = Field.getBlock(ahead);
	  return block.shape.equals(Shape.cover);
	}


	/* Check if the cell ahead is a wall */
	private boolean isWall() {
		return ahead.x<0 || ahead.y<0 || ahead.x>=Field.nX || ahead.y>=Field.nY;
	}

	/**********************/
	/**** C: actuators ****/
	/**********************/

	/* Rotate agent to right */
	public void rotateRandomly() {
		if(random.nextBoolean()) rotateLeft();
		else rotateRight();
	}
	
	/* Rotate agent to right */
	public void rotateRight() {
		direction = (direction+90)%360;
	}
	
	/* Rotate agent to left */
	public void rotateLeft() {
		direction = (direction-90)%360;
	}
	
	/* Move agent forward */
	public void moveAhead() {
		Field.updateEntityPosition(point,ahead);
		if(hasBall()) ball.moveBox(ahead);
		point = ahead;
	}

	/* Cargo box */
	public void grabBall() {
	  ball = (Ball) Field.getEntity(ahead);
	  ball.grabBall(point);
	}

	/* Drop box */
	public void dropBall() {
		ball.dropBall(ahead);
	    ball = null;
	}
	
	/**********************/
	/**** D: auxiliary ****/
	/**********************/

	/* Position ahead */
	private Point aheadPosition() {
		Point newpoint = new Point(point.x,point.y);
		switch(direction) {
			case 0: newpoint.y++; break;
			case 90: newpoint.x++; break;
			case 180: newpoint.y--; break;
			default: newpoint.x--; 
		}
		return newpoint;
	}
}
