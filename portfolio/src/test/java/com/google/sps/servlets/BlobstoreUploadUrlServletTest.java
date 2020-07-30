package com.google.sps.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class BlobstoreUploadUrlServletTest {
  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private BlobstoreUploadUrlServlet servlet;
  private final int OK = 200;
  private final int FORBIDDEN = 403;
  @BeforeEach
  public void setUp() {
    helper.setUp();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    servlet = new BlobstoreUploadUrlServlet();
  }

  @Test
  public void testGetBlobstoreUrlByAuthenticatedUserSuccess() {
    // log the user in
    helper.setEnvEmail("test@example.com");
    helper.setEnvIsLoggedIn(true);
    helper.setEnvAuthDomain("envAuthDomain");
    try {
      servlet.doGet(request, response);
      assertEquals(response.getStatus(), OK);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }

  @Test
  public void testGetBlobstoreUrlByNotAuthenticatedUserFails() {
    // omit the authentication
    try {
      servlet.doGet(request, response);
      assertEquals(response.getStatus(), FORBIDDEN);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
  }
}
