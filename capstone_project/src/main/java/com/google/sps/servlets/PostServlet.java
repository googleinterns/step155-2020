// Copyright 2020 Google LLC
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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.data.PostAnalysis;
import com.google.sps.data.Authenticator;
import com.google.sps.data.PostService;
import com.google.sps.data.Resource;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles loading and uploading posts. */
@WebServlet("/post-process")
public class PostServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      System.out.println("USER IS NOT LOGGED IN");
      String loginUrl = userService.createLoginURL("/pages/maps.html");
      response.sendRedirect(loginUrl);
      return;
    }

    Resource resource = new Resource();
    resource.addPreexistingResources();

    response.setContentType("application/json;");
    Query query = new Query("Post");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    List<Entity> posts = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(posts));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    if (!Authenticator.isLoggedIn(response, "/pages/comments.jsp")) {
      return;
    }

    PostService postService = PostService.Builder.builder().build();
    postService.storePost(request);
    PostAnalysis postAnalysis = new PostAnalysis(request);

    request.getSession().setAttribute("categories", postAnalysis.getCategories());
    response.sendRedirect("/pages/comments.jsp");
  }
}
