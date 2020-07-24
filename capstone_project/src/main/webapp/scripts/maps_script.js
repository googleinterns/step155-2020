// Copyright 2019 Google LLC
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

/** Toggle to hide news feed if it is already showing when the button is pressed again. */
function toggleToHideFeed(schoolName) {

  var x = document.getElementById(schoolName);
  if (x.innerHTML !== "") {
    x.innerHTML = "";
    return true;
  }
  return false;
}

/** Generates a hard-coded feed of news articles for UCI. */
function showUCINews() {

  if (toggleToHideFeed("UCInews"))  {
    return;
  }

  const articles =
      [
        'https://news.uci.edu/2020/07/07/uci-chancellor-emeritus-michael-v-drake-named-university-of-california-president/',
        'https://news.uci.edu/2020/06/30/virtual-nurses-make-a-real-difference/',
        'https://news.uci.edu/2020/06/18/uci-podcast-jessica-millward-on-the-meaning-and-importance-of-juneteenth/'
      ];

  var i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], "UCInews");
  }
}

/** Generates a hard-coded feed of news articles for UW. */
function showUWNews() {

  if (toggleToHideFeed("UWnews"))  {
    return;
  }

  const articles =
      [
        'https://www.washington.edu/news/2020/07/15/robotic-camera-backpack-for-insects/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS',
        'https://www.washington.edu/news/2020/07/08/uw-school-of-oceanography-holds-no-1-global-ranking-more-than-two-dozen-areas-in-top-50/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS',
        'https://www.washington.edu/news/2020/07/16/wsas-2020/?utm_source=UW%20News&utm_medium=tile&utm_campaign=UW%20NEWS'
      ];

  var i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], "UWnews");
  }
}

/** Generates a hard-coded feed of news articles for UW. */
function showUWBNews() {

  if (toggleToHideFeed("UWBnews"))  {
    return;
  }

  const articles =
      [
        'https://www.uwb.edu/news/july-2020/coronavirus-remote-learning',
        'https://www.uwb.edu/news/july-2020/alumni-snohomish-county-council',
        'https://www.washington.edu/uwit/stories/benefits-of-online-learning/?utm_source=UW_News_Subscribers&utm_medium=email&utm_campaign=UW_Today_row&mkt_tok=eyJpIjoiWTJReU5tVXdOR0UwWWpOayIsInQiOiJ1XC9cL1dXZ'
      ];

  var i;
  for (i = 0; i < articles.length; i++) {
    createArticleEntry(articles[i], "UWBnews");
  }
}


/** Creates an <li> element containing the link to a university news article. */
function createArticleEntry(text, schoolName) {

  var a = document.createElement('a');
  var link = document.createTextNode(text);
  a.appendChild(link);
  a.title = text;
  a.href = text;

  var node = document.createElement("LI");
  node.appendChild(a);
  document.getElementById(schoolName).appendChild(node);
}
