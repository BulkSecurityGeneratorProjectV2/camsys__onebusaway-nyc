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

package org.onebusaway.nyc.vehicle_tracking.impl.queue;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.queue.model.RealtimeEnvelope;
import org.onebusaway.nyc.transit_data_federation.services.tdm.VehicleAssignmentService;
import org.onebusaway.nyc.vehicle_tracking.services.inference.VehicleLocationInferenceService;
import org.onebusaway.nyc.vehicle_tracking.services.queue.InputService;
import org.onebusaway.nyc.vehicle_tracking.services.queue.InputTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.ServletContextAware;

import tcip_final_3_0_5_1.CPTVehicleIden;
import tcip_final_3_0_5_1.CcLocationReport;

/**
 * Test/Debug Class for loading realtime data from the archive database 
 * as if it came from the queue.
 * 
 *  This class alters timestamps of the realtime data to make them appear as if they are
 *  current, and meters the flow of data to mimic the original flow.
 *  
 *  To enable this uncomment the bean definition in data-sources.xml.
 *
 */
public class FileInputTask implements ServletContextAware, InputTask{

  protected static Logger _log = LoggerFactory.getLogger(FileInputTask.class);
  
  private VehicleLocationInferenceService _vehicleLocationService;
  private InputService _inputService;
  
  /*
   * Obtain dump_raw.sql via:
   *  mysql -u username -ppassword -h database.host.com 
   *  -e "select raw_message from obanyc_cclocationreport where time_received > subtime(now(), '1:00:00') 
   *  order by time_received" onebusaway_nyc >dump_raw.sql
   */
  private String filename = "/tmp/dump_raw.sql";
  private String _depotPartitionKey;
  private ExecutorService _executorService = null;
  
  @Autowired
  public void setVehicleLocationService(
      VehicleLocationInferenceService vehicleLocationService) {
    _vehicleLocationService = vehicleLocationService;
  }
  
  @Autowired
  @Qualifier("fileInputService")
  public void setInputService(
      InputService inputService) {
	  _inputService = inputService;
  }
  
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  @Override
  public void setServletContext(ServletContext servletContext) {
    // check for depot partition keys in the servlet context
    if (servletContext != null) {
    	setDepotPartitionKey(servletContext.getInitParameter("depot.partition.key"));
      _log.info("servlet context provied depot.partition.key=" + _depotPartitionKey);
    }
  }
  
  public void setDepotPartitionKey(String depotPartitionKey) {
	  _depotPartitionKey = depotPartitionKey;
  }
  
  public String getDepotPartitionKey(){
	  return _depotPartitionKey;
  }

  @PostConstruct
  public void execute() {
	  _inputService.setDepotPartitionKey(_depotPartitionKey);
	  _executorService = Executors.newFixedThreadPool(1);
	  startInsertionThread(); 
  }
  
  @PreDestroy
  public void destroy() {
	  _executorService.shutdownNow();
  }
  
  public void startInsertionThread(){
	  _executorService.execute(new InsertionThread(_vehicleLocationService, _inputService, filename));
  }
  
  private class InsertionThread implements Runnable {
    private VehicleLocationInferenceService _vehicleLocationService;
    private String filename = null;
    private Reader inputReader = null;
    private StringBuffer currentRecord = new StringBuffer();
    private long startTime = System.currentTimeMillis();
    private long firstRecordTime = 0;
    private InputService inputService;

    public InsertionThread(VehicleLocationInferenceService vls, 
        InputService inputService,
        String filename) {
      this._vehicleLocationService = vls;
      this.inputService = inputService;
      this.filename = filename;
    }
    
    public void run() {
      try {
        open();
        while (next()) {
          RealtimeEnvelope record = parseCurrentRecord();
          postProcessResult(record);
          processResult(record);
        }
      } catch (Exception e) {
        _log.error("fail: ", e);
      }
    }

    private void postProcessResult(RealtimeEnvelope record) throws Exception {
      if (record == null)
    	return;
      // startTime -- program boot
      // now -- right now
      long now = System.currentTimeMillis();
      long localOffset = now - startTime;
      // firstRecordTime: lazy init to first record received time
      if (firstRecordTime == 0) {
        firstRecordTime = record.getTimeReceived();
      }
      long dataOffset = record.getTimeReceived()- firstRecordTime;
      while (dataOffset > localOffset) {
    	Thread.sleep(250);
        now = System.currentTimeMillis();
        localOffset = now - startTime;
      }
      
      record.setTimeReceived(now);
    }

    private void processResult(RealtimeEnvelope record) {
      if (inputService.acceptMessage(record)) {
        _vehicleLocationService.handleRealtimeEnvelopeRecord(record);
      }
      
    }
    private RealtimeEnvelope parseCurrentRecord() throws Exception {
      String s = currentRecord.toString();
      RealtimeEnvelope record = inputService.deserializeMessage(s);
      return record;
    }
    
    // advance record pointer
    private boolean next() throws Exception {
      currentRecord = new StringBuffer();
      int i;
      while ((i = inputReader.read()) > 0) {
        char c = (char) i;
        if (c == '\n') {
          currentRecord.append(c);
          // read last space
          inputReader.read();
          return true;
        } else {
          currentRecord.append(c);
        }
      }
      return false;
    }

    private void open() throws Exception {
    	inputReader = new FileReader(filename);
 
    }
  }
}
