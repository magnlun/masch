import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.io.*;

public class Server extends Thread{
	HashMap<String,Socket> sockets = new HashMap<String,Socket>();
	HashMap<String,ClientHandler> ClientHandlers = new HashMap<String,ClientHandler>();
	ArrayList<String> usersOnline = new ArrayList<String>();
	HashMap<String, String> reconnect;
	
	Socket alternativeServer;
	boolean run = true;
	final int port = 5554;
	final String breakString = "0102defg5486asdefasdw62485ew13r4";
	
	Server(){
		this.setDaemon(true);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() { shutdown(); }  //If the server tries to shut down unexpecedly it will run shutdown() first
		});
	}
	
	public void run(){
		try(ServerSocket sock = new ServerSocket(port, 100)) {
			while (run){
				try{
					Socket temp = sock.accept();
					if(run){
						ClientHandler temp2 = new ClientHandler(temp, this);
						temp2.setDaemon(true);
						temp2.start();
					}
				}
				catch(Exception err){}
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			shutdown();
		}
	}
	
	public void shutdown(){
		Object[] itr = sockets.keySet().toArray();
		if(alternativeServer == null){
			//No alternative server is known so the server will just die
			messageToAllPlayers("Dead");
		}
		else{
			try(PrintWriter temp = new PrintWriter(alternativeServer.getOutputStream())){
				//temp.println("1q");
				temp.println("q");
				temp.flush();
				for(int i = 0; i < itr.length; i++){
					temp.println(itr[i]);
					temp.flush();
				}
				temp.println(breakString);
				temp.flush();
				
				for(int i = 0; i < itr.length; i++){
					Object user = itr[i];
					Socket soc = sockets.get(user);
					PrintWriter PW = new PrintWriter(soc.getOutputStream());
					PW.println("p"+alternativeServer.getPort());
					PW.flush();
					PW.println(alternativeServer.getLocalAddress());
					PW.flush();
					PW.close();
				}
				temp.println("exit");
				temp.flush();
				temp.close();
				Thread.sleep(5000);
			}
			catch(Exception err){			} //ingore
			
		}
		
		run = false;
		try{
			new Socket("127.0.0.1",port);  //Prevents the server from waiting for a connection
		}
		catch(Exception e){}
	}
	
	public void addOtherUsers(BufferedReader BR){
		reconnect = new HashMap<String, String>();
		try{
			String name = BR.readLine();
			while(!name.equals(breakString)){
				String newName = new String(name);
				int j = 2;
				while(contains(newName)){
					if(!contains(newName+j)){
						newName += j;
						break;
					}
					j++;
				}
				reconnect.put(name, newName);
				name = BR.readLine();
			}
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public synchronized void addUser(Socket socket, ClientHandler CH, String name){
		usersOnline.add(name);
		sockets.put(name, socket);
		ClientHandlers.put(name, CH);
	}
	
	public ArrayList<String> usersOnline(){
		return usersOnline;
	}
	
	public ClientHandler getHandler(String user){
		return ClientHandlers.get(user);
	}
	
	public Socket getSocket(String user){
		return sockets.get(user);
	}
	public synchronized void removeUser(String user){
		usersOnline.remove(user);
		try{
			for(String name : usersOnline){
				Socket soc = getSocket(name);
				PrintWriter ut = new PrintWriter(soc.getOutputStream());
				ut.println("ur" + user);
				ut.flush();
			}
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public boolean contains(String user){
		return sockets.containsKey(user);
	}
	
	public void addServer(String ip, int port){
		try {
			this.alternativeServer = new Socket(ip, port);
			PrintWriter PW = new PrintWriter(alternativeServer.getOutputStream());
			PW.println("Server");
			PW.flush();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void logOut(String name){
		removeUser(name);
		sockets.remove(name);
		ClientHandlers.remove(name);
	}
	
	public void causeException(){
		throw new NullPointerException();
	}
	
	public void printStatus(){
		System.out.println("Chattar:");
		for(String user : usersOnline)
			System.out.println(user);
		System.out.println("Anslutna:");
		Iterator<String> itr = sockets.keySet().iterator();
		while(itr.hasNext())
			System.out.println(itr.next());
		
		
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		Scanner sc = new Scanner(System.in);
		server.start();
		while(true){
			String command = sc.nextLine();
			String[] words = command.split(" ");
			if(words.length == 3 && words[0].equals("add")){
				try{
					server.addServer(words[1], Integer.parseInt(words[2]));
				}
				catch(Exception e){
					System.err.println("Unkown command");
				}
			}
			else if(command.equals("except")){
				server.causeException();
			}
			else if(command.equals("quit")){
				break;
			}
			else if(command.equals("status")){
				server.printStatus();
			}
			else{
				System.err.println("Unkown command");
			}
		}
		server.shutdown();
		sc.close();
	}
	
	public void messageToAllPlayers(String message){
		try{
			for(String name : usersOnline){
				Socket socket = getSocket(name);
				PrintWriter temp = new PrintWriter(socket.getOutputStream());
				temp.println(message);
				temp.flush();
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	public String getAlias(String name){
		return reconnect.get(name);
	}
}

class ClientHandler extends Thread {
	Server server;
	BufferedReader in;
	BufferedReader opponentReader;
	PrintWriter ut;
	PrintWriter opponentWriter;
	ClientHandler opponent;
	Socket socket;
	//Long code;
	//int offset = 0;
	String name;

	public ClientHandler(Socket socket, Server server) {
		this.server = server;
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			ut = new PrintWriter(socket.getOutputStream());
			name = in.readLine();
			if(name.equals("Server")){
				//code = (long) 1.0;
				return;
			}
			//code = Long.parseLong(in.readLine());
			name = changeName(name);
			ut.println('%'+name);
			ut.flush();
			server.messageToAllPlayers("uas" + this.name);
			requestUsers();
			server.addUser(socket, this, name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void requestUsers(){
		for(String user : server.usersOnline()){
			ut.println("uaq" + user);
		}
		ut.flush();
	}
	
	public void requestNumberOfUsers(){
		ut.println("d"+(server.usersOnline().size()-1));
		ut.flush();
	}
	
	public void logOut(){
		if(opponentWriter != null){
			opponentWriter.println("b");
			opponentWriter.flush();
		}
		server.logOut(this.name);
	}
	
	public String changeName(String name){
		int j = 2;
		while(server.contains(name)){
			if(!server.contains(name+j)){
				name += j;
				break;
			}
			j++;
		}
		for(int i = 0; i < name.length(); i++){
			//offset += name.charAt(i) * (i+1);
		}
		//offset %= 200000;
		return name;
	}
	
	public void close(){
		try{
			in.close();
			ut.close();
			opponentReader.close();
			opponentWriter.close();
		}
		catch(Exception e){
			//try at least
		}
	}
	
	public void removePlayer(){
		server.removeUser(name);
	}
	
	public boolean addOpponent(ClientHandler opponent, Socket opp){
		try{
			opponentWriter = new PrintWriter(opp.getOutputStream());
			opponentReader = new BufferedReader(new InputStreamReader(
					opp.getInputStream())); 
			this.opponent = opponent;
			return true;
		}
		catch(Exception e){
			ut.println("nc");
			ut.flush();
			return false;
		}
	}

	public void run() {
		try {
			while(true){
				String indata = in.readLine();
				/*if(!indata.startsWith(code.toString())){
					System.err.println(indata);
					System.err.println(code);
					System.err.println(name);
					System.err.println("Intrångsförsök");
					server.logOut(this.name);
					break;
				}*/
				//indata = indata.substring(code.toString().length());
				System.out.println(name + indata);
				if(indata.equals("exit")){
					//Remove the listener for this packet
					ut.println("Die");
					ut.flush();
					logOut();
					break;
				}
				else if(indata.equals("die")){
					logOut();
					break;
				}
				else if(opponentWriter == null && indata.charAt(0) == 'c'){
					//Packet for all the other players in the Lobby (Chat probably)
					//c(data)
					server.messageToAllPlayers(indata.substring(1));
				}
				else if(indata.equals("Vinst")){
					//Player A have won and tells player B
					opponentWriter.println("Loose");
					opponentWriter.flush();
				}
				else if(indata.charAt(0) == '!'){
					server.getHandler(indata.substring(1)).ut.println("!"+name);
					server.getHandler(indata.substring(1)).ut.flush();
				}
				else if(indata.charAt(0) == '¤'){
					//A player wants to play and tells the other player
					//¤Magnus
					String name = indata.substring(1);
					addOpponent(server.getHandler(name),server.getSocket(name));
					opponentWriter.println("§"+this.name);
					opponentWriter.flush();
				}
				else if(indata.charAt(0) == 'a'){
					requestUsers();
				}
				else if(indata.charAt(0) == 'b'){
					opponentWriter = null;
					opponent = null;
					opponentReader = null;
					server.messageToAllPlayers("uas" + this.name);
					server.addUser(socket, this, name);
				}
				else if(indata.charAt(0) == 'd'){
					requestNumberOfUsers();
				}
				else if(indata.charAt(0) == '§'){
					//Player B starts to play with player A
					//§Magnus
					String name = indata.substring(1);
					addOpponent(server.getHandler(name),server.getSocket(name));
					removePlayer();
					server.removeUser(name);
					opponentWriter.println("yc");
					opponentWriter.flush();
				}
				else if(indata.charAt(0) == '%'){
					changeName(indata.substring(1));
				}
				else if(indata.charAt(0) == 'c'){
					//Message that should be sent to the clients for translation
					//c(data)
					ut.println(indata.substring(1));
					ut.flush();
					opponentWriter.println(indata.substring(1));
					opponentWriter.flush();
				}
				else if(indata.charAt(0) == 'r'){
					removePlayer();
				}
				else if(indata.charAt(0) == 'q' && name.equals("Server")){
					server.addOtherUsers(in);
				}
				else if(indata.charAt(0) == 'p'){
					try{
						String ID = in.readLine();
						String opponent = server.getAlias(indata.substring(1));
						if(addOpponent(server.getHandler(opponent),server.getSocket(opponent))){
							opponentWriter.println("a"+name);
							opponentWriter.flush();
							opponentWriter.println(ID);
							opponentWriter.flush();
						}
					}
					catch(NullPointerException err){
						ut.println("nc");
						ut.flush();
					}
				}
				else{ 	
					System.err.println(indata);
					System.err.println("Någon har glömt en flagga");
					System.exit(7);
				}
				//code += offset;
			}
		} 
		catch (Exception e) {
			logOut();
		}
	}
}
