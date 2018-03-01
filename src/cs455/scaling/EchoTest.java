package cs455.scaling;

import cs455.scaling.util.GetSha;
import org.junit.*;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class EchoTest {

    Process server;
    Client client;

    @Before
    public void setup() throws IOException, InterruptedException {
        //server = cs455.scaling.Server.start();
        client = Client.start();
    }

    private byte[] generateBytes(){
        byte[] b = new byte[8192];
        new Random().nextBytes(b);
        return b;
    }

    @Test
    public void testWithStrings() {

        while(true) {
            byte[] testData = generateBytes();
            String hash = GetSha.SHA1FromBytes(testData);

            String resp3 = client.sendMessage(testData);
            assertEquals(hash, resp3);
        }
    }

    @After
    public void teardown() throws IOException {
        //server.destroy();
        Client.stop();
    }
}
