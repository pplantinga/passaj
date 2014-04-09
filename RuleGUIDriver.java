package passaj;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class RuleGUIDriver extends JFrame
  implements ActionListener, WindowListener
{
  private static final long serialVersionUID = 1234567890987654321L;
  private JTextArea myArea;
  private JButton myEnglish;
  private JButton myFrench;
  private JButton mySpanish;
  private JButton myDutch;
  private JButton myGerman;
  private JButton myItalian;
  private JPanel myPanel;
  private Board myBoard = new Board();
  private HexBoard myHexBoard = new HexBoard();
  private boolean myHex = false;

  public RuleGUIDriver()
  {
    initialize();
  }

  public RuleGUIDriver(Board board)
  {
    initialize();
    this.myBoard = board;
    this.myHex = false;
  }

  public RuleGUIDriver(HexBoard hexBoard)
  {
    initialize();
    this.myHexBoard = hexBoard;
    this.myHex = true;
  }

  public void initialize()
  {
    this.myArea = new JTextArea();
    this.myArea.setText("\nObject of the game:  get to the other side before your opponent gets to yours.\n\n\nMovement:  You may move one unit into an adjacent space\nif there is not a wall in the way or place one wall per turn.\n\nYou may jump your opponent if there isn't a wall in the way.\n\nIf there is a wall behind your opponent and he is in your way,\nyou may move one space to either side of your opponent if there isn't a wall in the way.\n\nYou may not have walls cross and you may not entirely cut your opponent off from the other side.\n\n\nHave a good time playing PASSAJ!!!  \n");

    this.myArea.setEditable(false);
    this.myEnglish = new JButton("English");
    this.myEnglish.addActionListener(this);
    this.myEnglish.setActionCommand("English");
    this.myFrench = new JButton("French");
    this.myFrench.addActionListener(this);
    this.myFrench.setActionCommand("French");
    this.mySpanish = new JButton("Spanish");
    this.mySpanish.addActionListener(this);
    this.mySpanish.setActionCommand("Spanish");
    this.myDutch = new JButton("Dutch");
    this.myDutch.addActionListener(this);
    this.myDutch.setActionCommand("Dutch");
    this.myGerman = new JButton("German");
    this.myGerman.addActionListener(this);
    this.myGerman.setActionCommand("German");
    this.myItalian = new JButton("Italian");
    this.myItalian.addActionListener(this);
    this.myItalian.setActionCommand("Italian");
    this.myPanel = new JPanel();
    add(this.myArea, "South");
    add(this.myPanel, "North");
    this.myPanel.setLayout(new FlowLayout());

    this.myPanel.add(this.myEnglish);
    this.myPanel.add(this.myFrench);
    this.myPanel.add(this.mySpanish);
    this.myPanel.add(this.myDutch);
    this.myPanel.add(this.myGerman);
    this.myPanel.add(this.myItalian);
    setDefaultCloseOperation(0);
    addWindowListener(this);
  }

  public static void main(String[] args)
  {
    RuleGUIDriver rules = new RuleGUIDriver();
    rules.pack();
    rules.setVisible(true);
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equalsIgnoreCase("english")) {
      this.myArea
        .setText("\nObject of the game:  Destroy the opponent.  In order to do this, you get to the other side before he gets to yours.\n\n\nMovement:  You may move one space orthoganally\nif there is not a wall in the way or place one wall per turn.\n\nYou may jump your opponent if there isn't a wall in the way.\n\nIf there is a wall behind your opponent and he is in your way,\nyou may move one space to either side of your opponent if there isn't a wall in the way.\n\nYou may not have walls cross and you may not entirely cut your opponent off from the other side.\n\n\nHave a good time playing PASSAJ!!!  \n");
    }

    if (e.getActionCommand().equalsIgnoreCase("french")) {
      this.myArea
        .setText("\nObjet du jeu: Détruire l'adversaire. Pour ce faire, vous obtenez de l'autre côté avant qu'il arrive à la vôtre.\n\n\nMouvement: Vous mai déplacer d'un espace horizontalement ou verticalement\ns'il n'y a pas de mur dans la manière ou le lieu d'un mur par tour.\n\nVous mai sauter de votre adversaire si il n'y a pas de mur dans le cours.\n\nS'il ya un mur derrière votre adversaire et il est sur votre chemin,\nMai vous déplacer d'un espace de chaque côté de votre adversaire si il n'y a pas de mur dans le cours.\n\nVous mai pas traverser les murs et mai de ne pas vous couper totalement de votre adversaire hors de l'autre côté.\n\n\nAvoir un bon temps à jouer PASSAJ! \n");
    }

    if (e.getActionCommand().equalsIgnoreCase("spanish")) {
      this.myArea
        .setText("\nObjeto del juego: destruir al adversario. Con el fin de hacer esto, llegar a la otra parte antes de que llegue a los suyos.\n\n\nMovimiento: Puede cambiar de un espacio horizontal o vertical\nsi no hay un muro en el camino o el lugar un muro por turno.\n\nPuede saltar de tu oponente si no hay un muro en el camino.\n\nSi hay una pared detrás de su oponente y se encuentra en su camino,\nPuede pasar un espacio a cada lado de tu oponente si no hay un muro en el camino.\n\nPuede que no tenga paredes de cruz y no puede reducir totalmente a su oponente desde el otro lado.\n\n\nTener un buen momento de juego PASSAJ! \n");
    }

    if (e.getActionCommand().equalsIgnoreCase("dutch")) {
      this.myArea
        .setText("\nDoel van het spel: Vernietig de tegenstander. Om dit mogelijk te maken, krijg je aan de andere kant krijgt voordat hij naar de uwe.\n\n\nVerandering: U kunt een ruimte horizontaal of verticaal\nals er niet een muur in de weg of plaats een muur per beurt.\n\nJe mag je tegenstander springen als er niet een muur in de weg.\n\nAls er een muur achter je tegenstander en is hij in de weg staat,\nJe mag verplaatsen naar een ruimte aan beide kanten van je tegenstander als er niet een muur in de weg.\n\nJe mag geen muren kruis en mag u geen gesneden volkomen je tegenstander uitschakelen van de andere kant.\n\n\nHeb een goede tijd aan het spel PASSAJ! \n");
    }

    if (e.getActionCommand().equalsIgnoreCase("german")) {
      this.myArea
        .setText("\nZiel des Spiels: Zerstören Sie die Gegner. Um dies zu tun, erhalten Sie auf der anderen Seite, bevor er zu Ihnen.\n\n\nBewegung: Sie bewegen Mai ein Raum horizontal oder vertikal,\nwenn es nicht eine Mauer in den Weg oder Platz eins Wand pro Umdrehung.\n\nSie können springen, wenn dein Gegner gibt es nicht eine Mauer in den Weg.\n\nWenn es eine Mauer hinter deinen Gegner und er ist in Ihrer Art,\nSie bewegen Mai ein Raum zu beiden Seiten des Gegners, wenn es nicht eine Mauer in den Weg.\n\nSie dürfen nicht zur Folge haben Wände Kreuz und Sie können nicht völlig entzieht dein Gegner aus der anderen Seite.\n\n\nWir wünschen Ihnen eine gute Zeit spielen PASSAJ! \n");
    }

    if (e.getActionCommand().equalsIgnoreCase("italian")) {
      this.myArea
        .setText("\nScopo del gioco: distruggere l'avversario. A tal fine, si arriva a lato prima che raggiungano i suoi.\n\n\nMovimento: È possibile spostare uno spazio orizzontalmente o verticalmente\nse non c'è un muro nel modo in cui uno o luogo parete per turno.\n\nÈ possibile saltare il tuo avversario se non c'è un muro nel modo.\n\nSe c'è un muro dietro il tuo avversario e lui è nel tuo modo,\nÈ possibile spostare uno spazio per entrambi i lati del tuo avversario se non c'è un muro nel modo.\n\nLei non può avere pareti croce e non si può tagliare completamente il tuo avversario fuori dal lato.\n\n\nHa un buon tempo giocando PASSAJ! \n");
    }

    setSize(new Dimension(876, 345));
  }

  public void windowActivated(WindowEvent e) {
  }

  public void windowClosed(WindowEvent e) {
  }

  public void windowClosing(WindowEvent e) {
    setVisible(false);
    if (this.myHex) {
      QuoridorGUIDriver hex = new QuoridorGUIDriver(this.myHexBoard);
      hex.pack();
      hex.setVisible(true);
    } else {
      QuoridorGUIDriver pass = new QuoridorGUIDriver(this.myBoard);
      pass.pack();
      pass.setVisible(true);
    }
  }

  public void windowDeactivated(WindowEvent e)
  {
  }

  public void windowDeiconified(WindowEvent e)
  {
  }

  public void windowIconified(WindowEvent e)
  {
  }

  public void windowOpened(WindowEvent e)
  {
  }
}
