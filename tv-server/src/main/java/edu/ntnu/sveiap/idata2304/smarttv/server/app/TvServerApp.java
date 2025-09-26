package edu.ntnu.sveiap.idata2304.smarttv.server.app;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.server.adapter.ProtocolHandler;
import edu.ntnu.sveiap.idata2304.smarttv.server.transport.TcpServer;

/**
 * Main application class for the TV server.
 * Starts a TCP server on the specified port (default 1238).
 * Accepts an optional command-line argument to specify a different port.
 */
public final class TvServerApp {
    private TvServerApp() {}

    public static void main(String[] args) throws Exception {
        int port = 1238;
        if (args.length >= 2 && "--port".equals(args[0])) {
            port = Integer.parseInt(args[1]);
        }
        System.out.println("[TvServerApp] Starting on port " + port);

        SmartTv tv = new SmartTv(10);

        ProtocolHandler handler = new ProtocolHandler(tv);
        TcpServer server = new TcpServer(port, handler);

        server.start();

    }
}
