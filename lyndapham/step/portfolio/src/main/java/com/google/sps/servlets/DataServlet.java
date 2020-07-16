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
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private ArrayList<String> pictureVotes;

  @Override
  public void init() {
    pictureVotes = new ArrayList<>();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    Query query = new Query("Vote").addSort("vote");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Vote> votes = new ArrayList<>();
    boolean hidden = false;

    for (Entity entity : results.asIterable()) {
      String vote = (String) entity.getProperty("vote");
      String comment = (String) entity.getProperty("comment");

      double score = (double) entity.getProperty("sentimentScore");
      Vote entry = new Vote(vote, comment, score);
      if (score < -0.6) {
        hidden = true;
        continue;
      }
      votes.add(entry);
    }

    Gson gson = new Gson();
    if (!votes.isEmpty()) {
      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(votes));
    }

    if (hidden) {
      response.setContentType("text/html;");
      response
          .getWriter()
          .println("Some messages have been hidden due to language! Please be kind.");
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String vote = request.getParameter("picture-vote");
    String comment = request.getParameter("text-input");

    Document doc =
        Document.newBuilder().setContent(comment).setType(Document.Type.PLAIN_TEXT).build();
    LanguageServiceClient languageService = LanguageServiceClient.create();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    double score = (double) sentiment.getScore();
    languageService.close();

    Entity voteEntity = new Entity("Vote");
    voteEntity.setProperty("vote", vote);
    voteEntity.setProperty("comment", comment);
    voteEntity.setProperty("sentimentScore", score);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(voteEntity);

    pictureVotes.add(vote);

    // Redirect to refreshed page
    response.sendRedirect("/index.html");
  }
}
