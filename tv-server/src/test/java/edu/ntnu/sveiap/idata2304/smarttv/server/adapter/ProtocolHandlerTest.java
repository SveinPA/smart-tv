package edu.ntnu.sveiap.idata2304.smarttv.server.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.ntnu.sveiap.idata2304.smarttv.common.logic.SmartTv;

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
    ProtocolHandler handler = new ProtocolHandler(tv);
    String reply = handler.handleLine("STATUS");
    assertEquals("OK OFF\r\n", reply);
  }

  /**
   * Test STATUS command when TV is on.
   */
  @Test
  void statusWhenOn() {
    SmartTv tv = new SmartTv(10);
    tv.turnOn();
    ProtocolHandler handler = new ProtocolHandler(tv);
    String reply = handler.handleLine("STATUS");
    assertEquals("OK ON\r\n", reply);
  }

  /**
   * Test that unknown commands return a 400 BAD_COMMAND error.
   */
  @Test
  void unknownCommandGives400() {
    SmartTv tv = new SmartTv(10);
    ProtocolHandler handler = new ProtocolHandler(tv);
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("HELLO"));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine(""));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine(null));
  }

  /**
   * Test a sequence of ON, STATUS, OFF, STATUS commands.
   */
  @Test
  void onOffFlow() {
    SmartTv tv = new SmartTv(10);
    ProtocolHandler handler = new ProtocolHandler(tv);

    assertEquals("OK OFF\r\n", handler.handleLine("STATUS"));

    assertEquals("OK\r\n", handler.handleLine("ON"));
    assertEquals("OK ON\r\n", handler.handleLine("STATUS"));

    assertEquals("OK\r\n", handler.handleLine("OFF"));
    assertEquals("OK OFF\r\n", handler.handleLine("STATUS"));
  }

  /**
   * Test that turning the TV on when it's already on, or off when it's already off, is handled gracefully.
   */
  @Test
  void idempotentOnOff() {
    SmartTv tv = new SmartTv(10);
    ProtocolHandler handler = new ProtocolHandler(tv);

    assertEquals("OK\r\n", handler.handleLine("OFF")); // Already off
    assertEquals("OK\r\n", handler.handleLine("OFF")); // Still off

    assertEquals("OK\r\n", handler.handleLine("ON")); // Turn on
    assertEquals("OK\r\n", handler.handleLine("ON")); // Still on
  }

  /**
   * Test CHANNELS and GET commands when TV is off and on, and setting channels within range.
   */
  @Test
  void channelsGetSetHappyPath() {
    SmartTv tv = new SmartTv(7);
    ProtocolHandler handler = new ProtocolHandler(tv);

    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("GET"));
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("CHANNELS"));

    handler.handleLine("ON");
    assertEquals("OK C=7\r\n", handler.handleLine("CHANNELS"));
    assertEquals("OK CH=1\r\n", handler.handleLine("GET")); // default channel on first ON

    assertEquals("OK CH=5\r\n", handler.handleLine("SET 5"));
    assertEquals("OK CH=5\r\n", handler.handleLine("GET"));
  }

  /**
   * Test setting channels out of range and with bad syntax.
   */
  @Test void setOutOfRangeAndBadSyntax() {
    SmartTv tv = new SmartTv(3);
    ProtocolHandler handler = new ProtocolHandler(tv);
    handler.handleLine("ON");

    assertEquals("ERR 404 OUT_OF_RANGE\r\n", handler.handleLine("SET 0"));
    assertEquals("ERR 404 OUT_OF_RANGE\r\n", handler.handleLine("SET 4"));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("SET abc"));
    assertEquals("ERR 400 BAD_COMMAND\r\n", handler.handleLine("SET"));
  }

  /**
   * Test UP and DOWN commands with proper error mapping.
   */
  @Test
  void upDownMapping() {
    SmartTv tv = new SmartTv(2);
    ProtocolHandler handler = new ProtocolHandler(tv);

    // OFF -> 401
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("UP"));
    assertEquals("ERR 401 TV_OFF\r\n", handler.handleLine("DOWN"));

    handler.handleLine("ON"); // Tv is now ON, channel 1
    assertEquals("ERR 409 INVALID_STATE\r\n", handler.handleLine("DOWN")); // Already at min
    assertEquals("OK CH=2\r\n", handler.handleLine("UP")); // 1 -> 2
    assertEquals("ERR 409 INVALID_STATE\r\n", handler.handleLine("UP")); // Already at max
    assertEquals("OK CH=1\r\n", handler.handleLine("DOWN")); // 2 -> 1
  }
  
}
