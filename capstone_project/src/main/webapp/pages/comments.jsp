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
<%! BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
   String uploadUrl = blobstoreService.createUploadUrl("/post-process"); %>

<!DOCTYPE html>
<script src="../scripts/comments.js"></script>
<link rel="stylesheet" href="../styles/posts.css">
<body onload="loadPosts()">
  <div id="user-posts"></div>
  <hr>
  <form method="POST" enctype="multipart/form-data" action="<%= uploadUrl %>">
    <textarea required id="post-entry" name="text"></textarea>
    <br>
    <img alt=""
         id="image-preview"
         src="//:0">
    <br>
    <input accept="image/*"
           id="image-upload"
           name="image"
           onchange="previewImage(event)"
           type="file">
    <br>
    <input type="submit" value="Post">
  </form>
</body>
