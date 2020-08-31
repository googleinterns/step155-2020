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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.sps.servlets.GetPostServlet;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class GetPostTest extends Mockito {
  private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private HttpServletRequest request;
  private HttpServletResponse response;
  private LocalServiceTestHelper serviceHelper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsLoggedIn(true);
  private GetPostServlet getPostServlet;

  @Before
  public void setUpServiceHelper() {
    serviceHelper.setUp();
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
    getPostServlet = new GetPostServlet();
  }

  @After
  public void tearDownServiceHelper() {
    serviceHelper.tearDown();
  }

  @Test
  public void respondWith404OnPostNotFound() throws IOException {
    when(request.getParameter("post-id")).thenReturn("1");
    getPostServlet.doGet(request, response);
    verify(response).setStatus(404);
  }

  @Test
  public void verifyWritesJsonToResponse() throws IOException {
    Entity post = new Entity("Post");
    post.setProperty("upvotes", 0L);
    post.setProperty("text", new Text("Default String"));
    post.setProperty("reactions", new EmbeddedEntity());
    datastore.put(post);

    PrintWriter printWriter = mock(PrintWriter.class);
    when(request.getParameter("post-id")).thenReturn(Long.toString(post.getKey().getId()));
    when(response.getWriter()).thenReturn(printWriter);
    doNothing().when(printWriter).println(any(String.class));

    getPostServlet.doGet(request, response);
    verify(printWriter).println(any(String.class));
  }

  @Test
  public void redirectOnNotLoggedIn() throws IOException {
    serviceHelper.setEnvIsLoggedIn(false);
    doNothing().when(response).sendRedirect(any(String.class));

    getPostServlet.doGet(request, response);
    verify(response).sendRedirect(any(String.class));
  }
}
