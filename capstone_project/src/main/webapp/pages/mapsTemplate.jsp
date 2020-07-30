<%! String mapsAPIKey = System.getenv("CAPSTONE_API_KEY"); %>

<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>Maps Feature</title>
    <link rel="stylesheet" href="../styles/maps_style.css">
    <link href="https://fonts.googleapis.com/css?family=Quicksand:100,200,300" rel="stylesheet">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/css/bootstrap.min.css">
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.4.1/js/bootstrap.min.js"></script>
    <script src="https://maps.googleapis.com/maps/api/js?key=<%= mapsAPIKey %>"></script>
    <script src=" ../scripts/maps_script.js"></script>
  </head>
  <body onload="createMap();">
    <div id="content">
      <h1>Maps Feature</h1>
      <p>This will be the maps feature of our capstone project.</p>
      <button onclick="showUCINews()">Generate UC Irvine News</button>
      <ul id="uci-news"></ul>
      <br>
      <button onclick="showUWNews()">Generate UW News</button>
      <ul id="uw-news"></ul>
      <br>
      <button onclick="showUWBNews()">Generate UW Bothell News</button>
      <ul id="uwb-news"></ul>
      <br>
      <div id="map"></div>
      <br>
      <p>Click <a href="../index.html">here</a> to navigate back to the homepage.</p>
    </div>
  </body>
</html>
