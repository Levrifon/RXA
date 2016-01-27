package main;

import java.io.IOException;

import server.ServerMasterTCP;

public class Main {
	public static void main(String[] args) {
		ServerMasterTCP master = new ServerMasterTCP(8080);
		try {
			/* uncomment this line to handle standard SlaveTCP connections
			master.handleConnections() (and comment the next line also)
			*/
			master.handleClientsConnections();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
