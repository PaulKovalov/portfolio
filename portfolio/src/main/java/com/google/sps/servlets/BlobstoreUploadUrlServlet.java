/**
 * Has BlobstoreUploadUrlServlet servlet which is used for generating Blobstore upload links
 */

package com.google.sps.servlets;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses Google Blobstore API
 * - doGet:
 * -- checks if the user is authenticated
 * -- Generates a blob upload link
 * -- returns it in JSON format {"url": link}
 *
 */
public class BlobstoreUploadUrlServlet extends HttpServlet {
  private final int FORBIDDEN = 403;
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
      // generate a upload link
      BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
      String uploadUrl = blobstoreService.createUploadUrl("/my-form-handler");
      // create a json object from the link
      JsonObject jsonObject = new JsonObject();
      jsonObject.addProperty("url", uploadUrl);
      response.setContentType("application/json");
      // send it back
      response.getWriter().println(jsonObject.toString());
    } else {
      response.setStatus(FORBIDDEN);
      response.getWriter().println("Not authenticated");
    }
  }
}
