package passaj;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class displays the quoridor board and handles interactions.
 */
public class QuoridorGUIDriver extends JFrame
	implements ActionListener, MouseListener, MouseMotionListener
{
	private JTextField myField;
	private JButton newGameButton;
	private JButton ruleButton;
	private JButton quitButton;
	private JPanel myButtonPanel;
	private QuoridorModel myModel;
	private BoardPanel myBoardPanel;
	private String myType;
	private String myMode;
	private String[] myNames;
	private String[] myColors;
	private int myPlayerCount;
	private int myBoardSize;
	private int myCompLevel;
 

	public QuoridorGUIDriver()
	{
		this.myModel = new QuoridorModel();
		this.myType = "default";
		this.myNames = new String[] {"Player 1", "Player 2"};
		this.myColors = new String[] {"#123456", "#987654"};
		this.myPlayerCount = 2;
		this.myBoardSize = 9;

		initialize();
	}

	public QuoridorGUIDriver(String[] names, String[] colors, String type, int size, int compLevel)
	{
		this.myNames = names;
		this.myColors = colors;
		this.myPlayerCount = names.length;
		this.myBoardSize = size;
		this.myCompLevel = compLevel;
		this.myType = type;

		initialize();
	}

	/**
	 * Rev up your engines, ladies and gentlemen.
	 */
	public void initialize()
	{
		// Initialize classes
		this.myModel = new QuoridorModel(myPlayerCount, myBoardSize, myType);
		Point[] locations = this.myModel.getLocations();
		this.myBoardPanel = new BoardPanel(myColors, myType, myBoardSize, locations);

		// Window title
		setTitle("Welcome to Passaj!");

		// Add action listeners
		this.myBoardPanel.addMouseListener(this);
		this.myBoardPanel.addMouseMotionListener(this);

		// Initialize UI elements
		this.myField = new JTextField();
		this.myField.setEditable(false);
		this.newGameButton = new JButton("New Game of PASSAJ");
		this.newGameButton.addActionListener(this);
		this.newGameButton.setVisible(true);
		this.newGameButton.setActionCommand("new game");
		this.ruleButton = new JButton("Rules of PASSAJ");
		this.ruleButton.addActionListener(this);
		this.ruleButton.setVisible(true);
		this.ruleButton.setActionCommand("rules");
		this.quitButton = new JButton("Quit PASSAJ");
		this.quitButton.addActionListener(this);
		this.quitButton.setVisible(true);
		this.quitButton.setActionCommand("quit");

		// Add things to the layout
		this.myButtonPanel = new JPanel();
		this.myButtonPanel.add(this.ruleButton, "East");
		this.myButtonPanel.add(this.quitButton, "Center");
		this.myButtonPanel.add(this.newGameButton, "West");
		setLayout(new BorderLayout());
		add(this.myBoardPanel, "North");
		add(this.myButtonPanel, "South");
		add(this.myField, "Center");
		setDefaultCloseOperation(3);

		// Begin with friendly welcoming text.
		String openText = "Welcome to the match-up between ";
		for (int i = 0; i < this.myPlayerCount - 1; i++)
			openText += this.myNames[i] + " and ";
		openText += this.myNames[this.myPlayerCount - 1] + ".";
		this.myField.setText(openText);
	}

	// Begin.
	public static void main(String[] args)
	{
		QuoridorGUIDriver pass = new QuoridorGUIDriver();
		pass.pack();
		pass.setLocationRelativeTo(null);
		pass.setVisible(true);
	}

	/**
	 * Handle button clicks.
	 */
	public void actionPerformed(ActionEvent e)
	{
		// Player clicked on "new game" button
		if (e.getActionCommand().equals("new game"))
		{
			InputGUIDriver input = new InputGUIDriver(myNames, myColors, myType, myBoardSize, myCompLevel);
			input.pack();
			input.setLocationRelativeTo(null);
			input.setVisible(true);
			setVisible(false);
		}

		// Player clicked on "rules" button
		if (e.getActionCommand().equals("rules"))
		{
			RuleGUIDriver rule = new RuleGUIDriver(this);

			rule.pack();
			rule.setLocationRelativeTo(null);
			rule.setVisible(true);
			this.setVisible(false);
		}

		// User clicked on "quit" button, so QUIT
		if (e.getActionCommand().equals("quit"))
			System.exit(128);
	}

	/**
	 * The player just made a move!
	 */
	public void mouseClicked(MouseEvent e)
	{
		Point move = this.myBoardPanel.pixToMovePoint(e.getX(), e.getY());

		Point[] locations = this.myModel.getLocations();
		Point player = locations[this.myModel.getPlayer()];

		if (this.myMode == "move")
		{
			if (this.myModel.move(move))
			{
				this.myBoardPanel.setLocations(locations);

				this.myMode = "wall";

				// Erase temporary pieces
				this.myBoardPanel.showMoves(new Point[0]);
			}

			// The user clicked on the player again, go back to wall mode
			else if (move.x == player.x
					&& move.y == player.y)
			{
				this.myMode = "wall";

				// Erase temporary pieces
				this.myBoardPanel.showMoves(new Point[0]);

				return;
			}

			// TODO Illegal move, make some warning.
			else
			{
				return;
			}
		}
		else if (move.x == player.x
			&& move.y == player.y)
		{
			this.myMode = "move";
			Point[] legalMoves = this.myModel.legalMoves(player);
			this.myBoardPanel.showMoves(legalMoves);
			return;
		}
		else
		{
			Point wall = this.myBoardPanel.pixToWallPoint(e.getX(), e.getY());
			int o = this.myBoardPanel.orientation(e.getX(), e.getY());
			System.out.println(wall.x + " " + wall.y);

			if (this.myModel.move(wall, o))
				this.myBoardPanel.addWall(new int[] {wall.x, wall.y, o});
			else
				return;
		}

		if (this.myCompLevel != 0)
		{
			int[] themove = this.myModel.ai_move(this.myCompLevel * 1000);

			if (themove[2] == 0)
			{
				locations = this.myModel.getLocations();
				this.myBoardPanel.setLocations(locations);
			}
			else
			{
				this.myBoardPanel.addWall(themove);
			}
		}

		String fieldText = "";
		for (int i = 0; i < this.myPlayerCount; i++)
			fieldText += this.myNames[i] + " "
				+ this.myModel.getWallCount(i) + " ";

		fieldText += "	walls left.	Player's Turn: "
			+ this.myNames[this.myModel.getPlayer()];
		this.myField.setText(fieldText);
	}

	/**
	 * When the player moves the mouse, display a helpful temporary
	 * piece or wall where they would move if they clicked.
	 */
	public void mouseMoved(MouseEvent e)
	{
		Point move = this.myBoardPanel.pixToMovePoint(e.getX(), e.getY());

		Point[] locations = this.myModel.getLocations();
		Point player = locations[this.myModel.getPlayer()];

		if (myMode == "move")
		{
			// TODO show the player what his move would be
		}
		else if (move.x == player.x && move.y == player.y)
		{
			this.myBoardPanel.setTempWall(new Point(0, 0), 0);
		}
		else
		{
			Point tempWall = this.myBoardPanel.pixToWallPoint(e.getX(), e.getY());
			int o = this.myBoardPanel.orientation(e.getX(), e.getY());

			if (this.myModel.isLegalWall(tempWall, o))
			{
				this.myBoardPanel.setTempWall(tempWall, o);
			}
		}
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseExited(MouseEvent arg0)
	{
	}

	public void mousePressed(MouseEvent arg0)
	{
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

	public void mouseDragged(MouseEvent e)
	{
	}
}
