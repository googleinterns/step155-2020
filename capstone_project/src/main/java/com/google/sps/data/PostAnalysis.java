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
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;

/**
 * Analyzes user posts to determine category classifications, which are used to provide users with
 * resources in response to the specific categories
 */
public class PostAnalysis {
  private final DatastoreService datastore;
  private final LanguageServiceClient languageService;
  private double sentimentScore;
  private final List<String> categories;
  private final List<String> resources;
  public final int MIN_TOKENS = 20;

  /**
   * Instantiates the LanguageServiceClient to reduce the number of calls to the API when run in the
   * server
   */
  public static class Builder {
    private LanguageServiceClient languageService;

    public static Builder builder() {
      return new Builder();
    }

    /** Sets LanguageServiceClient to the given argument */
    public Builder setLanguageService(LanguageServiceClient languageService) {
      this.languageService = languageService;
      return this;
    }

    /** Instantiates and sets LanguageServiceClient */
    public PostAnalysis build() throws IOException {
      if (this.languageService == null) {
        this.setLanguageService(LanguageServiceClient.create());
      }
      return new PostAnalysis(this);
    }
  }

  private PostAnalysis(Builder builder) throws IOException {
    datastore = DatastoreServiceFactory.getDatastoreService();
    languageService = builder.languageService;
    categories = new ArrayList<>();
    resources = new ArrayList<>();
  }

  /**
   * Returns sentiment score calculated for the message
   *
   * @return sentiment score
   */
  public double getSentimentScore() {
    return sentimentScore;
  }

  /**
   * Returns categories that the message falls under
   *
   * @return list of categories
   */
  public List<String> getCategories() {
    return Collections.unmodifiableList(categories);
  }

  /**
   * Returns the resources suggested for the user, based on the message's categories
   *
   * @return resources suggested
   */
  public List<String> getResources() {
    return Collections.unmodifiableList(resources);
  }

  /**
   * Analyzes the text to prodive either the sentiment score or the list of categories associated
   * with the text
   *
   * @param request the request from the webpage
   */
  public void analyzeText(HttpServletRequest request) {
    String message = request.getParameter("text");

    // messages must be at least 20 tokens long. StringTokenizer uses the default delimiter set
    StringTokenizer tokens = new StringTokenizer(message);
    if (tokens.countTokens() >= MIN_TOKENS) {
      classifyContent(message);
    } else {
      calculateSentimentScore(message);
    }
    setResources();
  }

  /**
   * Classifies what categories the messages falls under by using the pre-trained model supplied by
   * the Cloud Natural Language API
   *
   * @param message the user's message
   */
  private void classifyContent(String message) {
    List<ClassificationCategory> classCategories = new ArrayList<>();
    Document doc = Document.newBuilder().setContent(message).setType(Type.PLAIN_TEXT).build();
    ClassifyTextRequest classifyReq = ClassifyTextRequest.newBuilder().setDocument(doc).build();

    // detect categories in the given text
    ClassifyTextResponse classifyResp = languageService.classifyText(classifyReq);
    classCategories.addAll(classifyResp.getCategoriesList());

    setCategories(classCategories);
  }

  /** Inspects given text and identifies its emotional opinion (positive, negative, or neutral) */
  private void calculateSentimentScore(String message) {
    Document doc =
        Document.newBuilder().setContent(message).setType(Document.Type.PLAIN_TEXT).build();
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    sentimentScore = (double) sentiment.getScore();
    languageService.close();
  }

  /**
   * Performs string manipulation to retrieve the most specific categories available that the
   * message falls under, and stores them in the "categories" field
   *
   * <p>ClassificationCategory contains: a string "name" the name of the category which is from a
   * predefined taxonomy provided by the Natural Language API a number "confidence" the confidence
   * score that represents how certain the classifier is that the category represents the text
   *
   * <p>an example of a "name": /Arts & Entertainment/Comics & Animation/Anime & Manga this method
   * seeks to return the most specific (rightmost) category
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
   * falls under
   */
  private void setResources() {
    if (!categories.isEmpty()) {
      for (String category : categories) {
        queryDatastoreForEntity(category);
      }
    } else if (sentimentScore < -0.5) {
      queryDatastoreForEntity("General");
    }
  }

  /** Creates filter for querying the datastore and and stores resources in the "resources" field */
  private void queryDatastoreForEntity(String category) {
    Filter categoryFilter = new FilterPredicate("category", FilterOperator.EQUAL, category);
    Query query = new Query("Resource").setFilter(categoryFilter);
    Entity resource = datastore.prepare(query).asSingleEntity();
    if (resource != null) {
      // add to resources for the message
      String resourceUrl = (String) resource.getProperty("resource");
      resources.add(resourceUrl);
    }
  }
}
