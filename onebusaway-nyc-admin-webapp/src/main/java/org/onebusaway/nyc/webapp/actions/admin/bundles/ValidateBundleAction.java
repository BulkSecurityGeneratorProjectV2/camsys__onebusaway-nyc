package org.onebusaway.nyc.webapp.actions.admin.bundles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.onebusaway.nyc.util.configuration.ConfigurationServiceClient;
import org.onebusaway.nyc.webapp.actions.OneBusAwayNYCAdminActionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.onebusaway.nyc.admin.model.BundleValidateQuery;
import org.onebusaway.nyc.admin.model.BundleValidationCheckResult;
import org.onebusaway.nyc.admin.model.BundleValidationParseResults;
import org.onebusaway.nyc.admin.service.BuildBundleQueriesService;
import org.onebusaway.nyc.admin.service.BundleCheckParserService;

/**
 * Action class that holds properties and methods required to validate the data in the bundle.
 * @author abelsare
 * @author sheldonabrown
 * @author jpearson
 *
 */
@Namespace(value="/admin/bundles")
@Results({
  @Result(type = "redirectAction", name = "redirect", 
      params={"actionName", "validate-bundle"}),
  @Result(name="bundleValidationResults", type="json", 
      params={"root", "bundleValidationResults"})
})
public class ValidateBundleAction extends OneBusAwayNYCAdminActionSupport {
  private static Logger _log = LoggerFactory.getLogger(ManageBundlesAction.class);
  private static final long serialVersionUID = 1L;
    
  // Names of valid tests
  private static final String TEST_ROUTE = "route";
  private static final String TEST_ROUTE_SEARCH = "route search";
  private static final String TEST_SCHEDULE = "schedule";
  private static final String TEST_RT = "rt";
  private static final String TEST_SCHEDULE_DATE = "schedule-date";
  private static final String TEST_DELETED_ROUTE_SEARCH = "deleted route search";
  private static final String TEST_ROUTE_REVISION = "route revision";
  private static final String TEST_SATURDAY_SCHEDULE = "saturday schedule";
  private static final String TEST_SUNDAY_SCHEDULE = "sunday schedule";
  private static final String TEST_EXPRESS_INDICATOR = "express indicator";
  private static final String TEST_STOP_FOR_ROUTE = "stop for route";
  private static final String TEST_NOT_STOP_FOR_ROUTE = "not stop for route";
  private static final String TEST_STOP_DATE_AT_TIME = "stop date at time";
  private static final String TEST_NOT_STOP_DATE_AT_TIME = "not stop date at time";
  
  @Autowired
  private ConfigurationServiceClient _configurationServiceClient;
  
  @Autowired
  private BundleCheckParserService _bundleCheckParserService;
  
  @Autowired
  private BuildBundleQueriesService _buildBundleQueriesService;
  
  private String csvFile;
  private String checkEnvironment;
  private File csvDataFile;
  private List<BundleValidationCheckResult> bundleValidationResults = new ArrayList<BundleValidationCheckResult>();
  
  public String getCsvFile() {
    return csvFile;
  }
  
  public void setCsvFile(String csvFile) {
    this.csvFile = csvFile;
  }

  public String geCheckEnvironment() {
    return checkEnvironment;
  }
  
  public void setCheckEnvironment(String checkEnvironment) {
    this.checkEnvironment = checkEnvironment;
  }

  public File getCsvDataFile() {
    return csvDataFile;
  }
  
  public void setCsvDataFile(File csvDataFile) {
    this.csvDataFile = csvDataFile;
  }

  public List<BundleValidationCheckResult> getBundleValidationResults() {
    return this.bundleValidationResults;
  }

  @Override
  public String input() {
    _log.debug("in input");
    return SUCCESS;
  }

  @Override
  public String execute() {
    return SUCCESS;
  }

