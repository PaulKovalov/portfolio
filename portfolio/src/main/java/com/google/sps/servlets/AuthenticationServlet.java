/**
 * File provides AuthenticationServlet class, which utilizes Google Users API
 * to manage Paul's portfolio users' authentication
 */

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.JsonObject;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Uses Google Users API
 * GET request support only
 * - checks if the user is logged in
 * - if yes:
 * -- returns status (logged in)
 * -- returns logout url
 * -- returns user's email
 * - if no:
 * -- returns status (logged out)
 * -- returns the login url
 */
@WebServlet("/auth")
public class AuthenticationServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    // json object for the response
    JsonObject jsonObject = new JsonObject();
    if (userService.isUserLoggedIn()) {
      String userEmail = userService.getCurrentUser().getEmail();
      String urlToRedirectToAfterUserLogsOut = "/";
      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
      // construct a json objects
      jsonObject.addProperty("authenticated", true);
      jsonObject.addProperty("logoutUrl", logoutUrl);
      jsonObject.addProperty("email", userEmail);
    } else {
      String urlToRedirectToAfterUserLogsIn = "/";
      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
      // construct a json object
      jsonObject.addProperty("authenticated", false);
      jsonObject.addProperty("loginUrl", loginUrl);
    }
    // write user's data converted to the json in the response
    response.getWriter().println(jsonObject.toString());
  }
}
