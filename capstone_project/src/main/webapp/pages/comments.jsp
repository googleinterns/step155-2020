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
<link rel="stylesheet" href="../styles/posts.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
<script src="../scripts/comments.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>

<body onload="loadPosts(); loadSchools()">
  <div id="sort-types">
    <button onclick="sortPosts('new')">New</button>
    <button onclick="sortPosts('top')">Top</button>
    <button onclick="sortPosts('trending')">Trending</button>
  </div>
  <div id="user-posts" data-sort="default"></div>
  <hr>
  <form action="<%= blobstoreService.createUploadUrl("/post-process?file-type=none") %>"
        enctype="multipart/form-data"
        id="post-input"
        method="POST"
        onsubmit="disableInput(event)">
    <span>Title:</span>
    <input name="title" type="text" maxlength="64" required>
    <br>
    <label for="schools">Select a School: </label>
    <!-- NEED TO REMOVE THE COMMENTS HERE -->
    <%--<select name="schools" id="schools" required>
      <option value="">---</option>
    </select> --%>
    <p><a href="maps.html">Don't See Your School?</a></p>
    <textarea required id="post-entry" name="text"></textarea>
    <br>
    <img alt="" id="image-preview" src="//:0">
    <br>
    <input accept="image/*,video/*"
           id="image-upload"
           name="file"
           onchange="previewImage(event)"
           type="file">
    <br>
    <input type="submit" value="Post">
  </form>
  <hr>
  <c:if test="${not empty resources}">
    <script>
      alert("${resources}");
      loadResources(`${resources}`);
      // ; loadResources(<%= request.getAttribute("resources") %>)
    </script>
  </c:if> 
  
  <div class="container">
    

    <!-- Modal -->
    <div class="modal fade" id="myModal" role="dialog">
      <div class="modal-dialog">
      
        <!-- Modal content-->
        <div class="modal-content">
          <div class="modal-header">
            <button type="button" class="close" data-dismiss="modal">&times;</button>
            <h4 class="modal-title">Here are some resources that might help you!</h4>
          </div>
          <div class="modal-body"></div>
          <div class="modal-footer">
            <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
          </div>
        </div>
        
      </div>
    </div>  
  </div>
</body>