  /**
   * Uploads the bundle validate checks and uses them to test the validity of the bundle
   */
  public String runValidateBundle() {
    Path csvTarget = uploadCsvFile(csvFile);
    BundleValidationParseResults parseResults = _bundleCheckParserService.parseBundleChecksFile(csvTarget.toFile());
    List<BundleValidateQuery> queryResults = _buildBundleQueriesService.buildQueries(parseResults.getParsedBundleChecks(), checkEnvironment);
    for (BundleValidateQuery query : queryResults) {
      String queryString = query.getQuery();
      String queryResult = getQueryResult(query);
      query.setQueryResult(queryResult);
    }
    List<BundleValidationCheckResult> checkResults = checkResults(queryResults);
    try {
      Files.delete(csvTarget);
    } catch (IOException e) {
      _log.error("Exception while trying to delete temp .csv file");
      e.printStackTrace();
    }  
    bundleValidationResults = checkResults;         
    return "bundleValidationResults";
  }
    
  
  public Path uploadCsvFile(String csvFileName) {    
    Path csvTarget = null;
    try {
      csvTarget = Files.createTempFile("oba_", ".csv");
      _log.info("Temp file : " + csvTarget);
      csvTarget.toFile().deleteOnExit();
    } catch (IOException e) {
      _log.error("Exception trying to create temp .csv file");
      e.printStackTrace();
    }

    // Copy file
    try {
      Files.copy(csvDataFile.toPath(), csvTarget, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      _log.info(e.getMessage());
    }
    return csvTarget;
  }
 
  public String getQueryResult(BundleValidateQuery query) {
    String queryString = query.getQuery();
    String result = "";
    query.setErrorMessage("");
    try {
      URL apiURL = new URL(queryString);
      BufferedReader br = new BufferedReader(new InputStreamReader(apiURL.openStream()));
      String nextLine = "";
      while (null != (nextLine = br.readLine())) {
        result = nextLine;
      }
    } catch (MalformedURLException e) {
      query.setErrorMessage("MalformedURLException trying to get query results. ");
      _log.error("MalformedURLException getting QueryResults");
      _log.error(e.getMessage());
    } catch (IOException e) {
      query.setErrorMessage("IOException trying to get query results. ");
      _log.error("IOException getting QueryResults");
      _log.error(e.getMessage());
      _log.error("query string: " + queryString);
    }
    return result;
  }
  
  public List<BundleValidationCheckResult> checkResults(List<BundleValidateQuery> queryResults) {
    List<BundleValidationCheckResult> testResults = new ArrayList<BundleValidationCheckResult>();
    int linenum=1;
    for (BundleValidateQuery query : queryResults) {
      QueryResultChecker resultChecker = getResultChecker(query.getSpecificTest().toLowerCase());
      BundleValidationCheckResult checkResult = resultChecker.checkResults(query);
      checkResult.setLinenum(linenum);
      checkResult.setCsvLinenum(query.getLinenum());
      checkResult.setSpecificTest(query.getSpecificTest());
      checkResult.setTestQuery(query.getQuery());
      testResults.add(checkResult);
      ++linenum;
    }
    return testResults;
  }
    
  /* Private methods */
  
  private String getTargetURI(String checkEnvironment) {
    String targetURI = "";
    
    _log.info("checkEnvironment = " + checkEnvironment);
    try {
      List<Map<String, String>> components = _configurationServiceClient.getItems("config");
      if (components == null) {
        _log.info("getItems call failed");
      }
      for (Map<String, String> component: components) {
        _log.info("env = " + checkEnvironment + ", key = " + component.get("key"));
        if (checkEnvironment.equals("staging") && "apiStaging".equals(component.get("key"))) {
          _log.info("It's staging");
          targetURI = component.get("value");
        } else if (checkEnvironment.equals("prod") && "apiProd".equals(component.get("key"))) {
          _log.info("It's prod");
          targetURI = component.get("value");
        }
      }
    } catch (Exception e) {
      _log.error("Exception while trying to get environment host");
      e.printStackTrace();
    }
    
    return targetURI;
  }
  
  private QueryResultChecker getResultChecker(String test) {
    if (test.equals(TEST_SCHEDULE) || test.equals(TEST_SCHEDULE_DATE)) {
      return new ScheduleQueryResultChecker();
    } else if (test.equals(TEST_SATURDAY_SCHEDULE) || test.equals(TEST_SUNDAY_SCHEDULE)) {
      return new WeekendScheduleQueryResultChecker();
    } else if (test.equals(TEST_RT)) {
      return new RealtimeQueryResultChecker();
    } else if (test.equals(TEST_ROUTE) || test.equals(TEST_ROUTE_SEARCH) 
        || test.equals(TEST_ROUTE_REVISION) || test.equals(TEST_EXPRESS_INDICATOR)) {
      return new RouteQueryResultChecker();
    } else if (test.equals(TEST_DELETED_ROUTE_SEARCH)) {
      return new DeletedRouteQueryResultChecker();
    } else if (test.equals(TEST_STOP_FOR_ROUTE) || test.equals(TEST_NOT_STOP_FOR_ROUTE)) {
      return new StopForRouteResultChecker();
    } else if (test.equals(TEST_STOP_DATE_AT_TIME) || test.equals(TEST_NOT_STOP_DATE_AT_TIME)) {
      return new ScheduleTimeResultChecker();
    } else {
      return null;
    }
  }
}
