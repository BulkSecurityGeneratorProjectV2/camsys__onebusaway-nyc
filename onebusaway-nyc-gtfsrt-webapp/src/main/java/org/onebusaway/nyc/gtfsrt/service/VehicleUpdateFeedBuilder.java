package org.onebusaway.nyc.gtfsrt.service;

import com.google.transit.realtime.GtfsRealtime;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;

public interface VehicleUpdateFeedBuilder {

    GtfsRealtime.VehiclePosition getVehicleUpdateFromInferredLocation(VehicleLocationRecordBean record);
}
