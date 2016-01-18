package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;


public class ServerNIOTCP implements Runnable {
	private ServerSocketChannel channel;
	private ServerSocket socketServer;
	private Socket socket;
	private Selector selector;
	private InetSocketAddress address;
	
	public ServerNIOTCP(int port) {
		try {
			this.selector = this.initSelector();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Selector initSelector() throws IOException {
	    // Create a new selector
	    Selector socketSelector = SelectorProvider.provider().openSelector();

	    // Create a new non-blocking server socket channel
	    this.channel = ServerSocketChannel.open();
	    channel.configureBlocking(false);

	    // Bind the server socket to the specified address and port
	    InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
	    channel.socket().bind(isa);

	    // Register the server socket channel, indicating an interest in 
	    // accepting new connections
	    channel.register(socketSelector, SelectionKey.OP_ACCEPT);

	    return socketSelector;
	  }
	
	public void startStreamReading() {
		BufferedReader input = null;
		ByteBuffer buffer = null;
		PrintWriter output = null;
		String message = "Message reçu : ";
		try {
			input = new BufferedReader(new InputStreamReader(System.in));
			message = input.readLine();
			buffer = ByteBuffer.allocate(2048);
			buffer = ByteBuffer.wrap(message.getBytes());
		} catch (IOException e) {
			System.err.println("Erreur lors de l'instanciation des flux entrées/sorties");
		}
	
		try {
			channel.write(buffer);
		} catch (IOException e) {
			System.err.println("Erreur lors de l'écriture avec le buffer");
		}
		System.out.println();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
