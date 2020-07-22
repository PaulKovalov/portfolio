package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
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

  public String saveComment(Comment comment) throws BadRequestException {
    Entity commentEntity = new Entity("Comment");
    try {
      comment.timestamp = Long.toString(System.currentTimeMillis());
      // validate replyTo field if there is one
      if (comment.replyTo != null) {
        Key replyToKey = KeyFactory.stringToKey(comment.replyTo);
        try {
          // if comment's replyTo field points to an unexisting comment, throw an exception
          datastore.get(replyToKey);
        } catch (EntityNotFoundException ex) {
          throw new BadRequestException("field replyTo has invalid value");
        }
      }
      // create datastore entity from the Comment object using reflection API
      for (Field field : Comment.class.getDeclaredFields()) {
        Object f = field.get(comment);
        if (f != null) {
          commentEntity.setProperty(field.getName(), f);
        }
      }
      datastore.put(commentEntity);
      comment.key = KeyFactory.keyToString(commentEntity.getKey());
      Gson gson = new Gson();
      // use comment serializer to build a JSON string from
      return gson.toJson(new CommentRepresentationSerializer(comment));
    } catch (IllegalAccessException | IllegalArgumentException ex) {
      throw new BadRequestException("Invalid payload");
    }
  }

  public String getComments() {
    Gson gson = new Gson();
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    ArrayList<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      try {
        // instantiate comment objects based on the datastore entities
        Map<String, Object> properties = entity.getProperties();
        Comment comment = new Comment();
        for (Entry<String, Object> entry : properties.entrySet()) {
          Comment.class.getField(entry.getKey()).set(comment, entry.getValue().toString());
        }
        comment.key = KeyFactory.keyToString(entity.getKey());
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
