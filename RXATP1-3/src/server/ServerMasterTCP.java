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
	private int nboctets;
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

	public void toggleCommand(String cmd) {
		this.echoCommand = false;
		this.ackCommand = false;
		this.cmptCommand = false;
		if (cmd.equals("")) {
			return;
		}
		currentCommand = cmd;
		switch (cmd) {
		case "echo":
			this.echoCommand = true;
			System.out.println("Coucou echo");
			break;
		case "ack":
			this.ackCommand = true;
			System.out.println("Coucou ack");
			break;
		case "compute":
			this.cmptCommand = true;
			System.out.println("Coucou compute");
			break;
		case "none":
			break;
		default:
			break;
		}
	}

	public boolean isActivated(String cmd) {
		switch (cmd) {
		case "echo":
			return echoCommand;
		case "ack":
			return ackCommand;
		case "compute":
			return cmptCommand;
		default:
			return false;
		}
	}

	public String activatedCommand() {
		return currentCommand;
	}

	public void echo(Socket source, String message, int nbOctets)
			throws IOException {
		PrintWriter print;
		for (Socket slave : liste_sockets) {
			if (slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(), true);
				if (message.length() < nbOctets) {
					print.println(message);
				} else {
					message = message.substring(0, nbOctets);
					print.println(message);
				}
			}
		}
	}

	public void ack(Socket source, String message, int nbOctets)
			throws IOException {
		PrintWriter print;
		for (Socket slave : liste_sockets) {
			if (slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(), true);
				if (message.length() == nbOctets) {
					print.println("ok");
				} else if (message.length() < nbOctets) {
					if (message.length() == 0) {
						print.println("nok");
					} else {
						print.println("nok something is missing");
					}
				}
			}
		}
	}

	public void compute(Socket source, int nb) throws IOException {
		PrintWriter print = null;
		int resultat = 0;
		for (Socket slave : liste_sockets) {
			if (slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(), true);
				resultat = fib(nb);
			}
			print.println(resultat);
		}
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
	 *            a recopier
	 * @param source
	 *            de l'emmeteur
	 * @throws IOException
	 */
	public synchronized void repeterMessage(String message, Socket source)
			throws IOException {
		PrintWriter print;
		for (Socket slave : liste_sockets) {
			if (!slave.equals(source)) {
				print = new PrintWriter(slave.getOutputStream(), true);
				print.println(message);
				System.out
						.println("Taille en message :" + liste_sockets.size());
			}
		}
	}

	public ServerSocket getSocket() {
		return this.socket;
	}
}
