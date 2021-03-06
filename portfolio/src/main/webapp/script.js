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

/**
 * Adds a random fun fact to the page.
 */
function addRandomFact() {
  const funFacts = [
    `I've built 8 computers so far!`,
    'I interned at Google last summer.',
    'I spent my last summer in New York City.',
    'I speak Spanish.',
    'I love broccoli.',
    `I can solve a Rubik's Cube.`
  ];

  // Pick a random greeting.
  let funFact = funFacts[Math.floor(Math.random() * funFacts.length)];

  // Add it to the page.
  const funFactContainer = document.getElementById('fun-fact-container');
  while (funFactContainer.innerText == funFact) {
    funFact = funFacts[Math.floor(Math.random() * funFacts.length)];
  }
  funFactContainer.innerText = funFact;
  funFactContainer.style.backgroundColor = 'whitesmoke';
}

function getComments() {
  const selectElement = document.getElementById('limit-select');
  const commentLimit = selectElement.options[selectElement.selectedIndex].value;
  const languageSelect = document.getElementById('language-select');
  const language = languageSelect.options[languageSelect.selectedIndex].value;
  fetch('/data?limit=' + commentLimit + '&lang=' + language).then(
      response => response.json()).then((comments) => {
        clearComments();
        comments.forEach(addComment);
      });
}

function addComment(comment, index) {
  const commentContainer = document.getElementById('comment-container');
  const node = document.createElement('li');
  node.setAttribute('class', 'comment-node');
  node.setAttribute('id', 'comment' + index);
  const textNode = document.createTextNode(comment.text);
  node.appendChild(textNode);
  commentContainer.appendChild(node);
}

function clearComments() {
  const commentContainer = document.getElementById('comment-container');
  commentContainer.innerHTML = '';
}

function deleteComments() {
  fetch('/delete-data', {method: 'POST'}).then(() => getComments());
}

