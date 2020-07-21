package com.google.sps.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CommentsServletTest {
  private CommentsServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  @BeforeEach
  public void setUp() {
    servlet = new CommentsServlet();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    helper.setUp();
  }

  @Test
  void testPost() {
    Gson gson = new Gson();
    Comment comment = new Comment();
    request.setMethod("POST");
    request.setContentType("text/html");
    request.addParameter("username", "paul");
    request.addParameter("text", "A nice comment");
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), 200);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
