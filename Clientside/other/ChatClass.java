package other;

import java.util.HashMap;

import javax.swing.JFrame;

public abstract class ChatClass extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2341039613711920315L;
	public abstract void reciveChat(String message, HashMap<String, String> flags);

}
