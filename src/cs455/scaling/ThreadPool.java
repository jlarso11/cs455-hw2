package cs455.scaling;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ThreadPool {

    private ConcurrentLinkedQueue<SimpleThreadpoolThread> threads;

    private ConcurrentLinkedQueue<ThreadTask> queuedTasks;

    public ThreadPool() {
        this.threads = new ConcurrentLinkedQueue<>();
        this.queuedTasks = new ConcurrentLinkedQueue<>();
    }

    public void initializePool(int threadCount){
        synchronized (threads){
            for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                SimpleThreadpoolThread thread = new SimpleThreadpoolThread(new AtomicBoolean(true), this, "thread " + threadIndex);
                thread.start();
                this.threads.add(thread);
            }
        }
    }

    public void addTask(ThreadTask threadTask) {
        this.assignThread(threadTask);
    }

    private void assignThread(ThreadTask threadTask) {
        synchronized (threads){
            if (threads.size() == 0) {
                queuedTasks.add(threadTask);
            } else {
                SimpleThreadpoolThread thread = threads.poll();
                thread.acceptNewTask(threadTask);
            }
        }
    }

    public void threadDoneExecuting(SimpleThreadpoolThread thread) {
        synchronized (threads) {
            synchronized (queuedTasks) {
                if (queuedTasks.size() > 0) {
                    thread.acceptNewTask(queuedTasks.poll());
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

    public static void main(String[] args) {
        ThreadPool threadPool = new ThreadPool();
        threadPool.initializePool(10);
        int count = 0;
        while(count < 1000) {
            threadPool.addTask(new ThreadTask(null, new byte[20]));
            count++;
        }

    }
}
