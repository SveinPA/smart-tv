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
  
}
