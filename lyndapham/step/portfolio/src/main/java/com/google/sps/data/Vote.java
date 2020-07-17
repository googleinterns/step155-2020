// Copyright 2020 Lynda Pham

package com.google.sps.servlets;

/** A vote for a picture. */
public final class Vote {

  private final String vote;
  private final String comment;
  private final double sentimentScore;

  public Vote(String vote, String comment) {
    this.vote = vote;
    this.comment = comment;
    sentimentScore = (double) 0.0;
  }

  public Vote(String vote, String comment, double sentimentScore) {
    this.vote = vote;
    this.comment = comment;
    this.sentimentScore = sentimentScore;
  }
}
