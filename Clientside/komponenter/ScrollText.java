package komponenter;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;



public class ScrollText extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1880851659724196605L;
	textField textPane;
	JScrollPane scrollPane;
	public ScrollText(){
		textPane = new textField();
		scrollPane = new JScrollPane(textPane);
		scrollPane.setPreferredSize(new Dimension(250, 155));
		add(scrollPane);


	}
	public void addText(String Text, String Name){
		textPane.addText(Text, Name);
	}
	
	public void removePlayer(String Player){
		textPane.removePlayer(Player);
	}

}
