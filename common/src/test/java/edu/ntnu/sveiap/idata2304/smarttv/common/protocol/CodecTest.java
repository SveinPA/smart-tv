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
  
  // Additional coverage: other OK helpers
  @Test
  void formatsOtherOkResponses() {
    assertEquals("OK\r\n", Codec.ok());
    assertEquals("OK C=8\r\n", Codec.okChannels(8));
    assertEquals("OK CH=3\r\n", Codec.okChannel(3));
    assertEquals("OK PONG\r\n", Codec.okPong());
  }

  // Additional coverage: error helpers
  @Test
  void formatsErrorResponses() {
    assertEquals("ERR 400 BAD_COMMAND\r\n", Codec.errBadCommand());
    assertEquals("ERR 401 TV_OFF\r\n", Codec.errTvOff());
    assertEquals("ERR 404 OUT_OF_RANGE\r\n", Codec.errOutOfRange());
    assertEquals("ERR 409 INVALID_STATE\r\n", Codec.errInvalidState());
    assertEquals("ERR 500 SERVER_ERROR\r\n", Codec.errServerError());
  }

  // Parsing of other no-arg commands
  @Test
  void parsesOtherNoArgCommands() {
    assertEquals(Command.GET, Codec.parseRequest("GET").command());
    assertEquals(Command.CHANNELS, Codec.parseRequest("CHANNELS").command());
    assertEquals(Command.UP, Codec.parseRequest("UP").command());
    assertEquals(Command.DOWN, Codec.parseRequest("DOWN").command());
    assertEquals(Command.PING, Codec.parseRequest("PING").command());
    assertEquals(Command.SUB, Codec.parseRequest("SUB").command());
    assertEquals(Command.UNSUB, Codec.parseRequest("UNSUB").command());
  }

  // Trimming and multiple spaces are normalized
  @Test
  void trimsAndCollapsesSpaces() {
    Request r = Codec.parseRequest("   SET    9   ");
    assertEquals(Command.SET, r.command());
    assertEquals(9, r.arg());
  }

  // Reject line over max length (256)
  @Test
  void rejectsTooLongLine() {
    String longLine = "SET " + "9".repeat(Limits.MAX_LINE_LENGTH + 1); // definitely > 256 chars total
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest(longLine));
    assertEquals("LINE_TOO_LONG", ex.getMessage());
  }

  // Test that parsing is case-insensitive and trims input
  @Test
  void trimsAndIsCaseInsensitive() {
    Request r1 = Codec.parseRequest("    status    ");
    assertEquals(Command.STATUS, r1.command());

    Request r2 = Codec.parseRequest("SeT   5");
    assertEquals(Command.SET, r2.command());
    assertEquals(5, r2.arg());
  }

  // Test that various error conditions produce clear exceptions
  @Test
  void errorsHaveClearReasons() {
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest(null)); // NULL_LINE
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("")); // EMPTY_LINE
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("Test")); // UNKNOWN_CMD
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("SET")); // ARG_COUNT
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("SET test")); // ARG_NOT_INT
    assertThrows(IllegalArgumentException.class, () -> Codec.parseRequest("STATUS test")); // EXTRA_ARGS
  }
  
}
