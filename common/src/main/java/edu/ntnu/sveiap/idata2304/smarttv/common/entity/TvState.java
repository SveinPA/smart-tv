package edu.ntnu.sveiap.idata2304.smarttv.common.entity;

/**
 * Represents the state of the TV.
 * This class can be expanded to include various attributes
 * such as power status, volume level, current channel, etc.
 */
public final class TvState {
  private boolean on = false;
  private final int channels;
  private int currentChannel = 1; // Default to channel 1

  /**
   * Constructor for TvState.
   * Initializes the TV to be off and sets the maximum number of channels.
   * 
   * @param channels The maximum number of channels available on the TV.
   * @throws IllegalArgumentException if channels is less than 1.
   */
  public TvState(int channels) {
    if (channels < 1) throw new IllegalArgumentException("There must be at least one channel.");
    this.channels = channels;
  }

  /**
   * Check if the TV is on or off.
   * 
   * @return true if the TV is on, false otherwise.
   */
  public boolean isOn() {
    return on;
  }

  /**
   * Returns the the number of channels.
   * 
   * @return The number of channels as an int.
   */
  public int getChannelRange() {
    return channels;
  }

  /**
   * Returns the current channel of the TV.
   * 
   * @return The current channel as an int.
   */
  public int getCurrentChannel() {
    return currentChannel;
  }

  /**
   * turns the tv on or off.
   */
  public void setOn(boolean on) {
    this.on = on;
  }

  /**
   * Sets the current channel of the TV.
   * @param n The channel number to set as the current channel.
   * @throws IllegalArgumentException if the channel number is out of range.
   */
  public void setCurrentChannel(int n) {
    if (n < 1 || n > channels) {
      throw new IllegalArgumentException("Channel out of range: " + n);
    }
    this.currentChannel = n;
  }
}
