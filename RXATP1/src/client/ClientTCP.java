package client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientTCP {
	Socket socket;
	public ClientTCP(int port) {
		try {
			socket = new Socket("localhost", port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) {
		
	}
}
