package cs455.scaling.nodes;

import java.util.TimerTask;

public class ClientStatsPrinter extends TimerTask {
    private Client client;

    public ClientStatsPrinter(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        client.printStats();
    }
}
