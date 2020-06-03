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

package org.onebusaway.nyc.transit_data_manager.adapters.output.json;

import org.onebusaway.nyc.transit_data_manager.adapters.ModelCounterpartConverter;
import org.onebusaway.nyc.transit_data_manager.adapters.output.model.json.PullInOut;
import org.onebusaway.nyc.transit_data_manager.adapters.output.model.json.Vehicle;
import org.onebusaway.nyc.transit_data_manager.adapters.output.model.json.VehiclePullInOutInfo;
import org.springframework.beans.factory.annotation.Autowired;

import tcip_final_3_0_5_1.CPTVehicleIden;
import tcip_final_3_0_5_1.SCHPullInOutInfo;

/**
 * Converts tcip pull out object to a model object that can be serialized by JSON.
 * @author abelsare
 *
 */
public class PullInOutFromTcip implements
    ModelCounterpartConverter<VehiclePullInOutInfo, PullInOut> {

	private ModelCounterpartConverter<CPTVehicleIden, Vehicle> vehConv;

	public PullInOut convert(VehiclePullInOutInfo input) {

		PullInOut movement = new PullInOut();
		SCHPullInOutInfo pulloutInfo = input.getPullOutInfo();
		SCHPullInOutInfo pullinInfo = input.getPullInInfo();

		Vehicle vehicle = vehConv.convert(pulloutInfo.getVehicle());
		movement.setVehicleId(vehicle.getVehicleId());

		movement.setAgencyIdTcip(pulloutInfo.getVehicle().getAgencyId().toString());
		
		movement.setAgencyId(pulloutInfo.getGarage().getAgencydesignator());
		
		movement.setDepot(pulloutInfo.getGarage().getFacilityName());

		movement.setServiceDate(pulloutInfo.getDate());
		
		movement.setPulloutTime(pulloutInfo.getTime());
		
		movement.setRun(pulloutInfo.getRun().getDesignator());

		movement.setOperatorId(String.valueOf(pulloutInfo.getOperator().getOperatorId()));
		
		movement.setPullinTime(pullinInfo.getTime());

		return movement;
	}

	/**
	 * Injects {@link VehicleFromTcip}
	 * @param vehConv the vehConv to set
	 */
	@Autowired
	public void setVehConv(
			ModelCounterpartConverter<CPTVehicleIden, Vehicle> vehConv) {
		this.vehConv = vehConv;
	}

}
