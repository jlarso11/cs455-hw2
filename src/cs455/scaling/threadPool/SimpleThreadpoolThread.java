package cs455.scaling.threadPool;//import cs455.scaling.transportation.TCPSender;
//import cs455.scaling.cs455.scaling.util.cs455.scaling.util5.scaling.GetSha;

import cs455.scaling.nodes.Server;
import cs455.scaling.util.GetSha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleThreadpoolThread extends Thread {
    private final String threadName;
    private AtomicBoolean execute;
    private SelectionKey selectionKey;
    private ThreadPool threadPool;
    private final Server server;

    public SimpleThreadpoolThread(AtomicBoolean execute, ThreadPool threadPool, String threadName, Server server) {
        this.execute = execute;
        this.threadPool = threadPool;
        this.threadName = threadName;
        this.server = server;
    }

    public void setExecute(AtomicBoolean execute){
        this.execute = execute;
    }

    public void acceptNewTask(SelectionKey selectionKey){
        this.selectionKey = selectionKey;
    }

    public void sendReturnMessage(SelectionKey selectionKey, String hash) {
        try {
           ByteBuffer buffer = ByteBuffer.wrap(hash.getBytes());
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            channel.write(buffer);
            selectionKey.interestOps(SelectionKey.OP_READ);
            server.incrementMessageCounts(selectionKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int read = 0;
        try {
            while (buffer.hasRemaining() && read != -1) {
                read = channel.read(buffer);
            }
        } catch (IOException e) {
            /* Abnormal termination */
            // Cancel the key and close the socket channel
        }
        // You may want to flip the buffer here
        if (read == -1) {
            /* Connection was terminated by the client. */
            channel.close();
            key.cancel();
        }

        return buffer.array();
    }

    @Override
    public void run() {
        try {
            // format based on: https://caffinc.github.io/2016/03/simple-threadpool/
            // Continue to execute when the execute flag is true
            while (execute.get()) {
                if(selectionKey != null) {
                    synchronized (selectionKey) {
//                        System.out.println("Thread on: " + this.threadName);
                        byte[] byteArray = this.read(selectionKey);
                        String hash = GetSha.SHA1FromBytes(byteArray);

                        this.sendReturnMessage(selectionKey, hash);

                        this.selectionKey = null;
                        this.threadPool.threadDoneExecuting(this);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
