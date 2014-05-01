package passaj;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * QuoridorModel encapsulates all the logic relating to a game of Quoridor.
 */
public class QuoridorModel
{
	private static final int WALL_VALUE = 7;
	private Point[] myLocations;
  private int[][] myBoard;
  private int[] myWallCounts;
	private int[] pathLengths;
	private int[] myOpenings;
  private int myTurn;
  private int myBoardSize;
  private int myPlayers;
  private int myWallNumber;
	private String myBoardType;
  private IllegalArgumentException wallException;
  private IllegalArgumentException wallFilled;
  private IllegalArgumentException blockException;
	private List<int[]> moves;
	private boolean[][] wallsInPath;

  public QuoridorModel()
  {
    this.myBoardSize = 9;
    this.myPlayers = 2;
		this.myBoardType = "default";
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
		this.myBoardType = m.myBoardType;
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
  public QuoridorModel(int playerCount, int size, String type)
  {
    this.myBoardSize = size;
		this.myPlayers = playerCount;
		this.myBoardType = type;
   	this.myWallNumber = numberOfWalls(this.myBoardSize, this.myPlayers);

    this.myWallCounts = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCounts[i] = this.myWallNumber;
    }

    initialize();
  }

	/**
	 * This yields 10 for board size 9, as it should, as well as
	 * reasonable numbers for the other sizes.
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
		this.myLocations = new Point[this.myPlayers];
		this.placePieces();

		// Put the pieces on the board
		for (int i = 0; i < this.myPlayers; i++)
			this.myBoard[this.myLocations[i].x][this.myLocations[i].y] = i + 1;

		// Start list of moves
		this.moves = new ArrayList<int[]>();

		// Start keeping track of walls in my path.
		this.wallsInPath = new boolean[half * half * 2][2];
  }

	/**
	 * Make sure all pieces end up on appropriate starting squares.
	 */
	private void placePieces()
	{
		int half = this.myBoardSize - 1;

		// Every board has one player in the same location.
		this.myLocations[0] = new Point(half, 0);

		// Except with three players, every board has an opponent across
		if (this.myPlayers != 3)
			this.myLocations[1] = new Point(half, 2 * half);
		else
			this.myLocations[1] = new Point(3 * half / 2, 3 * half / 2);

		// Add third and fourth players for normal boards
		if (this.myPlayers == 4 && this.myBoardType != "hexagonal")
		{
			this.myLocations[2] = new Point(2 * half, half);
			this.myLocations[3] = new Point(0, half);
		}

		// Players 3-6 always go in the same place
		if (this.myBoardType == "hexagonal" && this.myPlayers > 2)
		{
			// Third player
			this.myLocations[2] = new Point(half / 2, 3 * half / 2);

			// Fourth player
			if (this.myPlayers > 3)
				this.myLocations[3] = new Point(3 * half / 2, half / 2);

			// Fifth and Sixth players
			if (this.myPlayers == 6)
			{
				this.myLocations[4] = new Point(3 * half / 2, 3 * half / 2);
				this.myLocations[5] = new Point(half / 2, half / 2);
			}
		}
		
		// If we have an even number of squares, we have to shift one out of
		// each pair of pieces to make the board rotation-symmetric.
		//
		// TODO adjust pieces on even hex boards.
    if (isEven(this.myBoardSize))
		{
			if (this.myBoardType != "hexagonal")
			{
				this.myLocations[0].x += 2;

				if (this.myPlayers == 4)
					this.myLocations[2].y += 2;
			}
    }
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
				wallVal(x, y, o, 0);

				this.myWallCounts[turn] += 1;
			}

			// undo move
			else
			{
				this.myBoard[x][y] = turn + 1;
				this.myBoard[this.myLocations[turn].x][this.myLocations[turn].y] = 0;
				this.myLocations[turn].x = x;
				this.myLocations[turn].y = y;
			}

