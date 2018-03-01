package cs455.scaling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    private static SocketChannel client;
    private static Client instance;

    public static Client start() {
        if (instance == null)
            instance = new Client();

        return instance;
    }

    public static void stop() throws IOException {
        client.close();
    }

    public Client() {
        try {
            client = SocketChannel.open(new InetSocketAddress("localhost", 5454));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(byte[] msg) {
        ByteBuffer buffer = ByteBuffer.wrap(msg);
        String response = null;
        try {
            client.write(buffer);
            buffer.clear();
            buffer = ByteBuffer.allocate(8192);
            client.read(buffer);
            response = new String(buffer.array()).trim();
            System.out.println("response=" + response);
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;

    }
}
