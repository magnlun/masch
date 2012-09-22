import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;

public class Server {
	HashMap<String,Socket> sockets = new HashMap<String,Socket>();
	HashMap<String,ClientHandler> ClientHandlers = new HashMap<String,ClientHandler>();
	Server(){
		try {
			@SuppressWarnings("resource")
			ServerSocket sock = new ServerSocket(5555, 100);
			while (true){
				Socket temp = sock.accept();
				ClientHandler temp2 = new ClientHandler(temp, this);
				temp2.start();
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
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
	
	public static void main(String[] args) {
		new Server();
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
			while(server.contains(name)){
				ut.println("%");
				ut.flush();
				name = in.readLine();
			}
			Iterator<Socket> iterator = server.socketsOnline();
			while(iterator.hasNext()){
				Socket soc = iterator.next();
				PrintWriter ut = new PrintWriter(soc.getOutputStream());
				ut.println("ua " + name);
				ut.flush();
			}
			server.addUser(socket, this, name);
			Iterator<String> itr = server.usersOnline();
			while(itr.hasNext()){
				String temp = itr.next();
				if(!temp.equals(name)){
					ut.println("ua " + temp);
				}
			}
			ut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void removePlayer(){
		server.removeUser(name);
		try{
			Iterator<Socket> iterator = server.socketsOnline();
			while(iterator.hasNext()){
				Socket soc = iterator.next();
				PrintWriter ut = new PrintWriter(soc.getOutputStream());
				ut.println("ur " + name);
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
				if(indata.equals("exit")){
					//Remove the listener for this packet
					removePlayer();
					ut.println("Die");
					ut.flush();
					break;
				}
				else if(opponentWriter == null && indata.charAt(0) == 'c'){
					//Packet for all the other players in the Lobby (Chat probably)
					//c(data)
					Iterator<Socket> itr = server.socketsOnline();
					while(itr.hasNext()){
						Socket socket = itr.next();
						PrintWriter temp = new PrintWriter(socket.getOutputStream());
						temp.println(indata.substring(1));
						temp.flush();
					}
				}
				else if(indata.equals("Vinst")){
					//Player A have won and tells player B
					opponentWriter.println("Vinst");
					opponentWriter.flush();
					ut.println("Die");
					ut.flush();
					break;
				}
				else if(indata.charAt(0) == '¤'){
					//A player wants to play and tells the other player
					//¤Magnus
					String name = indata.substring(1);
					addOpponent(server.getHandler(name),server.getSocket(name));
					removePlayer();
					opponentWriter.println("§"+this.name);
					opponentWriter.flush();
				}
				else if(indata.charAt(0) == '§'){
					//Player B starts to play with player A
					//§Magnus
					String name = indata.substring(1);
					addOpponent(server.getHandler(name),server.getSocket(name));
					removePlayer();
				}
				else if(indata.charAt(0) == 'c'){
					//Message that should be sent to the clients for translation
					//c(data)
					opponentWriter.println(indata.substring(1));
					opponentWriter.flush();
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
