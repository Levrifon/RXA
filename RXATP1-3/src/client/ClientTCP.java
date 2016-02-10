package client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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
	 * @param size in octet
	 */
	private void sendEcho(int size) {
		System.out.println("Transmitting echo");
		output.println("/echo " + size);
	}
	/**
	 * Envoie la commande ack au serveur via le socket
	 * @param size in octet
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
	 * @param currMessage to send
	 */
	private void sendMessage(String currMessage) {
		output.println(currMessage);
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

	public static void main(String[] args) {
		System.out.println("./testtcp [cmd] [number] [sizeofmessage] [IPAddress]");
		PrintWriter myfile = null;
		try {
			myfile = new PrintWriter("mygraph.csv","UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String cmd;
		int sizeofmessage;
		char c;
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		cmd = sc.nextLine();
		List<String> messages = new ArrayList<String>();
		long startTime, endTime;
		if (!cmd.equals(null)) {
			String[] arguments = cmd.split(" ");
			while (arguments.length < 5) {
				System.err
						.println("Wrong number of arguments : ./testtcp [cmd] [number] [sizeofmessage] [IPAddress]");
				cmd = sc.nextLine();
				arguments = cmd.split(" ");
			}
			if (arguments.length == 5) {
				@SuppressWarnings("unused")
				String name, command;
				int number, result;
				String ip;
				name = arguments[0];
				command = arguments[1];
				number = Integer.parseInt(arguments[2]);
				sizeofmessage = 1;
				ip = arguments[4];
				try {
					for(int j = 1 ; j < number ;  j++) {
						Socket socket = new Socket(ip, 8080);
						ClientTCP client = new ClientTCP(socket);
						result = client.checkCommand(command, number*sizeofmessage);
						StringBuffer messagetoSend = new StringBuffer(sizeofmessage);
						Random rdm = new Random();
						
						for(int i = 0 ; i < j ; i++) {
							rdm = new Random();
							c = (char)(rdm.nextInt(26) + 'a');
							messagetoSend.append(c);
						}
						startTime = System.currentTimeMillis();
						client.sendMessage(messagetoSend.toString());
						endTime = System.currentTimeMillis();
						
						/* conversion en secondes */
						float difference = (endTime - startTime);
						
						System.out.println("Time exec : " + difference + "ms");
						
						if (difference == 0) { /* evite la division par zero lors du calcul de perfs */
							difference = 1;
						}
						float debit = (number*sizeofmessage /difference)/976;
						System.out.println(String.format(
								"Sent %d bytes in %f so : %fMB/s", number*sizeofmessage, difference, debit));
						myfile.write("" +j);
						myfile.write(" ");
						myfile.write("" + debit);
						myfile.write('\n');
					}
				} catch (UnknownHostException e) {
					System.err
							.println(String
									.format("Il semblerait que le serveur ne soit pas sur ce port (%s)ou sur cette adresse (%s) !",
											8080, ip));
				} catch (IOException e) {
					System.err
							.println("La connection est refusée, le serveur tourne t'il ?");
				}
			}
		}
	}
}
