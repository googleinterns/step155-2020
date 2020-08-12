/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.data;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.OrderBy;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for deleting posts that were created longer than 24 hours ago. Note: This uses google
 * cloud datastore since a cloud function does not have access to app engine datstore properties.
 * Cloud datastore uses the same database as app engine. The only differences are in API usages and
 * methods.
 */
public class DeletePostService {
  // Represents one day in milliseconds
  private final long ONE_DAY = 86400000;
  private Datastore datastore;
  private Clock clock;

  public DeletePostService() {
    this.datastore = DatastoreOptions.getDefaultInstance().getService();
    this.clock = Clock.systemUTC();
  }

  public void setClock(Clock clock) {
    this.clock = clock;
  }

  public void setDatastore(Datastore datastore) {
    this.datastore = datastore;
  }

  // Retrieves a query of all posts entities in ascending order based on timestamp. Returns a list
  // of entities of all posts inside of datastore in sorted ascending order.
  public List<Entity> retrieveAllPosts() {
    Query<Entity> query =
        Query.newEntityQueryBuilder().setKind("Post").setOrderBy(OrderBy.asc("timestamp")).build();

    QueryResults<Entity> queriedPosts = datastore.run(query);
    List<Entity> posts = new ArrayList<>();
    queriedPosts.forEachRemaining(posts::add);
    return posts;
  }

  // Calculates the elasped time since the post entity was uploaded. Returns true if more than 24
  // hours have elapased since its creation time. Otherwise, returns false.
  private boolean isOlderThan24Hours(Entity post) {
    long creationTime = post.getLong("timestamp");
    long currentTime = clock.millis();
    long elapsedTime = currentTime - creationTime;

    return elapsedTime >= ONE_DAY;
  }

  // Deletes all posts with an elapsed time equal to or older than 24 hours from its creation time.
  public void deleteOldPosts() {
    List<Entity> posts = retrieveAllPosts();
    if (posts.isEmpty()) {
      return;
    }

    for (Entity post : posts) {
      if (isOlderThan24Hours(post)) {
        datastore.delete(post.getKey());
      } else {
        // These are sorted in ascended order largest to smallest timestamp (larger timestamp means
        // newer date), which means that as soon as one is not older than 24 hours, the remaining
        // ones are also less than 24 hours.
        break;
      }
    }
  }
}
