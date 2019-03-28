package pacman.entries.pacman;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.internal.Node;

public class Test extends Controller<MOVE>{
	
	static ArrayList<Junction> junctions = new ArrayList<Junction>();
	
	public MOVE getMove(Game game, long timeDue) {
		
		int nodes = game.getNumberOfNodes();
		int pill = game.getPillIndices().length;
		
		System.out.println(nodes +" "+ pill);
		return lastMove;
	}
	
}
