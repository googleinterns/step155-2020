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
import java.util.HashMap;
import java.util.Map;

/** Class representing a school that corresponds to a pin on the map. */
public class Resource {
  private final DatastoreService datastore;
  private final String category;
  private final String resource;
  // want to include String for state / university as well

  public Resource() {
    datastore = DatastoreServiceFactory.getDatastoreService();
    this.category = null;
    this.resource = null;
  }

  // will be used
  public Resource(String category, String resource) {
    datastore = DatastoreServiceFactory.getDatastoreService();
    this.category = category;
    this.resource = resource;
  }

  public String getCategory() {
    return category;
  }

  public String getResource() {
    return resource;
  }

  public void storeResource() {
    Entity resourceEntity = new Entity("Resource");
    resourceEntity.setProperty("category", category);
    resourceEntity.setProperty("resource", resource);
    datastore.put(resourceEntity);
  }

  public void addPreexistingResources() {
    Map<String, String> resourceMap = new HashMap<>();
    resourceMap.put("Depression", "https://www.crisistextline.org/");
    resourceMap.put("Troubled Relationships", "https://www.loveisrespect.org/");
    resourceMap.put("Anxiety & Stress", "https://www.nami.org/help");

    for (String category : resourceMap.keySet()) {
      Entity resourceEntity = new Entity("Resource");
      resourceEntity.setProperty("category", category);
      resourceEntity.setProperty("resource", resourceMap.get(category));
      datastore.put(resourceEntity);
    }
  }
}
