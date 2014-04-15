package passaj;

import java.util.ArrayList;

public class HexQuoridorModel
{
  private int[][] myHexBoard;
  private int[][] testBoard;
  private int[] myWallCount;
  private int myRadius;
  private int myTurn;
  private int myPlayers;
  private int myBoardSize;
  private int myWallNumber;
  private int myCompLevel = 0;
  private IllegalArgumentException wallException;
  private IllegalArgumentException wallFilled;
  private IllegalArgumentException blockException;

  public HexQuoridorModel()
  {
    this.myBoardSize = 9;
    this.myPlayers = 2;
    initialize();

    this.myWallCount = new int[2];
    this.myWallCount[0] = ((this.myBoardSize - 1) * (this.myBoardSize + 2 * this.myBoardSize - 3) / 16);
    this.myWallCount[1] = ((this.myBoardSize - 1) * (this.myBoardSize + 2 * this.myBoardSize - 3) / 16);

    this.myHexBoard[this.myBoardSize][1] = 1;
    this.myHexBoard[this.myBoardSize][(2 * this.myBoardSize - 1)] = 2;
  }

  public HexQuoridorModel(HexQuoridorModel model) {
    this.myBoardSize = model.getBoardSize();
    this.myPlayers = model.getPlayers();
    initialize();
    this.myCompLevel = model.getCompLevel();

    this.myWallCount = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCount[i] = model.getWallCount(i + 1);
    }

