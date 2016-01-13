package main;

import java.io.IOException;

import server.ServerMasterTCP;

public class Main {
	public static void main(String[] args) {
		ServerMasterTCP serv = new ServerMasterTCP(8080);
		try {
			serv.handleConnections();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
