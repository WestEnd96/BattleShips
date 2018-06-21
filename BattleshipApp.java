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
import java.util.Random;

public class BattleshipApp extends GameGrid
  implements GGMouseListener, GGExitListener, BtPeerListener, ActionListener, GGButtonListener
{
  private final static String title = "JGameGrid Battleship V2.0";
  protected volatile boolean isMyMove;
  public boolean vgameplay = false;
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
  public int vgameplayround=0;
  int score = 0, points = 0, bonus = 0;
  Ship[] fleet;
  Airforce[] airborne;
  Actor turncounter = new TextActor("Turns:" + " " + turns, BLACK, WHITE, font);
  Actor hitcounter = new TextActor("Hits:" + "" + hits, BLACK, WHITE, font);
  Actor highscore = new TextActor("Score" + Integer.toString(score), java.awt.Color.BLACK, java.awt.Color.WHITE, this.font);

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
    
    addActor(turncounter, new Location(1,11));
    addActor(hitcounter, new Location(1,12));
    addActor(highscore, new Location(1,13));
    
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
   /* int[] data = new int[1];
    data[0] = 4;
    receiveDataBlock(data);*/
    mainMenue();
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
		  fenster.dispose();
		  connect();  // Blocks until connected

	  }
	  if(e.getSource() == client) {
		  fenster.dispose();
		  connect();  // Blocks until connected
	  }
	  if(e.getSource() == sprache) {
		Vokabelspiel.menu();  
	  }
	}
  
  public boolean mouseEvent(GGMouse mouse)
  {
		if (currentLoc.y < 11) {
		    setMouseEnabled(false);
		    currentLoc = toLocationInGrid(mouse.getX(), mouse.getY());
		    int[] data =
		    {
		      currentLoc.x, currentLoc.y
		    };
		    bp.sendDataBlock(data);
		    return false;
			}
			if (currentLoc.y < 10) {
				return false;
			}
			return true;
  }
  
  void kamikazeClicked(GGButton kamikaze) 
  {
	  
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
	vgame = new Vokabelspiel(ulx,uly);  
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
	  if(data.length == 1) {
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
			  StatusDialog loss = new StatusDialog(ulx, uly, true);
			  loss.setText("Sie haben verloren ! Opfer m�ssen gebracht werden ! ", true);
			  Monitor.putSleep();
			  loss.dispose();  
			  int s = getNumberOfActors(Ship.class);
			  int a = getNumberOfActors(Airforce.class);
			  int[] senden = new int[2];
			  if(s>a) {
				  ArrayList<Actor> Fleet = getActors(Ship.class);
				  Location loc = Fleet.get((int)(Math.random()*s)).getLocation();
				  createReply(loc); 
				  senden[0] = loc.x;
				  senden[1] = loc.y;
			  }
			  else {
				  ArrayList<Actor> Fleet = getActors(Airforce.class);
				  Location loc = Fleet.get((int)(Math.random()*a)).getLocation();
				  createReply(loc); 
				  senden[0] = loc.x;
				  senden[1] = loc.y;
			  }
			  bp.sendDataBlock(senden);
			  
			  break;
		  }
	  }
	  else if (data.length == 4) { 
		  int n = getNumberOfActors(Ship.class) + 1;
		  Random target = new Random();
		  int t = target.nextInt(n);
		  ArrayList<Actor> Fleet = getActors(Ship.class);
		  createReply(Fleet.get(0).getLocation());
		  
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
		        turns();
		        if(turnnumber%10 != 0 && !vgameplay) {
		        	vgameplayround = (int)Math.random()*10;
		        }
		        if(turnnumber == vgameplayround) {
		        	getReady();
		        	vgameplay = true;
		        }
		        if(turnnumber % 10 == 0) {
		        	vgameplay = false;
		        }
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
  
  public void turns() {
	  ++turnnumber;
	  turns = Integer.toString(turnnumber);
	  Actor turncounter = new TextActor("Turns:" + " " + turns, BLACK, WHITE, font);
	    addActor(turncounter, new Location(1,11));
	  }
  public void hits() {
	  ++hitnumber;
	  hits = Integer.toString(hitnumber);
	  Actor hitcounter = new TextActor("Hits:" + " " + hits, BLACK, WHITE, font);
	    addActor(hitcounter, new Location(1,12));
	    Scoreboard();
  }
  public void Scoreboard() {
		if (turnnumber <= 10) {
			points =  10;
		}
		if (turnnumber > 10 && turnnumber <= 20) {
			points = 5;
		}
		if (turnnumber > 20) {
			points = 2;
		}
		bonus = getBonus();
		score = score + points + bonus;
		Actor highscore = new TextActor("Score" + Integer.toString(score), java.awt.Color.BLACK, java.awt.Color.WHITE, this.font);
		addActor(highscore, new Location(1,13));
	}
  
  public int getBonus() {
	  ArrayList<Actor> sunken = getActors();
	  bonus = (sunken.size() - fleet.length + airborne.length) * 20;
	return bonus;
	  
  }
  private int createReply(Location loc)
  {
	  if (loc.x < 11) {
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
	    } if (loc.x > 10) {
	    	 for (Actor a : getActors(Airforce.class))
	    	    {
	    	      String s = ((Airforce)a).hit(loc);
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
	    }
	    // miss
	    addActor(new Water(), loc);
	    return 0;
  }

  public boolean notifyExit()
  {
    bp.releaseConnection();
    return true;
  }

  public static void main(String[] args)
  {
    new BattleshipApp();
  }
  @Override

public void buttonClicked(GGButton kamikaze) {
	if (isMyMove = true) {
		System.out.println("Check");
		ArrayList<Actor> zeros = getActors(Plane.class);
		Location loczero = zeros.get(0).getLocation();
		int dirzero = zeros.get(0).getIntDirection();
		if (dirzero == 0) {
			int[] impact = 
				{
						loczero.x, loczero.y, loczero.x + 1, loczero.y
				};
				createReply(loczero);
				createReply(new Location(loczero.x + 1, loczero.y));
				bp.sendDataBlock(impact);
		}
		if (dirzero == 90) {
			int[] impact = 
				{
						loczero.x, loczero.y, loczero.x, loczero.y + 1
				}; 
		  		createReply(loczero);
		  		createReply(new Location(loczero.x, loczero.y + 1));
		  		bp.sendDataBlock(impact);
		}
		if (dirzero == 180) {
			int[] impact = 
				{
						loczero.x, loczero.y, loczero.x - 1, loczero.y
				}; 
		  		createReply(loczero);
		  		createReply(new Location(loczero.x - 1, loczero.y));
		  		bp.sendDataBlock(impact);
		}
		if (dirzero == 270) {
			int[] impact = 
				{
						loczero.x, loczero.y, loczero.x, loczero.y - 1
				}; 
				createReply(loczero);
				createReply(new Location(loczero.x, loczero.y - 1));
				bp.sendDataBlock(impact);
		}
		markLocation(4);
	// TODO Auto-generated method stub
	
	} else {
		Actor error = new TextActor("Not your Move!", BLACK, WHITE, font);
		addActor(error, new Location(5,10));
	}
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