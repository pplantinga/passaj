package passaj;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * This class operates a gui window for choosing game options.
 */
public class InputGUIDriver extends JFrame
	implements ActionListener
{
	private static final long serialVersionUID = 1234567890123456789L;
	private static final int MIN_BOARD_SIZE = 3;
	private static final int MAX_BOARD_SIZE = 15;
	private static final int DEFAULT_BOARD_SIZE = 9;
	private JButton theBigRedButton;
	private JButton theBigBlueButton;
	private JButton hexButton;
	private JButton regButton;
	private JLabel sizelabel;
	private JLabel playerNumberlabel;
	private JLabel compLevellabel;
	private JSlider boardSizeSlider;
	private JRadioButton[] myPlay = new JRadioButton[5];
	private JRadioButton[] myCompLevels = new JRadioButton[5];
	private JLabel[] playerColorLabels = new JLabel[12];
	private JTextField[] playerColorFields = new JTextField[12];
	private String[] playerColors = {
		"Peter Plantinga", "#FFD700",
		"Red Guy", "#8B0000",
		"Blue Guy", "#63D1F4",
		"Green Guy", "#BCED91",
		"Orange Guy", "#FF8247",
		"Pink Guy", "#CD5C5C"
	};

	private String myType = "default";
	private JPanel playersPanel;
	private JPanel compPanel;
	private int myPlayers = 1;
	private int myCompLevel = 1;
	private boolean compEnabled = true;

	/**
	 * Default constructor.
	 */
	public InputGUIDriver()
	{
		initialize();
	}

	/**
	 * Constructor with options, for when you want to change the defaults.
	 *
	 * Params:
	 * 	names = The names of the players
	 * 	colors = The colors of the players
	 * 	boardType = "Hexagonal" or "Default"
	 * 	size = board size, default 9
	 * 	compLevel = How difficult the computer is
	 */
	public InputGUIDriver(String[] names, String[] colors, String boardType, int size, int compLevel)
	{
		initialize();
		this.myPlayers = names.length;
		if (this.myPlayers != 6)
		{
			this.myPlay[this.myPlayers - 1].setSelected(true);
			this.myPlay[0].setSelected(false);
		}

		for (int i = 0; i < 5; i++)
			this.myCompLevels[i].setEnabled(false);

		this.compLevellabel.setForeground(Color.gray);

		if (boardType.equals("hexagonal"))
		{
			this.regButton.setSelected(false);
			this.regButton.addActionListener(this);
			this.hexButton.setSelected(true);
			this.hexButton.removeActionListener(this);
			this.myType = "hexagonal";
		}

		this.boardSizeSlider.setValue(size);

		// Enable as many fields as there are players
		for (int i = 0; i < 12; i++)
		{
			if (i < this.myPlayers * 2)
			{
				String text = names[i / 2];
				if (i % 2 == 1)
					text = colors[i / 2];
				toggleField(i, true, text);
			}
			else
			{
				toggleField(i, false, "");
			}
		}
	}

	/**
	 * This function is run for every constructor.
	 */
	public void initialize()
	{
		setTitle("Opening Screen");

		// Player labels
		this.playerNumberlabel = new JLabel("Number of players: ", SwingConstants.RIGHT);
		for (int i = 0; i < 12; i++)
		{
			int player = i / 2 + 1;
			String label = "Enter Player " + player + " name:";
			if (i % 2 == 1)
				label = "Enter Player " + player + " color:";

			this.playerColorLabels[i] = new JLabel(label, SwingConstants.RIGHT);
		}

		this.playersPanel = new JPanel();
		this.playersPanel.setLayout(new GridLayout(1, 5));
		for (int i = 0; i <= 4; i++)
		{
			// Set labels for number of players. Neither hex boards nor
			// default boards can have 5 players, but hex can have 6 players
			int j = i + 1;
			if (i == 4)
				j = 6;

			this.myPlay[i] = new JRadioButton(Integer.toString(j));
			this.myPlay[i].setFocusable(false);
			this.myPlay[i].setActionCommand(new Integer(i).toString());
			this.myPlay[i].addActionListener(this);
			this.playersPanel.add(this.myPlay[i]);

			// Select 1 player by default
			if (i == 0)
				this.myPlay[i].setSelected(true);
		}

		// Computer level chooser
		this.compLevellabel = new JLabel("Computer level: ", SwingConstants.RIGHT);
		this.compPanel = new JPanel();
		this.compPanel.setLayout(new GridLayout(1, 5));
		for (int i = 0; i <= 4; i++)
		{
			this.myCompLevels[i] = new JRadioButton(Integer.toString(i + 1));
			this.myCompLevels[i].setActionCommand("comp" + i);
			this.myCompLevels[i].setFocusable(false);
			this.myCompLevels[i].addActionListener(this);
			this.compPanel.add(this.myCompLevels[i]);

			// Select level 1 by default
			if (i == 0)
				this.myCompLevels[i].setSelected(true);
		}

		// Initialize text fields
		for (int i = 0; i < 12; i++)
		{
			this.playerColorFields[i] = new JTextField();
			if (i >= 2)
				toggleField(i, false, "");
		}
		this.playerColorFields[0].setText(this.playerColors[0]);
		this.playerColorFields[1].setText(this.playerColors[1]);

		this.sizelabel = new JLabel("Enter size of board: ", SwingConstants.RIGHT);
		this.boardSizeSlider = new JSlider(
			MIN_BOARD_SIZE,
			MAX_BOARD_SIZE,
			DEFAULT_BOARD_SIZE
		);
		this.boardSizeSlider.setMajorTickSpacing(3);
		this.boardSizeSlider.setPaintLabels(true);

		// Initialize buttons
		this.regButton = new JButton("Regular Board");
		this.regButton.setActionCommand("boardType");
		this.regButton.setFocusable(false);
		this.regButton.setSelected(true);
		this.hexButton = new JButton("Hexagonal Board");
		this.hexButton.addActionListener(this);
		this.hexButton.setActionCommand("boardType");
		this.hexButton.setFocusable(false);
		this.theBigRedButton = new JButton("Quit");
		this.theBigRedButton.addActionListener(this);
		this.theBigRedButton.setActionCommand("quit");
		this.theBigRedButton.setFocusable(false);
		this.theBigBlueButton = new JButton("PLAY PASSAJ!!!");
		this.theBigBlueButton.addActionListener(this);
		this.theBigBlueButton.setActionCommand("new");
		this.theBigBlueButton.setFocusable(false);

		// Layout all the parts
		setLayout(new GridLayout(17, 2, 5, 2));
		add(this.regButton);
		add(this.hexButton);
		add(this.playerNumberlabel);
		add(this.playersPanel);
		add(this.compLevellabel);
		add(this.compPanel);
		for (int i = 0; i < 12; i++)
		{
			add(this.playerColorLabels[i]);
			add(this.playerColorFields[i]);
		}
		add(this.sizelabel);
		add(this.boardSizeSlider);
		add(this.theBigRedButton);
		add(this.theBigBlueButton);
		setDefaultCloseOperation(3);
	}

	/**
	 * Handle all actions in one function.
	 */
	public void actionPerformed(ActionEvent arg0)
	{
		String cmd = arg0.getActionCommand();

		// They clicked the "quit" button
		if (cmd.equalsIgnoreCase("quit"))
			System.exit(128);

		// They changed the number of players
		if (cmd.equals("0")
				|| cmd.equals("1")
				|| cmd.equals("2")
				|| cmd.equals("3")
				|| cmd.equals("4"))
		{
			if (this.myPlayers != 6)
				this.myPlay[this.myPlayers - 1].setSelected(false);
			else
				this.myPlay[4].setSelected(false);

			int players = Integer.parseInt(cmd);
			this.myPlay[players].setFocusable(false);
			this.myPlay[players].setSelected(true);

			this.myPlayers = players + 1;
			if (players == 4)
				this.myPlayers = 6;

			// 3 and 6 players can only exist on hex boards
			if (this.myPlayers == 3 || this.myPlayers == 6)
			{
				this.regButton.setSelected(false);
				this.regButton.setEnabled(false);
				this.hexButton.setSelected(true);
				this.hexButton.removeActionListener(this);
				this.myType = "hexagonal";
			}
			else if (!this.regButton.isEnabled())
			{
				this.regButton.setEnabled(true);
				this.regButton.removeActionListener(this);
				this.regButton.addActionListener(this);
			}

			// Enable the computer if there's only one player
			this.compEnabled = this.myPlayers == 1;
			if (compEnabled)
				this.compLevellabel.setForeground(Color.black);
			else
				this.compLevellabel.setForeground(Color.gray);

			for (int i = 0; i < 5; i++)
				this.myCompLevels[i].setEnabled(this.compEnabled);

			// Update number of enabled fields accordingly
			for (int i = 2; i < 12; i++)
			{
				if (!this.playerColorFields[i].getText().equals(""))
					this.playerColors[i] = this.playerColorFields[i].getText();
				if (i < this.myPlayers * 2)
					toggleField(i, true, this.playerColors[i]);
				else
					toggleField(i, false, "");
			}
		}

		// User changed the difficulty of the computer
		if (cmd.equals("comp0")
				|| cmd.equals("comp1")
				|| cmd.equals("comp2")
				|| cmd.equals("comp3")
				|| cmd.equals("comp4"))
		{
			this.myCompLevels[this.myCompLevel - 1].setSelected(false);
			this.myCompLevel = cmd.charAt(4) - '/';
			this.myCompLevels[this.myCompLevel - 1].setSelected(true);
		}

		// User selected "Hexagonal" or "Default" board type
		if (cmd.equals("boardType"))
		{
			if (this.myType == "hexagonal")
			{
				this.regButton.setSelected(true);
				this.regButton.removeActionListener(this);
				this.hexButton.setSelected(false);
				this.hexButton.addActionListener(this);
				this.myType = "default";
			}
			else
			{
				this.regButton.setSelected(false);
				this.regButton.addActionListener(this);
				this.hexButton.setSelected(true);
				this.hexButton.removeActionListener(this);
				this.myType = "hexagonal";
			}
		}

		// User started the game!
		if (cmd.equalsIgnoreCase("new"))
		{
			// If there's nothing in a field, use the default
			for (int i = 0; i < 12; i++)
			{
				if (this.playerColorFields[i].getText().equals(""))
					this.playerColorFields[i].setText(this.playerColors[i]);
			}

			// If we're playing against the computer, give it a color and name
			if (this.myPlayers == 1)
				this.myPlayers = 2;

			String[] names = new String[this.myPlayers];
			String[] colors = new String[this.myPlayers];
			for (int i = 0; i < 2 * this.myPlayers; i += 2)
			{
				names[i / 2] = this.playerColorFields[i].getText();
				colors[i / 2] = this.playerColorFields[i + 1].getText();
			}

			// Use 0 for complevel if we're not using a computer
			if (!this.compEnabled)
				this.myCompLevel = 0;

			// Initialize the game
			QuoridorGUIDriver pass = new QuoridorGUIDriver(names, colors, this.myType, this.boardSizeSlider.getValue(), this.myCompLevel);
			pass.pack();
			pass.setVisible(true);

			// Hide the input GUI
			setVisible(false);
		}
	}

	/**
	 * Turn on/off the text fields for name/color.
	 *
	 * Params:
	 *	 field = which field to turn on/off
	 *	 enable = on/off switch
	 *	 text = text to put in the field
	 */
	private void toggleField(int field, boolean enable, String text)
	{
		this.playerColorFields[field].setText(text);
		this.playerColorFields[field].setEditable(enable);
		this.playerColorFields[field].setFocusable(enable);
		if (enable)
			this.playerColorLabels[field].setForeground(Color.black);
		else
			this.playerColorLabels[field].setForeground(Color.gray);
	}

	/**
	 * Start it all.
	 */
	public static void main(String[] args)
	{
		InputGUIDriver input = new InputGUIDriver();
		input.pack();
		input.setVisible(true);
	}
}
