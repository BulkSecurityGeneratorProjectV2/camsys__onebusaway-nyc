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

package org.onebusaway.nyc.transit_data_manager.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "obanyc_vehiclepullout")
@AccessType("field")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class VehiclePipoRecord implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(generator = "increment")
	@GenericGenerator(name = "increment", strategy = "increment")
	@AccessType("property")
	private Integer id;

	@Column(nullable = false, name = "vehicle_id")
	@Index(name = "vehicle_id")
	private Integer vehicleId;

	@Column(name = "agency_id_tcip")
	private Integer agencyIdTcip;

	@Column(name = "agency_id", length = 64)
	private String agencyId;

	@Column(name = "depot_id", length = 16)
	private String depotId;

	@Column(nullable = false, name = "service_date")
	@Temporal(TemporalType.DATE)
	private Date serviceDate;

	@Column(name = "pullout_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date pulloutTime;

	@Column(name= "run")
	private String run;

	@Column(name = "operator_id", length = 16)
	private String operatorId;

	@Column(name = "pullin_time")
	@Temporal(TemporalType.TIMESTAMP)
	private Date pullinTime;
	
	//No-arg constructor required by Hibernate
	public VehiclePipoRecord() {
		
	}
	
	@Override
	public boolean equals(Object vehicleRecordObj) {
		if(vehicleRecordObj == null) {
			return false;
		}
		if(this == vehicleRecordObj) {
			return true;
		}
		if(vehicleRecordObj.getClass() != getClass()) {
			return false;
		}
		
		VehiclePipoRecord vehicleRecord = (VehiclePipoRecord) vehicleRecordObj;
		
		return new EqualsBuilder().append(vehicleId, vehicleRecord.vehicleId)
				.append(agencyId, vehicleRecord.agencyId).isEquals();
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(vehicleId).append(agencyId).toHashCode();
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the vehicleId
	 */
	public Integer getVehicleId() {
		return vehicleId;
	}

	/**
	 * @param vehicleId the vehicleId to set
	 */
	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}

	/**
	 * @return the depotId
	 */
	public String getDepotId() {
		return depotId;
	}

	/**
	 * @param depotId the depotId to set
	 */
	public void setDepotId(String depotId) {
		this.depotId = depotId;
	}

	/**
	 * @return the serviceDate
	 */
	public Date getServiceDate() {
		return serviceDate;
	}

	/**
	 * @param serviceDate the serviceDate to set
	 */
	public void setServiceDate(Date serviceDate) {
		this.serviceDate = serviceDate;
	}

	/**
	 * @return the pulloutTime
	 */
	public Date getPulloutTime() {
		return pulloutTime;
	}

	/**
	 * @param pulloutTime the pulloutTime to set
	 */
	public void setPulloutTime(Date pulloutTime) {
		this.pulloutTime = pulloutTime;
	}

	/**
	 * @return the run
	 */
	public String getRun() {
		return run;
	}

	/**
	 * @param run the run to set
	 */
	public void setRun(String run) {
		this.run = run;
	}

	/**
	 * @return the operatorId
	 */
	public String getOperatorId() {
		return operatorId;
	}

	/**
	 * @param operatorId the operatorId to set
	 */
	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	/**
	 * @return the pullinTime
	 */
	public Date getPullinTime() {
		return pullinTime;
	}

	/**
	 * @param pullinTime the pullinTime to set
	 */
	public void setPullinTime(Date pullinTime) {
		this.pullinTime = pullinTime;
	}

	/**
	 * @return the agencyIdTcip
	 */
	public Integer getAgencyIdTcip() {
		return agencyIdTcip;
	}

	/**
	 * @param agencyIdTcip the agencyIdTcip to set
	 */
	public void setAgencyIdTcip(Integer agencyIdTcip) {
		this.agencyIdTcip = agencyIdTcip;
	}

	/**
	 * @return the agencyId
	 */
	public String getAgencyId() {
		return agencyId;
	}

	/**
	 * @param agencyId the agencyId to set
	 */
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
}
