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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    Query query = new Query("Comment");
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
    // order map keys
    ArrayList<CommentRepresentationSerializer> commentsList = new ArrayList<>(commentsMap.values());
    commentsList.sort((c1, c2) -> c2.timestamp.compareTo(c1.timestamp));
    return gson.toJson(commentsList);
  }

  // deletes the comment with the given key
  // deletes all child comments
  public void deleteComment(String key) throws RequestForbiddenException, EntityNotFoundException {
    // check that user is authenticated and they are an owner of the email
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      String commentAuthorUsername = (String)(datastore.get(KeyFactory.stringToKey(key)).getProperty("username"));
      if (commentAuthorUsername != null && commentAuthorUsername.equals(userService.getCurrentUser().getEmail())) {
        // Technically if there are replies to this comment I can delete the whole comments thread,
        // I don't know what is better - to delete them all or to change parent comment to dummy one
        // with "deleted" text Here I am implementing the first one
        Map<Key, Entity> commentsMap = new HashMap<>();
        Query query = new Query("Comment");
        PreparedQuery results = datastore.prepare(query);
        for (Entity entity : results.asIterable()) {
          commentsMap.put(entity.getKey(), entity);
        }
        deleteThread(KeyFactory.stringToKey(key), commentsMap);
      } else {
        throw new RequestForbiddenException("You are not owner of the comment");
      }
    } else {
      throw new RequestForbiddenException("Not authenticated");
    }
  }

  // deletes current comment and recursively calls itself to delete all
  // child comments
  private void deleteThread(Key current, Map<Key, Entity> commentsMap) {
    datastore.delete(current);
    // this can be done faster, presumably in O(thread length) if I store reversed reply keys in the
    // datastore, then I will only need to run the DFS over the thread. I'm doing this slow version
    // which runs in O(N* thread length) because it is faster to implement, better approach needs
    // way more time
    for (Entry<Key, Entity> entry : commentsMap.entrySet()) {
      Object replyToValue = entry.getValue().getProperty("replyTo");
      if (replyToValue != null && replyToValue.equals(KeyFactory.keyToString(current))) {
        // this comment is a child of current, call delete for it
        deleteThread(entry.getKey(), commentsMap);
      }
    }
  }
}
