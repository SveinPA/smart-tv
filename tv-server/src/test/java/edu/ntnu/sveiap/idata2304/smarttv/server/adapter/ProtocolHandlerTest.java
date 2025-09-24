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
  
}
