/**
 * Copyright (c) 2011 Metropolitan Transportation Authority
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.nyc.report_archive.impl;

import static org.junit.Assert.*;

import org.onebusaway.nyc.report_archive.model.CcLocationReport;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

public class CcLocationReportDaoImplTest {

  private SessionFactory _sessionFactory;

  private CcLocationReportDaoImpl _dao;

  @Before
  public void setup() throws IOException {

    Configuration config = new AnnotationConfiguration();
    config = config.configure("org/onebusaway/nyc/report_archive/hibernate-configuration.xml");
    _sessionFactory = config.buildSessionFactory();

    _dao = new CcLocationReportDaoImpl();
    _dao.setSessionFactory(_sessionFactory);
  }

  @After
  public void teardown() {
    if (_sessionFactory != null)
      _sessionFactory.close();
  }

  @Test
  public void test() {

    assertEquals(0, _dao.getNumberOfReports());

    CcLocationReport report = new CcLocationReport();
    report.setDestSignCode(123);
    report.setDirectionDeg(new BigDecimal("10.1"));
    report.setLatitude(456);
    report.setLongitude(789);
    report.setManufacturerData("manufacturerData");
    report.setOperatorId(123);
    report.setOperatorIdDesignator("operatorIdDesignator");
    report.setRequestId(456);
    report.setRouteId(789);
    report.setRouteIdDesignator("routeIdDesignator");
    report.setRunId(123);
    report.setRunIdDesignator("runIdDesignator");
    report.setSpeed(10);
    report.setStatusInfo(456);
    report.setTimeReported(new Date());
    report.setVehicleAgencydesignator("vehicleAgencydesignator");
    report.setVehicleAgencyId(789);
    report.setVehicleId(120);
    
    
    
//    User userA = new User();
//    userA.setCreationTime(new Date());
//    userA.setProperties(new UserPropertiesV1());
//    userA.getRoles().add(userRole);

    _dao.saveOrUpdateReport(report);
    assertEquals(1, _dao.getNumberOfReports());

//    assertEquals(0, _dao.getNumberOfUsersWithRole(adminRole));
//    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));
//
//    User userB = new User();
//    userB.setCreationTime(new Date());
//    userB.setProperties(new UserPropertiesV1());
//    userB.getRoles().add(adminRole);
//
//    _dao.saveOrUpdateUser(userB);
//
//    assertEquals(1, _dao.getNumberOfUsersWithRole(adminRole));
//    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));
//
//    userA.getRoles().add(adminRole);
//
//    _dao.saveOrUpdateUser(userA);
//
//    assertEquals(2, _dao.getNumberOfUsersWithRole(adminRole));
//    assertEquals(1, _dao.getNumberOfUsersWithRole(userRole));
  }

//  @Test
//  public void testGetNumberOfUsers() {
//
//    assertEquals(0, _dao.getNumberOfUsers());
//
//    User userA = new User();
//    userA.setCreationTime(new Date());
//    userA.setProperties(new UserPropertiesV1());
//
//    _dao.saveOrUpdateUser(userA);
//
//    assertEquals(1, _dao.getNumberOfUsers());
//
//    User userB = new User();
//    userB.setCreationTime(new Date());
//    userB.setProperties(new UserPropertiesV1());
//
//    _dao.saveOrUpdateUser(userB);
//
//    assertEquals(2, _dao.getNumberOfUsers());
//  }
//
//  @Test
//  public void testGetAllUserIds() {
//
//    Set<Integer> ids = new HashSet<Integer>();
//
//    for (int i = 0; i < 100; i++) {
//
//      User userA = new User();
//      userA.setCreationTime(new Date());
//      userA.setProperties(new UserPropertiesV1());
//
//      _dao.saveOrUpdateUser(userA);
//
//      ids.add(userA.getId());
//    }
//
//    int n = _dao.getNumberOfUsers();
//    assertEquals(100, n);
//
//    Set<Integer> retreivedIds = new HashSet<Integer>();
//    final int limit = 20;
//
//    for (int i = 0; i < n; i += limit) {
//      List<Integer> pageOfIds = _dao.getAllUserIdsInRange(i, limit);
//      assertTrue(pageOfIds.size() <= limit);
//      retreivedIds.addAll(pageOfIds);
//    }
//
//    assertEquals(ids, retreivedIds);
//  }
//
//  @Test
//  public void deleteUser() {
//
//    UserRole userRole = new UserRole("user");
//
//    _dao.saveOrUpdateUserRole(userRole);
//
//    User user = new User();
//    user.setCreationTime(new Date());
//    user.setProperties(new UserPropertiesV2());
//    user.getRoles().add(userRole);
//
//    UserIndexKey key = new UserIndexKey("phone", "2065551234");
//
//    UserIndex index = new UserIndex();
//    index.setId(key);
//    index.setUser(user);
//    user.getUserIndices().add(index);
//
//    _dao.saveOrUpdateUser(user);
//
//    assertEquals(1, _dao.getNumberOfUsers());
//
//    UserIndex index2 = _dao.getUserIndexForId(key);
//    assertEquals(key, index2.getId());
//    assertEquals(user, index2.getUser());
//
//    _dao.deleteUser(user);
//
//    assertEquals(0, _dao.getNumberOfUsers());
//    index2 = _dao.getUserIndexForId(key);
//    assertNull(index2);
//  }
//
//  @Test
//  public void testTransitionUserIndex() {
//
//    User userA = new User();
//    userA.setCreationTime(new Date());
//    userA.setProperties(new UserPropertiesV2());
//
//    UserIndex index = new UserIndex();
//    index.setId(new UserIndexKey("test", "A"));
//    index.setUser(userA);
//    userA.getUserIndices().add(index);
//
//    _dao.saveOrUpdateUser(userA);
//
//    User userB = new User();
//    userB.setCreationTime(new Date());
//    userB.setProperties(new UserPropertiesV2());
//
//    _dao.saveOrUpdateUser(userB);
//
//    assertEquals(1, _dao.getUserForId(userA.getId()).getUserIndices().size());
//    assertEquals(0, _dao.getUserForId(userB.getId()).getUserIndices().size());
//
//    index.setUser(userB);
//    userA.getUserIndices().remove(index);
//    userB.getUserIndices().add(index);
//
//    _dao.saveOrUpdateUsers(userA, userB);
//
//    assertEquals(0, _dao.getUserForId(userA.getId()).getUserIndices().size());
//    assertEquals(1, _dao.getUserForId(userB.getId()).getUserIndices().size());
//  }
}
