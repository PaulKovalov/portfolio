package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    CommentsHandler commentsHandler = new CommentsHandler();
    commentsHandler.deleteComments();
    response.sendRedirect("./index.html");
  }
}
