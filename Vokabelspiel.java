import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter; 

class Vokabelspiel
{
private Wörter[] wortListe;
private int difficulty;

public Vokabelspiel(String filename) {
    Document doc = null;
    File f = new File(filename);
    
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
private void setDifficulty(int diff) {
	difficulty = diff;
}
public int getDifficulty() {
	return this.difficulty;
}
}