package pacman.entries.pacman;

import java.util.Date;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class NearestPowerPill extends Controller<MOVE>{
	
	public MOVE getMove(Game game, long timeDue) {
		
		int [] activePowerPills = game.getActivePowerPillsIndices();
		int[] targetNodeIndices = new int[activePowerPills.length];
		int[] junctions = game.getJunctionIndices();
		int currentNode = 0;
		
		while(activePowerPills.length > 0) {
			currentNode = game.getPacmanCurrentNodeIndex();
		
			activePowerPills = game.getActivePowerPillsIndices();
			targetNodeIndices = new int[activePowerPills.length];
			
			for(int i = 0; i < targetNodeIndices.length ; i++) {
				targetNodeIndices[i] = activePowerPills[i];
			}
		
			return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),
					game.getClosestNodeIndexFromNodeIndex(currentNode, targetNodeIndices,DM.PATH),DM.PATH);
		}
		
		currentNode = game.getPacmanCurrentNodeIndex();
		//get all active pills
		int[] activePills= game.getActivePillsIndices();
		
		return game.getNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(),
				game.getClosestNodeIndexFromNodeIndex(currentNode, activePills,DM.PATH),DM.PATH);
	}
	
}
