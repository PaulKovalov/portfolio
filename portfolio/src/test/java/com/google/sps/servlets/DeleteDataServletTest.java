package com.google.sps.servlets;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class DeleteDataServletTest {
  private final int REDIRECT = 302;
  private final int FORBIDDEN = 403;
  private DeleteDataServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig());

  @BeforeEach
  public void setUp() {
    servlet = new DeleteDataServlet();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    helper.setUp();
  }

  @Test
  public void testDeleteCommentsSuccess() {
    // log the user in
    String testEmail = "test@example.com";
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(testEmail);
    helper.setEnvAuthDomain("envAuthDomain");

    // create comment entities
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", testEmail);
    commentEntity.setProperty("text", "text");
    ds.put(commentEntity);
    Entity commentEntityReply = new Entity("Comment");
    commentEntityReply.setProperty("username", "username");
    commentEntityReply.setProperty("text", "text");
    commentEntityReply.setProperty("replyTo", KeyFactory.keyToString(commentEntity.getKey()));
    ds.put(commentEntityReply);
    request.addParameter("commentKey", KeyFactory.keyToString(commentEntity.getKey()));
    // add some random comment that must not be affected
    Entity otherComment = new Entity("Comment");
    otherComment.setProperty("username", "usernameOther");
    otherComment.setProperty("text", "textOther");
    ds.put(otherComment);

    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), REDIRECT);
      // one must left
      PreparedQuery results = ds.prepare(new Query("Comment"));
      assertEquals(1, results.countEntities());
      for (Entity entity : results.asIterable()) {
        assertEquals(entity.getProperty("username"), "usernameOther");
        assertEquals(entity.getProperty("text"), "textOther");
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testDeleteCommentsFailNoAuth() {
    // this test doesn't log the user in
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", "username");
    commentEntity.setProperty("text", "text");
    ds.put(commentEntity);
    request.addParameter("commentKey", KeyFactory.keyToString(commentEntity.getKey()));
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), FORBIDDEN);
      // one must left, it should not be deleted
      PreparedQuery results = ds.prepare(new Query("Comment"));
      assertEquals(1, results.countEntities());
      for (Entity entity : results.asIterable()) {
        assertEquals(entity.getProperty("username"), "username");
        assertEquals(entity.getProperty("text"), "text");
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testDeleteCommentsFailWrongAuth() {
    // log the user in
    String testEmail = "test@example.com";
    helper.setEnvIsLoggedIn(true);
    helper.setEnvEmail(testEmail);
    helper.setEnvAuthDomain("envAuthDomain");
    // in case user attempts to delete comments of another user
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity commentEntity = new Entity("Comment");
    // use another email here, not the user's who is logged in
    commentEntity.setProperty("username", "username");
    commentEntity.setProperty("text", "text");
    ds.put(commentEntity);
    request.addParameter("commentKey", KeyFactory.keyToString(commentEntity.getKey()));
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), FORBIDDEN);
      // one must left, it should not be deleted
      PreparedQuery results = ds.prepare(new Query("Comment"));
      assertEquals(1, results.countEntities());
      for (Entity entity : results.asIterable()) {
        assertEquals(entity.getProperty("username"), "username");
        assertEquals(entity.getProperty("text"), "text");
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
