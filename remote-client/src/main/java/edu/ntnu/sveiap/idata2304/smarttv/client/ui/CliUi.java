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
        showInitialStatus(tcp);
        

        startMessageListener(tcp);

        // Main loop
        while (true) {
          System.out.print("smarttv> ");
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
            tcp.send(line);
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
   * Starts a background daemon thread that continuously reads messages from the server
   * and prints them to the console. Handles both sunchronous responses (OK/ERR) and
   * asynchronous events (EVT)
   */
  private static void startMessageListener(TcpClient tcp) {
    Thread listener = new Thread(() -> {
      try {
        while (true) {
          String message = tcp.receiveLine();
          if (message == null) {
            System.out.println("\n[Server disconnected]");
            break;
          }
          System.out.println(message);
          System.out.print("smarttv> ");
        }
      } catch (IOException e) {
        System.out.println("\n[Listener error: " + e.getMessage() + "]");
      }
    }, "ServerMessageListener");

    listener.setDaemon(true);
    listener.start();

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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

  /**
   * Queries the TV status on startup and displays current state to the user.  
   * If TV is ON, also fetches and displays the current channel.
   */
  private static void showInitialStatus(TcpClient tcp) {
    try {
      // Send STATUS command to check if TV is on or off
      String statusReply = tcp.sendAndReceive("STATUS");

      if (statusReply == null) {
        System.out.println("[Client] No response from server");
        return;
      }

      System.out.println("Current TV status: " + statusReply);

      // If TV is ON, also get current channel
      if (statusReply.contains("ON")) {
        String channelReply = tcp.sendAndReceive("GET");
        if (channelReply != null) {
          System.out.println("Current channel: " + channelReply);
        }
      }

      System.out.println(); // Blank line to make it easier to read

    } catch (IOException e) {
      System.out.println("[Client] Could not fetch initial status: " + e.getMessage());
    }
  }

}