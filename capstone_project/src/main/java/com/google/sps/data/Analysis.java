// Copyright 2020 Lynda Pham

package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;

/** A vote for a picture. */
public final class Analysis {

  private final String submission;
  private final String response;
  private final List<String> keyWords;
  private final double sentimentScore;

  public Analysis(String submission, String response, List<String> keyWords) {
    this.submission = submission;
    this.response = response;
    this.sentimentScore = (double) 0.0;
    this.keyWords = new ArrayList<>();
    this.keyWords.addAll(keyWords);
  }

}
