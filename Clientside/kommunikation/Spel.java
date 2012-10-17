package kommunikation;
import grafik.Lobby;
import grafik.TappadAnslutning;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import other.ChatClass;
import other.KommQueue;



public class Spel {
	List<String> kö = Collections.synchronizedList(new LinkedList<String>());
	BufferedReader in;
	PrintWriter ut;
	ChatClass chat;
	String name;
	Socket sock;
	final int port = 5554;
	String opponent = "";
	boolean challenger = false;
	Window ansl;
	KommQueue commands = new KommQueue();
	chat process;
	//long code = System.currentTimeMillis();
	//int offset = 0;
	
	public Spel(ChatClass chat, String Name){
		try{
			sock = new Socket("192.168.0.11",port);
			this.chat = chat;
			connect(sock, Name);
			process = new chat(this, sock);
			process.start();
			chat.setVisible(true);
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public void addToQueue(String string){
		kö.add(string);
	} 
	
	public void quit(){
		sendMessage("exit");
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
		sendMessage(message);
	}
	
	public void acceptChallenge(String name){
		try{
			@SuppressWarnings("unused")
			Lobby a = (Lobby) chat; //Makes sure that the player has a Lobby open;
			int accept = JOptionPane.showConfirmDialog(chat, name + " vill utmana dig, accepterar du?", "Utmaning", JOptionPane.YES_NO_OPTION); 
			if(accept == 0){
				opponent = name;
				challenger = true;
				sendMessage("¤"+name);
				//TODO add start of game here
			}
		}
		catch(Exception e){} //Ignore
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
		sendMessage("!"+player);
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
	
	public void connect(Socket socket, String name) throws IOException{
		this.sock = socket;
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		ut = new PrintWriter(sock.getOutputStream());
		ut.println(name);
		ut.flush();
		//ut.println(code);
		//ut.flush();
		changeName(in.readLine().substring(1));	//Changes the name to what the server whats
	}
	
	public void reconnect(Socket socket) throws IOException{
		connect(socket, this.name);
		if(opponent.length() > 0){
			if(challenger)
				ansl = new TappadAnslutning(this, chat);
		}
	}
	
	public void returnToLobby(){
		chat.dispose();
		chat = new Lobby(this.name, this);
	}
	
	public String getOpponent(){
		return opponent;
	}
	
	public String readLine() throws IOException{
		return in.readLine();
	}
	
	public void processCommand(String command) throws NumberFormatException, UnknownHostException, IOException{
		process.processCommand(command);
	}
	
	public void kill(){
		JOptionPane.showMessageDialog(chat, "Tyvärr har du förlorat kontakten med servern");
	}
	
	public void message(String message){
		JOptionPane.showMessageDialog(chat, message);
		chat.dispose();
		chat = new Lobby(name);
	}
	
	public void sendMove(String move){
		sendMessage('m'+move);
	}
	
	public void sendMessage(String message){
		//ut.println(code+message);
		ut.println(message);
		ut.flush();
		//code += offset;
	}
	
	public void changeName(String name){
		this.name = name;
		/*for(int i = 0; i < name.length(); i++){
			offset += name.charAt(i) * (i+1);
		}*/
		chat.setTitle(name);
	}
}

class chat extends Thread{
	BufferedReader rd;
	Spel spelare;
	chat(Spel spelare, Socket socket){
		this.spelare = spelare;
		try{
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public boolean processCommand(String command) throws NumberFormatException, UnknownHostException, IOException{
		if(command.equals("Die")){
			return false;
		}
		else if(command.equals("Draw")){
			spelare.message("Det blev lika!");
		}
		else if(command.equals("Loose")){
			spelare.message("Tyvärr förlorade du");
		}
		else if(command.equals("Dead")){
			spelare.kill();
			return false;
		}
		else if(command.charAt(0) == 'y'){
		}
		else if(command.charAt(0) == 'n'){
		}
		else if(command.charAt(0) == 'c'){
			spelare.recieveChat(command);
		}
		else if(command.charAt(0) == 'u'){
			if(command.charAt(1) == 'a'){
				spelare.addPlayer(command.substring(2));
			}
		else if(command.charAt(1) == 'r')
				spelare.removePlayer(command.substring(2));
		}
		else if(command.charAt(0) == '!'){
			spelare.acceptChallenge(command.substring(1));
		}
		else if(command.charAt(0) == '%'){
			spelare.changeName(command.substring(1));
		}
		else if(command.charAt(0) == 'm'){
			spelare.addToQueue(command.substring(1));
		}
		else if(command.charAt(0) == '§'){
			spelare.opponent = command.substring(1);
			spelare.sendMessage("§" + command.substring(1));
			try {
				spelare.ansl.dispose();
			}
			catch(NullPointerException e){}	//If it is not open ignore it
			//TODO add start of game here
		}
		else if(command.charAt(0) == 'r'){
			spelare.sendMessage("r");
		}
		else if(command.charAt(0) == 's'){
			System.out.println(command);
		}
		else if(command.charAt(0) == 'p'){
			spelare.sendMessage("die");
			String port = command.substring(1);
			String ip = rd.readLine().substring(1);
			spelare.removePlayer(null);	//Remove all players
			Socket socket = new Socket(ip, Integer.parseInt(port));
			rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			spelare.reconnect(socket);
		}
		else{
			System.err.println(command);
			System.err.println("Någon har glömt en flagga");
			spelare.quit();
			System.exit(7);
		}
		return true;
	}
	
	public void run(){
		boolean cont = true;
		while(cont){
			String command;
			try {
				command = rd.readLine();
				System.out.println(command);
				cont = processCommand(command);
			} 
			catch (IOException e) {
				spelare.quit();
				spelare.kill();
				e.printStackTrace();
			}
		}
		System.exit(0);
	}
}