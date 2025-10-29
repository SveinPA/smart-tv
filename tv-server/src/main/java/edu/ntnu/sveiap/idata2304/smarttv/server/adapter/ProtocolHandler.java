package edu.ntnu.sveiap.idata2304.smarttv.server.adapter;

import java.io.OutputStream;
import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Codec;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Command;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Request;
import edu.ntnu.sveiap.idata2304.smarttv.server.broadcast.Broadcaster;

/**
 * Server-side protocol handler for Smart TV protocol.  
 * Responsibilities:
 * - Parses commands with {@link Codec}
 * - Calls {@link SmartTv} to execute commands
 * - Formats responses with {@link Codec}
 */
public final class ProtocolHandler {
  private final SmartTv tv;
  private final Broadcaster broadcaster;
  
  /**
   * Creates a ProtocolHandler with the given SmartTv instance.
   * 
   * @param tv The SmartTv instance to control.
   * @throws IllegalArgumentException if tv is null.
   */
  public ProtocolHandler(SmartTv tv, Broadcaster broadcaster) {
    if (tv == null) throw new IllegalArgumentException("tv cannot be null");
    if (broadcaster == null) throw new IllegalArgumentException("broadcaster cannot be null");
    this.tv = tv;
    this.broadcaster = broadcaster;
  }

  /**
   * Handles a single raw input line from a client connection.
   * Parsing + dispatch + error mapping.
   * @param line raw line (may be null)
   * @return protocol response line (always CRLF terminated via Codec)
   */
  public String handleLine(String line, OutputStream currentClient) {
    final Request req;
    try {
      req = Codec.parseRequest(line);
      } catch (IllegalArgumentException badSyntax) {
      // Unknown command token / wrong arg count / invalid arg / null line
      return Codec.errBadCommand();
      }

      Command cmd = req.command();
      return switch (cmd) {
        case STATUS -> handleStatus();
        case ON -> handleOn();
        case OFF -> handleOff();
        case CHANNELS -> handleChannels();
        case GET -> handleGet();
        case SET -> handleSet(req.arg(), currentClient);
        case UP -> handleUp(currentClient);
        case DOWN -> handleDown(currentClient);
        case PING -> handlePing();
        // SUB/UNSUB/PING not implemented yet at adapter level; treat as BAD_COMMAND until added
        default -> Codec.errBadCommand();
      };
  }

  

  private String handleStatus() {
    return Codec.okStatus(tv.isOn());
  }

  private String handleOn() {
    tv.turnOn();
    return Codec.ok();
  }

  private String handleOff() {
    tv.turnOff();
    return Codec.ok();
  }

  private String handleChannels() {
    try {
      return Codec.okChannels(tv.getNumberOfChannels());
    } catch (IllegalStateException ex) {
      return Codec.errTvOff();
    }
  }

  private String handleGet() {
    try {
      return Codec.okChannel(tv.getChannel());
    } catch (IllegalStateException ex) {
      return Codec.errTvOff();
    }
  }

  private String handleSet(Integer n, OutputStream currentClient) {
    try {
      tv.setChannel(n);
      int newChannel = tv.getChannel();
      broadcaster.broadcastExcept(Codec.evtChannel(newChannel), currentClient);
      return Codec.okChannel(newChannel);
    } catch (IllegalStateException ex) {
      return Codec.errTvOff();
    } catch (IllegalArgumentException outOfRange) {
      return Codec.errOutOfRange();
    }
  }

  private String handleUp(OutputStream currentClient) {
    try {
      tv.channelUp();
      int newChannel = tv.getChannel();
      broadcaster.broadcastExcept(Codec.evtChannel(newChannel), currentClient);
      return Codec.okChannel(newChannel);
    } catch (IllegalStateException ex) {
      return mapIllegalState(ex);
    }
  }

  private String handleDown(OutputStream currentClient) {
    try {
      tv.channelDown();
      int newChannel = tv.getChannel();
      broadcaster.broadcastExcept(Codec.evtChannel(newChannel), currentClient);
      return Codec.okChannel(newChannel);
    } catch (IllegalStateException ex) {
      return mapIllegalState(ex);
    }
  }

  private String handlePing() {
    return Codec.ok();
  }

  /**
   * Maps IllegalStateException messages produced by SmartTv to protocol errors.
   * SmartTv.ensureOn() throws IllegalStateException("TV_OFF"). channelUp/Down throw
   * IllegalStateException("INVALID_STATE") for boundary conditions.
   */
  private String mapIllegalState(IllegalStateException ex) {
    return "TV_OFF".equals(ex.getMessage()) ? Codec.errTvOff() : Codec.errInvalidState();
  }
}
