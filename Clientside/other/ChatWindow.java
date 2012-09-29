package other;

import java.util.HashMap;

public interface ChatWindow {
	public void reciveChat(String message, HashMap<String, String> flags);
	public void setVisible(boolean status);
}
