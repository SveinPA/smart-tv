package edu.ntnu.sveiap.idata2304.smarttv.client.app;

import edu.ntnu.sveiap.idata2304.smarttv.client.ui.CliUi;

/**
 * Main application class for the remote client.
 * Expects two command-line arguments: host and port.
 */
public final class RemoteClientApp {
    private RemoteClientApp() {}

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: remote-client <host> <port>");
            System.out.println("Example: remote-client 127.0.0.1 1238");
            return;
        }
        String host = args[0];
        int port = Integer.parseInt(args[1]);

        new CliUi(host, port).run();
    }
}

