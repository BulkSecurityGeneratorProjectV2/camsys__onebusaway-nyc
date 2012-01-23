/*
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

var _gaq = _gaq || [];
var OBA = window.OBA || {};

// This is mostly for IE7, to make sure AJAX/JSON calls are not cached. 
$.ajaxSetup({ cache: false });

OBA.Config = {
		searchUrl: "api/search",
		configUrl: "api/config",
		stopsWithinBoundsUrl: "api/stops-within-bounds",
		
		siriSMUrl: "api/siri/stop-monitoring.json",
		siriVMUrl: "api/siri/vehicle-monitoring.json",
		
		refreshInterval: 15000,
		
		// This variable is overwritten by the configuration service--the JS found at configUrl (above)
		staleTimeout: 120,

		// This method is called by the JS found at configUrl (above) when the configuration has finished loading.
		configurationHasLoaded: function() {
			_gaq.push(['_setAccount', OBA.Config.googleAnalyticsSiteId]);
			_gaq.push(['_setDomainName', 'none']);
			_gaq.push(['_setAllowLinker', true]);
			_gaq.push(['_trackPageview']);
			(function() {var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);})();
		},
		
		// For debugging: set to an ISO time string to make UI request from another time
		time: null,

		// Called by UI functions that want to send events to GA
		analyticsFunction: function(type, value) {
			_gaq.push(['_trackEvent', "Desktop Web", type, value]);
		},
		
		infoBubbleFooterFunction: function(type, query) {
			var html = '';

			if(type === "stop")	{
				html += '<div class="footer">';
				html += '<span class="header">While at the bus stop... </strong></span>';
				html += '<br/>';

				html += 'Send stop code <strong>' + query + '</strong> as a text to <strong>511123</strong>';
				html += '<br/>';
				html += 'or check <a href="m/?q=' + query + '">this stop</a> on your smartphone.';

				html += '</div>';

			} else if(type === "route") {
				html += '<div class="footer">';
				html += '<span class="header">While at the bus stop... </strong></span>';
				html += '<br/>';
				
				html += 'Check <a href="m/?q=' + query + '">this route</a> on your smartphone.';

				html += '</div>';				

			} else if(type === "sign") {
				html += 'Text <strong> ' + query + '</strong> to <strong>511123</strong> ';
				html += 'or check this stop on your smartphone at <strong>http://' + window.location.hostname + ((window.location.port !== '') ? ':' + window.location.port : '') + '/m/?q=' + query + '</strong>';
			}

			return html;
		},
		
		loadLocationIcons: function() {
			var locationIcons = [], activeLocationIcons = [];
			var size = new google.maps.Size(24, 32), 
				o_point = new google.maps.Point(0,0), 
				mid_point = new google.maps.Point(12, 32);

			var normalLocationIcon = new google.maps.MarkerImage("img/location/location.png",
		            size, o_point, mid_point);
			var activeLocationIcon = new google.maps.MarkerImage("img/location/location_active.png",
					size, o_point, mid_point);
			
			locationIcons[0] = normalLocationIcon;
			activeLocationIcons[0] = activeLocationIcon;
			
			for (var i=1; i < 10; i++) {
				var numberedLocationIcon = new google.maps.MarkerImage("img/location/location_" + i + ".png",
						size, o_point, mid_point);
				var activeNumberedLocationIcon = new google.maps.MarkerImage("img/location/location_active_" + i + ".png",
						size, o_point, mid_point);
				
				locationIcons[i] = numberedLocationIcon;
				activeLocationIcons[i] = activeNumberedLocationIcon;
			}
			var shadowImage = new google.maps.MarkerImage('img/location/shadow.png',
					size, o_point, mid_point);
			
			return [locationIcons, activeLocationIcons, shadowImage];
		}
};
