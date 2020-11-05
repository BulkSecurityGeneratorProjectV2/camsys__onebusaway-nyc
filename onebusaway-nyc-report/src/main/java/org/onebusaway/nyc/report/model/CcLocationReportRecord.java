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

package org.onebusaway.nyc.report.model;

import org.onebusaway.nyc.queue.model.RealtimeEnvelope;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import tcip_final_3_0_5_1.CcLocationReport;
import tcip_final_3_0_5_1.SPDataQuality;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import static org.onebusaway.nyc.util.impl.queue.CCLocationRecordUtil.*;

@Entity
@Table(name = "obanyc_cclocationreport")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class CcLocationReportRecord implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(generator = "increment")
  @GenericGenerator(name = "increment", strategy = "increment")
  @AccessType("property")
  private Long id;

  @Index(name = "UUID")
  @Column(nullable = false, name = "UUID", length = 36)
  private String uuid;

  @Column(nullable = false, name = "request_id")
  private Integer requestId;

  @Column(nullable = false, name = "vehicle_id")
  @Index(name = "vehicle_id")
  private Integer vehicleId;

  @Column(nullable = false, name = "vehicle_agency_id")
  private Integer vehicleAgencyId;

  @Column(nullable = false, name = "vehicle_agency_designator", length = 64)
  private String vehicleAgencyDesignator;

  @Column(nullable = false, name = "time_reported")
  @Index(name = "time_reported")
  private Date timeReported;

  // this is the system time received -- when the queue first saw it
  @Column(nullable = false, name = "time_received")
  @Index(name = "time_received")
  private Date timeReceived;

  @Column(nullable = false, name = "archive_time_received")
  @Index(name = "archive_time_received")
  private Date archiveTimeReceived;

  @Column(nullable = false, columnDefinition = "DECIMAL(9,6)", name = "latitude")
  private BigDecimal latitude;

  @Column(nullable = false, columnDefinition = "DECIMAL(9,6)", name = "longitude")
  private BigDecimal longitude;

  @Column(nullable = false, columnDefinition = "DECIMAL(5,2)", name = "direction_deg")
  private BigDecimal directionDeg;

  @Column(nullable = false, columnDefinition = "DECIMAL(4,1)", name = "speed")
  private BigDecimal speed;

  @Column(name = "data_quality_qualitative_indicator")
  private Byte dataQuality;

  @Column(nullable = false, name = "manufacturer_data", length = 64)
  private String manufacturerData;

  @Column(nullable = false, name = "operator_id_designator", length = 16)
  private String operatorIdDesignator;

  @Column(nullable = false, name = "run_id_designator", length = 32)
  private String runIdDesignator;

  @Column(nullable = false, name = "dest_sign_code")
  private Integer destSignCode;

  @Column(name = "emergency_code", length = 1)
  private String emergencyCode;

  @Column(nullable = false, name = "route_id_designator", length = 16)
  private String routeIdDesignator;

  @Column(name = "nmea_sentence_gpgga", length = 160)
  private String nmeaSentenceGPGGA;

  @Column(name = "nmea_sentence_gprmc", length = 160)
  private String nmeaSentenceGPRMC;

  @Column(nullable = false, name = "raw_message", length = 1400)
  private String rawMessage;
  
  @Column(nullable = true, name = "vehicle_power_state", length = 1)
  private Integer vehiclePowerState;

  public CcLocationReportRecord() {
  }

  public CcLocationReportRecord(RealtimeEnvelope envelope, String contents,
      String zoneOffset) {
    super();
    if (envelope == null || envelope.getCcLocationReport() == null)
      return; // deserialization failure, abort
    setUUID(envelope.getUUID());
    CcLocationReport message = envelope.getCcLocationReport();

    setRequestId((int) message.getRequestId());

    // Data Quality requires special handling
    SPDataQuality quality = message.getDataQuality();
    Byte qualityValue = null;
    if (quality != null) {
      String indicator = quality.getQualitativeIndicator();

      if (indicator != null) {
        qualityValue = new Byte(indicator);
      }
    }
    setDataQuality(qualityValue);

    setDestSignCode(message.getDestSignCode().intValue());
    if (message.getEmergencyCodes() != null
        && message.getEmergencyCodes().getEmergencyCode().size() > 0) {
      setEmergencyCode(message.getEmergencyCodes().getEmergencyCode().get(0));
    }

    setDirectionDeg(message.getDirection().getDeg());
    setLatitude(convertMicrodegreesToDegrees(message.getLatitude()));
    setLongitude(convertMicrodegreesToDegrees(message.getLongitude()));
    setManufacturerData(message.getManufacturerData());
    setOperatorIdDesignator(message.getOperatorID().getDesignator());
    setRouteIdDesignator(message.getRouteID().getRouteDesignator());
    setRunIdDesignator(message.getRunID().getDesignator());
    setSpeed(convertSpeed(message.getSpeed()));
    setTimeReported(convertTime(message.getTimeReported(), zoneOffset));
    setArchiveTimeReceived(new Date(System.currentTimeMillis()));
    setTimeReceived(new Date(envelope.getTimeReceived()));
    setVehicleAgencyDesignator(message.getVehicle().getAgencydesignator());
    setVehicleAgencyId(message.getVehicle().getAgencyId().intValue());
    setVehicleId((int) message.getVehicle().getVehicleId());
    
    // Check for localCcLocationReport and extract sentences if available
    String gpggaSentence = null;
    String gprmcSentence = null;
    if (message.getLocalCcLocationReport() != null){
		if ((message.getLocalCcLocationReport().getNMEA() != null)
	    && (message.getLocalCcLocationReport().getNMEA().getSentence() != null)) {
	      for (String s : message.getLocalCcLocationReport().getNMEA().getSentence()) {
	        if (s != null) {
	          if (s.indexOf("$GPGGA") != -1) {
	            gpggaSentence = s;
	          } else if (s.indexOf("$GPRMC") != -1) {
	            gprmcSentence = s;
	          }
	        }
	      }
		}
		//Vehicle Power State
    	setVehiclePowerState(message.getLocalCcLocationReport().getVehiclePowerState());
    }
    setNmeaSentenceGPGGA(gpggaSentence);
    setNmeaSentenceGPRMC(gprmcSentence);
    // Check this.
    setRawMessage(contents);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public Integer getRequestId() {
    return requestId;
  }

  public void setRequestId(Integer requestId) {
    this.requestId = requestId;
  }

  public Integer getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(Integer vehicleId) {
    this.vehicleId = vehicleId;
  }

  public Integer getVehicleAgencyId() {
    return vehicleAgencyId;
  }

  public void setVehicleAgencyId(Integer vehicleAgencyId) {
    this.vehicleAgencyId = vehicleAgencyId;
  }

  public String getVehicleAgencyDesignator() {
    return vehicleAgencyDesignator;
  }

  public void setVehicleAgencyDesignator(String vehicleAgencyDesignator) {
    this.vehicleAgencyDesignator = vehicleAgencyDesignator;
  }

  public Date getTimeReported() {
    return timeReported;
  }

  public void setTimeReported(Date timeReported) {
    this.timeReported = timeReported;
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public void setLatitude(BigDecimal latitude) {
    this.latitude = latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  public void setLongitude(BigDecimal longitude) {
    this.longitude = longitude;
  }

  public BigDecimal getDirectionDeg() {
    return directionDeg;
  }

  public void setDirectionDeg(BigDecimal directionDeg) {
    this.directionDeg = directionDeg;
  }

  public BigDecimal getSpeed() {
    return speed;
  }

  public void setSpeed(BigDecimal speed) {
    this.speed = speed;
  }

  public Byte getDataQuality() {
    return dataQuality;
  }

  public void setDataQuality(Byte dataQuality) {
    this.dataQuality = dataQuality;
  }

  public String getManufacturerData() {
    return manufacturerData;
  }

  public void setManufacturerData(String manufacturerData) {
    this.manufacturerData = manufacturerData;
  }

  public String getOperatorIdDesignator() {
    return operatorIdDesignator;
  }

  public void setOperatorIdDesignator(String operatorIdDesignator) {
    this.operatorIdDesignator = operatorIdDesignator;
  }

  public String getRunIdDesignator() {
    return runIdDesignator;
  }

  public void setRunIdDesignator(String runIdDesignator) {
    this.runIdDesignator = runIdDesignator;
  }

  public Integer getDestSignCode() {
    return destSignCode;
  }

  public void setDestSignCode(Integer destSignCode) {
    this.destSignCode = destSignCode;
  }

  public String getEmergencyCode() {
    return emergencyCode;
  }

  public void setEmergencyCode(String emergencyCode) {
    this.emergencyCode = emergencyCode;
  }

  public String getRouteIdDesignator() {
    return routeIdDesignator;
  }

  public void setRouteIdDesignator(String routeIdDesignator) {
    this.routeIdDesignator = routeIdDesignator;
  }

  public String getNmeaSentenceGPGGA() {
    return nmeaSentenceGPGGA;
  }

  public void setNmeaSentenceGPGGA(String nmeaSentenceGPGGA) {
    this.nmeaSentenceGPGGA = nmeaSentenceGPGGA;
  }

  public String getNmeaSentenceGPRMC() {
    return nmeaSentenceGPRMC;
  }

  public void setNmeaSentenceGPRMC(String nmeaSentenceGPRMC) {
    this.nmeaSentenceGPRMC = nmeaSentenceGPRMC;
  }

  public Date getTimeReceived() {
    return timeReceived;
  }

  public void setTimeReceived(Date timeReceived) {
    this.timeReceived = timeReceived;
  }

  public Date getArchiveTimeReceived() {
    return archiveTimeReceived;
  }

  public void setArchiveTimeReceived(Date archiveTimeReceived) {
    this.archiveTimeReceived = archiveTimeReceived;
  }

  public String getRawMessage() {
    return rawMessage;
  }

  public void setRawMessage(String rawMessage) {
    this.rawMessage = rawMessage;
  }

public Integer getVehiclePowerState() {
	return vehiclePowerState;
}

public void setVehiclePowerState(Integer vehiclePowerState) {
	this.vehiclePowerState = vehiclePowerState;
}
}
