package edu.ntnu.sveiap.idata2304.smarttv.common.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for SmartTv.
 */
class SmartTvTest {

  /**
   * Test that the TV starts off.
   */
  @Test
  void startsOff() {
    SmartTv smartTv = new SmartTv(10);
    assertFalse(smartTv.isOn());
  }

  /**
   * Test that using channels while the TV is off is illegal.
   */
  @Test
  void usingChannelWhileOffIsIllegal() {
    SmartTv smartTv = new SmartTv(10);
    assertThrows(IllegalStateException.class, () -> smartTv.getChannel());
    assertThrows(IllegalStateException.class, () -> smartTv.setChannel(1));
  }

  /**
   * Tests that the TV remembers the last channel and enforces bounds.
   */
  @Test
  void channelBoundsAndMemory() {
    SmartTv smartTv = new SmartTv(5);
    smartTv.turnOn();
    assertEquals(5, smartTv.getNumberOfChannels());
    smartTv.setChannel(3);
    assertEquals(3, smartTv.getChannel());
    smartTv.turnOff();
    smartTv.turnOn();
    assertEquals(3, smartTv.getChannel()); // Should remember the last channel
    assertThrows(IllegalArgumentException.class, () -> smartTv.setChannel(0));
    assertThrows(IllegalArgumentException.class, () -> smartTv.setChannel(6));
  }

  /**
   * Tests channel up/down functionality without wrapping.
   */
  @Test
  void upDownNoWrap() {
    SmartTv tv = new SmartTv(3);
    assertThrows(IllegalStateException.class, tv::channelUp); // TV is off
    assertThrows(IllegalStateException.class, tv::channelDown); // TV is off

    tv.turnOn();
    // Initial channel is 1
    assertThrows(IllegalStateException.class, tv::channelDown); // Already at min

    tv.channelUp(); // 2
    assertEquals(2, tv.getChannel());
    tv.channelUp(); // 3
    assertEquals(3, tv.getChannel());
    assertThrows(IllegalStateException.class, tv::channelUp); // Already at max

    tv.channelDown(); // 2
    assertEquals(2, tv.getChannel());
  }
}
