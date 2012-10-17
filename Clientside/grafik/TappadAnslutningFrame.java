package grafik;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import kommunikation.Spel;

public class TappadAnslutningFrame extends JFrame implements ActionListener, Runnable{

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
	
	public TappadAnslutningFrame(Spel spelare){
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
		new Thread(this).start();
	}
	
	public void dispose(){
		run = false;
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == lobby){
			spelare.returnToLobby();
		}
		else{
			spelare.quit();
		}
	}

	@Override
	public void run() {
		while(run){
			try{
				Thread.sleep(1000);
		    }
		    catch (InterruptedException e) {
		    	 e.printStackTrace();
		    }
			count--;
			if(count == 0){
				startCount *= 2;
				count = startCount;
			}
			reconn.setText("Anslut igen (" + count +")");
		}
		
	}
}
