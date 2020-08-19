// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the 'License');
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an 'AS IS' BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/** Creates a map centered at the geographic center of the US and
 * adds it to the page.
 */
function createMap() { // eslint-disable-line no-unused-vars
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 39.828502, lng: -98.579512}, zoom: 4});

  // Create a marker for each school and load these onto the map.
  loadMarkersOntoMap(map);

  // Create the autocomplete Places search.
  loadAutocomplete(map);
}

/** Creates and displays an autocompleted Places search engine on the page.
 * @param {Object} map - A google maps Map object.
 */
function loadAutocomplete(map) {
  // Set up the autocomplete search widget.
  const input = document.getElementById('school-search');
  const autocomplete = new google.maps.places.Autocomplete(input);
  autocomplete.bindTo('bounds', map);
  autocomplete.setFields(
      ['address_components', 'geometry', 'icon', 'name']);

  // Grab the place submission from autocomplete and post it only when the
  // 'submit' button is clicked.
  document.getElementById('school-submit')
      .addEventListener('click', function() {
        const place = autocomplete.getPlace();
        if (!place.geometry) {
          // The user entered the name of a Place that was not suggested,
          // or the request for Place Details failed.
          window.alert(`No details were found for submission: '${place.name}'`);
          return;
        }
        const submission = {
          name: place.name,
          latitude: place.geometry.location.lat(),
          longitude: place.geometry.location.lng(),
        };
        postSchool(submission);
      });
}

/** Sends the school submission to a MapServlet.
 * @param {Object} submission - An object representing a school submission.
 */
function postSchool(submission) {
  fetch('/school-data', {
    method: 'POST',
    body: JSON.stringify(submission),
    headers: {
      'Accept': 'application/json',
    },
  }).then((response) => location.reload());
}

/** Fetches and webscrapes articles for each school,
 * and creates/places each school's marker on the map.
 * @param {Object} map - A google maps Map object.
 */
function loadMarkersOntoMap(map) {
  fetch('/school-data').then((response) => response.json()).then((schools) => {
    for (const school of schools) {
      const url = `https://www.googleapis.com/customsearch/v1?key=CAPSTONE_API_KEY&cx=CAPSTONE_SEARCH_ENG_ID&q=${school.name}`;
      $.getJSON(url, function(result) {
        createMarker(map, school.latitude, school.longitude,
            school.name, result);
      });
    }
  });
}

/** Creates and returns the "News" tab portion of a content string for a marker.
 * @param {Array.<string>} articles - A list of search results about a school.
 * @return {string} - A newsFeed string containing the HTML code for
 * the "News" tab of an infowindow.
 */
function createNewsFeed(articles) {
  let newsFeed = '<ul>';

  for (const article of articles) {
    newsFeed += `<li><a href="${article.link}">${article.htmlTitle}</a></li>`;
  }

  newsFeed += '</ul>';
  return newsFeed;
}

/** Creates and returns the content string for a marker.
 * @param {string} name - The name of a school.
 * @param {Array.<Object>} items - A list of search results about that school.
 * @return {string} - A contentString containing the
 * HTML code for an infowindow.
 */
function createContentString(name, items) {
  const contentString =
    `<div id="content">
        <div id="siteNotice">
        </div>
        <h1 id="firstHeading" class="firstHeading">${name}</h1>
        <div id="bodyContent">
            <p>Stay up-to-date on all things <b><u>${name}</u></b>:</p>`+

            // Create pills for tab navigation.
            `<ul class="nav nav-pills">
             <li class="active"><a data-toggle="pill" href="#news">News</a></li>
             <li><a data-toggle="pill" href="#posts">Posts</a></li>
            </ul>`+
            // Create content stored in tabs for each pill.
            `<div class="tab-content">
                <div id="news" class="tab-pane fade in active">
                    <h5><b>Here's the latest news on ${name}:</b></h5>

                    ${createNewsFeed(items)}

                </div>
                <div id="posts" class="tab-pane fade">
                    <h4>This is where the posts for ${name} will go.</h4>
                </div>
            </div>
        </div>
     </div>`;

  return contentString;
}


/** Creates a marker and its infowindow and adds these to the map.
 * @param {Object} map - A google maps Map object.
 * @param {number} latitude - The latitude of the marker's location.
 * @param {number} longitude - The longitude of the marker's location.
 * @param {string} name - The name of the school associated with the marker.
 * @param {Array.<Object>} result - A list of JSON objects that represent
 * the search results for news articles about a school.
 */
function createMarker(map, latitude, longitude, name, result) {
  const pos = {lat: latitude, lng: longitude};
  const contentString = createContentString(name, result.items);

  // Make the marker.
  const markerIcon = {
    url: 'https://maps.google.com/mapfiles/kml/paddle/purple-stars.png',
    scaledSize: new google.maps.Size(40, 40),
  };

  const marker = new google.maps.Marker({
    position: pos,
    map: map,
    title: name,
    icon: markerIcon,
  });

  // Make the associated infowindow.
  const infowindow = new google.maps.InfoWindow({
    content: contentString,
  });

  marker.addListener('click', function() {
    infowindow.open(map, marker);
  });
}
