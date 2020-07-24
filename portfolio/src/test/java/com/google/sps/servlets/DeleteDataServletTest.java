package com.google.sps.servlets;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.gson.JsonArray;
import com.google.appengine.repackaged.com.google.gson.JsonElement;
import com.google.appengine.repackaged.com.google.gson.JsonObject;
import com.google.appengine.repackaged.com.google.gson.JsonParser;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class DeleteDataServletTest {
  private final int REDIRECT = 302;
  private DeleteDataServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @BeforeEach
  public void setUp() {
    servlet = new DeleteDataServlet();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    helper.setUp();
  }

  @Test
  public void testDeleteComments() {
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    final int NUM_OF_COMMENTS = 5;
    for (int i = 0; i < NUM_OF_COMMENTS; ++i) {
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("username", "username");
      commentEntity.setProperty("text", "text");
      ds.put(commentEntity);
    }
    try {
      servlet.doPost(request, response);
      assertEquals(0, ds.prepare(new Query("Comment")).countEntities());
    } catch(IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
