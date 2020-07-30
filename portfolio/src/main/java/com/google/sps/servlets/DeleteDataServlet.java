/**
 * This file provides a servlet for deleting comments
 */

package com.google.sps.servlets;

import com.google.appengine.api.datastore.EntityNotFoundException;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {
  private final int BAD_REQUEST = 400;
  private final int FORBIDDEN = 403;
  // deletes the comment thread, needs authentication
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    CommentsHandler commentsHandler = new CommentsHandler();
    // get the key of comment that has to be deleted
    String commentKey = request.getParameter("commentKey");
    try {
      commentsHandler.deleteComment(commentKey);
      response.sendRedirect("./index.html");
    } catch (RequestForbiddenException ex) {
      response.getWriter().println(ex.getMessage());
      response.setStatus(FORBIDDEN);
    } catch (EntityNotFoundException ex) {
      response.getWriter().println("Entity with the given key does not exist");
      response.setStatus(BAD_REQUEST);
    }
  }
}
