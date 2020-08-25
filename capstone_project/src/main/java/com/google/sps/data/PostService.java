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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Text;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides multiple services for posts, including uploading an image to blobstore and storing posts
 * into datastore, and upvoting posts.
 */
public class PostService {
  private final BlobstoreService blobstore;
  private final DatastoreService datastore;
  private final Clock clock;
  private Map<String, Comparator<Entity>> postSorters;

  public static class Builder {
    private BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private Clock clock = Clock.systemUTC();

    public static Builder builder() {
      return new Builder();
    }

    public Builder blobstore(BlobstoreService blobstore) {
      this.blobstore = blobstore;
      return this;
    }

    public Builder datastore(DatastoreService datastore) {
      this.datastore = datastore;
      return this;
    }

    public Builder clock(Clock clock) {
      this.clock = clock;
      return this;
    }

    public PostService build() {
      return new PostService(this);
    }
  }

  private PostService(Builder builder) {
    this.blobstore = builder.blobstore;
    this.datastore = builder.datastore;
    this.clock = builder.clock;
    postSorters = new HashMap<>();
    initializeSorters();
  }

  /**
   * Returns the uploaded files' blob key string representation inside an Optional, if a file was
   * uploaded. Otherwise, returns an empty Optional object.
   */
  public Optional<String> uploadFile(HttpServletRequest request) {
    Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("file");

    if (blobKeys == null || blobKeys.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(blobKeys.get(0).getKeyString());
  }

  /**
   * Stores the post text and file blobkey into datastore. 'request' must have the parameter 'text'.
   */
  public void storePost(HttpServletRequest request) {
    Entity postEntity = new Entity("Post");
    Text message = new Text(request.getParameter("text"));
    String fileType = request.getParameter("file-type");
    String title = request.getParameter("title");
    String schoolName = request.getParameter("schools");
    Optional<String> fileBlobKey = uploadFile(request);

    if (fileType == null || fileType.isEmpty()) {
      fileType = "none";
    }

    // Only set the fileBlobKey if there is one. There is no need to store a blobKey if there isn't
    // one.
    if (fileBlobKey.isPresent()) {
      postEntity.setProperty("fileBlobKey", fileBlobKey.get());
    }

    EmbeddedEntity reactions = getReactionsEntity();
    postEntity.setProperty("reactions", reactions);
    postEntity.setProperty("fileType", fileType);
    postEntity.setProperty("text", message);
    postEntity.setProperty("upvotes", 0);
    postEntity.setProperty("schoolName", schoolName);
    // current milliseconds since the unix epoch.
    postEntity.setProperty("timestamp", clock.millis());
    postEntity.setProperty("title", title);
    datastore.put(postEntity);
  }

  /**
   * Increases the upvote count of a post by one. 'request' must have the parameter 'id'. Returns
   * the new upvote count after increase. If post does not exist, returns an empty Optional
   * instance.
   */
  public Optional<Long> upvotePost(HttpServletRequest request) {
    long postID = Long.parseLong(request.getParameter("id"));
    Optional<Entity> post = getEntityFromId(postID);
    if (!post.isPresent()) {
      return Optional.empty();
    }

    Entity postToUpvote = post.get();
    long currentUpvotes = (long) postToUpvote.getProperty("upvotes");
    postToUpvote.setProperty("upvotes", ++currentUpvotes);
    datastore.put(postToUpvote);
    return Optional.of(currentUpvotes);
  }

  /**
   * Sorts a list of entities based on the sort type passed in and returns the sorted list. If the
   * sort type does not exist, an empty list is returned.
   */
  public List<Entity> sortEntities(String sortType, List<Entity> entities) {
    Comparator<Entity> sortMethod = postSorters.get(sortType);
    if (sortMethod == null) {
      return Arrays.asList();
    }

    Collections.sort(entities, sortMethod);
    return entities;
  }

  /** Initializes postSorters with method references to the sorting methods. */
  private void initializeSorters() {
    postSorters.put("new", this::sortByNew);
    postSorters.put("top", this::sortByTop);
    postSorters.put("trending", this::sortByTrending);
  }

  /** Returns an int for comparison of the entities. Should be used within comparing only. */
  private int sortByNew(Entity first, Entity second) {
    long firstTime = (long) first.getProperty("timestamp");
    long secondTime = (long) second.getProperty("timestamp");
    return Long.compare(secondTime, firstTime);
  }

  /** Returns an int for comparison of the entities. Should be used within comparing only. */
  private int sortByTop(Entity first, Entity second) {
    long firstUpvotes = (long) first.getProperty("upvotes");
    long secondUpvotes = (long) second.getProperty("upvotes");
    return Long.compare(secondUpvotes, firstUpvotes);
  }

  /** Returns an int for comparison of the entities. Should be used within comparing only. */
  private int sortByTrending(Entity first, Entity second) {
    float firstRatio = getUpvoteRatio(first);
    float secondRatio = getUpvoteRatio(second);

    return Float.compare(secondRatio, firstRatio);
  }

  /** Gets the amount of upvotes a post earned per minute since the time of upload. */
  private float getUpvoteRatio(Entity post) {
    long upvotes = (long) post.getProperty("upvotes");
    long postTime = (long) post.getProperty("timestamp");
    long currentTime = clock.millis();
    long msDifference = currentTime - postTime;
    float minDifference = msDifference / 60f / 1000f;

    return upvotes / minDifference;
  }

  /**
   * Increases the reaction count of the submitted reaction by one. Returns the new reaction count
   * after increase. If the post or reaction does not exist, returns an empty Optional instance.
   */
  public Optional<Long> reactToPost(HttpServletRequest request) {
    String reaction = request.getParameter("reaction");
    if (reaction == null || reaction.isEmpty()) {
      return Optional.empty();
    }

    long postID = Long.parseLong(request.getParameter("post-id"));
    Optional<Entity> post = getEntityFromId(postID);
    if (!post.isPresent()) {
      return Optional.empty();
    }

    Entity postToReact = post.get();
    EmbeddedEntity reactions = (EmbeddedEntity) postToReact.getProperty("reactions");
    long reactionCount = (long) reactions.getProperty(reaction);

    // Update the reaction count, then update the Reactions Entity of the post.
    reactions.setProperty(reaction, ++reactionCount);
    postToReact.setProperty("reactions", reactions);
    datastore.put(postToReact);
    return Optional.of(reactionCount);
  }

  /** Returns an EmbeddedEntity with all possible reactions set to 0. */
  private EmbeddedEntity getReactionsEntity() {
    EmbeddedEntity reactions = new EmbeddedEntity();
    reactions.setProperty("laugh", 0L);
    reactions.setProperty("love", 0L);
    reactions.setProperty("sad", 0L);
    reactions.setProperty("think", 0L);
    reactions.setProperty("wow", 0L);
    reactions.setProperty("yikes", 0L);
    return reactions;
  }

  /**
   * Using the post id provided, recreates the key of the post. If the Entity exists, returns an
   * Optional with that Entity. Otherwise, returns an empty Optional.
   */
  public Optional<Entity> getEntityFromId(long postID) {
    Key key = KeyFactory.createKey("Post", postID);
    Optional<Entity> post = Optional.empty();
    try {
      post = Optional.ofNullable(datastore.get(key));
    } catch (EntityNotFoundException ok) {
      // this is okay because by default post is empty so there is a default value.
    }
    return post;
  }

  /**
   * Returns a list of all post entities with the given school name. An empty list is returned if no
   * posts belong to the school.
   */
  public List<Entity> getPostsBySchool(String schoolName) {
    Filter nameFilter = new FilterPredicate("schoolName", FilterOperator.EQUAL, schoolName);
    Query query = new Query("Post").setFilter(nameFilter);
    return datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());
  }
}
