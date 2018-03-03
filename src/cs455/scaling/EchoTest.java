package cs455.scaling;

import cs455.scaling.util.GetSha;
import org.junit.*;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class EchoTest {

    Client client;

    @Before
    public void setup() throws IOException{
        client = new Client();
        client.startClient();
    }

    private byte[] generateBytes(){
        byte[] b = new byte[8192];
        new Random().nextBytes(b);
        return b;
    }

    @Test
    public void testWithStrings() throws InterruptedException {
        int count = 0;
        while(count < 10) {
            byte[] testData = generateBytes();
            String hash = GetSha.SHA1FromBytes(testData);
            System.out.println("hash: " + hash);
            client.sendMessage(testData);
            //assertEquals(hash, resp3);
            count++;
            Thread.sleep(500);
        }
    }

//    @After
//    public void tearDown() throws IOException {
//        client.stopClient();
//    }
}
