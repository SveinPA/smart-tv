package edu.ntnu.sveiap.idata2304.smarttv.common.protocol;

/**
 * A class containing various limits used in the protocol.
 */
public final class Limits {

  // Prevent instantiation
  private Limits() {}

  // Maximum length of a line in the protocol (without CRLF)
  public static final int MAX_LINE_LENGTH = 256;

  // Minimum channel number
  public static final int MIN_CHANNEL = 1;
  
}
