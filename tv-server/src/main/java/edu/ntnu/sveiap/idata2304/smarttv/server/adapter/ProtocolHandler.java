package edu.ntnu.sveiap.idata2304.smarttv.server.adapter;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Codec;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Command;
import edu.ntnu.sveiap.idata2304.smarttv.common.protocol.Request;

/**
 * Server-side protocol handler for Smart TV protocol.  
 * Responsibilities:
 * - Parses commands with {@link Codec}
 * - Calls {@link SmartTv} to execute commands
 * - Formats responses with {@link Codec}
 */
public final class ProtocolHandler {
  private final SmartTv tv;
  
  /**
   * Creates a ProtocolHandler with the given SmartTv instance.
   * 
   * @param tv The SmartTv instance to control.
   * @throws IllegalArgumentException if tv is null.
   */
  public ProtocolHandler(SmartTv tv) {
    if (tv == null) throw new IllegalArgumentException("tv cannot be null");
    this.tv = tv;
  }

  /**
   * Handles a single raw input line from a client connection.
   * Parsing + dispatch + error mapping.
   * @param line raw line (may be null)
   * @return protocol response line (always CRLF terminated via Codec)
   */
  public String handleLine(String line) {
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
        case SET -> handleSet(req.arg());
        case UP -> handleUp();
        case DOWN -> handleDown();
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

  private String handleSet(Integer n) {
    try {
      tv.setChannel(n);
      return Codec.okChannel(tv.getChannel());
    } catch (IllegalStateException ex) { // TV off
      return Codec.errTvOff();
    } catch (IllegalArgumentException outOfRange) { // channel bounds
      return Codec.errOutOfRange();
    }
  }

  private String handleUp() {
    try {
      tv.channelUp();
      return Codec.okChannel(tv.getChannel());
    } catch (IllegalStateException ex) {
      return mapIllegalState(ex);
    }
  }

  private String handleDown() {
    try {
      tv.channelDown();
      return Codec.okChannel(tv.getChannel());
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
