package grafik;

import java.awt.Color;
import javax.swing.JButton;

public class Button extends JButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5057780394939071688L;
	int[] index; // Plats på spelplanen
	int value; // svart=1 eller röd=-1
	Game container;
	public static int tur = 1;
	boolean valid = true;

	Button(int[] i, Game container) {
		this.container = container;
		setIndex(i);
		// setText(""+i);
		addActionListener(container);
	}

	private void setIndex(int[] i) {
		index = i;
	}

	public int[] getIndex() {
		return index;
	}

	public void setValue(int v) {
		value = v;
	}

	public int getValue() {
		return value;
	}
	
	public void setValid(){
		valid = false;
	}

	public boolean place(Color color, int value) {
		if(valid){
			this.value = value;
			setBackground(color);
			container.win();
			valid = false;
			return true;
		}
		return false;
	}
}
