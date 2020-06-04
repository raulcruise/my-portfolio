package com.google.sps.data;

public final class Comment {
  
  private final String text;
  private final long timestamp;

  public Comment(String text, long timestamp) {
      this.text = text;
      this.timestamp = timestamp;
  }
}
