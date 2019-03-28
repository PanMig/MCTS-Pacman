package MCTS.pacman.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import pacman.controllers.Controller;
import pacman.controllers.examples.RandomGhosts;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.controllers.examples.StarterPacMan;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

//s@Author P.Migkotzidis

/*Summary : MCTS main class which implements the MCTS tree build.
 * The Mcts Search occurs when pacman visits a junction and return the move to that junction.
 * Then pacman follows that path until it reaches the choosen junction where the search starts again.
*/

public class MctsPacman extends Controller<MOVE> {

	//CONSTANTS
	public static final double C = 1.0f/Math.sqrt(2.0f);
	
	//PROPERTIES
	public static Controller<EnumMap<GHOST,MOVE>> ghosts = new StarterGhosts();
	public static final int ghost_dist = 9;
	public static final int hunt_dist = 25;
	public static final int TREE_LIMIT = 35;
	public static int tree_length = 0 ;
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		
		//Hunt edible ghosts if not far away
		for(GHOST ghost : GHOST.values()) {
			if(game.getGhostEdibleTime(ghost) > 0) {
				if(game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost)) < hunt_dist) {
					return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),game.getGhostCurrentNodeIndex(ghost), DM.PATH);
				}
			}
		}
		
		// run Mcts when in a junction to get next move (next move is based on next junction)
		if(pacmanInJunction(game)) {
			tree_length = 0;
			return MctsSearch(game);
		}
		
		// follow path until chosen junction is met.
		return FollowPath(game.getPacmanLastMoveMade() , game);
	}
	
	
	public MOVE FollowPath(MOVE dir,Game state) {
		MOVE[] possibleMoves = state.getPossibleMoves(state.getPacmanCurrentNodeIndex());
	    ArrayList<MOVE> moves = new ArrayList<MOVE>(Arrays.asList(possibleMoves));
	    
	    int current = state.getPacmanCurrentNodeIndex();
		//EVADE GHOSTS DURING PATH
		for(GHOST ghost : GHOST.values()) {
			if(state.getGhostEdibleTime(ghost)==0 && state.getGhostLairTime(ghost)==0) {
				if(state.getShortestPathDistance(current,state.getGhostCurrentNodeIndex(ghost)) < ghost_dist) {
					return state.getNextMoveAwayFromTarget(current,state.getGhostCurrentNodeIndex(ghost), DM.PATH);
				}
			}
		}
	    
        if(moves.contains(dir)) return dir;
        moves.remove(state.getPacmanLastMoveMade().opposite());
        assert moves.size() == 1; // along a path there is only one possible way remaining
		
        return moves.get(0);
	}
	
	private boolean pacmanInJunction(Game game) {
		if(game.isJunction(game.getPacmanCurrentNodeIndex())) return true;
		return false;
	}

	public MOVE MctsSearch(Game game) {
		
		//create root node with state0
		MctsNode root = new MctsNode(null,game,game.getPacmanCurrentNodeIndex());
		
		long start = new Date().getTime();
		
		while (new Date().getTime() < start + 30 && tree_length <= TREE_LIMIT) {
			MctsNode nd = TreePolicy(root);
			if(nd == null) return MOVE.DOWN;
			float reward = DefaultPolicy(nd);
			Backpropagation(nd,reward);
		}
		
		MctsNode bestChild = BestChild(root,0);
		
		if(bestChild == null) {
			return new RandomPacMan().getMove(game,-1);
		}
		
		return bestChild.actionMove;
	}
	
	
	public MctsNode TreePolicy(MctsNode nd) {
		if(nd == null) {
			return nd;
		}
		while(!nd.isTerminalGameState()) {
			if(!nd.isFullyExpanded()) {
				return nd.Expand();
			}
			else {
				return nd = TreePolicy(BestChild(nd,C));
			}
		}
		return nd;
	}
	
	
	public float DefaultPolicy(MctsNode nd) {
		
		float reward = 0;
		int steps = 0;
		Controller<MOVE> pacManController = new RandomPacMan();
		Controller<EnumMap<GHOST,MOVE>> ghostController = ghosts;
		if(nd == null) return 0;
		Game state = nd.game.copy();
		int pillsBefore = state.getNumberOfActivePills();
		int livesBefore = state.getPacmanNumberOfLivesRemaining();
		
		// if died during reaching the junction
		if(nd.deltaReward == 0.0f) {
			return 0;
		}
		
        while(!state.gameOver()) {
        	
        	//advance game
        	state.advanceGame(pacManController.getMove(state,System.currentTimeMillis()),
			ghostController.getMove(state,System.currentTimeMillis()));
        	
        	steps++;
        	
    		if(steps >= 15){
        		break;
    		}
        }
        
        // DEATH CONDITION
        int livesAfter = state.getPacmanNumberOfLivesRemaining();
		if (livesAfter < livesBefore) {
			return 0.0f;
		}
		
		// Maze level completed
		if(state.getNumberOfActivePills() == 0) {
			return 1.0f;
		}
		
		//reward based on pills eaten
        return  1.0f -  ((float) state.getNumberOfActivePills() / ((float) pillsBefore));
	}

	public MctsNode BestChild(MctsNode nd, double C) {
		MctsNode bestChild = null;
		
		double bestValue = -1.0f;
		for(int i = 0 ; i < nd.children.size(); i++) {
			if(UCTvalue(nd.children.get(i), C) >= bestValue) {
				bestValue = UCTvalue(nd.children.get(i),C);
				bestChild = (nd.children.get(i));
			}
		}
		return bestChild;
	}

	private double UCTvalue(MctsNode nd, double C) {
		double value = (float) ((nd.deltaReward / nd.timesVisited) + C* Math.sqrt(2*Math.log(nd.parent.timesVisited)/ nd.timesVisited));
		return value;
	}

	private void Backpropagation(MctsNode currentNode, double reward) {
		while(currentNode != null) {
			currentNode.timesVisited ++;
			currentNode.deltaReward += reward;
			currentNode = currentNode.parent;
		}
	}
	
}
