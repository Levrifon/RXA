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
	private String message;
	private ServerMasterTCP master;

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
			output = new PrintWriter(socketFromMaster.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
			System.err
					.println("Erreur lors de la création des entrées/sorties");
		}
	}

	@Override
	public void run() {
		String newMessage;
		try {
			output.println("Hello world !");
			message = "";
			/* tant que la personne veut écrire et n'envoie pas "bye" */
			while (!(message = input.readLine()).startsWith("bye")) {
				/* si le message n'est pas vide ou n'est pas égale a un espace */
				if (!message.startsWith(" ") && !message.isEmpty()
						&& !message.startsWith("/echo")
						&& !message.startsWith("/ack")
						&& !message.startsWith("/compute")) {
					System.out.println("Connexion sur :"
							+ socket.getInetAddress());
					System.out.println("Chaîne reçue : " + message + "\n");
					newMessage = "Message reçu de " + socket.getInetAddress()
							+ " : " + message + "\n";
					/* on répète le message sur tous les autres slaves */
					if (master.activatedCommand() != null) {
						if (master.activatedCommand().equals("echo")) {

						} else {
							master.repeterMessage(newMessage, socket);
						}
					}
				} else {
					String[] array = message.split(" ");
					if (array.length < 2) {
						System.out.println("tata");
					} else {
						String command = array[0];
						int nb = Integer.parseInt(array[1]);
						switch (command) {
						case "/echo":
							master.toggleCommand("echo");
							break;
						case "/ack":
							master.toggleCommand("ack");
							break;
						case "/compute":
							master.toggleCommand("compute");
							break;
						}
					}
				}
			}
			System.out.println("toto");
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

	public Socket getSocket() {
		return this.socket;
	}
}
