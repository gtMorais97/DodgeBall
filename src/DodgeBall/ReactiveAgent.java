package DodgeBall;

import java.awt.Color;
import java.awt.Point;

import DodgeBall.Block.Shape;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class ReactiveAgent extends Entity {

	public Ball ball;
	private Point ahead;

	public ReactiveAgent(Point point, Color color){ 
		super(point, color);
	} 
	
	
	/**********************
	 **** A: decision ***** 
	 **********************/
	
	public void agentDecision() {
	  //System.out.println(this.point.toString());
	  ahead = aheadPosition(1);
	  Ball ball = (Ball) getEntityInSight("ball");
	  ReactiveAgent agentInSight = (ReactiveAgent) getEntityInSight("agent");
	  if(ballIncoming(ball)) 
	  	evade();

	  if(isWall(ahead)) 
	  	rotateRandomly();

	  else if(isBallAhead() && !hasBall()) 
	  	grabBall();

	  else if(hasBall() && enemyAgent(agentInSight))
		throwBall();

	  else if(!isFreeCell()) 
	  	rotateRandomly();

	  else if(random.nextInt(5) == 0) 
	  	rotateRandomly();

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

	/* Check if the cell ahead is floor (which means not a wall, not a shelf nor a ramp) and there are any robot there */
	public boolean isFreeCell() {
		if(isWall(ahead)) return false;
		if(Field.getBlock(ahead).shape.equals(Shape.free) && !crossingMidField())
			if(Field.getEntity(ahead)==null) return true;
		return false;
	}

	private boolean crossingMidField(){
		return (point.y == Field.nY/2 && ahead.y == Field.nY/2 - 1) || (point.y == Field.nY/2-1 && ahead.y == Field.nY/2);
	}

	/* Check if the cell ahead contains a ball */
	public boolean isBallAhead(){
		Entity entity = Field.getEntity(ahead);
		return entity!=null && entity instanceof Ball;
	}

	/* Check if the cell ahead is a shelf */
	public boolean isCover() {
		Block block = Field.getBlock(ahead);
		return block.shape.equals(Shape.cover);
	}

	private Entity getEntityInSight(String opt) {
		
		Entity[] column = Field.getEntitiesInColumn(point.x);
		for(int i=0; i<column.length ; i++){
			if(column[i] != null){
				if((column[i] instanceof ReactiveAgent && !column[i].equals(this) && opt.equals("agent"))
					|| (column[i] instanceof Ball && opt.equals("ball")) ){
					return column[i];
				}
			}	
		}
		return null;
	}

	private boolean ballIncoming(Ball ball){
		if(ball == null) 
			return false;

		return ball.direction == 0 ||  ball.direction == 180;
	}

	private boolean enemyAgent(ReactiveAgent agent) {
		if(agent == null) return false;

		if(point.y < Field.nY/2){               //if this agent is on the top part of the field, 
			return agent.point.y >= Field.nY/2; //verify if the other agent is on the bottom part
		}else return agent.point.y < Field.nY/2;
			
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
		if(direction<0) direction += 360;
	}
	
	/* Move agent forward */
	public void moveAhead() {
		Field.updateEntityPosition(point,ahead);
		if(hasBall()) 
			ball.moveBall(ahead);
		point = ahead;
	}

	/* Grab ball */
	public void grabBall() {
	  ball = (Ball) Field.getEntity(ahead);
	  ball.grabBall(point);
	}

	/* Drop ball */
	public void dropBall() {
		ball.dropBall(ahead);
	    ball = null;
	}

	/* Evade ball */
	private void evade() {
		if((direction == 90 || direction == 270) && isFreeCell()) //if looking right or left, move
			moveAhead();
		else
			rotateRandomly();
	}

	private void throwBall() {
		this.ball.direction = this.direction;
		dropBall();
	}
	
}
