package com.google.sps.servlets;

import java.util.ArrayList;
/**
 * This class provides a set of Comment's fields that sent to the client
 */
public class CommentRepresentationSerializer extends Comment {
  // the idea here is to have a list of ids of comments
  // that are direct replies to this comment
  public ArrayList<String> replies;

  public CommentRepresentationSerializer(Comment comment) {
    super(comment);
    this.replies = new ArrayList<>();
  }
}
