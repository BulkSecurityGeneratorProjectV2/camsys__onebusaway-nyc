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
package org.onebusaway.nyc.vehicle_tracking.impl.model;

import static org.junit.Assert.assertEquals;

import org.onebusaway.csv_entities.schema.BeanWrapper;
import org.onebusaway.csv_entities.schema.BeanWrapperFactory;
import org.onebusaway.csv_entities.schema.FieldMapping;
import org.onebusaway.nyc.vehicle_tracking.model.NycTestInferredLocationRecord;
import org.onebusaway.nyc.vehicle_tracking.model.csv.DateTimeFieldMappingFactory;

import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class DateTimeFieldMappingFactoryTest {

  @Test
  public void test() throws ParseException {

    final DateTimeFieldMappingFactory factory = new DateTimeFieldMappingFactory();
    final FieldMapping mapping = factory.createFieldMapping(null,
        NycTestInferredLocationRecord.class, "dt", "timestamp", Long.class,
        true);

    final NycTestInferredLocationRecord record = new NycTestInferredLocationRecord();
    record.setTimestamp(1284377940000L);
    final BeanWrapper obj = BeanWrapperFactory.wrap(record);
    final Map<String, Object> csvValues = new HashMap<String, Object>();

    mapping.translateFromObjectToCSV(null, obj, csvValues);

    final Object value = csvValues.get("dt");
    assertEquals("2010-09-13 07:39:00", value);
  }
}
