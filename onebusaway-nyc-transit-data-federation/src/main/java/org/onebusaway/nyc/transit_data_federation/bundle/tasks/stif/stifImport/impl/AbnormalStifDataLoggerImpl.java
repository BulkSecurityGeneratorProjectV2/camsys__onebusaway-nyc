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

package org.onebusaway.nyc.transit_data_federation.bundle.tasks.stif.stifImport.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.transit_data_federation.bundle.tasks.MultiCSVLogger;
import org.onebusaway.nyc.transit_data_federation.bundle.tasks.stif.stifImport.StifTrip;

public class AbnormalStifDataLoggerImpl {
	
	  private MultiCSVLogger csvLogger = null;
	  
	  public void setLogger(MultiCSVLogger logger) {
	    this.csvLogger = logger;
	    
	    try{
			createHeaders();
		}catch(RuntimeException e){
			e.printStackTrace();
			//We may create many classes of this type but the headers will throw an exception if the file already exists
			//this will just ignore the issue, since it isn't an issue that we care about.
		}
	  }
	  
	  public MultiCSVLogger getLogger(){
		  return this.csvLogger;
	  }
	  
	  public void header(String file, String header){
		  csvLogger.header(file, header);
	  }
	  
	  public void log(String file, Object... args){
		  csvLogger.log(file, args);
	  }
	  
	  public void createHeaders() throws RuntimeException{
		  csvLogger.header("non_pullin_without_next_movement.csv", "stif_trip,stif_filename,stif_trip_record_line_num");
		  csvLogger.header(
		        "stif_trips_without_pullout.csv",
		        "stif_trip,stif_filename,stif_trip_record_line_num,gtfs_trip_id,synthesized_block_id");
		  csvLogger.header("matched_trips_gtfs_stif.csv", "agency_id,gtfs_service_id,service_id,blockId,tripId,dsc,firstStop,"+
		        "firstStopTime,lastStop,lastStopTime,runId,reliefRunId,recoveryTime,firstInSeq,lastInSeq,signCodeRoute,routeId,busType");
		  csvLogger.header("dsc_statistics.csv", "dsc,agency_id,number_of_trips_in_stif,number_of_distinct_route_ids_in_gtfs");
		  csvLogger.header("gtfs_trips_matched_with_multiple_stif_trips.csv","GTFS_Trip_Id, extra_stif_trip,stif_filename,stif_trip_record_line_num");
		  csvLogger.header("incorrect_gtfs_stif_mismatches_by_Service_Id.csv", "stif_trip,stif_filename,stif_trip_record_line_num, Gtfs_Trip_1, Gtfs_Trip_2, Gtfs_Trip_3, Gtfs_Trip_4...");
		  csvLogger.header("stif_gtfs_stoptime_mismatch.csv", "stifTrip.path, stifTrip, stifStop, stifId, gtfsTrip, gtfsStop, gtfsId");
		  csvLogger.header("trips_with_failed_stif_to_gtfs_stop_id_matching.csv", "stifTrip.path, stifTrip, gtfsTrip");
		  csvLogger.header("non-revenue_gtfs_stoptimes_updated.csv", "associatedStifTrip.path, associatedStifTrip, associatedStifStop, associatedStifId, gtfsTrip, gtfsStop, gtfsId");
	  }
	
	  // package private for unit testing
	  public void logDSCStatistics(Map<String, List<AgencyAndId>> dscToTripMap,
	      Map<AgencyAndId, String> tripToDscMap, HashMap<String, Set<AgencyAndId>> routeIdsByDsc) {
	    for (Map.Entry<String, List<AgencyAndId>> entry : dscToTripMap.entrySet()) {
	      String destinationSignCode = entry.getKey();
	      List<AgencyAndId> tripIds = entry.getValue();

	      Set<AgencyAndId> routeIds = routeIdsByDsc.get(destinationSignCode);
	      HashSet<String> set = new HashSet<String>();
	      for (AgencyAndId aaid : tripIds){
	        if (aaid != null){
	          set.add(aaid.getAgencyId());
	        }
	      }
	      for (String agencyId : set){
	        csvLogger.log("dsc_statistics.csv", destinationSignCode,agencyId, tripIds.size(),(routeIds != null ? routeIds.size() : 0));
	      }
	    }
	  }
	  
	  /**
	   * Dump some raw block matching data to a CSV file from stif trips
	   */
	  public void dumpBlockDataForTrip(StifTrip trip, String gtfsServiceId,
	      String tripId, String blockId, String routeId) {
		  
	    csvLogger.log("matched_trips_gtfs_stif.csv", trip.agencyId,
	        gtfsServiceId, trip.serviceCode, blockId, tripId, trip.getDsc(), trip.firstStop,
	        trip.firstStopTime, trip.lastStop, trip.lastStopTime, trip.runId,
	        trip.reliefRunId, trip.recoveryTime, trip.firstTripInSequence,
	        trip.lastTripInSequence, trip.getSignCodeRoute(), routeId, trip.busType);
	  }
	  
	
}
