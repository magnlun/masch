import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.io.*;

public class Server extends Thread{
	HashMap<String,Socket> sockets = new HashMap<String,Socket>();
	HashMap<String,ClientHandler> ClientHandlers = new HashMap<String,ClientHandler>();
	Socket alternativeServer;
	boolean run = true;
	final int port = 5555;
	
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
			shutdown();
			e.printStackTrace();
		}
	}
	
	public void shutdown(){
		if(alternativeServer == null){
			//No alternative server is known so the server will just die
			messageToAllPlayers("Dead");
		}
		else{
			messageToAllPlayers("p"+alternativeServer.getPort());
			messageToAllPlayers(""+alternativeServer.getLocalAddress());
		}
		
		run = false;
		try{
			new Socket("127.0.0.1",port);  //Prevents the server from waiting for a connection
		}
		catch(Exception e){}
	}
	
	public void addUser(Socket socket, ClientHandler CH, String name){
		sockets.put(name, socket);
		ClientHandlers.put(name, CH);
	}
	
	public Iterator<String> usersOnline(){
		return sockets.keySet().iterator();
	}
	
	public Iterator<Socket> socketsOnline(){
		return sockets.values().iterator();
	}
	
	public ClientHandler getHandler(String user){
		return ClientHandlers.get(user);
	}
	public Socket getSocket(String user){
		return sockets.get(user);
	}
	public void removeUser(String user){
		ClientHandlers.remove(user);
		sockets.remove(user);
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
	
	public void causeException(){
		throw new NullPointerException();
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
			else{
				System.err.println("Unkown command");
			}
		}
		server.shutdown();
		sc.close();
	}
	
	public void messageToAllPlayers(String message){
		try{
			Iterator<Socket> itr = socketsOnline();
			while(itr.hasNext()){
				Socket socket = itr.next();
				PrintWriter temp = new PrintWriter(socket.getOutputStream());
				temp.println(message);
				temp.flush();
			}
		}
		catch(Exception e){
			
		}
		
	}
}

class ClientHandler extends Thread {
	static int antaltr = 0;
	Server server;
	BufferedReader in;
	BufferedReader opponentReader;
	PrintWriter ut;
	PrintWriter opponentWriter;
	ClientHandler opponent;
	String name;

	public ClientHandler(Socket socket, Server server) {
		this.server = server;
		try {
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			ut = new PrintWriter(socket.getOutputStream());
			name = in.readLine();
			System.out.println(name);
			while(server.contains(name)){
				ut.println("%%");
				ut.flush();
				name = in.readLine();
			}
			ut.println("%g");
			ut.flush();
			if(!name.equals("Server")){
			Iterator<Socket> iterator = server.socketsOnline();
				while(iterator.hasNext()){
					Socket soc = iterator.next();
					PrintWriter uta = new PrintWriter(soc.getOutputStream());
					uta.println("uas" + name);
					uta.flush();
				}
				server.addUser(socket, this, name);
			}
			Iterator<String> itr = server.usersOnline();
			while(itr.hasNext()){
				String temp = itr.next();
				if(!temp.equals(name)){
					this.ut.println("uaq" + temp);
					this.ut.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		try{
			Iterator<Socket> iterator = server.socketsOnline();
			while(iterator.hasNext()){
				Socket soc = iterator.next();
				PrintWriter ut = new PrintWriter(soc.getOutputStream());
				ut.println("ur" + name);
				ut.flush();
			}
		}
		catch(Exception err){
			err.printStackTrace();
		}
	}
	
	public void addOpponent(ClientHandler opponent, Socket opp){
		try{
			this.opponent = opponent;
			opponentWriter = new PrintWriter(opp.getOutputStream());
			opponentReader = new BufferedReader(new InputStreamReader(
					opp.getInputStream())); 
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while(true){
				String indata = in.readLine();
				System.out.println(indata);
				if(indata.equals("exit")){
					//Remove the listener for this packet
					ut.println("Die");
					ut.flush();
					removePlayer();
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
					break;
				}
				else if(indata.charAt(0) == '!'){
					server.getHandler(indata.substring(1)).ut.println("¤"+name);
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
				else if(indata.charAt(0) == '§'){
					//Player B starts to play with player A
					//§Magnus
					String name = indata.substring(1);
					addOpponent(server.getHandler(name),server.getSocket(name));
					removePlayer();
					opponentWriter.println("r");
					opponentWriter.flush();
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
				else{
					System.err.println(indata);
					System.err.println("Någon har glömt en flagga");
					System.exit(7);
				}
			}
		} 
		catch (Exception e) {
			removePlayer();
		}
	}
}
