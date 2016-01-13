package server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP extends Thread {
	private ServerSocket socket;
	Socket ecoute;
	BufferedReader inMessage;
	DataOutputStream outMessage;

	public ServerTCP(int port) {
		try {
			this.socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!socket.isBound()) {
			System.err.println("Erreur lors du bind au port : " + port);
		}
	}

	@Override
	public void start() {
		try {
			initListen();
			initInput();
			initOutput();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void initListen() throws IOException {
		ecoute = socket.accept();
	}

	public void initInput() throws IOException {
		inMessage = new BufferedReader(new InputStreamReader(
				ecoute.getInputStream()));
	}

	public void initOutput() throws IOException {
		String messageClient;
		outMessage = new DataOutputStream(ecoute.getOutputStream());
		messageClient = inMessage.readLine();
		outMessage.writeBytes(messageClient + " reçu ! \n");
	}

}
