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
package org.onebusaway.nyc.integration_tests.vehicle_tracking_webapp.cases;

import org.onebusaway.nyc.integration_tests.vehicle_tracking_webapp.AbstractTraceRunner;

/**
 * OBANYC-1807
 * "Looks basically the same as the OBANYC-1797 on #324. around 13:10 it was departing 
 * eastern terminus with assigned_run_id of BX11-6 but the next outbound trip from that 
 * terminal on that run is at 14:58. Should be informal instead."
 * 
 * @author bwillard 
 *
 */
public class Trace_7726_20121010_IntegrationTest extends AbstractTraceRunner {

  public Trace_7726_20121010_IntegrationTest() throws Exception {
    super("7726-informal-labeled.csv");
    setBundle("2012September_Bronx_r10_b03", "2012-09-03T01:00:00EDT");
  }
}
