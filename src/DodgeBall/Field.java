package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import DodgeBall.Block.Shape;

/**
 * Environment
 * @author Rui Henriques
 */
public class Field {

	/** The environment */

	public static int nX = 10, nY = 10;
	private static Block[][] board;
	private static Entity[][] objects;
	private static List<Agent> agents;
	private static List<Ball> balls;

	private static int n_agents = 4;
	private static int n_balls = 2;
	private static Random random;
	
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {
		random = new Random();
		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++) 
			for(int j=0; j<nY; j++) 
				board[i][j] = new Block(Shape.free, Color.lightGray);
				
		/** B: create balls */
		balls = new ArrayList<Ball>();
		placeObjects("balls");

		
		/** C: create agents */
		agents = new ArrayList<Agent>();
		placeObjects("agents");
		
		objects = new Entity[nX][nY];
		for(Ball ball : balls) objects[ball.point.x][ball.point.y]=ball;
		for(Agent agent : agents) objects[agent.point.x][agent.point.y]=agent;
	}
	
	

	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/
	
	public static Entity getEntity(Point point) {
		return objects[point.x][point.y];
	}
	public static Block getBlock(Point point) {
		return board[point.x][point.y];
	}
	public static void updateEntityPosition(Point point, Point newpoint) {
		objects[newpoint.x][newpoint.y] = objects[point.x][point.y];
		objects[point.x][point.y] = null;
	}	
	public static void removeEntity(Point point) {
		objects[point.x][point.y] = null;
	}
	public static void insertEntity(Entity entity, Point point) {
		objects[point.x][point.y] = entity;
	}

	private static void placeObjects(String opt){
		int var = 1;
		switch(opt){
			case "agents":
				var = n_agents;
				break;
			case "balls":
				var = n_balls;
		}
		if(var % 2 != 0 && opt.equals("balls")) var++; //just to make sure n_balls is even
		for(int i=0; i<var/2 ; i++){
			int x1 = random.nextInt(nX/2); 
			int y1 = random.nextInt(nY/2);
			if(opt.equals("balls"))
				balls.add(new Ball(new Point(x1, y1), Color.RED));
			else if(opt.equals("agents"))
				agents.add(new Agent(new Point(x1, y1), Color.GREEN));

			int x2 = random.nextInt(nX-nX/2) + nX/2; 
			int y2 = random.nextInt(nY-nY/2) + nY/2;
			if(opt.equals("balls"))
				balls.add(new Ball(new Point(x2, y2), Color.RED));
			else if(opt.equals("agents"))
				agents.add(new Agent(new Point(x2, y2), Color.BLUE));	
		}
	}


	/***********************************
	 ***** C: ELICIT AGENT ACTIONS *****
	 ***********************************/
	
	private static RunThread runThread;
	private static GUI GUI;
	private static int steps = 0;
	private static int totalSteps = 0;
	private static ArrayList <Integer> counter = new ArrayList<Integer>();

	public static class RunThread extends Thread {
		
		int time;

		public RunThread(int time){
			this.time = time*time;
		}
		
	    public void run() {
	    	while(true){
	    		step();
				try {
					sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
	    }
	}
	
	public static void run(int time) {
		Field.runThread = new RunThread(time);
		Field.runThread.start();
	}

	public static void reset() {
		removeObjects();
		initialize();
		GUI.displayBoard();
		displayObjects();	
		GUI.update();
		counter.add(steps);
		steps = 0;
		System.out.println("Reset. Counter state:");
		System.out.println(counter);
		System.out.println("Total Steps: " + totalSteps);
	}

	public static void step() {
		removeObjects();
		for(Agent a : agents) a.agentDecision();
		displayObjects();
		GUI.update();				
		steps++;
		totalSteps++;
		System.out.println("Step:  " + steps);
	}

	public static void stop() {
		runThread.interrupt();
		runThread.stop();
	}

	public static void displayObjects(){
		for(Agent agent : agents) GUI.displayObject(agent);
		for(Ball ball : balls) GUI.displayObject(ball);
	}
	
	public static void removeObjects(){
		for(Agent agent : agents) GUI.removeObject(agent);
		for(Ball ball : balls) GUI.removeObject(ball);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}
}
