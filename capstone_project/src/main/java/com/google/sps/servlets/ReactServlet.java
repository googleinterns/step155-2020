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

import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.gson.Gson;
import com.google.sps.data.Authenticator;
import com.google.sps.data.PostService;
import java.io.IOException;
import java.util.Optional;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Creates a blobstore url with a parameter for the file type. */
@WebServlet("/post-react")
public class ReactServlet extends HttpServlet {
  private static final Gson gson = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PostService postService = PostService.Builder.builder().build();
    long postID = Long.parseLong(request.getParameter("post-id"));

    Optional<Entity> optionalEntity = postService.getEntityFromId(postID);
    if (!optionalEntity.isPresent()) {
      return;
    }

    Entity post = optionalEntity.get();
    EmbeddedEntity reactions = (EmbeddedEntity) post.getProperty("reactions");
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(reactions));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    if (!Authenticator.isLoggedIn(response, "/pages/comments.jsp")) {
      return;
    }

    PostService postService = PostService.Builder.builder().build();
    Optional<Long> newReactCount = postService.reactToPost(request);
    if (newReactCount.isPresent()) {
      response.getWriter().println(gson.toJson(newReactCount.get()));
    } else {
      response.getWriter().println("[]");
    }
  }
}
