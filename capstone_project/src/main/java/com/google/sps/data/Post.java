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

import com.google.appengine.api.datastore.Text;
import java.util.Map;

public class Post {
  private String text;
  private String fileType;
  private String title;
  private String schoolName;
  private String fileBlobKey;
  private Map<String, Object> reactions;
  private long upvotes;
  private long postId;

  /**
   * Builder class for posts. Creates a Post object with properties: text, fileType, title,
   * schoolName, fileBlobKey, reactions, upvotes, and post id.
   */
  public static class Builder {
    private String text;
    private String fileType;
    private String title;
    private String schoolName;
    private String fileBlobKey;
    private Map<String, Object> reactions;
    private long upvotes;
    private long postId;

    /** Sets the text property. Converts the given Text instance into a string. */
    public Builder setText(Text text) {
      this.text = text.getValue();
      return this;
    }

    /** Sets the fileType of the post. */
    public Builder setFileType(String fileType) {
      this.fileType = fileType;
      return this;
    }

    /** Sets the title of the post. */
    public Builder setTitle(String title) {
      this.title = title;
      return this;
    }

    /** Sets the school name of the post. */
    public Builder setSchoolName(String schoolName) {
      this.schoolName = schoolName;
      return this;
    }

    /** Sets the file blob key string of the post. */
    public Builder setFileBlobKey(String fileBlobKey) {
      this.fileBlobKey = fileBlobKey;
      return this;
    }

    /** Sets the reactions of the post in a map. */
    public Builder setReactions(Map<String, Object> reactions) {
      this.reactions = reactions;
      return this;
    }

    /** Sets the amount of upvotes a post has. */
    public Builder setUpvotes(long upvotes) {
      this.upvotes = upvotes;
      return this;
    }

    /** Sets the post id of the post. */
    public Builder setPostId(long postId) {
      this.postId = postId;
      return this;
    }

    /** Returns a Post instance with all the properties of this builder. */
    public Post build() {
      return new Post(this);
    }
  }

  /**
   * Private constructor that is called from the Builder class. Sets all properties of this Post
   * instance to the ones from the builder.
   */
  private Post(Builder builder) {
    this.text = builder.text;
    this.fileType = builder.fileType;
    this.title = builder.title;
    this.schoolName = builder.schoolName;
    this.fileBlobKey = builder.fileBlobKey;
    this.reactions = builder.reactions;
    this.upvotes = builder.upvotes;
    this.postId = builder.postId;
  }

  /** Returns a Builder object for the post. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Returns the text message of the post. */
  public String getText() {
    return this.text;
  }

  /** Returns the file type of the post. */
  public String getFileType() {
    return this.fileType;
  }

  /** Returns the title of the post. */
  public String getTitle() {
    return this.title;
  }

  /** Returns the school name of the post. */
  public String getSchoolName() {
    return this.schoolName;
  }

  /** Returns the file blob key string of the post. */
  public String getFileBlobKey() {
    return this.fileBlobKey;
  }

  /** Returns a Map of reactions of the post. */
  public Map<String, Object> getReactions() {
    return this.reactions;
  }

  /** Returns the amount of upvotes a post has. */
  public long getUpvotes() {
    return this.upvotes;
  }

  /** Returns the post id of the post. */
  public long getPostId() {
    return this.postId;
  }
}
