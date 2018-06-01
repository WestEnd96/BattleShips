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

public class BattleshipApp extends GameGrid
  implements GGMouseListener, GGExitListener, BtPeerListener, ActionListener
{
  private final static String title = "JGameGrid Battleship V2.0";
  protected volatile boolean isMyMove;
  protected String msgMyMove = "Click a cell to fire";
  protected String msgYourMove = "Please wait enemy bomb";
  protected volatile boolean isOver = false;
  private Location currentLoc;
  private final String serviceName = "Battleship";
  private BluetoothPeer bp;
  private Vokabelspiel vgame;
  private JDialog fenster;
  private JButton knopf;

  public BattleshipApp()
  {
    super(10, 10, 25, Color.black, null, false, 4);  // Only 4 rotated sprites
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    final int ulx = (dim.width - getWidth()) / 2 - 400;
    final int uly = (dim.height - getHeight()) / 2;
    setTitle(title);
    setBgColor(Color.blue);
    setSimulationPeriod(50);

    vgame = new Vokabelspiel("lang1.xml");
    
    Ship[] fleet =
    {
      new Carrier(),
      new Battleship(),
      new Destroyer(),
      new Submarine(),
      new PatrolBoat()
    };

    for (int i = 0; i < fleet.length; i++)
    {
      addActor(fleet[i], new Location(0, 2 * i));
      addMouseListener(fleet[i], GGMouse.lPress | GGMouse.lDrag | GGMouse.lRelease);
      addKeyListener(fleet[i]);
    }
    show();
    doRun();
    
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
    //getReady();
    vgame.play();
    connect();  // Blocks until connected
    addExitListener(this);
    addMouseListener(this, GGMouse.lPress);
  }

  public void actionPerformed(ActionEvent e) {
	  int i[] = new int[1];
	  
	  if(e.getSource() == knopf){
		  if(Vokabelspiel.Go) {
			  i[0] = 2;
			  bp.sendDataBlock(i);
			  i[0] = vgame.play();
			  bp.sendDataBlock(i);
		  }
		  else {
		  i[0] = 1;
          knopf.setText("Warte auf Ready-up");
          Vokabelspiel.Go = true;
          bp.sendDataBlock(i);
		  }
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

  protected void markLocation(int k)
  {
    switch (k)
    {
      case 0: // miss
        addActor(new Actor("sprites/miss.gif"), currentLoc);
        break;
      case 1: // hit
        addActor(new Actor("sprites/hit.gif"), currentLoc);
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
		    
		    fenster.setTitle("1 vs 1 Vokabelspiel");
		    // Breite und H�he des Fensters werden 
		    // auf 200 Pixel gesetzt
		   fenster.setSize(200,200);
		    // Dialog wird auf modal gesetzt
		   fenster.setModal(true);
		    // Wir lassen unseren Dialog anzeigen
		   fenster.add(knopf);
		   //knopf.addActionListener(this);
		   fenster.setVisible(true);   
  }
  
  public void receiveDataBlock(int[] data)
  {
	  if(data.length == 1) {
		  switch(data[0]) {
		  case 1:
			Vokabelspiel.Go = true;
		  	getReady();
		  	break;
		  case 2: 
			  data[0] = vgame.play();
			  bp.sendDataBlock(data);
			  break;
		  case 3: 
			  break;
		  }
		  }
	  else 
	  {
		  if (isMyMove)
		    {
		      markLocation(data[0]);
		      if (!isOver)
		      {
		        isMyMove = false;
		        setTitle(msgYourMove);
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

}