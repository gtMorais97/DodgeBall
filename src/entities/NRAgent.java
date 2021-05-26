package entities;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import main.*;

//Non Reactive Agent
public abstract class NRAgent extends Agent{

    public enum Desire { grabBall, throwBall }
	public enum Action { moveAhead, grabBall, dropBall, throwBall, rotateRandomly, rotateRight, rotateLeft}

    public NRAgent(Point point, Color color, int direction, int team){
        super(point, color, direction, team);
    }

    protected void execute(Action action) {
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

    /*******************************/
	/****** planning auxiliary *****/
	/*******************************/

	protected Queue<Action> buildPathPlan(Point p1, Point p2) {
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
                Point p = new Point(x,y);
    	        if(x==dest.x && y==dest.y) return new Node(dest,curr); 
	            if(!Field.isWall(p) && Field.getEntity(p) == null && !visited[x][y]){ 
	                visited[x][y] = true; 
	    	        q.add(new Node(p, curr)); 
	            } 
	        }
	    }
	    return null; //destination not reached
	}

}