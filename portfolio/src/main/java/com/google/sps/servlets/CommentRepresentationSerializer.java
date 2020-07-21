package com.google.sps.servlets;

import java.util.ArrayList;

public class CommentRepresentationSerializer {
    public String username;
    public String text;
    public String key;
    // the idea here is to have a list of ids of comments
    // that are direct replies to this comment
    public ArrayList<String> replies;
    
    public CommentRepresentationSerializer(Comment comment) {
        this.username = comment.username;
        this.text = comment.text;
        this.replies = new ArrayList<>();
        this.key = comment.key;
    }
}
