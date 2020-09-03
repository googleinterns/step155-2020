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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.sps.data.Post;
import com.google.sps.data.PostFilter;
import com.google.sps.data.PostService;
import com.google.sps.servlets.FilterPostsServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/**
 * Tests the PostFilter methods. It tests to ensure that posts are retrieved in the expected
 * priority order. In this class, the text "Test University" is set on three Post entities, all in
 * different locations. The posts should be retrieved in an order where if the query is found in the
 * school name, it is a top result. If it is found in a title of a post, it is second most top
 * result. Lastly, if it is in the text content of a post, it will be the least top result.
 */
@RunWith(JUnit4.class)
public final class PostFilterTest extends Mockito {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private LocalServiceTestHelper serviceHelper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(true);

  private final String SEARCH_QUERY = "Test University";
  private final String DEFAULT_STRING = "Some Text";
  private final String NON_EXISTANT_QUERY = "DNE";
  private Entity post1;
  private Entity post2;
  private Entity post3;

  @Before
  public void setUpServiceHelper() {
    serviceHelper.setUp();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    post1 = new Entity("Post");
    post2 = new Entity("Post");
    post3 = new Entity("Post");
    post1.setProperty("schoolName", DEFAULT_STRING);
    post1.setProperty("title", DEFAULT_STRING);
    post1.setProperty("upvotes", 0L);
    post1.setProperty("reactions", new EmbeddedEntity());
    post1.setProperty("text", new Text(SEARCH_QUERY));
    post2.setProperty("schoolName", DEFAULT_STRING);
    post2.setProperty("title", SEARCH_QUERY);
    post2.setProperty("upvotes", 0L);
    post2.setProperty("reactions", new EmbeddedEntity());
    post2.setProperty("text", new Text(DEFAULT_STRING));
    post3.setProperty("schoolName", SEARCH_QUERY);
    post3.setProperty("title", DEFAULT_STRING);
    post3.setProperty("upvotes", 0L);
    post3.setProperty("reactions", new EmbeddedEntity());
    post3.setProperty("text", new Text(DEFAULT_STRING));
    datastore.put(post1);
    datastore.put(post2);
    datastore.put(post3);
  }

  @After
  public void tearDownServiceHelper() {
    serviceHelper.tearDown();
  }

  @Test
  public void returnsAllPostsInPriorityOrderWithQuery() {
    List<Entity> expected = Arrays.asList(post3, post2, post1);
    List<Entity> actual = PostFilter.filterPosts(SEARCH_QUERY);
    assertEquals(expected, actual);
  }

  @Test
  public void returnsEmptyArrayOnNoMatchForQuery() {
    List<Entity> expected = Arrays.asList();
    List<Entity> actual = PostFilter.filterPosts(NON_EXISTANT_QUERY);
    assertEquals(expected, actual);
  }

  @Test
  public void writesJsonArrayToResponse() throws IOException {
    when(request.getParameter("search")).thenReturn(SEARCH_QUERY);
    doNothing().when(response).setContentType("application/json");
    PrintWriter printWriter = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(printWriter);
    new FilterPostsServlet().doGet(request, response);

    List<Entity> postEntities = Arrays.asList(post3, post2, post1);
    List<Post> expectedPostOrder =
        postEntities.stream().map(PostService::convertEntityToPost).collect(Collectors.toList());
    String expectedJson = new Gson().toJson(expectedPostOrder);
    verify(printWriter).println(expectedJson);
  }
}
