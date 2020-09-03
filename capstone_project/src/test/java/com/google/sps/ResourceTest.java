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
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.Resource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Class that tests the ability to add Resource Entities to Datastore */
@RunWith(JUnit4.class)
public final class ResourceTest {
  private static DatastoreService datastore;
  private String PREEXISTING_CATEGORY = "Depression";
  // number of the preexisting resources in addPreexistingResources()
  private final int NUM_PREEXISTING = 4;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void checkAddPreexistingResourcesToEmptyDatastore() {
    // First check that there are no Resources in Datastore
    int expected = 0;
    assertTrue(equalsDatastoreCount(expected));

    // call addPreexistingResources(), which adds 4 hardcoded Resources to Datastore
    Resource.addPreexistingResources();

    expected = NUM_PREEXISTING;
    assertTrue(equalsDatastoreCount(expected));
  }

  @Test
  public void checkAddPreexistingResourcesNoDuplicates() {
    // First add a Resource Entity to Datastore that shares the same
    // category as a preexisting Resource found in Resource.java, then call
    // addPreexistingResources(). If the duplicate is added, the count would be greater
    // than the number of preexisting Resources
    Resource.storeResource(PREEXISTING_CATEGORY, "");
    assertTrue(equalsDatastoreCount(1));

    Resource.addPreexistingResources();

    int expected = NUM_PREEXISTING;
    assertTrue(equalsDatastoreCount(expected));

    // Check that there is only one Resource Entity with the category name in Datastore
    expected = 1;
    Filter categoryFilter =
        new FilterPredicate("category", FilterOperator.EQUAL, PREEXISTING_CATEGORY);
    Query query = new Query("Resource").setFilter(categoryFilter);
    PreparedQuery results = datastore.prepare(query);
    int actual = datastore.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
    assertEquals(expected, actual);
  }

  private Boolean equalsDatastoreCount(int expected) {
    return expected
        == datastore
            .prepare(new Query("Resource"))
            .countEntities(FetchOptions.Builder.withDefaults());
  }
}