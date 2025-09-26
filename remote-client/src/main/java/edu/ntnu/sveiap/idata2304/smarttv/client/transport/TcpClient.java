package edu.ntnu.sveiap.idata2304.smarttv.client.transport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * A simple TCP client that connects to a server, sends and receives lines of text.
 * Each line is terminated with CRLF (\r\n).
 * The client uses UTF-8 encoding for sending and receiving text.
 */
public final class TcpClient implements Closeable {

  private final Socket socket;
  private final BufferedReader in;
  private final BufferedWriter out;

  public TcpClient(String host, int port) throws IOException {
    this.socket = new Socket(host, port);

    this.socket.setTcpNoDelay(true);

    this.in = new BufferedReader(
              new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    this.out = new BufferedWriter(
               new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    
  }

  /**
   * Sends a line to the server, terminated with CRLF.
   *
   * @param line The line to send. If null, an empty line is sent.
   */
  public void send(String line) throws IOException {
    if (line == null) line = "";
    out.write(line);
    out.write("\r\n");
    out.flush();
  }

  /**
   * Receives a line from the server, terminated with CRLF.
   *
   * @return The line received, without the CRLF. If the connection is closed,
   */
  public String receiveLine() throws IOException {
    return in.readLine();
  }

  /**
   * Sends a line to the server and waits for a response.
   * @param line The line to send. If null, an empty line is sent.
   * @return The line received from the server, without the CRLF.
   */
  public String sendAndRecevie(String line) throws IOException {
    send(line);
    return receiveLine();
  }

  /**
   * Closes the connection to the server.
   * @throws IOException If an I/O error occurs when closing the connection.
   * All resources are closed, even if an exception occurs.
   * If an exception occurs, it is thrown after all resources are closed.
   */
  @Override
  public void close() throws IOException {
    try {
      in.close();
    } finally {
      try {
        out.close();
      } finally {
        socket.close();
      }
    }
  }
}
