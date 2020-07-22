package com.google.sps.servlets;

// use it as a dataclass
public class Comment {
  public String username;
  public String text;
  public String replyTo;
  public String key;
  public String timestamp;

  public Comment(String username, String text) {
    this.username = username;
    this.text = text;
  }

  // Sometimes I create comments without parameters, so I need this constructor as well
  public Comment() {}

  public Comment(Comment other) {
    this.username = other.username;
    this.text = other.text;
    this.replyTo = other.replyTo;
    this.key = other.key;
  }
}
