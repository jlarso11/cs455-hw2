package cs455.scaling.nodes;

import cs455.scaling.util.CheckInteger;
import cs455.scaling.util.GetSha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Client {
    private SocketChannel client;
    private static Client instance;
    private Selector selector;
    private List<String> hashes;


    private void startClient(String ip, int port) throws IOException {
        hashes = Collections.synchronizedList(new LinkedList<>());
        this.client = SocketChannel.open();
        this.selector = Selector.open();
        this.client.configureBlocking(false);
        this.client.register(selector, SelectionKey.OP_CONNECT);
        this.client.connect(new InetSocketAddress(ip, port));
        ClientListener clientListener = new ClientListener(this.selector, this);
        new Thread(clientListener).start();
    }

    private void stopClient() throws IOException {
        client.close();
        selector.close();
    }

    private void checkIfHashIsInList(String hash) {
        synchronized (hashes) {
            System.out.println(hashes.contains(hash));
            hashes.remove(hash);
        }
    }

    public void read(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(40);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                read = channel.read(buffer);
            }

        } catch (IOException e) {
            System.out.println("error here");
        }
        String hash = new String(buffer.array()).trim();
        this.checkIfHashIsInList(hash);
    }

    public void connect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        channel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
    }

    private byte[] generateBytes(){
        byte[] b = new byte[8192];
        new Random().nextBytes(b);
        return b;
    }

    private void sendMessage(byte[] msg) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(msg);
            this.client.write(buffer);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startConnection(String[] args) throws IOException, InterruptedException {
        if(CheckInteger.isInteger(args[2] ) && CheckInteger.isInteger(args[1])) {
            this.startClient(args[0], Integer.parseInt(args[1]));
            int count = 0;
            while (count < 10) {
                byte[] testData = generateBytes();
                String hash = GetSha.SHA1FromBytes(testData);
                System.out.println(hash);
                synchronized (hashes) {
                    hashes.add(hash);
                }
                this.sendMessage(testData);
                count++;
                Thread.sleep(1000/Integer.parseInt(args[2]));
            }
        } else {
            this.printUsage();
            return;
        }
    }

    private void printUsage(){
        System.out.println("Usage: java cs455.scaling.nodes.Client [nodes-host] [nodes-port] [message-rate]");
        System.out.println("Server port and message must be integer values");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client();
        if(args.length == 3) {
            client.startConnection(args);
        } else {
            client.printUsage();
            return;
        }

    }
}