<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
      </tr>
      <c:forEach items="${commentData}" var="comment" varStatus="loop">
        <tr>
          <td class="table-text">
            <input type="checkbox"/>
          </td>
          <td class="table-text">${comment.name}</td>
          <td class="table-text">${comment.email}</td>
          <td class="table-text">${comment.sentimentScore}</td>
          <td class="table-text">${comment.comment}</td>
          <td class="table-text">${comment.timestamp}</td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
  <input name="delete" onclick="submitButton()" type="submit" value="Delete Selected Comments"/>
</body>

</html>
