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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.CommentInformation;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that allows a user to edit comments and updates the comment on Datastore. */
@WebServlet("/edit")
public class EditServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/");
      response.sendRedirect(loginUrl);
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<CommentInformation> comments = new ArrayList<CommentInformation>();

    for (Entity entity : results.asIterable()) {
      String comment = (String) entity.getProperty("comment");
      String name = (String) entity.getProperty("name");
      long timestamp = (long) entity.getProperty("timestamp");
      double sentimentScore = (double) entity.getProperty("sentimentScore");
      String email = (String) entity.getProperty("email");
      String key = KeyFactory.keyToString(entity.getKey());
      CommentInformation commentInformation =
          new CommentInformation(comment, name, timestamp, sentimentScore, key, email);

      comments.add(commentInformation);
    }

    int commentIndex = Integer.parseInt(request.getParameter("index"));
    CommentInformation commentToEdit = comments.get(commentIndex);

    if (!userEmail.equals(commentToEdit.getEmail())) {
      response.setStatus(403);
      return;
    }

    try {
      Entity commentEditEntity = datastore.get(KeyFactory.stringToKey(commentToEdit.getKey()));
      commentEditEntity.setProperty("comment", request.getParameter("new-comment"));
      datastore.put(commentEditEntity);
    } catch (EntityNotFoundException ok) {
      // The entity was not found, which means it has been deleted. The error
      // is okay since the JavaScript will handle this by warning the user and
      // preventing any changes from happening.
      response.setStatus(404);
    }
  }
}
