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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.development.testing.LocalBlobstoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.PostService;
import java.time.Clock;
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
  private Clock clock;
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
    request = Mockito.mock(HttpServletRequest.class);
    clock = Mockito.mock(Clock.class);
    postService =
        Mockito.spy(
            PostService.Builder.builder()
                .datastore(datastore)
                .blobstore(blobstoreService)
                .clock(clock)
                .build());
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
    String actual = postService.uploadFile(request);
    assertEquals(expected, actual);
  }

  @Test
  public void returnUrlBlobstore() {
    List<BlobKey> blobKeys = Arrays.asList(new BlobKey("BlobKey"));
    Map<String, List<BlobKey>> blobs = new HashMap<>();
    blobs.put("file", blobKeys);

    // Mock blobstore to return the prebuilt blobs.
    when(blobstoreService.getUploads(request)).thenReturn(blobs);
    // Since the blobkey does not exist in the blobstore, it is mocked to return a link.
    String expected = "BlobKey";
    String actual = postService.uploadFile(request);
    assertEquals(expected, actual);
  }

  @Test
  public void urlStoredInDatastore() {
    doReturn("BlobKey").when(postService).uploadFile(request);
    postService.storePost(request);

    verify(datastore).put(any(Entity.class));
  }

  @Test
  public void upvoteCountIncreasesByOne() throws EntityNotFoundException {
    Entity postEntity = new Entity("Post");
    postEntity.setProperty("upvotes", 0L);

    when(request.getParameter("id")).thenReturn("1");
    // Mock datastore to return the prebuilt Entity.
    when(datastore.get(any(Key.class))).thenReturn(postEntity);
    long expected = 1;
    long actual = postService.upvotePost(request);

    assertEquals(expected, actual);
  }

  @Test
  public void returnNegOneOnEntityNotFound() {
    when(request.getParameter("id")).thenReturn("1");

    long expected = -1;
    long actual = postService.upvotePost(request);
    assertEquals(expected, actual);
  }

  @Test
  public void postsSortByNew() {
    Entity firstPostEntity = new Entity("Post");
    Entity secondPostEntity = new Entity("Post");
    Entity thirdPostEntity = new Entity("Post");
    firstPostEntity.setProperty("timestamp", 3L);
    secondPostEntity.setProperty("timestamp", 1L);
    thirdPostEntity.setProperty("timestamp", 2L);
    List<Entity> posts = Arrays.asList(firstPostEntity, secondPostEntity, thirdPostEntity);

    List<Entity> expected = Arrays.asList(firstPostEntity, thirdPostEntity, secondPostEntity);
    List<Entity> actual = postService.sortEntities("new", posts);

    assertEquals(expected, actual);
  }

  @Test
  public void postsSortByTop() {
    Entity firstPostEntity = new Entity("Post");
    Entity secondPostEntity = new Entity("Post");
    Entity thirdPostEntity = new Entity("Post");
    firstPostEntity.setProperty("upvotes", 3L);
    secondPostEntity.setProperty("upvotes", 1L);
    thirdPostEntity.setProperty("upvotes", 2L);
    List<Entity> posts = Arrays.asList(firstPostEntity, secondPostEntity, thirdPostEntity);

    List<Entity> expected = Arrays.asList(firstPostEntity, thirdPostEntity, secondPostEntity);
    List<Entity> actual = postService.sortEntities("top", posts);

    assertEquals(expected, actual);
  }

  @Test
  public void postsSortByTrending() {
    Entity firstPostEntity = new Entity("Post");
    Entity secondPostEntity = new Entity("Post");
    Entity thirdPostEntity = new Entity("Post");
    firstPostEntity.setProperty("timestamp", 100L);
    firstPostEntity.setProperty("upvotes", 2L);
    secondPostEntity.setProperty("timestamp", 150L);
    secondPostEntity.setProperty("upvotes", 50L);
    thirdPostEntity.setProperty("timestamp", 199L);
    thirdPostEntity.setProperty("upvotes", 5L);
    List<Entity> posts = Arrays.asList(firstPostEntity, secondPostEntity, thirdPostEntity);

    List<Entity> expected = Arrays.asList(thirdPostEntity, secondPostEntity, firstPostEntity);

    // Mock clock to return a static value to avoid failing tests.
    when(clock.millis()).thenReturn(200L);
    List<Entity> actual = postService.sortEntities("trending", posts);

    assertEquals(expected, actual);
  }

  @Test
  public void returnEmptyListOnInvalidSortMethod() {
    Entity firstPostEntity = new Entity("Post");
    Entity secondPostEntity = new Entity("Post");
    Entity thirdPostEntity = new Entity("Post");
    List<Entity> posts = Arrays.asList(firstPostEntity, secondPostEntity, thirdPostEntity);

    List<Entity> expected = Arrays.asList();
    List<Entity> actual = postService.sortEntities("invalid", posts);

    assertEquals(expected, actual);
  }
}
