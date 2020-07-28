// Copyright 2019 Google LLC
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

/** Toggle to hide news feed if it is already showing when the
 * button is pressed again.
 * @param {string} schoolName
 * @return {boolean}
 */
function toggleToHideFeed(schoolName) {
  const x = document.getElementById(schoolName);
  if (x.innerHTML !== '') {
    x.innerHTML = '';
    return true;
  }
  return false;
}

/** Generates a hard-coded feed of news articles for UCI. */
function showUCINews() { // eslint-disable-line no-unused-vars
  if (toggleToHideFeed('uci-news')) {
    return;
  }

  const articles =
      [
        'https://news.uci.edu/2020/07/07/uci-chancellor-emeritus-michael-v-drake-named-university-of-california-president/',
        'https://news.uci.edu/2020/06/30/virtual-nurses-make-a-real-difference/',
        'https://news.uci.edu/2020/06/18/uci-podcast-jessica-millward-on-the-meaning-and-importance-of-juneteenth/',
      ];

  let i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], 'uci-news');
  }
}

/** Generates a hard-coded feed of news articles for UW. */
function showUWNews() { // eslint-disable-line no-unused-vars
  if (toggleToHideFeed('uw-news')) {
    return;
  }

  const articles =
      [
        'https://www.washington.edu/news/2020/07/15/robotic-camera-backpack-for-insects/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS',
        'https://www.washington.edu/news/2020/07/08/uw-school-of-oceanography-holds-no-1-global-ranking-more-than-two-dozen-areas-in-top-50/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS',
        'https://www.washington.edu/news/2020/07/16/wsas-2020/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS',
      ];

  let i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], 'uw-news');
  }
}

/** Generates a hard-coded feed of news articles for UWB. */
function showUWBNews() { // eslint-disable-line no-unused-vars
  if (toggleToHideFeed('uwb-news')) {
    return;
  }

  const articles =
      [
        'https://www.uwb.edu/news/july-2020/coronavirus-remote-learning',
        'https://www.uwb.edu/news/july-2020/alumni-snohomish-county-council',
        'https://www.washington.edu/uwit/stories/benefits-of-online-learning/?utm_source=UW_News_Subscribers&utm_medium=email&utm_campaign=UW_Today_row&mkt_tok=eyJpIjoiWTJReU5tVXdOR0UwWWpOayIsInQiOiJ1XC9cL1dXZ',
      ];

  let i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], 'uwb-news');
  }
}


/**
 * Creates an <li> element containing the link to a university news article.
 * @param {string} text
 * @param {string} schoolName
 */
function createArticleEntry(text, schoolName) {
  const a = document.createElement('a');
  const link = document.createTextNode(text);
  a.appendChild(link);
  a.title = text;
  a.href = text;

  const node = document.createElement('LI');
  node.appendChild(a);
  document.getElementById(schoolName).appendChild(node);
}

/** Creates a map centered at the geographic center of the US and
 * adds it to the page. */
function createMap() { // eslint-disable-line no-unused-vars
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 39.828502, lng: -98.579512}, zoom: 4});

  // Create a marker for each school. (TODO: fetch this info from server)
  createMarker(map, 33.639982, -117.844682, 'UCI');
  createMarker(map, 47.655277, -122.303606, 'UW');
  createMarker(map, 47.758821, -122.190725, 'UWB');
}

/** Creates and returns the content string for a marker.
 * @param {string} name
 * @return {string}
 */
function createContentString(name) {
  const contentString = `<div id="content">
      <div id="siteNotice">
      </div>
      <h1 id="firstHeading" class="firstHeading">${name}</h1>
      <div id="bodyContent">
      <p>Here you will find all the latest updates on <b>${name}</b>:</p>`+

      // Create pills for tab navigation.
      `<ul class="nav nav-pills">
      <li class="active"><a data-toggle="pill" href="#news">News</a></li>
      <li><a data-toggle="pill" href="#posts">Posts</a></li>
      </ul>`+
      // Create content stored in tabs for each pill.
      `<div class="tab-content">
      <div id="news" class="tab-pane fade in active">
      <h3>NEWS</h3>
      <p>This is where the news feed for ${name} will go.</p>
      </div>
      <div id="posts" class="tab-pane fade">
      <h3>POSTS</h3>
      <p>This is where the posts for ${name} will go.</p>
      </div>
      </div>
      </div>
      </div>`;

  return contentString;
}


/** Creates a marker and its info window and adds these to the map.
 * @param {Object} map
 * @param {number} latitude
 * @param {number} longitude
 * @param {String} name
 */
function createMarker(map, latitude, longitude, name) {
  const pos = {lat: latitude, lng: longitude};
  const contentString = createContentString(name);

  // Make the marker.
  const markerIcon = {
    url: 'http://maps.google.com/mapfiles/kml/paddle/purple-stars.png',
    scaledSize: new google.maps.Size(40, 40),
  };

  const marker = new google.maps.Marker({
    position: pos,
    map: map,
    title: name,
    icon: markerIcon,
  });

  // Make the associated info window.
  const infowindow = new google.maps.InfoWindow({
    content: contentString,
  });

  marker.addListener('click', function() {
    infowindow.open(map, marker);
  });
}
