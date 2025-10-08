package edu.ntnu.sveiap.idata2304.smarttv.server.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Limits;
import edu.ntnu.sveiap.idata2304.smarttv.server.adapter.ProtocolHandler;

class TcpServerTest {

  /**
   * Pick an available ephemeral TCP port.
   */
  private static int pickFreePort() throws IOException {
    try (ServerSocket s = new ServerSocket(0)) {
      return s.getLocalPort();
    }
  }

  /**
   * Start the server in a background deamon thread.
   */
  private static Thread startServer(int port) {
    SmartTv tv = new SmartTv(10);
    ProtocolHandler handler = new ProtocolHandler(tv);
    TcpServer server = new TcpServer(port, handler);

    Thread t = new Thread(() -> {
      try {
        server.start();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }, "TcpServerTest-ServerThread");
    t.setDaemon(true);
    t.start();
    return t;
  }

  /**
   * Connect with small retry window so the test doesn't flake on slow CI.
   */
  private static Socket connectWithRetry(int port) throws Exception {
    long deadline = System.currentTimeMillis() + 3_000;
    Exception last = null;
    while (System.currentTimeMillis() < deadline) {
      try {
        Socket s = new Socket("127.0.0.1", port);
        s.setSoTimeout(2_000);
        return s;
      } catch (Exception e) {
        last = e;
        Thread.sleep(50);
      }
    }
    throw last != null ? last : new IOException("connect retry timed out");
  }

  /**
   * Connect to server, send STATUS, expect OK OFF.
   */
  @Test
  void blankLineIsIgnored_thenStatusIsOkOff() throws Exception {
    int port = pickFreePort();
    startServer(port);

    try (Socket s = connectWithRetry(port);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
        
        // Send a blank line first, should be ignored
        out.write("       \r\n"); out.flush();

        // Now send STATUS, should get OK OFF
        out.write("STATUS\r\n"); out.flush();

        // Reads up to CRLF, CR trimmed by readLine()
        String reply = in.readLine();
        assertEquals("OK OFF", reply);
    }
  }

  @Test
  void tooLongLineReturns400_LineTooLong() throws Exception {
    int port = pickFreePort();
    startServer(port);

    try (Socket s = connectWithRetry(port);
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
        
        // Build a line longer than allowed max (without CRLF)
        String tooLong = "X".repeat(Limits.MAX_LINE_LENGTH + 1);
        out.write(tooLong + "\r\n"); out.flush();

        String reply = in.readLine();
        assertEquals("ERR 400 LINE_TOO_LONG", reply);

        // Connection should still be open, send a valid command and get a reply
        out.write("STATUS\r\n"); out.flush();
        String ok = in.readLine();
        assertEquals("OK OFF", ok);
    }
  }
  
}
