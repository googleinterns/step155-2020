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

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides multiple services for posts, including uploading an image to blobstore and storing posts
 * into datastore, and upvoting posts.
 */
public class PostService {
  private final BlobstoreService blobstore;
  private final DatastoreService datastore;
  private final ImagesService imagesService;
  private final Clock clock;
  private Map<String, Comparator<Entity>> postSorters;

  public static class Builder {
    private BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
    private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    private ImagesService imagesService = ImagesServiceFactory.getImagesService();
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

    public Builder imagesService(ImagesService imagesService) {
      this.imagesService = imagesService;
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
    this.imagesService = builder.imagesService;
    this.clock = builder.clock;
    postSorters = new HashMap<>();
    initializeSorters();
  }

  /** Returns the uploaded images' url if an image was uploaded. Otherwise, returns null. */
  public String uploadImage(HttpServletRequest request) {
    Map<String, List<BlobKey>> blobs = blobstore.getUploads(request);
    List<BlobKey> blobKeys = blobs.get("image");
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    BlobKey blobKey = blobKeys.get(0);
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);

    if (blobInfo == null || blobInfo.getSize() == 0) {
      blobstore.delete(blobKey);
    }

    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    try {
      URL url = new URL(imagesService.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(options);
    }
  }

  /**
   * Stores the post text and image url into datastore. 'request' much have the parameter 'text'.
   */
  public void storePost(HttpServletRequest request) {
    Entity postEntity = new Entity("Post");
    String message = request.getParameter("text");
    String imageURL = uploadImage(request);
    postEntity.setProperty("imageURL", imageURL);
    postEntity.setProperty("text", message);
    postEntity.setProperty("upvotes", 0);
    // current milliseconds since the unix epoch.
    postEntity.setProperty("timestamp", clock.millis());
    datastore.put(postEntity);
  }

  /**
   * Increases the upvote count of a post by one. 'request' must have the parameter 'id'. Returns
   * the new upvote count after increase.
   */
  public long upvotePost(HttpServletRequest request) {
    long postID = Long.parseLong(request.getParameter("id"));
    Key key = KeyFactory.createKey("Post", postID);
    Entity postToUpvote = null;

    try {
      postToUpvote = datastore.get(key);
    } catch (EntityNotFoundException ok) {
      // this is okay because by default postToUpvote is null and there is a null check
    }

    if (postToUpvote == null) {
      return -1;
    }

    long currentUpvotes = (long) postToUpvote.getProperty("upvotes");
    postToUpvote.setProperty("upvotes", ++currentUpvotes);
    datastore.put(postToUpvote);
    return currentUpvotes;
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

  /** Initialized postSorters with method references to the sorting methods. */
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
}
