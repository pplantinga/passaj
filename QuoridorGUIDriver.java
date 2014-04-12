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

public class QuoridorGUIDriver extends JFrame
  implements ActionListener, MouseListener, MouseMotionListener
{
  private static final long serialVersionUID = 3141592653589793238L;
  private Board myBoard;
  private JTextField myField;
  private JButton myButton;
  private JButton ruleButton;
  private JButton quitButton;
  private JPanel myPanel;
  private HexBoard myHexBoard;
  private boolean myHex;

  public QuoridorGUIDriver()
  {
    this.myBoard = new Board();
    this.myHex = false;
    initialize();
  }

  public QuoridorGUIDriver(Board board)
  {
    this.myBoard = board;
    this.myHex = false;
    initialize();
  }

  public QuoridorGUIDriver(HexBoard hexBoard)
  {
    this.myHexBoard = hexBoard;
    this.myHex = true;
    initialize();
  }

  public QuoridorGUIDriver(String[] names, String[] colors, int size, int compLevel, String type)
  {
		if (type == "Hexagonal")
		{
			this.myHexBoard = new HexBoard(names, colors, size, compLevel);
			this.myHex = true;
		}
		else
		{
			this.myBoard = new Board(names, colors, size, compLevel);
			this.myHex = false;
		}
    initialize();
  }

  public void initialize()
  {
    setTitle("TERRIFICAL PASSAJ!");
    if (this.myHex) {
      this.myHexBoard.addMouseListener(this);
      this.myHexBoard.addMouseMotionListener(this);
    } else {
      this.myBoard.addMouseListener(this);
      this.myBoard.addMouseMotionListener(this);
    }
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
    this.myPanel = new JPanel();
    this.myPanel.add(this.ruleButton, "East");
    this.myPanel.add(this.quitButton, "Center");
    this.myPanel.add(this.myButton, "West");
    setLayout(new BorderLayout());
    if (this.myHex)
      add(this.myHexBoard, "North");
    else
      add(this.myBoard, "North");
    add(this.myPanel, "South");
    add(this.myField, "Center");
    setDefaultCloseOperation(3);
    String openText = "Welcome to the match-up between ";
    if (this.myHex) {
      for (int i = 1; i < this.myHexBoard.getPlayers(); i++)
        openText = openText + this.myHexBoard.getName(i) + " and ";
      openText = openText + this.myHexBoard.getName(this.myHexBoard.getPlayers()) + ".";
    } else {
      for (int i = 1; i < this.myBoard.getPlayers(); i++)
        openText = openText + this.myBoard.getName(i) + " and ";
      openText = openText + this.myBoard.getName(this.myBoard.getPlayers()) + ".";
    }
    this.myField.setText(openText);
  }

  public static void main(String[] args)
  {
    QuoridorGUIDriver pass = new QuoridorGUIDriver();
    pass.pack();
    pass.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("new game")) {
      if (this.myHex) {
        InputGUIDriver input = new InputGUIDriver(this.myHexBoard.getNames(), this.myHexBoard.getColors(), "hexagonal", this.myHexBoard.getBoardSize(), this.myHexBoard.getCompLevel());
        input.pack();
        input.setVisible(true);
        setVisible(false);
      } else {
        InputGUIDriver input = new InputGUIDriver(this.myBoard.getNames(), this.myBoard.getColors(), "default", this.myBoard.getBoardSize(), this.myBoard.getCompLevel());
        input.pack();
        input.setVisible(true);
        setVisible(false);
      }
    }

    if (e.getActionCommand().equals("rules")) {
      if (this.myHex) {
        RuleGUIDriver hexRule = new RuleGUIDriver(this.myHexBoard);
        hexRule.pack();
        hexRule.setVisible(true);
        setVisible(false);
      } else {
        RuleGUIDriver rule = new RuleGUIDriver(this.myBoard);
        rule.pack();
        rule.setVisible(true);
        setVisible(false);
      }
    }
    if (e.getActionCommand().equals("quit"))
      System.exit(128);
  }

  public void mouseClicked(MouseEvent e)
  {
    if (this.myHex)
      try {
        if (!this.myHexBoard.isEven(this.myHexBoard.yPosToElement(e.getX(), e.getY())))
          this.myHexBoard.changePos(this.myHexBoard.xPosToElement(e.getX()), this.myHexBoard.yPosToElement(e.getX(), e.getY()));
        if (this.myHexBoard.isEven(this.myHexBoard.yPosToElement(e.getX(), e.getY())))
          this.myHexBoard.addWall(8, this.myHexBoard.xPosToElement(e.getX()), this.myHexBoard.yPosToElement(e.getX(), e.getY()), false);
        if ((this.myHexBoard.getComp()) && (this.myHexBoard.isEven(this.myHexBoard.getTurn()))) {
          this.myHexBoard.moveComp(2);
        }
        String fieldText = "";
        for (int i = 1; i <= this.myHexBoard.getPlayers(); i++)
          fieldText = fieldText + this.myHexBoard.getName(i) + " " + this.myHexBoard.getWallCount(i) + " ";
        fieldText = fieldText + "  walls left.  Player's Turn: " + this.myHexBoard.getName(this.myHexBoard.getPlayer());
        this.myField.setText(fieldText);
      }
      catch (Exception er) {
        for (int i = 1; i <= this.myHexBoard.getPlayers(); i++)
          if (er.getMessage().equals(this.myHexBoard.getName(i) + " WINS!!")) {
            this.myHexBoard.removeMouseListener(this);
            this.myHexBoard.removeMouseMotionListener(this);
            this.ruleButton.removeActionListener(this);
          }
        this.myField.setText(er.getMessage());
      }
    else
      try {
        if (this.myBoard.isHorizontal(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY())))
          this.myBoard.addWall(6, this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()), this.myBoard
            .isTopLeftHalf(e.getX()));
        else if (this.myBoard.isVertical(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY())))
          this.myBoard.addWall(6, this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()), this.myBoard
            .isTopLeftHalf(e.getY()));
        else if ((!this.myBoard.isEven(this.myBoard.posToElement(e.getX()))) && (!this.myBoard.isEven(this.myBoard.posToElement(e.getY()))))
          this.myBoard.changePos(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()));
        this.myBoard.repaint();

        if ((this.myBoard.getComp()) && (this.myBoard.getPlayer() == 2)) {
          this.myBoard.moveComp(2);
        }
        String fieldText = "";
        for (int i = 1; i <= this.myBoard.getPlayers(); i++)
          fieldText = fieldText + this.myBoard.getName(i) + " " + this.myBoard.getWallCount(i) + " ";
        fieldText = fieldText + "  walls left.  Player's Turn: " + this.myBoard.getName(this.myBoard.getPlayer());
        this.myField.setText(fieldText);
      }
      catch (Exception er) {
        this.myField.setText(er.getMessage());
        for (int i = 1; i <= this.myBoard.getPlayers(); i++)
          if (er.getMessage().equals(this.myBoard.getName(i) + " WINS!!")) {
            this.myBoard.removeMouseListener(this);
            this.myBoard.removeMouseMotionListener(this);
            this.ruleButton.removeActionListener(this);
          }
      }
  }

  public void mouseMoved(MouseEvent e)
  {
    if (this.myHex) {
      this.myHexBoard.removeTemp();
      try {
        if (this.myHexBoard.isEven(this.myHexBoard.yPosToElement(e.getX(), e.getY())))
          this.myHexBoard.addWall(9, this.myHexBoard.xPosToElement(e.getX()), this.myHexBoard.yPosToElement(e.getX(), e.getY()), false);
        else if (!this.myHexBoard.isEven(this.myHexBoard.yPosToElement(e.getX(), e.getY())))
          this.myHexBoard.tempPos(this.myHexBoard.xPosToElement(e.getX()), this.myHexBoard.yPosToElement(e.getX(), e.getY()));
      } catch (Exception localException) {
      }
    } else {
      this.myBoard.removeTemp();
      try {
        if (this.myBoard.isHorizontal(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY())))
          this.myBoard.addWall(7, this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()), this.myBoard
            .isTopLeftHalf(e.getX()));
        else if (this.myBoard.isVertical(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY())))
          this.myBoard.addWall(7, this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()), this.myBoard
            .isTopLeftHalf(e.getY()));
        else if ((!this.myBoard.isEven(this.myBoard.posToElement(e.getX()))) && (!this.myBoard.isEven(this.myBoard.posToElement(e.getY()))))
          this.myBoard.tempPos(this.myBoard.posToElement(e.getX()), this.myBoard.posToElement(e.getY()));
      }
      catch (Exception localException1)
      {
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
