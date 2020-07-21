package com.google.sps.servlets;
import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
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

class CommentsServletTest {
  private final int BAD_REQUEST = 400;
  private final int OK = 200;
  private CommentsServlet servlet;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  // helper method that quotes the string
  public String quoted(String unquoted) {
    return "\"" + unquoted + "\"";
  }

  @BeforeEach
  public void setUp() {
    servlet = new CommentsServlet();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    helper.setUp();
  }

  @Test
  public void testCreateWithValidPayload() {
    Gson gson = new Gson();
    Comment comment = new Comment();
    request.setMethod("POST");
    request.setContentType("text/html");
    request.addParameter("username", "paul");
    request.addParameter("text", "A nice comment");
    // add a valid reply to field
    String replyToKey = KeyFactory.keyToString(commentEntity.getKey());
    request.addParameter("replyTo", replyToKey);
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), OK);
      String responseString = response.getContentAsString();
      JsonObject jsonObject = (new JsonParser()).parse(responseString).getAsJsonObject();
      assertEquals(jsonObject.get("username").toString(), quoted("Paul"));
      assertEquals(jsonObject.get("text").toString(), quoted("A nice comment"));
      assertEquals(jsonObject.get("replyTo").toString(), quoted(replyToKey));
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testCreateWithInvalidPayload_v1() {
    Gson gson = new Gson();
    String jsonRequest = gson.toJson(null);
    request.setMethod("POST");
    request.setContentType("application/json");
    request.setContent(jsonRequest.getBytes());
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), BAD_REQUEST);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testCreateWithInvalidPayload_v2() {
    Gson gson = new Gson();
    Comment comment = new Comment();
    // set the username only, without the text
    comment.username = "Paul";
    String jsonRequest = gson.toJson(comment);
    request.setMethod("POST");
    request.setContentType("application/json");
    request.setContent(jsonRequest.getBytes());
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), BAD_REQUEST);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testCreateWithInvalidPayload_v3() {
    Gson gson = new Gson();
    Comment comment = new Comment();
    // set the username and text full of spaces
    comment.username = "Paul";
    comment.text = "     ";
    String jsonRequest = gson.toJson(comment);
    request.setMethod("POST");
    request.setContentType("application/json");
    request.setContent(jsonRequest.getBytes());
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), BAD_REQUEST);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testCreateWithInvalidPayload_v4() {
    request.setMethod("POST");
    request.addParameter("username", "Paul");
    request.addParameter("text", "A nice comment");
    // add reference to nuexisting comment
    request.addParameter("replyTo", "Unexisting key");
    try {
      servlet.doPost(request, response);
      assertEquals(response.getStatus(), BAD_REQUEST);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testGetComments_v1() {
    // insert comment entity to the datastore
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Comment")).countEntities(withLimit(10)));
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("username", "paul");
    commentEntity.setProperty("text", "Very cool comment text");
    ds.put(commentEntity);
    request.setMethod("GET");
    try {
      servlet.doGet(request, response);
      assertEquals(response.getStatus(), OK);
      String responseString = response.getContentAsString();
      JsonArray jsonArray = (new JsonParser()).parse(responseString).getAsJsonArray();
      for (JsonElement element : jsonArray) {
        JsonObject jsonObject = element.getAsJsonObject();
        assertEquals(jsonObject.get("username").toString(), quoted("paul"));
        assertEquals(jsonObject.get("text").toString(), quoted("Very cool comment text"));
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
