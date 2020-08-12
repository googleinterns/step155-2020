// Copyright 2019 Google LLC
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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.data.School;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles loading school data for map markers. */
@WebServlet("/school-data")
public class MapServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    Set<School> schoolsSet = new HashSet<School>();

    // Load Schools from Datastore.
    Query query = new Query("School");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    for (Entity entity : results.asIterable()) {
      String schoolName = (String) entity.getProperty("name");
      double schoolLatitude = (double) entity.getProperty("latitude");
      double schoolLongitude = (double) entity.getProperty("longitude");
      School retrievedSchool = new School(schoolName, schoolLatitude, schoolLongitude);
      schoolsSet.add(retrievedSchool);
    }

    // Send the School objects retrieved from Datastore.
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(schoolsSet));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

    // Initialize datastore object.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    // Get the input from the school entry form.
    String schoolName = getParameter(request, "name-input");
    double schoolLatitude = Double.parseDouble(getParameter(request, "latitude-input"));
    double schoolLongitude = Double.parseDouble(getParameter(request, "longitude-input"));

    // Check if the School is already in Datastore.
    Filter nameFilter = new FilterPredicate("name", FilterOperator.EQUAL, schoolName);
    Query query = new Query("School").setFilter(nameFilter);
    PreparedQuery results = datastore.prepare(query);

    // If it is not, create a schoolEntity for the School and add it to Datastore.
    if (results.countEntities(FetchOptions.Builder.withDefaults()) == 0) {
      Entity schoolEntity = new Entity("School");
      schoolEntity.setProperty("name", schoolName);
      schoolEntity.setProperty("latitude", schoolLatitude);
      schoolEntity.setProperty("longitude", schoolLongitude);
      datastore.put(schoolEntity);
    }

    // Respond with the result.
    response.sendRedirect("pages/maps.html");
  }

  private String getParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    return value == null ? "" : value;
  }
}
