package cs455.scaling;//import cs455.scaling.transportation.TCPSender;
//import cs455.scaling.cs455.scaling.util.cs455.scaling.util5.scaling.GetSha;

import cs455.scaling.util.GetSha;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimpleThreadpoolThread extends Thread {
    private final String threadName;
    private AtomicBoolean execute;
    private ThreadTask threadTask;
    private ThreadPool threadPool;

    public SimpleThreadpoolThread(AtomicBoolean execute, ThreadPool threadPool, String threadName) {
        this.execute = execute;
        this.threadPool = threadPool;
        this.threadName = threadName;
    }

    public void setExecute(AtomicBoolean execute){
        this.execute = execute;
    }

    public void acceptNewTask(ThreadTask threadTask){
        this.threadTask = threadTask;
    }

    public void sendReturnMessage(ThreadTask threadTask, String hash) {
        try {
           ByteBuffer buffer = ByteBuffer.wrap(hash.getBytes());
            SelectionKey key = threadTask.getKey();
            SocketChannel channel = (SocketChannel) key.channel();
            channel.write(buffer);
            key.interestOps(SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // format based on: https://caffinc.github.io/2016/03/simple-threadpool/
            // Continue to execute when the execute flag is true
            while (execute.get()) {
                if(threadTask != null) {
                    synchronized (threadTask) {
                        System.out.println("Thread on: " + this.threadName);
                        String hash = GetSha.SHA1FromBytes(threadTask.getBytes());

                        this.sendReturnMessage(threadTask, hash);

                        this.threadTask = null;
                        this.threadPool.threadDoneExecuting(this);
                    }
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }
}
