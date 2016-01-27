package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerSlaveTCP extends Thread {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;
	private String message,currentCommand;
	private ServerMasterTCP master;
	private int nbOctetsrestant,currentNboctets,nboctetsOrigin;
	private boolean echoCommand,ackCommand,cmptCommand;

	/**
	 * 
	 * @param socketFromMaster
	 *            socket créé avec le accept()
	 * @param server
	 *            Master du serveur tcp
	 */
	public ServerSlaveTCP(Socket socketFromMaster, ServerMasterTCP server) {
		this.socket = socketFromMaster;
		this.master = server;
		this.nbOctetsrestant = 0;
		try {
			/* initialisation des entrées sorties */
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
			System.err
					.println("Erreur lors de la création des entrées/sorties");
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

	@Override
	public void run() {
		String newMessage;
		String recu;
		int nbOctets = 0;
		try {
			output.println("Hello world !");
			
			message = "";
			/* tant que la personne veut écrire et n'envoie pas "bye" */
			while (!(message = input.readLine()).startsWith("bye")) {
				/*
				 * si le message n'est pas vide ou n'est pas égale a un espace
				 * ni a une commande
				 */
				if (isStandardMessage(message)) {
					if(message.equals("OK")) { this.toggleCommand("none", 0);}
					newMessage = createNewMessage();
					System.out.println(this.activatedCommand());
					
					if (this.activatedCommand() != null) {
						
						if (this.activatedCommand().equals("echo")) {
							System.out.println("Commande activée : echo ");
							master.echo(socket, newMessage);
							/*master.toggleCommand("none", nbOctets);*/
							System.out.println("echo command activated");
							
						} else if (this.activatedCommand().equals("ack")) {
							System.out.println("ack command activated");
							master.ack(socket, newMessage);
							
						} else if (this.activatedCommand().equals("compute")) {
							System.out.println("compute command activated");
							master.compute(socket,nbOctets);
							
						} else {
							/* on répète le message sur tous les autres slaves */
							master.repeterMessage(newMessage, socket);
						}
					}
				} else {
					String[] array = message.split(" ");
					if (array.length < 2) {
						output.println("command usage : /cmd [nb bits]");
					} else {
						String command = array[0];
						try {
							nbOctets = Integer.parseInt(array[1]);
						} catch (NumberFormatException e) {
							output.println("expected Integer but was String");
							nbOctets = 10;
						}
						switch (command) {
						case "/echo":
							this.toggleCommand("echo", nbOctets);
							break;
						case "/ack":
							this.toggleCommand("ack", nbOctets);
							break;
						case "/compute":
							this.toggleCommand("compute", nbOctets);
							break;
						}
					}
				}
			}
			message = socket.getInetAddress() + "," + socket.getPort()
					+ " is leaving";
			master.removeSlave(this);
			master.repeterMessage(message, socket);
			this.interrupt();
			socket.close();
			return;
		} catch (IOException e) {
			try {
				/* catch dans un catch .. intéressant */
				this.socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Renvoie simplement le message original 
	 * @return newMessage 
	 */
	private String createNewMessage() {
		String newMessage;
		System.out.println("Connexion sur :" + socket.getInetAddress());
		System.out.println("Chaîne reçue : " + message + "\n");
		newMessage = message;
		return newMessage;
	}
	/**
	 * Renvoie vrai si le message est un message normal (non vide ou pas de commandes)
	 * @param message
	 * @return isStandardMessage
	 */
	private boolean isStandardMessage(String message) {
		return !message.startsWith(" ") && !message.isEmpty()
				&& !message.startsWith("/echo") && !message.startsWith("/ack")
				&& !message.startsWith("/compute");
	}

	public Socket getSocket() {
		return this.socket;
	}
}
