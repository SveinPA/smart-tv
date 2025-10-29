package edu.ntnu.sveiap.idata2304.smarttv.server.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;
import edu.ntnu.sveiap.idata2304.smarttv.server.broadcast.Broadcaster;

/**
 * Test class for ProtocolHandler.
 */
class ProtocolHandlerTest {

  /**
   * Test STATUS command when TV is off.
   */
  @Test
  void statusWhenOff() {
    SmartTv tv = new SmartTv(10);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();
    String reply = handler.handleLine("STATUS", dummyOut);
    assertEquals("OK OFF\r\n", reply);
  }

  /**
   * Test STATUS command when TV is on.
   */
  @Test
  void statusWhenOn() {
    SmartTv tv = new SmartTv(10);
    Broadcaster broadcaster = new Broadcaster();
    OutputStream dummyOut = new ByteArrayOutputStream();
    tv.turnOn();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    String reply = handler.handleLine("STATUS", dummyOut);
    assertEquals("OK ON\r\n", reply);
  }

  /**
   * Test that unknown commands return a 400 BAD_COMMAND error.
   */
  @Test
  void unknownCommandGives400() {
    SmartTv tv = new SmartTv(10);
    Broadcaster broadcaster = new Broadcaster();
    OutputStream dummyOut = new ByteArrayOutputStream();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("HELLO", dummyOut));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("", dummyOut));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine(null, dummyOut));
  }

  /**
   * Test a sequence of ON, STATUS, OFF, STATUS commands.
   */
  @Test
  void onOffFlow() {
    SmartTv tv = new SmartTv(10);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();

    assertEquals("OK OFF\r\n", handler.handleLine("STATUS", dummyOut));

    assertEquals("OK\r\n", handler.handleLine("ON", dummyOut));
    assertEquals("OK ON\r\n", handler.handleLine("STATUS", dummyOut));

    assertEquals("OK\r\n", handler.handleLine("OFF", dummyOut));
    assertEquals("OK OFF\r\n", handler.handleLine("STATUS", dummyOut));
  }

  /**
   * Test that turning the TV on when it's already on, or off when it's already off, is handled gracefully.
   */
  @Test
  void idempotentOnOff() {
    SmartTv tv = new SmartTv(10);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();

    assertEquals("OK\r\n", handler.handleLine("OFF", dummyOut)); // Already off
    assertEquals("OK\r\n", handler.handleLine("OFF", dummyOut)); // Still off

    assertEquals("OK\r\n", handler.handleLine("ON", dummyOut)); // Turn on
    assertEquals("OK\r\n", handler.handleLine("ON", dummyOut)); // Still on
  }

  /**
   * Test CHANNELS and GET commands when TV is off and on, and setting channels within range.
   */
  @Test
  void channelsGetSetHappyPath() {
    SmartTv tv = new SmartTv(7);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();

    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("GET", dummyOut));
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("CHANNELS", dummyOut));

    handler.handleLine("ON", dummyOut);
    assertEquals("OK C=7\r\n", handler.handleLine("CHANNELS", dummyOut));
    assertEquals("OK CH=1\r\n", handler.handleLine("GET", dummyOut)); // default channel on first ON

    assertEquals("OK CH=5\r\n", handler.handleLine("SET 5", dummyOut));
    assertEquals("OK CH=5\r\n", handler.handleLine("GET", dummyOut));
  }

  /**
   * Test setting channels out of range and with bad syntax.
   */
  @Test void setOutOfRangeAndBadSyntax() {
    SmartTv tv = new SmartTv(3);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();
    handler.handleLine("ON", dummyOut);

    assertEquals("ERR 404 OUT_OF_RANGE\r\n", handler.handleLine("SET 0", dummyOut));
    assertEquals("ERR 404 OUT_OF_RANGE\r\n", handler.handleLine("SET 4", dummyOut));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("SET abc", dummyOut));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("SET", dummyOut));
  }

  /**
   * Test UP and DOWN commands with proper error mapping.
   */
  @Test
  void upDownMapping() {
    SmartTv tv = new SmartTv(2);
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler handler = new ProtocolHandler(tv, broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();

    // OFF -> 401
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("UP", dummyOut));
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("DOWN", dummyOut));

    handler.handleLine("ON", dummyOut); // Tv is now ON, channel 1
    assertEquals("ERR 409 INVALID_STATE\r\n", handler.handleLine("DOWN", dummyOut)); // Already at min
    assertEquals("OK CH=2\r\n", handler.handleLine("UP", dummyOut)); // 1 -> 2
    assertEquals("ERR 409 INVALID_STATE\r\n", handler.handleLine("UP", dummyOut)); // Already at max
    assertEquals("OK CH=1\r\n", handler.handleLine("DOWN", dummyOut)); // 2 -> 1
  }

  /**
   * Test PING command always returns OK regardless of TV state.
   */
  @Test
  void pingAlwaysReturnsOk_noMatterTheState() {
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler h = new ProtocolHandler(new SmartTv(3), broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();
    assertEquals("OK\r\n", h.handleLine("PING", dummyOut)); // off
    h.handleLine("ON", dummyOut);
    assertEquals("OK\r\n", h.handleLine("PING", dummyOut)); // on
  }

  /**
   * Test that PING with an argument returns 400 BAD_COMMAND.
   */
  @Test
  void pingWithArgIsBadCommand() {
    Broadcaster broadcaster = new Broadcaster();
    ProtocolHandler h = new ProtocolHandler(new SmartTv(3), broadcaster);
    OutputStream dummyOut = new ByteArrayOutputStream();
    assertEquals("ERR 400 BAD_COMMAND\r\n", h.handleLine("PING 1", dummyOut));
  }
  
}
