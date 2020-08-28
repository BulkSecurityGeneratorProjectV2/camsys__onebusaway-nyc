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

package org.onebusaway.nyc.webapp.actions.m.model;

import org.onebusaway.transit_data.model.RouteBean;

import java.util.List;
import java.util.Set;

/**
 * Route available at a stop, the stop being the top-level result.
 * @author jmaki
 *
 */
public class RouteAtStop {

  private RouteBean route;
  
  private List<RouteDirection> directions;
  
  private Set<String> serviceAlerts;

  private Boolean hasRealtime = true;

  public RouteAtStop(RouteBean route, List<RouteDirection> directions, Set<String> serviceAlerts,  Boolean hasRealtime) {
    this.route = route;
    this.directions = directions;
    this.serviceAlerts = serviceAlerts;
    this.hasRealtime = hasRealtime;
  }

  public RouteAtStop(RouteBean route, List<RouteDirection> directions, Set<String> serviceAlerts) {
    this.route = route;
    this.directions = directions;
    this.serviceAlerts = serviceAlerts;
  }
  
  public String getId() {
    return route.getId();
  }
  
  public String getShortName() {
    return route.getShortName();
  }
  
  public Set<String> getServiceAlerts() {
    return serviceAlerts;
  }
  
  public List<RouteDirection> getDirections() {
    return directions;
  }

  public Boolean getHasRealtime() { return hasRealtime; }

  public void setHasRealtime(Boolean hasRealtime) {this.hasRealtime = hasRealtime; }

}
