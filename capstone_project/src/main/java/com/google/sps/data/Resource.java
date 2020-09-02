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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import java.util.HashMap;
import java.util.Map;

/** Associates a category, from the Natural Language API taxonomy, to a link to a resource */
public class Resource {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  /** Creates an Entity with parameters and stores it in Datastore */
  public static void storeResource(String category, String resource) {
    Entity resourceEntity = new Entity("Resource");
    resourceEntity.setProperty("category", category);
    resourceEntity.setProperty("resource", resource);
    datastore.put(resourceEntity);
  }

  /** Adds hard-coded Resources to Datastore */
  public static void addPreexistingResources() {
    Map<String, String> resourceMap = new HashMap<>();
    resourceMap.put("Depression", "Crisis Textline");
    resourceMap.put("Troubled Relationships", "Love is Respect");
    resourceMap.put("Anxiety & Stress", "National Alliance on Mnetal Illness");
    resourceMap.put("General", "Happify");

    for (String category : resourceMap.keySet()) {
      Filter categoryFilter = new FilterPredicate("category", FilterOperator.EQUAL, category);
      Query query = new Query("Resource").setFilter(categoryFilter);
      PreparedQuery results = datastore.prepare(query);
      if (results.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
        Entity resourceEntity = new Entity("Resource");
        resourceEntity.setProperty("category", category);
        resourceEntity.setProperty("resource", resourceMap.get(category));
        datastore.put(resourceEntity);
      }
    }
  }
}
