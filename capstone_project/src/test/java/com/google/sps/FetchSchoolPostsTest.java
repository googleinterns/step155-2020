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
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.gson.Gson;
import com.google.sps.data.PostService;
import com.google.sps.servlets.FetchSchoolPostsServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class FetchSchoolPostsTest extends Mockito {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private LocalServiceTestHelper serviceHelper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(true);
  private PostService postService;

  private final String school1 = "School1";
  private final String school2 = "School2";
  private final String school3 = "School3";
  private Entity post1;
  private Entity post2;
  private Entity post3;

  @Before
  public void setUpServiceHelper() {
    serviceHelper.setUp();
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    postService = spy(new PostService.Builder().build());
    post1 = new Entity("Post");
    post2 = new Entity("Post");
    post3 = new Entity("Post");
    post1.setProperty("schoolName", school1);
    post2.setProperty("schoolName", school2);
    post3.setProperty("schoolName", school3);
    datastore.put(post1);
    datastore.put(post2);
    datastore.put(post3);
  }

  @After
  public void tearDownServiceHelper() {
    serviceHelper.tearDown();
  }

  @Test
  public void returnsAllPostsWithSchoolName() {
    List<Entity> expected = Arrays.asList(post1);
    List<Entity> actual = postService.getPostsBySchool(school1);
    assertEquals(expected, actual);
  }

  @Test
  public void returnEmptyListOnNoPostsFound() {
    List<Entity> expected = Arrays.asList();
    List<Entity> actual = postService.getPostsBySchool("NotASchool");
    assertEquals(expected, actual);
  }

  @Test
  public void writesJsonToResponse() throws IOException {
    PrintWriter printWriter = mock(PrintWriter.class);
    when(request.getParameter("school-name")).thenReturn(school1);
    when(response.getWriter()).thenReturn(printWriter);

    Gson gson = new Gson();
    String expectedJson = gson.toJson(Arrays.asList(post1));

    new FetchSchoolPostsServlet().doGet(request, response);
    verify(printWriter).println(expectedJson);
  }
}
