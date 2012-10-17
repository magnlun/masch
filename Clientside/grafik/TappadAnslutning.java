package grafik;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import kommunikation.Spel;


public class TappadAnslutning extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 204283862585453824L;

	int startCount = 10;
	int count = startCount;
	JButton reconn = new JButton("Anslut igen (" + startCount +")");
	JButton lobby = new JButton("Tillbaka till lobbyn");
	JButton avslut = new JButton("Avsluta");
	Spel spelare;
	boolean run = true;
	
	public TappadAnslutning(Spel spelare, JFrame parent){
		super(parent);
		this.spelare = spelare;
		GridBagConstraints gc = new GridBagConstraints();
		this.setLayout(new GridBagLayout());
		gc.gridwidth = 3;
		this.add(new JLabel("Tyvärr tappade du kontakten med den andra spelaren"),gc);
		GridBagConstraints gc2 = new GridBagConstraints();
		gc2.gridwidth = 3;
		gc2.gridy = 1;
		this.add(new JLabel("Vad vill du göra?"),gc2);
		GridBagConstraints gc3 = new GridBagConstraints();
		gc3.gridy = 2;
		reconn.addActionListener(this);
		lobby.addActionListener(this);
		avslut.addActionListener(this);
		this.add(reconn, gc3);
		gc.gridx++;
		this.add(lobby, gc3);
		gc.gridx++;
		this.add(avslut,gc3);
		this.pack();
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
		setModal(true);
		this.start();
	}
	
	private void start(){
		while(run){
			try{
				Thread.sleep(1000);
		    }
		    catch (InterruptedException e) {
		    	 e.printStackTrace();
		    }
			count--;
			if(count == 0){
				reconn.doClick();
				startCount *= 2;
				count = startCount;
			}
			reconn.setText("Anslut igen (" + count +")");
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == reconn){
			reconnect();
		}
		else if(e.getSource() == lobby){
			spelare.returnToLobby();
		}
		else{
			spelare.quit();
		}
	}
	
	public void reconnect(){
		spelare.sendMessage("p"+spelare.getOpponent());
		try {
			Thread.sleep(1000);
		} 
		catch (InterruptedException e) {}	//Ignore
		String command;
		try {
			command = spelare.readLine();
			while(!(command.equals("yc") || command.equals("nc"))){
				System.out.println(command);
				spelare.processCommand(command);
				command = spelare.readLine();
			}
			System.out.println("Command: " + command);
			if(command.equals("yc")){
				System.out.println("Ta bort");
				run = false;
				setModal(false);
				dispose();
			}
			
		} catch (IOException e) {
			e.printStackTrace();	//TODO Ta bort sen (bara här för felkoll)
		}
	}
}
