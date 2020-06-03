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

package org.onebusaway.nyc.webapp.actions.api.siri.model;

import java.util.ArrayList;
import java.util.List;

import org.onebusaway.transit_data.model.StopBean;

public class StopRouteDirection {
	
	private StopBean stopBean;
	
	private List<RouteForDirection> routeDirections = new ArrayList<RouteForDirection>();
	
	public StopRouteDirection(StopBean stopBean){
		this.setStop(stopBean);
	}
	
	public StopRouteDirection(StopBean stopBean, RouteForDirection routeDirection){
		this.setStop(stopBean);
		routeDirections.add(routeDirection);
	}

	public void addRouteDirection(RouteForDirection routeDirection) {
		routeDirections.add(routeDirection);
	}
	
	public List<RouteForDirection> getRouteDirections() {
		return routeDirections;
	}

	public StopBean getStop() {
		return stopBean;
	}

	public void setStop(StopBean stopBean) {
		this.stopBean = stopBean;
	}


}
