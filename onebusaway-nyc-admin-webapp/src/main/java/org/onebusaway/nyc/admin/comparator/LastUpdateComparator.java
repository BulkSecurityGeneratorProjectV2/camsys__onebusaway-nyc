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

package org.onebusaway.nyc.admin.comparator;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.onebusaway.nyc.admin.model.ui.VehicleStatus;

/**
 * Compares vehicle records on last reported time
 * @author abelsare
 *
 */
public class LastUpdateComparator implements Comparator<VehicleStatus>{

	private TimeComparator timeComparator;
	private DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
			
	public LastUpdateComparator(String order) {
		timeComparator = new TimeComparator(order);
	}

	@Override
	public int compare(VehicleStatus o1, VehicleStatus o2) {
		if(StringUtils.isBlank(o1.getLastUpdate())) {
			return 1;
		}
		if(StringUtils.isBlank(o2.getLastUpdate())) {
			return -1;
		}
		
		DateTime time1 = formatter.parseDateTime(o1.getLastUpdate());
		DateTime time2 = formatter.parseDateTime(o2.getLastUpdate());
		return timeComparator.compare(time1, time2);
	}
}