function createMap() {
  // Create a map with a dark color scheme centered at my hometown, Anaheim.
  const map = new google.maps.Map(
      document.getElementById('map'), {
        center: {lat: 33.835982, lng: -117.914157},
        zoom: 11,
        styles: [
          {
            elementType: 'geometry',
            stylers: [{color: '#292929'}]
          },
          {
            elementType: 'labels.text.stroke',
            stylers: [{color: '#242f3e'}]
          },
          {
            elementType: 'labels.text.fill',
            stylers: [{color: '#ff6347'}]
          },
          {
            featureType: 'administrative.locality',
            elementType: 'labels.text.fill',
            stylers: [{color: '#d59563'}]
          },
          {
            featureType: 'poi',
            elementType: 'labels.text.fill',
            stylers: [{color: '#087E8B'}]
          },
          {
            featureType: 'poi.park',
            elementType: 'geometry',
            stylers: [{color: '#263c3f'}]
          },
          {
            featureType: 'poi.park',
            elementType: 'labels.text.fill',
            stylers: [{color: '#6b9a76'}]
          },
          {
            featureType: 'road',
            elementType: 'geometry',
            stylers: [{color: '#38414e'}]
          },
          {
            featureType: 'road',
            elementType: 'geometry.stroke',
            stylers: [{color: '#212a37'}]
          },
          {
            featureType: 'road',
            elementType: 'labels.text.fill',
            stylers: [{color: '#9ca5b3'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'geometry',
            stylers: [{color: '#616163'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'geometry.stroke',
            stylers: [{color: '#1f2835'}]
          },
          {
            featureType: 'road.highway',
            elementType: 'labels.text.fill',
            stylers: [{color: '#f3d19c'}]
          },
          {
            featureType: 'transit',
            elementType: 'geometry',
            stylers: [{color: '#2f3948'}]
          },
          {
            featureType: 'transit.station',
            elementType: 'labels.text.fill',
            stylers: [{color: '#d59563'}]
          },
          {
            featureType: 'water',
            elementType: 'geometry',
            stylers: [{color: '#17263c'}]
          },
          {
            featureType: 'water',
            elementType: 'labels.text.fill',
            stylers: [{color: '#515c6d'}]
          },
          {
            featureType: 'water',
            elementType: 'labels.text.stroke',
            stylers: [{color: '#17263c'}]
          }
        ]
      });

  // Store urls in String variables for readability and easiness to follow
  const shabuShabuWikiUrl = 'https://en.wikipedia.org/wiki/Shabu-shabu';
  const koreanBarbecueWikiUrl = 'https://en.wikipedia.org/wiki/Korean_barbecue';
  const hotPotWikiUrl = 'https://en.wikipedia.org/wiki/Hot_pot';
  const boilingPointUrl = 'https://www.bpgroupusa.com/';
  const inNOutUrl = 'https://www.in-n-out.com/';
  const clydesHotChickenUrl = 'https://www.clydeshotchicken.com/';
  const portosBakeryUrl = 'https://www.portosbakery.com/';

  // Variables starting with shabu will be referring to "House of Shabu Shabu II".
  const shabuContentString = '<div id="shabu-content">' +
      '<h1 class="firstHeading">House of Shabu Shabu II</h1>' +
      '<div>' +
      '<p><b>House of <a href="' + shabuShabuWikiUrl + '">Shabu Shabu</a> II</b> is a great place to ' +
      'go to with a friend or a special someone, especially if you are into ' +
      'the interactive experience of <a href="' + koreanBarbecueWikiUrl + '">Korean barbecue</a>. ' +
      'The experience at House of Shabu Shabu II is similar in that ' +
      'you choose from a selection of menus which include a multitude ' +
      'of meats and vegetables and can order as much as you want! </p> ' +
      '</div>' +
      '</div>';

  // Variables starting with boiling will be referring to "Boiling Point".
  const boilingContentString = '<div id="boiling-content">' +
      '<h1 class="firstHeading">Boiling Point</h1>' +
      '<div>' +
      '<p><b>Boiling Point</b> is an establishment that specializes in <a href="' +
      hotPotWikiUrl + '">Taiwanese hot soup</a> cuisine. ' +
      'I fell in love with this restaurant simply because of the delicious flavors that I ' +
      'experienced here, and the fact that you get to eat this hot soup ' +
      'while it is sitting over a flame so it stays nice and hot. ' +
      'I recommend ordering a green tea to balance out the heat ' +
      'with a splash of freshness and sweetness. </p> ' +
      '<p>Link: <a href="' + boilingPointUrl + '">' + boilingPointUrl + '</a></p> ' +
      '</div>' +
      '</div>';

  // Variables starting with in will be referring to "In n Out".
  const inContentString = '<div id="in-content">' +
      '<h1 class="firstHeading">In-n-Out Burger</h1>' +
      '<div>' +
      '<p><b>In-n-Out</b> is a classic fast food chain that is dear to many in California and the ' +
      'Southwest. There are many ongoing debates where groups debate whether this chain is better ' +
      'than other localized chains such as Shake Shack from the East and Whataburger from Texas. ' +
      'Even though I often decide to eat here, everytime I take a bite out of their cheeseburger I get ' +
      'the same experience that I remember having when I first tried it. </p> ' +
      '<p>Link: <a href="' + inNOutUrl + '">' + inNOutUrl + '</a></p> ' +
      '</div>' +
      '</div>';

  // Variables starting with clydes will be referring to "Clyde's Hot Chicken".
  const clydesContentString = '<div id="clydes-content">' +
      `<h1 class="firstHeading">Clyde's Hot Chicken</h1>` +
      '<div>' +
      `<p><b>Clyde's Hot Chicken</b> is my go-to when I'm craving a good chicken sandwich. ` +
      `The chicken sandwich here caught my attention due to it's delicous seasoning, ` +
      'perfect spice level, and the balance it achieves paired with their coleslaw. ' +
      'I love their chicken sandwich, but the fries that come with the combo are ' +
      `nothing to laugh at. Unlike most fast food chains, Clyde's seasons their fries ` +
      'with a seasoning so good that it makes them my favorite fries from any food chain. </p>' +
      '<p>Link: <a href="' + clydesHotChickenUrl + '">' + clydesHotChickenUrl + '</a></p> ' +
      '</div>' +
      '</div>';

  // Variables starting with portos will be referring to "Porto's Bakery".
  const portosContentString = '<div id="portos-content">' +
      `<h1 class="firstHeading">Porto's Bakery</h1>` +
      '<div>' +
      `<p><b>Porto's Bakery</b> differs from the other restaurants on this list as ` +
      'they specialize in Cuban baked goods. The baked goods come out fresh everyday ' +
      'and range from sweet fruit flavored desserts to savory appetizers. Even though it ' +
      `sounds like Porto's is somewhere you would go to get a good breakfast, you can go ` +
      'at any time of your day and be satisfied with only a few pastries. You can also ' +
      'choose to explore their other menu items that include soups, sandwiches, and much more. </p>' +
      '<p>Link: <a href="' + portosBakeryUrl + '">' + portosBakeryUrl + '</a></p> ' +
      '</div>' +
      '</div>';

  const shabuLatLng = new google.maps.LatLng(33.840585, -117.942175);
  const boilingLatLng = new google.maps.LatLng(33.761388, -117.953333);
  const inLatLng = new google.maps.LatLng(33.819016, -117.888945);
  const clydesLatLng = new google.maps.LatLng(33.874545, -117.924601);
  const portosLatLng = new google.maps.LatLng(33.852421, -117.997408);

  addRestaurant(map, shabuLatLng, 'House of Shabu Shabu II!', shabuContentString);
  addRestaurant(map, boilingLatLng, 'Boiling Point!', boilingContentString);
  addRestaurant(map, inLatLng, 'In-N-Out Burger!', inContentString);
  addRestaurant(map, clydesLatLng, `Clyde's Hot Chicken!`, clydesContentString);
  addRestaurant(map, portosLatLng, `Porto's Bakery!`, portosContentString);

}

function addRestaurant(map, latLng, title, description) {
  const marker = new google.maps.Marker({
    position: latLng,
    map: map,
    title: title
  });

  const infoWindow = new google.maps.InfoWindow({
    content: description
  });

  marker.addListener('click', function() {
    infoWindow.open(map, marker);
  });
}
