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
 * This test is to check that informal pickup of in-progress occurs as expected.
 * 
 * @author bwillard 
 *
 */

public class Trace_3819_20120829_220004_222001_IntegrationTest extends AbstractTraceRunner {

  public Trace_3819_20120829_220004_222001_IntegrationTest() throws Exception {
    super("3819-pickup-labeled.csv");
    setBundle("2012July_r04_b02", "2012-08-28T00:00:00EDT");
  }
}
