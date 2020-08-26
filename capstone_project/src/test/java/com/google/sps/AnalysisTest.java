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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import com.google.sps.data.PostAnalysis;
import com.google.sps.data.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Class that tests the analyzeText, analyzeCon of PostAnalysis */
@RunWith(JUnit4.class)
public final class AnalysisTest extends Mockito {

  private DatastoreService datastore;
  private HttpServletRequest request;
  private Resource resource;
  private PostAnalysis postAnalysis;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private static final String SHORT_MESSAGE = "This is a short test";
  private static final String LONG_MESSAGE =
      "This is a long message, which has over twenty tokens, since that is the minimum number of tokens for classification";
  private static final String DEPRESSION_MESSAGE =
      "Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression Depression";
  private static final double SENTIMENT_LOWERBOUND = -1.0;
  private static final double SENTIMENT_UPPERBOUND = 1.0;

  @Before
  public void setUp() {
    helper.setUp();
    request = mock(HttpServletRequest.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void runClassifyContentOnTwentyPlusTokens() throws IOException {
    when(request.getParameter("text")).thenReturn(LONG_MESSAGE);

    PostAnalysis postAnalysis = new PostAnalysis();
    LanguageServiceClient languageService = mock(LanguageServiceClient.class);
    postAnalysis.setLanguageServiceClient(languageService);
    ClassifyTextResponse classifyResp = mock(ClassifyTextResponse.class);
    ClassificationCategory classifyCat = mock(ClassificationCategory.class);

    when(languageService.classifyText(any(ClassifyTextRequest.class))).thenReturn(classifyResp);
    when(classifyResp.getCategoriesList()).thenReturn(Arrays.asList(classifyCat));
    when(classifyCat.getName()).thenReturn("/Arts & Entertainment/Comics & Animation/Comics");

    postAnalysis.analyzeText(request);

    List<String> expected = Arrays.asList("Comics");
    List<String> actual = postAnalysis.getCategories();

    assertEquals(expected, actual);
  }

  @Test
  public void runAnalyzeSentimentOnLessThanTwentyTokens() throws IOException {
    when(request.getParameter("text")).thenReturn(SHORT_MESSAGE);

    PostAnalysis postAnalysis = new PostAnalysis();
    LanguageServiceClient languageService = mock(LanguageServiceClient.class);
    postAnalysis.setLanguageServiceClient(languageService);

    AnalyzeSentimentResponse sentimentResponse = mock(AnalyzeSentimentResponse.class);
    Sentiment sentiment = mock(Sentiment.class);

    when(languageService.analyzeSentiment(any(Document.class))).thenReturn(sentimentResponse);
    when(sentimentResponse.getDocumentSentiment()).thenReturn(sentiment);
    when(sentiment.getScore()).thenReturn(0F);
    doNothing().when(languageService).close();

    postAnalysis.analyzeText(request);

    double expected = 0.0;
    double actual = postAnalysis.getSentimentScore();

    assertEquals(expected, actual, 0.01F);
  }
}
