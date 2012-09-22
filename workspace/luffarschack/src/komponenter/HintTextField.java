package komponenter;


import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -925138954713042871L;
	String hint;
	
	public HintTextField(String hint){
		super();
		this.setForeground(Color.GRAY);
		this.setText(hint);
		this.hint = hint;
		this.addFocusListener(this);
	}
	
	@Override
	public String getText(){
		String rc = super.getText();
		if(rc.equals(hint)){
			return "";
		}
		return rc;
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(this.getText().equals("")){
			this.setText("");
			this.setForeground(Color.BLACK);
		}
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()){
			this.setForeground(Color.GRAY);
			this.setText(this.hint);
		}
	}
	
	public void appendText(String text){
		if(this.getText().equals("")){
			this.setText("");
			this.setForeground(Color.BLACK);
		}
		setText(getText() + text);
	}

}