			this.pathLengths = new int[this.myPlayers];
			for (int j = 0; j < this.myPlayers; j++)
				this.pathLengths[j] = this.pathLength(j);
		}
	}

	/**
	 * Checks for move legality, and if legal, moves the player.
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
		Point origin = new Point(this.myLocations[turn]);

		if (this.myBoardType == "hexagonal"
				&& isLegalHexMove(origin, new Point(x, y))
			|| this.myBoardType != "hexagonal"
				&& isLegalMove(x, y, origin.x, origin.y))
		{
			// make the move
			this.myLocations[turn].x = x;
			this.myLocations[turn].y = y;
			this.myBoard[origin.x][origin.y] = 0;
			this.myBoard[x][y] = turn + 1;

			// update shortest path length
			this.pathLengths[turn] = pathLength(turn);

			// update turn
			this.myTurn++;

			// add old location to undo list
			this.moves.add(new int[] {origin.x, origin.y, 0});

			return true;
		}

		return false;
	}

	/**
	 * Check if this piece movement is legal.
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
		if (!isOnBoard(x, y))
			return false;

		// Check if another player is where we're going
		if (this.myBoard[x][y] != 0)
			return false;

		// jump dist
		int Xdist = Math.abs(x - oldX);
		int Ydist = Math.abs(y - oldY);
		int avgX = (x + oldX) / 2;
		int avgY = (y + oldY) / 2;
		int inBetween = this.myBoard[avgX][avgY];
		int onePastX = x + avgX - oldX;
		int onePastY = y + avgY - oldY;

		// normal move: one space away and no wall between
		if (
			// one space away
			(Xdist == 2 && Ydist == 0
			 || Ydist == 2 && Xdist == 0)

			// no wall in-between
			&& inBetween != WALL_VALUE)
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
				&& inBetween != 0
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
	 * Check if this piece movement is legal on a hex board.
	 *
	 * Params:
	 *   origin = previous location
	 *   destination = attempted location
	 *
	 * Returns whether or not this move is legal, true or false
	 */
	boolean isLegalHexMove(final Point origin, final Point destination)
	{
		// Can't jump off the board!
		if (!isOnBoard(destination))
			return false;

		// There's a piece where we're trying to go!
		if (this.myBoard[destination.x][destination.y] != 0)
			return false;

		// How far did we travel in each direction?
		final int Xdist = Math.abs(destination.x - origin.x);
		final int Ydist = Math.abs(destination.y - origin.y);

		// The middle ground
		final int avgX = (destination.x + origin.x) / 2;
		final int avgY = (destination.y + origin.y) / 2;
		final int inBetween = this.myBoard[avgX][avgY];
		
		// Which direction are we moving in?
		final int Xdir = (destination.x - origin.x) / Xdist;
		final int Ydir = (destination.y - origin.y) / Ydist;

		// Normal move
		if ((Xdist == 2 && Ydist == 0 || Xdist == 1 && Ydist == 2)
				&& !wallBetween(origin, destination))
			return true;

		// Jump in a straight line
		Point jump = new Point(avgX, avgY);
		if ((Xdist == 4 && Ydist == 0 || Xdist == 2 && Ydist == 4)
				&& legalJump(origin, jump, destination))
			return true;

		// Jump two spaces vertically by jumping over a piece
		Point leftJump = new Point(origin.x - 1, avgY);
		Point leftWall = new Point(origin.x - 2, (destination.y + avgY) / 2);
		Point rightJump = new Point(origin.x + 1, avgY);
		Point rightWall = new Point(origin.x + 2, (destination.y + avgY) / 2);
		if (Xdist == 0 && Ydist == 4
				&& (legalJump(origin, leftJump, destination, leftWall)
					|| legalJump(origin, rightJump, destination, rightWall)))
			return true;

		// Jump in a 120 degree angle to a horizontal location.
		jump = new Point(origin.x + 2 * Xdir, origin.y);
		Point wall = new Point(origin.x + 3 * Xdir, origin.y - Ydir);
		if (Xdist == 3 && Ydist == 2
				&& legalJump(origin, jump, destination, wall))
			return true;

		// Jump in a 60 degree angle to a horizontal location.
		leftJump = new Point(avgX, origin.y + 2);
		Point leftWall1 = new Point(origin.x, origin.y + 3);
		Point leftWall2 = new Point(destination.x, origin.y + 3);
		rightJump = new Point(avgX, origin.y - 2);
		Point rightWall1 = new Point(origin.x, origin.y - 3);
		Point rightWall2 = new Point(destination.x, destination.y - 3);
		if (Xdist == 2 && Ydist == 0
				&& (legalJump(origin, leftJump, destination, leftWall1, leftWall2)
					|| legalJump(origin, rightJump, destination, rightWall1, rightWall2)))
			return true;

		// Jump in a 60 degree angle to a vertical location.
		leftJump = new Point(origin.x + 2 * Xdir, origin.y);
		leftWall1 = new Point(origin.x + 2 * Xdir, origin.y - Ydir);
		leftWall2 = new Point(origin.x + 3 * Xdir, avgY);
		rightJump = new Point(origin.x - Xdir, destination.y);
		rightWall1 = new Point(origin.x - Xdir, destination.y + Ydir);
		rightWall2 = new Point(origin.x - 2 * Xdir, avgY);
		if (Xdist == 1 && Ydist == 2
				&& (legalJump(origin, leftJump, destination, leftWall1, leftWall2)
					|| legalJump(origin, rightJump, destination, rightWall1, rightWall2)))
			return true;

		// We've failed to find a valid way to make this move
		return false;
	}

	/**
	 * Tests a jump to see if it is legal.
	 *
	 * Params:
	 *   origin = Where we're jumping from
	 *   jump = Where we're jumping over
	 *   destination = Where we're jumping to
	 *   walls = Array of walls that have to exist for the jump to be legal
	 */
	boolean legalJump(
			final Point origin,
			final Point jump,
			final Point destination,
			final Point... walls)
	{
		// Jumping over a piece
		if (!isOnBoard(jump) || this.myBoard[jump.x][jump.y] == 0)
			return false;
				
		// Gotta be a wall or the edge of the board behind them
		for (Point wall : walls)
			if (isOnBoard(wall) && this.myBoard[wall.x][wall.y] != WALL_VALUE)
				return false;

		// Can't be a wall between any space on the jump route
		if (wallBetween(origin, jump) || wallBetween(jump, destination))
			return false;
		
		return true;
	}

	/**
	 * Tests whether or not there is a wall between two adjacent hexes.
	 */
	boolean wallBetween(final Point origin, final Point destination)
	{
		// Assure we're only one space away
		assert Math.abs(destination.x - origin.x) == 2
			&& Math.abs(destination.y - origin.y) == 0
			|| Math.abs(destination.x - origin.x) == 1
			&& Math.abs(destination.y - origin.y) == 2;

		final int avgX = (origin.x + destination.x) / 2;
		final int avgY = (origin.y + destination.y) / 2;

		// If we're going horizontally by two board units
		if (origin.y == destination.y)
			return isOnBoard(origin.y + 1)
					&& this.myBoard[avgX][origin.y + 1] == WALL_VALUE
				|| isOnBoard(origin.y - 1)
					&& this.myBoard[avgX][origin.y - 1] == WALL_VALUE;

		// Otherwise we're going two up and one over
		else
			return this.myBoard[origin.x][avgY] == WALL_VALUE
				|| this.myBoard[destination.x][avgY] == WALL_VALUE;
	}

	/**
	 * Checks for wall legality, and if legal, places the wall.
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
	 * the shortest path, so we can recalculate when necessary.
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

		// vertical hex move
		else if (this.myBoardType == "hexagonal")
		{
			wallsInPath[player][linearize(x, avgY, 1)] = true;
			wallsInPath[player][linearize(oldX, avgY, 1)] = true;
		}

		// vertical move on normal board
		else
		{
			if (isOnBoard(x - 1))
				wallsInPath[player][linearize(x - 1, avgY, 2)] = true;

			if (isOnBoard(x + 1))
				wallsInPath[player][linearize(x + 1, avgY, 2)] = true;
		}
	}
	
	/**
	 * Calculate linear location in array from x and y.
	 */
	int linearize(int x, int y, int o)
	{
		return x - 1 + (this.myBoardSize - 1) * (y - 1) + o - 1;
	}

	/**
	 * Asserts a wall is legal.
	 *
	 * Params:
	 *   x = horizontal location of new wall
	 *   y = vertical location of new wall
	 *   o = orientation of new wall (vertical, 1, or horizontal, 2)
	 */
	boolean isLegalWall(final int x, final int y, final int o)
	{
		// Make sure wall isn't in move land
		assert this.myBoardType == "hexagonal" || !isEven(x) && !isEven(y);
		assert this.myBoardType != "hexagonal" || !isEven(y);

		// Make sure orientation is valid
		assert o == 1 || o == 2;

		// check for out-of-bounds
		if (!isOnBoard(x, y))
			return false;

		// Make sure the player has walls left
		if (this.myWallCounts[this.myTurn % this.myPlayers] == 0)
			return false;

		// Hex board walls cannot be immediately next to any other walls
		if (this.myBoardType == "hexagonal")
		{
			final int yAdd = 2 * ((x + y) % 2) - 1;

			if (this.myBoard[x][y] != 0
					|| this.myBoard[x][y + yAdd] != 0
					|| this.myBoard[x - 1][y] != 0
					|| this.myBoard[x + 1][y] != 0)
				return false;
		}

		// Default board actually fills three locations
		else
		{
			final int xAdd = o - 1;
			final int yAdd = o % 2;

			if (this.myBoard[x][y] != 0
					|| this.myBoard[x + xAdd][y + yAdd] != 0
					|| this.myBoard[x - xAdd][y - yAdd] != 0)
				return false;
		}

		return true;
	}

	/**
	 * The wall defined by x, y, and o will be set to 'val'.
	 */
	void wallVal(final int x, final int y, final int o, final int val)
	{
		// Walls on hex board take up 2 locations.
		if (this.myBoardType == "hexagonal")
		{
			final int yAdd = 2 * ((x + y) % 2) - 1;

			this.myBoard[x][y] = val;
			this.myBoard[x][y + yAdd] = val;
		}
		
		// Walls on the default board takes up 3 locations.
		else
		{
			final int xAdd = o - 1;
			final int yAdd = o % 2;

			this.myBoard[x][y] = val;
			this.myBoard[x + xAdd][y + yAdd] = val;
			this.myBoard[x - xAdd][y - yAdd] = val;
		}
	}

	/**
	 * Tests whether a single dimension is within the limits of the board.
	 *
	 * This should only be called for the default board, because figuring
	 * out if a dimension is on the hexagonal board requires both dimensions.
	 */
	boolean isOnBoard(int d)
	{
		return 0 <= d && d < this.myBoardSize * 2 + 1;
	}

	/**
	 * Tests whether a (x, y) location is within the limits of the board.
	 *
	 * This can be called for either default or hexagonal boards.
	 */
	boolean isOnBoard(int x, int y)
	{
		if (this.myBoardType != "hexagonal")
			return isOnBoard(x) && isOnBoard(y);
		else
			return y >= 0
				&& y < 2 * this.myBoardSize - 1
				&& -2 * x + y <= this.myBoardSize - 1
				&& 2 * x - y <= this.myBoardSize * 3 - 1
				&& 2 * x + y >= this.myBoardSize + 1
				&& 2 * x + y <= 5 * this.myBoardSize - 1;
	}


	/**
	 * Convenience function forwarding to isOnBoard(int, int).
	 */
	boolean isOnBoard(Point test)
	{
		return isOnBoard(test.x, test.y);
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
		if (this.myLocations[0].y == 0)
			won = -100;
		if (this.myLocations[1].y == this.myBoardSize - 1)
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
		final Point origin = this.myLocations[this.myTurn % 2];
		final int oldPathLength = this.pathLengths[this.myTurn % 2];
		final int opponentX = this.myLocations[(this.myTurn + 1) % 2].x;
		final int opponentY = this.myLocations[(this.myTurn + 1) % 2].y;
		boolean first = true;
		QuoridorModel testBoard = new QuoridorModel(this);

		// We'll only do this for the root node, where we
		// have a best move recorded
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
		for (Point move : legalMoves(origin))
		{
			// legal and we haven't checked it already
			if (isLegalMove(move.x, move.y, origin.x, origin.y)
				 && (best.length < 2
					 || best[2] != 0
					 || best[0] != move.x
					 || best[1] != move.y
					)
				)
			{
				testBoard = new QuoridorModel(this);
				testBoard.movePiece(move.x, move.y);
				
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
					bestX = move.x;
					bestY = move.y;
					bestO = 0;
				}

				if (alpha >= beta
						|| System.currentTimeMillis() - t0 > millis)
					return new int[] {bestX, bestY, bestO, alpha};

				scoutVal = alpha + 1;

				if (first)
					first = false;
			}

			// Check jumps
			else if (isOnBoard(move.x, move.y)
				 && this.myBoard[move.x][move.y] != 0)
			{
				for (Point jump : legalMoves(move))
				{
					if (isLegalMove(jump.x, jump.y, origin.x, origin.y))
					{
						testBoard = new QuoridorModel(this);
						testBoard.movePiece(jump.x, jump.y);
	
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
							bestX = jump.x;
							bestY = jump.y;
							bestO = 0;
						}

						if (alpha >= beta
								|| System.currentTimeMillis() - t0 > millis)
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
							|| Math.abs(x - origin.x) == 1 && Math.abs(y - origin.y) == 1

							// check walls around my opponent
							|| Math.abs(x - opponentX) == 1
							 	&& Math.abs(y - opponentY) == 1

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
	 * Finds the length of the shortest path for a player.
	 *
	 * Also keeps track of walls that would block the path.
	 *
	 * Returns: length of the shortest path, ignoring the other player.
	 * 	 If there is no available path, returns 0.
	 */
	int pathLength(int player)
	{
		// Remove other players from the board so that they don't
		// interfere with finding the shortest path.
		removePlayers(player);

		// get current location
		int x = this.myLocations[player].x;
		int y = this.myLocations[player].y;

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
			for (Point move : legalMoves(new Point(x, y)))
			{
				if (isLegalMove(move.x, move.y, x, y) && paths[move.x][move.y] == 0)
				{
					h = heuristic(player, move.x, move.y);
					paths[move.x][move.y] = 100 * x + y + 2;

					if (!nodes.containsKey(g + h + 2))
						nodes.put(g + h + 2, new ArrayList<int[]>());

					nodes.get(g + h + 2).add(new int[] {move.x, move.y, g + 2});
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
	 * List all legal moves.
	 */
	Point[] legalMoves(Point origin)
	{
		if (this.myBoardType == "hexagonal")
			return new Point[] {
				new Point(origin.x - 2, origin.y),
				new Point(origin.x + 2, origin.y),
				new Point(origin.x - 1, origin.y - 2),
				new Point(origin.x - 1, origin.y + 2),
				new Point(origin.x + 1, origin.y - 2),
				new Point(origin.x + 1, origin.y + 2)
			};
		else
			return new Point[] {
				new Point(origin.x - 2, origin.y),
				new Point(origin.x, origin.y - 2),
				new Point(origin.x + 2, origin.y),
				new Point(origin.x, origin.y + 2)
			};
	}

	/**
	 * Remove all players except the excluded player from the board
	 * for purposes of finding a shortest path.
	 */
	void removePlayers(int excludedPlayer)
	{
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (i != excludedPlayer)
				this.myBoard[this.myLocations[i].x][this.myLocations[i].y] = 0;
		}
	}

	/**
	 * Add all players except the excluded player to the board
	 * at the stored locations.
	 */
	void addPlayers(int excludedPlayer)
	{
		for (int i = 0; i < this.myPlayers; i++)
		{
			if (i != excludedPlayer)
				this.myBoard[this.myLocations[i].x][this.myLocations[i].y] = i + 1;
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
				if (this.myPlayers != 3)
					return this.myBoardSize * 2 - 1 - y;
				else
					return this.myBoardSize * 3 - 2 * x + y - 2;

			case 2:
				if (this.myBoardType == "hexagonal")
					return 5 * this.myBoardSize - 2 * x - y - 2;
				else
					return this.myBoardSize * 2 - 1 - x;

			case 3:
				return 5 * this.myBoardSize - 2 * x - y - 2;

			case 4:
				if (this.myBoardType == "hexagonal")
					return this.myBoardSize - 2 * x - y;
				else
					return x;

			case 5:
				return this.myBoardSize * 3 - 2 * x + y - 2;

			case 6:
				return this.myBoardSize + 2 * x - y - 2;

			default:
				assert false;
		}

		return 100;
	}

	/**
	 * Check if the game is over.
	 */
	boolean isGameOver()
	{
		for (int i = 0; i < this.myPlayers; i++)
			if (heuristic(i, this.myLocations[i].x, this.myLocations[i].y) == 0)
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
		return this.myLocations[player - 1].x + 1;
	}

	public int getRow(int player)
	{
		return this.myLocations[player - 1].y + 1;
	}
}
