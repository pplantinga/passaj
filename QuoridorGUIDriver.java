package passaj;

import java.awt.BorderLayout;
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
  private JButton myButton;
  private JButton ruleButton;
  private JButton quitButton;
  private JPanel myPanel;
  private QuoridorModel myModel;
	private BoardPanel myBoardPanel;
  private String myType;
	private String[] myNames;
	private String[] myColors;
	private int playerCount;
	private int boardSize;
	private int compLevel;
 

  public QuoridorGUIDriver()
	{
    this.myModel = new QuoridorModel();
    this.myType = "default";
    initialize();
  }

  public QuoridorGUIDriver(String[] names, String[] colors, String type, int size, int compLevel)
	{
		this.myNames = names;
		this.myColors = colors;
		this.playerCount = names.length;
		this.boardSize = size;
		this.compLevel = compLevel;
		this.myType = type;

		this.myModel = new QuoridorModel(this.playerCount, size, type);
		int[] xs = {this.myModel.getColumn(1), this.myModel.getColumn(2)};
		int[] ys = {this.myModel.getRow(1), this.myModel.getRow(2)};
		this.myBoardPanel = new BoardPanel(colors, type, size, xs, ys);

    initialize();
  }

	/**
	 * Rev up your engines, ladies and gentlemen.
	 */
  public void initialize()
	{
    setTitle("Welcome to Passaj!");

		// Add action listeners
		this.myBoardPanel.addMouseListener(this);
		this.myBoardPanel.addMouseMotionListener(this);

		// Initialize UI elements
    this.myField = new JTextField();
    this.myField.setEditable(false);
    this.myButton = new JButton("New Game of PASSAJ");
    this.myButton.addActionListener(this);
    this.myButton.setVisible(true);
    this.myButton.setActionCommand("new game");
    this.ruleButton = new JButton("Rules of PASSAJ");
    this.ruleButton.addActionListener(this);
    this.ruleButton.setVisible(true);
    this.ruleButton.setActionCommand("rules");
    this.quitButton = new JButton("Quit PASSAJ");
    this.quitButton.addActionListener(this);
    this.quitButton.setVisible(true);
    this.quitButton.setActionCommand("quit");

		// Add things to the layout
    this.myPanel = new JPanel();
    this.myPanel.add(this.ruleButton, "East");
    this.myPanel.add(this.quitButton, "Center");
    this.myPanel.add(this.myButton, "West");
    setLayout(new BorderLayout());
		add(this.myBoardPanel, "North");
    add(this.myPanel, "South");
    add(this.myField, "Center");
    setDefaultCloseOperation(3);

		// Begin with friendly welcoming text.
    String openText = "Welcome to the match-up between ";
		for (int i = 0; i < this.playerCount - 1; i++)
			openText += this.myNames[i] + " and ";
		openText += this.myNames[this.playerCount - 1] + ".";
    this.myField.setText(openText);
  }

	// Begin.
  public static void main(String[] args)
	{
    QuoridorGUIDriver pass = new QuoridorGUIDriver();
    pass.pack();
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
      InputGUIDriver input = new InputGUIDriver(this.myNames, this.myColors, this.myType, this.boardSize, this.compLevel);
			input.pack();
			input.setVisible(true);
			setVisible(false);
    }

		// Player clicked on "rules" button
    if (e.getActionCommand().equals("rules"))
    {
			RuleGUIDriver rule = new RuleGUIDriver(this);

			rule.pack();
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
		try
		{
			int x = this.myBoardPanel.pixToPos(e.getX());
			int y = this.myBoardPanel.pixToPos(e.getY());
			int o = this.myBoardPanel.orientation(e.getX(), e.getY());
			this.myModel.move(x, y, o);

			int[] xs = {this.myModel.getColumn(1), this.myModel.getColumn(2)};
			int[] ys = {this.myModel.getRow(1), this.myModel.getRow(2)};
			this.myBoardPanel.setPos(xs, ys);
			this.myBoardPanel.repaint();

			if (this.compLevel != 0)
				this.myModel.ai_move(this.compLevel * 1000);

			String fieldText = "";
			for (int i = 0; i < this.playerCount; i++)
				fieldText += this.myNames[i] + " "
					+ this.myModel.getWallCount(i) + " ";

			fieldText += "  walls left.  Player's Turn: "
				+ this.myNames[this.myModel.getPlayer()];
			this.myField.setText(fieldText);
		}
		catch (Exception ex)
		{
			this.myField.setText(ex.getMessage());
		}
  }

	/**
	 * When the player moves the mouse, display a helpful temporary
	 * piece or wall where they would move if they clicked.
	 */
  public void mouseMoved(MouseEvent e)
	{
		int x = this.myBoardPanel.pixToPos(e.getX());
		int y = this.myBoardPanel.pixToPos(e.getY());
		int o = this.myBoardPanel.orientation(e.getX(), e.getY());

		if (o != 0)
			this.myBoardPanel.setTempWall(x, y, o);
		else
			this.myBoardPanel.setTempPos(x, y);
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
