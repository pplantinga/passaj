package passaj;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * QuoridorModel encapsulates all the logic relating to a game of Quoridor.
 */
public class QuoridorModel
{
	private static final int WALL_VALUE = 5;
  private int[][] myBoard;
  private int[] myWallCounts;
	private int[] myXs;
	private int[] myYs;
	private int[] pathLengths;
	private int[] myOpenings;
  private int myTurn;
  private int myBoardSize;
  private int myPlayers;
  private int myWallNumber;
  private IllegalArgumentException wallException;
  private IllegalArgumentException wallFilled;
  private IllegalArgumentException blockException;
	private List<int[]> moves;
	private boolean[][] wallsInPath;

  public QuoridorModel()
  {
    this.myBoardSize = 9;
    this.myPlayers = 2;
    this.myWallNumber = numberOfWalls(9, 2);
    this.myWallCounts = new int[] { this.myWallNumber, this.myWallNumber };

    initialize();
  }

	/**
	 * Dupicates an instance of a quoridor model.
	 */
  public QuoridorModel(QuoridorModel m) {
    this.myBoardSize = m.myBoardSize;
    this.myPlayers = m.myPlayers;
		this.myWallNumber = m.myWallNumber;

    this.myWallCounts = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCounts[i] = m.myWallCounts[i];
    }

