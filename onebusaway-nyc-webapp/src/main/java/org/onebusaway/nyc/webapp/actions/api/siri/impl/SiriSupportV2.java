/**
 * Copyright (C) 2010 OpenPlans
 * Copyright (C) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.nyc.webapp.actions.api.siri.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.presentation.impl.AgencySupportLibrary;
import org.onebusaway.nyc.presentation.impl.DateUtil;
import org.onebusaway.nyc.presentation.impl.realtime.SiriSupportPredictionTimepointRecord;
import org.onebusaway.nyc.presentation.service.realtime.PresentationService;
import org.onebusaway.nyc.siri.support.SiriApcExtension;
import org.onebusaway.nyc.siri.support.SiriExtensionWrapper;
import org.onebusaway.nyc.siri.support.SiriPolyLinesExtension;
import org.onebusaway.nyc.siri.support.SiriUpcomingServiceExtension;
import org.onebusaway.nyc.transit_data.services.NycTransitDataService;
import org.onebusaway.nyc.webapp.actions.api.siri.model.DetailLevel;
import org.onebusaway.nyc.webapp.actions.api.siri.model.RouteDirection;
import org.onebusaway.nyc.webapp.actions.api.siri.model.RouteForDirection;
import org.onebusaway.nyc.webapp.actions.api.siri.model.RouteResult;
import org.onebusaway.nyc.webapp.actions.api.siri.model.StopOnRoute;
import org.onebusaway.nyc.webapp.actions.api.siri.model.StopRouteDirection;
import org.onebusaway.realtime.api.VehicleOccupancyRecord;
import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data.model.blocks.BlockInstanceBean;
import org.onebusaway.transit_data.model.blocks.BlockStopTimeBean;
import org.onebusaway.transit_data.model.blocks.BlockTripBean;
import org.onebusaway.transit_data.model.service_alerts.ServiceAlertBean;
import org.onebusaway.transit_data.model.trips.TripBean;
import org.onebusaway.transit_data.model.trips.TripStatusBean;

import uk.org.siri.siri_2.OccupancyEnumeration;
import uk.org.siri.siri_2.AnnotatedDestinationStructure;
import uk.org.siri.siri_2.AnnotatedLineStructure;
import uk.org.siri.siri_2.AnnotatedLineStructure.Destinations;
import uk.org.siri.siri_2.AnnotatedLineStructure.Directions;
import uk.org.siri.siri_2.AnnotatedStopPointStructure;
import uk.org.siri.siri_2.BlockRefStructure;
import uk.org.siri.siri_2.DataFrameRefStructure;
import uk.org.siri.siri_2.DestinationRefStructure;
import uk.org.siri.siri_2.DirectionRefStructure;
import uk.org.siri.siri_2.ExtensionsStructure;
import uk.org.siri.siri_2.FramedVehicleJourneyRefStructure;
import uk.org.siri.siri_2.JourneyPatternRefStructure;
import uk.org.siri.siri_2.JourneyPlaceRefStructure;
import uk.org.siri.siri_2.LineDirectionStructure;
import uk.org.siri.siri_2.LineRefStructure;
import uk.org.siri.siri_2.LocationStructure;
import uk.org.siri.siri_2.MonitoredCallStructure;
import uk.org.siri.siri_2.MonitoredVehicleJourneyStructure;
import uk.org.siri.siri_2.NaturalLanguageStringStructure;
import uk.org.siri.siri_2.OnwardCallStructure;
import uk.org.siri.siri_2.OnwardCallsStructure;
import uk.org.siri.siri_2.OperatorRefStructure;
import uk.org.siri.siri_2.ProgressRateEnumeration;
import uk.org.siri.siri_2.RouteDirectionStructure;
import uk.org.siri.siri_2.RouteDirectionStructure.JourneyPatterns;
import uk.org.siri.siri_2.RouteDirectionStructure.JourneyPatterns.JourneyPattern;
import uk.org.siri.siri_2.RouteDirectionStructure.JourneyPatterns.JourneyPattern.StopsInPattern;
import uk.org.siri.siri_2.SituationRefStructure;
import uk.org.siri.siri_2.SituationSimpleRefStructure;
import uk.org.siri.siri_2.StopPointInPatternStructure;
import uk.org.siri.siri_2.StopPointRefStructure;
import uk.org.siri.siri_2.VehicleRefStructure;
import uk.org.siri.siri_2.AnnotatedStopPointStructure.Lines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SiriSupportV2 {

	private static Logger _log = LoggerFactory.getLogger(SiriSupportV2.class);

	public enum OnwardCallsMode {
		VEHICLE_MONITORING, STOP_MONITORING
	}

	public enum Filters {
		DIRECTION_REF, OPERATOR_REF, LINE_REF, USE_LINE_REF, UPCOMING_SCHEDULED_SERVICE, 
		DETAIL_LEVEL, MAX_STOP_VISITS, MIN_STOP_VISITS, INCLUDE_POLYLINES
	}

	/**
	 * NOTE: The tripDetails bean here may not be for the trip the vehicle is
	 * currently on in the case of A-D for stop!
	 * @param filters
	 * @param isCancelled
	 */
	public static void fillMonitoredVehicleJourney(
			MonitoredVehicleJourneyStructure monitoredVehicleJourney,
			TripBean framedJourneyTripBean,
			TripStatusBean currentVehicleTripStatus,
			StopBean monitoredCallStopBean, OnwardCallsMode onwardCallsMode,
			PresentationService presentationService,
			NycTransitDataService nycTransitDataService,
			int maximumOnwardCalls,
			Map<String, SiriSupportPredictionTimepointRecord> stopIdToPredictionRecordMap,
			DetailLevel detailLevel,
			long responseTimestamp, Map<Filters, String> filters,
			boolean showApc, boolean showRawApc, boolean isCancelled) {

		BlockInstanceBean blockInstance = nycTransitDataService
				.getBlockInstance(currentVehicleTripStatus.getActiveTrip()
						.getBlockId(), currentVehicleTripStatus
						.getServiceDate());

		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration()
				.getTrips();

		if (monitoredCallStopBean == null) {
			monitoredCallStopBean = currentVehicleTripStatus.getNextStop();
		}

		/**********************************************/

		//Route ID
		LineRefStructure lineRef = new LineRefStructure();
		lineRef.setValue(framedJourneyTripBean.getRoute().getId());

		DirectionRefStructure directionRef = new DirectionRefStructure();
		directionRef.setValue(framedJourneyTripBean.getDirectionId());

		//Route Short Name
		NaturalLanguageStringStructure routeShortName = new NaturalLanguageStringStructure();
		routeShortName.setValue(framedJourneyTripBean.getRoute().getShortName());

		//Agency Id
		OperatorRefStructure operatorRef = new OperatorRefStructure();
		operatorRef.setValue(AgencySupportLibrary
				.getAgencyForId(framedJourneyTripBean.getRoute().getId()));

		//Framed Journey
		FramedVehicleJourneyRefStructure framedJourney = new FramedVehicleJourneyRefStructure();
		DataFrameRefStructure dataFrame = new DataFrameRefStructure();
		dataFrame.setValue(String.format("%1$tY-%1$tm-%1$td",
				currentVehicleTripStatus.getServiceDate()));
		framedJourney.setDataFrameRef(dataFrame);
		framedJourney.setDatedVehicleJourneyRef(framedJourneyTripBean.getId());

		//Shape Id
		JourneyPatternRefStructure journeyPattern = new JourneyPatternRefStructure();
		journeyPattern.setValue(framedJourneyTripBean.getShapeId());

		//Destination
		NaturalLanguageStringStructure headsign = new NaturalLanguageStringStructure();
		headsign.setValue(framedJourneyTripBean.getTripHeadsign());

		// Vehicle Id
		VehicleRefStructure vehicleRef = new VehicleRefStructure();
		if(currentVehicleTripStatus.getVehicleId() == null){
			String tripId = framedJourneyTripBean.getId();
			String blockId = framedJourneyTripBean.getBlockId();
			String directionId = framedJourneyTripBean.getDirectionId();
			String vehicleIdHash = Integer.toString((tripId + blockId + directionId).hashCode());
			String agencyName = tripId.split("_")[0];
			String vehicleId = agencyName + "_" + vehicleIdHash;

			vehicleRef.setValue(vehicleId);
		}
		else{
			vehicleRef.setValue(currentVehicleTripStatus.getVehicleId());
		}

		// Set Origin and Destination stops from Block trips. 
		StopBean lastStop = new StopBean();
		JourneyPlaceRefStructure origin = new JourneyPlaceRefStructure();

		for (int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if (blockTrip.getTrip().getId()
					.equals(framedJourneyTripBean.getId())) {
				List<BlockStopTimeBean> stops = blockTrip.getBlockStopTimes();

				origin.setValue(stops.get(0).getStopTime().getStop().getId());

				lastStop = stops.get(stops.size() - 1).getStopTime()
						.getStop();
				break;
			}
		}


		// location
		// if vehicle is detected to be on detour, use actual lat/lon, not
		// snapped location.
		LocationStructure location = new LocationStructure();

		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(6);

		if (presentationService.isOnDetour(currentVehicleTripStatus)) {
			location.setLatitude(new BigDecimal(df
					.format(currentVehicleTripStatus.getLastKnownLocation()
							.getLat())));
			location.setLongitude(new BigDecimal(df
					.format(currentVehicleTripStatus.getLastKnownLocation()
							.getLon())));
		} else {
			location.setLatitude(new BigDecimal(df
					.format(currentVehicleTripStatus.getLocation().getLat())));
			location.setLongitude(new BigDecimal(df
					.format(currentVehicleTripStatus.getLocation().getLon())));
		}

		// progress status
		List<String> progressStatuses = new ArrayList<String>();

		if (presentationService.isInLayover(currentVehicleTripStatus)) {
			progressStatuses.add("layover");
		}

		if (presentationService.isSpooking(currentVehicleTripStatus)) {
			progressStatuses.add("spooking");
		}

		// "prevTrip" really means not on the framedvehiclejourney trip
		if (!framedJourneyTripBean.getId().equals(
				currentVehicleTripStatus.getActiveTrip().getId())) {
			progressStatuses.add("prevTrip");
		}

		if (!progressStatuses.isEmpty()) {
			NaturalLanguageStringStructure progressStatus = new NaturalLanguageStringStructure();
			progressStatus.setValue(StringUtils.join(progressStatuses, ","));
			monitoredVehicleJourney.getProgressStatus().add(progressStatus);
		}

		// scheduled depature time
		if (presentationService.hasFormalBlockLevelMatch(currentVehicleTripStatus)
				&& (presentationService.isInLayover(currentVehicleTripStatus) || !framedJourneyTripBean
						.getId().equals(
								currentVehicleTripStatus.getActiveTrip()
								.getId()))) {
			BlockStopTimeBean originDepartureStopTime = null;

			for (int t = 0; t < blockTrips.size(); t++) {
				BlockTripBean thisTrip = blockTrips.get(t);
				BlockTripBean nextTrip = null;
				if (t + 1 < blockTrips.size()) {
					nextTrip = blockTrips.get(t + 1);
				}

				if (thisTrip
						.getTrip()
						.getId()
						.equals(currentVehicleTripStatus.getActiveTrip()
								.getId())) {
					// just started new trip
					if (currentVehicleTripStatus.getDistanceAlongTrip() < (0.5 * currentVehicleTripStatus
							.getTotalDistanceAlongTrip())) {
						originDepartureStopTime = thisTrip.getBlockStopTimes()
								.get(0);

						// at end of previous trip
					} else {
						if (nextTrip != null) {
							originDepartureStopTime = nextTrip
									.getBlockStopTimes().get(0);
						}
					}

					break;
				}
			}

			if (originDepartureStopTime != null) {
				long departureTime = currentVehicleTripStatus.getServiceDate()
						+ (originDepartureStopTime.getStopTime()
								.getDepartureTime() * 1000);
				monitoredVehicleJourney.setOriginAimedDepartureTime(DateUtil
						.toXmlGregorianCalendar(departureTime));
			}
		}

		// monitored call
		if (!presentationService.isOnDetour(currentVehicleTripStatus))
			fillMonitoredCall(monitoredVehicleJourney, blockInstance,
					framedJourneyTripBean, currentVehicleTripStatus,
					monitoredCallStopBean, presentationService, nycTransitDataService,
					stopIdToPredictionRecordMap, detailLevel, showApc, showRawApc, responseTimestamp, isCancelled);


		// detail level - minimal
		if (detailLevel.equals(DetailLevel.MINIMUM) || detailLevel.equals(DetailLevel.BASIC)
				|| detailLevel.equals(DetailLevel.NORMAL)|| detailLevel.equals(DetailLevel.CALLS)){
			monitoredVehicleJourney.getPublishedLineName().add(routeShortName);
			monitoredVehicleJourney.getDestinationName().add(headsign);
			monitoredVehicleJourney.setMonitored(currentVehicleTripStatus
					.isPredicted());
			monitoredVehicleJourney.setVehicleRef(vehicleRef);
			monitoredVehicleJourney.setBearing((float) currentVehicleTripStatus
					.getOrientation());
			monitoredVehicleJourney.setVehicleLocation(location);
		}

		// detail level - basic
		if (detailLevel.equals(DetailLevel.BASIC)|| detailLevel.equals(DetailLevel.NORMAL) || detailLevel.equals(DetailLevel.CALLS)){
			monitoredVehicleJourney.setFramedVehicleJourneyRef(framedJourney);
			monitoredVehicleJourney.setDirectionRef(directionRef);

			// since LineRef is fully qualified with operatorref, moving OperatorRef to normal detail
			//monitoredVehicleJourney.setOperatorRef(operatorRef);


			DestinationRefStructure dest = new DestinationRefStructure();
			dest.setValue(lastStop.getId());
			monitoredVehicleJourney.setDestinationRef(dest);

			monitoredVehicleJourney.setLineRef(lineRef);
			monitoredVehicleJourney.setProgressRate(getProgressRateForPhaseAndStatus(
					currentVehicleTripStatus.getStatus(),
					currentVehicleTripStatus.getPhase()));


			if(showApc && !isCancelled) {
				fillOccupancy(monitoredVehicleJourney,
						nycTransitDataService,
						currentVehicleTripStatus);
			}


		}

		// detail level - normal
		if (detailLevel.equals(DetailLevel.NORMAL) || detailLevel.equals(DetailLevel.CALLS)){
			monitoredVehicleJourney.setOperatorRef(operatorRef);
			// block ref
			if (presentationService.hasFormalBlockLevelMatch(currentVehicleTripStatus)) {
				BlockRefStructure blockRef = new BlockRefStructure();
				blockRef.setValue(framedJourneyTripBean.getBlockId());
				monitoredVehicleJourney.setBlockRef(blockRef);
			}

			monitoredVehicleJourney.setOriginRef(origin);
			monitoredVehicleJourney.setJourneyPatternRef(journeyPattern);
		}	

		// onward calls
		if (detailLevel.equals(DetailLevel.CALLS)){
			if (!presentationService.isOnDetour(currentVehicleTripStatus))
				fillOnwardCalls(monitoredVehicleJourney, blockInstance,
						framedJourneyTripBean, currentVehicleTripStatus,
						onwardCallsMode, presentationService,
						nycTransitDataService, stopIdToPredictionRecordMap,
						maximumOnwardCalls, responseTimestamp);
		}


		// situations
		fillSituations(monitoredVehicleJourney, currentVehicleTripStatus);

		return;
	}

	public static boolean fillAnnotatedStopPointStructure(
			AnnotatedStopPointStructure annotatedStopPoint, 
			StopRouteDirection stopRouteDirection,
			Map<Filters, String> filters, 
			DetailLevel detailLevel, 
			long currentTime
			) {

		StopBean stopBean = stopRouteDirection.getStop();
		List<RouteForDirection> routeDirections = stopRouteDirection.getRouteDirections();

		// Set Stop Name
		NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
		stopName.setValue(stopBean.getName());

		// Set Route and Direction
		Lines lines = new Lines();

		for (RouteForDirection routeDirection : routeDirections){

			String directionId = routeDirection.getDirectionId();
			String routeId = routeDirection.getRouteId();


			LineRefStructure line = new LineRefStructure();
			line.setValue(routeId);

			DirectionRefStructure direction = new DirectionRefStructure();
			direction.setValue(directionId);

			LineDirectionStructure lineDirection = new LineDirectionStructure();
			lineDirection.setDirectionRef(direction);
			lineDirection.setLineRef(line);

			lines.getLineRefOrLineDirection().add(lineDirection);

		}

		// Set Lat and Lon
		BigDecimal stopLat = new BigDecimal(stopBean.getLat());
		BigDecimal stopLon = new BigDecimal(stopBean.getLon());

		LocationStructure location = new LocationStructure();
		location.setLongitude(stopLon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
		location.setLatitude(stopLat.setScale(6, BigDecimal.ROUND_HALF_DOWN));

		// Set StopId
		StopPointRefStructure stopPointRef = new StopPointRefStructure();
		stopPointRef.setValue(stopBean.getId());

		// Details -- minimum
		annotatedStopPoint.getStopName().add(stopName);

		// Details -- normal
		if (detailLevel.equals(DetailLevel.NORMAL)|| detailLevel.equals(DetailLevel.FULL)){
			annotatedStopPoint.setLocation(location);
			annotatedStopPoint.setLines(lines);
			annotatedStopPoint.setMonitored(true);
		}

		annotatedStopPoint.setStopPointRef(stopPointRef);

		return true;
	}

	public static boolean fillAnnotatedLineStructure(
			AnnotatedLineStructure annotatedLineStructure,
			RouteResult routeResult,
			Map<Filters, String> filters, 
			DetailLevel detailLevel,
			long currentTime) {

		Directions directions = new Directions();

		// Set Line Value
		LineRefStructure line = new LineRefStructure();
		line.setValue(routeResult.getId());

		NaturalLanguageStringStructure lineName = new NaturalLanguageStringStructure();
		lineName.setValue(routeResult.getShortName());


		// DETAIL - minimum: Return only the name and identifier of stops
		//ideally, this would return only stops with scheduled service
		annotatedLineStructure.setLineRef(line);
		annotatedLineStructure.getLineName().add(lineName);
		annotatedLineStructure.setDirections(directions);
		annotatedLineStructure.setMonitored(true);

		// Loop through Direction Ids
		for(RouteDirection direction : routeResult.getDirections()){

			// Check for existing stops in direction
			if(direction == null | direction.getStops().size() == 0)
				continue;

			String directionId = direction.getDirectionId();

			// Journey patterns - holds stop points for direction
			JourneyPattern pattern = new JourneyPattern();
			JourneyPatterns patterns = new JourneyPatterns();

			// Directions
			DirectionRefStructure dirRefStructure = new DirectionRefStructure();
			dirRefStructure.setValue(directionId);

			RouteDirectionStructure routeDirectionStructure = new RouteDirectionStructure();
			NaturalLanguageStringStructure directionName = new NaturalLanguageStringStructure();

			directionName.setValue(direction.getDestination());
			routeDirectionStructure.getDirectionName().add(directionName);
			directions.getDirection().add(routeDirectionStructure);

			// Destination
			Destinations destinations = new Destinations();
			AnnotatedDestinationStructure annotatedDest = new AnnotatedDestinationStructure();
			DestinationRefStructure destRef = new DestinationRefStructure();
			destRef.setValue(direction.getDestination());
			annotatedDest.setDestinationRef(destRef );
			destinations.getDestination().add(annotatedDest);

			// Stops
			StopsInPattern stopsInPattern = new StopsInPattern();
			List<StopOnRoute> scheduledStops = new ArrayList<StopOnRoute>();
			List<StopOnRoute> allStops = new ArrayList<StopOnRoute>();			

			// Loop through StopOnRoute for particular Direction Id		
			// Categorize by Scheduled and Unscheduled Stops
			for(StopOnRoute stop : direction.getStops()){
				if(stop.getHasUpcomingScheduledStop() != null && stop.getHasUpcomingScheduledStop())
					scheduledStops.add(stop);

				allStops.add(stop);
			}

			// DETAIL -- normal: Return name, identifier and coordinates of the stop.??
			// my interpretation is that normal returns the list of stops with coordinates and their polylines
			//ideally, this would return only stops with scheduled service

			if (detailLevel.equals(DetailLevel.NORMAL)){

				for(int i = 0; i < scheduledStops.size(); i++){

					StopOnRoute stop = direction.getStops().get(i);

					BigDecimal stopLat = new BigDecimal(stop.getLatitude());
					BigDecimal stopLon = new BigDecimal(stop.getLongitude());

					LocationStructure location = new LocationStructure();
					location.setLongitude(stopLon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
					location.setLatitude(stopLat.setScale(6, BigDecimal.ROUND_HALF_DOWN));

					StopPointInPatternStructure pointInPattern = new StopPointInPatternStructure();
					pointInPattern.setLocation(location);
					pointInPattern.setOrder(BigInteger.valueOf(i));
					NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
					stopName.setValue(stop.getName());
					pointInPattern.getStopName().add(stopName);

					StopPointRefStructure spr = new StopPointRefStructure();
					spr.setValue(stop.getId());

					stopsInPattern.getStopPointInPattern().add(pointInPattern);
				}

			}

			// DETAIL -- stops: Return name, identifier and coordinates of the stop.??
			// my interpretation is that normal returns the list of stops with coordinates and their polylines
			//ideally, this would return both stops with scheduled and unscheduled service

			if (detailLevel.equals(DetailLevel.STOPS) || detailLevel.equals(DetailLevel.FULL)){
				for(int i = 0; i < allStops.size(); i++){

					StopOnRoute stop = direction.getStops().get(i);
					Boolean hasUpcomingScheduledService = stop.getHasUpcomingScheduledStop();

					BigDecimal stopLat = new BigDecimal(stop.getLatitude());
					BigDecimal stopLon = new BigDecimal(stop.getLongitude());

					LocationStructure location = new LocationStructure();
					location.setLongitude(stopLon.setScale(6, BigDecimal.ROUND_HALF_DOWN));
					location.setLatitude(stopLat.setScale(6, BigDecimal.ROUND_HALF_DOWN));

					StopPointRefStructure spr = new StopPointRefStructure();
					spr.setValue(stop.getId());

					StopPointInPatternStructure pointInPattern = new StopPointInPatternStructure();
					pointInPattern.setLocation(location);
					pointInPattern.setOrder(BigInteger.valueOf(i));
					NaturalLanguageStringStructure stopName = new NaturalLanguageStringStructure();
					stopName.setValue(stop.getName());
					pointInPattern.getStopName().add(stopName);
					pointInPattern.setStopPointRef(spr);

					stopsInPattern.getStopPointInPattern().add(pointInPattern);

					// HasUpcomingService Extension
					SiriUpcomingServiceExtension upcomingService = new SiriUpcomingServiceExtension();
					upcomingService.setUpcomingScheduledService(hasUpcomingScheduledService);

					ExtensionsStructure upcomingServiceExtensions = new ExtensionsStructure();
					upcomingServiceExtensions.setAny(upcomingService);
					pointInPattern.setExtensions(upcomingServiceExtensions);
				}
			}

			String includePolylineFilter = filters.get(Filters.INCLUDE_POLYLINES);
			if(includePolylineFilter != null && passFilter("true",includePolylineFilter)){
				// Polyline Extension
				SiriPolyLinesExtension polylines = new SiriPolyLinesExtension();
				for(String polyline : direction.getPolylines()){
					polylines.getPolylines().add(polyline);
				}

				ExtensionsStructure PolylineExtension = new ExtensionsStructure();
				PolylineExtension.setAny(polylines);
				routeDirectionStructure.setExtensions(PolylineExtension);
			}

			routeDirectionStructure.setJourneyPatterns(patterns);
			pattern.setStopsInPattern(stopsInPattern);
			patterns.getJourneyPattern().add(pattern);
			routeDirectionStructure.setDirectionRef(dirRefStructure);

		}

		return true;
	}


	/***
	 * PRIVATE STATIC METHODS
	 */
	private static void fillOnwardCalls(
			MonitoredVehicleJourneyStructure monitoredVehicleJourney,
			BlockInstanceBean blockInstance, TripBean framedJourneyTripBean,
			TripStatusBean currentVehicleTripStatus,
			OnwardCallsMode onwardCallsMode,
			PresentationService presentationService,
			NycTransitDataService nycTransitDataService,
			Map<String, SiriSupportPredictionTimepointRecord> stopLevelPredictions,
			int maximumOnwardCalls, long responseTimestamp) {

		String tripIdOfMonitoredCall = framedJourneyTripBean.getId();

		monitoredVehicleJourney.setOnwardCalls(new OnwardCallsStructure());

		// ////////

		// no need to go further if this is the case!
		if (maximumOnwardCalls == 0) {
			return;
		}

		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration()
				.getTrips();

		double distanceOfVehicleAlongBlock = 0;
		int blockTripStopsAfterTheVehicle = 0;
		int onwardCallsAdded = 0;

		boolean foundActiveTrip = false;
		for (int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if (!foundActiveTrip) {
				if (currentVehicleTripStatus.getActiveTrip().getId()
						.equals(blockTrip.getTrip().getId())) {
					distanceOfVehicleAlongBlock += currentVehicleTripStatus
							.getDistanceAlongTrip();

					foundActiveTrip = true;
				} else {
					// a block trip's distance along block is the *beginning* of
					// that block trip along the block
					// so to get the size of this one, we have to look at the
					// next.
					if (i + 1 < blockTrips.size()) {
						distanceOfVehicleAlongBlock = blockTrips.get(i + 1)
								.getDistanceAlongBlock();
					}

					// bus has already served this trip, so no need to go
					// further
					continue;
				}
			}

			if (onwardCallsMode == OnwardCallsMode.STOP_MONITORING) {
				// always include onward calls for the trip the monitored call
				// is on ONLY.
				if (!blockTrip.getTrip().getId().equals(tripIdOfMonitoredCall)) {
					continue;
				}
			}

			for (BlockStopTimeBean stopTime : blockTrip.getBlockStopTimes()) {

				// check for non-revenue stops for onward calls
				if(currentVehicleTripStatus.getActiveTrip().getRoute() != null) {
					String agencyId = currentVehicleTripStatus.getActiveTrip().getRoute().getAgency().getId();
					String routeId = currentVehicleTripStatus.getActiveTrip().getRoute().getId();
					String directionId = currentVehicleTripStatus.getActiveTrip().getDirectionId();
					String stopId  = stopTime.getStopTime().getStop().getId();
					if (!nycTransitDataService.stopHasRevenueServiceOnRoute(agencyId, stopId, routeId, directionId)){
						continue;
					}
				}

				// block trip stops away--on this trip, only after we've passed
				// the stop,
				// on future trips, count always.
				if (currentVehicleTripStatus.getActiveTrip().getId()
						.equals(blockTrip.getTrip().getId())) {
					if (stopTime.getDistanceAlongBlock() >= distanceOfVehicleAlongBlock) {
						blockTripStopsAfterTheVehicle++;
					} else {
						// stop is behind the bus--no need to go further
						continue;
					}

					// future trip--bus hasn't reached this trip yet, so count
					// all stops
				} else {
					blockTripStopsAfterTheVehicle++;
				}

				String stopPredictionKey = SiriSupportPredictionTimepointRecord
						.convertTripAndStopToKey(blockTrip.getTrip().getId(), stopTime.getStopTime().getStop().getId());

				monitoredVehicleJourney
				.getOnwardCalls()
				.getOnwardCall()
				.add(getOnwardCallStructure(
						stopTime.getStopTime().getStop(),
						presentationService,
						stopTime.getDistanceAlongBlock()
						- blockTrip.getDistanceAlongBlock(),
						stopTime.getDistanceAlongBlock()
						- distanceOfVehicleAlongBlock,
						 blockTripStopsAfterTheVehicle - 1,
						stopLevelPredictions.get(stopPredictionKey),
						responseTimestamp));

				onwardCallsAdded++;

				if (onwardCallsAdded >= maximumOnwardCalls) {
					return;
				}
			}

			// if we get here, we added our stops
			return;
		}

		return;
	}

	private static void fillMonitoredCall(
			MonitoredVehicleJourneyStructure monitoredVehicleJourney,
			BlockInstanceBean blockInstance, TripBean arrivalDepartureTrip, TripStatusBean tripStatus,
			StopBean monitoredCallStopBean,
			PresentationService presentationService,
			NycTransitDataService nycTransitDataService,
			Map<String, SiriSupportPredictionTimepointRecord> stopLevelPredictions,
			DetailLevel detailLevel,
			boolean showApc,
			boolean showRawApc,
			long responseTimestamp, boolean isCancelled) {

		List<BlockTripBean> blockTrips = blockInstance.getBlockConfiguration()
				.getTrips();

		double distanceOfVehicleAlongBlock = 0;
		int blockTripStopsAfterTheVehicle = 0;

		boolean foundActiveTrip = false;
		boolean foundArrivalDepartureTrip = false;

		for (int i = 0; i < blockTrips.size(); i++) {
			BlockTripBean blockTrip = blockTrips.get(i);

			if(foundActiveTrip != true){
				if(tripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
					if(!isCancelled) {
						distanceOfVehicleAlongBlock = blockTrip.getDistanceAlongBlock() + tripStatus.getDistanceAlongTrip();
					} else {
						distanceOfVehicleAlongBlock = blockTrip.getDistanceAlongBlock() + tripStatus.getScheduledDistanceAlongTrip();
					}
					foundActiveTrip = true;
				}
				else {
					continue;
				}
			}
			// Skip trips that don't match arrival departure (including active trip)
			if(foundActiveTrip && !foundArrivalDepartureTrip) {
				if(arrivalDepartureTrip.getId().equals(blockTrip.getTrip().getId())) {
					foundArrivalDepartureTrip = true;
					// If not active trip, don't show apc
					if(!tripStatus.getActiveTrip().getId().equals(blockTrip.getTrip().getId())) {
						showApc = false;
						showRawApc = false;
					}
				} else {
					// bus has already served this trip, so no need to go further
					continue;
				}
			}

			HashMap<String, Integer> visitNumberForStopMap = new HashMap<String, Integer>();

			for (BlockStopTimeBean stopTime : blockTrip.getBlockStopTimes()) {
				int visitNumber = getVisitNumber(visitNumberForStopMap, stopTime.getStopTime().getStop());

				// block trip stops away--on this trip, only after we've passed
				// the stop,
				// on future trips, count always.
				if (arrivalDepartureTrip.getId().equals(blockTrip.getTrip().getId())) {
					if (stopTime.getDistanceAlongBlock() >= distanceOfVehicleAlongBlock) {
						blockTripStopsAfterTheVehicle++;
					} else {
						// bus has passed this stop already--no need to go
						// further
						continue;
					}

					// future trip--bus hasn't reached this trip yet, so count
					// all stops
				} else {
					blockTripStopsAfterTheVehicle++;
				}

				// monitored call
				if (stopTime.getStopTime().getStop().getId().equals(monitoredCallStopBean.getId())) {
					if (!presentationService.isOnDetour(tripStatus)) {
						SiriSupportPredictionTimepointRecord ssptr = new SiriSupportPredictionTimepointRecord();
						String stopPredictionKey = ssptr.getKey(blockTrip.getTrip().getId(), stopTime.getStopTime().getStop().getId());
						monitoredVehicleJourney.setMonitoredCall(
							getMonitoredCallStructure(
								stopTime.getStopTime().getStop(),
								nycTransitDataService,
								presentationService,
								stopTime.getDistanceAlongBlock() - blockTrip.getDistanceAlongBlock(),
								stopTime.getDistanceAlongBlock() - distanceOfVehicleAlongBlock,
								visitNumber,
								blockTripStopsAfterTheVehicle - 1,
								stopLevelPredictions.get(stopPredictionKey),
								detailLevel,
								tripStatus.getVehicleId(),
								showApc,
								showRawApc,
								responseTimestamp,
								stopTime.getStopTime().getArrivalTime(), isCancelled)
						);

					}

					// we found our monitored call--stop
					return;
				}
			}
		}
	}

	private static void fillOccupancy(MonitoredVehicleJourneyStructure mvj, NycTransitDataService tds, TripStatusBean tripStatus) {
		if (tripStatus == null
				|| tripStatus.getActiveTrip() == null
				|| tripStatus.getActiveTrip().getRoute() ==  null) {
			return;
		}
		VehicleOccupancyRecord vor =
				tds.getVehicleOccupancyRecordForVehicleIdAndRoute(
						AgencyAndId.convertFromString(tripStatus.getVehicleId()),
						tripStatus.getActiveTrip().getRoute().getId(),
						tripStatus.getActiveTrip().getDirectionId());
		mvj.setOccupancy(mapOccupancyStatusToEnumeration(vor));
	}

	private static OccupancyEnumeration mapOccupancyStatusToEnumeration(VehicleOccupancyRecord vor) {
		if (vor == null || vor.getOccupancyStatus() == null) return null;
		switch (vor.getOccupancyStatus()) {
			case UNKNOWN:
				return null;
			case EMPTY:
			case MANY_SEATS_AVAILABLE:
			case FEW_SEATS_AVAILABLE:
				return OccupancyEnumeration.SEATS_AVAILABLE;
			case STANDING_ROOM_ONLY:
				return OccupancyEnumeration.STANDING_AVAILABLE;
			case FULL:
			case CRUSHED_STANDING_ROOM_ONLY:
			case NOT_ACCEPTING_PASSENGERS:
				return OccupancyEnumeration.FULL;
			default:
				return null;
		}
	}

	private static void fillSituations(
			MonitoredVehicleJourneyStructure monitoredVehicleJourney,
			TripStatusBean tripStatus) {
		if (tripStatus == null || tripStatus.getSituations() == null
				|| tripStatus.getSituations().isEmpty()) {
			return;
		}

		Set<String> uniqueSituationId = new HashSet<>();

		List<SituationRefStructure> situationRef = monitoredVehicleJourney
				.getSituationRef();

		for (ServiceAlertBean situation : tripStatus.getSituations()) {
			String situationId = situation.getId();
			if(uniqueSituationId.contains(situationId)){
				continue;
			}
			SituationRefStructure sitRef = new SituationRefStructure();
			SituationSimpleRefStructure sitSimpleRef = new SituationSimpleRefStructure();
			sitSimpleRef.setValue(situationId);
			sitRef.setSituationSimpleRef(sitSimpleRef);
			situationRef.add(sitRef);
			uniqueSituationId.add(situationId);
		}
	}

	private static OnwardCallStructure getOnwardCallStructure(
			StopBean stopBean, PresentationService presentationService,
			double distanceOfCallAlongTrip, double distanceOfVehicleFromCall,
			int index, SiriSupportPredictionTimepointRecord prediction,
			long responseTimestamp) {

		OnwardCallStructure onwardCallStructure = new OnwardCallStructure();

		StopPointRefStructure stopPointRef = new StopPointRefStructure();
		stopPointRef.setValue(stopBean.getId());
		onwardCallStructure.setStopPointRef(stopPointRef);

		NaturalLanguageStringStructure stopPoint = new NaturalLanguageStringStructure();
		stopPoint.setValue(stopBean.getName());
		onwardCallStructure.getStopPointName().add(stopPoint);

		boolean isNearFirstStop = false;
		if (distanceOfCallAlongTrip < 100) isNearFirstStop = true;

		if(prediction != null) {
			if (prediction.getTimepointPredictionRecord().getTimepointPredictedArrivalTime() < responseTimestamp) {
				if (!isNearFirstStop) { onwardCallStructure.setExpectedArrivalTime(DateUtil.toXmlGregorianCalendar(responseTimestamp));}
				else {
					onwardCallStructure.setExpectedDepartureTime(DateUtil.toXmlGregorianCalendar(responseTimestamp));
				}
			} else {
				if (!isNearFirstStop) {	onwardCallStructure.setExpectedArrivalTime(DateUtil.toXmlGregorianCalendar(prediction.getTimepointPredictionRecord().getTimepointPredictedArrivalTime()));}
				else {
					onwardCallStructure.setExpectedDepartureTime(DateUtil.toXmlGregorianCalendar(prediction.getTimepointPredictionRecord().getTimepointPredictedDepartureTime()));
				}
			}
		}

		// Distances
		NaturalLanguageStringStructure presentableDistance = new NaturalLanguageStringStructure();
		presentableDistance.setValue(presentationService
				.getPresentableDistance(distanceOfVehicleFromCall, index));

		// NOTE! now included in the specification, formerly an extension
		onwardCallStructure.setNumberOfStopsAway(BigInteger.valueOf(index));
		onwardCallStructure.setDistanceFromStop(new BigDecimal(distanceOfVehicleFromCall).toBigInteger());
		onwardCallStructure.setArrivalProximityText(presentableDistance);

		return onwardCallStructure;
	}

	private static MonitoredCallStructure getMonitoredCallStructure(
			StopBean stopBean,
			NycTransitDataService nycTransitDataService,
			PresentationService presentationService,
			double distanceOfCallAlongTrip,
			double distanceOfVehicleFromCall,
			int visitNumber,
			int index,
			SiriSupportPredictionTimepointRecord prediction,
			DetailLevel detailLevel,
			String vehicleId,
			boolean showApc,
			boolean showRawApc,
			long responseTimestamp,
			int arrivalTime,
			boolean isCancelled) {

		MonitoredCallStructure monitoredCallStructure = new MonitoredCallStructure();
		monitoredCallStructure.setVisitNumber(BigInteger.valueOf(visitNumber));

		StopPointRefStructure stopPointRef = new StopPointRefStructure();
		stopPointRef.setValue(stopBean.getId());


		NaturalLanguageStringStructure stopPoint = new NaturalLanguageStringStructure();
		stopPoint.setValue(stopBean.getName());

		if(prediction != null) {
			fillExpectedArrivalDepartureTimes(monitoredCallStructure,
					prediction.getTimepointPredictionRecord().getTimepointPredictedArrivalTime(),
					prediction.getTimepointPredictionRecord().getTimepointPredictedDepartureTime(),
					responseTimestamp);
		}

		GregorianCalendar calendar = new GregorianCalendar(); // gets a calendar using the default time zone and locale.
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		calendar.set(year, month, day, 0, 0, 0);
		calendar.add(Calendar.SECOND, arrivalTime);
		monitoredCallStructure.setAimedArrivalTime(DateUtil.toXmlGregorianCalendar(calendar));

		// NOTE!  distances have been moved into spec!

		if (vehicleId != null) {
			VehicleOccupancyRecord vor =
					nycTransitDataService.getLastVehicleOccupancyRecordForVehicleId(AgencyAndId.convertFromString(vehicleId));

			if (showRawApc && vor != null && vor.getCapacity() != null && vor.getRawCount() != null) {
				// siri extensions
				SiriExtensionWrapper wrapper = new SiriExtensionWrapper();

				ExtensionsStructure anyExtensions = new ExtensionsStructure();
				SiriApcExtension apcExtension = presentationService.getPresentableApc(vor);
				if (apcExtension != null) {
					wrapper.setCapacities(apcExtension);
					anyExtensions.setAny(wrapper);
					monitoredCallStructure.setExtensions(anyExtensions);
				}
			}
		}



		// distances -- formerly an extension but now in spec
		NaturalLanguageStringStructure presentableDistance = new NaturalLanguageStringStructure();
		presentableDistance.setValue(presentationService
				.getPresentableDistance(distanceOfVehicleFromCall, index));

		monitoredCallStructure.setNumberOfStopsAway(BigInteger.valueOf(index));
		if(!isCancelled) {
			monitoredCallStructure.setDistanceFromStop(new BigDecimal(distanceOfVehicleFromCall).toBigInteger());
			monitoredCallStructure.setArrivalProximityText(presentableDistance);
		}



		// basic 
		if (detailLevel.equals(DetailLevel.BASIC)|| detailLevel.equals(DetailLevel.NORMAL) || detailLevel.equals(DetailLevel.CALLS)){
			monitoredCallStructure.getStopPointName().add(stopPoint);
		}

		// normal
		if(detailLevel.equals(DetailLevel.NORMAL) || detailLevel.equals(DetailLevel.CALLS)){
			monitoredCallStructure.setStopPointRef(stopPointRef);
		}

		return monitoredCallStructure;
	}

	private static void fillExpectedArrivalDepartureTimes(MonitoredCallStructure monitoredCallStructure,
												   long arrivalTime,
												   long departureTime,
												   long responseTimestamp){

		// Both arrival and departure time are in past
		if (arrivalTime < responseTimestamp && departureTime < responseTimestamp) {
			monitoredCallStructure.setExpectedArrivalTime(DateUtil
					.toXmlGregorianCalendar(responseTimestamp + 1000));
			monitoredCallStructure.setExpectedDepartureTime(DateUtil
					.toXmlGregorianCalendar(responseTimestamp + 1000));
		}
		// arrival time undefined and departure time in the future
		else if(arrivalTime < 0 && departureTime > responseTimestamp ) {
			monitoredCallStructure.setExpectedArrivalTime(DateUtil
					.toXmlGregorianCalendar(departureTime));
			monitoredCallStructure.setExpectedDepartureTime(DateUtil
					.toXmlGregorianCalendar(departureTime));
		}
		// arrival time
		else if(arrivalTime < responseTimestamp){
			monitoredCallStructure.setExpectedArrivalTime(DateUtil
					.toXmlGregorianCalendar(responseTimestamp + 1000));
			monitoredCallStructure.setExpectedDepartureTime(DateUtil
					.toXmlGregorianCalendar(departureTime));
		}
		else if(departureTime < responseTimestamp){
			monitoredCallStructure.setExpectedArrivalTime(DateUtil
					.toXmlGregorianCalendar(arrivalTime));
			monitoredCallStructure.setExpectedDepartureTime(DateUtil
					.toXmlGregorianCalendar(arrivalTime));
		}
		else {
			monitoredCallStructure.setExpectedArrivalTime(DateUtil
					.toXmlGregorianCalendar(arrivalTime));
			monitoredCallStructure.setExpectedDepartureTime(DateUtil
					.toXmlGregorianCalendar(departureTime));
		}
	}

	private static int getVisitNumber(
			HashMap<String, Integer> visitNumberForStop, StopBean stop) {
		int visitNumber;

		if (visitNumberForStop.containsKey(stop.getId())) {
			visitNumber = visitNumberForStop.get(stop.getId()) + 1;
		} else {
			visitNumber = 1;
		}

		visitNumberForStop.put(stop.getId(), visitNumber);

		return visitNumber;
	}

	private static ProgressRateEnumeration getProgressRateForPhaseAndStatus(
			String status, String phase) {
		if (phase == null) {
			return ProgressRateEnumeration.UNKNOWN;
		}

		if (phase.toLowerCase().startsWith("layover")
				|| phase.toLowerCase().startsWith("deadhead")
				|| phase.toLowerCase().equals("at_base")) {
			return ProgressRateEnumeration.NO_PROGRESS;
		}

		if (status != null && status.toLowerCase().equals("stalled")) {
			return ProgressRateEnumeration.NO_PROGRESS;
		}

		if (phase.toLowerCase().equals("in_progress")) {
			return ProgressRateEnumeration.NORMAL_PROGRESS;
		}

		return ProgressRateEnumeration.UNKNOWN;
	}


	public static boolean passFilter(String value, String filterValue){
		if (StringUtils.isNotBlank(filterValue)
				&& !value.equalsIgnoreCase(filterValue.trim()))
			return false;

		return true;
	}

	public static Integer convertToNumeric(String param, Integer defaultValue){
		Integer numericValue = defaultValue;
		try {
			numericValue = Integer.parseInt(param);
		} catch (NumberFormatException e) {
			numericValue = defaultValue;
		}
		return numericValue;
	}

}
