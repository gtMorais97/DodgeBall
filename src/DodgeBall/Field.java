package DodgeBall;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
	public static List<Agent> agents;
	public static List<Agent> team1;
	public static List<Agent> team2;
	public static List<Agent> agentsToKill;
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
		
		objects = new Entity[nX][nY];
		
		/** B: create balls */
		balls = new ArrayList<>();
		placeObjects("balls");
	
		
		/** C: create agents */
		agents = new ArrayList<>();
		team1 = new ArrayList<>();
		team2 = new ArrayList<>();
		placeObjects("agents");
		
	}
	
	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/

	private static void placeObjects(String opt){
		int var;
		switch(opt){
			case "agents":
				var = team_size*2;
				break;
			case "balls":
				var = n_balls_per_team*2;
				break;
			default:
				var = 2;
		}
		
		boolean topHalf = true;
		for(int i=0; i<var/2 ; i++){

			Point p1 = getRandomPoint(topHalf);
			switch(opt){
				case "balls":
					Ball ball = new Ball(p1, Color.RED, -1);
					balls.add(ball);
					objects[p1.x][p1.y] = ball;
					break;
				case "agents":
					Agent agent = new HybridAgent(p1, Color.GREEN, 180, 1);
					agents.add(agent);
					team1.add(agent);
					objects[p1.x][p1.y] = agent;
					
					break;
			}
			
			
			
			Point p2 = getRandomPoint(!topHalf);
			switch(opt){
				case "balls":
					Ball ball = new Ball(p2, Color.RED, -1);
					balls.add(ball);
					objects[p2.x][p2.y] = ball;
					break;
				case "agents":
					Agent agent = new ReactiveAgent(p2, Color.BLUE, 0, 2);
					agents.add(agent);	
					team2.add(agent);
					objects[p2.x][p2.y] = agent;
					break;
			}
				
		}
	}

	private static Point getRandomPoint(boolean topHalf) {
		Point p = null;
		do{
			int x = random.nextInt(nX);
			int y = 0;
			if(topHalf)
				y = random.nextInt(nY-nY/2) + nY/2;
			else
				y = random.nextInt(nY/2);
			p = new Point(x,y);
		}while(getEntity(p) != null);
		
		return p;
	}

	public static boolean gameEnded(){
		return team1.isEmpty() || team2.isEmpty();
	}

	public static void printBoard(){
		for(int y=nY-1; y>=0 ; y--){
			for(int x=0; x < nX; x++){
				if(objects[x][y] instanceof Agent)
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
		if(isWall(point)) return null;
		return objects[point.x][point.y];
	}

	

	public static Block getBlock(Point point) {
		if(isWall(point)) return null;
		return board[point.x][point.y];
	}
	public static void updateEntityPosition(MovingEntity entity) {
		Point old_point = entity.currentPosition;
		Point new_point = entity.nextPosition;

		if(old_point.equals(new_point)) return;

		if(getEntity(new_point) instanceof Agent && entity instanceof Ball){
			Agent ag  = (Agent) getEntity(new_point);
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
		for(Agent ag: agentsToKill){
			if(ag.hasBall())
				ag.dropBall();

			agents.remove(ag);

			if(ag.team==1) 
				team1.remove(ag);
			else team2.remove(ag);

			GUI.removeObject(ag);
		}
		agentsToKill.clear();
	}

	public static boolean isWall(Point p){
		return p.x < 0 || p.x > nX-1 || p.y < 0 || p.y > nY -1;
	}
	

	public static Point getClosestEnemy(Point p, int agentTeam) {
		double closestDistance = Double.MAX_VALUE;
		Point closestPoint = null;
		List<Agent> team = null;
        if(agentTeam==1) team = team2;
		else team = team1;

		for(Agent ag: team){
			double distance = p.distance(ag.currentPosition);
			if(distance < closestDistance){
				closestDistance = distance;
				closestPoint = Utils.copyPoint(ag.currentPosition);
			}
		}

		return closestPoint;
    }

	public static Point getClosestBall(Point p) {
        double closestDistance = Double.MAX_VALUE;
		Point closestPoint = null;

		for(Ball ball: balls){
			if(!(topHalf(p) == topHalf(ball.currentPosition))
				|| ball.currentPosition.y == nY/2 || ball.currentPosition.y == (nY/2)-1)
				continue;

			double distance = p.distance(ball.currentPosition);
			if(distance < closestDistance){
				closestDistance = distance;
				closestPoint = Utils.copyPoint(ball.currentPosition);
			}
		}

		return closestPoint;
    }

	public static boolean topHalf(Point p){
		return (p.y>=nY/2);
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
		HashMap<Integer, Integer> score;

		public RunThread(int time){
			this.time = time*time;
		}
		
	    public void run() {
			int total_games = 5;

			score = new HashMap<>();
			score.put(1,0);
			score.put(2,0);

			int gameCounter = 0;
	    	while(gameCounter < total_games){
	    		step();
				if(gameEnded()){
					gameCounter++;
					updateScore();
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

		private void updateScore() {
			if(!team1.isEmpty()) 
				score.computeIfPresent(1, (k,v) -> v+1);
			else if(!team2.isEmpty()) 
				score.computeIfPresent(2, (k,v) -> v+1);
		}

		private void evaluate() {
			double averageGameLenght = counter.stream()
										   .mapToInt(i -> i)
										   .average()
										   .orElse(0);
			
			try {
				
				String filename = "evaluations/hybridVSreactive.txt";
				BufferedWriter writer = new BufferedWriter(new FileWriter(filename));

				writer.write("Average game length: " + averageGameLenght);
				writer.newLine(); 
				writer.newLine();

				writer.write("Score");
				writer.newLine();
				for(Map.Entry<Integer,Integer> entry : score.entrySet()){
					writer.write(entry.getKey() + ":"
									+ entry.getValue());
					writer.newLine();
				}
				
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("Error creating evaluation file");
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
		for(Agent agent : agents){
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
		for(Agent agent : agents) GUI.removeObject(agent);
		for(Ball ball : balls) GUI.removeObject(ball);
	}
	
	public static void associateGUI(GUI graphicalInterface) {
		GUI = graphicalInterface;
	}



    



    
}