    initialize();
    this.myBoard = deepCopy(m.myBoard);
    this.myTurn = m.myTurn;
  }

	/**
	 * Initialize a model with parameters.
	 *
	 * Params
	 * 	 playerCount = number of players in the game: 2 or 4
	 * 	 size = size of the board (default 9)
	 */
  public QuoridorModel(int playerCount, int size)
  {
    this.myBoardSize = size;
		this.myPlayers = playerCount;
   	this.myWallNumber = numberOfWalls(this.myBoardSize, this.myPlayers);

    this.myWallCounts = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCounts[i] = this.myWallNumber;
    }

    initialize();
  }

	/* This yeilds 10 for board size 9, as it should, as well as
	 * reasonable numbers for the other sizes
	 */
	private int numberOfWalls(int boardSize, int players)
	{
		return (int)Math.round(Math.pow(boardSize - 1, 2.0) * 5.0/16.0/players);
	}

	/**
	 * Set all internal variables to their initial values.
	 */
  public void initialize()
  {
    this.myTurn = 0;

		// Exceptions
    this.wallException = new IllegalArgumentException(
			"There is a wall between where you are and where you want to be.");
    this.wallFilled = new IllegalArgumentException(
			"There is a wall already there!");
    this.blockException = new IllegalArgumentException(
			"You may not block anybody from getting to their goal row");

		// Initialize the board
		int half = this.myBoardSize - 1;
    this.myBoard = new int[2 * half + 1][2 * half + 1];

		// Put pieces in appropriate starting locations
		this.myXs = new int[this.myPlayers];
		this.myYs = new int[this.myPlayers];

		this.myXs[0] = half;
		this.myYs[0] = 0;
		this.myXs[1] = half;
		this.myYs[1] = 2 * half;

		// Add third and fourth players
		if (this.myPlayers == 4)
		{
			this.myXs[2] = 2 * half;
			this.myYs[2] = half;
			this.myXs[3] = 0;
			this.myYs[3] = half;
		}

		// If we have an even number of squares, we have to shift one out of
		// each pair of pieces to make the board rotation-symmetric.
    if (isEven(this.myBoardSize))
		{
			this.myXs[0] += 2;

      if (this.myPlayers == 4)
				this.myYs[2] += 2;
    }
		
		// Put the pieces on the board
		for (int i = 0; i < this.myPlayers; i++)
			this.myBoard[this.myXs[i]][this.myYs[i]] = i + 1;

		// Start list of moves
		this.moves = new ArrayList<int[]>();

		// Start keeping track of walls in my path.
		this.wallsInPath = new boolean[(this.myBoardSize - 1) ^ 2 * 2][2];
  }

	/**
	 * Public move function.
	 */
	public void move(int x, int y, int o)
	{
		if (o == 0)
			movePiece(x, y);
		else
			placeWall(x, y, o);
	}

	/**
	 * ai_move uses minimax to decide on a move and take it.
	 *
	 * Params:
	 *   millis = the length of time to search moves in milliseconds
	 */
	void ai_move(long millis)
	{
		int i = 2;
		int[] move = new int[] {0, 0, 0};
		int[] testMove;
		long t0 = System.currentTimeMillis();
		QuoridorModel testModel = new QuoridorModel(this);
		
		// iterative deepening
		while (i < 100)
		{
			testMove = testModel.negascout(i, -1000, 1000, millis, t0, move);
			i += 1;
			if (System.currentTimeMillis() - t0 > millis && i < 100)
				move = testMove;
			else
				break;
		}

		// Print the level that we got to
		System.out.println(i);
		
		if (move[2] != 0)
			if (!placeWall(move[0], move[1], move[2]))
				throw wallException;
		else
			if (!movePiece(move[0], move[1]))
				throw wallException;
	}

	/**
	 * Undo last moves.
	 *
	 * Params:
	 *   n = the number of moves to undo
	 */
	void undo(int n)
	{
		for (int i = 0; i < n; i++)
		{
			int[] move = moves.remove(moves.size() - 1);
			int x = move[0];
			int y = move[1];
			int o = move[2];

			// update turn
			this.myTurn -= 1;
			int turn = this.myTurn % this.myPlayers;

			// undo wall
			if (o != 0)
			{
				int xAdd = o - 1;
				int yAdd = o % 2;

				this.myBoard[x][y] = 0;
				this.myBoard[x + xAdd][y + yAdd] = 0;
				this.myBoard[x - xAdd][y - yAdd] = 0;

				this.myWallCounts[turn] += 1;
			}

			// undo move
			else
			{
				this.myBoard[x][y] = turn + 1;
				this.myBoard[this.myXs[turn]][this.myYs[turn]] = 0;
				this.myXs[turn] = x;
				this.myYs[turn] = y;
			}

			this.pathLengths = new int[] {this.pathLength(0), pathLength(1)};
		}
	}

	/**
	 * Checks for move legality, and if legal, moves the player
	 *
	 * Params:
	 *   x = the desired horizontal location
	 *   y = the desired vertical location
	 *
	 * Returns: whether or not the move occurred 
	 */
	boolean movePiece(int x, int y)
	{
		int turn = this.myTurn % this.myPlayers;
		int oldX = this.myXs[turn];
		int oldY = this.myYs[turn];

		if (isLegalMove(x, y, oldX, oldY))
		{
			// make the move
			this.myXs[turn] = x;
			this.myYs[turn] = y;
			this.myBoard[oldX][oldY] = 0;
			this.myBoard[x][y] = turn + 1;

			// update shortest path length
			this.pathLengths[turn] = pathLength(turn);

			// update turn
			this.myTurn++;

			// add old location to undo list
			this.moves.add(new int[] {oldX, oldY, 0});

			return true;
		}

		return false;
	}

	/**
	 * Check if this piece movement is legal
	 *
	 * Params:
	 *   x, y = potential new location
	 *   oldX, oldY = current location
	 *
	 * Returns:
	 *   Whether or not the move is legal
	 */
	boolean isLegalMove(int x, int y, int oldX, int oldY)
	{

		// Check for out-of-bounds
		if (!isOnBoard(x) || !isOnBoard(y))
		{
			return false;
		}

		// Check if another player is where we're going
		if (this.myBoard[x][y] != 0)
		{
			return false;
		}

		// jump dist
		int Xdist = Math.abs(x - oldX);
		int Ydist = Math.abs(y - oldY);
		int avgX = (x + oldX) / 2;
		int avgY = (y + oldY) / 2;
		int in_between = this.myBoard[avgX][avgY];
		int onePastX = x + avgX - oldX;
		int onePastY = y + avgY - oldY;

		// normal move: one space away and no wall between
		if (
			// one space away
			(Xdist == 2 && Ydist == 0
			 || Ydist == 2 && Xdist == 0)

			// no wall in-between
			&& in_between != WALL_VALUE)
		{
			return true;
		}

		// jump in a straight line
		else if (
				(
					// target is two away in the row
					Xdist == 4 && Ydist == 0

					// no wall between players or between opponent and target
					&& this.myBoard[avgX + 1][oldY] != WALL_VALUE
					&& this.myBoard[avgX - 1][oldY] != WALL_VALUE

					|| 
					// two away in the column
					Ydist == 4 && Xdist == 0

					// no wall between players or between opponent and target
					&& this.myBoard[oldX][avgY + 1] != WALL_VALUE
					&& this.myBoard[oldX][avgY - 1] != WALL_VALUE
				)
				// opponent between target and active player
				&& in_between != 0
			)
		{
			return true;
		}

		/*
		 * jump diagonally if blocked by enemy player and a wall
		 * or another enemy player and the edge of the board
		 */
		else if (
				Xdist == 2 && Ydist == 2
				&& (
					// opponent above or below
					this.myBoard[x][oldY] != 0

					// wall or the edge is on the far side of opponent
				 	&& (!isOnBoard(onePastX)
				 	|| this.myBoard[onePastX][oldY] == WALL_VALUE)
				
					// no wall between you and opponent
				 	&& this.myBoard[avgX][oldY] != WALL_VALUE

					// no wall between opponent and target
				 	&& this.myBoard[x][avgY] != WALL_VALUE

				 	|| 
					// opponent to one side or the other
					this.myBoard[oldX][y] != 0

					// wall or edge of board beyond opponent
				 	&& (!isOnBoard(onePastY)
				 	|| this.myBoard[oldX][onePastY] == WALL_VALUE)
				
					// no wall between players
				 	&& this.myBoard[oldX][avgY] != WALL_VALUE

					// no wall between opponent and target
				 	&& this.myBoard[avgX][y] != WALL_VALUE
				)
			)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Checks for wall legality, and if legal, places the wall
	 *
	 * Params:
	 *   x = the horizontal location
	 *   y = the vertical location
	 *   o = the orientation (1 for vertical, 2 for horizontal)
	 */
	boolean placeWall(int x, int y, int o)
	{
		if (!isLegalWall(x, y, o))
			return false;

		// Add the wall for checking both player's paths
		wallVal(x, y, o, WALL_VALUE);

		int[] testLengths = new int[this.myPlayers];

		// check if this wall blocks any path
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (wallsInPath[0][linearize(x, y, o)])
			{
				testLengths[i] = pathLength(0);

				if (testLengths[i] == 0)
				{
					// remove wall
					wallVal(x, y, o, 0);
					return false;
				}
			}
		}

		// All players have a path, so update shortest paths
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (testLengths[i] != 0)
				this.pathLengths[i] = testLengths[i];
		}
		
		// Reduce the walls remaining
		this.myWallCounts[this.myTurn % this.myPlayers]--;

		// update turn
		this.myTurn++;

		// add wall to the list of moves (for undo)
		this.moves.add(new int[] {x, y, o});

		return true;
	}

	/**
	 * This function helps keep track of walls that would interrupt
	 * the shortest path, so we can recalculate when necessary
	 */
	void addWalls(int player, int x, int y, int oldX, int oldY)
	{
		int avgX = (x + oldX) / 2;
		int avgY = (y + oldY) / 2;

		// horizontal move
		if (Math.abs(x - oldX) == 2)
		{
			if (isOnBoard(y - 1))
				wallsInPath[player][linearize(avgX, y - 1, 1)] = true;

			if (isOnBoard(y + 1))
				wallsInPath[player][linearize(avgX, y + 1, 1)] = true;
		}

		// vertical move
		else
		{
			if (isOnBoard(x - 1))
				wallsInPath[player][linearize(x - 1, avgY, 2)] = true;

			if (isOnBoard(x + 1))
				wallsInPath[player][linearize(x + 1, avgY, 2)] = true;
		}
	}
	
	/**
	 * Calculate linear location in array from x and y
	 */
	int linearize(int x, int y, int o)
	{
		return x - 1 + (this.myBoardSize - 1) * (y - 1) + o - 1;
	}

	/**
	 * Asserts a wall is legal
	 *
	 * Params:
	 *   x = horizontal location of new wall
	 *   y = vertical location of new wall
	 *   o = orientation of new wall (vertical, 1, or horizontal, 2)
	 */
	boolean isLegalWall(int x, int y, int o)
	{
		// Make sure wall isn't in move land
		if (isEven(x) || isEven(y))
			return false;

		// check for out-of-bounds
		if (!isOnBoard(x) || !isOnBoard(y))
			return false;

		// Make sure orientation is valid
		if (o != 1 && o != 2)
			return false;

		// Make sure the player has walls left
		if (this.myWallCounts[this.myTurn % this.myPlayers] == 0)
			return false;

		int xAdd = o - 1;
		int yAdd = o % 2;

		if (this.myBoard[x][y] != 0
			|| this.myBoard[x + xAdd][y + yAdd] != 0
			|| this.myBoard[x - xAdd][y - yAdd] != 0)
			return false;

		return true;
	}

	/**
	 * The wall defined by x, y, and o will be set to 'val'.
	 */
	void wallVal(int x, int y, int o, int val)
	{
		int xAdd = o - 1;
		int yAdd = o % 2;

		this.myBoard[x][y] = val;
		this.myBoard[x + xAdd][y + yAdd] = val;
		this.myBoard[x - xAdd][y - yAdd] = val;
	}

	/**
	 * Asserts a move is within the limits of the board
	 */
	boolean isOnBoard(int d)
	{
		return 0 <= d && d < this.myBoardSize * 2 + 1;
	}

	/**
	 * Evaluate function for Negascout.
	 *
	 * Boards look better if your path is shorter than your opponent,
	 * and if you have more walls than your opponent.
	 *
	 * Negative numbers are good for player 1, positive are good for 2.
	 */
	int evaluate()
	{
		int won = 0;
		if (this.myYs[0] == 0)
			won = -100;
		if (this.myYs[1] == this.myBoardSize - 1)
			won = 100;
		return (
			won
			 - this.myWallCounts[0]
			 + this.myWallCounts[1]
			 + 2 * this.pathLengths[0]
			 - 2 * this.pathLengths[1]
		);
	}

	/**
	 * Negascout algorithm, a variation of the minimax algorithm, which
	 * recursively examines possible moves for both players and evaluates
	 * them, looking for the best one.
	 *
	 * Params:
	 *   qb = The board to search for a move on
	 *   depth = how many moves deep to search
	 *   a, b = alpha and beta for pruning unecessary sub-trees
	 *   seconds, t0 = time limit and time of beginning the search
	 *   best = best move so far for scouting
	 *
	 * Returns:
	 *   best move that could be found in form [x, y, o, score]
	 */
	int[] negascout(int depth, int alpha, int beta, long millis, long t0, int[] best)
	{
		if (depth <= 0 || this.isGameOver()
				|| System.currentTimeMillis() - t0 > millis)
		{
			int score = this.evaluate();
			if (this.myTurn % 2 == 0)
				score = -score;
			return new int[] {0, 0, 0, score};
		}

		// initialize values
		int[] opponentMove = new int[] {0, 0, 0};
		int scoutVal = beta;
		int bestX = 0;
		int bestY = 0;
		int bestO = 0;
		int score = 0;
		int oldX = this.myXs[this.myTurn % 2];
		int oldY = this.myYs[this.myTurn % 2];
		int oldPathLength = this.pathLengths[this.myTurn % 2];
		boolean first = true;
		QuoridorModel testBoard = new QuoridorModel(this);

		// We'll only do this for the root node, where we have a best move recorded
		if (best.length > 1)
		{
			if (best[2] == 0)
				testBoard.movePiece(best[0], best[1]);
			else
				testBoard.placeWall(best[0], best[1], best[2]);

			opponentMove = testBoard.negascout(
				depth - 1,
				-scoutVal,
				-alpha,
				millis,
				t0,
				null
			);

			alpha = -opponentMove[3];
			bestX = best[0];
			bestY = best[1];
			bestO = best[2];
			first = false;
		}

		// move piece
		for (int[] i : new int[][] {
				{oldX - 2, oldY},
				{oldX, oldY - 2},
				{oldX + 2, oldY},
				{oldX, oldY + 2}
			})
		{
			// legal and we haven't checked it already
			if (this.isLegalMove(i[0], i[1], oldX, oldY)
				 && (best.length < 2
					 || best[2] != 0
					 || best[0] != i[0]
					 || best[1] != i[1]
					)
				)
			{
				testBoard = new QuoridorModel(this);
				testBoard.movePiece(i[0], i[1]);
				
				/* Don't consider moves that don't shorten our path.
				 * This is usually bad, and sometimes the computer will make a
				 * dumb move to avoid getting blocked by a wall.
				 */
				if (testBoard.pathLengths[this.myTurn % 2] >= oldPathLength)
					continue;

				opponentMove = testBoard.negascout(
					depth - 1,
					-scoutVal,
					-alpha,
					millis,
					t0,
					null
				);

				if (alpha < -opponentMove[3] && -opponentMove[3] < beta && !first)
				{
					opponentMove = testBoard.negascout(
						depth - 1,
						-beta,
						-alpha,
						millis,
						t0,
						null
					);
				}

				if (-opponentMove[3] > alpha)
				{
					alpha = -opponentMove[3];
					bestX = i[0];
					bestY = i[1];
					bestO = 0;
				}

				if (alpha >= beta || System.currentTimeMillis() - t0 > millis)
					return new int[] {bestX, bestY, bestO, alpha};

				scoutVal = alpha + 1;

				if (first)
					first = false;
			}

			// Check jumps
			else if (isOnBoard(i[0]) && isOnBoard(i[1])
				 && this.myBoard[i[0]][i[1]] != 0)
			{
				for (int[] j : new int[][] {
						{i[0] - 2, i[1]}, 
						{i[0], i[1] - 2}, 
						{i[0] + 2, i[1]}, 
						{i[0], i[1] + 2}
					})
				{
					if (this.isLegalMove(j[0], j[1], oldX, oldY))
					{
						testBoard = new QuoridorModel(this);
						testBoard.movePiece(j[0], j[1]);
						
						/* Don't consider jumps that make our length longer.
						 * There can be situations where the only available move is
						 * a jump that doesn't make our path shorter, so examine those.
						 */
						if (testBoard.pathLengths[this.myTurn % 2] > oldPathLength)
							continue;

						opponentMove = testBoard.negascout(
							depth - 1,
							-scoutVal,
							-alpha,
							millis,
							t0,
							null
						);

						if (alpha < -opponentMove[3]
							 && -opponentMove[3] < beta
							 && !first)
						{
							opponentMove = testBoard.negascout(
								depth - 1,
								-beta,
								-alpha,
								millis,
								t0,
								null
							);
						}

						if (-opponentMove[3] > alpha)
						{
							alpha = -opponentMove[3];
							bestX = j[0];
							bestY = j[1];
							bestO = 0;
						}

						if (alpha >= beta || System.currentTimeMillis() - t0 > millis)
							return new int[] {bestX, bestY, bestO, alpha};

						scoutVal = alpha + 1;

						if (first)
							first = false;
					}
				}
			}
		}

		// walls
		for (int x = 1; x < this.myBoardSize; x += 2)
		{
			for (int y = 1; y < this.myBoardSize; y += 2)
			{
				for (int o = 1; o < 3; o++)
				{
					// limit to walls in the opponents path,
					// or walls in their own path, but opposite orientation to block
					if (
							// Walls in my opponent's path
							this.wallsInPath[(this.myTurn + 1) % 2][linearize(x, y, o)]

							// walls that block the wall the opponent would place if I move
							 || opponentMove != null
							 && (
								// opponent plays vertical wall, blocking walls have same x
									opponentMove[2] == 1 && opponentMove[0] == x

									// check same place, opposite orientation
									 && (opponentMove[1] == y && o == 2
									
										// check blocking either end
										 || Math.abs(opponentMove[1] - y) == 2 && o == 1)
									
									// opponent plays horizontal wall, blocking walls have same y
									 || (opponentMove[2] == 2 && opponentMove[1] == y

									// same place opposite orientation
									 && (opponentMove[0] == x && o == 1
									
										// blocking either end
										 || Math.abs(opponentMove[0] - x) == 2 && o == 2))
									)
							
							// check walls around me, in case I can block off my path
							// (least essential, but I think I'll keep it)
							|| Math.abs(x - oldX) == 1 && Math.abs(y - oldY) == 1

							// check walls around my opponent
							|| Math.abs(x - myXs[(this.myTurn + 1) % 2]) == 1
							 	&& Math.abs(y - myYs[(this.myTurn + 1) % 2]) == 1

							// check all walls in the first case
							// for obvious moves that we might otherwise miss
							|| best.length == 1
						)
					{
						// some testing done twice, but faster to test than allocate
						if (this.isLegalWall(x, y, o))
						{
							testBoard = new QuoridorModel(this);
							if (testBoard.placeWall(x, y, o))
							{
								score = -testBoard.negascout(
									depth - 1,
									-scoutVal,
									-alpha,
									millis,
									t0,
									null
								)[3];

								if (alpha < score && score < beta && !first)
								{
									score = -testBoard.negascout(
										depth - 1,
										-beta,
										-alpha,
										millis,
										t0,
										null
									)[3];
								}

								if (score > alpha)
								{
									alpha = score;
									bestX = x;
									bestY = y;
									bestO = o;
								}

								if (alpha >= beta
										|| System.currentTimeMillis() - t0 > millis)
									return new int[] {bestX, bestY, bestO, alpha};

								scoutVal = alpha + 1;
							}
						}
					}
				}
			}
		}

		return new int[] {bestX, bestY, bestO, alpha};
	}

	/**
	 * Finds the length of the shortest path for a player
	 * Also keeps track of walls that would block the path
	 *
	 * Returns: length of the shortest path, ignoring the other player
	 *   0 for no path
	 */
	int pathLength(int player)
	{
		// Remove other players from the board so that they don't
		// interfere with finding the shortest path.
		removePlayers(player);

		// get current location
		int x = this.myXs[player];
		int y = this.myYs[player];

		// distance from current location
		int g = 0;
		
		// heuristic distance (distance from goal)
		int h = heuristic(player, x, y);

		// To keep track of where we go
		int[][] paths = new int[this.myBoardSize][this.myBoardSize];
		
		// Starting location
		paths[x][y] = 1;

		// This is a sort of priority queue, specific to this application
		// We'll only be adding elements of the same or slightly lower priority
		Hashtable<Integer, ArrayList<int[]>> nodes = new Hashtable<Integer, ArrayList<int[]>>();
		
		// add first node, current location
		nodes.put(h, new ArrayList<int[]>());
		nodes.get(h).add(new int[] {x, y, g});

		// current stores the node we're using on each iteration
		int[] current;
		int length;
		int key = h;

		// while there are nodes left to evaluate
		while (!nodes.isEmpty())
		{
			current = nodes.get(h).remove(0);
			x = current[0];
			y = current[1];
			g = current[2];
			h = heuristic(player, x, y);

			// if we've reached the end
			if (h == 0)
				break;

			// Try all moves
			int[][] moves = new int[][] {
				{x - 2, y},
				{x, y - 2},
				{x + 2, y},
				{x, y + 2}
			};

			for (int[] i : moves)
			{
				if (isLegalMove(i[0], i[1], x, y) && paths[i[0]][i[1]] == 0)
				{
					h = heuristic(player, i[0], i[1]);
					paths[i[0]][i[1]] = 100 * x + y + 2;
					
					if (!nodes.containsKey(g + h + 2))
						nodes.put(g + h + 2, new ArrayList<int[]>());

					nodes.get(g + h + 2).add(new int[] {i[0], i[1], g + 2});
				}
			}

			// if this is the last of this weight
			// check for empty queue and change the key 
			if (nodes.get(key).isEmpty())
			{
				nodes.remove(key);
				
				if (nodes.isEmpty())
				{
					addPlayers(player);
					return 0;
				}

				while (!nodes.containsKey(key))
					key += 2;
			}
		}

		// If we ran out of nodes, we didn't reach the end
		if (nodes.isEmpty())
		{
			addPlayers(player);
			return 0;
		}

		// re-initialize this player's wallsInPath to false
		for (int i = 0; i < this.wallsInPath[player].length; i++)
			this.wallsInPath[player][i] = false;

		int oldX;
		int oldY;
		
		while (paths[x][y] != 1)
		{
			oldX = x;
			oldY = y;
			x = paths[x][y] / 100;
			y = paths[oldX][y] % 100 - 2;
			addWalls(player, x, y, oldX, oldY);
		}

		addPlayers(player);
		return g / 2;
	}

	/**
	 * Remove all players except the excluded player from the board
	 * for purposes of finding a shortest path.
	 */
	void removePlayers(int excludedPlayer)
	{
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (i == excludedPlayer)
				continue;

			this.myBoard[this.myXs[i]][this.myYs[i]] = 0;
		}
	}

	/**
	 * Add all players except the excluded player to the board
	 * at the myXs and myYs locations.
	 */
	void addPlayers(int excludedPlayer)
	{
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (i == excludedPlayer)
				continue;

			this.myBoard[this.myXs[i]][this.myYs[i]] = i + 1;
		}
	}


	/**
	 * This is a heuristic for distance to goal,
	 * just straight line distance to goal.
	 */
	int heuristic(int player, int x, int y)
	{
		switch (player)
		{
			case 0:
				return y;
			case 1:
				return this.myBoardSize * 2 - 1 - y;
			case 2:
				return this.myBoardSize * 2 - 1 - x;
			case 3:
				return x;
		}
		return 100;
	}

	/**
	 * Check if the game is over.
	 */
	boolean isGameOver()
	{
		for (int i = 0; i < this.myPlayers; i++)
			if (heuristic(i, this.myXs[i], this.myYs[i]) == 0)
				return true;
		return false;
	}

	/**
	 * Copy the board into a new array.
	 */
  public int[][] deepCopy(int[][] board)
  {
    int[][] newBoard = new int[board.length][board.length];
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        newBoard[i][j] = board[i][j];
      }
    }
    return newBoard;
  }

  public boolean isEven(int number)
  {
    return number % 2 == 0;
  }

  public int getPlayer()
  {
    if (this.myTurn % this.myPlayers == 0) {
      return this.myPlayers;
    }
    return this.myTurn % this.myPlayers;
  }

  public int getValue(int x, int y)
  {
    return this.myBoard[x][y];
  }

  public int getWallCount(int player)
  {
    return this.myWallCounts[player - 1];
  }

	public int getColumn(int player)
	{
		return this.myXs[player - 1] + 1;
	}

	public int getRow(int player)
	{
		return this.myYs[player - 1] + 1;
	}
}
