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

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.sps.data.DeletePostService;
import java.time.Clock;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public final class DeletePostTest extends Mockito {
  private Datastore datastore = mock(Datastore.class);
  private DeletePostService deleteService;
  private Clock clock;
  private final long ONE_DAY = 86400000L;

  @Before
  public void setUpServiceHelper() {
    clock = mock(Clock.class);
    deleteService = spy(new DeletePostService(datastore, clock));
  }

  @Test
  public void postIsDeletedWhenOlderThan24Hours() {
    Entity post1 = mock(Entity.class);

    when(post1.getLong("timestamp")).thenReturn(0L);
    when(clock.millis()).thenReturn(ONE_DAY + 1);

    List<Entity> posts = Arrays.asList(post1);
    doReturn(posts).when(deleteService).retrieveAllPosts();

    deleteService.deleteOldPosts();
    verify(datastore, times(1)).delete(any());
  }

  @Test
  public void postsNotOlderThan24HoursNotDeleted() {
    Entity post1 = mock(Entity.class);
    Entity post2 = mock(Entity.class);
    Entity post3 = mock(Entity.class);

    when(post1.getLong("timestamp")).thenReturn(0L);
    when(post2.getLong("timestamp")).thenReturn(0L);
    when(post3.getLong("timestamp")).thenReturn(ONE_DAY);
    when(clock.millis()).thenReturn(ONE_DAY);

    List<Entity> posts = Arrays.asList(post1, post2, post3);
    doReturn(posts).when(deleteService).retrieveAllPosts();

    deleteService.deleteOldPosts();
    verify(datastore, times(2)).delete(any());
  }
}
