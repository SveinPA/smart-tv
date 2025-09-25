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
public class ProtocolHandler {
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

  public String handleLine(String line) {
    try {

        Request req = Codec.parseRequest(line);
        Command cmd = req.command();

        return switch (cmd) {
          case STATUS -> Codec.okStatus(tv.isOn());
          case ON -> {
            tv.turnOn();
            yield Codec.ok();
          }

          case OFF -> {
            tv.turnOff();
            yield Codec.ok();
          }

          case CHANNELS -> {
            try {
              int c = tv.getNumberOfChannels();
              yield Codec.okChannels(c);
            } catch (IllegalStateException tvOff) {
              yield Codec.errTvOff();
            }
          }

          case GET -> {
            try {
              int ch = tv.getChannel();
              yield Codec.okChannel(ch);
            } catch (IllegalStateException tvOff) {
              yield Codec.errTvOff();
            }
          }

          case SET -> {
            Integer n = req.arg();
            try {
              tv.setChannel(n);
              yield Codec.okChannel(tv.getChannel());
            } catch (IllegalStateException tvOff) {
              yield Codec.errTvOff();
            } catch (IllegalArgumentException outOfRange) {
              yield Codec.errOutOfRange();
            }
          }

          default -> Codec.errBadCommand();
        };
        
      } catch (IllegalArgumentException badSyntax) {

      // Examples: Uknown command, wrong number of args, bad arg format
      return Codec.errBadCommand();

      } catch (Exception unexpected) {

      // Catch-all for unexpected errors
      return Codec.errServerError();

    }
  }
}
