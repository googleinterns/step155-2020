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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides multiple services for posts, including uploading an image to blobstore and storing posts
 * into datastore, and upvoting posts.
 */
public class PostService {
  private BlobstoreService blobstore;
  private DatastoreService datastore;
  private ImagesService imagesService;

  public PostService() {
    this.blobstore = BlobstoreServiceFactory.getBlobstoreService();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.imagesService = ImagesServiceFactory.getImagesService();
  }

  public void setDatastore(DatastoreService datastore) {
    this.datastore = datastore;
  }

  public void setBlobstore(BlobstoreService blobstore) {
    this.blobstore = blobstore;
  }

  public void setImagesService(ImagesService imagesService) {
    this.imagesService = imagesService;
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
    datastore.put(postEntity);
  }

  /**
   * Increases the upvote count of a post by one. 'request' must have the parameter 'id'. Returns
   * the new upvote count after increase.
   */
  public long upvotePost(HttpServletRequest request) {
    Query query = new Query("Post");

    List<Entity> posts = datastore.prepare(query).asList(FetchOptions.Builder.withDefaults());

    int postID = Integer.parseInt(request.getParameter("id"));
    Entity postToUpvote = posts.get(postID);

    long currentUpvotes = (long) postToUpvote.getProperty("upvotes");
    postToUpvote.setProperty("upvotes", ++currentUpvotes);
    datastore.put(postToUpvote);
    return currentUpvotes;
  }
}
