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
import javax.swing.JTextField;

public class InputGUIDriver extends JFrame
  implements ActionListener
{
  private static final long serialVersionUID = 1234567890123456789L;
  private JButton theBigRedButton;
  private JButton theBigBlueButton;
  private JButton hexButton;
  private JButton regButton;
  private JLabel backgroundlabel;
  private JLabel wallNumberlabel;
  private JLabel sizelabel;
  private JLabel playerNumberlabel;
  private JLabel compLevellabel;
  private JTextField backgroundfield;
  private JTextField wallNumberfield;
  private JTextField sizefield;
  private JRadioButton[] myPlay = new JRadioButton[5]; private JRadioButton[] myCompLevels = new JRadioButton[5];
  private JLabel[] playerColorLabels = new JLabel[12];
  private JTextField[] playerColorFields = new JTextField[12];
  private String[] playerColors = { "Peter Plantinga", "Gold", "Red Guy", "Dark Red", "Blue Guy", "Light Blue", "Green Guy", 
    "Light Green", "Orange Guy", "Orange", "Pink Guy", "Pink" };

  private boolean myHex = false;
  private JPanel playersPanel;
  private JPanel compPanel;
  private int myPlayers = 1; private int myCompLevel = 1;

  public InputGUIDriver()
  {
    initialize();
    this.backgroundfield.setText("Gray");
    this.wallNumberfield.setText("default");
    this.sizefield.setText("9");
  }

  public InputGUIDriver(String[] names, String[] colors, String boardType, int size, int compLevel, String wallNum)
  {
    initialize();
    if (names[1].equals("THE DOMINATOR")) {
      this.myPlayers = 1;
      if (compLevel != 1) {
        this.myCompLevels[0].setSelected(false);
        this.myCompLevels[(compLevel - 1)].setSelected(true);
        this.myCompLevel = compLevel;
      }
    } else {
      this.myPlayers = names.length;
      if (this.myPlayers != 6) {
        this.myPlay[(this.myPlayers - 1)].setSelected(true);
        this.myPlay[0].setSelected(false);
      }
      for (int i = 0; i < 5; i++) {
        this.myCompLevels[i].setEnabled(false);
      }
      this.compLevellabel.setForeground(Color.gray);
    }
    if (boardType.equals("hexagonal")) {
      this.regButton.setSelected(false);
      this.regButton.addActionListener(this);
      this.hexButton.setSelected(true);
      this.hexButton.removeActionListener(this);
      this.myHex = true;
    }
    this.backgroundfield.setText("Gray");
    this.wallNumberfield.setText(wallNum);
    this.sizefield.setText(Integer.toString(size));
    for (int i = 0; i < 12; i += 2)
      if (i < this.myPlayers * 2) {
        this.playerColorFields[i].setText(names[(i / 2)]);
        this.playerColorFields[i].setEditable(true);
        this.playerColorFields[i].setFocusable(true);
        this.playerColorLabels[i].setForeground(Color.black);
        this.playerColorFields[(i + 1)].setText(colors[(i / 2)]);
        this.playerColorFields[(i + 1)].setEditable(true);
        this.playerColorFields[(i + 1)].setFocusable(true);
        this.playerColorLabels[(i + 1)].setForeground(Color.black);
      } else {
        this.playerColorFields[i].setEditable(false);
        this.playerColorFields[i].setFocusable(false);
        this.playerColorFields[i].setText("");
        this.playerColorLabels[i].setForeground(Color.gray);
        this.playerColorFields[(i + 1)].setEditable(false);
        this.playerColorFields[(i + 1)].setFocusable(false);
        this.playerColorFields[(i + 1)].setText("");
        this.playerColorLabels[(i + 1)].setForeground(Color.gray);
      }
  }

  public void initialize()
  {
    setTitle("Opening Screen");
    this.playerNumberlabel = new JLabel("Number of players: ");
    for (int i = 1; i <= 12; i += 2) {
      this.playerColorLabels[(i - 1)] = new JLabel("Enter Player " + (i + 1) / 2 + " name: ");
      this.playerColorLabels[i] = new JLabel("Enter Player " + (i + 1) / 2 + " color: ");
    }
    this.playersPanel = new JPanel();
    this.playersPanel.setLayout(new GridLayout(1, 5));
    for (int i = 0; i <= 4; i++)
    {
      int j;
      if (i == 4)
        j = 6;
      else
        j = i + 1;
      this.myPlay[i] = new JRadioButton(Integer.toString(j));
      this.myPlay[i].setFocusable(false);
      this.myPlay[i].setActionCommand(new Integer(i).toString());
      this.myPlay[i].addActionListener(this);
      this.playersPanel.add(this.myPlay[i]);
      if (i == 0)
        this.myPlay[i].setSelected(true);
    }
    this.compLevellabel = new JLabel("Enter the level of the computer");
    this.compPanel = new JPanel();
    this.compPanel.setLayout(new GridLayout(1, 5));
    for (int i = 0; i <= 4; i++) {
      this.myCompLevels[i] = new JRadioButton(Integer.toString(i + 1));
      this.myCompLevels[i].setActionCommand("comp" + i);
      this.myCompLevels[i].setFocusable(false);
      this.myCompLevels[i].addActionListener(this);
      this.compPanel.add(this.myCompLevels[i]);
      if (i == 0)
        this.myCompLevels[i].setSelected(true);
    }
    this.backgroundlabel = new JLabel("Enter Background color: ");
    this.wallNumberlabel = new JLabel("Enter Number of Walls: ");
    this.sizelabel = new JLabel("Enter Size of Board: ");
    for (int i = 1; i <= 12; i += 2) {
      this.playerColorFields[(i - 1)] = new JTextField();
      this.playerColorFields[i] = new JTextField();
      if (i >= 3) {
        this.playerColorFields[(i - 1)].setEditable(false);
        this.playerColorFields[i].setEditable(false);
        this.playerColorFields[(i - 1)].setFocusable(false);
        this.playerColorFields[i].setFocusable(false);
        this.playerColorLabels[(i - 1)].setForeground(Color.gray);
        this.playerColorLabels[i].setForeground(Color.gray);
      }
    }
    this.playerColorFields[0].setText(this.playerColors[0]);
    this.playerColorFields[1].setText(this.playerColors[1]);
    this.regButton = new JButton("Regular Board");
    this.regButton.setActionCommand("boardType");
    this.regButton.setFocusable(false);
    this.regButton.setSelected(true);
    this.hexButton = new JButton("Hexagonal Board");
    this.hexButton.addActionListener(this);
    this.hexButton.setActionCommand("boardType");
    this.hexButton.setFocusable(false);
    this.backgroundfield = new JTextField(8);
    this.wallNumberfield = new JTextField(2);
    this.sizefield = new JTextField(2);
    this.sizefield.addActionListener(this);
    this.theBigRedButton = new JButton("Quit");
    this.theBigRedButton.addActionListener(this);
    this.theBigRedButton.setActionCommand("quit");
    this.theBigRedButton.setFocusable(false);
    this.theBigBlueButton = new JButton("PLAY PASSAJ!!!");
    this.theBigBlueButton.addActionListener(this);
    this.theBigBlueButton.setActionCommand("new");
    this.theBigBlueButton.setFocusable(false);
    setLayout(new GridLayout(19, 2));
    add(this.regButton);
    add(this.hexButton);
    add(this.playerNumberlabel);
    add(this.playersPanel);
    add(this.compLevellabel);
    add(this.compPanel);
    for (int i = 0; i < 12; i++) {
      add(this.playerColorLabels[i]);
      add(this.playerColorFields[i]);
    }
    add(this.backgroundlabel);
    add(this.backgroundfield);
    add(this.sizelabel);
    add(this.sizefield);
    add(this.wallNumberlabel);
    add(this.wallNumberfield);
    add(this.theBigRedButton);
    add(this.theBigBlueButton);
    setDefaultCloseOperation(3);
  }

  public void actionPerformed(ActionEvent arg0) {
    if (arg0.getActionCommand().equalsIgnoreCase("Quit")) {
      System.exit(128);
    }
    if ((arg0.getActionCommand().equals("0")) || (arg0.getActionCommand().equals("1")) || (arg0.getActionCommand().equals("2")) || 
      (arg0.getActionCommand().equals("3")) || (arg0.getActionCommand().equals("4"))) {
      if (this.myPlayers != 6)
        this.myPlay[(this.myPlayers - 1)].setSelected(false);
      else
        this.myPlay[4].setSelected(false);
      this.myPlay[Integer.parseInt(arg0.getActionCommand())].setFocusable(false);
      this.myPlay[Integer.parseInt(arg0.getActionCommand())].setSelected(true);
      if (Integer.parseInt(arg0.getActionCommand()) == 4)
        this.myPlayers = 6;
      else {
        this.myPlayers = (Integer.parseInt(arg0.getActionCommand()) + 1);
      }
      if ((this.myPlayers == 3) || (this.myPlayers == 6)) {
        this.regButton.setSelected(false);
        this.regButton.setEnabled(false);
        this.hexButton.setSelected(true);
        this.hexButton.removeActionListener(this);
        this.myHex = true;
      } else if (!this.regButton.isEnabled()) {
        this.regButton.setEnabled(true);
        this.regButton.removeActionListener(this);
        this.regButton.addActionListener(this);
      }

      for (int i = 0; i <= 4; i++) {
        if (this.myPlayers == 1) {
          this.compLevellabel.setForeground(Color.black);
          this.myCompLevels[i].setEnabled(true);
        } else {
          this.compLevellabel.setForeground(Color.gray);
          this.myCompLevels[i].setEnabled(false);
        }
      }

      for (int i = 2; i < 12; i++) {
        if (!this.playerColorFields[i].getText().equals(""))
          this.playerColors[i] = this.playerColorFields[i].getText();
        if (i < this.myPlayers * 2) {
          this.playerColorFields[i].setText(this.playerColors[i]);
          this.playerColorFields[i].setEditable(true);
          this.playerColorFields[i].setFocusable(true);
          this.playerColorLabels[i].setForeground(Color.black);
        } else {
          this.playerColorFields[i].setEditable(false);
          this.playerColorFields[i].setText("");
          this.playerColorFields[i].setFocusable(false);
          this.playerColorLabels[i].setForeground(Color.gray);
        }
      }
    }

    if ((arg0.getActionCommand().equals("comp0")) || (arg0.getActionCommand().equals("comp1")) || 
      (arg0.getActionCommand().equals("comp2")) || (arg0.getActionCommand().equals("comp3")) || (arg0.getActionCommand().equals("comp4"))) {
      this.myCompLevels[(this.myCompLevel - 1)].setSelected(false);
      this.myCompLevel = (arg0.getActionCommand().charAt(4) - '/');
      this.myCompLevels[(this.myCompLevel - 1)].setSelected(true);
    }

    if (arg0.getActionCommand().equals("boardType")) {
      if (this.myHex) {
        this.regButton.setSelected(true);
        this.regButton.removeActionListener(this);
        this.hexButton.setSelected(false);
        this.hexButton.addActionListener(this);
        this.myHex = false;
      } else {
        this.regButton.setSelected(false);
        this.regButton.addActionListener(this);
        this.hexButton.setSelected(true);
        this.hexButton.removeActionListener(this);
        this.myHex = true;
      }
    }

    if (arg0.getActionCommand().equalsIgnoreCase("New")) {
      for (int i = 0; i < 12; i++) {
        if (this.playerColorFields[i].getText().equals(""))
          this.playerColorFields[i].setText(this.playerColors[i]);
      }
      if (this.backgroundfield.getText().equals(""))
        this.backgroundfield.setText("gray");
      if (this.wallNumberfield.getText().equals(""))
        this.wallNumberfield.setText("default");
      if (this.sizefield.getText().equals("")) {
        this.sizefield.setText("9");
      }
      String[] names = new String[this.myPlayers];
      String[] colors = new String[this.myPlayers];
      for (int i = 0; i < 2 * this.myPlayers; i += 2) {
        names[(i / 2)] = this.playerColorFields[i].getText();
        colors[(i / 2)] = this.playerColorFields[(i + 1)].getText();
      }

      if (this.myHex) {
        QuoridorGUIDriver pass = new QuoridorGUIDriver(names, colors, this.backgroundfield.getText(), 
          this.wallNumberfield.getText(), Integer.parseInt(this.sizefield.getText()) - Integer.parseInt(this.sizefield.getText()) % 4 + 1, this.myCompLevel);
        pass.pack();
        pass.setVisible(true);
        setVisible(false);
      } else {
        QuoridorGUIDriver pass = new QuoridorGUIDriver(names, colors, this.backgroundfield.getText(), 
          Integer.parseInt(this.sizefield.getText()), this.wallNumberfield.getText(), this.myCompLevel);
        pass.pack();
        pass.setVisible(true);
        setVisible(false);
      }
    }
  }

  public static void main(String[] args)
  {
    InputGUIDriver input = new InputGUIDriver();
    input.pack();
    input.setVisible(true);
  }
}
