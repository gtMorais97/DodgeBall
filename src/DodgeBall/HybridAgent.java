package DodgeBall;

import java.awt.Color;
import java.awt.Point;

public class HybridAgent extends Agent {

    public HybridAgent(Point point, Color color, int direction, int team) {
        super(point, color, direction, team);
    }

    @Override
    public void agentDecision() {
        aheadPosition = aheadPosition(1);
        Ball ball = (Ball) getEntityInSight("ball");
        ReactiveAgent agentInSight = (ReactiveAgent) getEntityInSight("agent");

        //reactive
        if(ballIncoming(ball)){
            evade(); 
        }
        
        //deliberative
        else{
            
        }   
    }
    
}
