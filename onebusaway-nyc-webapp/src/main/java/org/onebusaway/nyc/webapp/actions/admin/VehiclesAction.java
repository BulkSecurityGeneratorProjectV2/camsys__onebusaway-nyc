/**
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
package org.onebusaway.nyc.webapp.actions.admin;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.onebusaway.nyc.transit_data.model.NycVehicleManagementStatusBean;
import org.onebusaway.nyc.transit_data.services.ConfigurationService;
import org.onebusaway.nyc.transit_data.services.VehicleTrackingManagementService;
import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCActionSupport;
import org.onebusaway.nyc.webapp.actions.admin.model.VehicleModel;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.services.TransitDataService;
import org.springframework.beans.factory.annotation.Autowired;

@Results({@Result(type = "redirectAction", name = "redirect", params = {
    "namespace", "/admin", "actionName", "vehicles"})})
public class VehiclesAction extends OneBusAwayNYCActionSupport implements ServletRequestAware {

  private static final long serialVersionUID = 1L;

  @Autowired
  private ConfigurationService _configurationService;

  @Autowired
  private VehicleTrackingManagementService _vehicleTrackingManagementService;

  @Autowired
  private TransitDataService transitService;

  private List<VehicleModel> vehicles = new ArrayList<VehicleModel>();

  private HttpServletRequest request;

  @Override
  public void setServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public String getCurrentTimestamp() {
	Date now = new Date();
	return DateFormat.getDateInstance().format(now) + " " + DateFormat.getTimeInstance().format(now);
  }
  
  @Override
  public String execute() throws Exception {

	ListBean<VehicleStatusBean> vehiclesForAgencyListBean = transitService.getAllVehiclesForAgency("MTA NYCT", System.currentTimeMillis());

	Map<String, VehicleStatusBean> vehicleMap = new HashMap<String, VehicleStatusBean>();
    for (VehicleStatusBean vehicleStatusBean : vehiclesForAgencyListBean.getList()) {
        String vehicleId = vehicleStatusBean.getVehicleId();
        vehicleMap.put(vehicleId, vehicleStatusBean);
    }	
	
	List<NycVehicleManagementStatusBean> nycVehicleStatusBeans = _vehicleTrackingManagementService.getAllVehicleManagementStatusBeans();

	String method = request.getMethod().toUpperCase();

	// disable vehicle form submitted
	if (method.equals("POST")) {
		Set<String> disabledVehicles = new HashSet<String>();

		Enumeration<?> parameterNames = request.getParameterNames();
		while(parameterNames.hasMoreElements()) {
			String key = parameterNames.nextElement().toString();
			if(key.startsWith("disable_")) {
				String vehicleId = key.substring("disable_".length()).replace("^", " ");
				disabledVehicles.add(vehicleId);
			}
		}

		for(NycVehicleManagementStatusBean vehicle : nycVehicleStatusBeans) {
			String vehicleId = vehicle.getVehicleId();
			if(disabledVehicles.contains(vehicleId)) {
		          _vehicleTrackingManagementService.setVehicleStatus(vehicleId, false);
			} else {
		          _vehicleTrackingManagementService.setVehicleStatus(vehicleId, true);
			}
		}

		return "redirect";
    }

	for(NycVehicleManagementStatusBean vehicleBean : nycVehicleStatusBeans) {
		VehicleModel v = new VehicleModel(vehicleBean, 
										vehicleMap.get(vehicleBean.getVehicleId()),
										_configurationService);
		vehicles.add(v);
	}
    
    return SUCCESS;
  }

  public List<VehicleModel> getVehicles() {
    return vehicles;
  }
}