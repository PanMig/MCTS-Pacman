# MCTS-Pacman
MCTS Pacman agent applied to the Pacman VS Ghosts conpetition framework.

# MCTS implementation
- This is an eclipse project that uses the already provided Ms Pacman framework. For information about the project 
  visit http://www.pacmanvghosts.co.uk/
  
- The implementation of the MCTS agent can be found in MCTS-Pacman/src/MCTS/pacman/controller directory.

- The MCTS algorithm used is the default MCTS algorithm with a random rollout policy.

- Pacman's next action is calculated from the algorithm when pacman is on a junction, to improve perfomance and search depth.
  This junction calculation approach is influenced by this paper (https://ieeexplore.ieee.org/abstract/document/6731713)
