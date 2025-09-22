package edu.ntnu.sveiap.idata2304.smarttv.common.protocol;

/**
 * Enum representing the various commands supported by the Smart TV protocol.
 */
public enum Command {
  ON,
  OFF,
  STATUS,
  CHANNELS,
  GET,
  SET,
  UP,
  DOWN,
  SUB,
  UNSUB,
  PING;

  /**
   * Converts a string token to its corresponding Command enum value.
   * The comparison is case-insensitive and ignores leading/trailing whitespace.
   * 
   * @param token The string representation of the command.
   * @return The corresponding Command enum value.
   */
  public static Command fromToken(String token) {
    return Command.valueOf(token.trim().toUpperCase());
  }
  
}
