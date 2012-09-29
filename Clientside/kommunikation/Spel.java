package kommunikation;
import grafik.Lobby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import other.ChatWindow;



public class Spel {
	List<String> kö = Collections.synchronizedList(new LinkedList<String>());
	BufferedReader in;
	PrintWriter ut;
	ChatWindow chat;
	String name;
	Socket sock;
	final int port = 5555;
	String opponent = "";
	boolean challenger = false;
	
	public Spel(ChatWindow chat, String Name){
		try{
			sock = new Socket("192.168.0.11",port);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			ut = new PrintWriter(sock.getOutputStream());
			new chat(this, sock).start();
			this.chat = chat;
			ut.println(Name);
			ut.flush();
			name = Name;
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public void addToQueue(String string){
		kö.add(string);
	} 
	
	public void quit(){
		ut.println("exit");
		ut.flush();
		ut.close();
		try{
			in.close();
			sock.close();
		}
		catch(Exception err){
			
		}
	}
	
	public void sendChat(String message){
		message = "cc -n " + name + "¤ -m " + message;
		ut.println(message);
		ut.flush();
	}
	
	public void recieveChat(String message){
		/*
		 * Flaggfixning här
		 */
		HashMap<String,String> flags = findFlags(message);
		String mess = findMessage(message);
		chat.reciveChat(mess, flags);
	}	
	
	public void removePlayer(String name){
		try{
			((Lobby) chat).removePlayers(name);
		}
		catch(Exception e){} //Drop packet
	}
	
	public void addPlayer(String message){
		try{
			((Lobby) chat).addPlayers(message);
		}
		catch(Exception e){}			//Drop packet
	}	
	
	public static HashMap<String,String> findFlags(String message){
		HashMap<String, String> rc = new HashMap<String,String>();
		for(int i = 0; i < message.length(); i++){
			String temp = "-";
			if(message.charAt(i) == '-'){
				i++;
				while(message.charAt(i) != ' '){
					temp += message.charAt(i);
					i++;
				}
				if(!temp.equals("-m")){
					String temp2 = "";
					for(int j = i + 1; j < message.length() && message.charAt(j) != '¤'; j++){
						temp2 += message.charAt(j);
					}
					rc.put(temp,temp2);
				}
			}
		}
		return rc;
	}
	
	public void challengePlayer(String player){
		opponent = player;
		challenger = true;
		ut.println("¤"+player);
		ut.flush();
	}
	
	public static String findMessage(String message){
		int counter = -1;
		for(int i = 0; i < message.length(); i++){
			if(message.charAt(i) == '-' && message.charAt(i+1) == 'm')
				counter = i;
		}
		if(counter > -1){
			return message.substring(counter + 2);
		}
		return null;
	}
	
	public void changeName(){
		name = JOptionPane.showInputDialog("Tyvärr är namnet upptaget, välj ett nytt");
		ut.println(name);
		ut.flush();
	}
	
	public void reconnect(Socket socket) throws IOException{
		sock = socket;
		ut = new PrintWriter(socket.getOutputStream());
		ut.println(name);
		ut.flush();
		if(opponent.length() > 0 && challenger){
			challengePlayer(opponent);
		}
	}

}

class chat extends Thread{
	BufferedReader rd;
	Spel spelare;
	Socket socket;
	chat(Spel spelare, Socket socket){
		this.spelare = spelare;
		this.socket = socket;
		try{
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public void run(){
		while(true){
			String command;
			try {
				command = rd.readLine();
				if(command.charAt(0) == 'c'){
					spelare.recieveChat(command);
				}
				else if(command.charAt(0) == 'u'){
					if(command.charAt(1) == 'a'){
						spelare.addPlayer(command.substring(2));
					}
					else if(command.charAt(1) == 'r')
						spelare.removePlayer(command.substring(2));
				}
				else if(command.charAt(0) == '%'){
					if(command.charAt(1) == '%')
						spelare.changeName();
					else
						spelare.chat.setVisible(true);
				}
				else if(command.equals("Die")||command.equals("Vinst")){
					break;
				}
				else if(command.charAt(0) == 'm'){
					spelare.addToQueue(command.substring(1));
				}
				else if(command.charAt(0) == '§'){
					spelare.opponent = command.substring(1);
					spelare.ut.println("§" + command.substring(1));
					spelare.ut.flush();
				}
				else if(command.charAt(0) == 'r'){
					spelare.ut.println('r');
					spelare.ut.flush();
				}
				else if(command.charAt(0) == 'p'){
					String port = command.substring(1);
					String ip = rd.readLine().substring(1);
					socket = new Socket(ip, Integer.parseInt(port));
					rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					spelare.reconnect(socket);
				}
				else{
					System.err.println(command);
					System.err.println("Någon har glömt en flagga");
					spelare.quit();
					System.exit(7);
				}
			} 
			catch (IOException e) {
				spelare.quit();
				System.exit(0);
			}
		}
		spelare.quit();
	}
}