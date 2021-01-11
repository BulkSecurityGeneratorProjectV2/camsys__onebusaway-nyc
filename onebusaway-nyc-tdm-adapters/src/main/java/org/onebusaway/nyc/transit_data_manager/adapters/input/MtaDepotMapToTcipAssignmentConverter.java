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

package org.onebusaway.nyc.transit_data_manager.adapters.input;

import java.util.List;

import org.onebusaway.nyc.transit_data_manager.adapters.input.model.MtaBusDepotAssignment;
import org.onebusaway.nyc.transit_data_manager.adapters.tools.DepotIdTranslator;

import tcip_final_3_0_5_1.CPTFleetSubsetGroup;
import tcip_final_3_0_5_1.CPTFleetSubsetGroup.GroupMembers;
import tcip_final_3_0_5_1.CPTTransitFacilityIden;
import tcip_final_3_0_5_1.CPTVehicleIden;

public class MtaDepotMapToTcipAssignmentConverter {

  private static String DATASOURCE_SYSTEM = "SPEAR";
  
  public MtaDepotMapToTcipAssignmentConverter() {
  }
  
  private DepotIdTranslator depotIdTranslator = null;

  /***
   * 
   * @param list
   * @return
   */
  public CPTFleetSubsetGroup ConvertToOutput(String mtaSourceDepotIdStr,
      List<MtaBusDepotAssignment> sourceMtaBusDepotAssigns) {

    CPTFleetSubsetGroup outputGroup = new CPTFleetSubsetGroup();

    outputGroup.setGroupId(new Long(0));
    outputGroup.setGroupName(getMappedId(mtaSourceDepotIdStr));

    // Add the group-garage block
    CPTTransitFacilityIden depotFacility = new CPTTransitFacilityIden();
    depotFacility.setFacilityId(new Long(0));
    depotFacility.setFacilityName(getMappedId(mtaSourceDepotIdStr));
    outputGroup.setGroupGarage(depotFacility);

    outputGroup.setGroupMembers(generateGroupMembers(sourceMtaBusDepotAssigns));

    return outputGroup;
  }

  private GroupMembers generateGroupMembers(
      List<MtaBusDepotAssignment> mtaBDAssigns) {
    GroupMembers gMembers = new GroupMembers();

    for (MtaBusDepotAssignment mtaSourceDepotAssignment : mtaBDAssigns) {
      gMembers.getGroupMember().add(makeTcipVehicleFromMtaAssignment(mtaSourceDepotAssignment));
    }

    return gMembers;
  }
  
  private CPTVehicleIden makeTcipVehicleFromMtaAssignment (MtaBusDepotAssignment mtaAssignment) {
    CPTVehicleIden vehicle = new CPTVehicleIden();
    
    vehicle.setAgencyId(mtaAssignment.getAgencyId());
    vehicle.setVehicleId(mtaAssignment.getBusNumber());
    
    return vehicle;
  }
  
  private String getMappedId(String fromId) {
    if (depotIdTranslator != null) {
      return depotIdTranslator.getMappedId(DATASOURCE_SYSTEM, fromId);
    } else {
      return fromId;
    }
  }
  
  public void setDepotIdTranslator(DepotIdTranslator depotIdTranslator) {
    this.depotIdTranslator = depotIdTranslator; 
  }
}
