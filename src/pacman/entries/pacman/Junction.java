package pacman.entries.pacman;

import pacman.game.Game;
import pacman.controllers.Controller;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;

public class Junction {

	public boolean visited = false;
	public int index = -1;
	public Game game;
	
	
	public boolean isVisited() {
		return visited;
	}


	public void setVisited(boolean visited) {
		this.visited = visited;
	}


	public Junction(boolean visited, Game game,int index) {
		this.visited = visited;
		this.game = game;
		this.index = index;
	}
	
}