    this.myTurn = model.getTurn();
    this.myHexBoard = deepCopy(model.getBoard());
  }

  public HexQuoridorModel(int players, int size, int compLevel)
  {
    this.myBoardSize = size;
    this.myPlayers = players;
    initialize();

    if (this.myPlayers == 1) {
      this.myCompLevel = compLevel;
      this.myPlayers = 2;
      this.myWallNumber = ((this.myBoardSize - 1) * (3 * this.myBoardSize - 3) / 8 / this.myPlayers);
      this.myWallCount = new int[] { this.myWallNumber, this.myWallNumber };
    } else {
      this.myWallNumber = ((this.myBoardSize - 1) * (3 * this.myBoardSize - 3) / 8 / this.myPlayers);
      this.myWallCount = new int[this.myPlayers];
      for (int i = 0; i < this.myPlayers; i++) {
        this.myWallCount[i] = this.myWallNumber;
      }
    }

    if (this.myPlayers != 4) {
      this.myHexBoard[this.myBoardSize][1] = 1;
    } else {
      this.myHexBoard[(2 * this.myBoardSize - (this.myBoardSize - 1) / 4 - 1)][((this.myBoardSize + 1) / 2)] = 1;
      this.myHexBoard[((this.myBoardSize - 1) / 4 + 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 2;
      this.myHexBoard[(2 * this.myBoardSize - (this.myBoardSize - 1) / 4 - 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 3;
      this.myHexBoard[((this.myBoardSize - 1) / 4 + 1)][((this.myBoardSize + 1) / 2)] = 4;
    }
    if ((this.myPlayers == 2) || (this.myPlayers == 6)) {
      this.myHexBoard[this.myBoardSize][(2 * this.myBoardSize - 1)] = 2;
    }
    if (this.myPlayers == 3) {
      this.myHexBoard[(2 * this.myBoardSize - (this.myBoardSize - 1) / 4 - 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 2;
      this.myHexBoard[((this.myBoardSize - 1) / 4 + 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 3;
    }

    if (this.myPlayers == 6) {
      this.myHexBoard[(2 * this.myBoardSize - (this.myBoardSize - 1) / 4 - 1)][((this.myBoardSize + 1) / 2)] = 3;
      this.myHexBoard[((this.myBoardSize - 1) / 4 + 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 4;
      this.myHexBoard[(2 * this.myBoardSize - (this.myBoardSize - 1) / 4 - 1)][((this.myBoardSize + 1) * 3 / 2 - 2)] = 5;
      this.myHexBoard[((this.myBoardSize - 1) / 4 + 1)][((this.myBoardSize + 1) / 2)] = 6;
    }
  }

  private void initialize()
	{
    this.wallException = new IllegalArgumentException("There is a wall between where you are and where you want to be.");
    this.wallFilled = new IllegalArgumentException("There is a wall already there!");
    this.blockException = new IllegalArgumentException("You may not block anybody from getting to their goal row");

    this.myTurn = 1;

    this.myHexBoard = new int[2 * this.myBoardSize + 1][2 * this.myBoardSize + 1];
  }

	public void move(int x, int y)
	{
		if (!isEven(y))
    	changePos(x, y);
		else
      addWall(8, x, y, false);
	}

  public void changePos(int x, int y)
  {
    int piece = getPlayer();

    int i = getColumn(piece);
    int j = getRow(piece);

    if (checkPos(this.myHexBoard, i, j, x, y, false)) {
      this.myHexBoard[i][j] = 0;
      this.myHexBoard[x][y] = piece;
      this.myTurn += 1;
    }

    for (int c = 1; c <= this.myPlayers; c++) {
      int a = getColumn(c);
      int b = getRow(c);
      this.testBoard = deepCopy(this.myHexBoard);
      if ((!find(this.testBoard, c, a, b)) && (this.myWallCount[(c - 1)] <= 0)) {
        this.myTurn += 1;
        throw new IllegalArgumentException("Player " + c + " was skipped because they couldn't move");
      }
    }
  }

  public void addWall(int wallNumber, int x, int y, boolean AImove)
  {
    int playerToAdd = getPlayer() - 1;

    if ((wallNumber == 8) && 
      (this.myWallCount[playerToAdd] <= 0)) {
      throw new IllegalArgumentException("You are all out of walls");
    }
    if ((y > 2 * this.myBoardSize - 1) || (y < 1) || (2 * x - y > 3 * this.myBoardSize - 2) || (2 * x + y < this.myBoardSize + 2) || 
      (2 * x + y > 5 * this.myBoardSize - 2) || (y - 2 * x > this.myBoardSize - 2)) {
      throw new IllegalArgumentException("Click ON the board");
    }

    if (checkWall(wallNumber, x, y, false)) {
      this.myHexBoard[x][y] = wallNumber;
      if ((wallNumber == 8) && (!AImove)) {
        this.myWallCount[playerToAdd] -= 1;
        this.myTurn += 1;
      }
    } else {
      throw this.wallFilled;
    }
  }

  public boolean checkWall(int wallNumber, int x, int y, boolean search)
  {
    if ((y >= 2 * this.myBoardSize - 1) || (y <= 1) || (2 * x - y >= 3 * this.myBoardSize - 2) || (2 * x + y <= this.myBoardSize + 2) || 
      (2 * x + y >= 5 * this.myBoardSize - 2) || (y - 2 * x >= this.myBoardSize - 2) || (x <= 1) || (x >= 2 * this.myBoardSize - 1)) {
      if (search) {
        return false;
      }
      throw new IllegalArgumentException("Click on the board");
    }if ((this.myHexBoard[x][y] != 8) && (this.myHexBoard[x][y] != wallNumber) && (this.myHexBoard[(x + 1)][y] != 8) && 
      (this.myHexBoard[(x + 1)][y] != wallNumber) && (this.myHexBoard[(x - 1)][y] != 8) && (this.myHexBoard[(x - 1)][y] != wallNumber) && (
      ((!isEven(x)) && 
      (!isEven(y / 2))) || (
      ((isEven(x)) && (isEven(y / 2)) && 
      (this.myHexBoard[x][(y + 2)] != 8) && (this.myHexBoard[x][(y + 2)] != wallNumber)) || (
      ((!isEven(x)) && (isEven(y / 2))) || ((isEven(x)) && 
      (!isEven(y / 2)) && 
      (this.myHexBoard[x][(y - 2)] != 8) && (this.myHexBoard[x][(y - 2)] != wallNumber))))))
    {
      for (int i = 1; i <= this.myPlayers; i++) {
        int a = getColumn(i);
        int b = getRow(i);
        this.testBoard = deepCopy(this.myHexBoard);
        this.testBoard[x][y] = 8;
        if (!find(this.testBoard, i, a, b)) {
          if (search) {
            return false;
          }
          this.myHexBoard[x][y] = 10;
          throw this.blockException;
        }
      }
      return true;
    }
    return false;
  }

  public boolean checkPos(int[][] board, int xold, int yold, int xnew, int ynew, boolean search)
  {
    int i = xold;
    int j = yold;
    int k = xnew;
    int l = ynew;

    if (!isOnBoard(k, l)) {
      if (search) {
        return false;
      }
      throw new IllegalArgumentException("Click ON the board");
    }
    if ((board[k][l] != 0) && (board[k][l] != this.myPlayers + 1)) {
      if (search) {
        return false;
      }
      throw new IllegalArgumentException("There is already a piece where you are attemting to move.");
    }

    if (((i + 2 != k) && (i - 2 != k)) || ((j == l) || (((j + 2 == l) || (j - 2 == l)) && ((i + 1 == k) || (i - 1 == k)))))
    {
      if ((j == l) && (board[((i + k) / 2)][(j + 1)] != 8) && (board[((i + k) / 2)][(j - 1)] != 8)) {
        return true;
      }
      if ((j + 3 <= 2 * this.myBoardSize) && 
        (board[((i + k) / 2)][(j - 1)] == 8) && 
        (board[((i + k) / 2)][(j + 2)] != 0) && 
        ((board[((i + k) / 2)][(j + 3)] == 8) || (board[k][(j + 3)] == 8) || (!isOnBoard((i + k) / 2, j + 3)) || 
        (!isOnBoard(k, j + 3))) && (board[i][(j + 1)] != 8) && (board[k][(j + 1)] != 8))
        return true;
      if ((j - 3 >= 0) && 
        (board[((i + k) / 2)][(j + 1)] == 8) && 
        (board[((i + k) / 2)][(j - 2)] != 0) && 
        ((board[((i + k) / 2)][(j - 3)] == 8) || (board[k][(j - 3)] == 8) || (!isOnBoard((i + k) / 2, j - 3)) || 
        (!isOnBoard(k, j - 3))) && (board[i][(j - 1)] != 8) && (board[k][(j - 1)] != 8)) {
        return true;
      }
      if (((j + 2 == l) || (j - 2 == l)) && (board[i][((j + l) / 2)] != 8) && (board[k][((j + l) / 2)] != 8)) {
        return true;
      }
      if (((j + 2 == l) || (j - 2 == l)) && (k + 2 <= 2 * this.myBoardSize) && (k - 2 >= 0)) {
        if (((board[(2 * i - k)][l] != 0) && 
          (board[k][((j + l) / 2)] == 8) && 
          (board[(2 * i - k)][((j + l) / 2)] != 8) && 
          (board[i][(j + 3 * (l - j) / 2)] != 8) && (
          (board[(2 * i - k)][(j + 3 * (l - j) / 2)] == 8) || (board[(3 * i - 2 * k)][(j + 3 * (l - j) / 2)] == 8) || 
          (!isOnBoard(2 * i - k, j + 3 * (l - j) / 2)) || (!isOnBoard(3 * i - 2 * k, j + 3 * (l - j) / 2)))) || (
          (board[(2 * k - i)][j] != 0) && 
          ((board[(3 * k - 2 * i)][(j + 1)] == 8) || (board[(3 * k - 2 * i)][(j - 1)] == 8) || 
          (!isOnBoard(3 * k - 2 * i, j + 1)) || (!isOnBoard(3 * k - 2 * i, j - 1))) && 
          (board[i][((j + l) / 2)] == 8) && (board[k][(j + 1)] != 8) && (board[k][(j - 1)] != 8) && 
          (board[(2 * k - i)][((j + l) / 2)] != 8)))
          return true;
        if (search) {
          return false;
        }
        throw this.wallException;
      }
    }
    else {
      if (((i + 4 != k) && (i - 4 != k)) || (((j == l) && (board[((i + k) / 2)][l] != 0)) || (((j + 4 == l) || (j - 4 == l)) && 
        ((i + 2 == k) || (i - 2 == k)) && (board[((i + k) / 2)][((j + l) / 2)] != 0)))) {
        if (((j == l) && (board[((i + k) / 2 + 1)][(j + 1)] != 8) && (board[((i + k) / 2 + 1)][(j - 1)] != 8) && 
          (board[((i + k) / 2 - 1)][(j + 1)] != 8) && (board[((i + k) / 2 - 1)][(j - 1)] != 8)) || (
          ((i + 2 == k) || (i - 2 == k)) && 
          (board[((i + k) / 2)][((j + l) / 2 + 1)] != 8) && (board[i][(j + (l - j) / 4)] != 8) && 
          (board[((i + k) / 2)][((j + l) / 2 - 1)] != 8) && (board[k][(j + 3 * (l - j) / 4)] != 8)))
          return true;
        if (search) {
          return false;
        }
        throw this.wallException;
      }

      if (((i + 3 != k) && (i - 3 != k)) || ((j + 2 == l) || (j - 2 == l) || ((i == k) && ((j + 4 == l) || (j - 4 == l))))) {
        if (((i == k) && 
          (board[i][((j + l) / 2 + 1)] != 8) && 
          (board[i][((j + l) / 2 - 1)] != 8) && (
          ((board[(i + 1)][((j + l) / 2)] != 0) && 
          ((board[(i + 2)][(j + 3 * (l - j) / 4)] == 8) || (!isOnBoard(i + 2, j + 3 * (l - j) / 4))) && 
          (board[(i + 1)][(j + (l - j) / 4)] != 8)) || (
          (board[(i - 1)][((j + l) / 2)] != 0) && 
          ((board[(i - 2)][(j + 3 * (l - j) / 4)] == 8) || (!isOnBoard(i - 2, j + 3 * (l - j) / 4))) && 
          (board[(i - 1)][(j + (l - j) / 4)] != 8)))) || (
          ((i + 3 == k) || (i - 3 == k)) && (
          ((board[(i + (k - i) * 2 / 3)][j] != 0) && ((board[k][(j + (j - l) / 2)] == 8) || (!isOnBoard(k, j + (j - l) / 2))) && 
          (board[(i + (k - i) / 3)][(j + (l - j) / 2)] != 8) && (board[(i + (k - i) / 3)][(j + (j - l) / 2)] != 8)) || (
          (board[
          (i + 
          (k - i) / 3)][l] != 0) && 
          ((board[(i + (k - i) / 3)][(j + 3 * ((j + l) / 2 - j))] == 8) || 
          (!isOnBoard(i + (k - i) / 3, j + 3 * (
          (j + l) / 2 - j)))) && (board[i][((j + l) / 2)] != 8) && 
          (board[(i + (k - i) / 3)][((j + l) / 2)] != 8) && (board[(i + (k - i) * 2 / 3)][((j + l) / 2)] != 8)))))
          return true;
        if (search) {
          return false;
        }
        throw this.wallException;
      }
    }
    if (search) {
      return false;
    }
    throw new IllegalArgumentException("You are not next to the space you clicked on");
  }

  public boolean checkMove(int[][] board, int xold, int yold, int xnew, int ynew)
  {
    int i = xold;
    int j = yold;
    int k = xnew;
    int l = ynew;

    if ((ynew > 2 * this.myBoardSize - 1) || (ynew < 1) || (2 * xnew - ynew > 3 * this.myBoardSize - 2) || (2 * xnew + ynew < this.myBoardSize + 2) || 
      (2 * xnew + ynew > 5 * this.myBoardSize - 2) || (ynew - 2 * xnew > this.myBoardSize - 2)) {
      return false;
    }

    if (((i + 2 != k) && (i - 2 != k)) || ((j == l) || (((j + 2 == l) || (j - 2 == l)) && ((i + 1 == k) || (i - 1 == k))))) {
      if (((j == l) && (board[((i + k) / 2)][(j + 1)] != 8) && (board[((i + k) / 2)][(j - 1)] != 8)) || (((j + 2 == l) || (j - 2 == l)) && 
        (board[i][((j + l) / 2)] != 8) && (
        ((board[(i - 1)][((j + l) / 2)] != 8) && (i - 1 == k)) || ((board[(i + 1)][((j + l) / 2)] != 8) && (i + 1 == k))))) {
        return true;
      }
      return false;
    }
    return false;
  }

  public boolean find(int[][] board, int piece, int x, int y)
  {
    if (gameOver(piece, x, y)) {
      return true;
    }
    if ((board[x][y] == 10) || (board[x][y] == 11) || (board[x][y] == 12) || (board[x][y] == 13) || (board[x][y] == 14) || 
      (board[x][y] == 15) || (board[x][y] == 16)) {
      return false;
    }
    board[x][y] += 10;
    if ((x > 1) && 
      (checkMove(board, x, y, x - 2, y)) && 
      (find(board, piece, x - 2, y)))
      return true;
    if ((x < 2 * this.myBoardSize - 1) && 
      (checkMove(board, x, y, x + 2, y)) && 
      (find(board, piece, x + 2, y)))
      return true;
    if ((y > 1) && (x < 2 * this.myBoardSize) && 
      (checkMove(board, x, y, x + 1, y - 2)) && 
      (find(board, piece, x + 1, y - 2)))
      return true;
    if ((y > 1) && (x > 0) && 
      (checkMove(board, x, y, x - 1, y - 2)) && 
      (find(board, piece, x - 1, y - 2)))
      return true;
    if ((y < 2 * this.myBoardSize - 1) && (x > 0) && 
      (checkMove(board, x, y, x - 1, y + 2)) && 
      (find(board, piece, x - 1, y + 2)))
      return true;
    if ((y < 2 * this.myBoardSize - 1) && (x < 2 * this.myBoardSize) && 
      (checkMove(board, x, y, x + 1, y + 2)) && 
      (find(board, piece, x + 1, y + 2)))
      return true;
    return false;
  }

  public ArrayList<Integer> shortPath(int piece) {
    ArrayList<Integer> path = new ArrayList<Integer>();
    ArrayList<Integer> queue = new ArrayList<Integer>();
    boolean finished = false;
    this.testBoard = deepCopy(this.myHexBoard);
    queue.add(getColumn(piece));
    queue.add(getRow(piece));
    int x = queue.get(0); int y = queue.get(1);
    this.testBoard[x][y] = (100 * x + y);
    for (int i = 0; 
      queue.size() != i; i += 2)
    {
      x = queue.get(i);
      y = queue.get(i + 1);
      for (int j = x - 4; j <= x + 4; j++) {
        for (int k = y - 4; k <= y + 4; k += 2) {
          if ((checkPos(this.myHexBoard, x, y, j, k, true)) && 
            (this.testBoard[j][k] < 10)) {
            queue.add(j);
            queue.add(k);
            this.testBoard[j][k] = (100 * x + y);
            if (gameOver(piece, j, k)) {
              finished = true;
              break;
            }
          }
        }
        if (finished)
          break;
      }
      if (finished)
        break;
    }
    path.add(queue.get(queue.size() - 2));
    path.add(queue.get(queue.size() - 1));
    for (int i = 1; i <= 2 * this.myBoardSize; i += 2) {
			if (path.get(0) != getColumn(piece) || path.get(1) != getRow(piece)) 
      	continue;
      for (int j = 1; j <= 2 * this.myBoardSize; j += 2)
        if ((path.get(0) == i) && (path.get(1) == j)) {
          path.add(0, this.testBoard[i][j] / 100);
          path.add(1, this.testBoard[i][j] - path.get(0) * 100);
        }
      i++;
    }

    if ((path.get(0) == path.get(2)) && (path.get(1) == path.get(3))) {
      path.remove(3);
      path.remove(2);
    }
    return path;
  }

  public void moveComp(int player)
  {
    ArrayList<Integer> path = shortPath(player);
    if (this.myWallCount[0] == this.myWallNumber) {
      if (checkPos(this.myHexBoard, getColumn(2), getRow(2), getColumn(2) + 2, getRow(2) - 4, true))
        changePos(getColumn(2) + 2, getRow(2) - 4);
      else if (checkPos(this.myHexBoard, getColumn(2), getRow(2), getColumn(2) - 2, getRow(2) - 4, true))
        changePos(getColumn(2) - 2, getRow(2) - 4);
      else if ((getRow(1) < getRow(2)) && (checkPos(this.myHexBoard, getColumn(2), getRow(2), getColumn(1), getRow(2), true)))
        changePos(getColumn(1), getRow(2));
      else if (checkPos(this.myHexBoard, getColumn(2), getRow(2), getColumn(1), getRow(2) - 2, true))
        changePos(getColumn(1), getRow(2) - 2);
      else {
        changePos(path.get(2), path.get(3));
      }
    }
    else if ((this.myCompLevel == 1) || (this.myWallCount[1] <= 0) || (path.size() == 4)) {
      changePos(path.get(2), path.get(3));
    }
    else if (this.myCompLevel == 2) {
      if (this.myWallCount[(player - 1)] > this.myWallCount[0]) {
        if (checkWall(8, getColumn(1), getRow(1) + 1, true))
          addWall(8, getColumn(1), getRow(1) + 1, false);
        else if (checkWall(8, getColumn(1) + 1, getRow(1) + 1, true))
          addWall(8, getColumn(1) + 1, getRow(1) + 1, false);
        else if (checkWall(8, getColumn(1) - 1, getRow(1) + 1, true))
          addWall(8, getColumn(1) - 1, getRow(1) + 1, false);
        else if (checkWall(8, getColumn(1) + 1, getRow(1) - 1, true))
          addWall(8, getColumn(1) + 1, getRow(1) - 1, false);
        else if (checkWall(8, getColumn(1) - 1, getRow(1) - 1, true))
          addWall(8, getColumn(1) - 1, getRow(1) - 1, false);
        else if (checkWall(8, getColumn(1), getRow(1) - 1, true))
          addWall(8, getColumn(1), getRow(1) - 1, false);
        else
          changePos(path.get(2), path.get(3));
      }
      else changePos(path.get(2), path.get(3));

    }
    else if (this.myCompLevel == 3) {
      if (this.myWallCount[(player - 1)] > this.myWallCount[0]) {
        int x = 0; int y = 0;
        ArrayList<Integer> path1 = shortPath(1);
        ArrayList<Integer> path2 = shortPath(player);
        double ratio = path1.size() / path2.size();
        for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
          for (int j = 2; j <= 2 * this.myBoardSize - 1; j += 2) {
            if (checkWall(8, i, j, true)) {
              addWall(8, i, j, true);
              path1 = shortPath(1);
              path2 = shortPath(player);
              if (path1.size() / path2.size() > ratio) {
                ratio = path1.size() / path2.size();
                x = i;
                y = j;
              }
              this.myHexBoard[i][j] = 0;
            }
          }
        }

        if ((x != 0) && (y != 0))
          addWall(8, x, y, false);
        else
          changePos(path.get(2), path.get(3));
      } else {
        changePos(path.get(2), path.get(3));
      }
    } else if (this.myCompLevel == 4) {
      double[] move = compmove(new HexQuoridorModel(this), 2);
      if (!isEven((int)move[1]))
        changePos((int)move[0], (int)move[1]);
      else
        addWall(8, (int)move[0], (int)move[1], false);
    } else if (this.myCompLevel == 5) {
      double[] move = compmove(new HexQuoridorModel(this), 3);
      if (!isEven((int)move[1]))
        changePos((int)move[0], (int)move[1]);
      else
        addWall(8, (int)move[0], (int)move[1], false);
    }
  }

  public double[] compmove(HexQuoridorModel model, int iteration) {
    ArrayList<Integer> path1 = model.shortPath(1);
    ArrayList<Integer> path2 = model.shortPath(2);

    if ((path1.size() == 4) || (path2.size() == 4) || (iteration == 0)) {
      return new double[] { path2.get(2), path2.get(3), 
        evaluate(path1.size() / 2, path2.size() / 2, model.getWallCount(1), model.getWallCount(2)) };
    }
    int x = 0; int y = 0;
    if (model.getPlayer() == 2) {
      double value = 0.0D; double maxValue = -100.0D;
      for (int i = 2 * this.myBoardSize - 2; i >= 2; i--) {
        for (int j = 2 * this.myBoardSize - 2; j >= 2; j -= 2) {
          if ((model.checkWall(8, i, j, true)) && (model.getWallCount(2) > 0))
          {
            HexQuoridorModel m1 = new HexQuoridorModel(model);
            m1.addWall(8, i, j, false);
            value = compmove(m1, iteration - 1)[2];

            for (int k = 0; k < path1.size() - 3; k += 2) {
              if ((((path1.get(k) + path1.get(k + 2)) / 2 == i) && (
                (path1.get(k + 1) + 1 == j) || (path1.get(k + 1) - 1 == j))) || (
                ((path1.get(k) == i) || (path1.get(k + 2) == i)) && 
                ((path1.get(k + 1) + path1.get(k + 3)) / 2 == j)))
                value += 0.0001D;
            }
            value -= (Math.abs(model.getColumn(1) - i) + Math.abs(model.getColumn(2) - j)) / 10000.0D;
            if (value > maxValue) {
              x = i;
              y = j;
              maxValue = value;
            }
          }
        }
      }

      int i = path2.get(2); int j = path2.get(3);
      HexQuoridorModel m1 = new HexQuoridorModel(model);
      m1.changePos(i, j);
      value = compmove(m1, iteration - 1)[2];
      if (value >= maxValue) {
        x = i;
        y = j;
        maxValue = value;
      }
      return new double[] { x, y, maxValue };
    }
    double value = 0.0D; double minValue = 100.0D;
    for (int i = 2 * this.myBoardSize - 2; i >= 2; i--) {
      for (int j = 2 * this.myBoardSize - 2; j >= 2; j -= 2) {
        if ((model.checkWall(8, i, j, true)) && (model.getWallCount(1) > 0))
        {
          HexQuoridorModel m1 = new HexQuoridorModel(model);
          m1.addWall(8, i, j, false);
          value = compmove(m1, iteration - 1)[2];

          for (int k = 0; k < path2.size() - 3; k += 2) {
            if ((((path2.get(k) + path2.get(k + 2)) / 2 == i) && (
              (path2.get(k + 1) + 1 == j) || (path2.get(k + 1) - 1 == j))) || (
              ((path2.get(k) == i) || (path2.get(k + 2) == i)) && 
              ((path2.get(k + 1) + path2.get(k + 3)) / 2 == j)))
              value -= 0.0001D;
          }
          value += (Math.abs(model.getColumn(2) - i) + Math.abs(model.getRow(2) - j)) / 10000.0D;
          if (value < minValue) {
            x = i;
            y = j;
            minValue = value;
          }
        }
      }
    }
    int i = path1.get(2); int j = path1.get(3);
    HexQuoridorModel m1 = new HexQuoridorModel(model);
    m1.changePos(i, j);
    value = compmove(m1, iteration - 1)[2];
    if (value < minValue) {
      x = i;
      y = j;
      minValue = value;
    }
    return new double[] { x, y, minValue };
  }

  public double evaluate(double dist1, double dist2, double wallNum1, double wallNum2)
  {
    return dist1 - dist2 + 0.5D * (wallNum2 - wallNum1);
  }

  public boolean gameOver(int piece, int x, int y)
  {
    if ((piece == 1) && (((this.myPlayers != 4) && (y == 2 * this.myBoardSize - 1)) || ((this.myPlayers == 4) && (y - 2 * x == this.myBoardSize - 2))))
      return true;
    if ((piece == 2) && (
      ((this.myPlayers != 2) && (this.myPlayers != 6)) || ((y == 1) || ((this.myPlayers == 3) && (2 * x + y == this.myBoardSize + 2)) || ((this.myPlayers == 4) && 
      (2 * x - y == this.myBoardSize * 3 - 2)))))
      return true;
    if ((piece == 3) && (
      ((this.myPlayers == 3) && (2 * x - y == this.myBoardSize * 3 - 2)) || ((this.myPlayers == 4) && (2 * x + y == this.myBoardSize + 2)) || ((this.myPlayers == 6) && 
      (y - 2 * x == this.myBoardSize - 2))))
      return true;
    if ((piece == 4) && (
      ((this.myPlayers == 4) && (2 * x + y == 5 * this.myBoardSize - 2)) || ((this.myPlayers == 6) && (2 * x - y == this.myBoardSize * 3 - 2))))
      return true;
    if ((piece == 5) && (2 * x + y == this.myBoardSize + 2))
      return true;
    if ((piece == 6) && (2 * x + y == 5 * this.myBoardSize - 2)) {
      return true;
    }
    return false;
  }

  public boolean isOnBoard(int x, int y)
  {
    if ((y <= 2 * this.myBoardSize - 1) && (y - 2 * x <= this.myBoardSize - 1) && (y >= 1) && (2 * x - y <= this.myBoardSize * 3 - 1) && 
      (2 * x + y >= this.myBoardSize + 1) && (2 * x + y <= 5 * this.myBoardSize - 1)) {
      return true;
    }
    return false;
  }

  public int[][] deepCopy(int[][] board)
  {
    int[][] newBoard = new int[2 * this.myBoardSize + 1][2 * this.myBoardSize + 1];
    for (int i = 0; i <= 2 * this.myBoardSize; i++) {
      for (int j = 0; j <= 2 * this.myBoardSize; j++) {
        newBoard[i][j] = board[i][j];
      }
    }
    return newBoard;
  }

  public int getValue(int x, int y)
  {
    return this.myHexBoard[x][y];
  }

  public int getPlayer()
  {
    if (this.myTurn % this.myPlayers == 0) {
      return this.myPlayers;
    }
    return this.myTurn % this.myPlayers;
  }

  public int getTurn() {
    return this.myTurn;
  }

  public int getPlayers()
  {
    return this.myPlayers;
  }

  public boolean isEven(int number)
  {
    return number % 2 == 0;
  }

  public int getWallCount(int player)
  {
    return this.myWallCount[(player - 1)];
  }

  public int getBoardSize()
  {
    return this.myBoardSize;
  }

  public int[][] getBoard() {
    return this.myHexBoard;
  }

  public int getCompLevel() {
    return this.myCompLevel;
  }

  public int getRow(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= 2 * this.myBoardSize - 1; j++) {
        if (this.myHexBoard[i][j] == piece)
          return j;
      }
    }
    throw new IllegalArgumentException("piece doesn't exist");
  }

  public int getColumn(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= 2 * this.myBoardSize - 1; j++) {
        if (this.myHexBoard[i][j] == piece)
          return i;
      }
    }
    throw new IllegalArgumentException("piece doesn't exist");
  }
}
