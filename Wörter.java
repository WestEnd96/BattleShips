import java.util.List;

import org.jdom2.Element;

public class Wörter {

private String[][] Liste;
private int WortAnzahl;
	
public Wörter(List<Element> LISTE) {
	this.Liste = setWorterliste(LISTE);
}

private String[][] setWorterliste(List<Element> Woerterliste) {
	setWortanzahl(Woerterliste.size());
	String[][] Liste = new String[getWortanzahl()][5];
	for(int i=0;i<getWortanzahl();i++) {
        Liste[i][0] = Woerterliste.get(i).getValue();
        String trans = Woerterliste.get(i).getAttributeValue("translation");
    	String[]AllTrans = trans.split(",");
	        for(int j=1;j<AllTrans.length+1;j++) {
	        	Liste[i][j] = AllTrans[j-1];
	        }
        }
	return Liste;
}
private void setWortanzahl(int anzahl) {
	this.WortAnzahl = anzahl;
}
public int getWortanzahl() {
	return this.WortAnzahl;
}
public String getWort(int wortwahl) {
	return Liste[wortwahl][0];
}
public void setWort(int wortwahl,String wort) {
	Liste[wortwahl][0] = wort;
}
}
