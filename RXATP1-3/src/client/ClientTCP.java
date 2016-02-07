package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
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

	private void sendEcho(int size) {
		System.out.println("Transmitting echo");
		output.println("/echo " + size);
	}

	private void sendACK(int size) {
		System.out.println("Transmitting ack");
		output.println("/ack " + size);
	}

	private void sendMessage(String currMessage) {
		output.println(currMessage);
		System.out.println("Message sent");

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
			/* a faire */
		} else {
			return -1;
		}

		return 0;
	}
	public Socket getSocket() {
		return this.socket;
	}
	
	private void getIncommingMessage() throws IOException {
		String message;
		message = input.readLine();
		while(message != null) {
			System.out.println(message);
			message = input.readLine();
		}
	}

	public static void main(String[] args) {
		String cmd;
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		cmd = sc.nextLine();
		List<String> messages = new ArrayList<String>();
		messages.add("Toto");
		messages.add("Titi");
		messages.add("Tutu");
		messages.add("Tata");
		long startTime, endTime;
		if (!cmd.equals(null)) {
			String[] arguments = cmd.split(" ");
			System.out.println(arguments.length);
			while (arguments.length < 4) {
				System.err
						.println("Wrong number of arguments : ./testtcp [cmd] [size] [IPAddress]");
				cmd = sc.nextLine();
				arguments = cmd.split(" ");
			}
			if (arguments.length == 4) {
				@SuppressWarnings("unused")
				String name, command;
				int size, result;
				String ip;
				name = arguments[0];
				command = arguments[1];

				size = Integer.parseInt(arguments[2]);
				ip = arguments[3];
				try {
					Socket socket = new Socket(ip, 8080);
					ClientTCP client = new ClientTCP(socket);
					result = client.checkCommand(command, size);
					startTime = System.currentTimeMillis();
					for (String currMessage : messages) {
						client.sendMessage(currMessage);
					}
					endTime = System.currentTimeMillis();
					long difference = endTime - startTime;
					if (difference == 0) {
						difference = 1;
					}
					long debit = size / ((difference));
					System.out.println(String.format(
							"Sent %d kbytes in %d so : %dkB/s", size, endTime
									- startTime, debit));
					client.getIncommingMessage();
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
