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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.gson.Gson;
import com.google.sps.data.Authenticator;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/get-resources")
public class ResourcesServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!Authenticator.isLoggedIn(response, "/pages/resources.html")) {
      return;
    }

    Query query = new Query("ResourceSubmission");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Entity> resources = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(resources));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    if (!Authenticator.isLoggedIn(response, "/pages/resources.html")) {
      return;
    }

    String category = (String) request.getParameter("category-selector");
    String resourceName = (String) request.getParameter("title");
    String resourceURL = (String) request.getParameter("url");

    Entity resourceSubmission = new Entity("ResourceSubmission");
    resourceSubmission.setProperty("category", category);
    resourceSubmission.setProperty("resourceName", resourceName);
    resourceSubmission.setProperty("resourceURL", resourceURL);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(resourceSubmission);

    // Redirect to refreshed page
    response.sendRedirect("/pages/resources.html");
  }
}
