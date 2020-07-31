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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
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
    String text = request.getParameter("text");
    String replyTo = request.getParameter("replyTo"); // this one is optional
    String uploadedFileUrl = getUploadedFileUrl(request, "image");
    // Get currently logged in user from Users API (for now username == email)
    Comment comment = new Comment(getUserNickname(), text);
    // if comment is a reply, add reply key to the comment
    if (replyTo != null) {
      comment.replyTo = replyTo;
    }
    // if user submitted a file, add file url to the comment
    if (uploadedFileUrl != null) {
      comment.imageUrl = uploadedFileUrl;
    } 
    CommentsHandler commentsHandler = new CommentsHandler();
    try {
      // when comment is saved, it is returned as a JSON string 
      String serializedComment = commentsHandler.saveComment(comment);
      // use the response's writer to return the comment
      response.getWriter().println(serializedComment);
      response.sendRedirect("/index.html");
    } catch (BadRequestException ex) {
      response.sendError(BAD_REQUEST, ex.getMessage());
    }
  }
  // returns current user's nickname
  public String getUserNickname() {
    UserService userService = UserServiceFactory.getUserService();
    // for now email == username, later I will change it so username will be stored in the datastore
    return userService.getCurrentUser().getEmail();
  }

  // Returns a URL that points to the uploaded file, or null if the user didn't upload a file.
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
    Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(formInputElementName);
    // User submitted form without selecting a file, so we can't get a URL. (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }
    // Our form only contains a single file input, so get the first index.
    BlobKey blobKey = blobKeys.get(0);
    // User submitted form without selecting a file, so we can't get a URL. (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return null;
    }
    // Use ImagesService to get a URL that points to the uploaded file.
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }
}
