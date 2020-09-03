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
import java.util.List;
import java.util.Optional;

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
    String lowerQuery = searchQuery.toLowerCase();
    Query query = new Query("Post");
    List<Entity> posts = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    // Removes every post that does not contain the search query, meaning the post has no weight.
    posts.removeIf(post -> !calculateWeight(post, lowerQuery).isPresent());
    posts.sort((post1, post2) -> compare(post1, post2, lowerQuery));
    return posts;
  }

  /** Returns an int comparison based on the weights of each post. */
  private static int compare(Entity post1, Entity post2, String lowerQuery) {
    Optional<Integer> post1Type = calculateWeight(post1, lowerQuery);
    Optional<Integer> post2Type = calculateWeight(post2, lowerQuery);
    // If one of the posts does not have a weight, the value defaults to 0.
    return Integer.compare(post2Type.orElse(0), post1Type.orElse(0));
  }

  /**
   * Given a post and a lowercased search query, determines the weight of the post. Returns an empty
   * Optional if the post does not contain the search query.
   */
  private static Optional<Integer> calculateWeight(Entity post, String lowerQuery) {
    String postSchool = ((String) post.getProperty("schoolName")).toLowerCase();
    String postTitle = ((String) post.getProperty("title")).toLowerCase();
    String postText = ((Text) post.getProperty("text")).getValue().toLowerCase();

    if (postSchool.contains(lowerQuery)) {
      return Optional.of(1);
    } else if (postTitle.contains(lowerQuery)) {
      return Optional.of(0);
    } else if (postText.contains(lowerQuery)) {
      return Optional.of(-1);
    }
    return Optional.empty();
  }
}
