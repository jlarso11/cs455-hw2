package cs455.scaling.threadPool;

import cs455.scaling.server.Server;

import java.nio.channels.SelectionKey;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {

    private ConcurrentLinkedQueue<SimpleThreadpoolThread> threads;
    private List<SelectionKey> queuedTasks;
    private final Server server;

    public ThreadPool(Server server) {
        this.threads = new ConcurrentLinkedQueue<>();
        this.queuedTasks = Collections.synchronizedList(new LinkedList<>());
        this.server = server;
    }

    public void initializePool(int threadCount){
        synchronized (threads){
            for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                SimpleThreadpoolThread thread = new SimpleThreadpoolThread(new AtomicBoolean(true), this, "thread " + threadIndex, this.server);
                thread.start();
                this.threads.add(thread);
            }
        }
    }

    public void addTask(SelectionKey selectionKey) {
        this.assignThread(selectionKey);
    }

    private void assignThread(SelectionKey selectionKey) {
        synchronized (threads){
            if(!this.server.checkIfThreadIsBeingRead(selectionKey)) {
                this.server.updateThreadIsBeingRead(selectionKey, true);
                if (threads.size() == 0) {
                    queuedTasks.add(selectionKey);
                } else {
                    SimpleThreadpoolThread thread = threads.poll();
                    thread.acceptNewTask(selectionKey);
                }
            }
        }
    }

    public void threadDoneExecuting(SimpleThreadpoolThread thread, SelectionKey selectionKey) {
        synchronized (threads) {
            synchronized (queuedTasks) {
                this.server.updateThreadIsBeingRead(selectionKey, false);
                if (queuedTasks.size() > 0) {
                    thread.acceptNewTask(queuedTasks.remove(0));
                } else {
                    threads.add(thread);
                }
            }
        }
    }

    public void shutdownPool() {
        synchronized (threads) {
            while(threads.peek() != null) {
                threads.poll().setExecute(new AtomicBoolean(false));
            }
        }
    }
}
