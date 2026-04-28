package org.strongcat.constant;

import java.util.Random;

public enum LogLevel {
  INFO,
  DEBUG,
  WARN,
  ERROR,
  TRACE;

  private static final Random RANDOM = new Random();

  public static LogLevel getRandom() {
    return values()[RANDOM.nextInt(values().length)];
  }
}
