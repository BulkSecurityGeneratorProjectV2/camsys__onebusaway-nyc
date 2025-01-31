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

package org.onebusaway.nyc.admin.search.impl;

import org.apache.commons.lang.StringUtils;
import org.onebusaway.nyc.admin.model.ui.VehicleStatus;
import org.onebusaway.nyc.admin.search.Filter;

/**
 * Filters vehicles that are inferred in revenue service i.e whose inferred state is either
 * IN PROGRESS or LAYOVER_*
 * @author abelsare
 *
 */
public class RevenueServiceFilter implements Filter<VehicleStatus> {

	@Override
	public boolean apply(VehicleStatus type) {
		if(StringUtils.isNotBlank(type.getInferredPhase())) {
			if(type.getInferredPhase().equalsIgnoreCase("IN PROGRESS") ||
					type.getInferredPhase().startsWith("LAY")) {
				return true;
			}
		}
		return false;
	}

}
