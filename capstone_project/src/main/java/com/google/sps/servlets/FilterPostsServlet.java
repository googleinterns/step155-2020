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

import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.Authenticator;
import com.google.sps.data.Post;
import com.google.sps.data.PostFilter;
import com.google.sps.data.PostService;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filters and returns all posts that contain the search query. GET request must have a search
 * attribute.
 */
@WebServlet("/filter-posts")
public class FilterPostsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!Authenticator.isLoggedIn(response, "/")) {
      return;
    }

    String searchQuery = request.getParameter("search");
    if (searchQuery == null || searchQuery.isEmpty()) {
      return;
    }

    List<Entity> postEntities = PostFilter.filterPosts(searchQuery);
    List<Post> foundPosts =
        postEntities.stream().map(PostService::convertEntityToPost).collect(Collectors.toList());

    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(foundPosts));
  }
}
