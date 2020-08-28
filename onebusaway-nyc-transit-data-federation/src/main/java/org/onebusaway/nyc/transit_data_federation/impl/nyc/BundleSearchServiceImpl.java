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

package org.onebusaway.nyc.transit_data_federation.impl.nyc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.security.auth.login.Configuration;

import org.onebusaway.container.refresh.Refreshable;
import org.onebusaway.geospatial.model.CoordinateBounds;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.nyc.transit_data.services.NycTransitDataService;
import org.onebusaway.nyc.transit_data_federation.services.nyc.BundleSearchService;
import org.onebusaway.nyc.util.configuration.ConfigurationService;
import org.onebusaway.transit_data.model.ListBean;
import org.onebusaway.transit_data.model.RouteBean;
import org.onebusaway.transit_data_federation.impl.RefreshableResources;
import org.onebusaway.transit_data_federation.services.AgencyAndIdLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Proposes suggestions to the user based on bundle content--e.g. stop ID and route short names.
 *
 * @author asutula
 *
 */
@Component("NycBundleSearchService")
public class BundleSearchServiceImpl implements BundleSearchService {

	@Autowired
	private NycTransitDataService _transitDataService = null;

	@Autowired
	private ConfigurationService _configurationService;

	private Map<String,List<String>> suggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());

	private boolean _disableInit = false;

	private int maxSearchResults = 20;

	@PostConstruct
	@Refreshable(dependsOn = {
			RefreshableResources.ROUTE_COLLECTIONS_DATA,
			RefreshableResources.TRANSIT_GRAPH })
	public void init() {
		if (_disableInit)
			return;
		Runnable initThread = new Runnable() {
			@Override
			public void run() {
				Map<String,List<String>> tmpSuggestions = Collections.synchronizedMap(new HashMap<String, List<String>>());


				Map<String, List<CoordinateBounds>> agencies = _transitDataService.getAgencyIdsWithCoverageArea();
				for (String agency : agencies.keySet()) {
					ListBean<RouteBean> routes = _transitDataService.getRoutesForAgencyId(agency);
					for (RouteBean route : routes.getList()) {
						String shortName = route.getShortName();
						generateInputsForString(tmpSuggestions, shortName, "\\s+");
					}

					ListBean<String> stopIds = _transitDataService.getStopIdsForAgencyId(agency);
					for (String stopId : stopIds.getList()) {
						if (_transitDataService.stopHasRevenueService(agency, stopId)) {
							AgencyAndId agencyAndId = AgencyAndIdLibrary.convertFromString(stopId);
							generateInputsForString(tmpSuggestions, agencyAndId.getId(), null);
						}
					}
				}
				suggestions = tmpSuggestions;
			}
		};

		new Thread(initThread).start();
	}

	public int getMaxSearchResults() {
		return maxSearchResults;
	}

	public void setMaxSearchResults(int maxSearchResults) {
		this.maxSearchResults = maxSearchResults;
	}

	@SuppressWarnings("unused")
	@Refreshable(dependsOn = "tdm.maxSearchResults")
	private void configChanged() {
		Integer maxSearchResults = _configurationService.getConfigurationValueAsInteger("tdm.maxSearchResults", 20);

		if (maxSearchResults != null) {
			setMaxSearchResults(maxSearchResults);
		}
	}

	private void generateInputsForString(Map<String,List<String>> tmpSuggestions, String string, String splitRegex) {
		String[] parts;
		if (splitRegex != null)
			parts = string.split(splitRegex);
		else
			parts = new String[] {string};
		for (String part : parts) {
			int length = part.length();
			for (int i = 0; i < length; i++) {
				String key = part.substring(0, i+1).toLowerCase();
				List<String> suggestion = tmpSuggestions.get(key);
				if (suggestion == null) {
					suggestion = new ArrayList<String>();
				}
				suggestion.add(string);
				Collections.sort(suggestion);
				tmpSuggestions.put(key, suggestion);
			}
		}
	}

	@Override
	public List<String> getSuggestions(String input) {
		List<String> tmpSuggestions = this.suggestions.get(input);
		if (tmpSuggestions == null)
			tmpSuggestions = new ArrayList<String>();
		if (tmpSuggestions.size() > getMaxSearchResults())
			tmpSuggestions = tmpSuggestions.subList(0, getMaxSearchResults());
		return tmpSuggestions;
	}

	public void setDisableInit(boolean disableInit) {
		_disableInit = disableInit;
	}
}
