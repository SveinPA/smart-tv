package edu.ntnu.sveiap.idata2304.smarttv.common.entity;

import edu.ntnu.sveiap.idata2304.smarttv.common.entity.TvState;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for TvState.
 */
class TvStateTest {

  /**
   * Test default values of TvState.
   */
  @Test
  void defaults() {
    TvState tvState = new TvState(10);
    assertFalse(tvState.isOn()); // TV should be off by default
    assertEquals(10, tvState.getChannelRange()); // Number of channels should be as set
    assertEquals(1, tvState.getCurrentChannel()); // Current channel should default to 1
  }

  /**
   * Test guard clauses for setting out-of-range channels.
   */
  @Test
  void rejectOutOfRangeChannels() {
    TvState tvState = new TvState(5);
    // Test setting invalid channels, should throw IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> tvState.setCurrentChannel(0));
    // Test setting invalid channels, should throw IllegalArgumentException
    assertThrows(IllegalArgumentException.class, () -> tvState.setCurrentChannel(6));
  }
}
