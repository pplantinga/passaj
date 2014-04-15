package passaj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;
import java.util.ArrayList;
import javax.swing.JPanel;

public class BoardPanel extends JPanel
{
  public static final int DEFAULT_BOARD_WIDTH = 700;
  public static final int DEFAULT_WALL_WIDTH = 15;
  public static final int DEFAULT_BOARD_SIZE = 9;
	public static final Color WallColor = Color.decode("#402000");
	public static final Color TempWallColor = Color.decode("#876543");
  private int myWidth;
	private int myRadius;
  private int myWallWidth;
  private int myBoardSize;
  private int myBlockWidth;
	private int myPieceDiameter;
	private int myPlayerCount;
	private String myBoardType;
	private Color[] myColors;
	private int[] xs;
	private int[] ys;
	private List<int[]> walls;

	public BoardPanel()
	{
		this.myBoardSize = DEFAULT_BOARD_SIZE;
		this.myBoardType = "default";

		initialize();
	}

	public BoardPanel(String[] colors, String boardType, int boardSize, int[] xs, int[] ys)
	{
		this.myPlayerCount = colors.length;
		this.myColors = new Color[colors.length];
		for (int i = 0; i < colors.length; i++)
			this.myColors[i] = Color.decode(colors[i]);

		this.myBoardType = boardType;
		this.myBoardSize = boardSize;
		this.xs = xs;
		this.ys = ys;

		initialize();
	}

	private void initialize()
	{
		this.myWidth = DEFAULT_BOARD_WIDTH;
		if (this.myBoardType == "hexagonal")
		{
			this.myWallWidth = this.myWidth / (5 * this.myBoardSize);
			this.myRadius = (int)(this.myWidth / (1.5D * this.myBoardSize) - this.myWallWidth);
		}
		else
		{
			this.myBlockWidth = 565 / this.myBoardSize;
			this.myWallWidth = 135 / this.myBoardSize;
			this.myPieceDiameter = this.myBlockWidth - this.myWallWidth;
		}
		this.walls = new ArrayList<int[]>();

    setPreferredSize(new Dimension(this.myWidth, this.myWidth));
    setBackground(Color.gray);
	}

  public void paintComponent(Graphics pen)
  {
    super.paintComponent(pen);

		if (this.myBoardType == "hexagonal")
		{
			paintHexBlocks(pen);
			paintHexPieces(pen);
			paintHexWalls(pen);
		}
		else
		{
			paintBlocks(pen);
			paintPieces(pen);
			paintWalls(pen);
		}
  }

	public void paintBlocks(Graphics pen)
	{
   	for (int i = 0; i < this.myBoardSize; i++)
		{
      for (int j = 0; j < this.myBoardSize; j++)
			{
				int x = convertToPix(i * 2, this.myWallWidth / 2);
				int y = convertToPix(j * 2, this.myWallWidth / 2);

				// Lighter square first, for a border
				pen.setColor(Color.lightGray);
        paintBlock(pen, x - 1, y - 1, this.myBlockWidth + 2);

				// If it's a goal row, make it slighty lighter than the rest
        if (j == 0
						|| j == this.myBoardSize - 1
						|| this.myPlayerCount == 4
							&& (i == 0 || i == this.myBoardSize - 1))
          pen.setColor(Color.darkGray);
        else
          pen.setColor(Color.black);

        paintBlock(pen, x, y, this.myBlockWidth);
      }
    }
	}

  public void paintBlock(Graphics pen, int xPos, int yPos, int width)
  {
    pen.fillRoundRect(xPos, yPos, width, width, this.myWallWidth, this.myWallWidth);
  }

  public void paintPieces(Graphics pen)
  {
		int adjust = -this.myPieceDiameter / 2;
    for (int piece = 0; piece < this.myPlayerCount; piece++)
		{
			int x = convertToPix(this.xs[piece], adjust);
			int y = convertToPix(this.ys[piece], adjust);
			int width = this.myPieceDiameter;

			// Add a white border by painting a slightly bigger piece
			// just underneath the piece.
			pen.setColor(Color.white);
			pen.fillOval(x - 1, y - 1, width + 2, width + 2);
			pen.setColor(this.myColors[piece]);
			pen.fillOval(x, y, width, width);
		}
  }

