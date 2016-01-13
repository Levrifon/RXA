package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMasterTCP {
	private ServerSocket socket;
	Socket ecoute;
	InputStreamReader input;
	List<Socket> liste_sockets;
	private PrintWriter print;

	public ServerMasterTCP(int port) {
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!socket.isBound()) {
			System.err.println("Error during bind on port : " + port);
		} else {
			System.out.println("Succesful bind on port: " + port);
		}
		liste_sockets = new ArrayList<Socket>();
	}

	public void handleConnections() throws IOException {
		System.out.println("Starting handle connections method ...");
		while (true) {
			ecoute = socket.accept();
			ServerSlaveTCP slave = new ServerSlaveTCP(ecoute, this);
			System.out.println("New connection from " + ecoute.getInetAddress());
			slave.start();
			liste_sockets.add(ecoute);
		}
	}

	

	public synchronized void repeterMessage(String message, ServerSlaveTCP source)
			throws IOException {
		for (Socket slave : liste_sockets) {
			if(slave.isClosed()) {
				liste_sockets.remove(slave);
			}
			if (!slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(),true);
				print.println(message);
			}
		}
	}

}
