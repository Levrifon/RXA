package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMasterTCP {
	private ServerSocket socket;
	Socket ecoute;
	List<Socket> liste_sockets;
	
	/**
	 * Constructeur du Master possedant une liste de sockets
	 * @param port de branchement du ServerSocket
	 */
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

	/**
	 * Permet de lancer les slaves dans des threads séparés
	 * @throws IOException
	 */
	public void handleConnections() throws IOException {
		System.out.println("Starting handle connections method ...");
		ServerSlaveTCP slave;
		while (true) {
			ecoute = socket.accept();
			liste_sockets.add(ecoute);
			slave = new ServerSlaveTCP(ecoute, this);
			System.out.println("New connection from " + ecoute.getInetAddress()+"," +ecoute.getPort());
			slave.start();
		}
	}

	/**
	 * 
	 * @param slave a retirer de la liste des sockets
	 */
	public void removeSlave(ServerSlaveTCP slave) {
		if(!slave.isInterrupted()) {
			slave.interrupt();
		}
		liste_sockets.remove(slave.getSocket());
	}

	/**
	 * Permet de répeter le message à tous les autres clients du chat en parcourant la liste_sockets
	 * @param message a recopier
	 * @param source de l'emmeteur
	 * @throws IOException
	 */
	public synchronized void repeterMessage(String message, Socket source)
			throws IOException {
		PrintWriter print;
		for (Socket slave : liste_sockets) {
			if (!slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(),true);
				print.println(message);
				System.out.println("Taille en message :" + liste_sockets.size());
			}
		}
	}

}
