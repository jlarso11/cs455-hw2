package cs455.scaling;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ThreadTask {

    private final SelectionKey key;
    private final byte[] bytes;

    public ThreadTask(SelectionKey key, byte[] bytes) {
        this.key = key;
        this.bytes = bytes;
    }


    public SelectionKey getKey() {
        return key;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
