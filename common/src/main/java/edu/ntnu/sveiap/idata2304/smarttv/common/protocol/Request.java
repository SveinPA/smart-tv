package edu.ntnu.sveiap.idata2304.smarttv.common.protocol;

/**
 * Represents a request made to the Smart TV.
 * Contains a command and an optional argument.
 * 
 * @param command The command to be executed.
 * @param arg An optional argument for the command, can be null.
 */
public record Request(Command command, Integer arg) {
  
}
