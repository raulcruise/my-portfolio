package com.google.sps.data;

public final class Comment {

  public static final String ENTITY_NAME_PARAM = "comment";
  public static final String TEXT_PARAM = "text";
  public static final String TIME_PARAM = "timestampMillis";

  private final String text;
  private final long timestampMillis;

  public Comment(String text, long timestampMillis) {
    this.text = text;
    this.timestampMillis = timestampMillis;
  }
}
