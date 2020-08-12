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
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
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
  private Datastore datastore = spy(DatastoreOptions.getDefaultInstance().getService());
  private DeletePostService deleteService;
  private Clock clock;
  private final long ONE_DAY = 86400000L;

  @Before
  public void setUpServiceHelper() {
    clock = mock(Clock.class);
    deleteService = spy(new DeletePostService());
    deleteService.setClock(clock);
    deleteService.setDatastore(datastore);
  }

  @Test
  public void postIsDeletedWhenOlderThan24Hours() {
    Key key = datastore.newKeyFactory().setKind("Post").newKey("someKey");
    Entity post1 = Entity.newBuilder(key).set("timestamp", 0L).build();

    when(clock.millis()).thenReturn(ONE_DAY + 1);
    List<Entity> posts = Arrays.asList(post1);
    doReturn(posts).when(deleteService).retrieveAllPosts();
    deleteService.deleteOldPosts();
    verify(datastore, times(1)).delete(any(Key.class));
  }

  @Test
  public void postsNotOlderThan24HoursNotDeleted() {
    Key key1 = datastore.newKeyFactory().setKind("Post").newKey("someKey1");
    Key key2 = datastore.newKeyFactory().setKind("Post").newKey("someKey2");
    Key key3 = datastore.newKeyFactory().setKind("Post").newKey("someKey3");
    Entity post1 = Entity.newBuilder(key1).set("timestamp", 0L).build();
    Entity post2 = Entity.newBuilder(key2).set("timestamp", 0L).build();
    Entity post3 = Entity.newBuilder(key3).set("timestamp", ONE_DAY).build();

    when(clock.millis()).thenReturn(ONE_DAY);
    List<Entity> posts = Arrays.asList(post1, post2, post3);
    doReturn(posts).when(deleteService).retrieveAllPosts();
    deleteService.deleteOldPosts();
    verify(datastore, times(2)).delete(any(Key.class));
  }
}
