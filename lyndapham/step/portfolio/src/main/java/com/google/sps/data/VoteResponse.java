// Copyright 2020 Lynda Pham

package com.google.sps.servlets;

import java.util.ArrayList;
import java.util.List;

/** A response that includes votes and a message. */
public final class VoteResponse{

  private final List<Vote> voteList;
  private final String message;

  public VoteResponse(List<Vote> voteList) {
    this.voteList = new ArrayList<>();
    this.voteList.addAll(voteList);
    message = null;
  }

  public VoteResponse(List<Vote> voteList, String message) {
    this.voteList = new ArrayList<>();
    this.voteList.addAll(voteList);
    this.message = message;
  }
  
  public List getList() {
    return voteList;
  }

  public String getMessage() {
    return message;
  }
}
