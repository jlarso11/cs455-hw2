package cs455.scaling.server;

import cs455.scaling.client.Client;

import java.util.TimerTask;

public class StatsPrinter extends TimerTask {
    private final Client client;
    private final Server server;

    public StatsPrinter(Client client) {
        this.client = client;
        this.server = null;
    }

    public StatsPrinter(Server server) {
        this.client = null;
        this.server = server;
    }

    @Override
    public void run() {
        if(client != null) {
            client.printStats();
        } else {
            server.printStats();
        }

    }
}
