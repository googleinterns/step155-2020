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

/** Associates a category, from the Natural Language API taxonomy, to a link to a resource */
public class Resource {
  private DatastoreService datastore;
  private final String category;
  private final String resource;

  public Resource() {
    datastore = DatastoreServiceFactory.getDatastoreService();
    this.category = "";
    this.resource = "";
  }

  public Resource(String category, String resource) {
    datastore = DatastoreServiceFactory.getDatastoreService();
    this.category = category;
    this.resource = resource;
  }

  /** Returns category */
  public String getCategory() {
    return category;
  }

  /** Returns the resource url associated with the category */
  public String getResource() {
    return resource;
  }

  /** Creates an Entity with this.fields and stores it in Datastore */
  public void storeResource() {
    Entity resourceEntity = new Entity("Resource");
    resourceEntity.setProperty("category", category);
    resourceEntity.setProperty("resource", resource);
    datastore.put(resourceEntity);
  }

  /** Adds hard-coded Resources to Datastore */
  public void addPreexistingResources() {
    Map<String, String> resourceMap = new HashMap<>();
    resourceMap.put("Depression", "https://www.crisistextline.org/");
    resourceMap.put("Troubled Relationships", "https://www.loveisrespect.org/");
    resourceMap.put("Anxiety & Stress", "https://www.nami.org/help");
    resourceMap.put("General", "https://www.happify.com");

    for (String category : resourceMap.keySet()) {
      Entity resourceEntity = new Entity("Resource");
      resourceEntity.setProperty("category", category);
      resourceEntity.setProperty("resource", resourceMap.get(category));
      datastore.put(resourceEntity);
    }
  }
}
