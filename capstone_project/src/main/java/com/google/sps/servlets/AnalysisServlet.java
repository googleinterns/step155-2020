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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/analysis")
public class AnalysisServlet extends HttpServlet {
  final int MIN_LENGTH = 20;
  private Map<String, String> resources = new HashMap<>();

  @Override
  public void init() {
      resources.put("Anxiety & Stress", "https://www.inspire.com/groups/mental-health-america/topic/anxiety-and-phobias/?origin=tfr");
      resources.put("Depression", "https://www.7cups.com/");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    response.setContentType("text/html;");
    response.getWriter().println();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    String init = request.getParameter("text-input");
    String submission = init;
    int count = 1;
    // workaround to ensure string length won't prevent classification
    while (init.length() * count < MIN_LENGTH) {
        submission.concat(init);
        count++;
    }
    
    List<String> arr = new ArrayList<>();
    Map<String, String> help = new HashMap<>();
    arr.add(classifyContent(submission));

    for (String str : arr) {
      int idx = str.lastIndexOf('/');
      String category = str.substring(idx + 1);
      if (resources.containsKey(category)) {
        help.put(resources.get(category), category);
      }
    }

    response.setContentType("text/html;");
    // response.getWriter().println("sentiment score:" + score);

    PrintWriter out = response.getWriter();
    // then write the response
    out.println("<html><body>");
    out.println("<p>Click <a href=\"../pages/maps.html\">here</a> to explore our maps feature.</p>" +
        "<p>Click <a href=\"../pages/analyze.html\">here</a> to check out our sentiment analysis feature.</p>" +
        "<p>Click <a href=\"../pages/comments.html\">here</a> to upload a post and view post submissions.</p>" +
        "<p>Click <a href=\"../../index.html\">here</a> to navigate back to the homepage.</p>");
    out.println("<h3>We have received your submission.</h3>");

    if (!help.isEmpty()) {
      out.println("<h4>We detected the following themes in your post. Try the link for resources that may help.</h4>");
      String res;
      for (String key : help.keySet()) {
        res = "<a href=\"" + key + "\">"+ help.get(key) + "</a>";
        out.println(res);
      }
    }

    out.println("</body></html>");
    out.close();
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
        str = str.concat(category.getName());
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
