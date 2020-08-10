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

import com.google.sps.data.School;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Class that tests the basic 'get' methods and equals operator of School objects. */
@RunWith(JUnit4.class)
public final class SchoolTest {

  private final double uciLatitude = 33.640339;
  private final double uciLongitude = -117.844248;

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
    boolean actual = (uci1.equals(uci2));
    boolean expected = true;
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void checkEqualsReturnsNotEqual() {
    School uci1 = new School("UCI", uciLatitude, uciLongitude);
    School uci2 = new School("UCI", uciLatitude + 1, uciLongitude);
    boolean actual = (uci1.equals(uci2));
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

    boolean actual = (schools.contains(uci1));
    boolean expected = true;
    Assert.assertEquals(expected, actual);
  }
}
