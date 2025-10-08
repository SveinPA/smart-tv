package edu.ntnu.sveiap.idata2304.smarttv.server.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Codec;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Limits;
import edu.ntnu.sveiap.idata2304.smarttv.server.adapter.ProtocolHandler;

/**
 * A simple TCP server that listens for incoming connections on a specified port.
 * For each connected client, it reads lines of text, processes them using a ProtocolHandler,
 * and sends back the response. Each line is terminated with CRLF (\r\n).
 * The server uses UTF-8 encoding for sending and receiving text.
 * Currently handles one client at a time.
 */
public class TcpServer {
  private static final Logger LOG = Logger.getLogger(TcpServer.class.getName());

  private final int port;
  private final ProtocolHandler handler;

  /**
   * Creates a TCP server that listens on the specified port and uses the given ProtocolHandler
   * to process incoming lines.
   */
  public TcpServer(int port, ProtocolHandler handler) {
    this.port = port;
    this.handler = handler;
  }

  /**
   * Starts the TCP server. This method blocks and runs indefinitely,
   * accepting and handling one client at a time.
   *
   * @throws IOException if an I/O error occurs when opening the socket or during communication.
   */
  public void start() throws IOException {
    try (ServerSocket server = new ServerSocket(port)) {
      LOG.log(Level.INFO, "Listening on port " + port + "...");
    
      // TODO: refactor to handle more than one client
      while (true) {
        try (Socket socket = server.accept()) {
          LOG.log(Level.INFO, "Client connected: {0}", socket.getRemoteSocketAddress());
          serve(socket);
          LOG.log(Level.INFO, "Client disconnected.");
        } catch (IOException e) {
          LOG.log(Level.WARNING, "Client I/O error: {0}" + e.getMessage(), e);
        }
      }
    }
  }

  /**
   * Handles communication with a connected client.
   * Reads lines from the client, processes them using the ProtocolHandler,
   * and sends back the response.
   * Exits when the client disconnects or an I/O error occurs.
   *
   * @param socket The connected client socket.
   * @throws IOException if an I/O error occurs during communication.
   */
  private void serve(Socket socket) throws IOException {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = socket.getOutputStream()) {

          String raw;
          while ((raw = in.readLine()) != null) {

            String line = raw.trim();
            if (line.isEmpty()) {
              continue;
            }

            if (line.length() > Limits.MAX_LINE_LENGTH) {
              out.write(Codec.errLineTooLong().getBytes(StandardCharsets.UTF_8));
              out.flush();
              continue;
            }

            final String reply;
            try {
              reply = handler.handleLine(line);
            } catch (Exception e) {
              LOG.log(Level.SEVERE, "Unexpected handler error: " + e.getMessage(), e);

              // In case of unexpected error, send a generic server error response.
              byte[] b = Codec.errServerError().getBytes(StandardCharsets.UTF_8);
              out.write(b);
              out.flush();
              continue;
            }

            out.write(reply.getBytes(StandardCharsets.UTF_8));
            out.flush();
          }
        }
  }
}
