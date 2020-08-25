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
import com.google.sps.data.PostService;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Fetches all posts that belong to the school. GET request must have a school-name attribute. */
@WebServlet("/fetch-school-posts")
public class FetchSchoolPostsServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!Authenticator.isLoggedIn(response, "/")) {
      return;
    }

    String schoolName = request.getParameter("school-name");
    if (schoolName == null || schoolName.isEmpty()) {
      return;
    }

    PostService postService = PostService.Builder.builder().build();
    List<Entity> posts = postService.getPostsBySchool(schoolName);
    response.setContentType("application/json");
    response.getWriter().println(new Gson().toJson(posts));
  }
}
