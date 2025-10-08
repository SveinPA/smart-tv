package edu.ntnu.sveiap.idata2304.smarttv.server.app;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;

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

    /**
     * Initializes logging configuration.
     */
    private static void initLogging() {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tT] %4$s %3$s - %5$s%6$s%n");

        Logger root = Logger.getLogger("");
        root.setLevel(Level.INFO);

        for (Handler h : root.getHandlers()) {
          h.setLevel(Level.INFO);
          if (h instanceof ConsoleHandler ch) {
            ch.setFormatter(new SimpleFormatter());
          }   
        }
    }

    

    public static void main(String[] args) throws Exception {
        initLogging();
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
