package client;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class ClientTCP extends Thread {
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	public ClientTCP(Socket ecoute) {
		this.socket = ecoute;
		try {
			input = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envoie la commande echo au serveur via le socket
	 * 
	 * @param size
	 *            in octet
	 */
	private void sendEcho(int size) {
		output.println("/echo " + size);
	}

	/**
	 * Envoie la commande ack au serveur via le socket
	 * 
	 * @param size
	 *            in octet
	 */

	private void sendACK(int size) {
		System.out.println("Transmitting ack");
		output.println("/ack " + size);
	}

	private void sendCompute(int size) {
		System.out.println("Transmitting compute");
		output.println("/compute " + size);
	}

	/**
	 * Envoie le message actuel au serveur via le socket
	 * 
	 * @param currMessage
	 *            to send
	 */
	private  void sendMessage(String currMessage) {
		output.println(currMessage);
		output.flush();
	}

	private int checkCommand(String command, int size) {
		if (command.isEmpty()) {
			return -1;
		}
		if (command.startsWith("/")) {
			/* remove the slash */
			command = command.substring(1, command.length() - 1);
		}
		if (command.equals("echo")) {
			sendEcho(size);
		} else if (command.equals("ack")) {
			sendACK(size);
		} else if (command.equals("compute")) {
			sendCompute(size);
		} else {
			return -1;
		}

		return 0;
	}

	public Socket getSocket() {
		return this.socket;
	}
	
	public BufferedReader getInput() {
		return input;
	}
	
	public void closeConnexion() throws IOException {
		if (!socket.isClosed()) {
			this.socket.close();
		}
	}

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		System.out
				.println("./testtcp [cmd] [number] [pas] [IPAddress] [Port]");
		/* init variable */
		PrintWriter myfile = null;
		String cmd;
		int pas;
		char c;
		Scanner sc = new Scanner(System.in);
		long startTime = 0, endTime;
		double difference;
		String command;
		int number;
		String ip,receivedFromServer;
		StringBuilder message;
		Socket socket;
		ClientTCP client;
		/* end init variable */

		try {
			myfile = new PrintWriter("mygraphEchoSameConnection.csv", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		cmd = sc.nextLine();
		if (!cmd.equals(null)) {
			String[] arguments = cmd.split(" ");
			while (arguments.length < 6) {
				System.err
						.println("Wrong number of arguments : ./testtcp [cmd] [numbertoSend] [pas] [IPAddress] [Port]");
				cmd = sc.nextLine();
				arguments = cmd.split(" ");
			}
			if (arguments.length == 6) {
				sc.close();
				command = arguments[1];
				/* number of packet */
				number = Integer.parseInt(arguments[2]);
				pas = Integer.parseInt(arguments[3]);
				ip = arguments[4];
				int port = Integer.parseInt(arguments[5]);
				int cpt = 0;
				//int compteur = 0;
				double debit;
				/* on creer la connexion pour parler au serveur */
					cpt=0;
					message = new StringBuilder();
					Random rdm;
					rdm = new Random();
					socket = new Socket(ip, port);
					client = new ClientTCP(socket);
					for(int i = 1 ; i <= number ; i +=pas) {
						client.checkCommand(command, number);
						cpt=0;
						//compteur = 0;
						/*
						 * on génére une chaine de caractère qu'on va envoyer number
						 * fois au serveur
						 */
						/*
						 * tant que le serveur ne nous repond pas ok cad : tant qu'il
						 * n'a pas fini sa procédure echo
						 */
						c = (char) (rdm.nextInt(26) + 'a');
						startTime = System.currentTimeMillis();
						message.append(c);
						client.sendMessage(message.toString());
						receivedFromServer = client.getInput().readLine();
						while (!receivedFromServer.contains("OK")) {
							while(cpt < i) {
								c = (char) (rdm.nextInt(26) + 'a');
								message.append(c);
								cpt++;
							}
							System.out.println("length : " + message.toString().length());
							client.sendMessage(message.toString());
							//compteur++;
							receivedFromServer = client.getInput().readLine();
						}
						message=new StringBuilder();
						//System.out.println("J'ai envoyé " + compteur +  "fois des messages de " + cpt + " taille");
						endTime = System.currentTimeMillis();
						difference = (endTime - startTime);
						debit = ((number*1000/ difference)/(1024*1024));
						/*client.sendMessage("bye");
						client.getSocket().close();
						socket.close();*/
						myfile.write(Integer.toString(i));
						myfile.write('\t');
						myfile.write(Double.toString(debit));
						myfile.write('\n');
					}
					myfile.close();
					client.sendMessage("bye");
					System.out.println("Travail terminé !");
			}
		}
	}
}
