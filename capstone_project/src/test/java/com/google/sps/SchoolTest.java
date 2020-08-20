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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.sps.data.School;
import com.google.sps.servlets.MapServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

/** Class that tests the basic 'get' methods and equals operator of School objects. */
@RunWith(JUnit4.class)
public final class SchoolTest extends Mockito {

  private final double uciLatitude = 33.640339;
  private final double uciLongitude = -117.844248;

  private DatastoreService datastore;
  private MapServlet mapServlet;
  private HttpServletRequest request;
  private HttpServletResponse response;

  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
    mapServlet = new MapServlet();
    request = Mockito.mock(HttpServletRequest.class);
    response = Mockito.mock(HttpServletResponse.class);
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void getNameReturnsSchoolName() {
    School uci = new School("UCI", uciLatitude, uciLongitude);

    String actual = uci.getName();
    String expected = "UCI";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getLatLongReturnsSchoolLatLong() {
    School uci = new School("UCI", uciLatitude, uciLongitude);

    double actualLat = uci.getLatitude();
    double expectedLat = uciLatitude;
    double actualLong = uci.getLongitude();
    double expectedLong = uciLongitude;
    Assert.assertEquals(expectedLat, actualLat, 0);
    Assert.assertEquals(expectedLong, actualLong, 0);
  }

  @Test
  public void checkEqualsReturnsEqual() {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude, uciLongitude);
    boolean actual = uci1.equals(uci2);
    boolean expected = true;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkEqualsReturnsNotEqual() {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude + 1, uciLongitude);
    boolean actual = uci1.equals(uci2);
    boolean expected = false;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkEqualsWorksForContains() {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude, uciLongitude);
    ArrayList<School> schools = new ArrayList<School>();
    schools.add(uci1);
    schools.add(uci2);

    boolean actual = schools.contains(uci1);
    boolean expected = true;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkEqualsWorksForSet() {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude, uciLongitude);
    School ucb = new School("UCB", 37.871942, -122.258476);
    School ucla = new School("UCLA", 34.068965, -118.445245);
    Set<School> schools = new HashSet<School>();
    schools.add(uci1);
    schools.add(uci2);
    schools.add(ucb);
    schools.add(ucla);
    schools.add(ucla);

    ArrayList<School> schoolsList = new ArrayList<School>(schools);
    int actual = schoolsList.size();
    int expected = 3;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testUploadingSchoolsWithNoDuplicatesInDatastore() throws IOException {
    School uci = new School("UCI", uciLatitude, uciLongitude);
    mapServlet.addToDatastore(uci);
    School ucb = new School("UCB", 37.871942, -122.258476);
    mapServlet.addToDatastore(ucb);

    // Make sure that UCB was added, since it was not already in Datastore.
    int actual =
        datastore.prepare(new Query("School")).countEntities(FetchOptions.Builder.withDefaults());
    int expected = 2;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void testUploadingSchoolsWithExistingDuplicatesInDatastore() throws IOException {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude, uciLongitude);
    mapServlet.addToDatastore(uci1);
    mapServlet.addToDatastore(uci2);

    // Make sure that UCI was not added again, as it was already in Datastore.
    int actual =
        datastore.prepare(new Query("School")).countEntities(FetchOptions.Builder.withDefaults());
    int expected = 1;
    Assert.assertEquals(expected, actual);
  }
}
