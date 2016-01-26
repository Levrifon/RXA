package main;

import java.io.IOException;

import server.ServerMasterTCP;

public class Main {
	public static void main(String[] args) {
		ServerMasterTCP master = new ServerMasterTCP(8080);
		try {
			master.handleClientsConnections();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
