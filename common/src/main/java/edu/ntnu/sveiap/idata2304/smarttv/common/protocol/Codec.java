package edu.ntnu.sveiap.idata2304.smarttv.common.protocol;

import java.util.Locale;

/**
 * Codec for encoding and decoding requests and responses in the Smart TV protocol.
 */
public final class Codec {

  // Prevent instantiation
  private Codec() {
  }

  private static final String CRLF = "\r\n";

  /**
   * Parses a line of text into a Request object.
   * The line should contain a command and optionally an argument.
   * 
   * @param line The line of text to parse.
   * @return A Request object representing the parsed command and argument.
   * @throws IllegalArgumentException if the line is invalid or cannot be parsed.
   */
  public static Request parseRequest(String line) {
    if (line == null) throw new IllegalArgumentException("EMPTY_LINE");
    
    String trimmed = line.trim();
    if (trimmed.isEmpty()) throw new IllegalArgumentException("EMPTY_LINE");
    if (trimmed.length() > Limits.MAX_LINE_LENGTH) throw new IllegalArgumentException("LINE_TOO_LONG");

    String[] parts = trimmed.split("\\s+");
    String token = parts[0].toUpperCase(Locale.ROOT);

    Command cmd;
    try {
      cmd = Command.fromToken(token);
    } catch (Exception e) {
      throw new IllegalArgumentException("UNKNOWN_CMD");
    }

    Integer arg = null;

    switch (cmd) {
      case SET -> {
        if (parts.length != 2) throw new IllegalArgumentException("ARG_COUNT");
        try {
          arg = Integer.parseInt(parts[1]);
        } catch (NumberFormatException nfe) {
          throw new IllegalArgumentException("ARG_NOT_INT");
        }
      }
      default -> {
        // All current non-SET commands forbid extra args.
        if (parts.length != 1) throw new IllegalArgumentException("EXTRA_ARGS");
      }
    }

    return new Request(cmd, arg);
  }

  // OK Responses

  /**
   * Encodes a successful response without additional data.
   * 
   * @return The encoded response string.
   */
  public static String ok() {
    return "OK" + CRLF;
  }

  /**
   * Encodes a successful status response.
   * 
   * @param on The current power state of the TV.
   * @return The encoded response string.
   */
  public static String okStatus(boolean on) {
    return "OK " + (on ? "ON" : "OFF") + CRLF;
  }

  /**
   * Encodes a successful channels response.
   * 
   * @param c The number of channels available on the TV.
   * @return The encoded response string.
   */
  public static String okChannels(int c) {
    return "OK C=" + c + CRLF;
  }

  /**
   * Encodes a successful channel response.
   * 
   * @param ch The current channel of the TV.
   * @return The encoded response string.
   */
  public static String okChannel(int ch) {
    return "OK CH=" + ch + CRLF;
  }

  /**
   * Encodes a successful pong response.
   * 
   * @return The encoded response string.
   */
  public static String okPong() {
    return "OK PONG" + CRLF;
  }

  // Error Responses

  /**
   * Encodes a bad command error response.
   * 
   * @return The encoded error response string.
   */
  public static String errBadCommand() {
    return "ERR 400 BAD_COMMAND" + CRLF;
  }

  /**
   * Encodes a line too long error response.
   * 
   * @return The encoded error response string.
   */
  public static String errLineTooLong() {
    return "ERR 400 LINE_TOO_LONG" + CRLF;
  }

  /**
   * Encodes a TV off error response.
   * 
   * @return The encoded error response string.
   */
  public static String errTvOff() {
    return "ERR 401 TV_OFF" + CRLF;
  }

  /**
   * Encodes an out of range error response.
   * 
   * @return The encoded error response string.
   */
  public static String errOutOfRange() {
    return "ERR 404 OUT_OF_RANGE" + CRLF;
  }

  /**
   * Encodes an invalid state error response.
   * 
   * @return The encoded error response string.
   */
  public static String errInvalidState() {
    return "ERR 409 INVALID_STATE" + CRLF;
  }

  /**
   * Encodes a server error response.
   * 
   * @return The encoded error response string.
   */
  public static String errServerError() {
    return "ERR 500 SERVER_ERROR" + CRLF;
  }

  // EVT Responses

  /**
   * Encodes a channel change event response.
   * 
   * @param ch The new channel of the TV.
   * @return The encoded event response string.
   */
  public static String evtChannel(int ch) {
    return "EVT CHANNEL" + ch + CRLF;
  }

  /**
   * Encodes a power on event response.
   * 
   * @return The encoded event response string.
   */
  public static String evtPowerOn() {
    return "EVT POWER ON" + CRLF;
  }

  /**
   * Encodes a power off event response.
   * 
   * @return The encoded event response string.
   */
  public static String evtPowerOff() {
    return "EVT POWER OFF" + CRLF;
  }

}
