package edu.ntnu.sveiap.idata2304.smarttv.common.logic;

import edu.ntnu.sveiap.idata2304.smarttv.common.entity.TvState;

/**
 * Logic for Smart TV operations.
 * This class will handle the main functionalities of the Smart TV,
 * such as turning on/off, changing channels, etc.
 */
public class SmartTv {
  private final TvState tvState;

  /**
   * Constructor for SmartTv.
   * Initializes the TV state with a given number of channels.
   * 
   * @param channels The maximum number of channels available on the TV.
   */
  public SmartTv(int channels) {
    this.tvState = new TvState(channels);
  }

  /**
   * Turns the TV on.
   */
  public synchronized void turnOn() {
    tvState.setOn(true);
  }

  /**
   * Turns the TV off.
   */
  public synchronized void turnOff() {
    tvState.setOn(false);
  }

  /**
   * Checks if the TV is on and returns the status.
   * 
   * @return true if the TV is on, false otherwise.
   */
  public synchronized boolean isOn() {
    return tvState.isOn();
  }

  /**
   * Gets the current channel range of the tv.
   * 
   * returns the number of channels.
   */
  public synchronized int getNumberOfChannels() {
    ensureOn();
    return tvState.getChannelRange();
  }

  /**
   * Gets the current channel of the tv.
   * 
   * @return the current channel.
   */
  public synchronized int getChannel() {
    ensureOn();
    return tvState.getCurrentChannel();
  }

  /**
   * Sets the current channel of the tv.
   * 
   * @param n The channel number to set.
   */
  public synchronized void setChannel(int n) {
    ensureOn();
    tvState.setCurrentChannel(n);
  }

  /**
   * Helper method to ensure the TV is on before performing operations.
   */
  private void ensureOn() {
    if (!tvState.isOn()) {
      throw new IllegalStateException("TV_OFF");
    }
  }


}
