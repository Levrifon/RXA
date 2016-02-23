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
		System.out.println("Transmitting echo");
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
	private synchronized void sendMessage(String currMessage) {
		synchronized (currMessage) {
			output.println(currMessage);
			output.flush();
		}
	}

	private boolean hasFinished() throws IOException {
		if (input.readLine() == null) {
			return false;
		}
		return input.readLine().contains("OK");
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

	public static void main(String[] args) throws UnknownHostException,
			IOException {
		System.out
				.println("./testtcp [cmd] [number] [sizeofmessage] [IPAddress]");
		/* init variable */
		PrintWriter myfile = null;
		StringBuffer messagetoSend;
		String cmd;
		int sizeofmessage;
		char c;
		Scanner sc = new Scanner(System.in);
		double startTime = 0, endTime;
		String name, command;
		int number, result;
		String ip;
		Socket socket;
		ClientTCP client;
		/* end init variable */

		try {
			myfile = new PrintWriter("mygraph2.csv", "UTF-8");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		cmd = sc.nextLine();
		if (!cmd.equals(null)) {
			String[] arguments = cmd.split(" ");
			while (arguments.length < 5) {
				System.err
						.println("Wrong number of arguments : ./testtcp [cmd] [numbertoSend] [sizeofmessage] [IPAddress]");
				cmd = sc.nextLine();
				arguments = cmd.split(" ");
			}
			if (arguments.length == 5) {
				name = arguments[0];
				command = arguments[1];
				/* number of packet */
				number = Integer.parseInt(arguments[2]);
				sizeofmessage = 1;
				ip = arguments[4];
				socket = new Socket(ip, 8080);
				client = new ClientTCP(socket);
				client.checkCommand(command, number);
				Random rdm;
				for(int i = 0 ; i < number ; i ++) {
					rdm = new Random();
					c =(char)(rdm.nextInt('a') + 26);
				}
			}
		}
	}
}
