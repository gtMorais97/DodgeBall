package main;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import entities.*;
import entities.Block.Shape;
import utils.*;
import entities.LearningAgent.LearningApproach;

/**
 * Environment
 * @author Rui Henriques
 */
public class Field {

	/** The environment */

	public static int nX = 10, nY = 10;
	private static Block[][] board;
	private static Entity[][] objects;
	public static List<Agent> agentsIMMUTABLE;
	public static List<Agent> team1IMMUTABLE;
	public static List<Agent> team2IMMUTABLE;
	public static List<Agent> agents;
	public static List<Agent> team1;
	public static List<Agent> team2;
	public static List<Agent> agentsToKill;
	public static List<Ball> balls;

	public static int team_size = 3;
	public static int balls_per_team = 1;
	public static int ball_step = 1;
	private static Random random;

	private static enum agentType{
		REACTIVE,
		DELIBERATIVE,
		HYBRID,
		Q,
		SARSA
	}
	public static final agentType team1Type = agentType.REACTIVE;
	public static final agentType team2Type = agentType.DELIBERATIVE;

	
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
		placeObjects("balls", false);
	
		
		/** C: create agents */
		agents = new ArrayList<>();
		team1 = new ArrayList<>();
		team2 = new ArrayList<>();
		placeObjects("agents", false);

		//create unmodifiable lists
		agentsIMMUTABLE = new ArrayList<>();
		team1IMMUTABLE = new ArrayList<>();
		team2IMMUTABLE = new ArrayList<>();
		agentsIMMUTABLE.addAll(agents);
		team1IMMUTABLE.addAll(team1);		
		team2IMMUTABLE.addAll(team2);

		agentsIMMUTABLE = Collections.unmodifiableList(agentsIMMUTABLE);
		team1IMMUTABLE = Collections.unmodifiableList(team1IMMUTABLE);
		team2IMMUTABLE = Collections.unmodifiableList(team2IMMUTABLE);

		
	}

	public static void reinitialize(){
		

		/** A: create board */
		board = new Block[nX][nY];
		for(int i=0; i<nX; i++) 
			for(int j=0; j<nY; j++) 
				board[i][j] = new Block(Shape.free, Color.lightGray);

		objects = new Entity[nX][nY];

		

		/* Reinsert agents*/
		for(Agent agent: agentsIMMUTABLE)
			agent.reset();

		agents = new ArrayList<>();
		team1 = new ArrayList<>();
		team2 = new ArrayList<>();
		agentsToKill = new ArrayList<>();
		agents.addAll(agentsIMMUTABLE);
		team1.addAll(team1IMMUTABLE);
		team2.addAll(team2IMMUTABLE);

		placeObjects("agents", true);

		
		System.out.println(team1.get(0).team);
		System.out.println(team1.get(0).team);
		/* Insert balls*/
		balls = new ArrayList<>();
		placeObjects("balls", false);
	}
	
	/****************************
	 ***** B: BOARD METHODS *****
	 ****************************/

	private static void placeObjects(String opt, boolean reinserting){
		int var;
		switch(opt){
			case "agents":
				var = team_size*2;
				break;
			case "balls":
				var = balls_per_team*2;
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
					Agent agent;
					if(reinserting){
						agent = team1.get(i);
						agent.currentPosition = p1;
						agent.nextPosition = p1;
					}
						
					else{
						Color color = null;
						switch(team1Type){
							case REACTIVE:
								color = Color.YELLOW;
								break;
							case DELIBERATIVE:
								color = Color.BLUE;
								break;
							case HYBRID:
								color = Color.GREEN;
								break;
							case Q:
								color = Color.GRAY;
								break;
							case SARSA:
								color = Color.DARK_GRAY;
								break;
						}
						agent = new ReactiveAgent(p1, color, 180, 1);
						agents.add(agent);
						team1.add(agent);
					}
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
					Agent agent;
					if(reinserting){
						agent = team2.get(i);
						agent.currentPosition = p2;
						agent.nextPosition = p2;
					}
					else{
						Color color = null;
						switch(team2Type){
							case REACTIVE:
								agent = new ReactiveAgent(p2, Color.YELLOW, 0, 2);
								break;
							case DELIBERATIVE:
								agent = new HybridAgent(p2, Color.BLUE, 0, 2, false);
								break;
							case HYBRID:
								agent = new HybridAgent(p2, Color.GREEN, 0, 2, true);
								break;
							case Q:
								agent = new LearningAgent(p2, Color.GRAY, 0, 2, LearningApproach.QLearning);
								break;
							case SARSA:
								agent = new LearningAgent(p2, Color.DARK_GRAY, 0, 2, LearningApproach.QLearning);
								break;
							default:
								agent = new ReactiveAgent(p2, Color.YELLOW, 0, 2);
									break;

						}
						
						agents.add(agent);
						team2.add(agent);
					}
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
		System.out.println();
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
			Agent agent  = (Agent) getEntity(new_point);
			if(!agentsToKill.contains(agent))
				agentsToKill.add(agent);
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
		agents.removeAll(agentsToKill);
		
		Iterator<Agent> it = agentsToKill.iterator();
		while(it.hasNext()){
			Agent agent = it.next();
			if(agent.hasBall())
				agent.dropBall();

			if(agent.team==1) 
				team1.remove(agent);
			else team2.remove(agent);

			GUI.removeObject(agent);
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

		for(Agent agent: team){
			double distance = p.distance(agent.currentPosition);
			if(distance < closestDistance){
				closestDistance = distance;
				closestPoint = Utils.copyPoint(agent.currentPosition);
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
		int total_games = 30;
		HashMap<agentType, Integer> score;

		String filename = "evaluations/" 
						  + team1Type.toString() 
						  + "vs" 
						  + team2Type.toString() 
						  + ".txt";
		

		public RunThread(int time){
			this.time = time*time;
		}
		
	    public void run() {
			score = new HashMap<>();
			score.put(team1Type,0);
			score.put(team2Type,0);

			int gameCounter = 0;
			int it = 0;
	    	while(gameCounter < total_games){
				step();
				it++;
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

				if(it%5 == 0) duckTape();
	    	}
			evaluate();
	    }

		private static void duckTape() {
			objects = new Entity[nX][nY];
			for(Agent agent: agents)
				objects[agent.currentPosition.x][agent.currentPosition.y] = agent;
			
			for(Ball ball: balls)
				if(!ball.beingHeld)
					objects[ball.currentPosition.x][ball.currentPosition.y] = ball;
			printBoard();
		}

		private void updateScore() {
			if(!team1.isEmpty()) 
				score.computeIfPresent(team1Type, (k,v) -> v+1);
			else if(!team2.isEmpty()) 
				score.computeIfPresent(team2Type, (k,v) -> v+1);
		}

		private void evaluate() {
			double averageGameLenght = counter.stream()
										   .mapToInt(i -> i)
										   .average()
										   .orElse(0);
			
			try {
				File file = new File(filename);
				if(!file.exists()){
					file.createNewFile();
				 }

				BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

				writer.write("Total number of games: " + total_games);
				writer.newLine(); 

				writer.write("Average game length: " + averageGameLenght);
				writer.newLine(); 
				writer.newLine();

				writer.write("Score");
				writer.newLine();
				for(Map.Entry<agentType,Integer> entry : score.entrySet()){
					writer.write(entry.getKey() + ":"
									+ entry.getValue());
					writer.newLine();
				}

				writer.newLine();
				writer.write("|-----------------------|");
				writer.newLine();
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
		reinitialize();
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
		//printBoard();
		//System.out.println("Step:  " + steps);
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
