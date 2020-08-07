// Copyright 2020 Lynda Pham

package com.google.sps.servlets;

import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ClassifyTextRequest;
import com.google.cloud.language.v1.ClassifyTextResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.Sentiment;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/analysis")
public class AnalysisServlet extends HttpServlet {

  @Override
  public void init() {
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/html;");
    response.getWriter().println();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String submission = request.getParameter("text-input");

    // Document doc =
    //     Document.newBuilder().setContent(submission).setType(Document.Type.PLAIN_TEXT).build();
    // LanguageServiceClient languageService = LanguageServiceClient.create();
    // Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    // double score = (double) sentiment.getScore();
    // languageService.close();
    
    List<String> arr = new ArrayList<>();
    arr.add(classifyContent(submission));

    

    response.setContentType("text/html;");
    // response.getWriter().println("sentiment score:" + score);
    response.getWriter().println(arr);
    // Redirect to refreshed page
    // response.sendRedirect("/index.html");
  }

  private String classifyContent(String submission) throws IOException {
    String str = "";
    // Instantiate the Language client com.google.cloud.language.v1.LanguageServiceClient
    try (LanguageServiceClient language = LanguageServiceClient.create()) {
      // set content to the text string
      Document doc = Document.newBuilder().setContent(submission).setType(Type.PLAIN_TEXT).build();
      ClassifyTextRequest req = ClassifyTextRequest.newBuilder().setDocument(doc).build();
      // detect categories in the given text
      ClassifyTextResponse resp = language.classifyText(req);

      for (ClassificationCategory category : resp.getCategoriesList()) {
        String template = "Category name : %s, Confidence : %.3f\n";
        String result = String.format(template, category.getName(), category.getConfidence());
        str = str.concat(result);
      }
    }
    return str;
  }

  private String analyzeEntity(String submission) {
      // try (LanguageServiceClient language = LanguageServiceClient.create()) {
      //   Document doc = Document.newBuilder().setContent(submission).setType(Type.PLAIN_TEXT).build();
      //   AnalyzeEntitiesRequest request =
      //     AnalyzeEntitiesRequest.newBuilder()
      //       .setDocument(doc)
      //       .setEncodingType(EncodingType.UTF16)
      //       .build();

      //   AnalyzeEntitiesResponse response = language.analyzeEntities(request);

      //   // Print the response
      //   for (Entity entity : response.getEntitiesList()) {
      //       System.out.printf("Entity: %s", entity.getName());
      //       System.out.printf("Salience: %.3f\n", entity.getSalience());
      //       System.out.println("Metadata: ");
      //       for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
      //       System.out.printf("%s : %s", entry.getKey(), entry.getValue());
      //       }
      //       for (EntityMention mention : entity.getMentionsList()) {
      //       System.out.printf("Begin offset: %d\n", mention.getText().getBeginOffset());
      //       System.out.printf("Content: %s\n", mention.getText().getContent());
      //       System.out.printf("Type: %s\n\n", mention.getType());
      //       }
      //   }
      // }
      return "";
  }
}
