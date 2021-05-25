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
		updateBeliefs();
		
	  if(ballIncoming(ballsInSight)) 
		evade();

	  else if(Field.isWall(aheadPosition)) 
	  	rotateRandomly();

	  else if(isBallAhead() && !hasBall()) 
	  	grabBall();

	  else if(hasBall() && containsAgentFromTeam(oppositeTeam()))
			throwBall();

	  else if(!isFreeCell(aheadPosition)) 
	  	rotateRandomly();

	  else if(random.nextInt(5) == 0) 
	  	rotateRandomly();

	  else moveAhead();
	}	

}
