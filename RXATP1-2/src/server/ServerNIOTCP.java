package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class ServerNIOTCP {
	private ServerSocketChannel serverSocket;
	private SelectionKey key;
	private Selector selector;
	private InetSocketAddress address;

	public ServerNIOTCP(int port) {
		try {
			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			address = new InetSocketAddress("localhost", port);
			serverSocket.bind(address);
			serverSocket.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		int ops = serverSocket.validOps();
		try {
			key = serverSocket.register(selector, ops, null);
		} catch (ClosedChannelException e1) {
			e1.printStackTrace();
		}
		for (;;) {

			System.out.println("Waiting for connections...");
			int noOfKeys = 0;
			try {
				noOfKeys = selector.select();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			System.out.println("Number of connections: " + noOfKeys);

			Set selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iter = selectedKeys.iterator();
			while (iter.hasNext()) {
				SelectionKey ky = iter.next();
				if (ky.isAcceptable()) {
					SocketChannel client = null;
					try {
						client = serverSocket.accept();
						client.configureBlocking(false);
						client.register(selector, SelectionKey.OP_READ);
					} catch (IOException e2) {
						e2.printStackTrace();
					}
				} else if (ky.isReadable()) {
					SocketChannel client = (SocketChannel) ky.channel();
					ByteBuffer buffer = ByteBuffer.allocate(256);
					try {
						client.read(buffer);
					} catch (IOException e) {
						e.printStackTrace();
					}
					String output = new String(buffer.array()).trim();

					System.out.println("Message read from client: " + output);

					if (output.equals("Bye.")) {
						try {
							client.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.out
								.println("Bye bye !.");
					}
				}
				iter.remove();
			}
		}
	}
}
