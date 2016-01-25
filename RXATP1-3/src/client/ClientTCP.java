package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import server.ServerMasterTCP;

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
		output.println("/echo");
	}

	public static void main(String[] args){
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
				int size;
				String ip;
				name = arguments[0];
				command = arguments[1];
				size = Integer.parseInt(arguments[2]);
				ip = arguments[3];
				try {
					Socket socket = new Socket(ip, 8080);
				} catch (UnknownHostException e) {
					System.err.println(String.format("Il semblerait que le serveur ne soit pas sur ce port (%s)ou sur cette adresse (%s) !",8080,ip));
				} catch (IOException e) {
					System.err.println("La connection est refusée, le serveur tourne t'il ?");
				}
				System.out.println("toto");
			}
		}

	}
}
