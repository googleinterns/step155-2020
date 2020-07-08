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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.CommentInformation;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Servlet that deletes comments from the Datastore upon user request.
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/admin");
      response.sendRedirect(loginUrl);
      return;
    }

    Query query =
        new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    ArrayList<Map<String, Object>> comments =
        new ArrayList<Map<String, Object>>();

    for (Entity entity : results.asIterable()) {
      comments.add(entity.getProperties());
    }

    request.setAttribute("commentData", comments);
    request.getRequestDispatcher("/pages/AdminPage.jsp")
        .forward(request, response);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    Gson gson = new Gson();
    CommentInformation[] commentsInformation =
        gson.fromJson(request.getReader(), CommentInformation[].class);

    Query query =
        new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      Map<String, Object> commentData = entity.getProperties();
      // A conversion from Object to Long is not possible; therefore, cast to
      // String before casting to Long.
      String stringTimestamp = String.valueOf(commentData.get("timestamp"));
      long dataStoreTimestamp = Long.parseLong(stringTimestamp);
      for (CommentInformation comment : commentsInformation) {
        // At the moment, the timestamp is closest unique element for each
        // comment.
        if (comment.getTimestamp() == dataStoreTimestamp) {
          datastore.delete(entity.getKey());
          break;
        }
      }
    }
  }
}
