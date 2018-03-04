package cs455.scaling.nodes;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

public class ClientListener implements Runnable {

    private final Selector selector;
    private final Client client;

    public ClientListener(Selector selector, Client client) {
        this.selector = selector;
        this.client = client;
    }


    @Override
    public void run() {
        while(true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                    SelectionKey key = selectedKeys.iterator().next();

                    if (key.isReadable()) {
                        this.client.read(key);
                    }

                    if (key.isConnectable()) {
                        this.client.connect(key);
                    }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
