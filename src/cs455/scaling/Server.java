package cs455.scaling;

import cs455.scaling.util.CheckInteger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {
    private final ThreadPool threadPool;
    private Selector selector;

    public Server() {
        threadPool = new ThreadPool();
    }

    public void startPool(int i) {
        threadPool.initializePool(i);
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = servSocket.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                read = channel.read(buffer);
            }
            ThreadTask threadTask = new ThreadTask(key, buffer.array());
            threadPool.addTask(threadTask);
        } catch (IOException e) {
            /* Abnormal termination */
            // Cancel the key and close the socket channel
        }
        // You may want to flip the buffer here
        if (read == -1) {
            /* Connection was terminated by the client. */
            channel.close();
            key.cancel();
            return;
        }
        key.interestOps(SelectionKey.OP_WRITE);
    }

    public void startSelector(int port) throws IOException {
        this.selector = Selector.open();

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Listening on IP: " + InetAddress.getLocalHost().getHostAddress() + " with open port : #" + port);

        while (true) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {

                SelectionKey key = iter.next();

                if (key.isAcceptable()) {
                    accept(key);
                }

                if (key.isReadable()) {
                    read(key);
                }
                iter.remove();
            }
        }
    }

    private void printUsage(){
        System.out.println("Usage: java cs.455.scaling.nodes.Server [portnum] [thread-pool-size]");
        System.out.println("Both arguments must be integers");
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        if(args.length == 2 && CheckInteger.isInteger(args[0]) && CheckInteger.isInteger(args[1])) {
            server.startPool(Integer.parseInt(args[1]));
            server.startSelector(Integer.parseInt(args[0]));
        } else {
            server.printUsage();
            return;
        }
    }
}

