/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
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
package org.onebusaway.nyc.transit_data_federation.impl.bundle;

/**
 * A collection of resources that can be notified upon bundle changes that 
 * they should refresh their data. This is an extension of an OBA paradigm. 
 * 
 * @author jmaki
 *
 */
public final class NycRefreshableResources {

  public static final String NON_REVENUE_MOVES_DATA = "__non_revenue_moves_data";
  
  public static final String NON_REVENUE_STOP_DATA = "__non_revenue_stop_data";

  public static final String DESTINATION_SIGN_CODE_DATA = "__dsc_data";

  public static final String TERMINAL_DATA = "__terminal_data";

  public static final String RUN_DATA = "__run_data";

  public static final String SUPPLIMENTAL_TRIP_DATA = "__supplimental_trip_info";

  public static final String BUSTREKDATA_REMARK = "__bustrekdata_remark";

  public static final String BUSTREKDATA_TRIP_INFO = "__bustrekdata_trip_info";

  public static final String BUSTREKDATA_TIME_POINT = "__bustrekdata_time_point";

  private NycRefreshableResources() {}

}
