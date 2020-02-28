package org.onebusaway.nyc.webapp.actions.m.model;

import org.onebusaway.transit_data.model.StopBean;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;

import java.util.List;

/**
 * A stop on a route, the route being the top-level search result.
 * @author jmaki
 *
 */
public class StopOnRoute {

  private StopBean stop;
  
  private List<String> distanceAways;

  private List<String> vehicleIds; // in approximate order with distanceAways;

  private Boolean hasRealtime = true;

  public StopOnRoute(StopBean stop, List<String> distanceAways, Boolean hasRealtime, List<String> vehicleIds) {
    this.stop = stop;
    this.distanceAways = distanceAways;
    if(hasRealtime != null)
      this.hasRealtime = hasRealtime;
    this.vehicleIds = vehicleIds;
  }
  public StopOnRoute(StopBean stop, List<String> distanceAways) {
    this.stop = stop;
    this.distanceAways = distanceAways;
  }
  
  public String getId() {
    return stop.getId();
  }
  
  public String getIdWithoutAgency() {
    return AgencyAndIdLibrary.convertFromString(getId()).getId();
  }
  
  public String getName() {
    return stop.getName();
  }
  
  public List<String> getDistanceAways() {
    return distanceAways;
  }

  public Boolean getHasRealtime() {
    return hasRealtime;
  }

  public void setHasRealtime(Boolean hasRealtime) {
    this.hasRealtime = hasRealtime;
  }

}
