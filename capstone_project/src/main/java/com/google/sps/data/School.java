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

import java.util.ArrayList;

/** Class representing a school that corresponds to a pin on the map. */
public class School {

  private final String name;
  private final double latitude;
  private final double longitude;
  private ArrayList<String> articles;

  public School(String name, double latitude, double longitude, ArrayList<String> articles) {
    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
    this.articles = articles;
  }

  public String getName() {
    return name;
  }

  public double getLatitude() {
    return latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public ArrayList<String> getArticles() {
    return articles;
  }

  // Overriding equals() to compare two School objects
  @Override
  public boolean equals(Object o) {

    if (o == this) {
      return true;
    }

    if (!(o instanceof School)) {
      return false;
    }

    // typecast o to School so that data members can be compared
    School s = (School) o;

    // Compare the data members and return the result
    return ((s.getName()).equals(this.name))
        && (Double.compare(s.getLatitude(), this.latitude) == 0)
        && (Double.compare(s.getLongitude(), this.longitude) == 0);
  }
}
