package entities;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import main.*;

public class LearningAgent extends NRAgent {


    public LearningAgent(Point point, Color color, int direction, int team){
        super(point, color, direction, team);
    }

    @Override
    public void agentDecision() {
        aheadPosition = aheadPosition(1); //percept
		int originalState = getQState();
		Action originalAction = selectAction();
		execute(originalAction);

		//proactiveDecision(); /* DBI */
		//reactiveDecision();
		learningDecision(originalState,originalAction); /* RL */
    }

    /************************
	 **** A: Q-learning ***** 
	 ************************/

	enum ActionSelection  { eGreedy, softMax }
	enum LearningApproach { QLearning, SARSA }

	ActionSelection actionSelection = ActionSelection.softMax;
	LearningApproach learningApproach = LearningApproach.QLearning;

	int it = 0, total = 1000000, directions = 4;
	double discount = 0.9, learningRate = 0.8;
	double epsilon = 0.9, randfactor = 0.05, dec;
	
	double[][] q;
	List<Action> actions;

    /* Creates the initial Q-value function structure: (x y action) <- 0 */
	public void initQFunction(){
		actions = new ArrayList<Action>(EnumSet.allOf(Action.class));
		q = new double[Field.nX*Field.nY*directions*Field.balls_per_team*2][actions.size()]; //2 -> holding or not holding a ball
		dec = (epsilon-0.1)/total;
    }
    
    /* Accesses the state of an agent given its position, direction and cargo */

	public int getQState() {
		int v = currentPosition.x*Field.nY+currentPosition.y; //coordinates
		v += (direction/90)*Field.nX*Field.nY; //direction
		if(ball!=null) {
			v += directions*Field.nX*Field.nY; //ball
		}
		return v;
    }
    
    /* Learns policy up to a certain step and then uses policy to behave */
	public void learningDecision(int originalState, Action originalAction) {
		it++;
		double u = reward(originalState,originalAction);
		double prevq = getQ(originalState,originalAction);
		double predError = 0;
		
		epsilon = Math.max(epsilon-dec,0.05);
		aheadPosition = aheadPosition(1); //percept
		
		switch(learningApproach) {
			case SARSA : 
				Action newAction = selectAction();
				predError = u + discount*getQ(getQState(), newAction) - prevq; break;
			case QLearning : predError = u + discount*getMaxQ(getQState()) - prevq; break;
		}
		setQ(originalState, originalAction, prevq+(learningRate * predError));
		//if(it%1000==0) System.out.println("e="+epsilon+"\n"+qToString());
	}
	
	/* Executes action according to the learned policy */
	public void executeQ() {
		if(random.nextDouble()<randfactor) execute(randomAction());
		else execute(getMaxActionQ(getQState(),availableActions()));
	}
	
	/* Selects action according to e-greedy or soft-max strategies */
	public Action selectAction() {
		epsilon -= dec;
		if(random.nextDouble()<randfactor) return randomAction(); 
		switch(actionSelection) {
			case eGreedy : return eGreedySelection();
			default : return softMax();
		}
	}

	/* Select a random action */
	private Action randomAction() {
		List<Integer> validActions = availableActions(); //index of available actions
		return actions.get(validActions.get(random.nextInt(validActions.size())));
	}

	/* SoftMax action selection */
	private Action softMax() {
		List<Integer> validActions = availableActions(); //index of available actions
		double[] cumulative = new double[validActions.size()];
		cumulative[0]=Math.exp(getQ(getQState(),actions.get(0))/(epsilon*100.0));
		for(int i=1; i<validActions.size(); i++) 
			cumulative[i]=Math.exp(getQ(getQState(),actions.get(i))/(epsilon*100.0))+cumulative[i-1];
		double total = cumulative[validActions.size()-1];
		double cut = random.nextDouble()*total;
		for(int i=0; i<validActions.size(); i++) 
			if(cut<=cumulative[i]) return actions.get(validActions.get(i));
		return null;
	}

	/* eGreedy action selection */
	private Action eGreedySelection() {
		List<Integer> validActions = availableActions(); //index of available actions
		if(random.nextDouble()>epsilon) 
			return actions.get(validActions.get(random.nextInt(validActions.size())));
		else return getMaxActionQ(getQState(),validActions);
	}

	/* Retrieves reward from action */
	private int reward(int state, Action action) {
		switch(action) {
			case grabBall : return 100;
			case throwBall : return 100;
			default : return 0;
		}
	}

	/* Gets the index of maximum Q-value action for a state */
	private Action getMaxActionQ(int state, List<Integer> actionsIndexes) {
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for(int i : actionsIndexes) {
			double v = q[state][i];
			if(v>max) {
				max = v;
				maxIndex = i;
			}
		}
		return actions.get(maxIndex);
	}
	
	/* Get action with higher likelihood for a given state from q */
	private Action getMaxActionQ(int state) {
		double max = Double.NEGATIVE_INFINITY;
		int maxIndex = -1;
		for(int i=0; i<actions.size(); i++) {
			if(q[state][i]>max) {
				max = q[state][i];
				maxIndex = i;
			}
		}
		return actions.get(maxIndex);
	}

	/* Gets the maximum Q-value action for a state (x y) */
	private double getMaxQ(int state) {
		double max = Double.NEGATIVE_INFINITY;
		for(double v : q[state]) max = Math.max(v, max);
		return max;
	}

	/* Gets the maximum Q-value action for a state (x y) */
	private boolean singleMaxQ(int state) {
		int count = 0;
		double max = getMaxQ(state);
		for(double v : q[state]) if(v==max) count++;
		return count<=1;
	}

	/* Gets the Q-value for a specific state-action pair (x y action) */
	private double getQ(int state, Action action) {
		return q[state][actions.indexOf(action)];
	}

	/* Sets the Q-value for a specific state-action pair (x y action) */
	private void setQ(int state, Action action, double val) {
		q[state][actions.indexOf(action)] = val;
	}

	/* Returns the index of eligible actions */
	private List<Integer> availableActions() {
		List<Integer> res = new ArrayList<Integer>();
		for(int i=0; i<actions.size(); i++) 
			if(eligible(actions.get(i))) res.add(i);
		return res;
	}
	
	/* Verifies if a specific action is eligible */
	private boolean eligible(Action action) {
		if(action==null) return false;
		switch(action) {
			case moveAhead : return isFreeCell(aheadPosition);
			case grabBall : return !Field.isWall(aheadPosition) && isBallAhead() && !hasBall();
			case throwBall : return !Field.isWall(aheadPosition) && hasBall();
			default : return true;
		}
	}

}