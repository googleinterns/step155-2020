<%--
Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
    https://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ taglib prefix = "c" uri = "http://java.sun.com/jsp/jstl/core" %>
<%! BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService(); %>

<!DOCTYPE html>
<script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
<script src="../scripts/comments.js"></script>
<script src="../scripts/analysis.js"></script>
<script src="../templates/sidebar_template.js"></script>
<script src="../scripts/search_bar.js"></script>
<link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
<link rel="stylesheet" href="../styles/posts.css">
<link rel="stylesheet" href="../styles/sidebar.css">

<body onload="loadPosts(); loadSchools()">
  <div class="row">
    <div class="col s3" id="sidebar-column">
      <side-bar id="sidebar" class="valign-wrapper"></side-bar>
    </div>
    <div class="col s6">
      <div id="search-bar">
        <nav id="search-bar-nav">
          <div class="nav-wrapper">
            <div class="input-field" id="search-bar-bg">
              <input onkeypress="searchPosts()" id="post-search-bar" type="search" required>
              <label class="label-icon" for="post-search-bar">
                <i class="material-icons" id="search-icon">search</i>
              </label>
              <i class="material-icons">close</i>
            </div>
          </div>
        </nav>
      </div>
      <div class="center" id="sort-types">
        <button class="btn" onclick="sortPosts('new')">New</button>
        <button class="btn" onclick="sortPosts('top')">Top</button>
        <button class="btn" onclick="sortPosts('trending')">Trending</button>
      </div>
      <div id="user-posts" data-sort="default"></div>
    </div>
    <div class="col s3" id="post-upload-container">
      <form action="<%= blobstoreService.createUploadUrl("/post-process?file-type=none") %>"
            enctype="multipart/form-data"
            id="post-input"
            method="POST"
            onsubmit="disableInput(event)">
        <div class="input-field">
          <em class="material-icons prefix">title</em>
          <input id="post-title" name="title" type="text" maxlength="64" required>
          <label for="post-title">Title</label>
        </div>
        <div  class="input-field">
          <em class="material-icons prefix">school</em>
          <select name="schools" id="schools" required>
            <option value="" disabled selected>---</option>
          </select>
          <label for="schools">Select a School:</label>
        </div>
        <p><a href="maps.html">Don't See Your School?</a></p>
        <div class="input-field">
          <em class="material-icons prefix">mode_edit</em>
          <textarea required
                    class="materialize-textarea"
                    id="post-entry"
                    name="text"></textarea>
          <label for="post-entry">Text</label>
        </div>
        <img alt="Image Preview" id="image-preview" src="//:0">
        <div class="file-field input-field" id="file-container">
          <div class="btn">
            <span>File</span>
            <input accept="image/*,video/*" id="image-upload" name="file" onchange="previewImage(event)" type="file">
          </div>
          <div class="file-path-wrapper">
            <input class="file-path validate valid" type="text">
          </div>
        </div>
        <input type="submit" value="Post" class="btn">
      </form>
    </div>
  </div>
  <div class="container">
    <!-- Modal Structure -->
    <div id="modal1" class="modal">
      <div class="modal-content">
        <h4>Would any of these resources help you?</h4>
        <p id="modal-text"></p>
      </div>
      <div class="modal-footer">
        <a href="#!" class="modal-close waves-effect waves-green btn-flat">Thanks!</a>
      </div>
    </div>
  </div>
  <c:if test="${not empty resources}">
    <c:forEach items="${resources}" var="resource">
      <script>
        loadResources("${resource}");
      </script>
    </c:forEach>
  </c:if> 
</body>
