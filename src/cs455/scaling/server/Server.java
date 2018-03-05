package cs455.scaling.server;

import cs455.scaling.threadPool.ThreadPool;
import cs455.scaling.util.CheckInteger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
    private final ThreadPool threadPool;
    private Selector selector;
    private final AtomicInteger totalMessagesProcessed;
    private final Map<Integer, AtomicInteger> individualResults;
    private final Map<Integer, Boolean> threadBeingRead;

    public Server() {
        threadPool = new ThreadPool(this);
        individualResults = new HashMap<>();
        threadBeingRead = new HashMap<>();
        totalMessagesProcessed = new AtomicInteger(0);
    }

    public void startPool(int i) {
        threadPool.initializePool(i);
    }

    public boolean checkIfThreadIsBeingRead(SelectionKey key) {
        synchronized (threadBeingRead) {
            if(threadBeingRead.containsKey(key.hashCode())) {
                return threadBeingRead.get(key.hashCode());
            }
            return false;
        }
    }

    public void updateThreadIsBeingRead(SelectionKey key, boolean isRead) {
        synchronized (threadBeingRead) {
            threadBeingRead.put(key.hashCode(), isRead);
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel servSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = servSocket.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
    }

    public void incrementMessageCounts(SelectionKey key) {
        synchronized (totalMessagesProcessed) {
            synchronized (individualResults) {
                totalMessagesProcessed.incrementAndGet();
                individualResults.get(key.hashCode()).incrementAndGet();
            }
        }
    }

    private void resetCounters(){
        synchronized (totalMessagesProcessed) {
            synchronized (individualResults) {
                totalMessagesProcessed.set(0);
                for(AtomicInteger ai : individualResults.values()) {
                    ai.set(0);
                }
            }
        }
    }

    public void startSelector(int port) throws IOException {
        Timer timer = new Timer();
        timer.schedule(new StatsPrinter(this), 20000, 20000);

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
//            System.out.println(selectedKeys.size());
//            System.out.println("the other count " + selector.keys().size());
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

    private void read(SelectionKey key) {
        synchronized (individualResults) {
            if(!individualResults.containsKey(key.hashCode())) {
                individualResults.put(key.hashCode(), new AtomicInteger(0));
            }
        }
        this.threadPool.addTask(key);
    }

    private void printUsage(){
        System.out.println("Usage: java cs.455.scaling.nodes.Server [portnum] [thread-pool-size]");
        System.out.println("Both arguments must be integers");
    }

    private double getStandardDeviation(double throughPutAverage) {
        double total = 0.0;
        for(AtomicInteger ai : this.individualResults.values()){
            total += Math.pow((ai.get()-throughPutAverage),2);
        }
        return Math.sqrt(total/individualResults.size())/20.0;
    }

    public void printStats() {
        synchronized (totalMessagesProcessed) {
            synchronized (individualResults) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                double throughput = totalMessagesProcessed.get()/20.0;

                int numOfKeys = individualResults.size();
                double throughPutAverage = throughput/numOfKeys;
                double standardDeviation = this.getStandardDeviation(throughPutAverage);
                System.out.println(timestamp + " Server Throughput: " + throughput + ", Active Client Connections: : "
                        + numOfKeys + ", Mean Per-client Throughput: " + throughPutAverage +
                        ", Std. Dev. of Per-client Throughput: " + standardDeviation);

                this.resetCounters();
            }
        }
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

