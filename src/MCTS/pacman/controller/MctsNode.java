package MCTS.pacman.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.controllers.Controller;
import pacman.controllers.examples.RandomPacMan;
import pacman.controllers.examples.StarterGhosts;
import pacman.game.Game;

// s@Author P.Migkotzidis

/*Summary : MCTSNode object represent each node in the MCTS tree.
Visually every node corresponds to a specific junction in the maze.
The number of children of a MCTSNode in the tree is the number of closest available junctions.
*/

public class MctsNode {

	public int junction = -1;
	public int timesVisited = 0;
	public MctsNode parent;
	public ArrayList<MctsNode> children = new ArrayList<MctsNode>();
	public MOVE actionMove;
	public double deltaReward;
	public ArrayList<MOVE> triedMoves = new ArrayList<MOVE>();
	public Game game;

	public MctsNode(MctsNode parent, Game game, int junction) {
		this.parent = parent;
		this.actionMove = MOVE.UP;
		this.deltaReward = -1.0f;
		this.game = game;
		this.junction = junction;
		this.triedMoves.clear();
		this.children.clear();
	}

	public MctsNode Expand() {
		
		MOVE nextMove = untriedMove(game);
		
	 	if((nextMove != game.getPacmanLastMoveMade().opposite())) {
	 		
	 		MctsNode expandedChild = GetClosestJunctionInDir(nextMove);
	 		expandedChild.actionMove = nextMove;
			//System.out.println("expanded child: " + expandedChild + "parent :" + expandedChild.parent + " Junction :" + expandedChild.junction + " Move: " + expandedChild.actionMove);
			MctsPacman.tree_length ++;
			this.children.add(expandedChild);
			expandedChild.parent = this;
			return expandedChild;
		}
	 	
		return this;
	}

	
	public MctsNode GetClosestJunctionInDir(MOVE dir) {
		
		Game state = game.copy();
		Controller<EnumMap<GHOST, MOVE>> ghostController = MctsPacman.ghosts;
		
		int from = state.getPacmanCurrentNodeIndex();
		int current = from;
		MOVE currentPacmanDir = dir;
		
		//Simulation reward variables
		int pillsBefore = state.getNumberOfActivePills();
		int ppillsBefore = state.getNumberOfActivePowerPills();
		int livesBefore = state.getPacmanNumberOfLivesRemaining();
		float transition_reward = 0.0f;
		
		// use current == from , so we skip the junction we are currently in
		while(!state.isJunction(current) || current == from){
			
			//make pacman follow the path
			currentPacmanDir = GetMoveToFollowPath(state,currentPacmanDir);
			
			//advance game state
			state.advanceGame(currentPacmanDir,
		    		ghostController.getMove(state,
		    		System.currentTimeMillis()));
			
			current = state.getPacmanCurrentNodeIndex();
		}
		
		int livesAfter = state.getPacmanNumberOfLivesRemaining();
		int pillsAfter = state.getNumberOfActivePills();
		int ppillsAfter = state.getNumberOfActivePowerPills();
		
		//dead during transition
		if (livesAfter < livesBefore) {
			transition_reward = 0.0f;
		}
		else if(ppillsAfter < ppillsBefore && AvgDistanceFromGhosts(state) > 100) {
			transition_reward = 0.0f;
		}
		//alive but no pills eaten
		else if(pillsAfter == pillsBefore) {
            transition_reward = 0.2f;
        }
		//pills eaten and alive
		else {
			transition_reward = 1.0f;
		}
		
		//return the child node with updated state and junction number
		MctsNode child = new MctsNode(this,state,current);
		child.deltaReward = transition_reward;
		return child;
	}
	
    // Make pacman follow a path where only one move is possible (excluding reverse)
	public MOVE GetMoveToFollowPath(Game state,MOVE direction) {
		MOVE[] possibleMoves = state.getPossibleMoves(state.getPacmanCurrentNodeIndex());
	    ArrayList<MOVE> moves = new ArrayList<MOVE>(Arrays.asList(possibleMoves));

        if(moves.contains(direction)) return direction;
        moves.remove(state.getPacmanLastMoveMade().opposite());
        assert moves.size() == 1; // along a path there is only one possible way remaining
        return moves.get(0);
	}

	public boolean isTerminalGameState() {
		if (game.wasPacManEaten() || game.getActivePillsIndices().length == 0) {
			return true;
		}
		return false;
	}

	//Pick randomly non-tried action
	public MOVE untriedMove(Game game) {
		ArrayList<MOVE> untriedMoves = new ArrayList<MOVE>();
		MOVE untriedMove = null;
		int current_node = game.getPacmanCurrentNodeIndex();
		List<MOVE> possibleMoves = Arrays.asList(game.getPossibleMoves(current_node));
	
		if (possibleMoves.contains(MOVE.UP) && !triedMoves.contains(MOVE.UP)) {
			untriedMoves.add(MOVE.UP);
		}
		if (possibleMoves.contains(MOVE.RIGHT) && !triedMoves.contains(MOVE.RIGHT)) {
			untriedMoves.add(MOVE.RIGHT);
		}
		if (possibleMoves.contains(MOVE.DOWN) && !triedMoves.contains(MOVE.DOWN)) {
			untriedMoves.add(MOVE.DOWN);
		} 
		if (possibleMoves.contains(MOVE.LEFT) && !triedMoves.contains(MOVE.LEFT)) {
			untriedMoves.add(MOVE.LEFT);
		}
		
		untriedMove = untriedMoves.get(new Random().nextInt(untriedMoves.size()));
		triedMoves.add(untriedMove);
		return untriedMove;
	}
	
	public boolean isFullyExpanded() {
		if ( children.size() <= 0) {
			return false;
		}
		
		int current_node = game.getPacmanCurrentNodeIndex();
		MOVE[] possibleMoves = game.getPossibleMoves(current_node);
		
		if(possibleMoves.length == triedMoves.size()) return true;
		
		if (possibleMoves.length != children.size()) {
			return false;
		} 
		else {
			return true;
		}
	}
	
	public static int AvgDistanceFromGhosts(Game state) {
		int sum = 0;
		for(GHOST ghost : GHOST.values())
			sum += state.getDistance(state.getPacmanCurrentNodeIndex(), state.getGhostCurrentNodeIndex(ghost), DM.PATH);
		return sum/4;
	}
	
}
			


