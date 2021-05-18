package DodgeBall;

import java.awt.Color;
import java.awt.Point;

/**
 * Agent behavior
 * @author Rui Henriques
 */
public class ReactiveAgent extends Agent {
	
	public ReactiveAgent(Point point, Color color, int direction, int team) {
		super(point, color, direction, team);
	}


	/**********************
	 **** A: decision ***** 
	 **********************/
	
	public void agentDecision() {
	  aheadPosition = aheadPosition(1);
	  Ball ball = (Ball) getEntityInSight("ball");
	  ReactiveAgent agentInSight = (ReactiveAgent) getEntityInSight("agent");
	  if(ballIncoming(ball)) 
	  	evade();

	  else if(isWall(aheadPosition)) 
	  	rotateRandomly();

	  else if(isBallAhead() && !hasBall()) 
	  	grabBall();

	  else if(hasBall() && isEnemyAgent(agentInSight))
		throwBall();

	  else if(!isFreeCell()) 
	  	rotateRandomly();

	  else if(random.nextInt(5) == 0) 
	  	rotateRandomly();

	  else moveAhead();
	}	

}
