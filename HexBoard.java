package passaj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

public class HexBoard extends JPanel
{
  private static final long serialVersionUID = 1234567890987654321L;
  public static final int DEFAULT_WIDTH = 700;
  public static final int DEFAULT_WALL_WIDTH = 15;
  public static final int DEFAULT_BOARD_SIZE = 9;
  private int[][] myHexBoard;
  private int[][] testBoard;
  private int[] myWallCount;
  private String[] myNames;
  private String[] myPieceColors;
  private String wallNumber;
  private int myWidth;
  private int myWallWidth;
  private int myRadius;
  private int myTurn;
  private int myPlayers;
  private int myBoardSize;
  private int myWallNumber;
  private int myCompLevel = 1;
  private Color myWallColor = stringToColor("dark brown"); private Color myTempWallColor = stringToColor("brown");
  private IllegalArgumentException wallException;
  private IllegalArgumentException wallFilled;
  private IllegalArgumentException blockException;
  private boolean compPlay = false;

  public HexBoard()
  {
    this.myBoardSize = 9;
    this.myPlayers = 2;
    initialize();

    this.wallNumber = "default";
    this.myWallCount = new int[2];
    this.myWallCount[0] = ((this.myBoardSize - 1) * (this.myBoardSize + 2 * this.myBoardSize - 3) / 16);
    this.myWallCount[1] = ((this.myBoardSize - 1) * (this.myBoardSize + 2 * this.myBoardSize - 3) / 16);

    this.myHexBoard[this.myBoardSize][1] = 1;
    this.myHexBoard[this.myBoardSize][(2 * this.myBoardSize - 1)] = 2;

    setBackground(Color.gray);
  }

  public HexBoard(HexBoard board) {
    this.myBoardSize = board.getBoardSize();
    this.myPlayers = board.getPlayers();
    initialize();
    this.myNames = board.getNames();
    this.myPieceColors = board.getColors();
    this.compPlay = (this.myNames[1] == "THE DOMINATOR");
    this.myCompLevel = board.getCompLevel();
    this.wallNumber = board.getWallNumber();

    this.myWallCount = new int[this.myPlayers];
    for (int i = 0; i < this.myPlayers; i++) {
      this.myWallCount[i] = board.getWallCount(i + 1);
    }

    this.myTurn = board.getTurn();
    this.myHexBoard = deepCopy(board.getBoard());
  }

