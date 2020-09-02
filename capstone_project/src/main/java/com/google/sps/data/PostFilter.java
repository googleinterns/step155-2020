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

package com.google.sps.data;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;
import java.util.ArrayList;
import java.util.List;

/** Filters and returns all posts that contain the search query. */
public class PostFilter {
  public static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  private PostFilter() {}

  /**
   * Filters all posts that contain the searchQuery. Returns a list of all posts in priortized
   * order: posts from the school first, posts with the search query in the title, and lastly text
   * that contains the search query.
   */
  public static List<Entity> filterPosts(String searchQuery) {
    List<Entity> postsFound = new ArrayList<>();
    String lowerQuery = searchQuery.toLowerCase();

    Query query = new Query("Post");
    List<Entity> allPosts = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
    for (Entity post : allPosts) {
      String postSchool = ((String) post.getProperty("schoolName")).toLowerCase();
      String postTitle = ((String) post.getProperty("title")).toLowerCase();
      String postText = ((Text) post.getProperty("text")).getValue().toLowerCase();

      if (postSchool.contains(lowerQuery)) {
        post.setProperty("weight", 1);
      } else if (postTitle.contains(lowerQuery)) {
        post.setProperty("weight", 0);
      } else if (postText.contains(lowerQuery)) {
        post.setProperty("weight", -1);
      }

      if (post.hasProperty("weight")) {
        postsFound.add(post);
      }
    }

    postsFound.sort(PostFilter::compare);
    postsFound.forEach(PostFilter::removeWeight);
    return postsFound;
  }

  /** Returns an int comparison based on the weights of each post. */
  private static int compare(Entity post1, Entity post2) {
    int post1Type = (int) post1.getProperty("weight");
    int post2Type = (int) post2.getProperty("weight");
    return Integer.compare(post2Type, post1Type);
  }

  /** Removes the temporarily added weight property. */
  private static void removeWeight(Entity post) {
    post.removeProperty("weight");
  }
}
