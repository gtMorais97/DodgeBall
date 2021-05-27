package entities;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import entities.Block.Shape;
import utils.*;
import main.*;

public abstract class Agent extends MovingEntity{

    public Ball ball;
	public int team;

	protected List<Ball> ballsInSight;
    protected List<Agent> agentsInSight;

	public Agent(Point point, Color color, int direction, int team){ 
		super(point, color, direction);
		this.team = team;
	} 
	
	
	/**********************
	 **** A: decision ***** 
	 **********************/
	
	public abstract void agentDecision();


	/********************/
	/**** B: sensors ****/
	/********************/

	protected void updateBeliefs(){
        aheadPosition = adjacentPosition(1, this.direction);
        ballsInSight = Utils.cast(getEntitiesInSight("ball"));
        agentsInSight = Utils.cast(getEntitiesInSight("agent"));
    }

	/* Check if agent is carrying box */
	public boolean hasBall() {
		return ball != null;
	}
	
	/* Return the color of the box */
	public Color cargoColor() {
		return ball.color;
	}

	/* Check if the cell ahead is floor (which means not a wall, not a shelf nor a ramp) and there are any robot there */
	public boolean isFreeCell(Point p) {
		if(Field.isWall(p)) return false;
		if(Field.getBlock(p).shape.equals(Shape.free) && !crossingMidField()){
			if(Field.getEntity(p)==null){
				for(Agent ag: Field.agents){
					if(ag.nextPosition.equals(p) && !ag.equals(this))
						return false;
				}
				for(Ball b: Field.balls){
					if(b.nextPosition.equals(p))
						return false;
				}
				return true;
			} 
		}
			
		return false;
	}

	private boolean crossingMidField(){
		return (currentPosition.y == Field.nY/2 && aheadPosition.y == Field.nY/2 - 1) 
				|| (currentPosition.y == Field.nY/2-1 && aheadPosition.y == Field.nY/2);
	}

	/* Check if the cell ahead contains a ball */
	public boolean isBallAhead(){
		Entity entity = Field.getEntity(aheadPosition);
		return entity!=null && entity instanceof Ball;
	}

	/* Check if the cell ahead is a shelf */
	public boolean isCover() {
		Block block = Field.getBlock(aheadPosition);
		return block.shape.equals(Shape.cover);
	}

	protected List<Entity> getEntitiesInSight(String opt) {
		
		Entity[] column = Field.getEntitiesInColumn(currentPosition.x);

		List<Entity> entities = new ArrayList<>();
		for(int i=0; i<column.length ; i++){
			if(column[i] != null){
				if((column[i] instanceof Agent && !column[i].equals(this) && opt.equals("agent"))
					|| (column[i] instanceof Ball && opt.equals("ball")) ){
					
					if(opt.equals("ball"))
						entities.add((Ball)column[i]);
					else if(opt.equals("agent"))
						entities.add((Agent)column[i]);
				}
			}	
		}
		return entities;
	}

	protected boolean ballIncoming(List<Ball> balls){
		if(balls.isEmpty()) 
			return false;

		for(Ball ball: balls){
			if(ball.direction == 0 ||  ball.direction == 180)
				return true;
		}
		return false;
	}

	protected boolean containsAgentFromTeam(int team) {
		if(agentsInSight.isEmpty()) 
			return false;

		for(Agent agent: agentsInSight){
			if(agent.team == team)
				return true;
		}
		return false;
			
	}

	public int oppositeTeam(){
		switch(this.team){
			case 1: return 2;
			case 2: return 1;
			default: return 0;
		}
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
	@Override
	public void moveAhead() {
		//Field.updateEntityPosition(currentPosition,aheadPosition);
		
		if(hasBall()) 
			ball.moveBall(Utils.copyPoint(aheadPosition));
			
		this.nextPosition = Utils.copyPoint(aheadPosition);
		
		
	}

	/* Grab ball */
	public void grabBall() {
	  ball = (Ball) Field.getEntity(aheadPosition);
	  if (ball != null){
		  ball.grabBall(Utils.copyPoint(currentPosition));
		  ball.team = this.team;
	  }
	  	
	}

	/* Drop ball */
	public void dropBall() {
		Point dropPoint;
		if(isFreeCell(aheadPosition))
			dropPoint = Utils.copyPoint(aheadPosition);
		else if(ball.isFreeCell(adjacentPosition(1, (this.direction+90)%360))) 
			dropPoint = adjacentPosition(1, (this.direction+90)%360);
		else if(ball.isFreeCell(adjacentPosition(1, (this.direction+180)%360))) 
			dropPoint = adjacentPosition(1, (this.direction+180)%360);
		else if(ball.isFreeCell(adjacentPosition(1, (this.direction+270)%360))) 
			dropPoint = adjacentPosition(1, (this.direction+270)%360);
		else dropPoint = Utils.copyPoint(this.currentPosition);

		ball.dropBall(dropPoint);
	    ball = null;
	}

	/* Evade ball */
	protected void evade() {
		if((direction == 90 || direction == 270) && isFreeCell(aheadPosition)) //if looking right or left, move
			moveAhead();
		else
			rotateRandomly();
	}

	protected void throwBall() {
		//rotate agent towards the enemy
		if(lowerField() && this.direction != 0)
			this.direction = 0;
		else if(!lowerField() && this.direction != 180)
			this.direction = 180;
		this.aheadPosition = adjacentPosition(1, this.direction);
		if(ball.isFreeCell(this.aheadPosition)){
			this.ball.direction = this.direction;
			dropBall();
		}
		
	}

	/*wether agent is the lower part of the field */
	private boolean lowerField(){
		return this.currentPosition.y < Field.nY/2;
	}


	public void reset() {
		this.ball = null;
		this.currentPosition = null;
		this.nextPosition = null;
		this.aheadPosition = null;

		switch (this.team) {
			case 1:
				this.direction = 180;
				break;
		
			case 2:
				this.direction = 0;
				break;
		}
	}
    
}
