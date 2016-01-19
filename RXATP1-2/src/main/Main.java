package main;

import server.ServerNIOTCP;

public class Main {
	public static void main(String[] args) {
		ServerNIOTCP server = new ServerNIOTCP(8080);
	}

}
