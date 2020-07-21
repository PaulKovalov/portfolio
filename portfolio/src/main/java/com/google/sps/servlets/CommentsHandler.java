package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommentsHandler {
  private DatastoreService datastore;

  public CommentsHandler() {
    datastore = DatastoreServiceFactory.getDatastoreService();
  };

  public String saveComment(Comment comment) {
    Entity commentEntity = new Entity("Comment");
    try {
      comment.timestamp = Long.toString(System.currentTimeMillis());
      for (Field field : Comment.class.getDeclaredFields()) {
        Object f = field.get(comment);
        if (f != null) {
          commentEntity.setProperty(field.getName(), f);
        }
      }
      datastore.put(commentEntity);
      comment.key = commentEntity.getKey().toString();
      Gson gson = new Gson();
      return gson.toJson(new CommentRepresentationSerializer(comment));
    } catch (IllegalAccessException ex) {
      System.out.println(ex.getMessage());
    }
    return null;
  }

  public String getComments() {
    Gson gson = new Gson();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      try {
        Map<String, Object> properties = entity.getProperties();
        Comment comment = new Comment();
        for (Entry<String, Object> entry : properties.entrySet()) {
          Comment.class.getField(entry.getKey()).set(comment, entry.getValue().toString());
        }
        comment.key = entity.getKey().toString();
        comments.add(comment);
      } catch (IllegalAccessException | NoSuchFieldException ex) {
        System.out.println(ex.getMessage());
      }
    }
    // now when comments retrieved I can build response data
    // use map to build a list of replies for each comment
    Map<String, CommentRepresentationSerializer> commentsMap = new HashMap<>();
    // save each comment to the map
    comments.forEach(
        comment -> commentsMap.put(comment.key, new CommentRepresentationSerializer(comment)));
    // update each comment's parent in case there is one
    for (Comment comment : comments) {
      if (comment.replyTo != null) {
        commentsMap.get(comment.replyTo).replies.add(comment.key);
      }
    }
    return gson.toJson(commentsMap.values());
  }
}
