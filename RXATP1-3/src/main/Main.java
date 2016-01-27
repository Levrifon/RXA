package main;

import java.io.IOException;

import server.ServerMasterTCP;

public class Main {
	public static void main(String[] args) throws IOException {
		ServerMasterTCP master = new ServerMasterTCP(8080);
		master.handleConnections();
	}

}
