import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter; 

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import ch.aplu.util.*;
import java.math.*;
import java.text.SimpleDateFormat;

class Vokabelspiel implements ActionListener
{
private Wörter[] wortListe;
private int difficulty;
private int wortWahl;
private JDialog fenster;
private JButton knopf;
private JLabel anzeigetext;
public static boolean Go;
private Timer timer;
private long startTime = -1;
private long duration = 3000;
public final int ulx,uly;

public Vokabelspiel(String filename,int x, int y){
    Document doc = null;
    File f = new File(filename);
  
    ulx = x;
    uly = y;
    
    if(!f.exists()) { 
    	System.out.print("File not found !");
    	System.exit(0);
    }

    try {
        // Das Dokument erstellen
        SAXBuilder builder = new SAXBuilder();
        doc = builder.build(f);
        //XMLOutputter fmt = new XMLOutputter()
        //Wurzelelement ausgeben
        Element root = doc.getRootElement();
        int levelCount  = root.getChildren().size();
        wortListe = new Wörter[levelCount];
        
        for(int i=0;i<levelCount;i++) {
        	String name = "Level"+(i+1);
        	
        	List<Element> LISTE = root.getChild(name).getChildren();
        	wortListe[i] = new Wörter(LISTE);
        }
        
        
    } catch (JDOMException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
} 
public int play() {
	if(Vokabelspiel.Go) {
		fenster = new JDialog();
		fenster.setTitle("COUNTDOWN");
		fenster.setSize(200,200);
		fenster.setModal(true);
		fenster.setLocation(ulx, uly);
		anzeigetext = new JLabel();
		anzeigetext.setText("Timer");
		timer = new Timer(10, this);
		timer.setInitialDelay(0);
		startTimer();
		fenster.add(anzeigetext);
		fenster.pack();
		fenster.setVisible(true);   
		Vokabelspiel.Go = false;
	}
	String antwort;
	
	timer = new Timer(10, this);
	timer.setInitialDelay(0);
	
    startTimer();
    fenster.setModal(false);
    fenster.setVisible(true); 
	do 
	{
	antwort = JOptionPane.showInputDialog(null, getRandomWort(), "");
	System.out.print("TEST");
	} while(!this.getResult(antwort) && timer.isRunning());
	
	timer.stop();
	return 2;
}
private String getRandomWort() {
	int random = (int) (wortListe[difficulty].getWortanzahl()*Math.random());
	setWortwahl(random);
	return wortListe[difficulty].getWort(random);
}
	   
private boolean getResult(String antwort) {
	return wortListe[getDifficulty()].Verify(antwort, getWortwahl());
}
private void setDifficulty(int diff) {
	difficulty = diff;
}
public int getDifficulty() {
	return this.difficulty;
}
private void setWortwahl(int wort) {
	wortWahl = wort;
}
public int getWortwahl() {
	return this.wortWahl;
}
@Override
public void actionPerformed(ActionEvent e) {
	// TODO Auto-generated method stub
	if(e.getSource() == knopf){
		startTimer();
	}
	if(e.getSource() == timer) {
		long now = System.currentTimeMillis();
	    long clockTime = now - startTime;
		SimpleDateFormat df = new SimpleDateFormat("mm:ss:SSS");
		anzeigetext.setText(df.format(duration - clockTime));
		  if (clockTime >= duration) {
		        clockTime = duration;
		        timer.stop();
		        duration = 30000;
		        startTime = -1;
		        fenster.setVisible(false);
		    }
	}
}
public void startTimer() {
	if (startTime < 0) {
        startTime = System.currentTimeMillis();
    }
    long now = System.currentTimeMillis();
    long clockTime = now - startTime;
  
    SimpleDateFormat df = new SimpleDateFormat("mm:ss:SSS");
    anzeigetext.setText(df.format(duration - clockTime));
    
    timer.addActionListener(this);
	if (!timer.isRunning()) {
        timer.start();
    }
}
}