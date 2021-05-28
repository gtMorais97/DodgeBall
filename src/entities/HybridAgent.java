package entities;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import main.*;

public class HybridAgent extends NRAgent {

    
    public Queue<Action> plan;
    private int planIterations;
    private final int numPlanIterationsBeforeReconsider = 3;

    public List<Desire> desires;
	public AbstractMap.SimpleEntry<Desire,Point> intention;

    public boolean hybrid; //when false, only deliberative sub system is active

    public HybridAgent(Point point, Color color, int direction, int team, boolean hybrid) {
        super(point, color, direction, team);
        this.hybrid = hybrid;
        this.plan = new LinkedList<>();
        this.planIterations = 0;
    }

    @Override
    public void agentDecision() {
        updateBeliefs();

        //reactive
        boolean reacted = false;
        if(hybrid)
            reacted = react();

        //deliberative
        if(!reacted){
            if(hasPlan() && !succeededIntention() && possibleIntention()){
                Action action = plan.remove();
                if(isPlanSound(action)){
                    execute(action);
                }
                else buildPlan();
                planIterations++;

                if(reconsider()){
                    deliberate();
                    planIterations = 0;
                } 
            }else{
                deliberate();
                buildPlan();
                if(!hasPlan())
                    agentReactiveDecision();
            }
        }   
    }

    private boolean react(){
        if(ballIncoming(ballsInSight)){
            evade();
            return true;
        }
        
        if(containsAgentFromTeam(oppositeTeam()) 
            && !containsAgentFromTeam(this.team) 
            && hasBall()){
            
                throwBall();
                return true;
        }
        
        if(isBallAhead()){
            grabBall();
            return true;
        }

        return false;
            
    }

    private void agentReactiveDecision() {
        if(Field.isWall(aheadPosition)) 
	        rotateRandomly();

        else if(isBallAhead() && !hasBall()) 
            grabBall();

        else if(containsAgentFromTeam(oppositeTeam()) 
            && !containsAgentFromTeam(this.team) 
            && hasBall())

                throwBall();

        else if(!isFreeCell(aheadPosition)) 
            rotateRandomly();

        else if(random.nextInt(5) == 0) 
            rotateRandomly();

        else moveAhead();
    }

    private void buildPlan() {
        plan = new LinkedList<>();
        if(intention.getValue()==null) return;

        switch(intention.getKey()){
            case throwBall:
                plan = buildPathPlan(currentPosition,getAttackPosition(intention.getValue()));
                if(!plan.isEmpty()) 
                    plan.add(Action.throwBall);
                break;
            case grabBall:
                plan = buildPathPlan(currentPosition,intention.getValue());
                if(!plan.isEmpty()) 
                    plan.add(Action.grabBall);
                break;
        }
    }

    private Point getAttackPosition(Point p) {
        int y;
        if(team==1)
            y = Field.nY/2-1;
        else y = Field.nY/2;
        Point pos = new Point(p.x, y);

        return pos;
    }

    private boolean reconsider() {
        return planIterations >= numPlanIterationsBeforeReconsider;
    }

    private void rebuildPlan() {
    }

    

    private boolean isPlanSound(Action action) {
        switch (action) {
            case throwBall:
                return containsAgentFromTeam(oppositeTeam()) 
                        && !containsAgentFromTeam(this.team) 
                        && hasBall();
        
            case grabBall:
                return !hasBall() && Field.getEntity(aheadPosition) instanceof Ball;
            
            case moveAhead:
                return isFreeCell(aheadPosition);

            default: return true;
        }
    }

    private boolean possibleIntention() {
        switch(intention.getKey()){
            case throwBall:
                return hasBall();
            case grabBall:
                return !hasBall();
            default:
                return false;
        }
    }

    private boolean succeededIntention() {
        switch(intention.getKey()){
            case throwBall:
                return !hasBall();
            case grabBall:
                return hasBall();
            default:
                return false;
        }
    }

    private boolean hasPlan() {
        return !plan.isEmpty();
    }

    private void deliberate(){
        desires = new ArrayList<>();
        if(hasBall()){
            desires.add(Desire.throwBall);
        }else desires.add(Desire.grabBall);

        intention = new AbstractMap.SimpleEntry<>(desires.get(0),null);

        Point targetPoint;
        switch (intention.getKey()) {
            case throwBall:
                targetPoint = Field.getClosestEnemy(this.currentPosition, this.team);
                intention.setValue(targetPoint);
                break;
            case grabBall:
                targetPoint = Field.getClosestBall(this.currentPosition);
                intention.setValue(targetPoint);
                break;
        }
    }

    
    
}
