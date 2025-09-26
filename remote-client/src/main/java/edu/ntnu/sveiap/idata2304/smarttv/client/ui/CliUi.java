package edu.ntnu.sveiap.idata2304.smarttv.client.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import edu.ntnu.sveiap.idata2304.smarttv.client.transport.TcpClient;

/**
 * A simple command-line interface (CLI) for interacting with the Smart TV server.
 * Connects to the server via TCP, sends user commands, and displays server responses.
 */
public final class CliUi {

  private final String host;
  private final int port;

  public CliUi (String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Runs the CLI UI, connecting to the server and handling user input.
   * Exits on "exit" command or on I/O error.
   */
  public void run() {
    System.out.printf("[Client] Connecting to %s:%d ...%n", host, port);

    try (TcpClient tcp = new TcpClient(host, port);
        BufferedReader console =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
              
        printWelcome();

        while (true) {
          System.out.println("smarttv> ");
          String line = console.readLine();
          if (line == null) break;
          line = line.trim();
          if (line.isEmpty()) continue;

          String lc = line.toLowerCase();
          if (lc.equals("exit") || lc.equals("quit")) break;
          if (lc.equals("help") || lc.equals("?")) {
            printHelp();
            continue;
          }

          try {
            String reply = tcp.sendAndRecevie(line);
            if (reply == null) {
              System.out.println("(connection closed by server)");
              break;
            }
            System.out.println(reply);
          } catch (IOException io) {
            System.out.println("[Client] I/O error: " + io.getMessage());
            break;
          }
        }
    } catch (IOException connectFail) {
      System.out.println("[Client] Could not connect: " + connectFail.getMessage());
    }

    System.out.println("[Client] Exiting ...");
  }

  /**
   * Prints welcome text on startup.
   */
  private static void printWelcome() {
    System.out.println("Type protocol commands (e.g., ON, OFF, STATUS, CHANNELS, GET, SET 5)");
    System.out.println("Local commands: help, exit");
  }

  /**
   * Prints help text for available commands.
   */
  private static void printHelp() {
    System.out.println("""
            Commands (sent to server):
              STATUS                 -> OK ON|OFF
              ON / OFF               -> OK
              CHANNELS               -> OK C=<int>
              GET                    -> OK CH=<int>
              SET <n>                -> OK CH=<n>
              UP / DOWN              -> OK CH=<n> (ERR 409 at edges)
            Local commands:
              help, exit
            """);
  }

}