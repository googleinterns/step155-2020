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
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import com.google.sps.data.CommentInformation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
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

    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(comments));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("user-comment");
    String name = request.getParameter("user-name");

    // Check for name is not required since the HTML input requires that the
    // user provide a name.
    if (comment.isEmpty()) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a valid comment.");
      return;
    }

    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      String loginUrl = userService.createLoginURL("/");
      response.sendRedirect(loginUrl);
      return;
    }

    String userEmail = userService.getCurrentUser().getEmail();

    double score = determineSentimentScore(comment);

    // Prevent users from submitting negative comments.
    if (score > 0.1) {
      storeComment(comment, name, score, userEmail);
      response.sendRedirect("/");
    } else {
      response.setContentType("text/html;");
      response.getWriter().println("<p>Please refrain from entering negative comments.</p>");
    }
  }

  private void storeComment(String comment, String name, double sentimentScore, String email) {
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("timestamp", new Date().getTime());
    commentEntity.setProperty("sentimentScore", sentimentScore);
    commentEntity.setProperty("email", email);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
  }

  private double determineSentimentScore(String comment) throws IOException {
    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    return sentiment.getScore();
  }
}
