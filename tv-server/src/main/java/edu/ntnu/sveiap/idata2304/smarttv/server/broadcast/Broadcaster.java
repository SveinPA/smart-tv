package edu.ntnu.sveiap.idata2304.smarttv.server.broadcast;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages broadcasting of events to all connected clients.
 * Thread-safe for concurrent access from multiple client threads.
 */
public class Broadcaster {
  private static final Logger LOG = Logger.getLogger(Broadcaster.class.getName());
  
  private final Set<OutputStream> subscribers = ConcurrentHashMap.newKeySet();
  
  /**
   * Registers a client's output stream to receive broadcast events.
   * 
   * @param out The client's output stream.
   */
  public void subscribe(OutputStream out) {
    subscribers.add(out);
    LOG.log(Level.INFO, "Client subscribed. Total subscribers: {0}", subscribers.size());
  }


  /**
   * Unregisters a client's output stream from receiving broadcast events.
   * 
   * @param out The client's output stream.
   */
  public void unsubscribe(OutputStream out) {
    subscribers.remove(out);
    LOG.log(Level.INFO, "Client unsubscribed. Total subscribers: {0}", subscribers.size());
  }

  /**
   * Broadcasts a message to all subscribed clients.
   * If sending to a client fails, that client is automatically unsubscribed.
   * 
   * @param message The message to broadcast (should already include CRLF).
   */
  public void broadcast(String message) {
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    
    // Iterate  over a copy to avoid concurrent modification
    for (OutputStream out : Set.copyOf(subscribers)) {
      try {
        out.write(bytes);
        out.flush();
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Failed to send to client, unsubscribing: {0}", e.getMessage());
        subscribers.remove(out);
      }
    }
  }

  /**
   * Broadcast a message to all subscribed client EXCEPT the specific one.
   * If sending to a client fails, that client is automatically unsubscribed.
   * 
   * @param message The message to broadcast (should already include CRLF)
   * @param exclude The OutputStream to exclude from broadcast (typically the sender).
   */
  public void broadcastExcept(String message, OutputStream exclude) {
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);

    for (OutputStream out : Set.copyOf(subscribers)) {
      if (out == exclude) {
        continue; // skips the sender
      }

      try {
        out.write(bytes);
        out.flush();
      } catch (Exception e) {
        LOG.log(Level.WARNING, "Failed to send to client, unsubscribing: {0}", e.getMessage());
        subscribers.remove(out); // remove unresponsive client
      }
    }
  }

  /**
   * Returns the current number of subscribed clients.
   * 
   * @return The subscriber count
   */
  public int getSubscriberCount() {
    return subscribers.size();
  }
}
