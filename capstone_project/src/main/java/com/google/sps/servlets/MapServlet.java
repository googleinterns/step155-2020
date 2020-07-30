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

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.sps.data.School;

/** Servlet that handles loading school data for map markers. */
@WebServlet("/school-data")
public class MapServlet extends HttpServlet {

  private ArrayList<School> schools = new ArrayList<School>();

  private void addUCIToSchools() {
    ArrayList<String> uciNews = new ArrayList<String>( Arrays.asList(
        "https://news.uci.edu/2020/06/30/virtual-nurses-make-a-real-difference/",
        "https://news.uci.edu/2020/07/07/uci-chancellor-emeritus-michael-v-drake-named-university-of-california-president/",
        "https://news.uci.edu/2020/06/18/uci-podcast-jessica-millward-on-the-meaning-and-importance-of-juneteenth/") );
    School uci = new School("UCI", 33.640339, -117.844248, uciNews);
    schools.add(uci);
  }

  private void addUWToSchools() {
    ArrayList<String> uwNews = new ArrayList<String>( Arrays.asList(
        "https://www.washington.edu/coronavirus/2020/07/10/back-to-school-town-hall/",
        "https://www.washington.edu/news/2020/07/16/wsas-2020/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS",
        "https://www.washington.edu/news/2020/07/29/expert-faq-wildfires-in-the-pacific-northwest-during-the-covid-19-pandemic/") );
    School uw = new School("UW", 47.655277, -122.303606, uwNews);
    schools.add(uw);
  }

  private void addUWBToSchools() {
    ArrayList<String> uwbNews = new ArrayList<String>( Arrays.asList(
        "https://news.uci.edu/2020/06/30/virtual-nurses-make-a-real-difference/",
        "https://news.uci.edu/2020/07/07/uci-chancellor-emeritus-michael-v-drake-named-university-of-california-president/",
        "https://news.uci.edu/2020/06/18/uci-podcast-jessica-millward-on-the-meaning-and-importance-of-juneteenth/") );
    School uwb = new School("UWB", 47.758821, -122.190725, uwbNews);
    schools.add(uwb);
  }

  /*Add some hard-coded School objects to the array of schools that will later be converted into markers on the map.*/
  public MapServlet() {
    addUCIToSchools();
    addUWToSchools();
    addUWBToSchools();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    Gson gson = new Gson();
    response.getWriter().println(gson.toJson(schools));
  }

}
