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

import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.data.Authenticator;
import com.google.sps.data.PostService;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that handles upvoting a post. increases the upvote count of a post by one at every POST
 * request.
 */
@WebServlet("/upvote")
public class UpvoteServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!Authenticator.isLoggedIn(response, "/pages/comments.jsp")) {
      return;
    }

    response.setContentType("application/json;");

    Gson gson = new Gson();
    Optional<Long> newCount = PostService.Builder.builder().build().upvotePost(request);

    if (newCount.isPresent()) {
      response.getWriter().println(gson.toJson(newCount.get()));
    } else {
      response.getWriter().println("[]");
    }
  }
}