	public int convertToPix(int pos, int adjust)
	{
		return this.myWidth * pos / (2 * this.myBoardSize) + adjust; 
	}

  public void paintWalls(Graphics pen)
  {
		pen.setColor(BoardPanel.WallColor);

		int width = this.myWallWidth;
		int height = this.myBlockWidth + this.myWallWidth;
		int adjust = -this.myWallWidth / 2;
		for (int[] wall : this.walls)
		{
			if (wall[2] == 1)
			{
				int x = this.convertToPix(wall[0], adjust);
				int y = this.convertToPix(wall[1] - 1, adjust);
				pen.fillRect(x, y, width, height);
			}
			else
			{
				int x = this.convertToPix(wall[0] - 1, adjust);
				int y = this.convertToPix(wall[1], adjust);
				pen.fillRect(x, y, height, width);
			}
		}
  }

	public void paintHexBlocks(Graphics pen)
	{
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      for (int j = 1; j <= this.myBoardSize; j++) {
        pen.setColor(Color.lightGray);
        if (((!isEven(i)) || (isEven(j))) && ((isEven(i)) || (!isEven(j))) && 
          (i + j >= (this.myBoardSize + 1) / 2 + 1) && (j >= 1) && (j <= this.myBoardSize) && 
          (i - j <= 1.5D * (this.myBoardSize + 1) - 3.0D) && (i + j <= 2.5D * (this.myBoardSize + 1) - 3.0D) && (j - i <= (this.myBoardSize + 1) / 2 - 1))
          paintHexBlock(pen, this.myWidth * i / (2 * this.myBoardSize), 
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
          paintHexBlock(pen, this.myWidth * i / (2 * this.myBoardSize), 
            (int)(this.myWidth * j / this.myBoardSize * 
            Math.sin(1.047197551196598D)), this.myRadius);
        }
      }
    }
	}

	public void paintHexBlock(Graphics pen, int x, int y, int radius)
	{
    int radiusCos60 = (int)(radius * Math.cos(1.047197551196598D));
    int radiusSin60 = (int)(radius * Math.sin(1.047197551196598D));
    int[] xpos = { x, x - radiusSin60, x - radiusSin60, x, x + radiusSin60, x + radiusSin60 };
    int[] ypos = { y + radius, y + radiusCos60, y - radiusCos60, y - radius, y - radiusCos60, y + radiusCos60 };
    pen.fillPolygon(xpos, ypos, 6);
	}

	public void paintHexPieces(Graphics pen)
	{
		int width = (this.myRadius - this.myWallWidth) * 2;
		int adjust = -this.myRadius + this.myWallWidth;
    for (int piece = 0; piece < this.myPlayerCount; piece++) {
			int x = convertToPix(this.xs[piece], adjust);
			int y = convertToPix(this.ys[piece] + 1, adjust);

			// For a white border, just draw the same thing
			// slightly bigger and underneath the other.
      pen.setColor(Color.white);
			pen.fillOval(x - 1, y - 1, width + 2,	width + 2);
			pen.setColor(this.myColors[piece]);
			pen.fillOval(x, y, width,	width);
    }
	}

	public void paintHexWalls(Graphics pen)
	{
    pen.setColor(BoardPanel.WallColor);
		for (int[] wall : this.walls)
			paintHexWall(pen, wall[0], wall[1]);
    repaint();
  }

	public void paintHexWall(Graphics pen, int x, int y)
	{
		int[] xpos = new int[6];
    int[] ypos = new int[6];
		xpos[0] = convertToPix(x, 0);
		xpos[1] = convertToPix(x, this.myWallWidth / 3);
		xpos[2] = convertToPix(x + 1, 0);
		xpos[3] = convertToPix(x, 0);
		xpos[4] = convertToPix(x - 1, 0);
		xpos[5] = convertToPix(x, -this.myWallWidth / 3);
		if (((isEven(x)) && (!isEven(y / 2))) || ((!isEven(x)) && (isEven(y / 2)))) {
			ypos[0] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) - this.myWidth / (2 * this.myBoardSize) + this.myWallWidth / 1.5D) * 
				Math.sin(1.047197551196598D)));
			ypos[1] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize) + this.myWallWidth / 4) * 
				Math.sin(1.047197551196598D)));
			ypos[2] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) + this.myWidth / (1.5D * this.myBoardSize)) * 
				Math.sin(1.047197551196598D)));
			ypos[3] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) + this.myWidth / (2 * this.myBoardSize) - this.myWallWidth / 2) * 
				Math.sin(1.047197551196598D)));
			ypos[4] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) + this.myWidth / (1.5D * this.myBoardSize)) * 
				Math.sin(1.047197551196598D)));
			ypos[5] = 
				((int)((this.myWidth * y / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize) + this.myWallWidth / 4) * 
				Math.sin(1.047197551196598D)));
		} else {
			ypos[0] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) + this.myWidth / (2 * this.myBoardSize) - this.myWallWidth / 1.5D) * 
				Math.sin(1.047197551196598D)));
			ypos[1] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) - this.myWidth / (4 * this.myBoardSize) - this.myWallWidth / 4) * 
				Math.sin(1.047197551196598D)));
			ypos[2] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) - this.myWidth / (1.5D * this.myBoardSize)) * 
				Math.sin(1.047197551196598D)));
			ypos[3] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) - this.myWidth / (2 * this.myBoardSize) + this.myWallWidth / 2) * 
				Math.sin(1.047197551196598D)));
			ypos[4] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) - this.myWidth / (1.5D * this.myBoardSize)) * 
				Math.sin(1.047197551196598D)));
			ypos[5] = 
				((int)((this.myWidth * (y + 2) / (2 * this.myBoardSize) - this.myWidth / (4 * this.myBoardSize) - this.myWallWidth / 4) * 
				Math.sin(1.047197551196598D)));
		}
		pen.fillPolygon(xpos, ypos, 6);
	}

	/**
	 * Interpret a mouse location as a position on the board.
	 */
  public int pixToPos(int pix)
  {
    for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
      if ((pix > this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (pix <= this.myWidth * i / this.myBoardSize - this.myWallWidth / 2))
        return i * 2 - 1;
      if ((pix > this.myWidth * (i - 1) / this.myBoardSize - this.myWallWidth / 2) && (pix <= this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (i != 0))
        return i * 2 - 2;
    }
    throw new IllegalArgumentException("Click somewhere ON the board.");
  }

  public boolean isTopLeftHalf(int pix)
  {
    if (pixToPos(pix) == 1) {
      return false;
    }
    if (pixToPos(pix) == 2 * this.myBoardSize - 1) {
      return true;
    }
    for (int i = 0; i <= 8; i++) {
      if ((pix > this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (pix <= this.myWidth * i / this.myBoardSize - this.myWallWidth / 2 - this.myBlockWidth / 2))
        return true;
    }
    return false;
  }


  public int hexPixToPos(int x, int y, String which)
  {
		if (which == "x")
		{
			for (int i = 0; i <= 2 * this.myBoardSize; i++) {
				if ((x > this.myWidth * (i - 1) / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize)) && 
					(x <= this.myWidth * i / (2 * this.myBoardSize) + this.myWidth / (4 * this.myBoardSize)))
					return i;
			}
			throw new IllegalArgumentException("Click somewhere ON the board.");
		}
		else
		{
		  int xElement = hexPixToPos(x, y, "x");
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
  }

	public boolean isEven(int number)
	{
		return number % 2 == 0;
	}

	public void setPos(int[] xs, int[] ys)
	{
		this.xs = xs;
		this.ys = ys;
	}

	public void addWall(int[] wall)
	{
		this.walls.add(wall);
	}
}

