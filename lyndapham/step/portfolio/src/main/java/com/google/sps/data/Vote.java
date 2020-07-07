// Copyright 2020 Lynda Pham

package com.google.sps.servlets;

/** An item on a todo list. */
public final class Vote {

  private final String vote;
  private final String comment;

  public Vote(String vote, String comment) {
    this.vote = vote;
    this.comment = comment;
  }
}
