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

var OBA = window.OBA || {};

OBA.RouteMap = function(mapNode, mapMoveCallbackFn) {	
	var mtaSubwayMapType = new google.maps.ImageMapType({
		bounds: new google.maps.LatLngBounds(
					new google.maps.LatLng(40.48801936882241,-74.28397178649902),
					new google.maps.LatLng(40.92862373397717,-73.68182659149171)
				),
		getTileUrl: function(coord, zoom) {
			if(!(zoom >= this.minZoom && zoom <= this.maxZoom)) {
				return null;
			}
			
			var zoomFactor = Math.pow(2, zoom);
			var center_p = new google.maps.Point((coord.x * 256 + 128) / zoomFactor, (((coord.y + 1) * 256) + 128) / zoomFactor);
		    var center_ll = map.getProjection().fromPointToLatLng(center_p);

		    if(!this.bounds.contains(center_ll)) {
		    	return null;
		    }
		    
			var quad = "", i;
		    for(i = zoom; i > 0; i--) {
		        var mask = 1 << (i - 1); 
		        var cell = 0; 
		        if ((coord.x & mask) != 0) {
		            cell++; }
		        if ((coord.y & mask) != 0) {
		            cell += 2; }
		        quad += cell; 
		    } 
			return 'http://tripplanner.mta.info/maps/SystemRoutes_New/' + quad + '.png'; 
		},
		tileSize: new google.maps.Size(256, 256),
		opacity: 0.5,
		maxZoom: 15,
		minZoom: 14,
		name: 'MTA Subway Map',
		isPng: true,
		alt: ''
	});

	var mutedTransitStylesArray = 
		[{
			featureType: "road.arterial",
			elementType: "geometry",
			stylers: [
			          { saturation: -100 },
			          { lightness: 100 },
			          { visibility: "simplified" },
			          { hue: "#ffffff" }
			          ]
		},{
			featureType: "road.highway",
			elementType: "geometry",
			stylers: [
			          { saturation: -80 },
			          { lightness: 60 },
			          { visibility: "on" },
			          { hue: "#0011FF" }
			          ]
		},{
			featureType: "road.local",
			elementType: "geometry",
			stylers: [
			          { saturation: 0 },
			          { lightness: 100 },
			          { visibility: "on" },
			          { hue: "#ffffff" }
			          ]
		},{
			featureType: "road.arterial",
			elementType: "labels",
			stylers: [
			          { lightness: 25 },
			          { saturation: -25 },
			          { visibility: "off" },
			          { hue: "#ddff00" }
			          ]
		},{
			featureType: "road.highway",
			elementType: "labels",
			stylers: [
			          { lightness: 60 },
			          { saturation: -70 },
			          { hue: "#0011FF" },
			          { visibility: "on" }
			          ]
		},{ 
			featureType: "administrative.locality", 
			elementyType: "labels",
			stylers: [ { visibility: "on" }, 
			           { lightness: 50 },
			           { saturation: -80 }, 
			           { hue: "#ffff00" } ] 
		},{ 
			featureType: "administrative.neighborhood", 
			elementyType: "labels",
			stylers: [ { visibility: "on" }, 
			           { lightness: 50 },
			           { saturation: -80 }, 
			           { hue: "#ffffff" } ] 
		},{
			featureType: 'landscape',
			elementType: 'labels',
			stylers: [ {'visibility': 'on'},
			           { lightness: 50 },
			           { saturation: -80 },
			           { hue: "#0099ff" }
			           ]
		},{
			featureType: 'poi',
			elementType: 'labels',
			stylers: [ {'visibility': 'on'},
			           { lightness: 50 },
			           { saturation: -80 },
			           { hue: "#0099ff" }
			           ]
		},{
			featureType: 'water',
			elementType: 'labels',
			stylers: [ {'visibility': 'off'}
			]
		}];

	var transitStyledMapType = 
		new google.maps.StyledMapType(mutedTransitStylesArray, {name: "Transit"});
	
	var defaultMapOptions = {
			zoom: 11,
			mapTypeControl: false,
			streetViewControl: false,
			zoomControl: true,
			zoomControlOptions: {
				style: google.maps.ZoomControlStyle.LARGE
			},
			minZoom: 9, 
			maxZoom: 19,
			navigationControlOptions: { style: google.maps.NavigationControlStyle.DEFAULT },
			center: new google.maps.LatLng(40.639228,-74.081154)
	};

	// Create Subway Tiles toggle button
	var subwayControlDiv = document.createElement('DIV');
	subwayControlDiv.index = 1;
	 
	// Adds a button control to toggle MTA Subway tiles
	function SubwayTilesControl(controlDiv, map) {
	  controlDiv.style.padding = '5px';
	  
	  var controlUI = document.createElement('DIV');
	  controlUI.style.backgroundColor = 'white';
	  controlUI.style.borderStyle = 'solid';
	  controlUI.style.borderWidth = '1px';
	  controlUI.style.cursor = 'pointer';
	  controlUI.style.textAlign = 'center';
	  controlUI.title = 'Click to toggle MTA Subway lines';
	  controlUI.style.color = '#000000';
	  controlDiv.appendChild(controlUI);

	  var controlText = document.createElement('DIV');
	  controlText.style.fontFamily = 'Arial,sans-serif';
	  controlText.style.fontWeight = 'normal';
	  controlText.style.fontSize = '12px';
	  controlText.style.paddingLeft = '5px';
	  controlText.style.paddingRight = '5px';
	  controlText.style.paddingTop = '3px';
	  controlText.style.paddingBottom = '3px';
	  controlText.innerHTML = '<b>Subway</b>';
	  controlUI.appendChild(controlText);
	  
	  // toggle map tiles and color of button text
	  var toggleSubway = function () {
			  (map.overlayMapTypes.length == 1) ? 
					  map.overlayMapTypes.removeAt(0, mtaSubwayMapType) : map.overlayMapTypes.insertAt(0, mtaSubwayMapType);

			  controlText.style.color = (new RGBColor(controlText.style.color).toHex().toUpperCase() == "#CCCCCC") ?
					  "#000000" : "#CCCCCC";
			  
			  controlUI.style.color = (new RGBColor(controlUI.style.color).toHex().toUpperCase() == "#CCCCCC") ?
					  "#000000" : "#CCCCCC";
	  };
	  google.maps.event.addDomListener(controlUI, 'click', function() { toggleSubway(); });

	  return { 
		  toggleSubwayTiles: function() {
			  toggleSubway();
		  }
	  };
	}

	var map = null;
	var mgr = null;
	var infoWindow = null;

	var disambiguationMarkers = [];
	var vehiclesByRoute = {};
	var vehiclesById = {};
	var polylinesByRoute = {};
	var hoverPolylines = [];
	var stopsById = {};
	var stopsAddedForRoute = {};
	var alreadyDisplayedStopIcons = {};

	var normalLocationIcon = new google.maps.MarkerImage("img/location/beachflag.png",
            new google.maps.Size(20, 32),
            new google.maps.Point(0,0),
            new google.maps.Point(0, 32));
	
	var activeLocationIcon = new google.maps.MarkerImage("img/location/beachflag_active.png",
            new google.maps.Size(20, 32),
            new google.maps.Point(0,0),
            new google.maps.Point(0, 32));
	
	// POPUPS
	function preparePopup(marker) {
		// only one popup open at a time!
		var closeFn = function() {
			if(infoWindow !== null) {
				infoWindow.close();
				infoWindow = null;
			}
		};
		closeFn();

		// make a popup, but don't open it yet!
		infoWindow = new google.maps.InfoWindow({
	    	pixelOffset: new google.maps.Size(0, (marker.getIcon().size.height / 2)),
	    	disableAutoPan: false
	    });

		google.maps.event.addListener(infoWindow, "closeclick", closeFn);
	}
	
	function showPopupWithContent(marker, content) {
		preparePopup(marker);
		
		infoWindow.setContent(content);
		infoWindow.open(map, marker);
	}
	
	function showPopupWithContentFromRequest(marker, url, params, contentFn, userData) {
		preparePopup(marker);		

		var popupContainerId = "container" + Math.floor(Math.random() * 1000000);
		var refreshFn = function() {
			jQuery.getJSON(url, params, function(json) {
				if(infoWindow === null) {
					return;
				}
				
				infoWindow.setContent(contentFn(json, userData, popupContainerId));
				infoWindow.open(map, marker);

				var container = jQuery("#" + popupContainerId);

				// resize bubble to fit (new) content
				container.parent().parent().css("height", Math.min(Math.max(200, container.height()), 300));
				container.parent().parent().css("width", Math.min(Math.max(300, container.width() + 25), 400)); // 25 = margin for close button 

				container.parent().css("overflow", "hidden");
				container.parent().parent().css("overflow", "hidden");
			});
		};
		refreshFn();		
		infoWindow.refreshFn = refreshFn;	

		var updateTimestamp = function() {
			var timestampContainer = jQuery("#" + popupContainerId).find(".updated");
			
			if(timestampContainer.length === 0) {
				return;
			}
			
			var age = parseInt(timestampContainer.attr("age"), 10);
			var referenceEpoch = parseInt(timestampContainer.attr("referenceEpoch"), 10);
			var newAge = age + ((new Date().getTime() - referenceEpoch) / 1000);
			timestampContainer.text("Data updated " + OBA.Util.displayTime(newAge));
		};
		updateTimestamp();		
		infoWindow.updateTimestamp = updateTimestamp;	
	}
	
	// return html for a SIRI VM response
	function getVehicleContentForResponse(r, unusedUserData, popupContainerId) {
		var activity = r.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity[0];
		if(activity === null || activity.MonitoredVehicleJourney === null) {
			return null;
		}

		var vehicleId = activity.MonitoredVehicleJourney.VehicleRef;
		var vehicleIdParts = vehicleId.split("_");
		var vehicleIdWithoutAgency = vehicleIdParts[1];

		var routeId = activity.MonitoredVehicleJourney.LineRef;
		var routeIdParts = routeId.split("_");
		var routeIdWithoutAgency = routeIdParts[1];

		var html = '<div id="' + popupContainerId + '" class="popup">';
		
		// header
		html += '<div class="header vehicle">';
		html += '<p class="title">' + routeIdWithoutAgency + " " + activity.MonitoredVehicleJourney.DestinationName + '</p><p>';
		html += '<span class="type">Vehicle #' + vehicleIdWithoutAgency + '</span>';

		var updateTimestamp = OBA.Util.ISO8601StringToDate(activity.RecordedAtTime).getTime();
		var updateTimestampReference = OBA.Util.ISO8601StringToDate(r.Siri.ServiceDelivery.ResponseTimestamp).getTime();

		var age = (parseInt(updateTimestampReference, 10) - parseInt(updateTimestamp, 10)) / 1000;
		var staleClass = ((age > OBA.Config.staleTimeout) ? " stale" : "");			

		html += '<span class="updated' + staleClass + '"' + 
				' age="' + age + '"' + 
				' referenceEpoch="' + new Date().getTime() + '"' + 
				'>Data updated ' 
				+ OBA.Util.displayTime(age) 
				+ '</span>'; 
		
		// (end header)
		html += '</p>';
		html += '</div>';
		
		// service alerts
		html += getServiceAlerts(r, activity.MonitoredVehicleJourney.SituationRef);		
		
		// service available at stop
		if(typeof activity.MonitoredVehicleJourney.MonitoredCall === 'undefined' 
			|| typeof activity.MonitoredVehicleJourney.OnwardCalls === 'undefined'
			|| typeof activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall === 'undefined') {

			html += '<p class="service">Next stops are not known for this vehicle.</p>';
		} else {
			var nextStops = [];
			nextStops.push(activity.MonitoredVehicleJourney.MonitoredCall);
			jQuery.each(activity.MonitoredVehicleJourney.OnwardCalls.OnwardCall, function(_, onwardCall) {
				if(nextStops.length >= 3) {
					return false;
				}
				nextStops.push(onwardCall);
			});
		
			html += '<p class="service">Next stops:</p>';
			html += '<ul>';			
			jQuery.each(nextStops, function(_, call) {
				var stopIdParts = call.StopPointRef.split("_");
				var stopIdWithoutAgencyId = stopIdParts[1];
				
				html += '<li class="nextStop">';				
				html += '<a href="#' + stopIdWithoutAgencyId + '">' + call.StopPointName + '</a>';
				html += '<span>';
				html +=   call.Extensions.Distances.PresentableDistance;
				html += '</span></li>';
			});
			html += '</ul>';
		}
		
		html += OBA.Config.infoBubbleFooterFunction('route', routeIdWithoutAgency);			
		html += getZoomHereLink();
		
		// (end popup)
		html += '</div>';
		
		return html;
	}
	
	function getZoomHereLink() {
		var zoomHere = '<p id="zoomHere" style="line-height: 210%;"><a href="#">Zoom In</a></p>';

		jQuery('#zoomHere').live("click", function(e) { 
			e.preventDefault();

			if (infoWindow !== null && infoWindow.anchor !== null) {
				map.setCenter(infoWindow.anchor.getPosition());
			}
			
			map.setZoom(map.maxZoom - 3); 
		});
		
		return zoomHere;
	}

	function getServiceAlerts(r, situationRefs) {
	    var html = '';

	    var situationIds = {};
        var situationRefsCount = 0; 
        if (situationRefs != null) {
            jQuery.each(situationRefs, function(_, situation) {
                situationIds[situation.SituationSimpleRef] = true;
                situationRefsCount += 1;
            });
        }
        
        if (situationRefs == null || situationRefsCount > 0) {
            if (r.Siri.ServiceDelivery.SituationExchangeDelivery != null) {
                jQuery.each(r.Siri.ServiceDelivery.SituationExchangeDelivery[0].Situations.PtSituationElement, function(_, ptSituationElement) {
                    var situationId = ptSituationElement.SituationNumber;
                    if (situationRefs == null || situationIds[situationId] === true) {
                        html += ptSituationElement.Description.replace(/\n/g, "<br/>");
                    }
                });
            }
        }
        
        if (html !== '') {
            html = '<p class="service-alert">' + html + '</p>';
        }
        
        return html;
	}
	
	function getStopContentForResponse(r, stopResult, popupContainerId) {
		var visits = r.Siri.ServiceDelivery.StopMonitoringDelivery[0].MonitoredStopVisit;
		if(visits === null) {
			return null;
		}
		
		var html = '<div id="' + popupContainerId + '" class="popup">';
		
		// header
		html += '<div class="header stop">';
		html += '<p class="title">' + stopResult.name + '</p><p>';
		html += '<span class="type">Stop #' + stopResult.stopIdWithoutAgency + '</span>';
		
		// update time across all arrivals
		var updateTimestampReference = OBA.Util.ISO8601StringToDate(r.Siri.ServiceDelivery.ResponseTimestamp).getTime();
		var maxUpdateTimestamp = null;
		jQuery.each(visits, function(_, monitoredJourney) {
			var updateTimestamp = OBA.Util.ISO8601StringToDate(monitoredJourney.RecordedAtTime).getTime();
			if(updateTimestamp > maxUpdateTimestamp) {
				maxUpdateTimestamp = updateTimestamp;
			}
		});		
		
		if(maxUpdateTimestamp !== null) {
			var age = (parseInt(updateTimestampReference, 10) - parseInt(maxUpdateTimestamp, 10)) / 1000;
			var staleClass = ((age > OBA.Config.staleTimeout) ? " stale" : "");

			html += '<span class="updated' + staleClass + '"' + 
					' age="' + age + '"' + 
					' referenceEpoch="' + new Date().getTime() + '"' + 
					'>Data updated ' 
					+ OBA.Util.displayTime(age) 
					+ '</span>'; 
		}
		
		// (end header)
		html += '  </p>';
		html += ' </div>';
		
		// service alerts
	    html += getServiceAlerts(r, null);
		
	    // service available
		html += '<p class="service">Service available at this stop:</p>';
		html += '<ul>';
		
		var totalRouteCount = 0;
		var headsignsByRouteId = {};
    	jQuery.each(stopResult.routesAvailable, function(_, routeResult) {
    		headsignsByRouteId[routeResult.routeId] = routeResult.routeIdWithoutAgency + " " + routeResult.longName;
    		totalRouteCount++;
    	});
		
		var maxObservationsToShow = 3;
		if(totalRouteCount > 10) {
			maxObservationsToShow = 1;
		} else if(totalRouteCount > 5) {
			maxObservationsToShow = 2;
		}		
		
		var arrivalsByRouteId = {};
		jQuery.each(visits, function(_, monitoredJourney) {
			var routeId = monitoredJourney.MonitoredVehicleJourney.LineRef;
			
			if(typeof arrivalsByRouteId[routeId] === 'undefined') {
				arrivalsByRouteId[routeId] = [];
				delete headsignsByRouteId[routeId];
			}
			
			arrivalsByRouteId[routeId].push(monitoredJourney.MonitoredVehicleJourney);
		});

		jQuery.each(arrivalsByRouteId, function(routeId, monitoredVehicleJourneyCollection) {
			var routeIdParts = routeId.split("_");
			var routeIdWithoutAgency = routeIdParts[1];
			
			html += '<li class="route">';
			html += '<a href="#' + routeIdWithoutAgency + '">' + routeIdWithoutAgency + " " + monitoredVehicleJourneyCollection[0].DestinationName + '</a>';
			html += '</li>';
			
			jQuery.each(monitoredVehicleJourneyCollection, function(_, monitoredVehicleJourney) {
				if(_ >= maxObservationsToShow) {
					return false;
				}
					
				var distance = monitoredVehicleJourney.MonitoredCall.Extensions.Distances.PresentableDistance;
					
				if(typeof monitoredVehicleJourney.ProgressStatus !== 'undefined' && 
						monitoredVehicleJourney.ProgressStatus === "layover") {
					distance += " (at terminal)";
				}
					
				html += '<li class="arrival">' + distance + '</li>';
			});
		});
		
		// ...and the routes available with no upcoming service
		var haveRoutesWithoutService = false;
		var j = 0;
		jQuery.each(headsignsByRouteId, function(routeId, headsign) {
			var routeIdParts = routeId.split("_");
			var routeIdWithoutAgency = routeIdParts[1];

			if(j > 0) {
				html += '<li class="route no-padding">';
			} else {
				html += '<li class="route">';
			}

			html += '<a href="#' + routeIdWithoutAgency + '">' + headsign + '</a>';
			html += '</li>';
			
			haveRoutesWithoutService = true;
			j++;
		});

		if(haveRoutesWithoutService === true) {
			html += '<p class="service">No buses en-route. Check back shortly for an update.</p>';
		}
		
		html += '</ul>';

		html += OBA.Config.infoBubbleFooterFunction("stop", stopResult.stopIdWithoutAgency);
		html += getZoomHereLink();
	        
		// (end popup)
		html += '</div>';
		
		return html;
	}
	
	// POLYLINE
	function removePolylines(routeId) {
		if(typeof polylinesByRoute[routeId] !== 'undefined') {
			var polylines = polylinesByRoute[routeId];

			jQuery.each(polylines, function(_, polyline) {
				polyline.setMap(null);
			});
			
			delete polylinesByRoute[routeId];
		}
	}
	
	function addPolylines(routeId, encodedPolylines, color) {
		if(typeof polylinesByRoute[routeId] === 'undefined') {
			polylinesByRoute[routeId] = [];
		}

		jQuery.each(encodedPolylines, function(_, encodedPolyline) {
			var points = OBA.Util.decodePolyline(encodedPolyline);
		
			var latlngs = jQuery.map(points, function(x) {
				return new google.maps.LatLng(x[0], x[1]);
			});

			var options = {
				path: latlngs,
				strokeColor: "#" + color,
				strokeOpacity: 1.0,
				strokeWeight: 3,
				clickable: false,
				map: map
			};
			
			var shape = new google.maps.Polyline(options);

			polylinesByRoute[routeId].push(shape);
		});	
	}

	// STOPS
	function removeStops(routeId, preserveStopsInView) {
		if(typeof stopsAddedForRoute[routeId] !== 'undefined') {
			var stops = stopsAddedForRoute[routeId];
			
			jQuery.each(stops, function(_, marker) {
				var stopId = marker.stopId;
				
				if(preserveStopsInView && map.getBounds().contains(marker.getPosition())) {
					return false;
				}

				alreadyDisplayedStopIcons[stopId] = false;
				delete stopsById[stopId];				
				mgr.removeMarker(marker);
				marker.setMap(null);
			});
			
			delete stopsAddedForRoute[routeId];
		}		
	}
	
	function addStops(routeId, stopItems) {
		if(typeof stopsAddedForRoute[routeId] === 'undefined') {
			stopsAddedForRoute[routeId] = [];
		}

		jQuery.each(stopItems, function(_, stop) {
			var stopId = stop.stopId;
			var name = stop.name;
			var latitude = stop.latitude;
			var longitude = stop.longitude;
			var direction = stop.stopDirection;
			
			// does the stop arleady exist, e.g. from another route?
			if(typeof stopsById[stopId] !== 'undefined') {
				stopsAddedForRoute[routeId].push(stopsById[stopId]);
				return;
			}
			
			// if we get here, we're adding a new stop marker:
			var directionKey = direction;
			if(directionKey === null) {
				directionKey = "unknown";
			}
			
			var icon = new google.maps.MarkerImage("img/stop/stop-" + directionKey + ".png",
					new google.maps.Size(21, 21),
					new google.maps.Point(0,0),
					new google.maps.Point(10, 10));
			
			var markerOptions = {
					position: new google.maps.LatLng(latitude, longitude),
					icon: icon,
					zIndex: 1,
					title: name,
					stopId: stopId
					};

	        var marker = new google.maps.Marker(markerOptions);
	        
	       google.maps.event.addListener(marker, "click", function(mouseEvent) {
	    	   var stopIdParts = stopId.split("_");
	    	   var agencyId = stopIdParts[0];
	    	   var stopIdWithoutAgency = stopIdParts[1];
	    	   
	    	   OBA.Config.analyticsFunction("Stop Marker Click", stopIdWithoutAgency);
	    	   
	    	   showPopupWithContentFromRequest(this, OBA.Config.siriSMUrl, 
	    			   { OperatorRef: agencyId, MonitoringRef: stopIdWithoutAgency, StopMonitoringDetailLevel: "normal" },
	    			   getStopContentForResponse, stop);
	    	   });

	    	// FIXME: route zoom level configuration?
	    	mgr.addMarker(marker, 16, 19);
	    
	        stopsAddedForRoute[routeId].push(marker);
	        stopsById[stop.stopId] = marker;
	    });
	}
	
	function mapShowPopupForStopId(stopId) {
		var stopMarker = stopsById[stopId];
		
		if(typeof stopMarker === 'undefined') {
			return;
		}
		if (stopMarker.getMap() !== null) {
			stopMarker.setMap(map);
		}

		map.setCenter(stopMarker.getPosition());
		map.setZoom(14);
		google.maps.event.trigger(stopMarker, "click");
		
		alreadyDisplayedStopIcons[stopId] = true;
	}
	
	// VEHICLES
	function updateVehicles(routeId) {
		if(typeof vehiclesByRoute[routeId] === 'undefined') {
			vehiclesByRoute[routeId] = {};
		}
		
		var routeIdParts = routeId.split("_");
		var agencyId = routeIdParts[0];
		var routeIdWithoutAgency = routeIdParts[1];
		
		jQuery.getJSON(OBA.Config.siriVMUrl + "?callback=?", { OperatorRef: agencyId, LineRef: routeIdWithoutAgency }, 
		function(json) {

			var vehiclesByIdInResponse = {};
			jQuery.each(json.Siri.ServiceDelivery.VehicleMonitoringDelivery[0].VehicleActivity, function(_, activity) {

				var latitude = activity.MonitoredVehicleJourney.VehicleLocation.Latitude;
				var longitude = activity.MonitoredVehicleJourney.VehicleLocation.Longitude;
				var orientation = activity.MonitoredVehicleJourney.Bearing;
				var headsign = activity.MonitoredVehicleJourney.DestinationName;

				var vehicleId = activity.MonitoredVehicleJourney.VehicleRef;
				var vehicleIdParts = vehicleId.split("_");
				var vehicleIdWithoutAgency = vehicleIdParts[1];
				var marker = vehiclesById[vehicleId];
				
				// create marker if it doesn't exist				
				if(typeof marker === 'undefined' || marker === null) {
					var markerOptions = {
							zIndex: 2,
							map: map,
							title: routeIdWithoutAgency + " " + headsign,
							vehicleId: vehicleId,
							routeId: routeId
					};

					marker = new google.maps.Marker(markerOptions);
			        
			    	google.maps.event.addListener(marker, "click", function(mouseEvent) {
			    		OBA.Config.analyticsFunction("Vehicle Marker Click", vehicleIdWithoutAgency);

			    		showPopupWithContentFromRequest(this, OBA.Config.siriVMUrl + "?callback=?", 
			    				{ OperatorRef: agencyId, VehicleRef: vehicleIdWithoutAgency, MaximumNumberOfCallsOnwards: "2", VehicleMonitoringDetailLevel: "calls" }, 
			    				getVehicleContentForResponse, null);
			    	});
				}

				// icon
				var orientationAngle = "unknown";
				if(orientation !== null && orientation !== 'NaN') {
					orientationAngle = Math.floor(orientation / 5) * 5;
				}
					
				var icon = new google.maps.MarkerImage("img/vehicle/vehicle-" + orientationAngle + ".png",
						new google.maps.Size(51, 51),
						new google.maps.Point(0,0),
						new google.maps.Point(25, 25));

				marker.setIcon(icon);

				// position
				var position = new google.maps.LatLng(latitude, longitude);
				marker.setPosition(position);
							    	
				// (mark that this vehicle is still in the response)
				vehiclesByIdInResponse[vehicleId] = true;

				// maps used to keep track of marker
				vehiclesByRoute[routeId][vehicleId] = marker;
				vehiclesById[vehicleId] = marker; 
			});
			
			// remove vehicles from map that are no longer in the response, for all routes in the query
			jQuery.each(vehiclesById, function(vehicleOnMap_vehicleId, vehicleOnMap) {
				if(typeof vehiclesByIdInResponse[vehicleOnMap_vehicleId] === 'undefined') {
					var vehicleOnMap_routeId = vehicleOnMap.routeId;
					
					// the route of the vehicle on the map wasn't in the query, so don't check it.
					if(routeId !== vehicleOnMap_routeId) {
						return;
					}
					
					vehicleOnMap.setMap(null);
					delete vehiclesById[vehicleOnMap_vehicleId];
					delete vehiclesByRoute[vehicleOnMap_routeId][vehicleOnMap_vehicleId];
				}
			});
		});
	}
	
	function removeVehicles(routeId) {
		if(typeof vehiclesByRoute[routeId] !== 'undefined') {
			var vehicles = vehiclesByRoute[routeId];
			
			jQuery.each(vehicles, function(_, marker) {
				var vehicleId = marker.vehicleId;
				
				marker.setMap(null);
				delete vehiclesById[vehicleId];
			});

			delete vehiclesByRoute[routeId];
		}
	}
	
	// MISC
	function removeRoutesNotInSet(routeResults) {
		removeHoverPolyline();

		jQuery.each(polylinesByRoute, function(routeAndAgencyId, _) {
			if(routeAndAgencyId === null) {
				return;
			}

			// don't remove the routes we just added!
			var removeMe = true;
			jQuery.each(routeResults, function(__, result) {
				if(routeAndAgencyId === result.routeId) {
					removeMe = false;
					return false;
				}				
			});
			
			if(removeMe) {			
				removePolylines(routeAndAgencyId);
				removeStops(routeAndAgencyId, false);
				removeVehicles(routeAndAgencyId);
			}
		});
	}
	
	function removeDisambiguationMarkers() {
		jQuery.each(disambiguationMarkers, function(_, marker) {
			marker.setMap(null);
		});
	}
	
	function removeHoverPolyline() {
		if(hoverPolylines !== null) {
			jQuery.each(hoverPolylines, function(_, polyline) {
				polyline.setMap(null);
			});
		}
		hoverPolylines = null;
	}
	
	// TODO convert to sprites
	function getActiveIcon(iconMarker) {
		if (iconMarker !== null && iconMarker.url !== null) {
			iconMarker.url = iconMarker.url.replace(/\.png/i, "-active.png");	
		}
		return iconMarker;
	}
	
	function getInactiveIcon(iconMarker) {
		if (iconMarker !== null && iconMarker.url !== null) {
			iconMarker.url = iconMarker.url.replace(/\-active/i, "");	
		}
		return iconMarker;
	}
		
	//////////////////// CONSTRUCTOR /////////////////////
	map = new google.maps.Map(mapNode, defaultMapOptions);
	mgr = new MarkerManager(map);

	// mta custom tiles
	map.overlayMapTypes.insertAt(0, mtaSubwayMapType);

	// styled basemap
	map.mapTypes.set('Transit', transitStyledMapType);
	map.setMapTypeId('Transit');
		
	// request list of stops in viewport when user stops moving map
	google.maps.event.addListener(map, "idle", function() {
		if(map.getZoom() < 16) {
			removeStops("__VIEWPORT__", false);
			return;
		}
		
		jQuery.getJSON(OBA.Config.stopsWithinBoundsUrl + "?callback=?", { bounds: map.getBounds().toUrlValue() }, 
		function(json) {
			removeStops("__VIEWPORT__", true);

			addStops("__VIEWPORT__", json.searchResults);
		});
	});
	
	// show subway tiles toggle control only at relevant zoom levels
	var subwayTilesControl = new SubwayTilesControl(subwayControlDiv, map);
	
	function showSubwayToggle() {
		var topRightControls = map.controls[google.maps.ControlPosition.TOP_RIGHT];
		var length = map.controls[google.maps.ControlPosition.TOP_RIGHT].getLength();
		var currentZoom = map.getZoom();
		var i = 0;
		for (; i < length; i++) {
			if (topRightControls.getAt(i) == subwayControlDiv) {
				if (currentZoom < 14) {
					subwayTilesControl.toggleSubwayTiles();
					topRightControls.removeAt(i);
				} 
			}
		}
		if (currentZoom >= 14 && (i > length || i == 0)) {
			topRightControls.push(subwayControlDiv);
		} 
	}
	
	var zoomSubwayCtrlTimeout = null;
	google.maps.event.addListener(map, 'zoom_changed', function() {
		if (zoomSubwayCtrlTimeout !== null) {
			clearTimeout(zoomSubwayCtrlTimeout);
		}
		zoomSubwayCtrlTimeout = setTimeout(showSubwayToggle, 2000);
	});
	
	// timer to update periodically
	setInterval(function() {
		jQuery.each(vehiclesByRoute, function(routeId, vehicles) {
			updateVehicles(routeId);
		});

		if(infoWindow !== null && typeof infoWindow.refreshFn === 'function') {
			infoWindow.refreshFn();
		}
	}, OBA.Config.refreshInterval);

	// updates timestamp
	setInterval(function() {
		if(infoWindow !== null && typeof infoWindow.updateTimestamp === 'function') {
			infoWindow.updateTimestamp();
		}
	}, 1000);

	//////////////////// PUBLIC INTERFACE /////////////////////
	return {
		getBounds: function() {
			return map.getBounds();
		},
		
		removeAllRoutes: function() {
			removeRoutesNotInSet({});
		},
		
		removeRoutesNotInSet: removeRoutesNotInSet,
		
		showPopupForStopId: function(stopId) {
			mapShowPopupForStopId(stopId);
		},
		
		showStopIcon: function(stopId) {
			var stopMarker = stopsById[stopId];
			
			if(typeof stopMarker === 'undefined') {
				return;
			}
			var activeIcon = getActiveIcon(stopMarker.getIcon());
			stopMarker.setIcon(activeIcon);
			
			if (stopMarker.getMap() == null) {
				stopMarker.setMap(map);
			} else if (stopMarker.getVisible() == true) {
				alreadyDisplayedStopIcons[stopId] = true;
			}
		},
		
		hideStopIcon: function(stopId) {
			var stopMarker = stopsById[stopId];
			
			if(typeof stopMarker === 'undefined') {
				return;
			}
			var inactiveIcon = getInactiveIcon(stopMarker.getIcon());
			stopMarker.setIcon(inactiveIcon);
			
			// only hide if wasn't already visible
			if (alreadyDisplayedStopIcons[stopId] !== true) {
				stopMarker.setMap(null);
			}
		},
		
		showRoute: function(routeResult) {
			// already on map
			if(typeof polylinesByRoute[routeResult.routeId] !== 'undefined') {
				return
			}
			
			jQuery.each(routeResult.destinations, function(_, destination) {
				addPolylines(routeResult.routeId, destination.polylines, routeResult.color);
				addStops(routeResult.routeId, destination.stops);
			});

			updateVehicles(routeResult.routeId);
		},
	
		// pan to route extent unless zoomed in and the route is nearby
		panToRoute: function(routeResult) {
			var polylines = polylinesByRoute[routeResult.routeId];
			var newBounds = new google.maps.LatLngBounds();
			var routeBounds = new google.maps.LatLngBounds();
			var currentBounds = map.getBounds();
			var currentBoundsBuffer = currentBounds;
			var sw = currentBounds.getSouthWest();
			var ne = currentBounds.getNorthEast();
			var boundaryBuffer = .05;  // ~3.5 miles?
			var alreadyInView = false;
			
			// filter bounds by what's in current viewport plus some
			// Note: for western hemisphere, above equator
			currentBoundsBuffer.extend(new google.maps.LatLng(sw.lat() - boundaryBuffer, sw.lng() - boundaryBuffer));
			currentBoundsBuffer.extend(new google.maps.LatLng(ne.lat() + boundaryBuffer, ne.lng() + boundaryBuffer));	
					
			jQuery.each(polylines, function(_, polyline) {
				if (typeof polyline !== 'undefined') { 
					var coordinates = polyline.getPath();
					
					// scenario 0: route is aleady in view
					// scenario 1: route will be in bounds + buffer						
					for (var k=0; k < coordinates.length; k++) {
						var coordinate = coordinates.getAt(k);
						if (currentBoundsBuffer.contains(coordinate)) {	
							newBounds.extend(coordinate);
						}
						if (currentBounds.contains(coordinate)) {	
							alreadyInView = true;
							break;
						}
						routeBounds.extend(coordinate);
					}
				}
			});
			
			if (alreadyInView) {
				// do nothing for now
			} else if (newBounds.isEmpty()) {
				// scenario 2: route will not be in bounds
				map.fitBounds(routeBounds);
			} else {
				// stay close to user's existing zoom level
				var currentZoom = map.getZoom();
				map.fitBounds(newBounds);
				map.setZoom(currentZoom);
			}	
		},
		
		removeHoverPolyline: removeHoverPolyline,
		
		showHoverPolyline: function(encodedPolylines, color) {
			hoverPolylines = [];
			jQuery.each(encodedPolylines, function(_, encodedPolyline) {
				var points = OBA.Util.decodePolyline(encodedPolyline);
			
				var latlngs = jQuery.map(points, function(x) {
					return new google.maps.LatLng(x[0], x[1]);
				});

				var shape = new google.maps.Polyline({
					path: latlngs,
					strokeColor: "#" + color,
					strokeOpacity: 0.7,
					strokeWeight: 3,
					map: map
				});
			
				hoverPolylines.push(shape);
			});
		},
		
		showBounds: function(bounds) {
			map.fitBounds(bounds);
		},

		showLocation: function(lat, lng) {
			var location = new google.maps.LatLng(lat, lng);
			map.panTo(location);
			map.setZoom(16);
		},
		
		removeDisambiguationMarkers: removeDisambiguationMarkers,
		
		addDisambiguationMarkerWithContent: function(latlng, address, neighborhood) {				
			var markerOptions = {
					position: latlng,
		            icon: normalLocationIcon,
		            zIndex: 2,
		            title: address,
		            map: map
			};

		    var marker = new google.maps.Marker(markerOptions);
		    disambiguationMarkers.push(marker);
		    
	    	google.maps.event.addListener(marker, "click", function(mouseEvent) {
	    		var content = '<h3><b>' + address + '</b></h3>';

	    		if(neighborhood !== null) {
	    			content += neighborhood;
	    		}
	    		
	    		showPopupWithContent(marker, content);
	    	});
	    	
	    	return marker;
		},
		
		activateLocationIcon: function(marker) {
			marker.setIcon(activeLocationIcon);
		},
		
		deactivateLocationIcon: function(marker) {
			marker.setIcon(normalLocationIcon);
		},
		
		registerMapListener: function(listener, fx) {
			google.maps.event.addListener(map, listener, fx);
		}
	};
};
