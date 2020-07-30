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

package com.google.sps.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Class that tests the basic 'get' methods of School objects. */
@RunWith(JUnit4.class)
public final class SchoolTest {

  private final ArrayList<String> uciNews = new ArrayList<String>( Arrays.asList(
        "https://news.uci.edu/2020/06/30/virtual-nurses-make-a-real-difference/",
        "https://news.uci.edu/2020/07/07/uci-chancellor-emeritus-michael-v-drake-named-university-of-california-president/",
        "https://news.uci.edu/2020/06/18/uci-podcast-jessica-millward-on-the-meaning-and-importance-of-juneteenth/") );

  private final double uciLatitude = 33.640339;
  private final double uciLongitude = -117.844248;

  @Test
  public void getNameReturnsSchoolName() {
    School uci = new School("UCI", uciLatitude, uciLongitude, uciNews);

    String actual = uci.getName();
    String expected = "UCI";
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void getLatLongReturnsSchoolLatLong() {
    School uci = new School("UCI", uciLatitude, uciLongitude, uciNews);

    double actualLat = uci.getLatitude();
    double expectedLat = uciLatitude;
    double actualLong = uci.getLongitude();
    double expectedLong = uciLongitude;
    Assert.assertEquals(expectedLat, actualLat, 0);
    Assert.assertEquals(expectedLong, actualLong, 0);
  }

  @Test
  public void getArticlesReturnsSchoolArticles() {
    School uci = new School("UCI", uciLatitude, uciLongitude, uciNews);
    ArrayList<String> actual = uci.getArticles();
    ArrayList<String> expected = uciNews;
    Assert.assertEquals(expected, actual);
  }
}
