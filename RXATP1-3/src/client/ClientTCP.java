package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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

	private void sendEcho() {
		System.out.println("Transmitting echo");
		output.println("/echo");
	}

	private void sendACK() {
		System.out.println("Transmitting ack");
		output.println("/ack");
	}

	private int checkCommand(String command) {
		if (command.isEmpty()) {
			return -1;
		}
		if (command.startsWith("/")) {
			/* remove the slash */
			command = command.substring(1, command.length() - 1);
		}
		switch (command) {
		case "echo":
			sendEcho();
			break;
		case "ack":
			sendACK();
			break;
		case "compute":
			/* a faire */
			break;
		}
		return 0;
	}

	public static void main(String[] args) {
		String cmd;
		Scanner sc = new Scanner(System.in);
		cmd = sc.nextLine();
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
				String name, command;
				int size,result;
				String ip;
				name = arguments[0];
				command = arguments[1];
				
				size = Integer.parseInt(arguments[2]);
				ip = arguments[3];
				try {
					Socket socket = new Socket(ip, 8080);
					ClientTCP client = new ClientTCP(socket);
					result = client.checkCommand(command);
					System.out.println("result : " + result);
				} catch (UnknownHostException e) {
					System.err
							.println(String
									.format("Il semblerait que le serveur ne soit pas sur ce port (%s)ou sur cette adresse (%s) !",
											8080, ip));
				} catch (IOException e) {
					System.err
							.println("La connection est refusée, le serveur tourne t'il ?");
				}
				System.out.println("toto");
			}
		}

	}

}
