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
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class PostAnalysis {
  private DatastoreService datastore;
  private final String message;
  private final List<String> categories;
  private final List<String> resources;

  public PostAnalysis(HttpServletRequest request) throws IOException {
    datastore = DatastoreServiceFactory.getDatastoreService();
    categories = new ArrayList<>();
    resources = new ArrayList<>();
    message = classifyContent(request);
  }

  public List<String> getCategories() {
    return Collections.unmodifiableList(categories);
  }

  public List<String> getResources() {
    return Collections.unmodifiableList(resources);
  }

  private String classifyContent(HttpServletRequest request) throws IOException {
    String message = request.getParameter("text");

    // messages must be at least 20 tokens long
    message = checkMessageLength(message);

    List<ClassificationCategory> classCategories = new ArrayList<>();
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // set content to the text string
      Document doc = Document.newBuilder().setContent(message).setType(Type.PLAIN_TEXT).build();
      ClassifyTextRequest classifyReq = ClassifyTextRequest.newBuilder().setDocument(doc).build();

      // detect categories in the given text
      ClassifyTextResponse classifyResp = language.classifyText(classifyReq);
      classCategories.addAll(classifyResp.getCategoriesList());

      setCategories(classCategories);
    }
    return message;
  }

  private String checkMessageLength(String message) {
    String messageCopy = message;
    while (messageCopy.length() < 20) {
      messageCopy += " " + message;
    }
    return messageCopy;
  }

  private void setCategories(List<ClassificationCategory> classCategories) {
    for (ClassificationCategory cat : classCategories) {
      String name = cat.getName();
      int idx = name.lastIndexOf('/');
      categories.add(name.substring(idx + 1));
    }
  }

  public void searchResourcesFromDatastore() {
    Filter categoryFilter;
    Query query;
    for (String category : categories) {
      categoryFilter = new FilterPredicate("category", FilterOperator.EQUAL, category);
      query = new Query("Resource").setFilter(categoryFilter);
      Entity resource = datastore.prepare(query).asSingleEntity();
      resources.add((String) resource.getProperty("resource"));
    }
  }
}
