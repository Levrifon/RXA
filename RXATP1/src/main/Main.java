package main;

import java.io.IOException;

import server.ServerMasterTCP;
/**
 * 
 * @author remy
 * Programme qui déroule tout l'algo, il suffit ensuite de lancer des telnet en localhost sur le port 8080 pour écrire, envoyer bye pour quitter
 */
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
