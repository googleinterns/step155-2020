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

/** Class containing Comment information. */
public final class CommentInformation {

  private final String name;
  private final String comment;
  private final long timestamp;
  private final double sentimentScore;

  public CommentInformation(String comment, String name, long timestamp,
                            double sentimentScore) {
    this.comment = comment;
    this.name = name;
    this.timestamp = timestamp;
    this.sentimentScore = sentimentScore;
  }

  public String getName() { return name; }

  public String getComment() { return comment; }

  public long getTimestamp() { return timestamp; }

  public double getSentimentScore() { return sentimentScore; }
}
