<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>

<!DOCTYPE html>
<html>

<head>
  <meta charset="UTF-8">
  <title>Admin Page</title>
  <link href="../style.css" rel="stylesheet" type="text/css">
</head>

<body>
  <script src="../script.js"></script>
  <h1>Delete All Comments</h1>
  <table class="table-style" id="comments-table" data-all-selected="false">
    <tbody>
      <tr>
        <th class="table-header-style">
          <input name="select-all" onclick="selectAll()" type="checkbox"/>
        </th>
        <th class="table-header-style">Name</th>
        <th class="table-header-style">Email</th>
        <th class="table-header-style">Sentiment</th>
        <th class="table-header-style">Comment</th>
        <th class="table-header-style">Timestamp</th>
        <th class="table-header-style">Key</th>
      </tr>
      <c:forEach items="${commentData}" var="comment" varStatus="loop">
        <tr>
          <td class="table-text">
            <input type="checkbox"/>
          </td>
          <td class="table-text">${comment.getProperty("name")}</td>
          <td class="table-text">${comment.getProperty("email")}</td>
          <td class="table-text">${comment.getProperty("sentimentScore")}</td>
          <td class="table-text">${comment.getProperty("comment")}</td>
          <td class="table-text">${comment.getProperty("timestamp")}</td>
          <td class="table-text">${KeyFactory.keyToString(comment.getKey())}</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
  <input name="delete" onclick="submitButton()" type="submit" value="Delete Selected Comments"/>
</body>

</html>
