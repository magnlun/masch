package grafik;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JPanel;

import kommunikation.Spel;
import komponenter.HintTextField;
import komponenter.ScrollText;

import other.ChatClass;

public class Game extends ChatClass implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3059362838748572617L;
	final int size = 10;
	int[][] values = new int[size][size];
	Button[][] buttons = new Button[size][size];
	boolean yourturn;
	Spel spel;
	ScrollText text = new ScrollText();
	HintTextField chatt = new HintTextField("Chatta här");
	
	public Game(boolean turn, Spel spel, String name){
		setTitle(name);
		JPanel panel = new JPanel(new GridLayout(size,size));
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		this.spel = spel;
		yourturn = turn;
		for(int i = 0; i < size; i++){
			for(int j = 0; j < size; j++){
				buttons[i][j] = new Button(new int[] {i,j}, this);
				panel.add(buttons[i][j]);
			}
		}
		
		add(panel, gc);
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridy++;
		add(text,gc);
		gc.gridy++;
		gc.gridwidth = 2;
		add(chatt,gc);
		chatt.addActionListener(this);
		pack();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);		
	}

	public void win(){
		for(int i = 0; i < values.length; i++){
			for(int j = 0; j < values[i].length-2; j++)
				if(values[i][j]+values[i][j+1]+values[i][j+2] == 3){
					lockGame();
			}	
		}
				
		//Lodrätt tre i rad
		for(int i = 0; i < values.length-2; i++){
			for(int j = 0; j < values[i].length; j++)
				if(values[i][j]+values[i+1][j]+values[i+2][j] == 3){
					lockGame();
				}
		}
				
		//Diagonalt \ tre i rad
		for(int i = 0; i < values.length-2; i++){
			for(int j = 0; j < values[i].length-2; j++)
			if(values[i][j]+values[i+1][j+1]+values[i+2][j+2] == 3){
				lockGame();
			}
		}
				
		//Diagonalt / tre i rad
		for(int i = 0; i < values.length-2; i++){
			for(int j = 2; j < values[i].length; j++)
			if(values[i][j]+values[i+1][j-1]+values[i+2][j-2] == 3){
				lockGame();
			}
		}
	}
	
	public void lockGame(){
		spel.sendMessage("Vinst");
		for(int i = 0; i < values.length; i++){
			for(int j = 0; j < values[i].length; j++)
				buttons[i][j].setValid();
		}
		spel.win();
	}
	
	public void reciveMove(String move){
		String[] split = move.split(",");
		int x = Integer.parseInt(split[0]);
		int y = Integer.parseInt(split[1]);
		Button button = buttons[x][y];
		if(place(button, -1, Color.red)){
			yourturn = true;
		}
	}

	@Override
	public void reciveChat(String message, HashMap<String, String> flags) {
		text.addText(message, flags.get("-n"));
	}
	
	public boolean place(Button button, int value, Color color){
		if(button.place(color, value)){
			values[button.getIndex()[0]][button.getIndex()[1]] = value;
			return true;
		}
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == chatt){
			spel.sendChat(chatt.getText());
			chatt.setText("");
		}
		else if(yourturn){
			Button temp = ((Button)arg0.getSource());
			if(place(temp, 1, Color.black)){
				yourturn = false;
				int[] position = temp.getIndex();
				spel.sendMove(position[0]+","+position[1]);
				win();
			}
		}
	}
}
