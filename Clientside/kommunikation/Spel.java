package kommunikation;
import grafik.Game;
import grafik.Lobby;
import grafik.TappadAnslutning;
import grafik.TappadAnslutningFrame;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JOptionPane;

import other.ChatClass;



public class Spel {
	BufferedReader in;
	PrintWriter ut;
	ChatClass chat;
	String name;
	Socket sock;
	int port = 5554;
	String ip = "192.168.0.11";
	String opponent = "";
	boolean challenger = false;
	Window ansl;
	chat process;
	int sessionID = 0;
	//long code = System.currentTimeMillis();
	//int offset = 0;
	
	public Spel(ChatClass chat, String Name){
		try{
			sock = new Socket(ip,port);
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
		if(sessionID == 0){
			int accept = JOptionPane.showConfirmDialog(chat, name + " vill utmana dig, accepterar du?", "Utmaning", JOptionPane.YES_NO_OPTION); 
			if(accept == 0){
				opponent = name;
				challenger = true;
				sendMessage("¤"+name);
				Random r = new Random();
				int id = Math.abs(r.nextInt());
				if(id == 0)
					id++;
				sendMessage("ci"+id);
				chat.dispose();
				chat = new Game(true, this);
			}
		}
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
	
	public void reciveMove(String move){
		try{
			((Game)chat).reciveMove(move);
		}
		catch(Exception err){}
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
	
	public void sendID(){
		sendMessage(Integer.toString(sessionID));
	}
	
	public void reconnect(Socket socket) throws IOException{
		connect(socket, this.name);
		if(opponent.length() > 0){
			if(challenger)
				ansl = new TappadAnslutning(this, chat);
			else
				ansl = new TappadAnslutningFrame(this);
		}
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
		sendMessage("cm"+move);
	}
	
	public void sendMessage(String message){
		//ut.println(code+message);
		ut.println(message);
		ut.flush();
		//code += offset;
	}
	
	public void newLobby(){
		chat.dispose();
		sessionID = 0;
		chat = new Lobby(this.name,this);
		sendMessage("a");
		sendMessage("b");
	}
	
	public void changeName(String name){
		this.name = name;
		/*for(int i = 0; i < name.length(); i++){
			offset += name.charAt(i) * (i+1);
		}*/
		chat.setTitle(name);
	}
	
	public void win(){
		JOptionPane.showMessageDialog(chat, "Grattis du vann!");
		newLobby();
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
		//Used: !,%,§,a,b,c,i,m,n,p,r,s,u,y
		if(command.equals("Die")){
			return false;
		}
		else if(command.equals("Draw")){
			spelare.message("Det blev lika!");
			spelare.newLobby();
		}
		else if(command.equals("Loose")){
			spelare.message("Tyvärr förlorade du");
			spelare.newLobby();
		}
		else if(command.equals("Dead")){
			spelare.kill();
			return false;
		}
		else if(command.charAt(0) == 'b'){
			spelare.ansl = new TappadAnslutning(spelare, spelare.chat);
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
			spelare.reciveMove(command.substring(1));
		}
		else if(command.charAt(0) == '§'){
			spelare.opponent = command.substring(1);
			spelare.sendMessage("§" + command.substring(1));
			try {
				spelare.ansl.dispose();
			}
			catch(NullPointerException e){}	//If it is not open ignore it
			spelare.chat.dispose();
			spelare.chat = new Game(false, spelare);
		}
		else if(command.charAt(0) == 's'){
			System.out.println(command);
		}
		else if(command.charAt(0) == 'i'){
			spelare.sessionID = Integer.parseInt(command.substring(1));
		}
		else if(command.charAt(0) == 'a'){
			String ID = rd.readLine();
			if(spelare.sessionID == Integer.parseInt(ID)){
				spelare.opponent = command.substring(1);
				spelare.sendMessage("§" + command.substring(1));
				try {
					spelare.ansl.dispose();
				}
				catch(NullPointerException e){}	//If it is not open ignore it				
			}
			else
				spelare.sendMessage("nc");
		}
		else if(command.charAt(0) == 'p'){
			spelare.sendMessage("die");
			spelare.port = Integer.parseInt(command.substring(1));
			spelare.ip = rd.readLine().substring(1);
			spelare.removePlayer(null);	//Remove all players
			Socket socket = new Socket(spelare.ip, spelare.port);
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