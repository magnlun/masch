package komponenter;
import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;


public class textField extends JTextPane{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1987679915247554982L;
	textField(){
	    setEditable(false);
	}
	/**
	 * addText lägger till texten den får som parameter sist i TextPanen. Innan den lägger den till 
	 * Spelarens namn som skrivs i färgen den får som tredje parameter
	 * @param Text
	 * @param Name
	 * @param color
	 */
	void addText(String Text, String Name){
	    StyleContext sc = StyleContext.getDefaultStyleContext();
	    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY,StyleConstants.Foreground, Color.black);	//Skapar textens attribut
	    setCharacterAttributes(aset, false);
	    setCaretPosition(getDocument().getLength());	//Säger att den ska skriva sist i dokumentet
	    setEditable(true);
	    replaceSelection(Name + ">");		//Skriver ut användarens namn
	    Color clr = new Color(0,0,0);
	    Text += " ";
	    String[] flags = Text.split("¤");
	    boolean färg = false;
	    for(int i = 0; i < flags.length; i++){
	    	String[] temp = flags[i].split("=");
	    	if(i > 0 && !färg)
	    		flags[i] = "¤" + flags[i];
	    	färg = false;
	    	if(temp[0].equals("Color")){
	    		try{
	    			clr = Color.decode(temp[1]);
	    			färg = true;
	    		}
	    		catch(Exception err){}
	    	}
	    	if(!färg){
	    		setCaretPosition(getDocument().getLength());	//Säger att den ska skriva sist i dokumentet
			    aset = sc.addAttribute(SimpleAttributeSet.EMPTY,StyleConstants.Foreground, clr);
			    setCharacterAttributes(aset, false);
			    replaceSelection(flags[i]);			//Skriver ut det användaren skrev
	    	}
	    }
		setCaretPosition(getDocument().getLength());
		replaceSelection("\n");
	    setEditable(false);
	}
	
	public void removePlayer(String Player){
		SimpleAttributeSet attributes = new SimpleAttributeSet();
	    StyleConstants.setItalic(attributes, true);
		StyleContext sc = StyleContext.getDefaultStyleContext();
	    AttributeSet aset = sc.addAttribute(attributes,StyleConstants.Foreground, Color.gray);	//Skapar textens attribut
	    setCharacterAttributes(aset, false);
	    setCaretPosition(getDocument().getLength());	//Säger att den ska skriva sist i dokumentet
	    setEditable(true);
	    replaceSelection(Player + " har loggat ut\n");		//Skriver ut användarens namn
	    setEditable(false);
	}
	
}
