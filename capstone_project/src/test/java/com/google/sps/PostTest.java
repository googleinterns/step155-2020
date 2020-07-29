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

package com.google.sps;

import static org.junit.Assert.assertEquals;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.PostService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class PostTest extends Mockito {
  private DatastoreService datastore;
  private BlobstoreService blobstoreService;
  private ImagesService imagesService;
  private HttpServletRequest request;
  private LocalServiceTestHelper serviceHelper =
      new LocalServiceTestHelper(
          new LocalDatastoreServiceTestConfig(), new LocalBlobstoreServiceTestConfig());
  private PostService postService;

  @Before
  public void setUpServiceHelper() {
    serviceHelper.setUp();
    datastore = Mockito.mock(DatastoreService.class);
    blobstoreService = Mockito.mock(BlobstoreService.class);
    imagesService = Mockito.mock(ImagesService.class);
    request = Mockito.mock(HttpServletRequest.class);
    postService = Mockito.spy(new PostService());
    postService.setDatastore(datastore);
    postService.setBlobstore(blobstoreService);
    postService.setImagesService(imagesService);
  }

  @After
  public void tearDownServiceHelper() {
    serviceHelper.tearDown();
  }

  @Test
  public void returnNullOnNoImageBlobstore() {
    // Set up an empty map
    List<BlobKey> blobKeys = Arrays.asList();
    Map<String, List<BlobKey>> blobs = new HashMap<>();
    blobs.put("image", blobKeys);

    // Mock blobstore to return the prebuilt blobs
    when(blobstoreService.getUploads(request)).thenReturn(blobs);
    String expected = null;
    String actual = postService.uploadImage(request);
    assertEquals(expected, actual);
  }

  @Test
  public void returnUrlBlobstore() {
    List<BlobKey> blobKeys = Arrays.asList(new BlobKey("BlobKey"));
    Map<String, List<BlobKey>> blobs = new HashMap<>();
    blobs.put("image", blobKeys);

    // Mock blobstore to return the prebuilt blobs.
    when(blobstoreService.getUploads(request)).thenReturn(blobs);
    // Since the blobkey does not exist in the blobstore, it is mocked to return a link.
    when(imagesService.getServingUrl(any(ServingUrlOptions.class))).thenReturn("/link_to_image");
    String expected = "/link_to_image";
    String actual = postService.uploadImage(request);
    assertEquals(expected, actual);
  }

  @Test
  public void urlStoredInDatastore() {
    doReturn("link_to_image").when(postService).uploadImage(request);
    postService.storePost(request);

    verify(datastore).put(any(Entity.class));
  }

  @Test
  public void upvoteCountIncreasesByOne() {
    Entity postEntity = new Entity("Post");
    postEntity.setProperty("upvotes", 0L);
    List<Entity> posts = Arrays.asList(postEntity);
    PreparedQuery pqMock = mock(PreparedQuery.class);

    when(request.getParameter("id")).thenReturn("0");

    // Mock datastore to return the prebuilt Entity.
    when(datastore.prepare(any(Query.class))).thenReturn(pqMock);
    when(pqMock.asList(any(FetchOptions.class))).thenReturn(posts);

    long expected = 1;
    long actual = postService.upvotePost(request);

    assertEquals(expected, actual);
  }
}
