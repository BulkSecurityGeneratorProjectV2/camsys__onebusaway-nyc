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

package org.onebusaway.nyc.transit_data_federation.impl.predictions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.onebusaway.nyc.transit_data_federation.services.predictions.PredictionCacheService;
import org.onebusaway.nyc.util.configuration.ConfigurationService;
import org.onebusaway.realtime.api.TimepointPredictionRecord;
import org.onebusaway.transit_data.model.trips.TripStatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Component
public class PredictionCacheServiceImpl implements PredictionCacheService {
	
	private static final int DEFAULT_CACHE_TIMEOUT = 2 * 60; // seconds
	private static Logger _log = LoggerFactory
			.getLogger(PredictionCacheServiceImpl.class);

	@Autowired
	private ConfigurationService _configurationService;

	private Cache<String, List<TimepointPredictionRecord>> _cache = null;
	
	private PredictionCacheServiceImpl(){}

	public Cache<String, List<TimepointPredictionRecord>> getCache() {
		return _cache;
	}
	
	@PostConstruct
	private void setup(){
		_cache = CacheBuilder.newBuilder()
        		.expireAfterWrite(DEFAULT_CACHE_TIMEOUT, TimeUnit.SECONDS).build();
	}
	
	@Override
	public List<TimepointPredictionRecord> getPredictionsForTrip(
			TripStatusBean tripStatus) {
		return getCache().getIfPresent(
				hash(tripStatus.getVehicleId(), tripStatus.getActiveTrip()
						.getId()));
	}
	
	@Override
	public List<TimepointPredictionRecord> getPredictionRecordsForVehicleAndTrip(
			String VehicleId, String TripId) {
		Cache<String, List<TimepointPredictionRecord>> cache = getCache();
		List<TimepointPredictionRecord> records = cache.getIfPresent(hash(VehicleId, TripId));
		return records;
	}

	private String hash(String vehicleId, String tripId) {
		return vehicleId + "-" + tripId;
	}
}
