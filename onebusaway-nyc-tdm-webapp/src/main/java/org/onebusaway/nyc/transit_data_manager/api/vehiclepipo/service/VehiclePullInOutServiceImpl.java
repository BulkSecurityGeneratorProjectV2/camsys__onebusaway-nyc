/**
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

package org.onebusaway.nyc.transit_data_manager.api.vehiclepipo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.nyc.transit_data_manager.adapters.output.model.json.VehiclePullInOutInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link VehiclePullInOutService}
 * @author abelsare
 *
 */
public class VehiclePullInOutServiceImpl implements VehiclePullInOutService {

	private Logger log = LoggerFactory.getLogger(VehiclePullInOutServiceImpl.class);
	private static DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();
	
	@Override
	public List<VehiclePullInOutInfo> getActivePullOuts(
			List<VehiclePullInOutInfo> allPullouts, boolean includeAllPullouts) {
		List<VehiclePullInOutInfo> activePullouts = new ArrayList<VehiclePullInOutInfo>();

		//group pullout data by bus
		Map<Long, List<VehiclePullInOutInfo>> pulloutsByBus = buildPulloutDataByBus(allPullouts);

		if(includeAllPullouts) {
			for(List<VehiclePullInOutInfo> pullouts : pulloutsByBus.values()) {
				//Check all pullouts for the bus and add only active ones to the list 
				for(VehiclePullInOutInfo pullout : pullouts) {
					if(isActive(pullout.getPullOutInfo().getTime())) {
						activePullouts.add(pullout);
					}
				}
			}
		} else {
			for(List<VehiclePullInOutInfo> pullouts : pulloutsByBus.values()) {
				//Check for active pullouts
				List<VehiclePullInOutInfo> allActivePulloutsByBus = new ArrayList<VehiclePullInOutInfo>();
				for(VehiclePullInOutInfo pullout : pullouts) {
					if(isActive(pullout.getPullOutInfo().getTime())) {
						allActivePulloutsByBus.add(pullout);
					}
				}
				//Get the most recent active pullout by sorting records by descending pullout time
				Collections.sort(allActivePulloutsByBus, new PulloutsComparator());
				if(!allActivePulloutsByBus.isEmpty()) {
					activePullouts.add(allActivePulloutsByBus.get(0));
				}
			}
		}

		return activePullouts;
	}

	@Override
	public VehiclePullInOutInfo getMostRecentActivePullout(	List<VehiclePullInOutInfo> activePullouts) {
		DateTimeFormatter format = ISODateTimeFormat.dateTimeNoMillis();
		VehiclePullInOutInfo mostRecentActivePullout;

		if(activePullouts.isEmpty()) {
			mostRecentActivePullout = null;
			log.debug("Call to getMostRecentActivePullout with empty list");
		} else {
			mostRecentActivePullout = activePullouts.get(0);
			//Loop through active pull out list to get pull out with the latest time
			for(VehiclePullInOutInfo currentActivePullout : activePullouts) {
				DateTime currentActivepullOutTime = format.parseDateTime(
						currentActivePullout.getPullOutInfo().getTime());
				DateTime mostRecentActivepullOutTime = format.parseDateTime(
						mostRecentActivePullout.getPullOutInfo().getTime());
				if(currentActivepullOutTime.isAfter(mostRecentActivepullOutTime)) {
					mostRecentActivePullout = currentActivePullout;
				}
			}
		}
		return mostRecentActivePullout;
	}
	
	private Map<Long, List<VehiclePullInOutInfo>> buildPulloutDataByBus(
			List<VehiclePullInOutInfo> allPullouts) {
		Map<Long, List<VehiclePullInOutInfo>> pulloutsByBus = new HashMap<Long, List<VehiclePullInOutInfo>>();

		//Group pullout data by bus number
		for(VehiclePullInOutInfo currentPullout : allPullouts) {
			//Check if the pullout record has required information
			if(currentPullout.getPullOutInfo() != null) {
				Long vehicleId = currentPullout.getPullOutInfo().getVehicle().getVehicleId();
				if(pulloutsByBus.containsKey(vehicleId)) {
					List<VehiclePullInOutInfo> existingPullouts = pulloutsByBus.get(vehicleId);
					existingPullouts.add(currentPullout);
				} else {
					List<VehiclePullInOutInfo> currentPullouts = new ArrayList<VehiclePullInOutInfo>();
					currentPullouts.add(currentPullout);
					pulloutsByBus.put(vehicleId, currentPullouts);
				}
			}
		}
		return pulloutsByBus;
	}
	
	// allow sub-classing for unit tests
	protected boolean isActive(String pullOuttimeString) {
		DateTime pullOutTime = format.parseDateTime(pullOuttimeString);
		boolean activePullOut = pullOutTime.isBeforeNow();
		return activePullOut;
	}
	
	private class PulloutsComparator implements Comparator<VehiclePullInOutInfo> {

		@Override
		public int compare(VehiclePullInOutInfo o1, VehiclePullInOutInfo o2) {
			DateTime pulloutTime1 = format.parseDateTime(o1.getPullOutInfo().getTime());
			DateTime pulloutTime2 = format.parseDateTime(o2.getPullOutInfo().getTime());
			return pulloutTime2.compareTo(pulloutTime1);
		}
		
	}

}
