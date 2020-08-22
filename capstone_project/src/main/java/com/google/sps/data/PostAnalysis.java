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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.PreparedQuery;
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
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;


/**
 * Analyzes user posts to determine category classifications, which are used to provide
 * users with resources in response to the specific categories
 */
public class PostAnalysis {
  private DatastoreService datastore;
  private final String message;
  private final List <String> categories;
  private final List <String> resources;
 
  public PostAnalysis (HttpServletRequest request) throws IOException {
    datastore = DatastoreServiceFactory.getDatastoreService();
    categories = new ArrayList<>();
    resources = new ArrayList<>();
    message = classifyContent(request);
  }

  /** Returns categories that the message falls under
   *
   * @return list of categories
   */
  public List<String> getCategories() {
    return Collections.unmodifiableList(categories);
  }

  /** Returns the resources suggested for the user, based on the message's categories
   *
   * @return resources suggested
   */
  public List<String> getResources() {
    return Collections.unmodifiableList(resources);
  }

  /** Classifies what categories the messages falls under by using the pre-trained model 
   *  supplied by the Cloud Natural Language API
   *
   * @param request the HttpServletRequest containing user input
   * @return the user's message
   */
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

  /** Verifies that the user's message is at least 20 tokens (words) long */
  private String checkMessageLength(String message) {
    String messageCopy = message;
    while (messageCopy.length() < 20) {
      messageCopy += " " + message;
    }
    return messageCopy;
  }

  /** Performs string manipulation to retrieve the most specific categories available that the
   *  message falls under, and stores them in the "categories" field
   *
   *  ClassificationCategory contains:
   *      a string "name" the name of the category which is from a predefined taxonomy 
   *                      provided by the Natural Language API
   *      a number "confidence" the confidence score that represents how certain the classifier
   *                            is that the category represents the text
   *
   *
   *      an example of a "name":
   *          /Arts & Entertainment/Comics & Animation/Anime & Manga
   *          this method seeks to return the most specific (rightmost) category
   */
  private void setCategories(List<ClassificationCategory> classCategories) {
    for (ClassificationCategory cat : classCategories) {
      String name = cat.getName();
      int idx = name.lastIndexOf('/');
      categories.add(name.substring(idx + 1));
    }
  }

  /**
   * Retrieves resources from datastore that are associated with the categories that the message
   * falls under, and stores them in the "resources" field 
   */
  public void setResources() {
    Filter categoryFilter;
    Query query;
    for (String category : categories) {
        // find Resource entity that is associated with the specific category
        categoryFilter = new FilterPredicate("category", FilterOperator.EQUAL, category);
        query = new Query("Resource").setFilter(categoryFilter);
        Entity resource = datastore.prepare(query).asSingleEntity();

        // add to resources for the message
        String resourceUrl = (String) resource.getProperty("resource");
        resources.add(resourceUrl);
    }
  }
}
