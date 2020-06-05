package com.google.sps.data;

public final class Comment {
  
  public static final String ENTITY_NAME_PARAM = "comment";
  public static final String TEXT_PARAM = "text";
  public static final String TIME_PARAM = "timestamp";

  private final String text;
  private final long timestamp;

  public Comment(String text, long timestamp) {
    this.text = text;
    this.timestamp = timestamp;
  } 
}
