package edu.ntnu.sveiap.idata2304.smarttv.common.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CodecTest {

  /**
   * Test parsing a simple STATUS command in various cases.
   */
  @Test
  void parsesStatusCaseInsensitive() {
    Request r1 = Codec.parseRequest("STATUS"); // Case-sensitive
    Request r2 = Codec.parseRequest("status"); // Lowercase
    Request r3 = Codec.parseRequest("StAtUs"); // Mixed case

    // All should parse to the same command
    assertEquals(Command.STATUS, r1.command());
    assertEquals(Command.STATUS, r2.command()); 
    assertEquals(Command.STATUS, r3.command());
    // No arguments for STATUS command
    assertNull(r1.arg());
  }

  /**
   * Test parsing a SET command with an integer argument.
   */
  @Test
  void parsesSetWithIntArg() {
    Request r = Codec.parseRequest("SET 5");

    // Should parse to SET command with argument 5
    assertEquals(Command.SET, r.command());

    // Argument should be 5
    assertEquals(5, r.arg());
  }

  // Test if SET command wihout argument gets rejected
  @Test
  void rejectsSetWithoutArg() {
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("SET"));
  }

  // Tests that bad arguments get rejected
  @Test
  void rejectsSetWithBadArg() {
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("SET invalid"));
  }

  // Tests for extra arguments where they are forbidden
  @Test
  void rejectsExtraArgsWhereForbidden() {
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("STATUS now"));
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("GET 1 2"));
  }

  // test if Codec formats OK responses correctly
  @Test
  void formatsOkStatus() {
    assertEquals("OK ON\r\n", Codec.okStatus(true));
    assertEquals("OK OFF\r\n", Codec.okStatus(false));
  }
  
}
