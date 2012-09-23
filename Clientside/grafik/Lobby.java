package grafik;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import other.ChatWindow;


import kommunikation.Spel;
import komponenter.HintTextField;
import komponenter.ScrollText;


public class Lobby extends JFrame implements ChatWindow, ActionListener, MouseListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ScrollText text = new ScrollText();
	Spel spel;
	DefaultTableModel model = new DefaultTableModel();
	HintTextField chatt;
	int players = 0;
	JTable t = new JTable(model);
	JComboBox<String> combo = new JComboBox<String>();
	HashMap<String,String> colorMap = new HashMap<String,String>();
	boolean ändra = true;

	Lobby(String name){
		model.addColumn("Online");
		spel = new Spel(this, name);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = 2;
		add(text,c);
		c.fill = GridBagConstraints.HORIZONTAL;
		chatt = new HintTextField("Chatta här");
		chatt.addActionListener(this);
		c.gridy = 1;
		c.gridwidth = 1;
		add(combo, c);
		fillCombo();
		combo.addActionListener(this);
		c.gridwidth = 2;
		c.gridy = 2;
		add(chatt, c);
		c.gridy = 0;
		c.gridx = 2;
		c.gridheight = 2;
		add(t,c);
		pack();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		t.addMouseListener(this);
	}
	
	public void fillCombo(){
		combo.removeActionListener(this);
		combo.addItem("Text color");
		combo.addItem("Pink");
		colorMap.put("Pink", "#FF00A6");
		combo.addItem("Blue");
		colorMap.put("Blue", "#0110BE");
		combo.addItem("Black");
		colorMap.put("Black", "#000000");
		combo.addItem("Grey");
		colorMap.put("Grey", "#8D8D8D");
		combo.addItem("Red");
		colorMap.put("Red", "#F30000");
		combo.addItem("Green");
		colorMap.put("Green", "#127A00");
		combo.addActionListener(this);
	}
	
	@Override
	public void reciveChat(String message, String Name) {
		text.addText(message, Name);
	}
	/**
	 * Removes a player from the list of connected players in the lobby
	 * 
	 * @param player The name of the player that should be removed
	 * 
	 */
	public void removePlayers(String player){
		for(int i = 0; i < t.getRowCount(); i++){
			if(t.getValueAt(i, 0).equals(player)){
				model.removeRow(i);
				text.removePlayer(player);
				break;
			}
		}
	}
	
	public void addPlayers(String player){
		System.out.println(player);
		text.addPlayer(player);
		model.addRow(new Object[] {player});
	}
	
	public void dispose(){
		spel.quit();
		System.exit(0);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String name = JOptionPane.showInputDialog("Vad heter du?");
		new Lobby(name);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == combo){
			if(ändra){
				chatt.appendText("¤Color="+colorMap.get(combo.getSelectedItem())+"¤");
				chatt.requestFocus();
			}
			ändra = !ändra;
		}
		else{
			spel.sendChat(chatt.getText());
			chatt.setText("");
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int row = t.rowAtPoint(e.getPoint());
		int col = t.columnAtPoint(e.getPoint());
		spel.challengePlayer((String) t.getValueAt(row, col));
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
