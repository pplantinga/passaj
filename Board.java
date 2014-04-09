package passaj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class Board extends JPanel
{
  private static final long serialVersionUID = 2718281828459045235L;
  public static final int DEFAULT_WIDTH = 700;
  public static final int DEFAULT_WALL_WIDTH = 15;
  public static final int DEFAULT_BOARDSIZE = 9;
  public static final String DEFAULT_PIECE1_COLOR = "gold";
  public static final String DEFAULT_TEMPPIECE_COLOR = "light grey";
  public static final String DEFAULT_NAME1 = "Peter Plantinga";
  public static final String DEFAULT_NAME2 = "THE DOMINATOR";
  public static final Color DEFAULT_WALL_COLOR = stringToColor("dark brown"); public static final Color DEFAULT_BACKGROUND_COLOR = stringToColor("grey");
  public static final Color DEFAULT_BLOCK_COLOR = stringToColor("black"); public static final Color DEFAULT_LASTROW_COLOR = stringToColor("dark grey");
  public static final Color DEFAULT_TEMPWALL_COLOR = stringToColor("brown");
  private int[][] myBoard;
  private int[][] testBoard;
  private int[] myWallCount;
  private int myWidth;
  private int myBlockWidth;
  private int myWallWidth;
  private int myPieceDiameter;
  private int myTurn;
  private int myBoardSize;
  private int myPlayers;
  private int myCompLevel;
  private int myWallNumber;
  private String[] myNames;
  private String[] myPieceColors;
  private Color myWallColor;
  private Color myTempWallColor;
  private String wallNumber;
  private IllegalArgumentException wallException;
  private IllegalArgumentException wallFilled;
  private IllegalArgumentException blockException;
  private boolean compPlay;

  public Board()
  {
    this.myBoardSize = 9;
    this.myNames = new String[] { "Peter Plantinga", "THE DOMINATOR" };
    String piece2color = randomColor();
    while (stringToColor(piece2color).equals(stringToColor("gold")))
      piece2color = randomColor();
    this.myPieceColors = new String[] { "gold", piece2color, "light grey" };
    this.myPlayers = 2;
    this.compPlay = true;
    this.myCompLevel = 1;

    this.wallNumber = "default";
    this.myWallNumber = ((int)Math.round(Math.pow(this.myBoardSize - 1, 2.0D) * 5.0D / 32.0D));
    this.myWallCount = new int[] { this.myWallNumber, this.myWallNumber };

    setBackground(DEFAULT_BACKGROUND_COLOR);
    initialize();
  }

  public Board(Board board) {
    this.myBoardSize = board.getBoardSize();
    this.myNames = board.getNames();
    this.myPieceColors = board.getColors();
    this.myPlayers = board.getPlayers();
    this.compPlay = (this.myNames[1] == "THE DOMINATOR");
    this.myCompLevel = board.getCompLevel();
    this.wallNumber = board.getWallNumber();

    this.myWallCount = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCount[i] = board.getWallCount(i + 1);
    }

    setBackground(DEFAULT_BACKGROUND_COLOR);
    initialize();
    this.myBoard = deepCopy(board.getBoard());
    this.myTurn = board.getTurn();
  }

  public Board(String[] names, String[] colors, String background, String wallNum, int size, int compLevel)
  {
    this.myBoardSize = size;
    if (names.length == 1) {
      this.myPlayers = 2;
      this.compPlay = true;
      this.myCompLevel = compLevel;
      this.myNames = new String[] { names[0], "THE DOMINATOR" };
      String piece2color = randomColor();
      while (stringToColor(piece2color).equals(stringToColor(colors[0])))
        piece2color = randomColor();
      this.myPieceColors = new String[] { colors[0], piece2color, "light grey" };
    } else {
      if (names.length >= 3)
        this.myPlayers = 4;
      else
        this.myPlayers = 2;
      this.myNames = names;
      this.myPieceColors = new String[this.myPlayers + 1];
      for (int i = 0; i < this.myPlayers; i++)
        this.myPieceColors[i] = colors[i];
      this.myPieceColors[this.myPlayers] = "light grey";
      this.compPlay = false;
    }

    this.wallNumber = wallNum;
    if (wallNum.equalsIgnoreCase("default"))
      this.myWallNumber = ((int)Math.round(Math.pow(this.myBoardSize - 1, 2.0D) * 5.0D / 16.0D / this.myPlayers));
    this.myWallCount = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCount[i] = this.myWallNumber;
    }
    setBackground(stringToColor(background));
    initialize();
  }

  public void initialize()
  {
    this.myWidth = 700;
    this.myBlockWidth = (565 / this.myBoardSize);
    this.myWallWidth = (135 / this.myBoardSize);
    this.myPieceDiameter = (this.myBlockWidth - this.myWallWidth);
    this.myWallColor = DEFAULT_WALL_COLOR;
    this.myTempWallColor = DEFAULT_TEMPWALL_COLOR;
    this.myTurn = 1;

    this.wallException = new IllegalArgumentException("There is a wall between where you are and where you want to be.");
    this.wallFilled = new IllegalArgumentException("There is a wall already there!");
    this.blockException = new IllegalArgumentException("You may not block anybody from getting to their goal row");

    this.myBoard = new int[2 * this.myBoardSize + 1][2 * this.myBoardSize + 1];
    if (!isEven(this.myBoardSize)) {
      this.myBoard[this.myBoardSize][1] = 1;
      this.myBoard[this.myBoardSize][(2 * this.myBoardSize - 1)] = 2;
      if (this.myPlayers == 4) {
        this.myBoard[(2 * this.myBoardSize - 1)][this.myBoardSize] = 3;
        this.myBoard[1][this.myBoardSize] = 4;
      }
    } else {
      this.myBoard[(this.myBoardSize + 1)][1] = 1;
      this.myBoard[(this.myBoardSize - 1)][(2 * this.myBoardSize - 1)] = 2;
      if (this.myPlayers == 4) {
        this.myBoard[(2 * this.myBoardSize - 1)][(this.myBoardSize + 1)] = 3;
        this.myBoard[1][(this.myBoardSize - 1)] = 4;
      }

    }

    for (int i = 0; i <= 2 * this.myBoardSize; i++) {
      for (int j = 0; j <= 2 * this.myBoardSize; j++) {
        if ((i == 0) || (i == 2 * this.myBoardSize) || (j == 0) || (j == 2 * this.myBoardSize)) {
          this.myBoard[i][j] = 6;
        }
      }
    }
    setPreferredSize(new Dimension(this.myWidth, this.myWidth));
  }

  public void paintComponent(Graphics pen)
  {
    super.paintComponent(pen);
    for (int i = 0; i <= this.myBoardSize - 1; i++) {
      for (int j = 0; j <= this.myBoardSize - 1; j++) {
        pen.setColor(Color.lightGray);
        paintBlock(pen, this.myWidth * i / this.myBoardSize + this.myWallWidth / 2 - 1, 
          this.myWidth * j / this.myBoardSize + this.myWallWidth / 2 - 1, this.myBlockWidth + 2);
        if ((j == 0) || (j == this.myBoardSize - 1) || ((this.myPlayers == 4) && ((i == 0) || (i == this.myBoardSize - 1))))
          pen.setColor(DEFAULT_LASTROW_COLOR);
        else
          pen.setColor(DEFAULT_BLOCK_COLOR);
        paintBlock(pen, this.myWidth * i / this.myBoardSize + this.myWallWidth / 2, 
          this.myWidth * j / this.myBoardSize + this.myWallWidth / 2, this.myBlockWidth);
      }
    }
    paintPieces(pen);
    paintWalls(pen);
  }

  public void paintBlock(Graphics pen, int xPos, int yPos, int width)
  {
    pen.fillRoundRect(xPos, yPos, width, width, this.myWallWidth, this.myWallWidth);
  }

  public void paintPieces(Graphics pen)
  {
    for (int piece = 1; piece <= this.myPlayers + 1; piece++)
      if (exist(piece)) {
        pen.setColor(Color.white);
        pen
          .fillOval(
          this.myWidth * (getColumn(piece) / 2) / this.myBoardSize + this.myWallWidth / 2 + this.myBlockWidth / 2 - this.myPieceDiameter / 2 - 1, 
          this.myWidth * (getRow(piece) / 2) / this.myBoardSize + this.myWallWidth / 2 + this.myBlockWidth / 2 - this.myPieceDiameter / 2 - 1, 
          this.myPieceDiameter + 2, this.myPieceDiameter + 2);
        pen.setColor(stringToColor(this.myPieceColors[(piece - 1)]));
        pen
          .fillOval(
          this.myWidth * (getColumn(piece) / 2) / this.myBoardSize + this.myWallWidth / 2 + this.myBlockWidth / 2 - this.myPieceDiameter / 2, 
          this.myWidth * (getRow(piece) / 2) / this.myBoardSize + this.myWallWidth / 2 + this.myBlockWidth / 2 - this.myPieceDiameter / 2, 
          this.myPieceDiameter, this.myPieceDiameter);
      }
  }

  public void paintWalls(Graphics pen)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++)
      for (int j = 1; j <= 2 * this.myBoardSize - 1; j++)
        if ((this.myBoard[i][j] == 6) || (this.myBoard[i][j] == 7) || (this.myBoard[i][j] == 8)) {
          if (this.myBoard[i][j] == 6)
            pen.setColor(this.myWallColor);
          if (this.myBoard[i][j] == 7)
            pen.setColor(this.myTempWallColor);
          if (this.myBoard[i][j] == 8)
            pen.setColor(Color.red);
          if ((isEven(i)) && (!isEven(j)))
            pen.fillRect(this.myWidth * (i / 2) / this.myBoardSize - this.myWallWidth / 2, this.myWidth * ((j - 1) / 2) / this.myBoardSize + 
              this.myWallWidth / 2 - 2, this.myWallWidth - 2, this.myBlockWidth + 4);
          if ((!isEven(i)) && (isEven(j)))
            pen.fillRect(this.myWidth * ((i - 1) / 2) / this.myBoardSize + this.myWallWidth / 2 - 2, this.myWidth * (j / 2) / 
              this.myBoardSize - this.myWallWidth / 2, this.myBlockWidth + 4, this.myWallWidth - 2);
          if ((isEven(i)) && (isEven(j)))
            pen.fillRect(this.myWidth * (i / 2) / this.myBoardSize - this.myWallWidth / 2, this.myWidth * (j / 2) / this.myBoardSize - 
              this.myWallWidth / 2, this.myWallWidth - 2, this.myWallWidth - 2);
        }
  }

  public int posToElement(int pos)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      if ((pos > this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (pos <= this.myWidth * i / this.myBoardSize - this.myWallWidth / 2))
        return i * 2 - 1;
      if ((pos > this.myWidth * (i - 1) / this.myBoardSize - this.myWallWidth / 2) && 
        (pos <= this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (i != 0))
        return i * 2 - 2;
    }
    throw new IllegalArgumentException("Click somewhere ON the board.");
  }

  public void changePos(int x, int y)
  {
    int piece = getPlayer();

    int k = x;
    int l = y;
    int i = getColumn(piece);
    int j = getRow(piece);

    if (checkPos(this.myBoard, i, j, k, l, false)) {
      this.myBoard[i][j] = 0;
      this.myBoard[k][l] = piece;
    }
    repaint();
    if (getRow(1) == 2 * this.myBoardSize - 1)
      throw new IllegalArgumentException(this.myNames[0] + " WINS!!");
    if (getRow(2) == 1)
      throw new IllegalArgumentException(this.myNames[1] + " WINS!!");
    if (this.myPlayers == 4) {
      if (getColumn(3) == 1)
        throw new IllegalArgumentException(this.myNames[2] + " WINS!!");
      if (getColumn(4) == 2 * this.myBoardSize - 1)
        throw new IllegalArgumentException(this.myNames[3] + " WINS!!");
    }
    this.myTurn += 1;

    for (int c = 1; c <= 4; c++)
      if ((c <= 2) || ((c > 2) && (this.myPlayers == 4))) {
        int a = getColumn(c);
        int b = getRow(c);
        this.testBoard = deepCopy(this.myBoard);
        if ((!find(this.testBoard, c, a, b)) && (this.myWallCount[(c - 1)] <= 0)) {
          this.myTurn += 1;
          throw new IllegalArgumentException("Player " + c + " was skipped because they couldn't move");
        }
      }
  }

  public void addWall(int boardNumber, int x, int y, boolean half)
  {
    int playerToAdd = getPlayer() - 1;

    if (this.myWallCount[playerToAdd] <= 0) {
      throw new IllegalArgumentException("You are all out of walls");
    }

    if (((isEven(x)) && (isEven(y))) || ((!isEven(x)) && (!isEven(y)))) {
      throw new IllegalArgumentException("You can't put a wall there");
    }

    if (isVertical(x, y)) {
      int scaleFactor = checkWall(x, y, half, false);
      this.myBoard[x][y] = boardNumber;
      this.myBoard[x][(y + scaleFactor)] = boardNumber;
      this.myBoard[x][(y + scaleFactor * 2)] = boardNumber;
    }

    if (isHorizontal(x, y)) {
      int scaleFactor = checkWall(x, y, half, false);
      this.myBoard[x][y] = boardNumber;
      this.myBoard[(x + scaleFactor)][y] = boardNumber;
      this.myBoard[(x + scaleFactor * 2)][y] = boardNumber;
    }
    if (boardNumber == 6) {
      this.myWallCount[playerToAdd] -= 1;
      this.myTurn += 1;
    }
    repaint();
  }

  public int checkWall(int x, int y, boolean half, boolean search)
  {
    int xfac = 1; int yfac = 0;
    if (isVertical(x, y)) {
      xfac = 0;
      yfac = 1;
    }
    int scalefactor;
    if (half)
      scalefactor = 1;
    else {
      scalefactor = -1;
    }
    if (this.myBoard[x][y] == 6) {
      if (search) {
        return 0;
      }
      throw this.wallFilled;
    }if ((x == 1) && (this.myBoard[x][y] != 6) && (this.myBoard[(x + xfac)][(y + yfac)] != 6) && (this.myBoard[(x + 2 * xfac)][(y + 2 * yfac)] != 6))
      scalefactor = 1;
    else if ((x == 2 * this.myBoardSize - 1) && (this.myBoard[x][y] != 6) && (this.myBoard[(x - xfac)][(y - yfac)] != 6) && (this.myBoard[(x - 2 * xfac)][(y - 2 * yfac)] != 6))
      scalefactor = -1;
    if ((this.myBoard[x][y] == 6) || (
      ((this.myBoard[(x - xfac)][(y - yfac)] == 6) || (this.myBoard[(x - 2 * xfac)][(y - 2 * yfac)] == 6)) && (
      (this.myBoard[(x + xfac)][(y + yfac)] == 6) || (this.myBoard[(x + 2 * xfac)][(y + 2 * yfac)] == 6)))) {
      if (search) {
        return 0;
      }
      throw this.wallFilled;
    }if (half) {
      if ((this.myBoard[(x - xfac)][(y - yfac)] != 6) && (this.myBoard[(x - 2 * xfac)][(y - 2 * yfac)] != 6))
        scalefactor = -1;
      else
        scalefactor = 1;
    } else if ((this.myBoard[(x + xfac)][(y + yfac)] != 6) && (this.myBoard[(x + 2 * xfac)][(y + 2 * yfac)] != 6))
      scalefactor = 1;
    else
      scalefactor = -1;
    for (int i = 1; i <= this.myPlayers; i++) {
      int a = getColumn(i);
      int b = getRow(i);
      this.testBoard = deepCopy(this.myBoard);
      this.testBoard[x][y] = 6;
      this.testBoard[(x + xfac * scalefactor)][(y + yfac * scalefactor)] = 6;
      this.testBoard[(x + xfac * scalefactor * 2)][(y + yfac * scalefactor * 2)] = 6;
      if (!find(this.testBoard, i, a, b)) {
        if (search) {
          return 0;
        }
        this.myBoard[x][y] = 8;
        this.myBoard[(x + xfac * scalefactor)][(y + yfac * scalefactor)] = 8;
        this.myBoard[(x + xfac * scalefactor * 2)][(y + yfac * scalefactor * 2)] = 8;
        throw this.blockException;
      }
    }

    return scalefactor;
  }

  public boolean checkPos(int[][] board, int xold, int yold, int xnew, int ynew, boolean search)
  {
    int i = xold;
    int j = yold;
    int k = xnew;
    int l = ynew;

    if ((k > 2 * this.myBoardSize - 1) || (k < 1) || (l > 2 * this.myBoardSize - 1) || (l < 1)) {
      if (search) {
        return false;
      }
      throw new IllegalArgumentException("click on the board");
    }
    if ((board[k][l] != 0) && (board[k][l] != this.myPlayers + 1)) {
      if (search) {
        return false;
      }
      throw new IllegalArgumentException("There is already a piece where you are attemting to move.");
    }

    if (((i + 2 != k) && (i - 2 != k)) || ((j == l) || (((j + 2 == l) || (j - 2 == l)) && (i == k)))) {
      if (((j == l) && (board[((i + k) / 2)][j] != 6)) || ((i == k) && (board[i][((j + l) / 2)] != 6)))
        return true;
      if (search) {
        return false;
      }
      throw this.wallException;
    }

    if (((i + 4 != k) && (i - 4 != k)) || (((j == l) && (board[((i + k) / 2)][l] != 0)) || (((j + 4 == l) || (j - 4 == l)) && (i == k) && 
      (board[k][((j + l) / 2)] != 0)))) {
      if (((j == l) && (board[((i + k) / 2 + 1)][j] != 6) && (board[((i + k) / 2 - 1)][j] != 6)) || ((i == k) && 
        (board[i][((j + l) / 2 + 1)] != 6) && (board[i][((j + l) / 2 - 1)] != 6)))
        return true;
      if (search) {
        return false;
      }
      throw this.wallException;
    }

    if (((i + 2 == k) || (i - 2 == k)) && 
      ((j + 2 == l) || (j - 2 == l)) && (
      ((board[k][j] != 0) && ((board[(i + 3 * (k - i) / 2)][j] == 6) || (board[(2 * k - i)][j] != 0))) || ((board[i][l] != 0) && (
      (board[i][(j + 3 * (l - j) / 2)] == 6) || (board[i][(2 * l - j)] != 0))))) {
      if (((board[k][j] != 0) && (board[((i + k) / 2)][j] != 6) && (board[k][((j + l) / 2)] != 6)) || ((board[i][l] != 0) && 
        (board[i][((j + l) / 2)] != 6) && (board[((i + k) / 2)][l] != 6)))
        return true;
      if (search) {
        return false;
      }
      throw this.wallException;
    }
    if (search) {
      return false;
    }
    throw new IllegalArgumentException("The place you are trying to go is not next to your piece.");
  }

  public boolean checkMove(int[][] board, int xold, int yold, int xnew, int ynew)
  {
    if (((xold + 2 != xnew) && (xold - 2 != xnew)) || (((yold == ynew) || (((yold + 2 == ynew) || (yold - 2 == ynew)) && (xold == xnew))) && (
      ((yold == ynew) && (board[((xold + xnew) / 2)][yold] != 6)) || ((xold == xnew) && (board[xold][((yold + ynew) / 2)] != 6)))))
      return true;
    return false;
  }

  public boolean find(int[][] board, int piece, int x, int y)
  {
    if (((piece == 1) && (y == 2 * this.myBoardSize - 1)) || ((piece == 2) && (y == 1)) || ((this.myPlayers == 4) && (piece == 3) && (x == 1)) || (
      (this.myPlayers == 4) && (piece == 4) && (x == 2 * this.myBoardSize - 1)))
      return true;
    if ((board[x][y] == 10) || (board[x][y] == 11) || (board[x][y] == 12) || (board[x][y] == 13) || (board[x][y] == 14))
      return false;
    board[x][y] += 10;
    if ((y - 1 != 0) && 
      (checkMove(board, x, y, x, y - 2)) && 
      (find(board, piece, x, y - 2)))
      return true;
    if ((x - 1 != 0) && 
      (checkMove(board, x, y, x - 2, y)) && 
      (find(board, piece, x - 2, y)))
      return true;
    if ((y + 1 != 2 * this.myBoardSize) && 
      (checkMove(board, x, y, x, y + 2)) && 
      (find(board, piece, x, y + 2)))
      return true;
    if ((x + 1 != 2 * this.myBoardSize) && 
      (checkMove(board, x, y, x + 2, y)) && 
      (find(board, piece, x + 2, y)))
      return true;
    return false;
  }

  public ArrayList<Integer> shorterPath(int piece)
  {
    ArrayList<Integer> path = new ArrayList<Integer>();
    ArrayList<Integer> queue = new ArrayList<Integer>();
    boolean finished = false;
    this.testBoard = deepCopy(this.myBoard);
    queue.add(getColumn(piece));
    queue.add(getRow(piece));
    int x = queue.get(0);
		int y = queue.get(1);
    this.testBoard[x][y] = (100 * x + y);
    for (int i = 0; queue.size() != i; i += 2)
    {
      x = queue.get(i);
      y = queue.get(i + 1);
      for (int j = x - 4; j <= x + 4; j += 2) {
        for (int k = y - 4; k <= y + 4; k += 2) {
          if ((checkPos(this.myBoard, x, y, j, k, true)) && 
            (this.testBoard[j][k] < 10)) {
            queue.add(Integer.valueOf(j));
            queue.add(Integer.valueOf(k));
            this.testBoard[j][k] = (100 * x + y);
            if (((piece == 3) && (j == 1)) || ((piece == 4) && (j == 2 * this.myBoardSize - 1)) || ((piece == 1) && (k == 2 * this.myBoardSize - 1)) || (
              (piece == 2) && (k == 1))) {
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
        if (((path.get(0)) == i) && ((path.get(1)) == j)) {
          path.add(0, Integer.valueOf(this.testBoard[i][j] / 100));
          path.add(1, Integer.valueOf(this.testBoard[i][j] - (path.get(0)) * 100));
        }
      i += 2;
    }

    if ((path.get(0) == path.get(2)) && (path.get(1) == path.get(3))) {
      path.remove(3);
      path.remove(2);
    }
    return path;
  }

  public void moveComp(int player)
  {
    repaint();
    ArrayList<Integer> path = shorterPath(player);
    if ((path.size() == 4) || (this.myCompLevel == 1) || (this.myWallCount[1] <= 0) || (this.myWallCount[0] == this.myWallNumber)) {
      changePos((path.get(2)), (path.get(3)));
    }
    else if (this.myCompLevel == 2) {
      if (this.myWallCount[(player - 1)] > this.myWallCount[0]) {
        if (checkWall(getColumn(1), getRow(1) + 1, true, true) == -1)
          addWall(6, getColumn(1), getRow(1) + 1, true);
        else if (checkWall(getColumn(1), getRow(1) + 1, false, true) == 1)
          addWall(6, getColumn(1), getRow(1) + 1, false);
        else if (checkWall(getColumn(1) - 1, getRow(1), false, true) == 1)
          addWall(6, getColumn(1) - 1, getRow(1), false);
        else if (checkWall(getColumn(1) + 1, getRow(1), false, true) == 1)
          addWall(6, getColumn(1) + 1, getRow(1), false);
        else if (checkWall(getColumn(1) - 1, getRow(1), true, true) == -1)
          addWall(6, getColumn(1) - 1, getRow(1), true);
        else if (checkWall(getColumn(1) + 1, getRow(1), true, true) == -1)
          addWall(6, getColumn(1) + 1, getRow(1), true);
        else
          changePos(path.get(2), path.get(3));
      }
      else changePos(path.get(2), path.get(3));

    }
    else if (this.myCompLevel == 3) {
      ArrayList<Integer> path1 = shorterPath(1);
      ArrayList<Integer> path2 = shorterPath(player);

      if (path2.size() > path1.size() && (this.myWallCount[(player - 1)] > 0)) {
        int x = 0; int y = 0;

        double ratio = path1.size() / path2.size();
        for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
          for (int j = 2 * this.myBoardSize - 1; j >= 1; j--) {
            if (((isHorizontal(i, j)) && (checkWall(i, j, true, true) == -1)) || ((isVertical(i, j)) && 
              (checkWall(i, j, true, true) == -1))) {
              Board b1 = new Board(this);
              b1.addWall(6, i, j, true);
              path1 = b1.shorterPath(1);
              path2 = b1.shorterPath(player);
              if (path1.size() / path2.size() > ratio) {
                ratio = path1.size() / path2.size();
                x = i;
                y = j;
              }
            }
          }
        }

        if ((x != 0) && (y != 0))
          addWall(6, x, y, true);
        else
          changePos(path.get(2), path.get(3));
      } else {
        changePos(path.get(2), path.get(3));
      }

    }
    else if (this.myCompLevel == 4) {
      double[] move = compmove(new Board(this), 2);
      if ((!isEven((int)move[0])) && (!isEven((int)move[1])))
        changePos((int)move[0], (int)move[1]);
      else {
        addWall(6, (int)move[0], (int)move[1], true);
      }
    }
    else if (this.myCompLevel == 5) {
      double[] move = compmove(new Board(this), 3);
      if ((!isEven((int)move[0])) && (!isEven((int)move[1])))
        changePos((int)move[0], (int)move[1]);
      else
        addWall(6, (int)move[0], (int)move[1], true);
    }
  }

  public double[] compmove(Board board, int iteration) {
    ArrayList<Integer> path1 = board.shorterPath(1);
    ArrayList<Integer> path2 = board.shorterPath(2);

    if (path1.size() == 4 || path2.size() == 4 || iteration == 0) {
      return new double[] { path2.get(2), path2.get(3), 
        evaluate(path1.size() / 2, path2.size() / 2, board.getWallCount(1), board.getWallCount(2)) };
    }
    int x = 0; int y = 0;
    if (board.getPlayer() == 2) {
      double value = 0.0D; double maxValue = -100.0D;
      for (int i = 2 * this.myBoardSize - 1; i >= 2; i--) {
        for (int j = 2; j <= 2 * this.myBoardSize - 1; j++) {
          if (((isHorizontal(i, j)) && (board.checkWall(i, j, true, true) == -1)) || (
            (isVertical(i, j)) && 
            (board.checkWall(i, j, true, true) == -1) && 
            (board.getWallCount(2) > 0))) {
            Board b1 = new Board(board);
            b1.addWall(6, i, j, true);
            value = compmove(b1, iteration - 1)[2];

            for (int k = 0; k < path1.size() - 3; k += 2) {
              if (((path1.get(k) + path1.get(k + 2)) / 2 == i) && ((path1.get(k + 1) + path1.get(k + 3)) / 2 == j))
                value += 0.0001D;
            }
            value -= (Math.abs(path1.get(0) - i) + Math.abs(path1.get(1) - j)) / 10000.0D;

            if (value > maxValue) {
              x = i;
              y = j;
              maxValue = value;
            }
          }
        }
      }
      int i = path2.get(2);
			int j = path2.get(3);
      Board b1 = new Board(board);
      b1.changePos(i, j);
      value = compmove(b1, iteration - 1)[2];
      if (value > maxValue) {
        x = i;
        y = j;
        maxValue = value;
      }
      return new double[] { x, y, maxValue };
    }
    double value = 0.0D; double minValue = 100.0D;
    for (int i = 2 * this.myBoardSize - 1; i >= 2; i--) {
      for (int j = 2; j <= 2 * this.myBoardSize - 1; j++) {
        if (((isHorizontal(i, j)) && (board.checkWall(i, j, true, true) == -1)) || (
          (isVertical(i, j)) && 
          (board.checkWall(i, j, true, true) == -1) && 
          (board.getWallCount(1) > 0)))
        {
          Board b1 = new Board(board);
          b1.addWall(6, i, j, true);
          value = compmove(b1, iteration - 1)[2];

          for (int k = 0; k < path2.size() - 3; k += 2) {
            if ((((path2.get(k)) + (path2.get(k + 2))) / 2 == i) && (((path2.get(k + 1)) + (path2.get(k + 3))) / 2 == j))
              value -= 0.0001D;
          }
          value += (Math.abs(path2.get(0) - i) + Math.abs(path2.get(1) - j)) / 10000.0D;

          if (value < minValue) {
            x = i;
            y = j;
            minValue = value;
          }
        }
      }
    }
    int i = path1.get(2);
		int j = path1.get(3);
    Board b1 = new Board(board);
    b1.changePos(i, j);
    value = compmove(b1, iteration - 1)[2];
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

  public void tempPos(int x, int y)
  {
    int piece = getPlayer();
    int i = getColumn(piece);
    int j = getRow(piece);

    if (checkPos(this.myBoard, i, j, x, y, true)) {
      this.myBoard[x][y] = (this.myPlayers + 1);
    }
    repaint();
  }

  public void removeTemp()
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= 2 * this.myBoardSize - 1; j++) {
        if ((this.myBoard[i][j] == 7) || (this.myBoard[i][j] == 8) || (this.myBoard[i][j] == this.myPlayers + 1))
          this.myBoard[i][j] = 0;
      }
    }
    repaint();
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

  public boolean isTopLeftHalf(int pos)
  {
    if (posToElement(pos) == 1) {
      return false;
    }
    if (posToElement(pos) == 2 * this.myBoardSize - 1) {
      return true;
    }
    for (int i = 0; i <= 8; i++) {
      if ((pos > this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && 
        (pos <= this.myWidth * i / this.myBoardSize - this.myWallWidth / 2 - this.myBlockWidth / 2))
        return true;
    }
    return false;
  }

  public boolean isHorizontal(int x, int y)
  {
    return (!isEven(x)) && (isEven(y));
  }

  public boolean isVertical(int x, int y)
  {
    return (isEven(x)) && (!isEven(y));
  }

  public boolean isEven(int number)
  {
    return number % 2 == 0;
  }

  public boolean getPlay4()
  {
    return this.myPlayers == 4;
  }

  public int getPlayer()
  {
    if (this.myTurn % this.myPlayers == 0) {
      return this.myPlayers;
    }
    return this.myTurn % this.myPlayers;
  }

  public boolean getComp()
  {
    return this.compPlay;
  }

  public int getCompLevel() {
    return this.myCompLevel;
  }

  public int getPlayers()
  {
    return this.myPlayers;
  }

  public int getTurn()
  {
    return this.myTurn;
  }

  public int getValue(int x, int y)
  {
    return this.myBoard[x][y];
  }

  public String getName(int piece)
  {
    return this.myNames[(piece - 1)];
  }

  public String[] getNames()
  {
    return this.myNames;
  }

  public String[] getColors()
  {
    return this.myPieceColors;
  }

  public int[] getWallCounts()
  {
    return this.myWallCount;
  }

  public String getWallNumber() {
    return this.wallNumber;
  }

  public int getBoardSize()
  {
    return this.myBoardSize;
  }

  public int getWallCount(int player)
  {
    return this.myWallCount[(player - 1)];
  }

  public int[][] getBoard() {
    return this.myBoard;
  }

  public int getRow(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize; i += 2) {
      for (int j = 1; j <= 2 * this.myBoardSize; j += 2) {
        if (this.myBoard[i][j] == piece)
          return j;
      }
    }
    throw new IllegalArgumentException("piece doesn't exist");
  }

  public int getColumn(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize; i += 2) {
      for (int j = 1; j <= 2 * this.myBoardSize; j += 2) {
        if (this.myBoard[i][j] == piece)
          return i;
      }
    }
    throw new IllegalArgumentException("piece doesn't exist");
  }

  public boolean exist(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize; i += 2) {
      for (int j = 1; j <= 2 * this.myBoardSize; j += 2) {
        if (this.myBoard[i][j] == piece)
          return true;
      }
    }
    return false;
  }

  private static Color stringToColor(String color)
  {
    if (color.equalsIgnoreCase("red"))
      return Color.red;
    if (color.equalsIgnoreCase("green"))
      return Color.decode("#00aa00");
    if (color.equalsIgnoreCase("light green"))
      return Color.green;
    if (color.equalsIgnoreCase("dark green"))
      return Color.decode("#008040");
    if (color.equalsIgnoreCase("black"))
      return Color.black;
    if (color.equalsIgnoreCase("magenta"))
      return Color.magenta;
    if ((color.equalsIgnoreCase("dark gray")) || (color.equalsIgnoreCase("dark grey")) || (color.equalsIgnoreCase("darkgray")) || 
      (color.equalsIgnoreCase("darkgrey")))
      return Color.darkGray;
    if (color.equalsIgnoreCase("gray"))
      return Color.gray;
    if ((color.equalsIgnoreCase("yellow")) || (color.equalsIgnoreCase("gold")))
      return Color.decode("#ffa700");
    if (color.equalsIgnoreCase("sun"))
      return Color.yellow;
    if (color.equalsIgnoreCase("blue"))
      return Color.blue;
    if (color.equalsIgnoreCase("light blue"))
      return Color.decode("#4080ff");
    if (color.equalsIgnoreCase("cyan"))
      return Color.cyan;
    if ((color.equalsIgnoreCase("light gray")) || (color.equalsIgnoreCase("light grey")) || 
      (color.equalsIgnoreCase("lightgray")) || (color.equalsIgnoreCase("lightgrey")))
      return Color.lightGray;
    if (color.equalsIgnoreCase("pink"))
      return Color.decode("#ff3080");
    if (color.equalsIgnoreCase("orange"))
      return Color.decode("#ff6020");
    if ((color.equalsIgnoreCase("gray")) || (color.equalsIgnoreCase("grey")))
      return Color.gray;
    if ((color.equalsIgnoreCase("white")) || (color.equalsIgnoreCase("empty")) || (color.equalsIgnoreCase("brads mind")) || 
      (color.equalsIgnoreCase("surrender")))
      return Color.white;
    if (color.equalsIgnoreCase("purple"))
      return Color.decode("#750075");
    if (color.equalsIgnoreCase("dark brown"))
      return Color.decode("#402000");
    if (color.equalsIgnoreCase("brown"))
      return Color.decode("#876543");
    if (color.equalsIgnoreCase("dark blue"))
      return Color.decode("#202080");
    if ((color.equalsIgnoreCase("dark red")) || (color.equalsIgnoreCase("maroon")))
      return Color.decode("#600020");
    if (color.equalsIgnoreCase("clear")) {
      return Color.decode("#000000");
    }
    return Color.decode("#123321");
  }

  public static String randomColor() {
    int rand = (int)Math.ceil(Math.random() * 10.0D);
    switch (rand) {
    case 1:
      return "red";
    case 2:
      return "orange";
    case 3:
      return "light green";
    case 4:
      return "clear";
    case 5:
      return "dark green";
    case 6:
      return "yellow";
    case 7:
      return "pink";
    case 8:
      return "purple";
    case 9:
      return "light blue";
    case 10:
      return "dark blue";
    }
    return "white";
  }
}
