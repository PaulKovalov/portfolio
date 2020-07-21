// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/comments")
public class CommentsServlet extends HttpServlet {
  private final int BAD_REQUEST = 400;
  /**
   * Returns the list of all comments
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    CommentsHandler commentsHandler = new CommentsHandler();
    // retrieve comments from Datastore in JSON format
    String comments = commentsHandler.getComments();
    response.setContentType("application/json");
    // use the response's writer to return comments list
    response.getWriter().println(comments);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // get URL parameters from the request
    String username = request.getParameter("username");
    String text = request.getParameter("text");
    String replyTo = request.getParameter("replyTo"); // this one is optional
    Comment comment = new Comment(username, text);
    if (replyTo != null) {
      comment.replyTo = replyTo;
    }
    CommentsHandler commentsHandler = new CommentsHandler();
    try {
      // when comment is saved, it is returned as a JSON string 
      String serializedComment = commentsHandler.saveComment(comment);
      // use the response's writer to return the comment
      response.getWriter().println(serializedComment);
    } catch (BadRequestException ex) {
      response.sendError(400, ex.getMessage());
    }
  }
}
