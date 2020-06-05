package com.google.sps.data;

public final class Comment {
  
  public static final String entityNameParam = "comment";
  public static final String textParam = "text";
  public static final String timeParam = "timestamp";

  private final String text;
  private final long timestamp;

  public Comment(String text, long timestamp) {
    this.text = text;
    this.timestamp = timestamp;
  } 
}
