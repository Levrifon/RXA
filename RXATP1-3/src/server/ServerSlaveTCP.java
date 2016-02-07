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
	private int currentNboctets;
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
		this.currentNboctets = nbOctets;
		if (currentNboctets <= 0) {
			sendEndOfTransmission();
		} else {
			if (cmd.isEmpty()) {
				return;
			}
			currentCommand = cmd;
			if(cmd.equals("echo")) {
				this.echoCommand = true;
			}else if (cmd.equals("ack")) {
				this.ackCommand = true;
			}else if (cmd.equals("compute")) {
				this.cmptCommand = true;
			}else {
				System.out.println("WTF");
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
					if(message.equals("OK")) { sendEndOfTransmission();}
					newMessage = createNewMessage();
					
					if (this.activatedCommand() != null) {
						
						if (this.activatedCommand().equals("echo")) {
							echo(socket, newMessage);
						} else if (this.activatedCommand().equals("ack")) {
							ack(socket, newMessage);
						} else if (this.activatedCommand().equals("compute")) {
							compute(socket,nbOctets);
						} else {
							/* on répète le message sur tous les autres slaves */
							master.repeterMessage(newMessage, socket);
						}
					} else {
						master.repeterMessage(newMessage, socket);
					}
					/* si on envoie une commande */
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
							this.currentNboctets = nbOctets;
							this.toggleCommand("echo", nbOctets);
							break;
						case "/ack":
							this.currentNboctets = nbOctets;
							this.toggleCommand("ack", nbOctets);
							break;
						case "/compute":
							this.currentNboctets = nbOctets;
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
		newMessage = message;
		return newMessage;
	}
	
	public void echo(Socket source, String message) throws IOException {
		String messagerecu;
		int difference;
		System.out.println("message reçu : " + message);
		if ((difference = currentNboctets - message.length()) > 0) {
			this.currentNboctets = currentNboctets - message.length();
		}

		if (difference > 0) {
			output.println(message);
		} else {
			/*
			 * recupere le bout restant de la chaine si le dernier message est
			 * trop grand
			 */
			messagerecu = message.substring(0, currentNboctets);
			output.println(messagerecu);
			sendEndOfTransmission();
		}
	}
	
	public void compute(Socket source, int nb) throws IOException {
		int resultat = 0;
		resultat = fib(nb);
		output.println(resultat);
		sendEndOfTransmission();
	}

	private int fib(int nb) {
		if (nb < 2) {
			return nb;
		} else {
			return fib(nb - 1) + fib(nb - 2);
		}
	}
	public void ack(Socket source, String message) throws IOException {
		this.currentNboctets = currentNboctets - message.length();
		if (currentNboctets < 0) {
			currentNboctets = 0;
		}
		if (currentNboctets == 0) {
			sendEndOfTransmission();
		}
	}
	
	private void sendEndOfTransmission(){
		currentNboctets = 0;
		this.echoCommand = false;
		this.ackCommand = false;
		this.cmptCommand = false;
		currentCommand = "none";
		output.println("OK");
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
