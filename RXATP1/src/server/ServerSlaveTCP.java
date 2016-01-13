package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerSlaveTCP extends Thread {
	private Socket socket;
	private BufferedReader input;
	private String message;
	private ServerMasterTCP master;

	public ServerSlaveTCP(Socket socketFromMaster, ServerMasterTCP server) {
		this.socket = socketFromMaster;
		this.master = server;
		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		String newMessage;
		try {
				message = "";
			while (!(message = input.readLine()).startsWith("bye")) {
				if (!message.startsWith(" ") && !message.isEmpty()) {
					System.out.println("Connexion sur :"+ socket.getInetAddress());
					System.out.println("Chaîne reçue : " + message + "\n");
					newMessage = "Message reçu de " + socket.getInetAddress()+ " : " + message + "\n";
					master.repeterMessage(newMessage, this);
				}
			}
			this.socket.close();
		} catch (IOException e) {
			try {
				this.socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
