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

package org.onebusaway.nyc.transit_data_manager.adapters.tcip;

import java.util.Iterator;
import java.util.List;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.onebusaway.nyc.transit_data_manager.adapters.tools.UtsMappingTool;

import tcip_3_0_5_local.CPTFileIdentifier;
import tcip_final_3_0_5_1.CPTPushHeader;
import tcip_final_3_0_5_1.SCHOperatorAssignment;
import tcip_final_3_0_5_1.SchPushOperatorAssignments;

public class PushOperatorAssignsGenerator {

  private DateTime nowDateTime;
  private DateMidnight headerEffectiveDate;
  private UtsMappingTool mappingTool = null;

  public PushOperatorAssignsGenerator(DateMidnight headerEffectiveDate) {
    super();
    this.nowDateTime = new DateTime();
    this.headerEffectiveDate = headerEffectiveDate;
    this.mappingTool = new UtsMappingTool();
  }

  public SchPushOperatorAssignments generateFromOpAssignList(
      List<SCHOperatorAssignment> opAssignList) {

    SchPushOperatorAssignments resultOpAssigns = new SchPushOperatorAssignments();

    resultOpAssigns.setPushHeader(generatePushHeader());
    resultOpAssigns.setAssignments(generateAssignments(opAssignList));

//    resultOpAssigns.setCreated(value);
//    resultOpAssigns.setSchVersion(value);
//    resultOpAssigns.setSourceapp(value);
//    resultOpAssigns.setSourceip(value);
//    resultOpAssigns.setSourceport(value);
//    resultOpAssigns.setNoNameSpaceSchemaLocation(value);
    
    return resultOpAssigns;
  }

  private CPTPushHeader generatePushHeader() {
    CPTPushHeader ph = new CPTPushHeader();

    ph.setFileType("3");
    ph.setEffective(mappingTool.dateTimeToXmlDatetimeFormat(headerEffectiveDate));
    ph.setSource(0);
    ph.setUpdatesOnly(false);
    ph.setUpdatesThru(mappingTool.dateTimeToXmlDatetimeFormat(nowDateTime));
    ph.setTimeSent(mappingTool.dateTimeToXmlDatetimeFormat(nowDateTime));

    return ph;
  }

  private SchPushOperatorAssignments.Assignments generateAssignments(
      List<SCHOperatorAssignment> assignmentList) {
    SchPushOperatorAssignments.Assignments assignmentsBlock = new SchPushOperatorAssignments.Assignments();

    // iterate over assignmentList and add each element using
    // assignmentsBlock.getAssignment().add()
    Iterator<SCHOperatorAssignment> itr = assignmentList.iterator();
    while (itr.hasNext()) {
      assignmentsBlock.getAssignment().add(itr.next());
    }

    return assignmentsBlock;
  }
}
