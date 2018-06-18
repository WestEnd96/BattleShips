// BattleshipApp.java
// package ch.aplu.bluetooth from www.aplu.ch
// package bluecove from www.bluecove.org

import ch.aplu.jgamegrid.*;
import java.awt.*;
import ch.aplu.util.*;
import ch.aplu.bluetooth.*;
import javax.swing.*;
import javax.xml.crypto.Data;
import java.awt.event.*;
import java.util.ArrayList;

public class BattleshipApp extends GameGrid
  implements GGMouseListener, GGExitListener, BtPeerListener, ActionListener, GGButtonListener
{
  private final static String title = "JGameGrid Battleship V2.0";
  protected volatile boolean isMyMove;
  protected String msgMyMove = "Click a cell to fire";
  protected String msgYourMove = "Please wait enemy bomb";
  protected volatile boolean isOver = false;
  public final int ulx,uly;
  private Location currentLoc;
  private final String serviceName = "Battleship";
  private BluetoothPeer bp;
  private final Color BLACK = java.awt.Color.BLACK;
  private final Color GRAY= java.awt.Color.GRAY;
  private final Color WHITE = java.awt.Color.WHITE;
  private Vokabelspiel vgame;
  private JDialog fenster;
  private JButton knopf;
  private JButton server = new JButton("Server starten");
  private JButton client = new JButton("Als Client starten");
  private JButton sprache = new JButton("Sprache wechseln");
  private JButton exit = new JButton("Exit");
  public String turns = "0";
  public int turnnumber = 0;
  public String hits = "0";
  public int hitnumber = 0;
  Font font = new Font("Serif", Font.BOLD, 18);

  public BattleshipApp()
  {
	    super(21, 20, 30, Color.black, null, false, 4);  // Only 4 rotated sprites
	    // setCellSize(40);
	     Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	     ulx = (dim.width - getWidth()) / 2 - 400;
	     uly = (dim.height - getHeight()) / 2;
	     setTitle(title);
	     setBgColor(Color.blue);
	     setSimulationPeriod(50);
	     
	  // Trennstrich
	     GGBackground bg =this.getBg();
	     bg.setPaintColor(BLACK);
	     bg.fillCell(new Location(10,0), BLACK);
	     bg.fillCell(new Location(10,1), BLACK);
	     bg.fillCell(new Location(10,2), BLACK);
	     bg.fillCell(new Location(10,3), BLACK);
	     bg.fillCell(new Location(10,4), BLACK);
	     bg.fillCell(new Location(10,5), BLACK);
	     bg.fillCell(new Location(10,6), BLACK);
	     bg.fillCell(new Location(10,7), BLACK);
	     bg.fillCell(new Location(10,8), BLACK);
	     bg.fillCell(new Location(10,9), BLACK);

    vgame = new Vokabelspiel("lang1.xml",ulx,uly);
    
    Ship[] fleet =
    {
      new Carrier(),
      new Battleship(),
      new Destroyer(),
      new Submarine(),
      new PatrolBoat()
    };
    Airforce[] airborne =
    {
      new Plane(),
      new Plane(),
      new Airship(),
      new Choppah(),
      new HeliumBalloon()
    };

    for (int i = 0; i < fleet.length; i++)
    {
      addActor(fleet[i], new Location(0, 2 * i));
      addMouseListener(fleet[i], GGMouse.lPress | GGMouse.lDrag | GGMouse.lRelease);
      addKeyListener(fleet[i]);
    }
    for (int i = 0; i < airborne.length; i++)
    {
      addActor(airborne[i], new Location(11, 2 * i));
      addMouseListener(airborne[i], GGMouse.lPress | GGMouse.lDrag | GGMouse.lRelease);
      addKeyListener(airborne[i]);
    }
    show();
    doRun();
    
    bg.setPaintColor(GRAY);
    for(int j=0;j<21; j++)  for(int jj=10;jj<20; jj++)  bg.fillCell(new Location(j,jj), GRAY );
    
    GGButton kamikaze = new GGButton("sprites/Destroyer.gif", false); 
    addActor(kamikaze, new Location(1,10));
    kamikaze.addButtonListener(this);
    
    StatusDialog status = new StatusDialog(ulx, uly, true);
    status.setText("Deploy your fleet now!\n" +
      "Use the red marker to drag the ship.\n" +
      "While dragging, press the cursor\nup/down key to rotate the ship.\n\n" +
      "Press 'Continue' to start the game.", true);
    Monitor.putSleep();  // Wait for dialog to be closed
    status.dispose();
    
    for (int i = 0; i < fleet.length; i++)
    {
      fleet[i].show(0);
      fleet[i].setMouseEnabled(false);
    }
    mainMenue();
    //connect();  // Blocks until connected
    addExitListener(this);
    addMouseListener(this, GGMouse.lPress);
  }
  
  public void actionPerformed(ActionEvent e) {
	  int data[] = new int[1];  
	  if(e.getSource() == knopf)
	  {
	     if(Vokabelspiel.Go) {
			  data[0] = 2;
			  bp.sendDataBlock(data);
			  fenster.dispose();
			  data[0] = vgame.play();
			  bp.sendDataBlock(data);
		  }
		  else {
		  data[0] = 1;
          knopf.setText("Warte auf Ready-up");
          Vokabelspiel.Go = true;
          bp.sendDataBlock(data);
		  }
      } 
	  if(e.getSource() == exit) {
		  System.exit(0);
	  }
	  if(e.getSource() == server) {
		  
	  }
	  if(e.getSource() == client) {
		  
	  }
	  if(e.getSource() == sprache) {
		vgame.menu();  
	  }
	}
  
  public boolean mouseEvent(GGMouse mouse)
  {
    setMouseEnabled(false);
    currentLoc = toLocationInGrid(mouse.getX(), mouse.getY());
    int[] data =
    {
      currentLoc.x, currentLoc.y
    };
    bp.sendDataBlock(data);
    return false;
  }
  
  void kamikazeClicked(GGButton kamikaze) 
  {
	  System.out.println("Check");
	  ArrayList<Actor> zeros = getActors(Plane.class);
	  Location loczero = zeros.get(0).getLocation();
	  int[] impact = 
			  {
					  loczero.x, loczero.y, 0
			  };
	  bp.sendDataBlock(impact);
  }

  protected void markLocation(int k)
  {
    switch (k)
    {
      case 0: // miss
        addActor(new Actor("sprites/miss.gif"), currentLoc);
        break;
      case 1: // hit
        addActor(new Actor("sprites/hit.gif"), currentLoc);
        hits();
        break;
      case 2: // sunk
        addActor(new Actor("sprites/sunk.gif"), currentLoc);
        break;
      case 3: // allsunk
        isOver = true;
        removeAllActors();
        addActor(new Actor("sprites/gameover.gif"), new Location(5, 2));
        addActor(new Actor("sprites/winner.gif"), new Location(5, 6));
        setTitle("Game over. You win.");
        setMouseEnabled(false);
        break;
    }
  }

  private void connect()
  {
    String prompt = "Enter Bluetooth Name";
    String serverName;
    do
    {
      serverName = JOptionPane.showInputDialog(null, prompt, "");
      if (serverName == null)
        System.exit(0);
    }
    while (serverName.trim().length() == 0);

    setTitle("Connecting to " + serverName);
    bp = new BluetoothPeer(serverName, serviceName, this, true);
    if (bp.isConnected())
    {
      setTitle("Connect OK. You shoot now");
      isMyMove = true;  // Client has first move
      //getReady();
    }
    else
      setTitle("Waiting as server " + BluetoothFinder.getLocalBluetoothName());
  }

  public void notifyConnection(boolean connected)
  {
    if (connected)
    {
      setTitle("Connect OK. Wait for shoot");
      isMyMove = false;  // Client has first move
    }
    else
    {
      setTitle("Connection lost");
      setMouseEnabled(false);
    }
  }
  
  public void getReady() {
		 fenster = new JDialog();
		 knopf = new JButton("Ready ?");
		 knopf.setSize(100,100);
		 knopf.addActionListener(this);
		 fenster.setLocation(ulx, uly);
		 fenster.setTitle("1 vs 1 Vokabelspiel");
		 fenster.setSize(200,200);
		 fenster.setModal(true);
		 fenster.add(knopf);
		 fenster.setVisible(true);   
  }
 
  public void receiveDataBlock(int[] data)
  {
	 /* if(data.length == 1) {
		  switch(data[0]) {
		  case 1:
			Vokabelspiel.Go = true;
		  	getReady();
		  	break;
		  case 2: 
			  fenster.dispose();
			  data[0] = vgame.play();
			  bp.sendDataBlock(data);
			  break;
		  case 3: 
			  StatusDialog win = new StatusDialog(ulx, uly, true);
			  win.setText("Sie haben gewonnen ! Hier ihr Preis ! ", true);
			  Monitor.putSleep();
			  win.dispose();  
			  data[0] = 4;
			  bp.sendDataBlock(data);
			  break;
		  case 4:
			  int[] vgameLocation = new int[4];
			  vgameLocation = getRandomShipLocation(2);
			  
			  break;
		  case 10:
			  //bp.sendDataBlock(kamikazeCliked());
			  break;
		  }
	  }
	  else 
	  {*/
	  
		  if (isMyMove)
		    {
		      markLocation(data[0]);
		      if (!isOver)
		      {
		        isMyMove = false;
		        setTitle(msgYourMove);
		        turns();
		      }
		    }
		    else
		    {
		      Location loc = new Location(data[0], data[1]);
		      int[] reply =
		      {
		        createReply(loc)
		      };
		      bp.sendDataBlock(reply);
		      if (!isOver)
		      {
		        isMyMove = true;
		        setTitle(msgMyMove);
		        setMouseEnabled(true);
		      }
		    }
	  }
  
  public void mainMenue() {
	  int buttonx = 200;
	  int buttony = 50;
	  	 fenster = new JDialog();
	  	 fenster.setLayout(null);
		 server.setSize(buttonx,buttony);
		 client.setSize(buttonx,buttony);
		 sprache.setSize(buttonx,buttony);
		 exit.setSize(buttonx,buttony);
		 server.setLocation(50, 0);
		 client.setLocation(50,50);
		 sprache.setLocation(50,100);
		 exit.setLocation(50, 150);
		 server.addActionListener(this);
		 client.addActionListener(this);
		 sprache.addActionListener(this);
		 exit.addActionListener(this);
		 fenster.setLocation(ulx, uly);
		 fenster.setTitle("1 vs 1 Vokabelspiel - Hauptmen�");
		 fenster.setSize(300,300);
		 fenster.setModal(false);
		 fenster.add(server);
		 fenster.add(client);
		 fenster.add(sprache);
		 fenster.add(exit);
		 fenster.setVisible(true);
  }

public String turns() {
	  ++turnnumber;
	  turns = Integer.toString(turnnumber);
	return turns;
	  }
  public String hits() {
	  ++hitnumber;
	  hits = Integer.toString(hitnumber);
	  return hits;
  }

  private int createReply(Location loc)
  {
    for (Actor a : getActors(Ship.class))
    {
      String s = ((Ship)a).hit(loc);
      if (s.equals("hit"))
        return 1;
      if (s.equals("sunk"))
        return 2;
      if (s.equals("allSunk"))
      {
        isOver = true;
        removeAllActors();
        addActor(new Actor("sprites/gameover.gif"), new Location(5, 2));
        addActor(new Actor("sprites/allsunk.gif"), new Location(5, 6));
        setTitle("Game over. You lost");
        return 3;
      }
    }
    // miss
    addActor(new Water(), loc);
    return 0;
  }

  public boolean notifyExit()
  {
    //bp.releaseConnection();
    return true;
  }

  public static void main(String[] args)
  {
    new BattleshipApp();
  }
  @Override
  public void buttonClicked(GGButton arg0) {
  	// TODO Auto-generated method stub
  	
  }
  @Override
  public void buttonPressed(GGButton arg0) {
  	// TODO Auto-generated method stub
  	
  }
  @Override
  public void buttonReleased(GGButton arg0) {
  	// TODO Auto-generated method stub
  	
  }

}