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

package org.onebusaway.nyc.ops.queue;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onebusaway.geospatial.model.CoordinatePoint;
import org.onebusaway.nyc.ops.services.CcAndInferredLocationService;
import org.onebusaway.nyc.report.services.CcAndInferredLocationDao;
import org.onebusaway.nyc.report.services.InferencePersistenceService;
import org.onebusaway.nyc.report.services.CcAndInferredLocationDao;
import org.onebusaway.nyc.report.services.RecordValidationService;
import org.onebusaway.nyc.transit_data.model.NycQueuedInferredLocationBean;
import org.onebusaway.nyc.transit_data.model.NycVehicleManagementStatusBean;
import org.onebusaway.nyc.transit_data.services.NycTransitDataService;
import org.onebusaway.transit_data.model.VehicleStatusBean;
import org.onebusaway.transit_data.model.realtime.VehicleLocationRecordBean;

public class OpsInferenceQueueListenerTaskTest {

	private OpsInferenceQueueListenerTask inferenceQueueListenertask;
	
	@Mock
	private NycQueuedInferredLocationBean inferredResult;
	
	@Mock
	private CcAndInferredLocationDao locationDao;
	
	@Mock
	private CcAndInferredLocationService locationService;
	
	@Mock
	private InferencePersistenceService persister;
	
	@Mock
	private NycTransitDataService nycTransitDataService;
	
	@Mock
	private RecordValidationService validationService;
	
	@Mock
	private NycVehicleManagementStatusBean managementBean;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(inferredResult.getVehicleId()).thenReturn("MTA NYCT_4123");
		when(inferredResult.getServiceDate()).thenReturn(1L);
		when(inferredResult.getRecordTimestamp()).thenReturn(1L);
		when(inferredResult.getManagementRecord()).thenReturn(managementBean);
		when(managementBean.getUUID()).thenReturn("123");
		when(inferredResult.getInferredLatitude()).thenReturn(1D);
		when(inferredResult.getInferredLongitude()).thenReturn(-1D);
		
		inferenceQueueListenertask = new OpsInferenceQueueListenerTask();
		inferenceQueueListenertask.setLocationDao(locationDao);
		inferenceQueueListenertask.setLocationService(locationService);
		inferenceQueueListenertask.setValidationService(validationService);
		inferenceQueueListenertask.setInferencePersistenceService(persister);	
	}

	@Test
	public void testInvalidInferredResult() {
		when(validationService.validateInferenceRecord(inferredResult)).thenReturn(false);	
		inferenceQueueListenertask.processResult(inferredResult, "");
		
	}
	
	
	@Test
	public void testPostProcessingInferredLatitude() {
		VehicleLocationRecordBean vehicleLocationRecord = mock(VehicleLocationRecordBean.class);
		CoordinatePoint currentLocation = new CoordinatePoint(-1000.0000, 74.0000);
		
		when(validationService.validateInferenceRecord(inferredResult)).thenReturn(true);
		
		when(nycTransitDataService.getVehicleForAgency(isA(String.class), isA(Long.class))).thenReturn(null);
		when(nycTransitDataService.getVehicleLocationRecordForVehicleId(isA(String.class), isA(Long.class)))
			.thenReturn(vehicleLocationRecord);
		
		when(vehicleLocationRecord.getCurrentLocation()).thenReturn(currentLocation);
		when(validationService.isValueWithinRange(-1000.0000, -999.999999, 999.999999)).thenReturn(false);
		when(validationService.isValueWithinRange(74.0000, -999.999999, 999.999999)).thenReturn(true);
		
		inferenceQueueListenertask.processResult(inferredResult, "");
		
	}
	
	@Test
	public void testPostProcessingInferredLongitude() {
		VehicleLocationRecordBean vehicleLocationRecord = mock(VehicleLocationRecordBean.class);
		CoordinatePoint currentLocation = new CoordinatePoint(-43.0000, 1000.0000);
		
		when(validationService.validateInferenceRecord(inferredResult)).thenReturn(true);
		
		when(nycTransitDataService.getVehicleForAgency(isA(String.class), isA(Long.class))).thenReturn(null);
		when(nycTransitDataService.getVehicleLocationRecordForVehicleId(isA(String.class), isA(Long.class)))
			.thenReturn(vehicleLocationRecord);
		
		when(vehicleLocationRecord.getCurrentLocation()).thenReturn(currentLocation);
		
		when(validationService.isValueWithinRange(-43.0000, -999.999999, 999.999999)).thenReturn(true);
		when(validationService.isValueWithinRange(1000.0000, -999.999999, 999.999999)).thenReturn(false);
		
		inferenceQueueListenertask.processResult(inferredResult, "");
		
	}
	
	@Test 
	public void testValidInferenceRecord() {
		VehicleLocationRecordBean vehicleLocationRecord = mock(VehicleLocationRecordBean.class);
		VehicleStatusBean vehicleStatusBean = new VehicleStatusBean();
		CoordinatePoint currentLocation = new CoordinatePoint(-43.0000, 70.0000);
		
		when(validationService.validateInferenceRecord(inferredResult)).thenReturn(true);
		
		when(nycTransitDataService.getVehicleForAgency(isA(String.class), isA(Long.class))).thenReturn(vehicleStatusBean);
		when(nycTransitDataService.getVehicleLocationRecordForVehicleId(isA(String.class), isA(Long.class)))
			.thenReturn(vehicleLocationRecord);
		
		when(vehicleLocationRecord.getCurrentLocation()).thenReturn(currentLocation);
		
		when(validationService.isValueWithinRange(-43.0000, -999.999999, 999.999999)).thenReturn(true);
		when(validationService.isValueWithinRange(70.0000, -999.999999, 999.999999)).thenReturn(true);
		
		inferenceQueueListenertask.processResult(inferredResult, "");
		
	}

}
