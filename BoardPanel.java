package passaj;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
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
	private Point[] myLocations;
	private List<int[]> walls;
	private Point[] tempPos;
	private int[] tempWall;

	public BoardPanel()
	{
		this.myBoardSize = DEFAULT_BOARD_SIZE;
		this.myBoardType = "default";

		initialize();
	}

	public BoardPanel(
		final String[] colors,
		final String boardType,
		final int boardSize,
		final Point[] locations)
	{
		this.myPlayerCount = colors.length;
		this.myColors = new Color[colors.length];
		for (int i = 0; i < colors.length; i++)
			this.myColors[i] = Color.decode(colors[i]);

		this.myBoardType = boardType;
		this.myBoardSize = boardSize;
		this.myLocations = locations;

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

		this.tempPos = new Point[0];
		this.tempWall = new int[] {0, 0, 0};
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
				paintBlock(pen, x, y, this.myBlockWidth);

				// If it's a goal row, make it slighty lighter than the rest
				if (isGoalRow(new Point(i, j)))
					pen.setColor(Color.darkGray);
				else
					pen.setColor(Color.black);

				paintBlock(pen, x + 1, y + 1, this.myBlockWidth - 2);
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
		int width = this.myPieceDiameter;

		// Paint all players pieces
		for (int piece = 0; piece < this.myPlayerCount; piece++)
		{
			int x = convertToPix(this.myLocations[piece].x + 1, adjust);
			int y = convertToPix(this.myLocations[piece].y + 1, adjust);

			// Add a white border by painting a slightly bigger piece
			// just underneath the piece.
			pen.setColor(Color.white);
			pen.fillOval(x - 1, y - 1, width + 2, width + 2);
			pen.setColor(this.myColors[piece]);
			pen.fillOval(x, y, width, width);
		}

		// If there are temporary pieces to indicate where
		// a player can move, then paint them light gray
		for (Point piece : tempPos)
		{
			int x = convertToPix(piece.x + 1, adjust);
			int y = convertToPix(piece.y + 1, adjust);
			pen.setColor(Color.lightGray);
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

		for (int[] wall : this.walls)
			paintWall(pen, wall);

		if (tempWall[0] != 0)
		{
			pen.setColor(BoardPanel.TempWallColor);
			paintWall(pen, tempWall);
		}
	}

	public void paintWall(Graphics pen, int[] wall)
	{
		int width = this.myWallWidth;
		int height = 2 * this.myBlockWidth + this.myWallWidth;
		int adjust = this.myBlockWidth / 2;
		if (wall[2] == 1)
		{
			int x = this.convertToPix(wall[0], adjust);
			int y = this.convertToPix(wall[1], -adjust);
			pen.fillRect(x, y, width, height);
		}
		else
		{
			int x = this.convertToPix(wall[0], -adjust);
			int y = this.convertToPix(wall[1], adjust);
			pen.fillRect(x, y, height, width);
		}
	}

	public void paintHexBlocks(Graphics pen)
	{
		// We need to do twice the board size in the x direction
		// so that we can stagger the hexes
		for (int i = 0; i < this.myBoardSize * 2 - 1; i++)
		{
			for (int j = 0; j < this.myBoardSize; j++)
			{
				if (!isValidHex(new Point(i, j)))
					continue;

				// Convert the board location into pixels
				int x = this.convertToPix(i + 1, 0);
				int y = this.convertToPix((j + 1) * 2, -j * this.myWallWidth);

				// Paint a slightly bigger hex underneath to give it a border
				pen.setColor(Color.lightGray);
				paintHexBlock(pen, new Point(x, y), this.myRadius + 2);

				// Paint the goal row slightly lighter
				if (isGoalRow(new Point(i, j)))
					pen.setColor(Color.darkGray);
				else
					pen.setColor(Color.black);

				// Paint the block
				paintHexBlock(pen, new Point(x, y), this.myRadius);
			}
		}
	}

	public void paintHexBlock(Graphics pen, final Point p, final int radius)
	{
		final int rCos60 = (int)(radius * Math.cos(Math.PI / 3));
		final int rSin60 = (int)(radius * Math.sin(Math.PI / 3));

		// Set up vertices of the hex
		int[] xpos = {
			p.x,
			p.x - rSin60,
			p.x - rSin60,
			p.x,
			p.x + rSin60,
			p.x + rSin60
		};

		int[] ypos = {
			p.y + radius,
			p.y + rCos60,
			p.y - rCos60,
			p.y - radius,
			p.y - rCos60,
			p.y + rCos60
		};

		// Paint the hex
		pen.fillPolygon(xpos, ypos, 6);
	}

	private boolean isGoalRow(final Point p)
	{
		final int half = (this.myBoardSize - 1) / 2;

		// Top and bottom row are always goal rows
		if (p.y == 0 || p.y == half * 2)
			return true;

		// Always call the border of the hex board a goal row
		if (this.myBoardType == "hexagonal")
			return 2 * p.x + p.y == half
				|| p.y - 2 * p.x == half
				|| 2 * p.x + p.y == 5 * half
				|| 2 * p.x - p.y == 3 * half;

		// Only call the sides of the board a goal row when there's 4 people
		else if (this.myPlayerCount == 4)
			return p.x == 0 || p.x == half * 2;

		else
			return false;
	}

	private boolean isValidHex(final Point p)
	{
		if (isEven(p.x) && !isEven(p.y) || !isEven(p.x) && isEven(p.y))
			return false;

		final int half = (this.myBoardSize - 1) / 2;
		return p.y >= 0
			&& p.y <= half * 2
			&& p.x + p.y >= half
			&& p.y - p.x <= half 
			&& p.x - p.y <= 3 * half
			&& p.x + p.y <= 5 * half;
	}

	public void paintHexPieces(Graphics pen)
	{
		int width = (this.myRadius - this.myWallWidth) * 2;
		int adjust = -this.myRadius + this.myWallWidth;
		for (int piece = 0; piece < this.myPlayerCount; piece++) {
			int x = convertToPix(this.myLocations[piece].x, adjust);
			int y = convertToPix(this.myLocations[piece].y + 1, adjust);

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
	/*public int pixToPos(int pix)
	{
		for (int i = 1; i <= 2 * this.myBoardSize - 1; i++) {
			if ((pix > this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (pix <= this.myWidth * i / this.myBoardSize - this.myWallWidth / 2))
				return i * 2 - 1;
			if ((pix > this.myWidth * (i - 1) / this.myBoardSize - this.myWallWidth / 2) && (pix <= this.myWidth * (i - 1) / this.myBoardSize + this.myWallWidth / 2) && (i != 0))
				return i * 2 - 2;
		}
		throw new IllegalArgumentException("Click somewhere ON the board.");
	}*/

	private int pixToPos(final int pix)
	{
		return (pix * this.myBoardSize / this.myWidth);
	}

	private int pixToWallPos(final int pix)
	{
		return pixToPos(pix - this.myBlockWidth / 2) * 2 + 1;
	}

	/**
	 * Interpret a mouse location (in pixels) as a wall on the board.
	 */
	public Point pixToWallPoint(final int x, final int y)
	{
		return new Point(pixToWallPos(x), pixToWallPos(y));
	}

	public Point pixToMovePoint(final int x, final int y)
	{
		return new Point(pixToPos(x) * 2, pixToPos(y) * 2);
	}

	public int orientation(final int x, final int y)
	{
		final int xplusy = pixToPos(x + y) % 2;
		final int xminusy = pixToPos(this.myWidth + x - y) % 2;
		return (xplusy + xminusy) % 2 + 1;
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

	public void setLocations(final Point[] locations)
	{
		this.myLocations = locations;
		repaint();
	}

	public void addWall(int[] wall)
	{
		this.walls.add(wall);
		repaint();
	}

	public void setTempWall(Point wall, int o)
	{
		this.tempWall = new int[] {wall.x, wall.y, o};
		this.tempPos = new Point[0];
		repaint();
	}

	public void showMoves(Point[] moves)
	{
		this.tempPos = moves;
		this.tempWall = new int[] {0, 0, 0};
		repaint();
	}
}

