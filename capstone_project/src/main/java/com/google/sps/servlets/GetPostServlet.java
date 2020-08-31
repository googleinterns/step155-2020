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
import com.google.sps.data.PostService;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Manually serves a post, using the id to render it onto the page. */
@WebServlet("/serve-post")
public class GetPostServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // The user is redirected to the the page with all posts rather than the single post.
    if (!Authenticator.isLoggedIn(response, "/pages/comments.jsp")) {
      return;
    }

    long postID = Long.parseLong(request.getParameter("post-id"));
    PostService postService = PostService.Builder.builder().build();
    Optional<Entity> optionalPost = postService.getEntityFromId(postID);

    if (optionalPost.isPresent()) {
      response.setContentType("application/json");
      Gson gson = new Gson();
      Post post = PostService.convertEntityToPost(optionalPost.get());
      response.getWriter().println(gson.toJson(post));
    } else {
      response.setStatus(404);
    }
  }
}
