package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

public class HybridAgent extends Agent {

    public enum Desire { grabBall, throwBall }
	public enum Action { moveAhead, grabBall, dropBall, throwBall, rotateRandomly, rotateRight, rotateLeft}

    public Queue<Action> plan;
    private int planIterations;
    private final int numPlanIterationsBeforeReconsider = 3;

    public List<Desire> desires;
	public AbstractMap.SimpleEntry<Desire,Point> intention;

    public HybridAgent(Point point, Color color, int direction, int team) {
        super(point, color, direction, team);
        plan = new LinkedList<>();
        planIterations = 0;
    }

    @Override
    public void agentDecision() {
        updateBeliefs();

        //reactive
        boolean reacted = react();

        //deliberative
        if(!reacted){
            if(hasPlan() && !succeededIntention() && possibleIntention()){
                Action action = plan.peek();
                if(isPlanSound(action)){
                    execute(action);
                    plan.remove();
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

        else if(!isFreeCell()) 
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
                plan.add(Action.throwBall);
                break;
            case grabBall:
                plan = buildPathPlan(currentPosition,intention.getValue());
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
        System.out.println(pos);
        return pos;
    }

    private boolean reconsider() {
        return planIterations >= numPlanIterationsBeforeReconsider;
    }

    private void rebuildPlan() {
    }

    private void execute(Action action) {
        switch (action) {
            case throwBall:
                throwBall();
                break;
        
            case grabBall:
                grabBall();
                break;
            
            case dropBall:
                dropBall();
                break;
            
            case moveAhead:
                moveAhead();
                break;
            
            case rotateRight:
                rotateRight();
                break;
            
            case rotateLeft:
                rotateLeft();
                break;
            
            case rotateRandomly:
                rotateRandomly();
                break;

        }
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
                return isFreeCell();

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

    /*******************************/
	/****** planning auxiliary *****/
	/*******************************/

	private Queue<Action> buildPathPlan(Point p1, Point p2) {
		Stack<Point> path = new Stack<Point>();
		Node node = shortestPath(p1,p2);
		path.add(node.point);
		while(node.parent!=null) {
			node = node.parent;
			path.push(node.point);
		}
		Queue<Action> result = new LinkedList<Action>();
		p1 = path.pop();
		int auxdirection = direction;
		while(!path.isEmpty()) {
			p2 = path.pop();
			result.add(Action.moveAhead);
			result.addAll(rotations(p1,p2));
			p1 = p2;
		}
		direction = auxdirection;
		result.remove();
		return result;
	}
	
	private List<Action> rotations(Point p1, Point p2) {
		List<Action> result = new ArrayList<Action>();
		while(!p2.equals(aheadPosition)) {
			Action action = rotate(p1,p2);
			if(action==null) break;
			execute(action);
			result.add(action);
		}
		return result;
	}

	private Action rotate(Point p1, Point p2) {
		boolean vertical = Math.abs(p1.x-p2.x)<Math.abs(p1.y-p2.y);
		boolean upright = vertical ? p1.y<p2.y : p1.x<p2.x;
		if(vertical) {  
			if(upright) { //move up
				if(direction!=0) return direction==90 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=180) return direction==90 ? Action.rotateRight : Action.rotateLeft;
		} else {
			if(upright) { //move right
				if(direction!=90) return direction==180 ? Action.rotateLeft : Action.rotateRight;
			} else if(direction!=270) return direction==180 ? Action.rotateRight : Action.rotateLeft;
		}
		return null;
	}

    //For queue used in BFS 
	public class Node { 
	    Point point;   
	    Node parent; //cell's distance to source 
	    public Node(Point point, Node parent) {
	    	this.point = point;
	    	this.parent = parent;
	    }
	    public String toString() {
	    	return "("+point.x+","+point.y+")";
	    }
	} 
	
	public Node shortestPath(Point src, Point dest) { 
	    boolean[][] visited = new boolean[100][100]; 
	    visited[src.x][src.y] = true; 
	    Queue<Node> q = new LinkedList<Node>(); 
	    q.add(new Node(src,null)); //enqueue source cell 
	    
		//access the 4 neighbours of a given cell 
		int row[] = {-1, 0, 0, 1}; 
		int col[] = {0, -1, 1, 0}; 
	     
	    while (!q.isEmpty()){//do a BFS 
	        Node curr = q.remove(); //dequeue the front cell and enqueue its adjacent cells
	        Point pt = curr.point; 
			//System.out.println(">"+pt);
	        for (int i = 0; i < 4; i++) { 
	            int x = pt.x + row[i], y = pt.y + col[i]; 
    	        if(x==dest.x && y==dest.y) return new Node(dest,curr); 
	            if(!Field.isWall(new Point(x,y)) && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(new Point(x,y), curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	}
    
}