  public HexBoard(String[] names, String[] colors, String background, String wallNum, int size, int compLevel)
  {
    this.myBoardSize = size;
    this.myPlayers = names.length;
    this.wallNumber = wallNum;
    initialize();

    if (this.myPlayers == 1) {
      this.compPlay = true;
      this.myCompLevel = compLevel;
      this.myPlayers = 2;
      this.myNames = new String[] { names[0], "THE DOMINATOR" };
      String piece2color = randomColor();
      while (stringToColor(piece2color).equals(stringToColor(colors[0])))
        piece2color = randomColor();
      this.myPieceColors = new String[] { colors[0], piece2color, "light gray" };
      if (wallNum.equals("default"))
        this.myWallNumber = ((this.myBoardSize - 1) * (3 * this.myBoardSize - 3) / 8 / this.myPlayers);
      else
        this.myWallNumber = Integer.parseInt(wallNum);
      this.myWallCount = new int[] { this.myWallNumber, this.myWallNumber };
    } else {
      if (wallNum.equals("default"))
        this.myWallNumber = ((this.myBoardSize - 1) * (3 * this.myBoardSize - 3) / 8 / this.myPlayers);
      else
        this.myWallNumber = Integer.parseInt(wallNum);
      this.myWallCount = new int[this.myPlayers];
      this.myNames = new String[this.myPlayers];
      this.myPieceColors = new String[this.myPlayers + 1];
      for (int i = 0; i < this.myPlayers; i++) {
        this.myWallCount[i] = this.myWallNumber;
        this.myNames[i] = names[i];
        this.myPieceColors[i] = colors[i];
      }
      this.myPieceColors[this.myPlayers] = "light gray";
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

    setBackground(stringToColor(background));
  }

  private void initialize() {
    this.myWidth = 700;
    this.myWallWidth = (this.myWidth / (5 * this.myBoardSize));
    this.myRadius = ((int)(this.myWidth / (1.5D * this.myBoardSize) - this.myWallWidth));

    this.wallException = new IllegalArgumentException("There is a wall between where you are and where you want to be.");
    this.wallFilled = new IllegalArgumentException("There is a wall already there!");
    this.blockException = new IllegalArgumentException("You may not block anybody from getting to their goal row");

    this.myTurn = 1;

    this.myHexBoard = new int[2 * this.myBoardSize + 1][2 * this.myBoardSize + 1];
    setPreferredSize(new Dimension(this.myWidth, this.myWidth));
  }

  public void paintComponent(Graphics pen)
  {
    super.paintComponent(pen);
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= this.myBoardSize; j++) {
        pen.setColor(Color.lightGray);
        if (((!isEven(i)) || (isEven(j))) && ((isEven(i)) || (!isEven(j))) && 
          (i + j >= (this.myBoardSize + 1) / 2 + 1) && (j >= 1) && (j <= this.myBoardSize) && 
          (i - j <= 1.5D * (this.myBoardSize + 1) - 3.0D) && (i + j <= 2.5D * (this.myBoardSize + 1) - 3.0D) && (j - i <= (this.myBoardSize + 1) / 2 - 1))
          paintBlock(pen, this.myWidth * i / (2 * this.myBoardSize), 
            (int)(this.myWidth * j / this.myBoardSize * 
            Math.sin(1.047197551196598D)), this.myRadius + 2);
        if ((i + j == (this.myBoardSize + 1) / 2 + 1) || (j == 1) || (j == this.myBoardSize) || (i - j == 1.5D * (this.myBoardSize + 1) - 3.0D) || 
          (i + j == 2.5D * (this.myBoardSize + 1) - 3.0D) || (j - i == (this.myBoardSize + 1) / 2 - 1))
          pen.setColor(Color.darkGray);
        else
          pen.setColor(Color.black);
        if (((!isEven(i)) || (isEven(j))) && ((isEven(i)) || (!isEven(j))) && 
          (i + j >= (this.myBoardSize + 1) / 2 + 1) && (j >= 1) && (j <= this.myBoardSize) && 
          (i - j <= 1.5D * (this.myBoardSize + 1) - 3.0D) && (i + j <= 2.5D * (this.myBoardSize + 1) - 3.0D) && (j - i <= (this.myBoardSize + 1) / 2 - 1)) {
          paintBlock(pen, this.myWidth * i / (2 * this.myBoardSize), 
            (int)(this.myWidth * j / this.myBoardSize * 
            Math.sin(1.047197551196598D)), this.myRadius);
        }
      }
    }
    paintPieces(pen);
    paintWalls(pen);
  }

  public void paintBlock(Graphics pen, int x, int y, int radius)
  {
    int radiusCos60 = (int)(radius * Math.cos(1.047197551196598D));
    int radiusSin60 = (int)(radius * Math.sin(1.047197551196598D));
    int[] xpos = { x, x - radiusSin60, x - radiusSin60, x, x + radiusSin60, x + radiusSin60 };
    int[] ypos = { y + radius, y + radiusCos60, y - radiusCos60, y - radius, y - radiusCos60, y + radiusCos60 };
    pen.fillPolygon(xpos, ypos, 6);
  }

  public void paintPieces(Graphics pen)
  {
    for (int piece = 1; piece <= this.myPlayers + 1; piece++) {
      pen.setColor(Color.white);
      if (exist(piece))
        pen.fillOval(this.myWidth * getColumn(piece) / (2 * this.myBoardSize) - this.myRadius + this.myWallWidth - 1, 
          (int)(this.myWidth * (
          getRow(piece) + 1) / (2 * this.myBoardSize) * Math.sin(1.047197551196598D) - this.myRadius + this.myWallWidth) - 1, 
          (this.myRadius - this.myWallWidth) * 2 + 2, (this.myRadius - this.myWallWidth) * 2 + 2);
      pen.setColor(stringToColor(this.myPieceColors[(piece - 1)]));
      if (exist(piece))
        pen.fillOval(this.myWidth * getColumn(piece) / (2 * this.myBoardSize) - this.myRadius + this.myWallWidth, 
          (int)(this.myWidth * (
          getRow(piece) + 1) / (2 * this.myBoardSize) * Math.sin(1.047197551196598D) - this.myRadius + this.myWallWidth), 
          (this.myRadius - this.myWallWidth) * 2, (this.myRadius - this.myWallWidth) * 2);
    }
  }

  public void paintWalls(Graphics pen)
  {
    int[] xpos = new int[6];
    int[] ypos = new int[6];
    for (int i = 0; i <= 2 * this.myBoardSize; i++) {
      for (int j = 0; j <= 2 * this.myBoardSize; j += 2) {
        if ((this.myHexBoard[i][j] == 8) || (this.myHexBoard[i][j] == 9) || (this.myHexBoard[i][j] == 10)) {
          if (this.myHexBoard[i][j] == 8)
            pen.setColor(this.myWallColor);
          if (this.myHexBoard[i][j] == 9)
            pen.setColor(this.myTempWallColor);
          if (this.myHexBoard[i][j] == 10)
            pen.setColor(Color.red);
          if (((isEven(i)) && (!isEven(j / 2))) || ((!isEven(i)) && (isEven(j / 2)))) {
            xpos[0] = (this.myWidth * i / (2 * this.myBoardSize));
            xpos[1] = (this.myWidth * i / (2 * this.myBoardSize) + this.myWallWidth / 3);
            xpos[2] = (this.myWidth * (i + 1) / (2 * this.myBoardSize));
            xpos[3] = (this.myWidth * i / (2 * this.myBoardSize));
            xpos[4] = (this.myWidth * (i - 1) / (2 * this.myBoardSize));
            xpos[5] = (this.myWidth * i / (2 * this.myBoardSize) - this.myWallWidth / 3);
            ypos[0] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) - this.myWidth / (2 * this.myBoardSize) + this.myWallWidth / 1.5D) * 
              Math.sin(1.047197551196598D)));
            ypos[1] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize) + this.myWallWidth / 4) * 
              Math.sin(1.047197551196598D)));
            ypos[2] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) + this.myWidth / (1.5D * this.myBoardSize)) * 
              Math.sin(1.047197551196598D)));
            ypos[3] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) + this.myWidth / (2 * this.myBoardSize) - this.myWallWidth / 2) * 
              Math.sin(1.047197551196598D)));
            ypos[4] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) + this.myWidth / (1.5D * this.myBoardSize)) * 
              Math.sin(1.047197551196598D)));
            ypos[5] = 
              ((int)((this.myWidth * j / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize) + this.myWallWidth / 4) * 
              Math.sin(1.047197551196598D)));
          } else {
            xpos[0] = (this.myWidth * i / (2 * this.myBoardSize));
            xpos[1] = (this.myWidth * i / (2 * this.myBoardSize) + this.myWallWidth / 3);
            xpos[2] = (this.myWidth * (i + 1) / (2 * this.myBoardSize));
            xpos[3] = (this.myWidth * i / (2 * this.myBoardSize));
            xpos[4] = (this.myWidth * (i - 1) / (2 * this.myBoardSize));
            xpos[5] = (this.myWidth * i / (2 * this.myBoardSize) - this.myWallWidth / 3);
            ypos[0] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) + this.myWidth / (2 * this.myBoardSize) - this.myWallWidth / 1.5D) * 
              Math.sin(1.047197551196598D)));
            ypos[1] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) - this.myWidth / (4 * this.myBoardSize) - this.myWallWidth / 4) * 
              Math.sin(1.047197551196598D)));
            ypos[2] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) - this.myWidth / (1.5D * this.myBoardSize)) * 
              Math.sin(1.047197551196598D)));
            ypos[3] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) - this.myWidth / (2 * this.myBoardSize) + this.myWallWidth / 2) * 
              Math.sin(1.047197551196598D)));
            ypos[4] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) - this.myWidth / (1.5D * this.myBoardSize)) * 
              Math.sin(1.047197551196598D)));
            ypos[5] = 
              ((int)((this.myWidth * (j + 2) / (2 * this.myBoardSize) - this.myWidth / (4 * this.myBoardSize) - this.myWallWidth / 4) * 
              Math.sin(1.047197551196598D)));
          }
          pen.fillPolygon(xpos, ypos, 6);
        }
      }
    }
    repaint();
  }

  public int xPosToElement(int x)
  {
    for (int i = 0; i <= 2 * this.myBoardSize; i++) {
      if ((x > this.myWidth * (i - 1) / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize)) && 
        (x <= this.myWidth * i / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize)))
        return i;
    }
    throw new IllegalArgumentException("Click somewhere ON the board.");
  }

  public int yPosToElement(int x, int y)
  {
    int xElement = xPosToElement(x);
    int j = 1;
    if (isEven(xElement))
      for (int i = 0; i <= 2 * this.myBoardSize; i++)
        if (!isEven((i + 1) / 2)) {
          if ((y > this.myWidth * i / (2 * this.myBoardSize) * Math.sin(1.047197551196598D)) && 
            (y <= this.myWidth * (i + 1) / (2 * this.myBoardSize) * Math.sin(1.047197551196598D)))
            return i - i % 2;
        } else {
          j = i / 2;
          if ((y > (this.myWidth * j / this.myBoardSize - this.myWidth / (2 * this.myBoardSize)) * Math.sin(1.047197551196598D)) && 
            (y <= (this.myWidth * (j + 1) / this.myBoardSize - this.myWidth / (2 * this.myBoardSize)) * Math.sin(1.047197551196598D)))
            return 2 * j - 1;
        }
    if (!isEven(xElement))
      for (int i = 0; i <= 2 * this.myBoardSize; i++)
        if (isEven((i + 1) / 2)) {
          if ((y > this.myWidth * i / (2 * this.myBoardSize) * Math.sin(1.047197551196598D)) && 
            (y <= this.myWidth * (i + 1) / (2 * this.myBoardSize) * Math.sin(1.047197551196598D)))
            return i - i % 2;
        } else {
          j = i / 2;
          if ((y > (this.myWidth * j / this.myBoardSize - this.myWidth / (2 * this.myBoardSize)) * Math.sin(1.047197551196598D)) && 
            (y <= (this.myWidth * (j + 1) / this.myBoardSize - this.myWidth / (2 * this.myBoardSize)) * Math.sin(1.047197551196598D)))
            return 2 * j - 1;
        }
    throw new IllegalArgumentException("Click somewhere on the board.");
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
      repaint();
      if (gameOver(piece, x, y)) {
        throw new IllegalArgumentException(this.myNames[(piece - 1)] + " WINS!!");
      }
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
      double[] move = compmove(new HexBoard(this), 2);
      if (!isEven((int)move[1]))
        changePos((int)move[0], (int)move[1]);
      else
        addWall(8, (int)move[0], (int)move[1], false);
    } else if (this.myCompLevel == 5) {
      double[] move = compmove(new HexBoard(this), 3);
      if (!isEven((int)move[1]))
        changePos((int)move[0], (int)move[1]);
      else
        addWall(8, (int)move[0], (int)move[1], false);
    }
  }

  public double[] compmove(HexBoard board, int iteration) {
    ArrayList<Integer> path1 = board.shortPath(1);
    ArrayList<Integer> path2 = board.shortPath(2);

    if ((path1.size() == 4) || (path2.size() == 4) || (iteration == 0)) {
      return new double[] { path2.get(2), path2.get(3), 
        evaluate(path1.size() / 2, path2.size() / 2, board.getWallCount(1), board.getWallCount(2)) };
    }
    int x = 0; int y = 0;
    if (board.getPlayer() == 2) {
      double value = 0.0D; double maxValue = -100.0D;
      for (int i = 2 * this.myBoardSize - 2; i >= 2; i--) {
        for (int j = 2 * this.myBoardSize - 2; j >= 2; j -= 2) {
          if ((board.checkWall(8, i, j, true)) && (board.getWallCount(2) > 0))
          {
            HexBoard b1 = new HexBoard(board);
            b1.addWall(8, i, j, false);
            value = compmove(b1, iteration - 1)[2];

            for (int k = 0; k < path1.size() - 3; k += 2) {
              if ((((path1.get(k) + path1.get(k + 2)) / 2 == i) && (
                (path1.get(k + 1) + 1 == j) || (path1.get(k + 1) - 1 == j))) || (
                ((path1.get(k) == i) || (path1.get(k + 2) == i)) && 
                ((path1.get(k + 1) + path1.get(k + 3)) / 2 == j)))
                value += 0.0001D;
            }
            value -= (Math.abs(board.getColumn(1) - i) + Math.abs(board.getColumn(2) - j)) / 10000.0D;
            if (value > maxValue) {
              x = i;
              y = j;
              maxValue = value;
            }
          }
        }
      }

      int i = path2.get(2); int j = path2.get(3);
      HexBoard b1 = new HexBoard(board);
      b1.changePos(i, j);
      value = compmove(b1, iteration - 1)[2];
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
        if ((board.checkWall(8, i, j, true)) && (board.getWallCount(1) > 0))
        {
          HexBoard b1 = new HexBoard(board);
          b1.addWall(8, i, j, false);
          value = compmove(b1, iteration - 1)[2];

          for (int k = 0; k < path2.size() - 3; k += 2) {
            if ((((path2.get(k) + path2.get(k + 2)) / 2 == i) && (
              (path2.get(k + 1) + 1 == j) || (path2.get(k + 1) - 1 == j))) || (
              ((path2.get(k) == i) || (path2.get(k + 2) == i)) && 
              ((path2.get(k + 1) + path2.get(k + 3)) / 2 == j)))
              value -= 0.0001D;
          }
          value += (Math.abs(board.getColumn(2) - i) + Math.abs(board.getRow(2) - j)) / 10000.0D;
          if (value < minValue) {
            x = i;
            y = j;
            minValue = value;
          }
        }
      }
    }
    int i = path1.get(2); int j = path1.get(3);
    HexBoard b1 = new HexBoard(board);
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

  public boolean getComp()
  {
    return this.compPlay;
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

  public void tempPos(int x, int y)
  {
    int piece = getPlayer();
    int i = getColumn(piece);
    int j = getRow(piece);

    if (checkPos(this.myHexBoard, i, j, x, y, true)) {
      this.myHexBoard[x][y] = (this.myPlayers + 1);
    }
    repaint();
  }

  public void removeTemp()
  {
    for (int i = 0; i <= 2 * this.myBoardSize; i++) {
      for (int j = 0; j <= 2 * this.myBoardSize; j++) {
        if ((this.myHexBoard[i][j] == this.myPlayers + 1) || (this.myHexBoard[i][j] == 9) || (this.myHexBoard[i][j] == 10))
          this.myHexBoard[i][j] = 0;
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

  public String getWallNumber() {
    return this.wallNumber;
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

  public boolean exist(int piece)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= 2 * this.myBoardSize - 1; j++) {
        if (this.myHexBoard[i][j] == piece)
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
      return Color.decode("#ffd700");
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
