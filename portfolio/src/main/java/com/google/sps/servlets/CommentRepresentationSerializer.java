package com.google.sps.servlets;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CommentRepresentationSerializer {
  public String username;
  public String text;
  public String key;
  public String timestamp;
  // the idea here is to have a list of ids of comments
  // that are direct replies to this comment
  public ArrayList<String> replies;

  public CommentRepresentationSerializer(Comment comment) {
    this.replies = new ArrayList<>();
    for (Field field: comment.getClass().getDeclaredFields()) {
      // here I just copy each property of comment to the serializer
      // it's okay that some fields may not be initialized -
      // serializer makes its best to initialize itself
      try {
        String fieldName = field.getName();
        String fieldValue = (String)field.get(comment);
        if (fieldValue != null) {
          CommentRepresentationSerializer.class.getField(fieldName).set(this, fieldValue);
        }
      } catch (IllegalAccessException | NoSuchFieldException ex) {
        System.out.println(ex.getMessage());
      }
    }
  }
}
