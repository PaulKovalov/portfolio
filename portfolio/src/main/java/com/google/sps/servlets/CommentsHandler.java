package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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
        for (Field field: Comment.class.getDeclaredFields()) {
            try {
                if (field.get(comment) != null) {
                    commentEntity.setProperty(field.getName(), field.get(comment));
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex.getMessage());
            }
        }
        datastore.put(commentEntity);
        return commentEntity.getKey().toString();
    }

    public String getComments() {
        Gson gson = new Gson();
        Query query = new Query("Comment");
        PreparedQuery results = datastore.prepare(query);
        ArrayList<Comment> comments = new ArrayList<>();
        for (Entity entity: results.asIterable()) {
            Map<String, Object> properties = entity.getProperties();
            Comment comment = new Comment();
            for (Entry<String, Object> entry: properties.entrySet()) {
                try {
                    Comment.class.getField(entry.getKey()).set(comment, entry.getValue().toString());
                } catch (IllegalAccessException ex) {
                    System.out.println(ex.getMessage());
                } catch (NoSuchFieldException ex) {
                    System.out.println(ex.getMessage());
                }
            }
            comment.key = entity.getKey().toString();
            comments.add(comment);
        }
        // now when comments retrieved I can build response data
        // use map to build a list of replies for each comment
        Map<String, CommentRepresentationSerializer> commentsMap = new HashMap<>();
        // save each comment to the map
        comments.forEach(comment -> commentsMap.put(comment.key, new CommentRepresentationSerializer(comment)));
        // update each comment's parent in case there is one
        for (Comment comment: comments) {
            if (comment.replyTo != null) {
                commentsMap.get(comment.replyTo).replies.add(comment.key);
            }
        }
        return gson.toJson(commentsMap.values());
    }
}
