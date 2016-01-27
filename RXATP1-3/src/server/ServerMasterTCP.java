package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerMasterTCP {
	private ServerSocket socket;
	private Socket ecoute;
	private List<Socket> liste_sockets;
	private boolean echoCommand, ackCommand, cmptCommand;
	private int currentNboctets, nboctetsOrigin;
	private String currentCommand;

	/**
	 * Constructeur du Master possedant une liste de sockets
	 * 
	 * @param port
	 *            de branchement du ServerSocket
	 */
	public ServerMasterTCP(int port) {
		try {
			this.socket = new ServerSocket(port);
			System.out.println("Socket : " + this.socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!socket.isBound()) {
			System.err.println("Error during bind on port : " + port);
		} else {
			System.out.println("Succesful bind on port: " + port);
		}
		liste_sockets = new ArrayList<Socket>();
		echoCommand = false;
		ackCommand = false;
		cmptCommand = false;
		currentCommand = null;
		currentNboctets = 0;
	}

	/**
	 * Permet de lancer les slaves dans des threads séparés
	 * 
	 * @throws IOException
	 */
	public void handleConnections() throws IOException {
		System.out.println("Starting handle connections method ...");
		ServerSlaveTCP slave;
		while (true) {
			ecoute = socket.accept();
			liste_sockets.add(ecoute);
			slave = new ServerSlaveTCP(ecoute, this);
			System.out.println("New connection from " + ecoute.getInetAddress()
					+ "," + ecoute.getPort());
			slave.start();
		}
	}

	public void toggleCommand(String cmd, int nbOctets) {
		System.out.println("Entering in toggleCommand");
		System.out.println("Your command is :"+ cmd);
		this.currentNboctets = nbOctets;
		if (currentNboctets <= 0) {
			this.echoCommand = false;
			this.ackCommand = false;
			this.cmptCommand = false;
			currentCommand = "none";
		} else {
			if (cmd.equals("")) {
				return;
			}
			currentCommand = cmd;
			if(cmd.equals("echo")) {
				this.echoCommand = true;
				this.nboctetsOrigin = nbOctets;
			}else if (cmd.equals("ack")) {
				this.ackCommand = true;
				this.nboctetsOrigin = nbOctets;
			}else if (cmd.equals("compute")) {
				this.cmptCommand = true;
				this.nboctetsOrigin = nbOctets;
			} 
		}
	}

	public boolean isActivated(String cmd) {
		if(cmd.equals("echo")) {
			return echoCommand;
		}else if (cmd.equals("ack")) {
			return ackCommand;
		}else if (cmd.equals("compute")) {
			return cmptCommand;
		}else {
			return false;
		}
	}

	public String activatedCommand() {
		return currentCommand;
	}

	public void echo(Socket source, String message) throws IOException {
		PrintWriter print;
		String messagerecu;
		int difference;
		if ((difference = currentNboctets - message.length()) > 0) {
			this.currentNboctets = currentNboctets - message.length();
		}
		print = new PrintWriter(source.getOutputStream(), true);

		if (difference > 0) {
			print.println(message);
		} else {
			/*
			 * recupere le bout restant de la chaine si le dernier message est
			 * trop grand
			 */
			messagerecu = message.substring(0, currentNboctets);
			print.println(messagerecu);
			print.println("OK");
			toggleCommand("none", 0);
		}
	}

	public void ack(Socket source, String message) throws IOException {
		PrintWriter print;
		this.currentNboctets = currentNboctets - message.length();
		if (currentNboctets < 0) {
			currentNboctets = 0;
		}
		print = new PrintWriter(source.getOutputStream(), true);
		if (currentNboctets == 0) {
			print.println("ok");
			toggleCommand("none", 0);
		}
	}

	public void compute(Socket source, int nb) throws IOException {
		PrintWriter print = null;
		int resultat = 0;
		print = new PrintWriter(source.getOutputStream(), true);
		resultat = fib(nb);
		print.println(resultat);
	}

	private int fib(int nb) {
		if (nb < 2) {
			return nb;
		} else {
			return fib(nb - 1) + fib(nb - 2);
		}
	}

	/**
	 * 
	 * @param slave
	 *            a retirer de la liste des sockets
	 */
	public void removeSlave(ServerSlaveTCP slave) {
		if (!slave.isInterrupted()) {
			slave.interrupt();
		}
		liste_sockets.remove(slave.getSocket());
	}

	/**
	 * Permet de répeter le message à tous les autres clients du chat en
	 * parcourant la liste_sockets
	 * 
	 * @param message
	 * @param source
	 * @throws IOException
	 */
	public synchronized void repeterMessage(String message, Socket source)
			throws IOException {
		PrintWriter print;
		for (Socket slave : liste_sockets) {
			if (!slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(), true);
				print.println(message);
			}
		}
	}

	public ServerSocket getSocket() {
		return this.socket;
	}
}
