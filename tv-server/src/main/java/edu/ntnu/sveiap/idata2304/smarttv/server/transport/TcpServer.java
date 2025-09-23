package edu.ntnu.sveiap.idata2304.smarttv.server.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import edu.ntnu.sveiap.idata2304.smarttv.server.adapter.ProtocolHandler;

public class TcpServer {

  private final int port;
  private final ProtocolHandler handler;

  public TcpServer(int port, ProtocolHandler handler) {
    this.port = port;
    this.handler = handler;
  }

  public void start() throws IOException {
    try (ServerSocket server = new ServerSocket(port)) {
      System.out.println("[TcpServer] Listening on port " + port + "...");
    
      // TODO: refactor to handle more than one client
      while (true) {
        try (Socket socket = server.accept()) {
          System.out.println("[TcpServer] Client connected: " + socket.getRemoteSocketAddress());
          serve(socket);
          System.out.println("[TcpServer] Client disconnected.");
        } catch (IOException e) {
          System.err.println("[TcpServer] I/O error: " + e.getMessage());
        }
      }
    }
  }

  private void serve(Socket socket) throws IOException {

    try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        OutputStream out = socket.getOutputStream()) {

          String line;
          while ((line = in.readLine()) != null) {

            String reply = handler.handleLine(line);

            out.write(reply.getBytes(StandardCharsets.UTF_8));
            out.flush();
          }
        }
  }
}
