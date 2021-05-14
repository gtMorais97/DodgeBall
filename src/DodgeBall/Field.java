package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
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
	public static List<ReactiveAgent> agents;
	public static List<ReactiveAgent> agentsToKill;
	public static List<Ball> balls;

	private static int team_size = 3;
	private static int n_balls_per_team = 1;
	private static int ball_step = 1;
	private static Random random;
	
	
	/****************************
	 ***** A: SETTING BOARD *****
	 ****************************/
	
	public static void initialize() {
		random = new Random();
		agentsToKill = new ArrayList<>();
		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++) 
			for(int j=0; j<nY; j++) 
				board[i][j] = new Block(Shape.free, Color.lightGray);
				
		/** B: create balls */
		balls = new ArrayList<Ball>();
		placeObjects("balls");
	
		
		/** C: create agents */
		agents = new ArrayList<ReactiveAgent>();
		placeObjects("agents");
		
		objects = new Entity[nX][nY];
		for(Ball ball : balls) 
			objects[ball.currentPosition.x][ball.currentPosition.y] = ball;
		for(ReactiveAgent agent : agents)
			objects[agent.currentPosition.x][agent.currentPosition.y] = agent;
	}
	
	

	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/

	public static boolean gameEnded(){
		HashSet<Integer> teams = new HashSet<>();
		for(ReactiveAgent ag: agents){
			teams.add(ag.team);
			if(teams.size() > 1)
				return false;
		}

		return true;
	}

	public static void printBoard(){
		for(int y=nY-1; y>=0 ; y--){
			for(int x=0; x < nX; x++){
				if(objects[x][y] instanceof ReactiveAgent)
					System.out.print("A");
				else if(objects[x][y] instanceof Ball)
					System.out.print("B");
				else System.out.print(".");
			}
			System.out.println();
		}
	}

	public static Entity[] getEntitiesInColumn(int x){
		Entity[] column = new Entity[nY];
		for(int y = 0; y < nY; y++)
			column[y] = objects[x][y];
		
		return column;
	}
	
	public static Entity getEntity(Point point) {
		return objects[point.x][point.y];
	}
	public static Block getBlock(Point point) {
		return board[point.x][point.y];
	}
	public static void updateEntityPosition(MovingEntity entity) {
		if(entity instanceof Ball){
			System.out.println();
		}
		Point old_point = entity.currentPosition;
		Point new_point = entity.nextPosition;

		if(old_point.equals(new_point)) return;

		if(getEntity(new_point) instanceof ReactiveAgent && entity instanceof Ball){
			ReactiveAgent ag  =(ReactiveAgent) getEntity(new_point);
			agentsToKill.add(ag);
		}
			

		objects[new_point.x][new_point.y] = objects[old_point.x][old_point.y];
		objects[old_point.x][old_point.y] = null;
		
	}	
	public static void removeEntity(Point point) {
		objects[point.x][point.y] = null;
	}
	public static void insertEntity(Entity entity, Point point) {
		objects[point.x][point.y] = entity;
	}

	public static void killAgents(){
		for(ReactiveAgent ag: agentsToKill){
			//removeEntity(ag.currentPosition);
			agents.remove(ag);
			GUI.removeObject(ag);
		}
		agentsToKill.clear();
	}

	private static void placeObjects(String opt){
		int var = 1;
		switch(opt){
			case "agents":
				var = team_size*2;
				break;
			case "balls":
				var = n_balls_per_team*2;
		}
		
		for(int i=0; i<var/2 ; i++){
			int x1 = random.nextInt(nX); 
			int y1 = random.nextInt(nY/2);
			if(opt.equals("balls"))
				balls.add(new Ball(new Point(x1, y1), Color.RED, -1));
			else if(opt.equals("agents")){
				agents.add(new ReactiveAgent(new Point(x1, y1), Color.GREEN, 0, 0));
			}
				

			int x2 = random.nextInt(nX); 
			int y2 = random.nextInt(nY-nY/2) + nY/2;
			if(opt.equals("balls"))
				balls.add(new Ball(new Point(x2, y2), Color.RED, -1));
			else if(opt.equals("agents")){
				agents.add(new ReactiveAgent(new Point(x2, y2), Color.BLUE, 180, 1));	
			}
				
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
			int total_games = 0;
	    	while(total_games < 5){
	    		step();
				if(gameEnded()){
					total_games++;
					reset();
				}
				try {
					sleep(time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
			evaluate();
	    }

		private void evaluate() {
			System.out.println("evaluating");
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
		for(ReactiveAgent a : agents) a.agentDecision();
		for(Ball b: balls) b.getNextPosition(ball_step);
		killAgents();
		displayObjects();
		GUI.update();			
		steps++;
		totalSteps++;
		printBoard();
		System.out.println("Step:  " + steps);
	}

	public static void stop() {
		runThread.interrupt();
		runThread.stop();
	}

	public static void displayObjects(){
		for(ReactiveAgent agent : agents){
			updateEntityPosition(agent);
			agent.updatePosition();
			GUI.displayObject(agent);
		} 
		for(Ball ball : balls){
			if(!ball.beingHeld)
				updateEntityPosition(ball);

			ball.updatePosition();
			GUI.displayObject(ball);

			
		} 
	}
	
	public static void removeObjects(){
		for(ReactiveAgent agent : agents) GUI.removeObject(agent);
		for(Ball ball : balls) GUI.removeObject(ball);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}
}
