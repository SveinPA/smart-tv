package edu.ntnu.sveiap.idata2304.smarttv.server.app;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.server.adapter.ProtocolHandler;
import edu.ntnu.sveiap.idata2304.smarttv.server.transport.TcpServer;

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
